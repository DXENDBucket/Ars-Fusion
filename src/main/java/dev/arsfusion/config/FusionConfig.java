package dev.arsfusion.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/** Server policy is synchronized to clients. Crafting toggles never erase fused data. */
public final class FusionConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue SWORD_FUSION_ENABLED;
    public static final ModConfigSpec.BooleanValue ARMOR_FUSION_ENABLED;
    public static final ModConfigSpec.BooleanValue AUTOMATIC_ARMOR_COMPATIBILITY;
    public static final ModConfigSpec.BooleanValue DISABLE_MANUAL_COMPATIBILITY;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ATTRIBUTE_BLACKLIST;
    public static final ModConfigSpec.BooleanValue TOUGHNESS_ENABLED;
    public static final ModConfigSpec.DoubleValue ARS_ARMOR_TOUGHNESS_PER_TIER;
    private static List<? extends String> cachedEntries = List.of();
    private static Set<String> cachedBlacklist = Set.of();

    static {
        var builder = new ModConfigSpec.Builder();
        builder.push("sword_fusion");
        SWORD_FUSION_ENABLED=builder.comment("Allow one anvil fusion: receiving tool on the left, Enchanter's Sword on the right, for one XP level.",
                "Targets use spell_fusion_targets and spell_fusion_blacklist item tags; max stack size must be one and existing Ars casting is rejected.",
                "Only native casting data/behavior is added. Does not copy donor damage, speed, durability or enchantments. Existing bindings survive disabling crafting.")
                .define("enabled",true);
        builder.pop();
        builder.push("armor_fusion");
        ARMOR_FUSION_ENABLED = builder.comment("Enable vanilla-anvil fusion: left Ars armor + right same-slot donor armor, for one XP level. Enabled by default.",
                "Keeps Ars appearance, threads, upgrades, durability and enchantments. Existing fused equipment retains its data when crafting is disabled.")
                .define("enabled", true);
        AUTOMATIC_ARMOR_COMPATIBILITY = builder.comment("Allow other non-Ars ArmorItem armor as donors when no manual adapter matches. Enabled by default.",
                "Only copies item Attribute Modifiers for the matching slot, filtered by attribute_blacklist.",
                "Does not copy capabilities, special passives, enchantments or spell containers. Manual adapters such as Iron remain available when this is false.")
                .define("automatic_compatibility", true);
        DISABLE_MANUAL_COMPATIBILITY = builder.comment("Force every donor through attribute-only automatic compatibility, including donors with a manual adapter.",
                "Only effective when both enabled and automatic_compatibility are true. Default false preserves explicit integrations such as Iron spell containers.",
                "Affects creation only; never removes casting data from already fused items.")
                .define("disable_manual_compatibility", false);
        ATTRIBUTE_BLACKLIST = builder.comment("Attribute registry IDs excluded from donor modifier transfer, for both manual and automatic fusion.",
                "Default excludes armor and armor toughness. Other modifiers retain their native value and operation.",
                "Unknown valid IDs are harmless; an empty list allows every donor modifier. Re-equip after changing at runtime.")
                .defineListAllowEmpty("attribute_blacklist", () -> List.of("minecraft:generic.armor", "minecraft:generic.armor_toughness"), () -> "minecraft:generic.armor", FusionConfig::validAttribute);
        builder.pop().push("ars_armor_toughness");
        TOUGHNESS_ENABLED = builder.comment("Add toughness to all Ars-perk armor, fused or not. Disabled by default.")
                .define("enabled", false);
        ARS_ARMOR_TOUGHNESS_PER_TIER = builder.comment("Extra toughness per piece = armor upgrade tier * this value.",
                "All three native armor sets separately progress through tiers 1/2/3; does not use the armor style or an individual thread/slot level.",
                "No tier-three cap is imposed here. Re-equip after changing at runtime.")
                .defineInRange("per_tier", 1.0, 0.0, 1000.0);
        builder.pop();
        SPEC = builder.build();
    }

    private FusionConfig() {}
    private static <T> T read(ModConfigSpec.ConfigValue<T> value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }
    private static boolean validAttribute(Object value) {
        return value instanceof String id && ResourceLocation.tryParse(id) != null;
    }
    public static boolean swordFusionEnabled() {return read(SWORD_FUSION_ENABLED);}
    public static boolean armorFusionEnabled() { return read(ARMOR_FUSION_ENABLED); }
    public static boolean automaticArmorCompatibility() { return read(AUTOMATIC_ARMOR_COMPATIBILITY); }
    public static boolean automaticOnly() {
        return armorFusionEnabled() && automaticArmorCompatibility() && read(DISABLE_MANUAL_COMPATIBILITY);
    }
    public static boolean toughnessEnabled() { return read(TOUGHNESS_ENABLED); }
    public static double arsArmorToughnessPerTier() {
        return toughnessEnabled() ? read(ARS_ARMOR_TOUGHNESS_PER_TIER) : 0.0;
    }
    public static synchronized boolean allowsAttribute(String id) {
        var entries = read(ATTRIBUTE_BLACKLIST);
        if (!entries.equals(cachedEntries)) {
            var blacklist = new HashSet<String>();
            for (var entry : entries) {
                var key = ResourceLocation.tryParse(entry);
                if (key != null) blacklist.add(key.toString());
            }
            cachedEntries = List.copyOf(entries);
            cachedBlacklist = Set.copyOf(blacklist);
        }
        return !cachedBlacklist.contains(id);
    }
}
