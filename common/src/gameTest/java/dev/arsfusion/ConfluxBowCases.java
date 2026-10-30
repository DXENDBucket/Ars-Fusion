package dev.arsfusion;

import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentDampen;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectHarm;
import com.hollingsworth.arsnouveau.common.spell.method.MethodProjectile;
import dev.arsconflux.api.casting.*;
import dev.arsfusion.bow.*;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import java.util.*;

/** Tests the optional public API boundary, without linking any particular glyph addon. */
public final class ConfluxBowCases {
    private static boolean registered;
    private static final Map<UUID,Integer> releases=new HashMap<>();
    private static final Set<UUID> blocked=new HashSet<>();
    public static void run(GameTestHelper h,ServerPlayer player) {
        if(!registered) {
            registered=true;
            ActiveCasts.register(net.minecraft.resources.ResourceLocation.tryParse("ars_fusion:bow_test"),new ActiveCastProvider() {
                public boolean supports(Spell spell) {var parts=dev.arsconflux.api.glyph.ArsSpellAccess.parts(spell);return parts.size()==3&&parts.get(1)==EffectHarm.INSTANCE&&parts.get(2)==AugmentDampen.INSTANCE;}
                public boolean chantingEnabled(ActiveCastRequest request) {return true;}
                public List<Integer> chantTicks(ActiveCastRequest request) {return List.of(2);}
                public boolean allowed(ActiveCastRequest request) {return !blocked.contains(request.caster().getUUID());}
                public void released(ActiveCastRequest request,List<Spell> spells) {releases.merge(request.caster().getUUID(),1,Integer::sum);blocked.add(request.caster().getUUID());}
            });
        }
        h.getLevel().addNewPlayer(player);SwordChecks.mana(player,1000);
        var bow=BowFusionCases.fused(MethodProjectile.INSTANCE,EffectHarm.INSTANCE,AugmentDampen.INSTANCE);var ammo=new ItemStack(Items.ARROW,5);
        var shots=BowFusionCases.shoot(h,player,bow,ammo);
        h.assertTrue(shots.isEmpty()&&ammo.getCount()==5&&ActiveCasts.isChanting(player),"Conflux queues the native bow release without consuming ammo early");
        h.runAfterDelay(5,()->{
            try {
                var arrows=new ArrayList<AbstractArrow>();for(var entity:h.getLevel().getAllEntities())if(entity instanceof AbstractArrow a&&a.getOwner()==player)arrows.add(a);
                h.assertTrue(arrows.size()==1&&((SpellCarryingProjectile)arrows.get(0)).arsFusion$payload()!=null,"Queued release retains native arrow and attaches magic");
                h.assertTrue(releases.getOrDefault(player.getUUID(),0)==1&&ammo.getCount()==4,"Acknowledge exactly one real release and cooldown");arrows.forEach(net.minecraft.world.entity.Entity::discard);
                var blockedShot=BowFusionCases.shoot(h,player,bow,new ItemStack(Items.ARROW,3));
                h.assertTrue(blockedShot.size()==1&&((SpellCarryingProjectile)blockedShot.get(0)).arsFusion$payload()==null,"Cooldown rejection preserves physical arrow but refuses magic");
                blockedShot.forEach(net.minecraft.world.entity.Entity::discard);h.succeed();
            } finally {blocked.remove(player.getUUID());releases.remove(player.getUUID());player.discard();}
        });
    }
}
