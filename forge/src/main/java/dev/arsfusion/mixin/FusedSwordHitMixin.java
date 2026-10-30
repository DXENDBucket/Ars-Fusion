package dev.arsfusion.mixin;
import dev.arsfusion.sword.NativeSwordCasting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
@Mixin(ItemStack.class)
public abstract class FusedSwordHitMixin {
    @Inject(method="hurtEnemy",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/Item;hurtEnemy(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private void arsFusion$cast(LivingEntity target,Player attacker,CallbackInfo ci) {
        NativeSwordCasting.onHit((ItemStack)(Object)this,target,attacker);
    }
}
