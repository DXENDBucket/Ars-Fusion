package dev.arsfusion.compat;

import dev.arsfusion.api.ArmorFusionAdapter;
import io.redspace.ironsspellbooks.api.spells.IPresetSpellContainer;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.item.armor.ExtendedArmorItem;
import net.minecraft.world.item.ItemStack;

/** Loaded only when Iron is present. Native spell data remains an explicit integration. */
public final class IronArmor implements ArmorFusionAdapter {
    public static boolean accepts(ItemStack stack) {
        return stack.getItem() instanceof ExtendedArmorItem;
    }

    @Override public boolean supports(ItemStack donor) {
        return accepts(donor);
    }

    @Override public void prepareDonor(ItemStack donor) {
        if (donor.getItem() instanceof IPresetSpellContainer preset) preset.initializeSpellContainer(donor);
    }

    @Override public void transferData(ItemStack donor, ItemStack result) {
        var spells = ISpellContainer.get(donor);
        if (spells != null) ISpellContainer.set(result, spells.mutableCopy().toImmutable());
    }
}
