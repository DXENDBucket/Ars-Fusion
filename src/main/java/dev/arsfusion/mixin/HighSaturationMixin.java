package dev.arsfusion.mixin;
import com.hollingsworth.arsnouveau.common.perk.SaturationPerk;
import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=SaturationPerk.class,remap=false)
public abstract class HighSaturationMixin {
    @Inject(method="applyAttributeModifiers",at=@At("RETURN"),cancellable=true)
    private void arsFusion$continueNumericGrowth(ItemAttributeModifiers modifiers,ItemStack stack,int slotValue,EquipmentSlotGroup group,CallbackInfoReturnable<ItemAttributeModifiers> cir) {
        if(slotValue>3)cir.setReturnValue(cir.getReturnValue().withModifierAdded(PerkAttributes.WHIRLIESPRIG,
                new AttributeModifier(SaturationPerk.INSTANCE.getRegistryName(),1.0+0.3*(slotValue-3),AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),group));
    }
}
