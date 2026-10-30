package dev.arsfusion.compat;
import io.redspace.ironsspellbooks.api.item.ISpellbook;
import io.redspace.ironsspellbooks.item.weapons.StaffItem;
import net.minecraft.world.item.ItemStack;
/** Optional staff recognition. Iron spellbooks are excluded from both sides of caster fusion. */
public final class IronCaster {
    public static boolean book(ItemStack stack) {return stack.getItem() instanceof ISpellbook;}
    public static boolean supports(ItemStack stack) {return stack.getItem() instanceof StaffItem;}
}
