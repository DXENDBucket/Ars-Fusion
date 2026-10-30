package dev.arsfusion.bow;

import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.common.items.SpellArrow;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import dev.arsfusion.platform.BowPlatform;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import java.util.*;
import java.util.function.Predicate;

public final class BowCasting {
    private record Launch(List<Projectile> projectiles) {}
    private static final ThreadLocal<Launch> LAUNCH=new ThreadLocal<>();
    private BowCasting() {}
    public static Predicate<ItemStack> ammo(Predicate<ItemStack> original,ItemStack weapon) {
        return BowFusion.has(weapon)?original.or(stack->stack.getItem() instanceof SpellArrow):original;
    }
    public static void release(Item item,ItemStack stack,net.minecraft.world.level.Level world,LivingEntity actor,int remaining) {
        Runnable physical=()->item.releaseUsing(stack,world,actor,remaining);
        if(!BowFusion.has(stack)||world.isClientSide) {physical.run();return;}
        BowPlatform.active(stack.copy(),actor,()->capture(BowPlatform.prepare(stack,actor),actor,physical),physical);
    }
    private static void capture(ItemStack tool,LivingEntity actor,Runnable physical) {
        var previous=LAUNCH.get();var scope=new Launch(new ArrayList<>());LAUNCH.set(scope);
        try {physical.run();} finally {if(previous==null)LAUNCH.remove();else LAUNCH.set(previous);}
        if(!(actor.level() instanceof ServerLevel world))return;
        for(var projectile:scope.projectiles) {
            if(projectile.getOwner()!=actor||world.getEntity(projectile.getUUID())!=projectile
                    ||projectile instanceof com.hollingsworth.arsnouveau.common.entity.EntitySpellArrow)continue;
            var resolver=attach(projectile,actor,tool);
            if(resolver!=null&&projectile instanceof AbstractArrow arrow)split(arrow,actor,tool,resolver);
        }
    }
    /** Only synchronous native launch spawns are observed, never unrelated arrows owned by this player. */
    public static void spawned(Entity entity) {
        var scope=LAUNCH.get();
        if(scope!=null&&entity instanceof Projectile projectile)scope.projectiles.add(projectile);
    }
    private static SpellResolver attach(Projectile projectile,LivingEntity actor,ItemStack tool) {
        var carrier=(SpellCarryingProjectile)projectile;
        if(carrier.arsFusion$payload()!=null)return null;
        var resolver=BowPlatform.resolver(tool,actor);
        if(!resolver.canCast(actor)||!BowPlatform.admit(resolver))return null;
        if(!(actor instanceof Player player)||!player.isCreative())resolver.expendMana();
        carrier.arsFusion$payload(new BowPayload(actor,tool,resolver));
        if(projectile instanceof AbstractArrow arrow) {
            int pierce=resolver.spell.getBuffsAtIndex(0,actor,AugmentPierce.INSTANCE);
            ((dev.arsfusion.mixin.BowArrowAccessor)arrow).arsFusion$setPierceLevel((byte)Math.min(127,Byte.toUnsignedInt(arrow.getPierceLevel())+pierce));
        }
        BowPlatform.dispatched(resolver);
        return resolver;
    }
    private static void split(AbstractArrow original,LivingEntity actor,ItemStack tool,SpellResolver resolver) {
        int count=resolver.spell.getBuffsAtIndex(0,actor,AugmentSplit.INSTANCE);
        if(count<=0)return;
        var world=(ServerLevel)original.level();var extras=new ArrayList<AbstractArrow>();
        // Serialize the native type, keeping tracking/ammo/custom NBT. Never substitute EntitySpellArrow.
        var data=original.saveWithoutId(new CompoundTag());data.remove("UUID");data.remove(BowPlatform.PAYLOAD_KEY);
        // attach() adds Pierce independently, so clone the pre-augmentation level.
        int addedPierce=resolver.spell.getBuffsAtIndex(0,actor,AugmentPierce.INSTANCE);
        data.putByte("PierceLevel",(byte)Math.max(0,Byte.toUnsignedInt(original.getPierceLevel())-addedPierce));
        for(int i=0;i<count;i++) {
            var entity=original.getType().create(world);
            if(!(entity instanceof AbstractArrow extra))break;
            extra.load(data.copy());extra.setUUID(UUID.randomUUID());extra.setOwner(actor);
            extra.pickup=AbstractArrow.Pickup.CREATIVE_ONLY;
            // Canceled spawns never cost mana. Unsupported/save-less types keep their original shot.
            if(!world.addFreshEntity(extra))continue;
            if(attach(extra,actor,tool)==null) {extra.discard();break;}
            extras.add(extra);
        }
        if(extras.isEmpty())return;
        // Only Split changes the launch angles. Recenter the funded fan rather than leaving half a fan.
        var velocity=original.getDeltaMovement();extras.add(0,original);
        for(int i=0;i<extras.size();i++) {
            double angle=Math.toRadians((i-(extras.size()-1)/2.0)*10);
            var extra=extras.get(i);var motion=velocity.yRot((float)angle);
            extra.shoot(motion.x,motion.y,motion.z,(float)motion.length(),0);
        }
    }
}
