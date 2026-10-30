package dev.arsfusion.bow;

import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import dev.arsfusion.platform.BowPlatform;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import java.util.UUID;

/** Snapshot, never the mutable held bow. No payload is inherited by secondary projectiles. */
public final class BowPayload {
    private final UUID caster;
    private final ItemStack tool;
    private SpellResolver prepared;
    public BowPayload(UUID caster,ItemStack tool) {this.caster=caster;this.tool=tool.copy();}
    public BowPayload(LivingEntity caster,ItemStack tool,SpellResolver resolver) {this(caster.getUUID(),tool);this.prepared=resolver;}
    public UUID caster() {return caster;}
    public ItemStack tool() {return tool.copy();}
    public void resolve(ServerLevel world,HitResult hit,net.minecraft.world.phys.Vec3 direction) {
        if(!(world.getEntity(caster) instanceof LivingEntity actor)||!actor.isAlive())return;
        // Clone per impact: piercing and repeated hits must not share a delayed/mutated context.
        var resolver=prepared==null||prepared.spellContext.getUnwrappedCaster()!=actor?BowPlatform.resolver(tool,actor):prepared.clone();
        BowPlatform.resolve(resolver,world,hit,direction);
    }
}
