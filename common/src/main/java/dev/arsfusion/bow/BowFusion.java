package dev.arsfusion.bow;

import com.hollingsworth.arsnouveau.common.items.SpellBow;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import dev.arsfusion.config.FusionConfig;
import dev.arsfusion.platform.BowPlatform;
import dev.arsfusion.platform.FusionPlatform;
import dev.arsfusion.platform.SwordPlatform;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;

/** The receiving bow remains responsible for ammo, shooting, projectiles and wear. */
public final class BowFusion {
    public static final TagKey<Item> TARGETS=TagKey.create(Registries.ITEM,ResourceLocation.tryParse("ars_fusion:bow_fusion_targets"));
    public static final TagKey<Item> BLACKLIST=TagKey.create(Registries.ITEM,ResourceLocation.tryParse("ars_fusion:bow_fusion_blacklist"));
    private BowFusion() {}
    public static SpellBow bow() {return (SpellBow)ItemsRegistry.SPELL_BOW.get();}
    public static boolean has(ItemStack stack) {return !stack.isEmpty()&&BowPlatform.has(stack);}
    public static boolean canFuse(ItemStack left,ItemStack right) {
        return FusionConfig.bowFusionEnabled()&&left.getCount()==1&&left.getMaxStackSize()==1
                &&left.getItem() instanceof ProjectileWeaponItem&&!(left.getItem() instanceof CrossbowItem)
                &&left.is(TARGETS)&&!left.is(BLACKLIST)&&!dev.arsfusion.casting.FusedCasting.bound(left)&&!SwordPlatform.existingCaster(left)
                &&right.getItem() instanceof SpellBow;
    }
    public static ItemStack fuse(ItemStack left,ItemStack right) {
        if(!canFuse(left,right))return ItemStack.EMPTY;
        var result=left.copy();result.setCount(1);SwordPlatform.copySpell(right,result);BowPlatform.mark(result);return result;
    }
    public static Component name(ItemStack stack,Component original) {
        return !has(stack)||FusionPlatform.customName(stack)?original:Component.translatable("item.ars_fusion.enchanters_tool",original);
    }
}
