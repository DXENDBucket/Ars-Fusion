package dev.arsfusion.crossbow;

import com.hollingsworth.arsnouveau.common.items.SpellCrossbow;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import dev.arsfusion.casting.FusedCasting;
import dev.arsfusion.config.FusionConfig;
import dev.arsfusion.platform.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;

public final class CrossbowFusion {
    public static final TagKey<Item> TARGETS=TagKey.create(Registries.ITEM,ResourceLocation.tryParse("ars_fusion:crossbow_fusion_targets"));
    public static final TagKey<Item> BLACKLIST=TagKey.create(Registries.ITEM,ResourceLocation.tryParse("ars_fusion:crossbow_fusion_blacklist"));
    private CrossbowFusion() {}
    public static SpellCrossbow crossbow() {return (SpellCrossbow)ItemsRegistry.SPELL_CROSSBOW.get();}
    public static boolean has(ItemStack stack) {return !stack.isEmpty()&&EquipmentPlatform.crossbow(stack);}
    public static boolean canFuse(ItemStack left,ItemStack right) {
        return FusionConfig.crossbowFusionEnabled()&&left.getCount()==1&&left.getMaxStackSize()==1
                &&left.getItem() instanceof CrossbowItem&&(FusionConfig.automaticCrossbowCompatibility()||left.is(TARGETS))&&!left.is(BLACKLIST)
                &&!FusedCasting.bound(left)&&!SwordPlatform.existingCaster(left)&&right.getItem() instanceof SpellCrossbow;
    }
    public static ItemStack fuse(ItemStack left,ItemStack right) {
        if(!canFuse(left,right))return ItemStack.EMPTY;
        var result=left.copy();SwordPlatform.copySpell(right,result);EquipmentPlatform.markCrossbow(result);return result;
    }
}
