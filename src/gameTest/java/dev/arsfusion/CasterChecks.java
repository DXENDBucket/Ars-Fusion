package dev.arsfusion;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import java.util.*;
public final class CasterChecks {
    public static void castMirror(net.minecraft.gametest.framework.GameTestHelper h,net.minecraft.server.level.ServerPlayer player,ItemStack stack) {
        // Ars 5 use() sends a client packet; invoke the same native server dispatch as PacketCastSpell.
        var caster=com.hollingsworth.arsnouveau.api.registry.SpellCasterRegistry.from(stack);
        caster.castSpell(h.getLevel(),player,net.minecraft.world.InteractionHand.MAIN_HAND,net.minecraft.network.chat.Component.translatable("ars_nouveau.mirror.invalid"),caster.getSpell());
    }
    public static List<Object> bookSignature(ItemStack stack) {
        var caster=com.hollingsworth.arsnouveau.api.registry.SpellCasterRegistry.from(stack);
        return List.of(caster.getMaxSlots(),caster.getCurrentSlot(),caster.getSpellName(9),List.copyOf(caster.getSpell(0).unsafeList()),List.copyOf(caster.getSpell(9).unsafeList()));
    }
    public static void book(net.minecraft.world.item.ItemStack stack) {
        new com.hollingsworth.arsnouveau.api.spell.SpellCaster(10)
                .setSpell(new com.hollingsworth.arsnouveau.api.spell.Spell(com.hollingsworth.arsnouveau.common.spell.method.MethodSelf.INSTANCE,com.hollingsworth.arsnouveau.common.spell.effect.EffectHeal.INSTANCE),0)
                .setSpell(new com.hollingsworth.arsnouveau.api.spell.Spell(com.hollingsworth.arsnouveau.common.spell.method.MethodTouch.INSTANCE,com.hollingsworth.arsnouveau.common.spell.effect.EffectHarm.INSTANCE),9)
                .setSpellName("Last slot",9).setCurrentSlot(9).saveToStack(stack);
    }
    public static void attributes(ItemStack stack) {
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS,ItemAttributeModifiers.builder()
                .add(Attributes.MAX_HEALTH,new AttributeModifier(ResourceLocation.tryParse("ars_fusion:test_health"),6,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_DAMAGE,new AttributeModifier(ResourceLocation.tryParse("ars_fusion:test_attack"),50,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
                .add(Attributes.MOVEMENT_SPEED,new AttributeModifier(ResourceLocation.tryParse("ars_fusion:test_speed"),0.1,AttributeModifier.Operation.ADD_MULTIPLIED_BASE),EquipmentSlotGroup.OFFHAND).build());
    }
    public static List<String> bonuses(ItemStack stack,EquipmentSlot slot) {
        var result=new ArrayList<String>();stack.forEachModifier(slot,(attribute,modifier)->{
            if(modifier.id().getNamespace().equals("ars_fusion")&&modifier.id().getPath().startsWith("caster/"))
                result.add(attribute.getRegisteredName()+"/"+modifier.amount()+"/"+modifier.operation().ordinal());
        });Collections.sort(result);return result;
    }
}
