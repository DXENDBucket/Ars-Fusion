package dev.arsfusion.mixin;

import com.hollingsworth.arsnouveau.common.block.tile.AlterationTile;
import dev.arsfusion.platform.HighArmorPlatform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(value=AlterationTile.class,remap=false)
public abstract class HighArmorAlterationMixin {
    @ModifyConstant(method="addPerkStack",constant=@Constant(intValue=3))
    private int arsFusion$actualCapacity(int original) {
        return Math.max(original,HighArmorPlatform.slotCount(((AlterationTile)(Object)this).armorStack));
    }
}
