package dev.arsfusion;

import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.spell.method.MethodProjectile;
import com.hollingsworth.arsnouveau.common.spell.effect.*;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import dev.arsfusion.bow.*;
import dev.arsfusion.platform.BowPlatform;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.nbt.CompoundTag;
import java.util.*;

public final class BowFusionCases {
    public static ItemStack donor(AbstractSpellPart... parts) {
        var tool=BowFusion.bow().getDefaultInstance();BowChecks.write(tool,new Spell(parts));return tool;
    }
    public static ItemStack fused(AbstractSpellPart... parts) {return BowFusion.fuse(Items.BOW.getDefaultInstance(),donor(parts));}
    private static void enroll(GameTestHelper h,ServerPlayer player) {h.getLevel().addNewPlayer(player);SwordChecks.mana(player,1000);}
    public static List<AbstractArrow> shoot(GameTestHelper h,ServerPlayer player,ItemStack bow,ItemStack ammo) {
        player.getInventory().clearContent();player.setItemInHand(InteractionHand.MAIN_HAND,bow);player.getInventory().setItem(9,ammo);
        player.startUsingItem(InteractionHand.MAIN_HAND);
        var before=new HashSet<UUID>();for(var entity:h.getLevel().getAllEntities())before.add(entity.getUUID());
        bow.releaseUsing(h.getLevel(),player,BowChecks.useDuration(bow,player)-20);
        player.stopUsingItem();
        var shots=new ArrayList<AbstractArrow>();for(var entity:h.getLevel().getAllEntities())if(entity instanceof AbstractArrow arrow&&arrow.getOwner()==player&&!before.contains(arrow.getUUID()))shots.add(arrow);
        return shots;
    }
    public static void policiesAndScribing(GameTestHelper h,ServerPlayer player) {
        var donor=donor(MethodProjectile.INSTANCE,EffectIgnite.INSTANCE);
        var left=Items.BOW.getDefaultInstance();left.setDamageValue(13);SwordChecks.enchant(h,left);var stats=SwordChecks.stats(left);
        player.experienceLevel=10;var menu=new AnvilMenu(1,player.getInventory(),ContainerLevelAccess.NULL);
        menu.getSlot(0).set(left.copy());menu.getSlot(1).set(donor);menu.createResult();var result=menu.getSlot(2).getItem().copy();
        h.assertTrue(BowFusion.has(result)&&result.is(Items.BOW)&&result.getDamageValue()==13&&result.isEnchanted()&&stats.equals(SwordChecks.stats(result)),"Bow anvil retains physical item, wear, enchantments and attributes");
        h.assertTrue(menu.getCost()==1,"One level");menu.getSlot(2).onTake(player,result);
        h.assertTrue(player.experienceLevel==9&&menu.getSlot(0).getItem().isEmpty()&&menu.getSlot(1).getItem().isEmpty(),"Native anvil consumes both once");
        h.assertTrue(!BowFusion.canFuse(result,donor)&&!BowFusion.canFuse(Items.CROSSBOW.getDefaultInstance(),donor)&&!BowFusion.canFuse(Items.STICK.getDefaultInstance(),donor),"No repeat, crossbow or stackable targets");
        h.assertTrue(BowFusion.has(SwordChecks.roundTrip(h,result)),"Persistent and network codecs retain binding");
        SwordChecks.scribe(h,player,result,false);var control=donor(MethodProjectile.INSTANCE,EffectHarm.INSTANCE);SwordChecks.scribe(h,player,control,false);
        h.assertTrue(SwordChecks.recipe(result).equals(SwordChecks.recipe(control))&&SwordChecks.recipe(result).get(0)==MethodProjectile.INSTANCE,"Real scribe table uses native bow rules and implicit Projectile");
        var previous=List.copyOf(SwordChecks.recipe(result));SwordChecks.scribe(h,player,result,true);
        h.assertTrue(SwordChecks.recipe(result).equals(previous),"Native bow rejects explicit forms");h.succeed();
    }
    public static void nativeReleaseAndAugmentArrows(GameTestHelper h,ServerPlayer player) {
        enroll(h,player);
        try {
            for(var ammoItem:List.of(Items.ARROW,ItemsRegistry.AMPLIFY_ARROW.get(),ItemsRegistry.PIERCE_ARROW.get(),ItemsRegistry.SPLIT_ARROW.get())) {
                BowChecks.mana(player,10000);var bow=fused(MethodProjectile.INSTANCE,EffectIgnite.INSTANCE);var ammo=new ItemStack(ammoItem,3);
                player.setItemInHand(InteractionHand.MAIN_HAND,bow);player.getInventory().setItem(9,ammo);
                h.assertTrue(player.getProjectile(bow).is(ammoItem),"Fused bow accepts Ars augment arrow "+ammoItem);
                var expected=BowPlatform.prepare(bow,player);var recipe=List.copyOf(SwordChecks.recipe(expected));
                double before=SwordChecks.mana(player);var shots=shoot(h,player,bow,ammo);
                int splits=BowPlatform.resolver(expected,player).spell.getBuffsAtIndex(0,player,AugmentSplit.INSTANCE);
                h.assertTrue(shots.size()==1+splits,"Native shot plus native Split amount for "+ammoItem+", got "+shots.size()+" wanted "+(1+splits)+", mana "+SwordChecks.mana(player));
                h.assertTrue(ammo.getCount()==2&&bow.getDamageValue()==1,"Original bow consumes only its ammo and durability once");
                h.assertTrue(SwordChecks.mana(player)<before,"Magic payloads cost mana");
                int pickup=0;for(var shot:shots) {
                    var payload=((SpellCarryingProjectile)shot).arsFusion$payload();
                    h.assertTrue(shot.getType()==EntityType.ARROW&&payload!=null,"Never replace original arrow type with Ars spell arrow");
                    h.assertTrue(SwordChecks.recipe(payload.tool()).equals(recipe),"Native arrow modifySpell applied in correct position");
                    int pierce=BowPlatform.resolver(expected,player).spell.getBuffsAtIndex(0,player,AugmentPierce.INSTANCE);
                    h.assertTrue(Byte.toUnsignedInt(shot.getPierceLevel())==pierce,"Native Pierce form augment applies");
                    if(shot.pickup==AbstractArrow.Pickup.ALLOWED)pickup++;
                    var loaded=shot.getType().create(h.getLevel());loaded.load(shot.saveWithoutId(new CompoundTag()));
                    h.assertTrue(((SpellCarryingProjectile)loaded).arsFusion$payload()!=null,"Projectile payload survives entity save/load");
                    shot.discard();
                }
                h.assertTrue(pickup==1,"Split cannot duplicate survival ammo pickups");
            }
            var bound=fused(MethodProjectile.INSTANCE,EffectIgnite.INSTANCE);SwordChecks.mana(player,0);var ammo=new ItemStack(ItemsRegistry.SPLIT_ARROW.get(),3);
            var shots=shoot(h,player,bound,ammo);
            h.assertTrue(shots.size()==1&&((SpellCarryingProjectile)shots.get(0)).arsFusion$payload()==null&&ammo.getCount()==2,"No mana leaves normal physical arrow and no magic Split");
            shots.forEach(Entity::discard);
            player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);SwordChecks.mana(player,500);double creativeMana=SwordChecks.mana(player);
            var free=shoot(h,player,fused(MethodProjectile.INSTANCE,EffectIgnite.INSTANCE),new ItemStack(Items.ARROW,3));
            h.assertTrue(free.size()==1&&((SpellCarryingProjectile)free.get(0)).arsFusion$payload()!=null&&SwordChecks.mana(player)==creativeMana,"Creative attaches spell without consuming mana");free.forEach(Entity::discard);h.succeed();
        } finally {player.discard();}
    }
    public static void impactsAndPartialBudget(GameTestHelper h,ServerPlayer player) {
        enroll(h,player);
        try {
            var bound=fused(MethodProjectile.INSTANCE,EffectIgnite.INSTANCE);var shots=shoot(h,player,bound,new ItemStack(Items.ARROW,3));var shot=shots.get(0);
            var victim=h.spawn(EntityType.COW,new BlockPos(4,3,4));victim.setNoAi(true);
            shot.setPos(victim.getX()-2,victim.getY()+0.7,victim.getZ());shot.setDeltaMovement(3,0,0);shot.setNoGravity(true);shot.tick();BowImpacts.tick();
            h.assertTrue(victim.isOnFire(),"Actual native arrow collision and successful damage append Ignite");victim.clearFire();
            // A queued impact with no successful native damage does not trigger the payload.
            var hit=new EntityHitResult(victim);BowChecks.impact(shot,hit,false);BowImpacts.tick();h.assertTrue(!victim.isOnFire(),"Failed/i-frame damage never appends spell");
            BowChecks.impact(shot,hit,true);BowImpacts.damaged(shot,victim,1);BowImpacts.tick();h.assertTrue(!victim.isOnFire(),"Canceled impact does not append magic");
            BowChecks.impact(shot,hit,false);BowImpacts.damaged(shot,victim,1);BowImpacts.tick();h.assertTrue(victim.isOnFire(),"Later valid hit on same victim is allowed; no lifetime victim blacklist");victim.clearFire();
            var secondary=EntityType.ARROW.create(h.getLevel());h.assertTrue(((SpellCarryingProjectile)secondary).arsFusion$payload()==null,"Unrelated/secondary arrows never inherit payload by owner");
            shot.discard();
            var split=fused(MethodProjectile.INSTANCE,AugmentSplit.INSTANCE,AugmentSplit.INSTANCE,EffectIgnite.INSTANCE);
            var cost=BowPlatform.resolver(BowPlatform.prepare(split,player),player).getResolveCost();SwordChecks.mana(player,cost*2);
            var fan=shoot(h,player,split,new ItemStack(Items.ARROW,3));
            h.assertTrue(fan.size()==2,"Pay individually: budget funds two shots instead of an unpaid third, cost="+cost+" count="+fan.size());
            var middle=fan.get(0).getDeltaMovement().normalize().add(fan.get(1).getDeltaMovement().normalize());
            h.assertTrue(Math.abs(middle.x)<0.06,"Partial Split fan remains centered on original aim");
            fan.forEach(Entity::discard);victim.discard();h.succeed();
        } finally {player.discard();BowImpacts.stop();}
    }
    public static void cataclysmNativeProjectiles(GameTestHelper h,ServerPlayer player) {
        var item=net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.tryParse("cataclysm:cursed_bow"));
        if(item==Items.AIR) {h.succeed();return;}
        enroll(h,player);
        try {
            var original=item.getDefaultInstance();var nativeShots=shoot(h,player,original,new ItemStack(Items.ARROW,4));
            var types=nativeShots.stream().map(Entity::getType).toList();var damages=nativeShots.stream().map(AbstractArrow::getBaseDamage).toList();int wear=original.getDamageValue();nativeShots.forEach(Entity::discard);
            var bow=BowFusion.fuse(item.getDefaultInstance(),donor(MethodProjectile.INSTANCE,EffectIgnite.INSTANCE));
            h.assertTrue(BowFusion.has(bow),"Tag-driven Cursed Bow works despite not inheriting BowItem");
            var shots=shoot(h,player,bow,new ItemStack(Items.ARROW,4));
            h.assertTrue(shots.size()==nativeShots.size()&&shots.stream().map(Entity::getType).toList().equals(types)&&shots.stream().map(AbstractArrow::getBaseDamage).toList().equals(damages)&&bow.getDamageValue()==wear,"Cursed Bow retains native multi-shot, Phantom arrow classes, physical damage and wear");
            for(var arrow:shots)h.assertTrue(((SpellCarryingProjectile)arrow).arsFusion$payload()!=null,"Each native Phantom arrow carries independent spell");
            var victim=h.spawn(EntityType.COW,new BlockPos(4,3,4));victim.setNoAi(true);
            var phantom=shots.get(0);phantom.setPos(victim.getX()-2,victim.getY()+0.7,victim.getZ());phantom.setDeltaMovement(3,0,0);phantom.setNoGravity(true);phantom.tick();BowImpacts.tick();
            h.assertTrue(victim.isOnFire(),"Native Phantom arrow's own collision/damage successfully triggers added spell");victim.discard();
            shots.forEach(Entity::discard);h.succeed();
        } finally {player.discard();}
    }
}
