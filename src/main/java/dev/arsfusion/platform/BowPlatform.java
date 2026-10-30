package dev.arsfusion.platform;

import com.hollingsworth.arsnouveau.api.registry.SpellCasterRegistry;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.items.SpellArrow;
import dev.arsfusion.bow.*;
import dev.arsfusion.compat.ConfluxBowCasting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.*;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BowPlatform {
    public static final String PAYLOAD_KEY="ars_fusion:bow_payload";
    private static final DeferredRegister<DataComponentType<?>> TYPES=DeferredRegister.create(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE,"ars_fusion");
    private static final java.util.function.Supplier<DataComponentType<Boolean>> TYPE=TYPES.register("bow_fusion",()->DataComponentType.<Boolean>builder().persistent(com.mojang.serialization.Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    private BowPlatform() {}
    private static boolean conflux() {return net.neoforged.fml.ModList.get().isLoaded("ars_conflux");}
    public static boolean has(ItemStack stack) {return stack.getOrDefault(TYPE.get(),false);}
    public static void mark(ItemStack stack) {stack.set(TYPE.get(),true);}
    public static ItemStack prepare(ItemStack stack,LivingEntity actor) {
        var tool=stack.copy();var caster=SpellCasterRegistry.from(tool);var spell=caster.getSpell();
        spell=caster.modifySpellBeforeCasting((net.minecraft.server.level.ServerLevel)actor.level(),actor,actor.getUsedItemHand(),spell);
        var mutable=spell.mutable();
        if(actor instanceof Player player&&player.getProjectile(stack).getItem() instanceof SpellArrow arrow)arrow.modifySpell(mutable);
        caster.setSpell(mutable.immutable()).saveToStack(tool);return tool;
    }
    public static SpellResolver resolver(ItemStack tool,LivingEntity actor) {
        var caster=SpellCasterRegistry.from(tool);
        return new SpellResolver(SpellContext.fromEntity(caster.getSpell(),actor,tool)).withSilent(true);
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
        var data=new CompoundTag();data.putUUID("Caster",payload.caster());data.put("Tool",payload.tool().save(world.registryAccess()));tag.put(PAYLOAD_KEY,data);
    }
    public static void load(SpellCarryingProjectile carrier,CompoundTag tag,Level world) {
        carrier.arsFusion$payload(null);
        if(!tag.contains(PAYLOAD_KEY,Tag.TAG_COMPOUND))return;
        var data=tag.getCompound(PAYLOAD_KEY);if(!data.hasUUID("Caster"))return;
        var tool=ItemStack.parseOptional(world.registryAccess(),data.getCompound("Tool"));
        if(BowFusion.has(tool)&&SpellCasterRegistry.from(tool)!=null)carrier.arsFusion$payload(new BowPayload(data.getUUID("Caster"),tool));
    }
    public static void register(net.neoforged.bus.api.IEventBus bus) {
        TYPES.register(bus);
        NeoForge.EVENT_BUS.addListener((EntityJoinLevelEvent e)->{if(!e.getLevel().isClientSide)BowCasting.spawned(e.getEntity());});
        NeoForge.EVENT_BUS.addListener(net.neoforged.bus.api.EventPriority.LOWEST,(ProjectileImpactEvent e)->BowImpacts.impact(e.getProjectile(),e.getRayTraceResult(),e::isCanceled));
        NeoForge.EVENT_BUS.addListener((LivingDamageEvent.Post e)->BowImpacts.damaged(e.getSource().getDirectEntity(),e.getEntity(),e.getNewDamage()));
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post e)->BowImpacts.tick());
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.server.ServerStoppedEvent e)->BowImpacts.stop());
    }
}
