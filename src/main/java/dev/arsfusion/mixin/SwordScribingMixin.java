package dev.arsfusion.mixin;
import com.hollingsworth.arsnouveau.common.block.ScribesBlock;
import dev.arsfusion.sword.NativeSwordCasting;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=ScribesBlock.class,remap=false)
public abstract class SwordScribingMixin {
    @Redirect(method="useItemOn",remap=true,at=@At(value="INVOKE",target="Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;",remap=true))
    private Item arsFusion$scribable(ItemStack stack) {return NativeSwordCasting.scribeItem(stack);}
}
