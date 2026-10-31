package dev.arsfusion;

import dev.arsfusion.armor.ReverseArmorFusion;
import dev.arsfusion.config.FusionConfig;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/** Real item-attribute event regressions, without depending on one loader's modifier format. */
public final class ReverseDefenseCases {
    public record Values(double armor,double toughness,double knockback) { }
    private ReverseDefenseCases() { }

    public static void verify(GameTestHelper h,ItemStack threaded,ItemStack empty,
                              UnaryOperator<ItemStack> upgrade,Function<ItemStack,Values> attributes) {
        boolean enabled=FusionConfig.ARMOR_FUSION_ENABLED.get(),reverse=FusionConfig.REVERSE_ARMOR_FUSION_ENABLED.get();
        boolean replace=FusionConfig.REVERSE_USES_ARS_DEFENSE.get(),toughness=FusionConfig.TOUGHNESS_ENABLED.get();
        double perTier=FusionConfig.ARS_ARMOR_TOUGHNESS_PER_TIER.get();
        try {
            h.assertTrue(FusionConfig.REVERSE_USES_ARS_DEFENSE.getDefault(),"Ars defense replacement defaults on");
            FusionConfig.ARMOR_FUSION_ENABLED.set(true);FusionConfig.REVERSE_ARMOR_FUSION_ENABLED.set(true);
            FusionConfig.REVERSE_USES_ARS_DEFENSE.set(true);FusionConfig.TOUGHNESS_ENABLED.set(true);
            FusionConfig.ARS_ARMOR_TOUGHNESS_PER_TIER.set(2.0);
            var left=new ItemStack(Items.NETHERITE_CHESTPLATE);var fused=ReverseArmorFusion.fuse(left,threaded);
            var baseline=attributes.apply(left);var expected=attributes.apply(threaded);var actual=attributes.apply(fused);
            check(h,actual.armor(),expected.armor(),"Receiver armor is replaced, not added to Ars armor");
            check(h,actual.toughness(),expected.toughness(),"Source material, tier bonus and toughness thread each apply once");
            check(h,actual.knockback(),baseline.knockback(),"Non-defense receiver modifiers are retained");
            var upgraded=upgrade.apply(fused.copy());var upgradedDonor=upgrade.apply(threaded.copy());
            check(h,attributes.apply(upgraded).toughness(),attributes.apply(upgradedDonor).toughness(),"Replacement reads the live tier after native upgrade");
            FusionConfig.REVERSE_USES_ARS_DEFENSE.set(false);
            var restored=attributes.apply(fused);
            double threadBonus=attributes.apply(threaded).toughness()-attributes.apply(empty).toughness();
            check(h,restored.armor(),baseline.armor(),"Disabling policy restores existing receiver armor");
            check(h,restored.toughness(),baseline.toughness()+threadBonus,"Disabling policy keeps receiver toughness and actual thread only");
            FusionConfig.REVERSE_USES_ARS_DEFENSE.set(true);FusionConfig.TOUGHNESS_ENABLED.set(false);
            check(h,attributes.apply(fused).toughness(),attributes.apply(threaded).toughness(),"Optional tier-toughness toggle also governs replacement");
            check(h,attributes.apply(left).toughness(),baseline.toughness(),"Unfused stacks of the receiver item are untouched");
        } finally {
            FusionConfig.ARMOR_FUSION_ENABLED.set(enabled);FusionConfig.REVERSE_ARMOR_FUSION_ENABLED.set(reverse);
            FusionConfig.REVERSE_USES_ARS_DEFENSE.set(replace);FusionConfig.TOUGHNESS_ENABLED.set(toughness);
            FusionConfig.ARS_ARMOR_TOUGHNESS_PER_TIER.set(perTier);
        }
        h.succeed();
    }

    private static void check(GameTestHelper h,double actual,double expected,String message) {
        h.assertTrue(Math.abs(actual-expected)<.00001,message+": "+actual+" != "+expected);
    }
}
