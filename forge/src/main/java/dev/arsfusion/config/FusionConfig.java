package dev.arsfusion.config;
import net.minecraftforge.common.ForgeConfigSpec;

/** Server policy is synced to clients. Disabling crafting never erases existing equipment. */
public final class FusionConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ARMOR_FUSION_ENABLED;
    public static final ForgeConfigSpec.BooleanValue TOUGHNESS_ENABLED;
    public static final ForgeConfigSpec.DoubleValue ARS_ARMOR_TOUGHNESS_PER_TIER;
    static {
        var builder = new ForgeConfigSpec.Builder();
        builder.push("armor_fusion");
        ARMOR_FUSION_ENABLED = builder.comment("Allow one vanilla-anvil fusion: left Ars armor + right same-slot Iron armor, for one XP level.",
                "Keeps Ars appearance, threads, upgrades, durability and enchantments. Transfers donor attributes except armor/toughness and native spell container.",
                "Requires Iron's Spells 'n Spellbooks; Iron & Nouveau is not required. Existing fused armor keeps working when disabled.")
                .define("enabled", false);
        builder.pop().push("ars_armor_toughness");
        TOUGHNESS_ENABLED = builder.comment("Add toughness to all Ars-perk armor, including unfused armor. Disabled by default.").define("enabled", false);
        ARS_ARMOR_TOUGHNESS_PER_TIER = builder.comment("Extra toughness per piece = actual displayed Ars armor tier * this value.",
                "Reads the armor upgrade tier, not the armor style or an individual thread/slot. No tier-3 cap. Re-equip after changing at runtime.")
                .defineInRange("per_tier", 1.0, 0.0, 1000.0);
        builder.pop(); SPEC = builder.build();
    }
    private FusionConfig() {}
    private static <T> T read(ForgeConfigSpec.ConfigValue<T> value) {return SPEC.isLoaded() ? value.get() : value.getDefault();}
    public static boolean armorFusionEnabled() {return read(ARMOR_FUSION_ENABLED);}
    public static boolean toughnessEnabled() {return read(TOUGHNESS_ENABLED);}
    public static double arsArmorToughnessPerTier() {return toughnessEnabled() ? read(ARS_ARMOR_TOUGHNESS_PER_TIER) : 0.0;}
}
