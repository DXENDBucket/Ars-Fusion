package dev.arsfusion.sword;

import com.hollingsworth.arsnouveau.common.items.EnchantersSword;
import dev.arsfusion.config.FusionConfig;
import dev.arsfusion.platform.FusionPlatform;
import dev.arsfusion.platform.SwordPlatform;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** One native Enchanter's Sword casting layer on the original receiving item. */
public final class SwordFusion {
    public static final TagKey<Item> TARGETS=TagKey.create(Registries.ITEM,ResourceLocation.tryParse("ars_fusion:spell_fusion_targets"));
    public static final TagKey<Item> BLACKLIST=TagKey.create(Registries.ITEM,ResourceLocation.tryParse("ars_fusion:spell_fusion_blacklist"));
    private SwordFusion() {}

    public static boolean has(ItemStack stack) {return !stack.isEmpty()&&SwordPlatform.has(stack);}
    public static boolean canFuse(ItemStack left,ItemStack right) {
        return FusionConfig.swordFusionEnabled()&&left.getCount()==1&&left.getMaxStackSize()==1
                &&left.is(TARGETS)&&!left.is(BLACKLIST)&&!has(left)&&!dev.arsfusion.casting.FusedCasting.bound(left)&&!SwordPlatform.existingCaster(left)
                &&right.getItem() instanceof EnchantersSword&&!has(right);
    }
    public static ItemStack fuse(ItemStack left,ItemStack right) {
        if(!canFuse(left,right))return ItemStack.EMPTY;
        var result=left.copy();result.setCount(1);
        SwordPlatform.copySpell(right,result);
        SwordPlatform.mark(result);
        return result;
    }
    public static Component name(ItemStack stack,Component original) {
        return !has(stack)||FusionPlatform.customName(stack)?original:Component.translatable("item.ars_fusion.enchanters_tool",original);
    }
}
