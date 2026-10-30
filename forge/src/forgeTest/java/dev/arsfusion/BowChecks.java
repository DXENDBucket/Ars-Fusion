package dev.arsfusion;
import com.hollingsworth.arsnouveau.api.spell.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
public final class BowChecks {
    public static void mana(net.minecraft.server.level.ServerPlayer player,int n) {
        // Ars 4 recalculates the cap when tracking each new entity. Configure the real attribute too.
        player.getAttribute(com.hollingsworth.arsnouveau.api.perk.PerkAttributes.MAX_MANA.get()).setBaseValue(20000);
        com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry.getMana(player).ifPresent(m->{m.setMaxMana(20000);m.setMana(n);});
    }
    public static void write(ItemStack tool,Spell spell) {new SpellCaster(tool).setSpell(spell);}
    public static int useDuration(ItemStack tool,LivingEntity actor) {return tool.getUseDuration();}
    public static void impact(Projectile arrow,HitResult hit,boolean cancel) {
        var event=new net.minecraftforge.event.entity.ProjectileImpactEvent(arrow,hit);event.setCanceled(cancel);net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(event);
    }
}
