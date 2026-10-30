package dev.arsfusion.platform;

import net.minecraft.nbt.Tag;
import net.minecraft.world.item.*;
import java.util.*;

public final class EquipmentPlatform {
    private static final String CROSSBOW="ars_fusion:crossbow_fusion",SHIELD="ars_fusion:shield_fusion";
    private EquipmentPlatform() {}
    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {}
    public static boolean crossbow(ItemStack stack) {return stack.hasTag()&&stack.getTag().getBoolean(CROSSBOW);}
    public static boolean shield(ItemStack stack) {return stack.hasTag()&&stack.getTag().getBoolean(SHIELD);}
    public static void markCrossbow(ItemStack stack) {stack.getOrCreateTag().putBoolean(CROSSBOW,true);}
    public static void markShield(ItemStack stack) {stack.getOrCreateTag().putBoolean(SHIELD,true);}
    public static boolean canBlock(ItemStack stack) {return stack.canPerformAction(net.minecraftforge.common.ToolActions.SHIELD_BLOCK);}
    public static List<ItemStack> chargedAmmo(ItemStack stack) {
        if(!CrossbowItem.isCharged(stack)||!stack.hasTag())return List.of();
        var tags=stack.getTag().getList("ChargedProjectiles",Tag.TAG_COMPOUND);var ammo=new ArrayList<ItemStack>();
        for(int i=0;i<tags.size();i++)ammo.add(ItemStack.of(tags.getCompound(i)));return ammo;
    }
}
