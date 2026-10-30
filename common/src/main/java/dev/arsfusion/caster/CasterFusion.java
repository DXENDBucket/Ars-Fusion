package dev.arsfusion.caster;

import com.hollingsworth.arsnouveau.common.items.*;
import dev.arsfusion.compat.GoetyCaster;
import dev.arsfusion.config.FusionConfig;
import dev.arsfusion.platform.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;

/** Attribute augmentation on an existing Ars caster; does not replace its native casting data. */
public final class CasterFusion {
    public static final TagKey<Item> TARGETS=tag("caster_fusion_targets"),TARGET_BLACKLIST=tag("caster_fusion_target_blacklist");
    public static final TagKey<Item> MATERIALS=tag("caster_fusion_materials"),MATERIAL_BLACKLIST=tag("caster_fusion_material_blacklist");
    private static TagKey<Item> tag(String path) {return TagKey.create(Registries.ITEM,ResourceLocation.tryParse("ars_fusion:"+path));}
    private CasterFusion() {}
    public static boolean receiver(ItemStack stack) {
        return !stack.isEmpty()&&!CasterPlatform.ironBook(stack)&&!stack.is(TARGET_BLACKLIST)&&SwordPlatform.existingCaster(stack)
                &&(stack.is(TARGETS)||stack.getItem() instanceof Wand||stack.getItem() instanceof EnchantersMirror||stack.getItem() instanceof SpellBook);
    }
    public static boolean has(ItemStack stack) {return !stack.isEmpty()&&CasterPlatform.has(stack);}
    public static ItemStack donor(ItemStack stack) {return has(stack)?CasterPlatform.donor(stack):ItemStack.EMPTY;}
    public static boolean canFuse(ItemStack left,ItemStack right) {
        return FusionConfig.casterFusionEnabled()&&receiver(left)&&left.getCount()==1&&left.getMaxStackSize()==1&&!has(left)
                &&!(right.getItem() instanceof SpellBook)&&!CasterPlatform.ironBook(right)&&right.getCount()==1&&right.getMaxStackSize()==1&&!has(right)&&!right.is(MATERIAL_BLACKLIST)
                &&(CasterPlatform.specialDonor(right)||FusionConfig.automaticCasterCompatibility()&&right.is(MATERIALS));
    }
    public static ItemStack fuse(ItemStack left,ItemStack right) {
        if(!canFuse(left,right))return ItemStack.EMPTY;
        var result=left.copy();var snapshot=right.copy();snapshot.setCount(1);
        boolean goety=CasterPlatform.goetyLoaded()&&FusionConfig.goetyStaffFusionEnabled()&&GoetyCaster.supports(snapshot);
        CasterPlatform.set(result,snapshot,goety);return result;
    }
    public static Component name(ItemStack stack,Component original) {
        var donor=donor(stack);
        return donor.isEmpty()||FusionPlatform.customName(stack)?original:Component.translatable("item.ars_fusion.fused_caster",original,donor.getHoverName());
    }
}
