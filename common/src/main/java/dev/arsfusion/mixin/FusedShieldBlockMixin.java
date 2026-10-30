package dev.arsfusion.mixin;
import com.hollingsworth.arsnouveau.common.event.EventHandler;
import dev.arsfusion.shield.ShieldFusion;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=EventHandler.class,remap=false)
public abstract class FusedShieldBlockMixin {
    @Redirect(method="shieldEvent",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;",remap=true))
    private static Item arsFusion$nativeRecognition(ItemStack stack) {return ShieldFusion.recognizedItem(stack);}
}
