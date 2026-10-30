package dev.arsfusion.mixin;
import dev.arsfusion.bow.BowCasting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(ItemStack.class)
public abstract class FusedBowReleaseMixin {
    @Redirect(method="releaseUsing",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/Item;releaseUsing(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;I)V"))
    private void arsFusion$nativeLaunch(Item item,ItemStack stack,Level world,LivingEntity actor,int remaining) {
        BowCasting.release(item,stack,world,actor,remaining);
    }
}
