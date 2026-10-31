package dev.arsfusion.mixin;

import com.hollingsworth.arsnouveau.api.perk.IPerkHolder;
import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import dev.arsfusion.armor.ReverseArmorFusion;
import dev.arsfusion.platform.ReverseArmorPlatform;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value=PerkUtil.class,remap=false)
public abstract class ReverseArmorSlotsMixin {
    @Inject(method="getPerkHolder",at=@At("HEAD"),cancellable=true)
    private static void arsFusion$holder(ItemStack stack,CallbackInfoReturnable<IPerkHolder<ItemStack>> cir) {
        if (ReverseArmorFusion.has(stack)) cir.setReturnValue(ReverseArmorPlatform.holder(stack));
    }
}
