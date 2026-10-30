package dev.arsfusion.mixin;
import dev.arsfusion.shield.ShieldFusion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ItemStack.class)
public abstract class FusedShieldRepairMixin {
    @Inject(method="inventoryTick",at=@At("RETURN"))
    private void arsFusion$repair(Level world,Entity holder,int slot,boolean selected,CallbackInfo ci) {ShieldFusion.inventoryTick((ItemStack)(Object)this,holder);}
}
