package dev.arsfusion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.ItemStack;
import java.util.*;
public final class CasterChecks {
    public static void castMirror(net.minecraft.gametest.framework.GameTestHelper h,net.minecraft.server.level.ServerPlayer player,ItemStack stack) {
        stack.use(h.getLevel(),player,net.minecraft.world.InteractionHand.MAIN_HAND);
    }
    public static List<Object> bookSignature(ItemStack stack) {
        var caster=((com.hollingsworth.arsnouveau.api.item.ICasterTool)stack.getItem()).getSpellCaster(stack);
        return List.of(caster.getMaxSlots(),caster.getCurrentSlot(),caster.getSpellName(9),List.copyOf(caster.getSpell(0).recipe),List.copyOf(caster.getSpell(9).recipe));
    }
    public static void book(net.minecraft.world.item.ItemStack stack) {
        var caster=((com.hollingsworth.arsnouveau.api.item.ICasterTool)stack.getItem()).getSpellCaster(stack);
        caster.setSpell(new com.hollingsworth.arsnouveau.api.spell.Spell(com.hollingsworth.arsnouveau.common.spell.method.MethodSelf.INSTANCE,com.hollingsworth.arsnouveau.common.spell.effect.EffectHeal.INSTANCE),0);
        caster.setSpell(new com.hollingsworth.arsnouveau.api.spell.Spell(com.hollingsworth.arsnouveau.common.spell.method.MethodTouch.INSTANCE,com.hollingsworth.arsnouveau.common.spell.effect.EffectHarm.INSTANCE),9);
        caster.setSpellName("Last slot",9);caster.setCurrentSlot(9);
    }
    public static void attributes(ItemStack stack) {
        stack.addAttributeModifier(Attributes.MAX_HEALTH,new AttributeModifier(UUID.fromString("d5c3dab7-ae76-4417-98ce-cf0d99128abd"),"test_health",6,AttributeModifier.Operation.ADDITION),EquipmentSlot.MAINHAND);
        stack.addAttributeModifier(Attributes.ATTACK_DAMAGE,new AttributeModifier(UUID.fromString("d5c3dab7-ae76-4417-98ce-cf0d99128abe"),"test_attack",50,AttributeModifier.Operation.ADDITION),EquipmentSlot.MAINHAND);
        stack.addAttributeModifier(Attributes.MOVEMENT_SPEED,new AttributeModifier(UUID.fromString("d5c3dab7-ae76-4417-98ce-cf0d99128abf"),"test_speed",0.1,AttributeModifier.Operation.MULTIPLY_BASE),EquipmentSlot.OFFHAND);
    }
    public static List<String> bonuses(ItemStack stack,EquipmentSlot slot) {
        var result=new ArrayList<String>();stack.getAttributeModifiers(slot).forEach((attribute,modifier)->{
            if(modifier.getName().startsWith("ars_fusion:caster/"))result.add(BuiltInRegistries.ATTRIBUTE.getKey(attribute)+"/"+modifier.getAmount()+"/"+modifier.getOperation().ordinal());
        });Collections.sort(result);return result;
    }
}
