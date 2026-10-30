package dev.arsfusion.casting;

import dev.arsfusion.bow.BowFusion;
import dev.arsfusion.crossbow.CrossbowFusion;
import dev.arsfusion.shield.ShieldFusion;
import dev.arsfusion.sword.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;

/** Shared per-stack recognition for native scribing, caster previews and UI. Shields are not casters. */
public final class FusedCasting {
    private FusedCasting() {}
    public static boolean has(ItemStack stack) {return SwordFusion.has(stack)||BowFusion.has(stack)||CrossbowFusion.has(stack);}
    public static boolean bound(ItemStack stack) {return has(stack)||ShieldFusion.has(stack);}
    public static Item nativeItem(ItemStack stack) {
        return SwordFusion.has(stack)?NativeSwordCasting.sword():BowFusion.has(stack)?BowFusion.bow()
                :CrossbowFusion.has(stack)?CrossbowFusion.crossbow():stack.getItem();
    }
    public static Component name(ItemStack stack,Component original) {
        return !bound(stack)||dev.arsfusion.platform.FusionPlatform.customName(stack)?original:Component.translatable("item.ars_fusion.enchanters_tool",original);
    }
}
