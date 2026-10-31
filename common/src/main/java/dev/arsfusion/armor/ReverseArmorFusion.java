package dev.arsfusion.armor;

import dev.arsfusion.config.FusionConfig;
import dev.arsfusion.platform.FusionPlatform;
import dev.arsfusion.platform.ReverseArmorPlatform;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/** The left item remains the armor; the donor supplies threads and optionally base defense. */
public final class ReverseArmorFusion {
    private ReverseArmorFusion() { }
    public static boolean has(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem && ReverseArmorPlatform.has(stack);
    }
    public static ItemStack donor(ItemStack stack) {
        if (!has(stack)) return ItemStack.EMPTY;
        var donor=ReverseArmorPlatform.donor(stack);
        return donor.getItem() instanceof ArmorItem && !ReverseArmorPlatform.has(donor)
                && !FusionPlatform.has(donor) && ArmorFusion.slot(donor)==ArmorFusion.slot(stack)
                ?donor:ItemStack.EMPTY;
    }
    public static boolean canFuse(ItemStack receiver,ItemStack donor) {
        return FusionConfig.armorFusionEnabled() && FusionConfig.reverseArmorFusionEnabled()
                && receiver.getItem() instanceof ArmorItem && receiver.getCount()==1 && receiver.getMaxStackSize()==1
                && donor.getCount()==1 && ArmorFusion.ars(donor)
                && !FusionPlatform.has(receiver) && !ReverseArmorPlatform.has(receiver)
                && !FusionPlatform.has(donor) && !ReverseArmorPlatform.has(donor)
                && !ReverseArmorPlatform.hasNativePerks(receiver)
                && ArmorFusion.slot(receiver)==ArmorFusion.slot(donor)
                && ReverseArmorPlatform.validDonor(donor);
    }
    public static ItemStack fuse(ItemStack receiver,ItemStack donor) {
        if (!canFuse(receiver,donor)) return ItemStack.EMPTY;
        var result=receiver.copy();result.setCount(1);
        ReverseArmorPlatform.transfer(result,donor);
        return result;
    }
    public record Defense(double armor,double toughness) { }

    /** Read the donor's material, but the live recipient tier after any native upgrades.
     * Installed threads are applied separately, so their toughness is never copied twice. */
    public static Defense defense(ItemStack stack) {
        if(!FusionConfig.reverseUsesArsDefense())return null;
        var donor=donor(stack);
        if(donor.isEmpty())return null;
        var armor=(ArmorItem)donor.getItem();
        return new Defense(armor.getDefense(),armor.getToughness()
                +FusionConfig.arsArmorToughnessPerTier()*FusionPlatform.tier(stack));
    }
    public static Component name(ItemStack stack,Component original) {
        var donor=donor(stack);
        return donor.isEmpty()||FusionPlatform.customName(stack)?original
                :Component.translatable("item.ars_fusion.fused_armor",original,donor.getHoverName());
    }
}
