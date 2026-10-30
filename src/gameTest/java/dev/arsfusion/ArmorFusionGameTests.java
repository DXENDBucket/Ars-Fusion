package dev.arsfusion;

import com.hollingsworth.arsnouveau.common.crafting.recipes.ApparatusRecipeInput;
import com.hollingsworth.arsnouveau.common.crafting.recipes.ArmorUpgradeRecipe;
import dev.arsfusion.armor.ArmorFusion;
import dev.arsfusion.config.FusionConfig;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder("ars_fusion_iron") @PrefixGameTestTemplate(false)
public final class ArmorFusionGameTests {
    @GameTest(template="empty") public static void nativeAnvilAndPolicies(GameTestHelper h) {
        ArmorFusionCases.anvil(h, TestPlayers.player(h));
    }
    @GameTest(template="empty") public static void nativeInfusionUpgradeAndSync(GameTestHelper h) {
        ArmorFusionCases.infusionAndUpgrade(h, TestPlayers.player(h),
                s -> new ArmorUpgradeRecipe(List.of(), 0, 1)
                        .assemble(new ApparatusRecipeInput(s, List.of(), null), h.getLevel().registryAccess()),
                s -> {
                    var saved = ItemStack.parse(h.getLevel().registryAccess(), s.save(h.getLevel().registryAccess())).orElseThrow();
                    var buf = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), h.getLevel().registryAccess());
                    try { ItemStack.STREAM_CODEC.encode(buf, saved); return ItemStack.STREAM_CODEC.decode(buf); }
                    finally { buf.release(); }
                });
    }
    private record Stat(String attribute, double amount, int operation) {}
    private static List<Stat> stats(ItemStack s) {
        var result = new ArrayList<Stat>();
        s.forEachModifier(ArmorFusion.slot(s), (a,m) -> result.add(new Stat(BuiltInRegistries.ATTRIBUTE.getKey(a.value()).toString(), m.amount(), m.operation().ordinal())));
        return result;
    }
    @GameTest(template="empty") public static void attributesAndTierToughness(GameTestHelper h) {
        ArmorFusionCases.enabled(() -> {
            double old = FusionConfig.ARS_ARMOR_TOUGHNESS_PER_TIER.get();
            try {
                FusionConfig.TOUGHNESS_ENABLED.set(true);
                FusionConfig.ARS_ARMOR_TOUGHNESS_PER_TIER.set(0.0);
                for (var type : List.of(ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE, ArmorItem.Type.LEGGINGS, ArmorItem.Type.BOOTS)) {
                    var source = ArmorFusionCases.ars(type); var donor = ArmorFusionCases.iron(type);
                    var result = ArmorFusion.fuse(source, donor);
                    var expected = new ArrayList<>(stats(source));
                    stats(donor).stream().filter(s -> !s.attribute.equals("minecraft:generic.armor") && !s.attribute.equals("minecraft:generic.armor_toughness"))
                            .forEach(expected::add);
                    var actual = stats(result);
                    h.assertTrue(actual.size() == expected.size() && actual.containsAll(expected), "Only non-armor donor attributes are added: " + type);
                    FusionConfig.ARS_ARMOR_TOUGHNESS_PER_TIER.set(1.0);
                    for (int tier=0; tier<3; tier++) {
                        var upgraded = tier == 0 ? result : new ArmorUpgradeRecipe(List.of(), 0, tier)
                                .assemble(new ApparatusRecipeInput(result.copy(), List.of(), null), h.getLevel().registryAccess());
                        double expectedBonus = tier+1;
                        var ids = new HashSet<String>();
                        upgraded.forEachModifier(ArmorFusion.slot(upgraded), (a,m) -> {
                            h.assertTrue(ids.add(a.getRegisteredName()+"/"+m.id()), "Modifier IDs do not collide");
                            if (m.id().getPath().startsWith("ars_toughness/")) h.assertTrue(a == Attributes.ARMOR_TOUGHNESS && m.amount() == expectedBonus, "Live tier toughness");
                        });
                        h.assertTrue(ArmorFusion.toughness(upgraded) == expectedBonus, "Displayed tier controls bonus");
                    }
                    FusionConfig.ARS_ARMOR_TOUGHNESS_PER_TIER.set(0.0);
                }
            } finally { FusionConfig.TOUGHNESS_ENABLED.set(false); FusionConfig.ARS_ARMOR_TOUGHNESS_PER_TIER.set(old); }
        });
        h.succeed();
    }
    @GameTest(template="empty") public static void legacyFusionCompatibility(GameTestHelper h) {
        ArmorFusionCases.enabled(() -> {
            var source=ArmorFusionCases.ars(ArmorItem.Type.CHESTPLATE);
            var donor=ArmorFusionCases.iron(ArmorItem.Type.CHESTPLATE);
            @SuppressWarnings("unchecked") var type=(net.minecraft.core.component.DataComponentType<net.minecraft.world.item.component.ItemContainerContents>)BuiltInRegistries.DATA_COMPONENT_TYPE.get(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("irons_nouveau","armor_fusion"));
            h.assertTrue(type!=null,"Historical codec registered exactly once");
            source.set(type,net.minecraft.world.item.component.ItemContainerContents.fromItems(List.of(donor)));
            var saved=ItemStack.parse(h.getLevel().registryAccess(),source.save(h.getLevel().registryAccess())).orElseThrow();
            h.assertTrue(ArmorFusion.donor(saved).is(donor.getItem()),"Historical donor survives save/load without requiring old mod");
            h.assertTrue(!ArmorFusion.canFuse(saved,donor),"Historical fusion cannot be fused a second time");
            h.assertTrue(saved.getHoverName().getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents name
                    &&name.getKey().equals("item.ars_fusion.fused_armor"),"One combined name, including when both mods are installed");
            var expected=new ArrayList<>(stats(ArmorFusionCases.ars(ArmorItem.Type.CHESTPLATE)));
            stats(donor).stream().filter(v->!v.attribute.equals("minecraft:generic.armor")&&!v.attribute.equals("minecraft:generic.armor_toughness")).forEach(expected::add);
            var actual=stats(saved);
            h.assertTrue(actual.size()==expected.size()&&actual.containsAll(expected),"Legacy donor stats transferred once, not once per installed mod");
        });
        h.succeed();
    }
    @GameTest(template="empty") public static void conditionalManualCompatibilityOverride(GameTestHelper h) {
        ArmorFusionCases.manualCompatibilityOverride(h);
    }
}
