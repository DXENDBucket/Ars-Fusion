package dev.arsfusion.mixin;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(AbstractArrow.class)
public interface BowArrowAccessor {
    @Invoker("setPierceLevel") void arsFusion$setPierceLevel(byte level);
}
