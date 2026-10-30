package dev.arsfusion.compat;

import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import dev.arsconflux.api.casting.*;
import dev.arsfusion.platform.BowPlatform;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Loaded only when Conflux is present. An accepted payload acknowledges the active use at
 * launch; a later impact resolves its captured context without preparing the bow again. */
public final class ConfluxBowCasting {
    private ConfluxBowCasting() {}
    public static void active(ItemStack tool,LivingEntity actor,Runnable magic,Runnable physical) {
        var spell=BowPlatform.resolver(tool,actor).spell;
        var hand=actor.getUsedItemHand();
        // The supplier runs only after admission/chant. A denial still permits ordinary physical shooting.
        boolean admitted=ActiveCasts.perform(spell,actor,hand,ActiveCastSource.CASTER_TOOL,()->{magic.run();return true;},false,true);
        if(!admitted)physical.run();
    }
    public static boolean admit(SpellResolver resolver) {return ActiveCasts.allowDispatch(resolver);}
    public static void dispatched(SpellResolver resolver) {ActiveCasts.dispatched(resolver,true);}
    public static void resolve(SpellResolver resolver,net.minecraft.server.level.ServerLevel world,
                               net.minecraft.world.phys.HitResult hit,net.minecraft.world.phys.Vec3 direction) {
        dev.arsconflux.api.context.ResolverContexts.resolveFrom(resolver,world,hit,direction);
    }
}
