package dev.arsfusion.compat;

import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.item.armor.ExtendedArmorItem;
import net.minecraft.world.item.ItemStack;

/** Loaded only when Iron is present. No Iron type leaks into shared signatures. */
public final class IronArmor {
    private IronArmor() {}

    public static boolean accepts(ItemStack stack) {
        return stack.getItem() instanceof ExtendedArmorItem;
    }

    public static void initialize(ItemStack stack) {
        if (stack.getItem() instanceof IPresetSpellContainer preset) preset.initializeSpellContainer(stack);
    }

    public static void transferSpells(ItemStack donor, ItemStack result) {
        var spells = ISpellContainer.get(donor);
        if (spells != null) ISpellContainer.set(result, spells.mutableCopy().toImmutable());
    }
}
