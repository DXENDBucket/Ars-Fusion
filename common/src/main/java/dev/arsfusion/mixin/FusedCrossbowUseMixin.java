package dev.arsfusion.mixin;
import dev.arsfusion.crossbow.CrossbowCasting;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(ItemStack.class)
public abstract class FusedCrossbowUseMixin {
    @Redirect(method="use",at=@At(value="INVOKE",target="Lnet/minecraft/world/item/Item;use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResultHolder;"))
    private InteractionResultHolder<ItemStack> arsFusion$fire(Item item,Level world,Player actor,InteractionHand hand) {return CrossbowCasting.use(item,world,actor,hand);}
}
