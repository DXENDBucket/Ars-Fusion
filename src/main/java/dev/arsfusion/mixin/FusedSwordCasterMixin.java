package dev.arsfusion.mixin;
import com.hollingsworth.arsnouveau.api.registry.SpellCasterRegistry;
import dev.arsfusion.sword.SwordFusion;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=SpellCasterRegistry.class,remap=false)
public abstract class FusedSwordCasterMixin {
    @Inject(method="hasCaster",at=@At("HEAD"),cancellable=true)
    private static void arsFusion$boundCaster(ItemStack stack,CallbackInfoReturnable<Boolean> cir) {
        if(SwordFusion.has(stack)||dev.arsfusion.bow.BowFusion.has(stack))cir.setReturnValue(true);
    }
}
