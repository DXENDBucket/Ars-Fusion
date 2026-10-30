package dev.arsfusion;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentDampen;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectHarm;
import com.hollingsworth.arsnouveau.common.spell.method.MethodProjectile;
import dev.arsconflux.api.casting.*;
import dev.arsfusion.bow.*;
import dev.arsfusion.crossbow.CrossbowFusion;
import dev.arsfusion.platform.EquipmentPlatform;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.projectile.Projectile;
import java.util.*;

public final class ConfluxCrossbowCases {
    public static void run(GameTestHelper h,ServerPlayer player) {
        var released=new int[1];var blocked=new HashSet<UUID>();
        ActiveCasts.register(net.minecraft.resources.ResourceLocation.tryParse("ars_fusion:crossbow_test_"+player.getUUID()),new ActiveCastProvider() {
            public boolean supports(Spell spell) {var parts=dev.arsconflux.api.glyph.ArsSpellAccess.parts(spell);return parts.size()==4&&parts.get(1)==EffectHarm.INSTANCE&&parts.get(2)==AugmentDampen.INSTANCE&&parts.get(3)==AugmentDampen.INSTANCE;}
            public boolean chantingEnabled(ActiveCastRequest request) {return true;}
            public List<Integer> chantTicks(ActiveCastRequest request) {return List.of(2);}
            public boolean allowed(ActiveCastRequest request) {return !blocked.contains(request.caster().getUUID());}
            public void released(ActiveCastRequest request,List<Spell> recipes) {released[0]++;blocked.add(request.caster().getUUID());}
        });
        h.getLevel().addNewPlayer(player);BowChecks.mana(player,10000);
        var donor=EquipmentFusionCases.donor();BowChecks.write(donor,new Spell(MethodProjectile.INSTANCE,EffectHarm.INSTANCE,AugmentDampen.INSTANCE,AugmentDampen.INSTANCE));
        var bow=CrossbowFusion.fuse(Items.CROSSBOW.getDefaultInstance(),donor);
        EquipmentFusionCases.load(h,player,bow,new ItemStack(Items.ARROW,4),InteractionHand.OFF_HAND);
        h.assertTrue(!EquipmentPlatform.chargedAmmo(bow).isEmpty()&&!ActiveCasts.isChanting(player),"Physical crossbow loading is not a magic cast");
        var shots=EquipmentFusionCases.fire(h,player,bow,InteractionHand.OFF_HAND);
        h.assertTrue(shots.isEmpty()&&!EquipmentPlatform.chargedAmmo(bow).isEmpty()&&ActiveCasts.isChanting(player),"Offhand firing queues native use without clearing loaded ammo");
        h.runAfterDelay(5,()->{
            try {
                var arrows=new ArrayList<Projectile>();for(var entity:h.getLevel().getAllEntities())if(entity instanceof Projectile p&&p.getOwner()==player)arrows.add(p);
                h.assertTrue(arrows.size()==1&&((SpellCarryingProjectile)arrows.get(0)).arsFusion$payload()!=null&&EquipmentPlatform.chargedAmmo(bow).isEmpty()&&released[0]==1,"One queued release, payload and cooldown acknowledgement");arrows.forEach(net.minecraft.world.entity.Entity::discard);
                EquipmentFusionCases.load(h,player,bow,new ItemStack(Items.ARROW,4),InteractionHand.OFF_HAND);
                var physical=EquipmentFusionCases.fire(h,player,bow,InteractionHand.OFF_HAND);
                h.assertTrue(physical.size()==1&&((SpellCarryingProjectile)physical.get(0)).arsFusion$payload()==null&&released[0]==1,"Cooldown cannot add magic through a preloaded crossbow; native physical shot remains");physical.forEach(net.minecraft.world.entity.Entity::discard);h.succeed();
            } finally {blocked.clear();player.discard();}
        });
    }
}
