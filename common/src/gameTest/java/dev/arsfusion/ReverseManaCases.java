package dev.arsfusion;

import dev.arsfusion.armor.ReverseArmorFusion;
import dev.arsfusion.config.FusionConfig;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/** Compare the actual native base/thread modifiers, including each modifier operation. */
public final class ReverseManaCases {
    private ReverseManaCases() { }
    public static void verify(GameTestHelper h,ItemStack left,ItemStack threaded,ItemStack empty,
                              UnaryOperator<ItemStack> upgrade,Function<ItemStack,double[]> attributes) {
        boolean enabled=FusionConfig.ARMOR_FUSION_ENABLED.get(),reverse=FusionConfig.REVERSE_ARMOR_FUSION_ENABLED.get();
        boolean transfer=FusionConfig.REVERSE_TRANSFERS_ARS_MANA.get(),defense=FusionConfig.REVERSE_USES_ARS_DEFENSE.get();
        try {
            h.assertTrue(FusionConfig.REVERSE_TRANSFERS_ARS_MANA.getDefault(),"Base mana transfer defaults on");
            FusionConfig.ARMOR_FUSION_ENABLED.set(true);FusionConfig.REVERSE_ARMOR_FUSION_ENABLED.set(true);
            FusionConfig.REVERSE_TRANSFERS_ARS_MANA.set(true);FusionConfig.REVERSE_USES_ARS_DEFENSE.set(true);
            var fused=ReverseArmorFusion.fuse(left,threaded);
            var baseline=attributes.apply(left);var source=attributes.apply(threaded);
            compare(h,attributes.apply(fused),baseline,source,null,"Source mana/regen and installed thread each transfer once");
            var upgraded=upgrade.apply(fused.copy());var upgradedSource=upgrade.apply(threaded.copy());
            compare(h,attributes.apply(upgraded),baseline,attributes.apply(upgradedSource),null,"Native upgrade changes transferred base mana/regen");
            FusionConfig.REVERSE_USES_ARS_DEFENSE.set(false);
            compare(h,attributes.apply(fused),baseline,source,null,"Mana transfer is independent of defense replacement");
            FusionConfig.REVERSE_TRANSFERS_ARS_MANA.set(false);
            compare(h,attributes.apply(fused),baseline,source,attributes.apply(empty),"Disabling transfer keeps thread mana and removes only Ars base mana/regen");
        } finally {
            FusionConfig.ARMOR_FUSION_ENABLED.set(enabled);FusionConfig.REVERSE_ARMOR_FUSION_ENABLED.set(reverse);
            FusionConfig.REVERSE_TRANSFERS_ARS_MANA.set(transfer);FusionConfig.REVERSE_USES_ARS_DEFENSE.set(defense);
        }
        h.succeed();
    }
    private static void compare(GameTestHelper h,double[] actual,double[] baseline,double[] source,double[] subtract,String message) {
        for(int i=0;i<actual.length;i++) {
            double expected=baseline[i]+source[i]-(subtract==null?0:subtract[i]);
            h.assertTrue(Math.abs(actual[i]-expected)<.00001,message+" ["+i+"]: "+actual[i]+" != "+expected);
        }
    }
}
