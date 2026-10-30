package dev.arsfusion;

import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.common.spell.method.MethodSelf;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectHeal;
import dev.arsfusion.caster.CasterFusion;
import dev.arsfusion.config.FusionConfig;
import dev.arsfusion.platform.CasterPlatform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.*;
import java.util.*;

public final class CasterFusionCases {
    public static ItemStack item(String path) {return BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(path)).getDefaultInstance();}
    public static ItemStack wand() {var stack=item("ars_nouveau:wand");BowChecks.write(stack,new Spell(MethodSelf.INSTANCE,EffectHeal.INSTANCE));return stack;}
    public static ItemStack material() {var stack=Items.BRUSH.getDefaultInstance();CasterChecks.attributes(stack);return stack;}
    public static void policyAndPersistence(GameTestHelper h,ServerPlayer player) {
        var left=wand();left.setDamageValue(0);var recipe=List.copyOf(SwordChecks.recipe(left));var right=material();right.setDamageValue(12);SwordChecks.enchant(h,right);player.experienceLevel=10;
        h.assertTrue(CasterFusion.canFuse(left,right)&&CasterFusion.receiver(item("ars_nouveau:enchanters_mirror")),"Wand and mirror are receivers");
        for(var book:List.of("ars_nouveau:novice_spell_book","ars_nouveau:apprentice_spell_book","ars_nouveau:archmage_spell_book"))h.assertTrue(CasterFusion.canFuse(item(book),right)&&!CasterFusion.canFuse(left,item(book)),"Ars spellbooks are receivers, never materials: "+book);
        h.assertTrue(!CasterFusion.canFuse(left,Items.STICK.getDefaultInstance())&&!CasterFusion.canFuse(Items.DIAMOND_SWORD.getDefaultInstance(),right),"Require tagged nonstackable material and real Ars caster");
        var menu=new AnvilMenu(1,player.getInventory(),ContainerLevelAccess.NULL);menu.getSlot(0).set(left.copy());menu.getSlot(1).set(right.copy());menu.createResult();var fused=menu.getSlot(2).getItem().copy();
        h.assertTrue(CasterFusion.has(fused)&&fused.is(left.getItem())&&SwordChecks.recipe(fused).equals(recipe)&&!fused.isEnchanted(),"Only donor attributes/traits added; receiver spell and identity remain, donor enchantments not copied");
        h.assertTrue(!CasterFusion.has(left)&&right.getDamageValue()==12,"Consumed input snapshots are not mutated");h.assertTrue(menu.getCost()==1,"One XP level");menu.getSlot(2).onTake(player,fused);
        h.assertTrue(player.experienceLevel==9&&menu.getSlot(0).getItem().isEmpty()&&menu.getSlot(1).getItem().isEmpty(),"Native anvil consumes both once");
        var saved=SwordChecks.roundTrip(h,fused);h.assertTrue(CasterFusion.has(saved)&&CasterFusion.donor(saved).getDamageValue()==12&&SwordChecks.recipe(saved).equals(recipe),"Native spell and donor snapshot survive persistent and network codecs");
        h.assertTrue(!CasterFusion.canFuse(saved,right)&&!CasterFusion.canFuse(left,saved),"One caster fusion and no nested donor");
        boolean enabled=FusionConfig.CASTER_FUSION_ENABLED.get(),auto=FusionConfig.AUTOMATIC_CASTER_COMPATIBILITY.get();
        try {FusionConfig.CASTER_FUSION_ENABLED.set(false);h.assertTrue(CasterFusion.fuse(left,right).isEmpty()&&CasterFusion.has(saved),"Creation toggle keeps stored data");FusionConfig.CASTER_FUSION_ENABLED.set(true);FusionConfig.AUTOMATIC_CASTER_COMPATIBILITY.set(false);h.assertTrue(!CasterFusion.canFuse(left,right),"Generic tag-based donors require automatic compatibility");}
        finally {FusionConfig.CASTER_FUSION_ENABLED.set(enabled);FusionConfig.AUTOMATIC_CASTER_COMPATIBILITY.set(auto);}
        var book=item("ars_nouveau:novice_spell_book");CasterChecks.book(book);
        var fusedBook=SwordChecks.roundTrip(h,CasterFusion.fuse(book,material()));
        var source=CasterChecks.bookSignature(book);var result=CasterChecks.bookSignature(fusedBook);
        h.assertTrue(CasterFusion.has(fusedBook)&&result.equals(source)&&result.get(0).equals(10)&&result.get(1).equals(9)&&result.get(2).equals("Last slot"),
                "Ars book retains all native spell slots, selected slot and names through fusion and serialization");
        h.succeed();
    }
    public static void slotsBlacklistAndRealEquipment(GameTestHelper h,ServerPlayer player) {
        var fused=CasterFusion.fuse(wand(),material());
        h.assertTrue(CasterChecks.bonuses(fused,EquipmentSlot.MAINHAND).equals(List.of("minecraft:generic.max_health/6.0/0")),"Main-hand donor modifiers keep native amount/operation; attack damage filtered");
        h.assertTrue(CasterChecks.bonuses(fused,EquipmentSlot.OFFHAND).equals(List.of("minecraft:generic.movement_speed/0.1/1")),"Offhand-only modifier retains its operation and native hand");
        h.assertTrue(CasterChecks.bonuses(fused,EquipmentSlot.HEAD).isEmpty(),"Held caster cannot grant donor bonuses from inventory/armor slots");
        var old=FusionConfig.CASTER_ATTRIBUTE_BLACKLIST.get();
        try {FusionConfig.CASTER_ATTRIBUTE_BLACKLIST.set(List.of());h.assertTrue(CasterChecks.bonuses(fused,EquipmentSlot.MAINHAND).contains("minecraft:generic.attack_damage/50.0/0"),"Empty caster blacklist allows source attack modifier");FusionConfig.CASTER_ATTRIBUTE_BLACKLIST.set(List.of("minecraft:generic.max_health"));h.assertTrue(CasterChecks.bonuses(fused,EquipmentSlot.MAINHAND).stream().noneMatch(s->s.contains("max_health")),"Runtime blacklist also governs existing caster fusion");}
        finally {FusionConfig.CASTER_ATTRIBUTE_BLACKLIST.set(old);}
        h.getLevel().addNewPlayer(player);
        try {player.setItemInHand(InteractionHand.MAIN_HAND,fused);player.doTick();h.assertTrue(Math.abs(player.getMaxHealth()-26)<0.01,"Real equipped attributes receive exactly one modifier copy");player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);player.doTick();h.assertTrue(Math.abs(player.getMaxHealth()-20)<0.01,"Unequipping removes the donor bonus");h.succeed();}finally {player.discard();}
    }
    public static void mirrorNativeEditingAndCasting(GameTestHelper h,ServerPlayer player) {
        var mirror=item("ars_nouveau:enchanters_mirror");var fused=CasterFusion.fuse(mirror,material());
        SwordChecks.scribe(h,player,mirror,false);SwordChecks.scribe(h,player,fused,false);
        h.assertTrue(CasterFusion.has(fused)&&SwordChecks.recipe(fused).equals(SwordChecks.recipe(mirror)),"Real mirror scribing retains fusion and matches original native implicit-form rules");
        BowChecks.write(mirror,new Spell(MethodSelf.INSTANCE,EffectHeal.INSTANCE));BowChecks.write(fused,new Spell(MethodSelf.INSTANCE,EffectHeal.INSTANCE));
        SwordChecks.mana(player,1000);player.setItemInHand(InteractionHand.MAIN_HAND,mirror);player.setHealth(5);double before=SwordChecks.mana(player);CasterChecks.castMirror(h,player,mirror);double spent=before-SwordChecks.mana(player);float health=player.getHealth();
        h.assertTrue(health>5&&spent>0,"Native mirror baseline heals and pays mana");
        SwordChecks.mana(player,1000);player.setItemInHand(InteractionHand.MAIN_HAND,fused);player.setHealth(5);before=SwordChecks.mana(player);CasterChecks.castMirror(h,player,fused);
        h.assertTrue(player.getHealth()==health&&Math.abs((before-SwordChecks.mana(player))-spent)<0.01,"Fusion retains actual mirror casting and its native mana discount");h.succeed();
    }
    public static void nativeIronStaffAndBookBoundary(GameTestHelper h,ServerPlayer player) {
        if(!dev.arsfusion.platform.FusionPlatform.ironLoaded()) {h.succeed();return;}
        var staff=item("irons_spellbooks:graybeard_staff");h.assertTrue(!staff.is(Items.AIR),"Native Iron staff fixture available");
        var fused=CasterFusion.fuse(wand(),staff);h.assertTrue(CasterFusion.has(fused),"Iron native staff is accepted without a material tag");
        for(var slot:List.of(EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND)) {
            var expected=CasterPlatform.bonuses(staff,slot).stream().filter(b->FusionConfig.allowsCasterAttribute(b.attribute())).map(b->b.attribute()+"/"+b.amount()+"/"+b.operation()).sorted().toList();
            h.assertTrue(CasterChecks.bonuses(fused,slot).equals(expected),"Native Iron staff panel transfer preserves slot, amount and operation");
        }
        h.assertTrue(!CasterChecks.bonuses(fused,EquipmentSlot.MAINHAND).isEmpty(),"Test actually transfers native magic bonuses");
        int books=0;for(var item:BuiltInRegistries.ITEM)if(dev.arsfusion.compat.IronCaster.book(item.getDefaultInstance())) {
            books++;h.assertTrue(!CasterFusion.canFuse(wand(),item.getDefaultInstance())&&!CasterFusion.canFuse(item.getDefaultInstance(),staff),"Every native Iron spellbook is excluded from both sides");
        }
        h.assertTrue(books>0,"Iron book rejection fixture is substantive");h.succeed();
    }
}
