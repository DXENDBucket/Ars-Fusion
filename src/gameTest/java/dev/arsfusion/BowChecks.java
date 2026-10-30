package dev.arsfusion;
import com.hollingsworth.arsnouveau.api.spell.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
public final class BowChecks {
    public static void mana(net.minecraft.server.level.ServerPlayer player,int n) {var m=com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry.getMana(player);m.setMaxMana(20000);m.setMana(n);}
    public static void write(ItemStack tool,Spell spell) {new SpellCaster(1).setSpell(spell).saveToStack(tool);}
    public static int useDuration(ItemStack tool,LivingEntity actor) {return tool.getUseDuration(actor);}
    public static void impact(Projectile arrow,HitResult hit,boolean cancel) {
        var event=new net.neoforged.neoforge.event.entity.ProjectileImpactEvent(arrow,hit);event.setCanceled(cancel);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(event);
    }
}
