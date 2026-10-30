package dev.arsfusion;
import dev.arsfusion.armor.ArmorFusion;
import dev.arsfusion.config.FusionConfig;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.ArrayList;
import com.hollingsworth.arsnouveau.api.registry.PerkRegistry;
import com.hollingsworth.arsnouveau.api.enchanting_apparatus.ArmorUpgradeRecipe;
import net.minecraftforge.gametest.*;
@GameTestHolder("ars_fusion") @PrefixGameTestTemplate(false)
public final class StandaloneGameTests {
    @GameTest(template="empty") public static void standaloneAndUncappedToughness(GameTestHelper h) {
        h.assertTrue(!FusionConfig.ARMOR_FUSION_ENABLED.getDefault()&&!FusionConfig.TOUGHNESS_ENABLED.getDefault(),"Both changes are disabled by default");
        var source=BuiltInRegistries.ITEM.stream().map(ItemStack::new).filter(ArmorFusion::ars).findFirst().orElseThrow();
        boolean old=FusionConfig.TOUGHNESS_ENABLED.get();double amount=FusionConfig.ARS_ARMOR_TOUGHNESS_PER_TIER.get();
        // A higher-tier addon must also provide enough native perk slot tiers.
        var slots=PerkRegistry.getPerkProvider(source.getItem());
        var extended=java.util.Collections.nCopies(6,slots.getPerkHolder(source).getSlotsForTier());
        PerkRegistry.registerPerkProvider(source.getItem(),stack -> new com.hollingsworth.arsnouveau.api.perk.ArmorPerkHolder(stack,extended));
        try {
            FusionConfig.TOUGHNESS_ENABLED.set(true);FusionConfig.ARS_ARMOR_TOUGHNESS_PER_TIER.set(1.0);
            var sets=BuiltInRegistries.ITEM.stream().map(ItemStack::new).filter(ArmorFusion::ars).toList();
            h.assertTrue(sets.size()>=12,"All three native armor sets, with all four slots, are covered");
            for(var base:sets)for(int level=0;level<3;level++) {
                var upgraded=new ArmorUpgradeRecipe(List.of(),0,level).getResult(List.of(),base.copy(),null);
                h.assertTrue(ArmorFusion.toughness(upgraded)==level+1,"Each set has its own 1/2/3 armor upgrade tiers: "+BuiltInRegistries.ITEM.getKey(base.getItem()));
            }
            for(int tier=0;tier<6;tier++) {
                var stack=new ArmorUpgradeRecipe(List.of(),0,tier).getResult(List.of(),source.copy(),null);
                h.assertTrue(ArmorFusion.toughness(stack)==tier+1,"Tier "+(tier+1)+" is read from real Ars data, without a tier-3 cap");
                double[] value={0};stack.getAttributeModifiers(ArmorFusion.slot(stack)).forEach((a,m)->{if(m.getName().equals("Ars armor tier toughness"))value[0]+=m.getAmount();});
                h.assertTrue(value[0]==tier+1,"Attribute event supplies exactly one uncapped tier modifier");
            }
            FusionConfig.TOUGHNESS_ENABLED.set(false);
            h.assertTrue(ArmorFusion.toughness(source)==0,"Toughness is independently disabled");
        } finally {PerkRegistry.registerPerkProvider(source.getItem(),slots);FusionConfig.TOUGHNESS_ENABLED.set(old);FusionConfig.ARS_ARMOR_TOUGHNESS_PER_TIER.set(amount);}
        h.succeed();
    }
}
