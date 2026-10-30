package dev.arsfusion.mixin;
import com.hollingsworth.arsnouveau.common.items.EnchantersSword;
import dev.arsfusion.sword.NativeSwordCasting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=EnchantersSword.class,remap=false)
public abstract class NativeSwordPhysicalMixin {
    @Inject(method="hurtEnemy",remap=true,at=@At(value="INVOKE",target="Lnet/minecraft/world/item/SwordItem;hurtEnemy(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/LivingEntity;)Z",remap=true),cancellable=true)
    private void arsFusion$physicalOwner(ItemStack stack,LivingEntity target,LivingEntity attacker,CallbackInfoReturnable<Boolean> cir) {
        if(NativeSwordCasting.delegating(stack))cir.setReturnValue(false);
    }
}
