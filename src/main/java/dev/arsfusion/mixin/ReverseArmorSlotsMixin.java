package dev.arsfusion.mixin;

import com.hollingsworth.arsnouveau.api.perk.PerkSlot;
import com.hollingsworth.arsnouveau.api.registry.PerkRegistry;
import dev.arsfusion.armor.ReverseArmorFusion;
import dev.arsfusion.platform.ReverseArmorPlatform;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

/** Stack-local provider lookup; unfused items of this type never acquire thread slots. */
@Mixin(value=PerkRegistry.class,remap=false)
public abstract class ReverseArmorSlotsMixin {
    @Inject(method="getPerkProvider(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;",at=@At("HEAD"),cancellable=true)
    private static void arsFusion$slots(ItemStack stack,CallbackInfoReturnable<List<List<PerkSlot>>> cir) {
        if (ReverseArmorFusion.has(stack)) cir.setReturnValue(ReverseArmorPlatform.slots(stack));
    }
}
