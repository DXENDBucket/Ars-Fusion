package dev.arsfusion.api;

import dev.arsfusion.compat.IronArmor;
import dev.arsfusion.platform.FusionPlatform;
import net.minecraft.world.item.ItemStack;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/** Manual integrations take precedence over the optional attribute-only fallback. */
public final class ArmorDonors {
    private static final CopyOnWriteArrayList<ArmorFusionAdapter> ADAPTERS = new CopyOnWriteArrayList<>();
    private static boolean defaultsInstalled;
    private ArmorDonors() {}

    /** Register on both sides during mod setup. Later, more-specific integrations take priority. */
    public static void register(ArmorFusionAdapter adapter) {
        ADAPTERS.add(0,Objects.requireNonNull(adapter));
    }

    public static ArmorFusionAdapter manualAdapter(ItemStack donor) {
        for (var adapter : ADAPTERS) if (adapter.supports(donor)) return adapter;
        return null;
    }

    public static synchronized void installDefaults() {
        if (defaultsInstalled) return;
        defaultsInstalled = true;
        if (FusionPlatform.ironLoaded()) register(new IronArmor());
    }
}
