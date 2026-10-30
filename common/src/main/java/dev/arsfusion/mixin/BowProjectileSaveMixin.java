package dev.arsfusion.mixin;
import dev.arsfusion.bow.SpellCarryingProjectile;
import dev.arsfusion.platform.BowPlatform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(Entity.class)
public abstract class BowProjectileSaveMixin {
    @Inject(method="saveWithoutId",at=@At("RETURN"))
    private void arsFusion$save(CompoundTag tag,CallbackInfoReturnable<CompoundTag> cir) {
        if((Object)this instanceof SpellCarryingProjectile carrier)BowPlatform.save(carrier,cir.getReturnValue(),((Entity)(Object)this).level());
    }
    @Inject(method="load",at=@At("RETURN"))
    private void arsFusion$load(CompoundTag tag,CallbackInfo ci) {
        if((Object)this instanceof SpellCarryingProjectile carrier)BowPlatform.load(carrier,tag,((Entity)(Object)this).level());
    }
}
