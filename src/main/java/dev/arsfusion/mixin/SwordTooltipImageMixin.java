package dev.arsfusion.mixin;
import dev.arsfusion.sword.NativeSwordCasting;
import dev.arsfusion.sword.SwordFusion;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import java.util.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(ItemStack.class)
public abstract class SwordTooltipImageMixin {
    @Inject(method="getTooltipImage",at=@At("HEAD"),cancellable=true)
    private void arsFusion$spellImage(CallbackInfoReturnable<Optional<TooltipComponent>> cir) {
        var stack=(ItemStack)(Object)this;
        if(dev.arsfusion.casting.FusedCasting.has(stack))cir.setReturnValue(dev.arsfusion.casting.FusedCasting.nativeItem(stack).getTooltipImage(stack));
    }
}
