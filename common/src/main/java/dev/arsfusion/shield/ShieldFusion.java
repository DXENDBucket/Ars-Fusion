package dev.arsfusion.shield;

import com.hollingsworth.arsnouveau.common.items.EnchantersShield;
import com.hollingsworth.arsnouveau.common.perk.RepairingPerk;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import dev.arsfusion.casting.FusedCasting;
import dev.arsfusion.config.FusionConfig;
import dev.arsfusion.platform.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;

/** Native shield utility, without inventing a spell container or overwriting the original shield. */
public final class ShieldFusion {
    public static final TagKey<Item> TARGETS=TagKey.create(Registries.ITEM,ResourceLocation.tryParse("ars_fusion:shield_fusion_targets"));
    public static final TagKey<Item> BLACKLIST=TagKey.create(Registries.ITEM,ResourceLocation.tryParse("ars_fusion:shield_fusion_blacklist"));
    private ShieldFusion() {}
    public static EnchantersShield shield() {return (EnchantersShield)ItemsRegistry.ENCHANTERS_SHIELD.get();}
    public static boolean has(ItemStack stack) {return !stack.isEmpty()&&EquipmentPlatform.shield(stack);}
    public static boolean canFuse(ItemStack left,ItemStack right) {
        return FusionConfig.shieldFusionEnabled()&&left.getCount()==1&&left.getMaxStackSize()==1
                &&(FusionConfig.automaticShieldCompatibility()||left.is(TARGETS))&&!left.is(BLACKLIST)&&EquipmentPlatform.canBlock(left)
                &&!(left.getItem() instanceof EnchantersShield)&&!FusedCasting.bound(left)&&!SwordPlatform.existingCaster(left)
                &&right.getItem() instanceof EnchantersShield;
    }
    public static ItemStack fuse(ItemStack left,ItemStack right) {
        if(!canFuse(left,right))return ItemStack.EMPTY;
        var result=left.copy();EquipmentPlatform.markShield(result);return result;
    }
    /** Used only by Ars' shield-event identity check, never by physical blocking or rendering. */
    public static Item recognizedItem(ItemStack stack) {return has(stack)?shield():stack.getItem();}
    public static void inventoryTick(ItemStack stack,Entity holder) {
        if(has(stack)&&holder instanceof Player player)RepairingPerk.attemptRepair(stack,player);
    }
}
