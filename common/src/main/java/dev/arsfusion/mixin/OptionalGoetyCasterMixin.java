package dev.arsfusion.mixin;
import dev.arsfusion.compat.GoetyCaster;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Pseudo
@Mixin(targets="com.Polarice3.Goety.utils.WandUtil",remap=false)
public abstract class OptionalGoetyCasterMixin {
    @Inject(method="findWand",at=@At("HEAD"),cancellable=true)
    private static void arsFusion$staff(LivingEntity actor,CallbackInfoReturnable<ItemStack> cir) {
        var view=GoetyCaster.selected(actor);if(!view.isEmpty())cir.setReturnValue(view);
    }
    @Inject(method="findWandOnHand",at=@At("RETURN"),cancellable=true)
    private static void arsFusion$hand(LivingEntity actor,InteractionHand hand,CallbackInfoReturnable<ItemStack> cir) {
        if(actor!=null&&cir.getReturnValue().isEmpty()) {
            var view=GoetyCaster.view(actor.getItemInHand(hand));if(!view.isEmpty())cir.setReturnValue(view);
        }
    }
}
