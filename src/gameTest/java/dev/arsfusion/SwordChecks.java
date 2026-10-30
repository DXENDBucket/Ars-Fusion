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
        new SpellCaster(1).setSpell(spell).setSpellName("Test binding").saveToStack(stack);
    }
    public static List<AbstractSpellPart> recipe(ItemStack stack) {return com.hollingsworth.arsnouveau.api.registry.SpellCasterRegistry.from(stack).getSpell().unsafeList();}
    public static void enchant(GameTestHelper h,ItemStack stack) {stack.enchant(h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING),1);}
    public static void rename(ItemStack stack,Component name) {stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,name);}
    public static ItemStack roundTrip(GameTestHelper h,ItemStack stack) {
        var saved=ItemStack.parse(h.getLevel().registryAccess(),stack.save(h.getLevel().registryAccess())).orElseThrow();var buf=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),h.getLevel().registryAccess());
        try {ItemStack.STREAM_CODEC.encode(buf,saved);return ItemStack.STREAM_CODEC.decode(buf);} finally {buf.release();}
    }
    public static List<String> stats(ItemStack s) {var list=new ArrayList<String>();s.forEachModifier(net.minecraft.world.entity.EquipmentSlot.MAINHAND,(a,m)->list.add(a.getRegisteredName()+"/"+m.id()+"/"+m.amount()+"/"+m.operation()));return list;}
    public static void hit(ItemStack s,LivingEntity target,ServerPlayer player) {if(s.hurtEnemy(target,player))s.postHurtEnemy(target,player);}
    public static void mana(ServerPlayer player,int n) {var m=CapabilityRegistry.getMana(player);m.setMaxMana(1000);m.setMana(n);}
    public static double mana(ServerPlayer player) {return CapabilityRegistry.getMana(player).getCurrentMana();}
    public static void scribe(GameTestHelper h,ServerPlayer player,ItemStack tool,boolean invalid) {
        var pos=h.absolutePos(new BlockPos(1,2,4));
        var state=BlockRegistry.SCRIBES_BLOCK.get().defaultBlockState().setValue(com.hollingsworth.arsnouveau.common.block.ScribesBlock.PART,com.hollingsworth.arsnouveau.common.block.ThreePartBlock.HEAD);
        h.getLevel().setBlock(pos,state,3);
        var tile=(com.hollingsworth.arsnouveau.common.block.tile.ScribesTile)h.getLevel().getBlockEntity(pos);tile.setStack(tool);
        var parchment=ItemsRegistry.SPELL_PARCHMENT.get().getDefaultInstance();write(parchment,false,invalid);
        player.setItemInHand(InteractionHand.MAIN_HAND,parchment);player.setShiftKeyDown(true);
        var hit=new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
        state.useItemOn(parchment,h.getLevel(),player,InteractionHand.MAIN_HAND,hit);
        player.setShiftKeyDown(false);
    }
}
