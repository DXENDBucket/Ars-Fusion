package dev.arsfusion.platform;

import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.items.SpellArrow;
import dev.arsfusion.bow.*;
import dev.arsfusion.compat.ConfluxBowCasting;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.TickEvent;

public final class BowPlatform {
    public static final String PAYLOAD_KEY="ars_fusion:bow_payload";
    private static final String KEY="ars_fusion:bow_fusion";
    private BowPlatform() {}
    private static boolean conflux() {return net.minecraftforge.fml.ModList.get().isLoaded("ars_conflux");}
    public static boolean has(ItemStack stack) {return stack.hasTag()&&stack.getTag().getBoolean(KEY);}
    public static void mark(ItemStack stack) {stack.getOrCreateTag().putBoolean(KEY,true);}
    public static ItemStack prepare(ItemStack stack,LivingEntity actor) {
        var tool=stack.copy();var caster=new SpellCaster(tool);
        var spell=caster.modifySpellBeforeCasting((net.minecraft.server.level.ServerLevel)actor.level(),actor,actor.getUsedItemHand(),caster.getSpell()).clone();
        if(actor instanceof Player player&&player.getProjectile(stack).getItem() instanceof SpellArrow arrow)arrow.modifySpell(spell);
        caster.setSpell(spell);return tool;
    }
    public static SpellResolver resolver(ItemStack tool,LivingEntity actor) {
        return new SpellResolver(SpellContext.fromEntity(new SpellCaster(tool).getSpell(),actor,tool)).withSilent(true);
    }
    public static void active(ItemStack tool,LivingEntity actor,Runnable magic,Runnable physical) {
        if(conflux())ConfluxBowCasting.active(tool,actor,magic,physical);else magic.run();
    }
    public static boolean admit(SpellResolver resolver) {return !conflux()||ConfluxBowCasting.admit(resolver);}
    public static void dispatched(SpellResolver resolver) {if(conflux())ConfluxBowCasting.dispatched(resolver);}
    public static void resolve(SpellResolver resolver,net.minecraft.server.level.ServerLevel world,net.minecraft.world.phys.HitResult hit,net.minecraft.world.phys.Vec3 direction) {
        if(conflux())ConfluxBowCasting.resolve(resolver,world,hit,direction);else resolver.onResolveEffect(world,hit);
    }
    public static void save(SpellCarryingProjectile carrier,CompoundTag tag,Level world) {
        var payload=carrier.arsFusion$payload();if(payload==null)return;
        var data=new CompoundTag();data.putUUID("Caster",payload.caster());data.put("Tool",payload.tool().save(new CompoundTag()));tag.put(PAYLOAD_KEY,data);
    }
    public static void load(SpellCarryingProjectile carrier,CompoundTag tag,Level world) {
        carrier.arsFusion$payload(null);
        if(!tag.contains(PAYLOAD_KEY,Tag.TAG_COMPOUND))return;
        var data=tag.getCompound(PAYLOAD_KEY);if(!data.hasUUID("Caster"))return;
        var tool=ItemStack.of(data.getCompound("Tool"));
        if(BowFusion.has(tool)&&SwordPlatform.existingCaster(tool))carrier.arsFusion$payload(new BowPayload(data.getUUID("Caster"),tool));
    }
    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {
        MinecraftForge.EVENT_BUS.addListener((EntityJoinLevelEvent e)->{if(!e.getLevel().isClientSide)BowCasting.spawned(e.getEntity());});
        MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST,(ProjectileImpactEvent e)->BowImpacts.impact(e.getProjectile(),e.getRayTraceResult(),()->e.isCanceled()
                ||e.getImpactResult()==ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE
                ||e.getImpactResult()==ProjectileImpactEvent.ImpactResult.SKIP_ENTITY&&e.getRayTraceResult() instanceof net.minecraft.world.phys.EntityHitResult));
        MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST,(LivingDamageEvent e)->{if(!e.isCanceled())BowImpacts.damaged(e.getSource().getDirectEntity(),e.getEntity(),e.getAmount(),()->!e.isCanceled()&&e.getAmount()>0);});
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent e)->{if(e.phase==TickEvent.Phase.END)BowImpacts.tick();});
        MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.server.ServerStoppedEvent e)->BowImpacts.stop());
    }
}
