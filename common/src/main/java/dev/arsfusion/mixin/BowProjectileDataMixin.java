package dev.arsfusion.mixin;
import dev.arsfusion.bow.*;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
@Mixin(Projectile.class)
public abstract class BowProjectileDataMixin implements SpellCarryingProjectile {
    @Unique private BowPayload arsFusion$spell;
    public BowPayload arsFusion$payload() {return arsFusion$spell;}
    public void arsFusion$payload(BowPayload value) {arsFusion$spell=value;}
}
