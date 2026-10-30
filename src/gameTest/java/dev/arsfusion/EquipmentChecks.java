package dev.arsfusion;
import com.hollingsworth.arsnouveau.setup.registry.ModPotions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
public final class EquipmentChecks {
    public static void multishot(GameTestHelper h,ItemStack stack) {stack.enchant(h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.MULTISHOT),1);}
    public static boolean buffs(ServerPlayer player) {var a=player.getEffect(ModPotions.MANA_REGEN_EFFECT);var b=player.getEffect(ModPotions.SPELL_DAMAGE_EFFECT);return a!=null&&b!=null&&a.getAmplifier()==1&&b.getAmplifier()==1&&a.getDuration()==200&&b.getDuration()==200;}
    public static void clearBuffs(ServerPlayer player) {player.removeEffect(ModPotions.MANA_REGEN_EFFECT);player.removeEffect(ModPotions.SPELL_DAMAGE_EFFECT);}
}
