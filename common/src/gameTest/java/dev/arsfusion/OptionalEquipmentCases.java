package dev.arsfusion;

import dev.arsfusion.crossbow.CrossbowFusion;
import dev.arsfusion.shield.ShieldFusion;
import dev.arsfusion.platform.EquipmentPlatform;
import dev.arsfusion.config.FusionConfig;
import dev.arsfusion.bow.SpellCarryingProjectile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Registry-only references: these native fixtures never become production dependencies. */
public final class OptionalEquipmentCases {
    private static Item item(String id) {return BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(id));}
    public static void autoloader(GameTestHelper h,ServerPlayer player) {
        var item=item("irons_spellbooks:autoloader_crossbow");if(item==Items.AIR) {h.succeed();return;}
        h.getLevel().addNewPlayer(player);BowChecks.mana(player,10000);
        try {
            var bound=CrossbowFusion.fuse(item.getDefaultInstance(),EquipmentFusionCases.donor());h.assertTrue(!bound.isEmpty(),"Native Iron autoloader is eligible without a dedicated adapter");
            var ammo=new ItemStack(Items.ARROW,8);player.setItemInHand(InteractionHand.MAIN_HAND,bound);player.getInventory().setItem(9,ammo);double mana=SwordChecks.mana(player);
            bound.use(h.getLevel(),player,InteractionHand.MAIN_HAND);
            for(int i=0;i<110;i++)bound.inventoryTick(h.getLevel(),player,0,true);
            h.assertTrue(!EquipmentPlatform.chargedAmmo(bound).isEmpty()&&SwordChecks.mana(player)==mana,"Native autoload inventory ticks fill the crossbow without magic payment");
            var shots=EquipmentFusionCases.fire(h,player,bound,InteractionHand.MAIN_HAND);
            h.assertTrue(shots.size()==1&&((SpellCarryingProjectile)shots.get(0)).arsFusion$payload()!=null,"Autoloader's original use fires a real arrow carrying Ars magic");shots.forEach(Entity::discard);
            int afterShot=ammo.getCount();for(int i=0;i<110;i++)bound.inventoryTick(h.getLevel(),player,0,true);
            h.assertTrue(!EquipmentPlatform.chargedAmmo(bound).isEmpty()&&ammo.getCount()<afterShot,"Automatic reload after firing still works");h.succeed();
        } finally {player.discard();}
    }
    public static void revolver(GameTestHelper h,ServerPlayer player) {
        var item=item("goety:revolver_crossbow");if(item==Items.AIR) {h.succeed();return;}
        h.getLevel().addNewPlayer(player);BowChecks.mana(player,10000);
        try {
            var bound=CrossbowFusion.fuse(item.getDefaultInstance(),EquipmentFusionCases.donor());h.assertTrue(!bound.isEmpty(),"Untagged native Goety revolver crossbow is automatically eligible");
            player.setItemInHand(InteractionHand.MAIN_HAND,bound);var ammo=new ItemStack(Items.ARROW,32);player.getInventory().setItem(9,ammo);
            bound.use(h.getLevel(),player,InteractionHand.MAIN_HAND);bound.releaseUsing(h.getLevel(),player,BowChecks.useDuration(bound,player)-100);player.stopUsingItem();
            h.assertTrue(!EquipmentPlatform.chargedAmmo(bound).isEmpty(),"Native revolver magazine loads");int fired=0;
            for(int i=0;i<3&&!EquipmentPlatform.chargedAmmo(bound).isEmpty();i++) {
                var shots=EquipmentFusionCases.fire(h,player,bound,InteractionHand.MAIN_HAND);
                h.assertTrue(!shots.isEmpty(),"Each magazine use fires native projectiles");
                for(var p:shots)h.assertTrue(((SpellCarryingProjectile)p).arsFusion$payload()!=null,"Each repeated native shot receives an independently paid payload");
                fired++;shots.forEach(Entity::discard);
                bound=SwordChecks.roundTrip(h,bound);player.setItemInHand(InteractionHand.MAIN_HAND,bound);
            }
            h.assertTrue(fired>=2,"Retains multi-use native magazine, including save/network round trips");h.succeed();
        } finally {player.discard();}
    }
    public static void cataclysmPolicyAndDash(GameTestHelper h,ServerPlayer player) {
        var bulwark=item("cataclysm:bulwark_of_the_flame");if(bulwark==Items.AIR) {h.succeed();return;}
        for(var id:new String[]{"cataclysm:bulwark_of_the_flame","cataclysm:black_steel_targe","cataclysm:azure_sea_shield"}) {
            var source=item(id);h.assertTrue(source!=Items.AIR,"Expected native Cataclysm shield "+id);var fused=ShieldFusion.fuse(source.getDefaultInstance(),ShieldFusion.shield().getDefaultInstance());
            h.assertTrue(ShieldFusion.has(fused)&&fused.is(source)&&EquipmentPlatform.canBlock(fused),"Shield action, not ShieldItem inheritance or upstream tags, permits "+id);
        }
        boolean old=FusionConfig.AUTOMATIC_SHIELD_COMPATIBILITY.get();
        try {FusionConfig.AUTOMATIC_SHIELD_COMPATIBILITY.set(false);h.assertTrue(!ShieldFusion.canFuse(bulwark.getDefaultInstance(),ShieldFusion.shield().getDefaultInstance()),"Disabling auto compatibility requires target tags for untagged shield");}
        finally {FusionConfig.AUTOMATIC_SHIELD_COMPATIBILITY.set(old);}
        h.getLevel().addNewPlayer(player);
        try {
            var nativeShield=bulwark.getDefaultInstance();var fused=ShieldFusion.fuse(bulwark.getDefaultInstance(),ShieldFusion.shield().getDefaultInstance());
            player.setShiftKeyDown(true); // Native Bulwark's dash is a sneaking release action.
            player.setItemInHand(InteractionHand.MAIN_HAND,nativeShield);player.setDeltaMovement(Vec3.ZERO);nativeShield.use(h.getLevel(),player,InteractionHand.MAIN_HAND);nativeShield.releaseUsing(h.getLevel(),player,BowChecks.useDuration(nativeShield,player)-4);var dash=player.getDeltaMovement();player.stopUsingItem();
            h.assertTrue(dash.lengthSqr()>0&&player.getCooldowns().isOnCooldown(bulwark),"Native Bulwark baseline actually dashes and starts its own cooldown");player.getCooldowns().removeCooldown(bulwark);
            player.setDeltaMovement(Vec3.ZERO);player.setItemInHand(InteractionHand.MAIN_HAND,fused);fused.use(h.getLevel(),player,InteractionHand.MAIN_HAND);fused.releaseUsing(h.getLevel(),player,BowChecks.useDuration(fused,player)-4);
            h.assertTrue(player.getDeltaMovement().distanceTo(dash)<0.001&&player.getCooldowns().isOnCooldown(bulwark),"Fusion retains original flame shield dash magnitude and native cooldown");player.stopUsingItem();h.succeed();
        } finally {player.setShiftKeyDown(false);player.discard();}
    }
    public static void cataclysmActualBlock(GameTestHelper h,ServerPlayer player) {
        var bulwark=item("cataclysm:bulwark_of_the_flame");if(bulwark==Items.AIR) {h.succeed();return;}
        EquipmentFusionCases.shieldBlocking(h,player,bulwark);
    }
}
