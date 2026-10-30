package dev.arsfusion;

import dev.arsfusion.api.ArmorDonors;
import dev.arsfusion.armor.ArmorFusion;
import dev.arsfusion.config.FusionConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/** Native item modifier/codec integration with no Iron linkage. */
public final class AutomaticFusionCases {
    public record Stat(String attribute, double amount, int operation, boolean defense) {}
    private AutomaticFusionCases() {}

    public static void verify(GameTestHelper h, ItemStack donor,
                              Function<ItemStack, List<Stat>> stats, UnaryOperator<ItemStack> roundTrip) {
        var source=BuiltInRegistries.ITEM.stream().map(ItemStack::new)
                .filter(s->ArmorFusion.ars(s)&&((ArmorItem)s.getItem()).getType()==ArmorItem.Type.CHESTPLATE)
                .findFirst().orElseThrow();
        boolean enabled=FusionConfig.ARMOR_FUSION_ENABLED.get();
        boolean automatic=FusionConfig.AUTOMATIC_ARMOR_COMPATIBILITY.get();
        boolean toughness=FusionConfig.TOUGHNESS_ENABLED.get();
        try {
            FusionConfig.ARMOR_FUSION_ENABLED.set(true);
            FusionConfig.AUTOMATIC_ARMOR_COMPATIBILITY.set(true);
            FusionConfig.TOUGHNESS_ENABLED.set(false);
            h.assertTrue(ArmorDonors.manualAdapter(donor)==null,"Unregistered native armor uses automatic fallback");
            h.assertTrue(!ArmorFusion.canFuse(source,source),"Ars armor remains the recipient, not an automatic donor");
            source.setDamageValue(17);
            var result=ArmorFusion.fuse(source,donor);
            h.assertTrue(!result.isEmpty()&&result.is(source.getItem())&&result.getDamageValue()==17,"Keep the Ars item and durability");
            h.assertTrue(!ArmorFusion.has(source)&&!ArmorFusion.has(donor),"Never modify either input");
            var expected=new ArrayList<>(stats.apply(source));
            stats.apply(donor).stream().filter(s->!s.defense()).forEach(expected::add);
            var actual=stats.apply(result);
            h.assertTrue(actual.size()==expected.size()&&actual.containsAll(expected),"Copy non-defense modifiers with original amounts and operations, exactly once");
            h.assertTrue(!ArmorFusion.canFuse(result,donor),"Automatic fusion also allows one donor only");
            var saved=roundTrip.apply(result);
            h.assertTrue(ArmorFusion.donor(saved).is(donor.getItem()),"Automatic donor survives persistent and network codecs");
            FusionConfig.AUTOMATIC_ARMOR_COMPATIBILITY.set(false);
            h.assertTrue(!ArmorFusion.canFuse(source,donor),"Fallback toggle prevents new unregistered donors");
            var retained=stats.apply(saved);
            h.assertTrue(retained.size()==expected.size()&&retained.containsAll(expected),"Fallback toggle never strips existing fused bonuses");
            FusionConfig.ARMOR_FUSION_ENABLED.set(false);
            retained=stats.apply(saved);
            h.assertTrue(retained.size()==expected.size()&&retained.containsAll(expected),"Master toggle prevents crafting, not existing equipment bonuses");
        } finally {
            FusionConfig.ARMOR_FUSION_ENABLED.set(enabled);
            FusionConfig.AUTOMATIC_ARMOR_COMPATIBILITY.set(automatic);
            FusionConfig.TOUGHNESS_ENABLED.set(toughness);
        }
        h.succeed();
    }
    public static void netheriteAndBlacklist(GameTestHelper h, Function<ItemStack,List<Stat>> stats) {
        var source=BuiltInRegistries.ITEM.stream().map(ItemStack::new)
                .filter(v->ArmorFusion.ars(v)&&((ArmorItem)v.getItem()).getType()==ArmorItem.Type.CHESTPLATE)
                .findFirst().orElseThrow();
        boolean enabled=FusionConfig.ARMOR_FUSION_ENABLED.get();
        boolean auto=FusionConfig.AUTOMATIC_ARMOR_COMPATIBILITY.get();
        boolean toughness=FusionConfig.TOUGHNESS_ENABLED.get();
        var blacklist=FusionConfig.ATTRIBUTE_BLACKLIST.get();
        try {
            FusionConfig.ARMOR_FUSION_ENABLED.set(true);FusionConfig.AUTOMATIC_ARMOR_COMPATIBILITY.set(true);FusionConfig.TOUGHNESS_ENABLED.set(false);
            FusionConfig.ATTRIBUTE_BLACKLIST.set(FusionConfig.ATTRIBUTE_BLACKLIST.getDefault());
            h.assertTrue(!FusionConfig.DISABLE_MANUAL_COMPATIBILITY.getDefault(),"Dedicated compatibility is preserved by default");
            var donor=new ItemStack(net.minecraft.world.item.Items.NETHERITE_CHESTPLATE);
            var nativeBonuses=stats.apply(donor).stream().filter(v->!v.defense()).toList();
            h.assertTrue(nativeBonuses.size()==1&&nativeBonuses.get(0).attribute().contains("knockback_resistance"),"Vanilla netherite contributes knockback resistance only");
            var result=ArmorFusion.fuse(source,donor);
            var expected=new ArrayList<>(stats.apply(source));expected.addAll(nativeBonuses);
            h.assertTrue(stats.apply(result).size()==expected.size()&&stats.apply(result).containsAll(expected),"Default blacklist excludes netherite armor and toughness");
            var blocked=new ArrayList<String>(FusionConfig.ATTRIBUTE_BLACKLIST.getDefault());blocked.add(nativeBonuses.get(0).attribute());
            FusionConfig.ATTRIBUTE_BLACKLIST.set(blocked);
            h.assertTrue(stats.apply(result).equals(stats.apply(source)),"Configured blacklist also removes donor knockback resistance");
            FusionConfig.ATTRIBUTE_BLACKLIST.set(List.of());
            expected=new ArrayList<>(stats.apply(source));expected.addAll(stats.apply(donor));
            h.assertTrue(stats.apply(result).size()==expected.size()&&stats.apply(result).containsAll(expected),"Empty blacklist permits all donor modifiers, including defense");
        } finally {
            FusionConfig.ATTRIBUTE_BLACKLIST.set(blacklist);FusionConfig.ARMOR_FUSION_ENABLED.set(enabled);
            FusionConfig.AUTOMATIC_ARMOR_COMPATIBILITY.set(auto);FusionConfig.TOUGHNESS_ENABLED.set(toughness);
        }
        h.succeed();
    }
}
