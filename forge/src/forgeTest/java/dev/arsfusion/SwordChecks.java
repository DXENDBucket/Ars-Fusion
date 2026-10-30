package dev.arsfusion;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.spell.method.*;
import com.hollingsworth.arsnouveau.common.spell.effect.*;
import com.hollingsworth.arsnouveau.setup.registry.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.*;
import java.util.*;
public final class SwordChecks {
    public static void write(ItemStack stack,boolean touch,boolean invalid) {
        var spell=new Spell();if(touch)spell=spell.add(MethodTouch.INSTANCE);if(invalid)spell=spell.add(MethodProjectile.INSTANCE);spell=spell.add(touch?EffectHarm.INSTANCE:EffectIgnite.INSTANCE);
        var caster=new SpellCaster(stack);caster.setSpell(spell);caster.setSpellName("Test binding");
    }
    public static List<AbstractSpellPart> recipe(ItemStack stack) {return new SpellCaster(stack).getSpell().recipe;}
    public static void enchant(GameTestHelper h,ItemStack stack) {stack.enchant(Enchantments.UNBREAKING,1);}
    public static void rename(ItemStack stack,Component name) {stack.setHoverName(name);}
    public static ItemStack roundTrip(GameTestHelper h,ItemStack stack) {
        var saved=ItemStack.of(stack.save(new net.minecraft.nbt.CompoundTag()));var buf=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try {buf.writeItem(saved);return buf.readItem();} finally {buf.release();}
    }
    public static void existingMagic(ItemStack stack) {
        var original=com.google.common.collect.ArrayListMultimap.create(stack.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND));
        original.forEach((a,m)->stack.addAttributeModifier(a,m,net.minecraft.world.entity.EquipmentSlot.MAINHAND));
        stack.addAttributeModifier(com.hollingsworth.arsnouveau.api.perk.PerkAttributes.SPELL_DAMAGE_BONUS.get(),
            new net.minecraft.world.entity.ai.attributes.AttributeModifier(java.util.UUID.fromString("da0ff177-3acf-41ad-8ee6-3d28d47ce6ac"),"existing_magic",3,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION),net.minecraft.world.entity.EquipmentSlot.MAINHAND);
    }
    public static double spellDamage(LivingEntity actor) {return actor.getAttributeValue(com.hollingsworth.arsnouveau.api.perk.PerkAttributes.SPELL_DAMAGE_BONUS.get());}
    public static List<String> physicalStats(ItemStack stack) {
        var result=new ArrayList<String>();stack.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND).forEach((a,m)->{if(a!=com.hollingsworth.arsnouveau.api.perk.PerkAttributes.SPELL_DAMAGE_BONUS.get())result.add(a.getDescriptionId()+"/"+m.getId()+"/"+m.getAmount()+"/"+m.getOperation());});return result;
    }
    public static List<String> stats(ItemStack s) {var list=new ArrayList<String>();s.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND).forEach((a,m)->list.add(a.getDescriptionId()+"/"+m.getId()+"/"+m.getAmount()+"/"+m.getOperation()));return list;}
    public static void hit(ItemStack s,LivingEntity target,ServerPlayer player) {s.hurtEnemy(target,player);}
    public static void mana(ServerPlayer player,int n) {CapabilityRegistry.getMana(player).ifPresent(m->{m.setMaxMana(1000);m.setMana(n);});}
    public static double mana(ServerPlayer player) {return CapabilityRegistry.getMana(player).map(m->m.getCurrentMana()).orElse(0.0);}
    public static void scribe(GameTestHelper h,ServerPlayer player,ItemStack tool,boolean invalid) {
        var pos=h.absolutePos(new BlockPos(1,2,4));
        var state=BlockRegistry.SCRIBES_BLOCK.get().defaultBlockState().setValue(com.hollingsworth.arsnouveau.common.block.ScribesBlock.PART,com.hollingsworth.arsnouveau.common.block.ThreePartBlock.HEAD);
        h.getLevel().setBlock(pos,state,3);
        var tile=(com.hollingsworth.arsnouveau.common.block.tile.ScribesTile)h.getLevel().getBlockEntity(pos);tile.setStack(tool);
        var parchment=ItemsRegistry.SPELL_PARCHMENT.get().getDefaultInstance();write(parchment,false,invalid);
        player.setItemInHand(InteractionHand.MAIN_HAND,parchment);player.setShiftKeyDown(true);
        var hit=new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
        state.use(h.getLevel(),player,InteractionHand.MAIN_HAND,hit);
        player.setShiftKeyDown(false);
    }
}
