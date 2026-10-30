package dev.arsfusion;

import com.hollingsworth.arsnouveau.common.spell.method.MethodProjectile;
import com.hollingsworth.arsnouveau.common.spell.effect.*;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import dev.arsfusion.bow.*;
import dev.arsfusion.crossbow.*;
import dev.arsfusion.shield.ShieldFusion;
import dev.arsfusion.platform.*;
import dev.arsfusion.config.FusionConfig;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.core.BlockPos;
import java.util.*;

public final class EquipmentFusionCases {
    public static ItemStack donor() {
        var item=CrossbowFusion.crossbow().getDefaultInstance();BowChecks.write(item,new com.hollingsworth.arsnouveau.api.spell.Spell(MethodProjectile.INSTANCE,EffectIgnite.INSTANCE));return item;
    }
    public static void rulesAndScribing(GameTestHelper h,ServerPlayer player) {
        var crossbow=Items.CROSSBOW.getDefaultInstance();crossbow.setDamageValue(7);SwordChecks.enchant(h,crossbow);
        var shield=Items.SHIELD.getDefaultInstance();shield.setDamageValue(19);SwordChecks.enchant(h,shield);
        player.experienceLevel=10;
        for(var pair:List.of(List.of(crossbow,donor()),List.of(shield,ShieldFusion.shield().getDefaultInstance()))) {
            var left=pair.get(0);var menu=new AnvilMenu(1,player.getInventory(),ContainerLevelAccess.NULL);menu.getSlot(0).set(left.copy());menu.getSlot(1).set(pair.get(1));menu.createResult();var output=menu.getSlot(2).getItem().copy();
            h.assertTrue(output.is(left.getItem())&&output.getDamageValue()==left.getDamageValue()&&output.isEnchanted()&&SwordChecks.stats(output).equals(SwordChecks.stats(left)),"Fusion retains receiver identity, enchantments, damage and modifiers");
            h.assertTrue(menu.getCost()==1,"One XP level");menu.getSlot(2).onTake(player,output);
            h.assertTrue(menu.getSlot(0).getItem().isEmpty()&&menu.getSlot(1).getItem().isEmpty(),"Native anvil consumes each once");
            var saved=SwordChecks.roundTrip(h,output);
            h.assertTrue(dev.arsfusion.casting.FusedCasting.bound(saved)&&!CrossbowFusion.canFuse(saved,donor())&&!ShieldFusion.canFuse(saved,ShieldFusion.shield().getDefaultInstance()),"Binding survives both codecs and cannot be repeated");
        }
        h.assertTrue(player.experienceLevel==8,"Both anvil fusions cost exactly one level");
        h.assertTrue(!CrossbowFusion.canFuse(Items.BOW.getDefaultInstance(),donor())&&!ShieldFusion.canFuse(Items.STICK.getDefaultInstance(),ShieldFusion.shield().getDefaultInstance()),"Require actual crossbow or shield functionality");
        var bound=CrossbowFusion.fuse(Items.CROSSBOW.getDefaultInstance(),donor());SwordChecks.scribe(h,player,bound,false);var nativeTool=donor();SwordChecks.scribe(h,player,nativeTool,false);
        h.assertTrue(SwordChecks.recipe(bound).equals(SwordChecks.recipe(nativeTool))&&SwordChecks.recipe(bound).get(0)==MethodProjectile.INSTANCE,"Real scribe block follows native crossbow implicit-form rules");
        var recipe=List.copyOf(SwordChecks.recipe(bound));SwordChecks.scribe(h,player,bound,true);h.assertTrue(recipe.equals(SwordChecks.recipe(bound)),"Reject explicit forms as native crossbow does");
        var fusedShield=ShieldFusion.fuse(shield,ShieldFusion.shield().getDefaultInstance());
        h.assertTrue(!SwordPlatform.existingCaster(fusedShield)&&dev.arsfusion.sword.NativeSwordCasting.scribeItem(fusedShield)==Items.SHIELD,"Shield fusion does not invent a caster or scribing ability");
        boolean c=FusionConfig.CROSSBOW_FUSION_ENABLED.get(),s=FusionConfig.SHIELD_FUSION_ENABLED.get();
        try {FusionConfig.CROSSBOW_FUSION_ENABLED.set(false);FusionConfig.SHIELD_FUSION_ENABLED.set(false);
            h.assertTrue(CrossbowFusion.fuse(crossbow,donor()).isEmpty()&&ShieldFusion.fuse(shield,ShieldFusion.shield().getDefaultInstance()).isEmpty()&&CrossbowFusion.has(bound)&&ShieldFusion.has(fusedShield),"Crafting toggles preserve existing bindings");
        } finally {FusionConfig.CROSSBOW_FUSION_ENABLED.set(c);FusionConfig.SHIELD_FUSION_ENABLED.set(s);}
        h.succeed();
    }
    public static void load(GameTestHelper h,ServerPlayer player,ItemStack weapon,ItemStack ammo,InteractionHand hand) {
        player.getInventory().clearContent();player.setItemInHand(hand,weapon);player.getInventory().setItem(9,ammo);
        weapon.use(h.getLevel(),player,hand);weapon.releaseUsing(h.getLevel(),player,BowChecks.useDuration(weapon,player)-40);player.stopUsingItem();
    }
    public static List<Projectile> fire(GameTestHelper h,ServerPlayer player,ItemStack weapon,InteractionHand hand) {
        player.setItemInHand(hand,weapon);var before=new HashSet<UUID>();for(var entity:h.getLevel().getAllEntities())before.add(entity.getUUID());
        weapon.use(h.getLevel(),player,hand);
        var shots=new ArrayList<Projectile>();for(var entity:h.getLevel().getAllEntities())if(entity instanceof Projectile p&&p.getOwner()==player&&!before.contains(p.getUUID()))shots.add(p);
        return shots;
    }
    public static void crossbowArrows(GameTestHelper h,ServerPlayer player) {
        h.getLevel().addNewPlayer(player);
        try {
            for(var ammoItem:List.of(Items.ARROW,ItemsRegistry.AMPLIFY_ARROW.get(),ItemsRegistry.PIERCE_ARROW.get(),ItemsRegistry.SPLIT_ARROW.get())) {
                BowChecks.mana(player,10000);var bound=CrossbowFusion.fuse(Items.CROSSBOW.getDefaultInstance(),donor());var ammo=new ItemStack(ammoItem,4);double initial=SwordChecks.mana(player);
                load(h,player,bound,ammo,InteractionHand.OFF_HAND);
                var charged=EquipmentPlatform.chargedAmmo(bound);h.assertTrue(charged.size()==1&&charged.get(0).is(ammoItem)&&ammo.getCount()==3&&SwordChecks.mana(player)==initial,"Loading uses native ammo and no magic mana yet: "+ammoItem);
                var saved=SwordChecks.roundTrip(h,bound);h.assertTrue(CrossbowFusion.has(saved)&&EquipmentPlatform.chargedAmmo(saved).get(0).is(ammoItem),"Loaded ammunition and marker survive persistence/network");
                var prepared=BowPlatform.prepare(saved,player,InteractionHand.OFF_HAND,charged.get(0));
                player.getInventory().setItem(9,new ItemStack(Items.ARROW,4));var shots=fire(h,player,saved,InteractionHand.OFF_HAND);
                int count=1+BowPlatform.resolver(prepared,player).spell.getBuffsAtIndex(0,player,AugmentSplit.INSTANCE);
                h.assertTrue(shots.size()==count&&EquipmentPlatform.chargedAmmo(saved).isEmpty(),"Firing consumes loaded ammo once, with native Split count");
                for(var p:shots) {
                    var payload=((SpellCarryingProjectile)p).arsFusion$payload();
                    h.assertTrue(p.getType()==EntityType.ARROW&&payload!=null&&SwordChecks.recipe(payload.tool()).equals(SwordChecks.recipe(prepared)),"Original arrow type receives the LOADED ammo augment, not replacement inventory ammo");
                    int pierce=BowPlatform.resolver(prepared,player).spell.getBuffsAtIndex(0,player,AugmentPierce.INSTANCE);
                    h.assertTrue(Byte.toUnsignedInt(((AbstractArrow)p).getPierceLevel())==pierce,"Crossbow native and Ars piercing are supported");
                }
                h.assertTrue(SwordChecks.mana(player)<initial,"Firing magic pays mana");
                shots.forEach(Entity::discard);
            }
            h.succeed();
        } finally {player.discard();}
    }
    public static void multishotFireworksAndEmptyMana(GameTestHelper h,ServerPlayer player) {
        h.getLevel().addNewPlayer(player);
        try {
            BowChecks.mana(player,10000);var vanilla=Items.CROSSBOW.getDefaultInstance();EquipmentChecks.multishot(h,vanilla);var ammo=new ItemStack(Items.ARROW,4);
            load(h,player,vanilla,ammo,InteractionHand.MAIN_HAND);var nativeShots=fire(h,player,vanilla,InteractionHand.MAIN_HAND);int wear=vanilla.getDamageValue();var speeds=nativeShots.stream().map(p->p.getDeltaMovement().length()).toList();nativeShots.forEach(Entity::discard);
            var fused=CrossbowFusion.fuse(Items.CROSSBOW.getDefaultInstance(),donor());EquipmentChecks.multishot(h,fused);var cost=BowPlatform.resolver(BowPlatform.prepare(fused,player,InteractionHand.MAIN_HAND,Items.ARROW.getDefaultInstance()),player).getResolveCost();
            load(h,player,fused,new ItemStack(Items.ARROW,4),InteractionHand.MAIN_HAND);BowChecks.mana(player,cost*2);var shots=fire(h,player,fused,InteractionHand.MAIN_HAND);
            h.assertTrue(shots.size()==3&&fused.getDamageValue()==wear,"Retains native multi-shot and wear");
            int paid=0;for(int i=0;i<shots.size();i++){if(((SpellCarryingProjectile)shots.get(i)).arsFusion$payload()!=null)paid++;h.assertTrue(Math.abs(shots.get(i).getDeltaMovement().length()-speeds.get(i))<0.08,"Native crossbow projectile speed retained");}
            h.assertTrue(paid==2,"Native three-arrow shot survives limited mana, with exactly two paid payloads");shots.forEach(Entity::discard);
            BowChecks.mana(player,10000);var rocketBow=CrossbowFusion.fuse(Items.CROSSBOW.getDefaultInstance(),donor());
            player.setItemInHand(InteractionHand.MAIN_HAND,Items.FIREWORK_ROCKET.getDefaultInstance());
            load(h,player,rocketBow,new ItemStack(Items.FIREWORK_ROCKET,4),InteractionHand.MAIN_HAND); // native held-ammo predicate accepts fireworks
            // Some native versions require fireworks in the opposite hand rather than inventory.
            if(EquipmentPlatform.chargedAmmo(rocketBow).isEmpty()) {
                player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.FIREWORK_ROCKET,4));rocketBow.use(h.getLevel(),player,InteractionHand.MAIN_HAND);rocketBow.releaseUsing(h.getLevel(),player,BowChecks.useDuration(rocketBow,player)-40);player.stopUsingItem();
            }
            var rockets=fire(h,player,rocketBow,InteractionHand.MAIN_HAND);
            h.assertTrue(rockets.size()==1&&rockets.get(0) instanceof FireworkRocketEntity&&((SpellCarryingProjectile)rockets.get(0)).arsFusion$payload()!=null,"Fireworks remain actual native rockets, not Ars arrows");rockets.forEach(Entity::discard);
            var empty=CrossbowFusion.fuse(Items.CROSSBOW.getDefaultInstance(),donor());load(h,player,empty,new ItemStack(Items.ARROW,4),InteractionHand.MAIN_HAND);SwordChecks.mana(player,0);
            var physical=fire(h,player,empty,InteractionHand.MAIN_HAND);h.assertTrue(physical.size()==1&&((SpellCarryingProjectile)physical.get(0)).arsFusion$payload()==null,"Empty mana retains normal physical crossbow shot");physical.forEach(Entity::discard);h.succeed();
        } finally {player.discard();}
    }
    public static void shieldBlocking(GameTestHelper h,ServerPlayer player) {
        shieldBlocking(h,player,Items.SHIELD);
    }
    public static void shieldBlocking(GameTestHelper h,ServerPlayer player,Item item) {
        h.getLevel().addNewPlayer(player);var shield=ShieldFusion.fuse(item.getDefaultInstance(),ShieldFusion.shield().getDefaultInstance());
        // New ServerPlayers have 60 ticks of spawn immunity, which short-circuits shield blocking.
        for(int i=0;i<61;i++)player.tick();
        player.setItemInHand(InteractionHand.OFF_HAND,shield);shield.use(h.getLevel(),player,InteractionHand.OFF_HAND);
        var attacker=h.spawn(EntityType.ZOMBIE,new BlockPos(1,2,4));attacker.setNoAi(true);
        h.runAfterDelay(7,()->{
            try {
                // This headless player has no real packet-listener tick to advance ServerPlayer.doTick.
                for(int i=0;i<7&&player.getTicksUsingItem()<5;i++)player.doTick();
                h.assertTrue(player.isBlocking(),"Original shield raises normally; using="+player.isUsingItem()+" ticks="+player.getTicksUsingItem());float health=player.getHealth();
                player.hurt(player.damageSources().mobAttack(attacker),4);
                h.assertTrue(player.getHealth()==health,"Native physical blocking prevents damage");
                h.assertTrue(EquipmentChecks.buffs(player),"Actual blocking grants both native Ars buffs");
                h.assertTrue(!shield.isDamageableItem()||shield.getDamageValue()>0,"Damageable original shield still takes its own blocking wear");
                EquipmentChecks.clearBuffs(player);player.stopUsingItem();player.invulnerableTime=0;
                player.hurt(player.damageSources().mobAttack(attacker),1);
                h.assertTrue(!EquipmentChecks.buffs(player),"Holding an unused fused shield does not grant block buffs");h.succeed();
            } finally {attacker.discard();player.discard();}
        });
    }
    public static void shieldRepair(GameTestHelper h,ServerPlayer player) {
        var control=ShieldFusion.shield().getDefaultInstance();control.setDamageValue(30);
        var fused=ShieldFusion.fuse(Items.SHIELD.getDefaultInstance(),ShieldFusion.shield().getDefaultInstance());fused.setDamageValue(30);
        // Neither stack is held/inventory-ticked automatically. Wait for native RepairingPerk's interval.
        h.succeedWhen(()->{
            h.assertTrue(h.getLevel().getGameTime()%200==0,"Waiting for native repair interval");
            SwordChecks.mana(player,1000);control.inventoryTick(h.getLevel(),player,0,false);double mana=SwordChecks.mana(player);
            SwordChecks.mana(player,1000);fused.inventoryTick(h.getLevel(),player,0,false);
            h.assertTrue(fused.getDamageValue()==control.getDamageValue()&&fused.getDamageValue()<30&&SwordChecks.mana(player)==mana,"Native repairing behavior and mana fee match exactly, without donor durability");
            SwordChecks.mana(player,0);int before=fused.getDamageValue();fused.inventoryTick(h.getLevel(),player,0,false);h.assertTrue(fused.getDamageValue()==before,"No free repair without native required mana");
        });
    }
}
