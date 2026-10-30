package dev.arsfusion.api;

import net.minecraft.world.item.ItemStack;

/** Optional, explicit armor integration. Attributes are transferred by the shared engine. */
public interface ArmorFusionAdapter {
    boolean supports(ItemStack donor);

    /** Initialize native defaults on the one-item donor snapshot, never on the consumed input. */
    default void prepareDonor(ItemStack donor) {}

    /** Transfer supported extra casting data to the copied Ars item. */
    default void transferData(ItemStack donor, ItemStack result) {}
}
