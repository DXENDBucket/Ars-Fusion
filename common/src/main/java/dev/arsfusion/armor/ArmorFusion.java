package dev.arsfusion.armor;

import dev.arsfusion.compat.IronArmor;
import dev.arsfusion.api.ArmorDonors;
import dev.arsfusion.config.FusionConfig;
import dev.arsfusion.platform.FusionPlatform;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/** Fusion policy, independent of loader and optional donor implementation. */
public final class ArmorFusion {
    private ArmorFusion() {}

    public static boolean iron(ItemStack stack) {
        return FusionPlatform.ironLoaded() && IronArmor.accepts(stack);
    }

    public static boolean ars(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem && !iron(stack) && !dev.arsfusion.platform.ReverseArmorPlatform.has(stack) && FusionPlatform.tier(stack) > 0;
    }

    public static EquipmentSlot slot(ItemStack stack) {
        return ((ArmorItem) stack.getItem()).getType().getSlot();
    }

    public static boolean has(ItemStack stack) {
        return ars(stack) && FusionPlatform.has(stack);
    }

    public static ItemStack donor(ItemStack stack) {
        if (!has(stack)) return ItemStack.EMPTY;
        var donor = FusionPlatform.donor(stack);
        return isDonorArmor(donor) && slot(donor) == slot(stack) ? donor : ItemStack.EMPTY;
    }

    private static boolean isDonorArmor(ItemStack donor) {
        // Ars is the receiving layer. Do not nest fused snapshots or fuse two Ars sets.
        return donor.getItem() instanceof ArmorItem && !ars(donor) && !FusionPlatform.has(donor) && !dev.arsfusion.platform.ReverseArmorPlatform.has(donor);
    }

    public static boolean canFuse(ItemStack ars, ItemStack donor) {
        return FusionConfig.armorFusionEnabled() && ars(ars) && isDonorArmor(donor)
                && !has(ars) && slot(ars) == slot(donor)
                && (FusionConfig.automaticArmorCompatibility() || ArmorDonors.manualAdapter(donor) != null);
    }

    public static ItemStack fuse(ItemStack ars, ItemStack donor) {
        if (!canFuse(ars, donor)) return ItemStack.EMPTY;
        var snapshot = donor.copy();
        snapshot.setCount(1);
        var adapter = FusionConfig.automaticOnly() ? null : ArmorDonors.manualAdapter(snapshot);
        if (adapter != null) adapter.prepareDonor(snapshot);
        var result = ars.copy();
        result.setCount(1);
        FusionPlatform.set(result, snapshot);
        if (adapter != null) adapter.transferData(snapshot, result);
        return result;
    }

    public static Component name(ItemStack stack, Component original) {
        var donor = donor(stack);
        return donor.isEmpty() || FusionPlatform.customName(stack) ? original
                : Component.translatable("item.ars_fusion.fused_armor", original, donor.getHoverName());
    }

    public static double toughness(ItemStack stack) {
        return ars(stack) ? FusionConfig.arsArmorToughnessPerTier() * FusionPlatform.tier(stack) : 0;
    }
}
