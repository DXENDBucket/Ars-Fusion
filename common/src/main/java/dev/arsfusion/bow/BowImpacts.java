package dev.arsfusion.bow;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.*;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Observe native impact/damage without replacing collision handlers or arrow lifetimes. */
public final class BowImpacts {
    private static final List<Impact> PENDING=new ArrayList<>();
    private static final class Impact {
        final Projectile projectile; final HitResult hit; final BowPayload payload; final BooleanSupplier canceled;
        final Vec3 direction;
        BooleanSupplier damaged=()->false;
        Impact(Projectile p,HitResult h,BowPayload b,BooleanSupplier c) {projectile=p;hit=h;payload=b;canceled=c;direction=p.getDeltaMovement();}
    }
    private BowImpacts() {}
    public static void impact(Projectile projectile,HitResult hit,BooleanSupplier canceled) {
        if(!(projectile.level() instanceof ServerLevel)||hit.getType()==HitResult.Type.MISS)return;
        var payload=((SpellCarryingProjectile)projectile).arsFusion$payload();
        if(payload!=null)PENDING.add(new Impact(projectile,hit,payload,canceled));
    }
    public static void damaged(Entity direct,Entity victim,float amount) {
        damaged(direct,victim,amount,()->true);
    }
    public static void damaged(Entity direct,Entity victim,float amount,BooleanSupplier accepted) {
        if(amount<=0||!(direct instanceof Projectile))return;
        for(int i=PENDING.size()-1;i>=0;i--) {
            var impact=PENDING.get(i);
            if(impact.projectile==direct&&impact.hit instanceof EntityHitResult entityHit&&entityHit.getEntity()==victim) {
                impact.damaged=accepted;break;
            }
        }
    }
    public static void tick() {
        var impacts=List.copyOf(PENDING);PENDING.clear();
        for(var impact:impacts)if(!impact.canceled.getAsBoolean()
                &&(impact.hit instanceof BlockHitResult||impact.damaged.getAsBoolean())
                &&impact.projectile.level() instanceof ServerLevel world)
            impact.payload.resolve(world,impact.hit,impact.direction);
    }
    public static void stop() {PENDING.clear();}
}
