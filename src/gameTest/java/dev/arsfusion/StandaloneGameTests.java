package dev.arsfusion;
import dev.arsfusion.armor.ArmorFusion;
import dev.arsfusion.config.FusionConfig;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import java.util.List;
import java.util.ArrayList;
import com.hollingsworth.arsnouveau.api.registry.PerkRegistry;
import com.hollingsworth.arsnouveau.common.crafting.recipes.ArmorUpgradeRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.ApparatusRecipeInput;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("ars_fusion") @PrefixGameTestTemplate(false)
public final class StandaloneGameTests {
    @GameTest(template="empty") public static void standaloneAndUncappedToughness(GameTestHelper h) {
        h.assertTrue(FusionConfig.ARMOR_FUSION_ENABLED.getDefault()&&FusionConfig.AUTOMATIC_ARMOR_COMPATIBILITY.getDefault()&&!FusionConfig.TOUGHNESS_ENABLED.getDefault(),"Fusion and automatic compatibility default on; toughness defaults off");
        var source=BuiltInRegistries.ITEM.stream().map(ItemStack::new).filter(ArmorFusion::ars).findFirst().orElseThrow();
        boolean old=FusionConfig.TOUGHNESS_ENABLED.get();double amount=FusionConfig.ARS_ARMOR_TOUGHNESS_PER_TIER.get();
        // A higher-tier addon must also provide enough native perk slot tiers.
        var slots=PerkRegistry.getPerkProvider(source);var extended=new ArrayList<>(slots);
        while(extended.size()<6)extended.add(slots.get(slots.size()-1));
        PerkRegistry.registerPerkProvider(source.getItem(),extended);
        try {
            FusionConfig.TOUGHNESS_ENABLED.set(true);FusionConfig.ARS_ARMOR_TOUGHNESS_PER_TIER.set(1.0);
            var sets=BuiltInRegistries.ITEM.stream().map(ItemStack::new).filter(ArmorFusion::ars).toList();
            h.assertTrue(sets.size()>=12,"All three native armor sets, with all four slots, are covered");
            for(var base:sets)for(int level=0;level<3;level++) {
                var upgraded=new ArmorUpgradeRecipe(List.of(),0,level).assemble(new ApparatusRecipeInput(base.copy(),List.of(),null),h.getLevel().registryAccess());
                h.assertTrue(ArmorFusion.toughness(upgraded)==level+1,"Each set has its own 1/2/3 armor upgrade tiers: "+BuiltInRegistries.ITEM.getKey(base.getItem()));
            }
            for(int tier=0;tier<6;tier++) {
                var stack=new ArmorUpgradeRecipe(List.of(),0,tier).assemble(new ApparatusRecipeInput(source.copy(),List.of(),null),h.getLevel().registryAccess());
                h.assertTrue(ArmorFusion.toughness(stack)==tier+1,"Tier "+(tier+1)+" is read from real Ars data, without a tier-3 cap");
                double[] value={0};stack.forEachModifier(ArmorFusion.slot(stack),(a,m)->{if(m.id().getNamespace().equals("ars_fusion")&&m.id().getPath().startsWith("ars_toughness/"))value[0]+=m.amount();});
                h.assertTrue(value[0]==tier+1,"Attribute event supplies exactly one uncapped tier modifier");
            }
            FusionConfig.TOUGHNESS_ENABLED.set(false);
            h.assertTrue(ArmorFusion.toughness(source)==0,"Toughness is independently disabled");
        } finally {PerkRegistry.registerPerkProvider(source.getItem(),slots);FusionConfig.TOUGHNESS_ENABLED.set(old);FusionConfig.ARS_ARMOR_TOUGHNESS_PER_TIER.set(amount);}
        h.succeed();
    }
}
