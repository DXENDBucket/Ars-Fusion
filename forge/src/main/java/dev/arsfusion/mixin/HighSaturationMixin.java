package dev.arsfusion.mixin;
import com.hollingsworth.arsnouveau.common.perk.SaturationPerk;
import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import com.google.common.collect.Multimap;
import com.google.common.collect.HashMultimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=SaturationPerk.class,remap=false)
public abstract class HighSaturationMixin {
    @Inject(method="getModifiers",at=@At("RETURN"),cancellable=true)
    private void arsFusion$continueNumericGrowth(EquipmentSlot slot,ItemStack stack,int slotValue,CallbackInfoReturnable<Multimap<Attribute,AttributeModifier>> cir) {
        if(slotValue<=3)return;
        var modifiers=HashMultimap.create(cir.getReturnValue());modifiers.removeAll(PerkAttributes.WHIRLIESPRIG.get());
        modifiers.put(PerkAttributes.WHIRLIESPRIG.get(),new AttributeModifier(SaturationPerk.PERK_UUID,"SaturationPerk",1.0+0.3*(slotValue-3),AttributeModifier.Operation.MULTIPLY_TOTAL));
        cir.setReturnValue(modifiers);
    }
}
