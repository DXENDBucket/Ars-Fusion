package dev.arsfusion.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraft.resources.ResourceLocation;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/** Server policy is synchronized to clients. Crafting toggles never erase fused data. */
public final class FusionConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue CASTER_FUSION_ENABLED, AUTOMATIC_CASTER_COMPATIBILITY, GOETY_STAFF_FUSION_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CASTER_ATTRIBUTE_BLACKLIST;
    private static List<? extends String> cachedCasterEntries=List.of();
    private static Set<String> cachedCasterBlacklist=Set.of();
    public static final ForgeConfigSpec.BooleanValue SWORD_FUSION_ENABLED;
    public static final ForgeConfigSpec.BooleanValue CROSSBOW_FUSION_ENABLED;
    public static final ForgeConfigSpec.BooleanValue AUTOMATIC_CROSSBOW_COMPATIBILITY;
    public static final ForgeConfigSpec.BooleanValue AUTOMATIC_SHIELD_COMPATIBILITY;
    public static final ForgeConfigSpec.BooleanValue SHIELD_FUSION_ENABLED;
    public static final ForgeConfigSpec.BooleanValue BOW_FUSION_ENABLED;
    public static final ForgeConfigSpec.BooleanValue ARMOR_FUSION_ENABLED;
    public static final ForgeConfigSpec.BooleanValue REVERSE_ARMOR_FUSION_ENABLED;
    public static final ForgeConfigSpec.BooleanValue REVERSE_USES_ARS_DEFENSE;
    public static final ForgeConfigSpec.BooleanValue REVERSE_TRANSFERS_ARS_MANA;
    public static final ForgeConfigSpec.BooleanValue AUTOMATIC_ARMOR_COMPATIBILITY;
    public static final ForgeConfigSpec.BooleanValue DISABLE_MANUAL_COMPATIBILITY;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> ATTRIBUTE_BLACKLIST;
    public static final ForgeConfigSpec.BooleanValue TOUGHNESS_ENABLED;
    public static final ForgeConfigSpec.DoubleValue ARS_ARMOR_TOUGHNESS_PER_TIER;
    private static List<? extends String> cachedEntries = List.of();
    private static Set<String> cachedBlacklist = Set.of();

    static {
        var builder = new ForgeConfigSpec.Builder();
        builder.push("caster_fusion");
        CASTER_FUSION_ENABLED=builder.comment("Fuse one staff into an Ars spellbook, wand, mirror or tagged reusable caster in a normal anvil. Iron spellbooks and other spellbook materials are excluded.",
                "Preserves the receiver's native spell data, slots, casting, rendering and wear. Consumes both items and one XP level; may be done once.").define("enabled",true);
        AUTOMATIC_CASTER_COMPATIBILITY=builder.comment("Permit tagged casting materials with attribute-only transfer. Native Iron staffs and enabled Goety staff integration remain available independently.").define("automatic_compatibility",true);
        GOETY_STAFF_FUSION_ENABLED=builder.comment("New Goety staff fusions retain intrinsic native staff-type/identity traits. Does not transplant focus contents, focus enchantments or a native Goety use loop.",
                "Affects new fusions. Existing manual integration data remains usable.").define("goety_staff_compatibility",true);
        CASTER_ATTRIBUTE_BLACKLIST=builder.comment("Attribute IDs excluded from staff donor modifiers. Defaults exclude attack damage/speed, armor and toughness; amount, operation and native hand slot otherwise remain unchanged.",
                "Applies to existing fusions too; re-equip after changing.").defineListAllowEmpty("attribute_blacklist", List.of("minecraft:generic.attack_damage","minecraft:generic.attack_speed","minecraft:generic.armor","minecraft:generic.armor_toughness"), FusionConfig::validAttribute);
        builder.pop();
        builder.push("crossbow_fusion");
        CROSSBOW_FUSION_ENABLED=builder.comment("Allow anvil fusion: receiving crossbow + Enchanter's Crossbow. Preserves native loading/firing; attaches spells at firing time using loaded ammo.").define("enabled",true);
        AUTOMATIC_CROSSBOW_COMPATIBILITY=builder.comment("Allow any CrossbowItem receiver, including untagged modded crossbows. Blacklist always wins. Disable to require crossbow_fusion_targets.").define("automatic_compatibility",true);
        builder.pop().push("shield_fusion");
        SHIELD_FUSION_ENABLED=builder.comment("Allow anvil fusion: receiving shield + Enchanter's Shield. Adds native block buffs and mana-funded repair, not a spell container.").define("enabled",true);
        AUTOMATIC_SHIELD_COMPATIBILITY=builder.comment("Allow any receiver with the native shield-block action, including shields that do not extend ShieldItem. Blacklist always wins. Disable to require shield_fusion_targets.").define("automatic_compatibility",true);
        builder.pop();
        builder.push("bow_fusion");
        BOW_FUSION_ENABLED=builder.comment("Allow anvil fusion: receiving bow + Enchanter's Bow. Preserves native firing and projectile types; attaches Ars spells to successful hits.",
                "Supports native Ars augment arrows. Existing bindings survive disabling crafting.").define("enabled",true);
        builder.pop();
        builder.push("sword_fusion");
        SWORD_FUSION_ENABLED=builder.comment("Allow one anvil fusion: receiving tool on the left, Enchanter's Sword on the right, for one XP level.",
                "Targets use spell_fusion_targets and spell_fusion_blacklist item tags; max stack size must be one and existing Ars casting is rejected.",
                "Native casting and its spell-damage attribute are retained. Physical attack damage/speed, durability and enchantments do not transfer. Existing bindings survive disabling crafting.")
                .define("enabled",true);
        builder.pop();
        builder.push("armor_fusion");
        ARMOR_FUSION_ENABLED = builder.comment("Enable vanilla-anvil fusion: left Ars armor + right same-slot donor armor, for one XP level. Enabled by default.",
                "Keeps Ars appearance, threads, upgrades, durability and enchantments. Existing fused equipment retains its data when crafting is disabled.")
                .define("enabled", true);
        REVERSE_ARMOR_FUSION_ENABLED = builder.comment("Allow reversed armor fusion: other armor on the left + same-slot Ars armor on the right.",
                "Transfers native thread slots, their upgrade tier and installed threads. Defense and base mana transfer have independent switches. Keeps the receiver item and its own abilities. Requires enabled.")
                .define("reverse_enabled", true);
        REVERSE_USES_ARS_DEFENSE = builder.comment("Replace a reverse-fused receiver's armor and armor toughness modifiers with the Ars donor's material defense.",
                "Includes the optional Ars tier-toughness bonus at the result's current upgrade tier. Thread modifiers are applied separately.",
                "Keeps other attributes, enchantments and native abilities. Applies to existing fusions too; disable to retain the receiver's original defense. Re-equip after changing.")
                .define("reverse_uses_ars_defense", true);
        REVERSE_TRANSFERS_ARS_MANA = builder.comment("Also transfer the Ars donor's native maximum-mana and mana-regeneration attribute modifiers during reverse fusion.",
                "Reads the actual Ars item at the result's current upgrade tier, without installed threads. Keeps the receiver's own mana modifiers; thread bonuses apply separately.",
                "Applies to existing fusions too. Re-equip after changing.")
                .define("reverse_transfers_ars_mana", true);
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
                .defineListAllowEmpty("attribute_blacklist", List.of("minecraft:generic.armor", "minecraft:generic.armor_toughness"), FusionConfig::validAttribute);
        builder.pop().push("ars_armor_toughness");
        TOUGHNESS_ENABLED = builder.comment("Add toughness to Ars armor, including reverse fusions when reverse_uses_ars_defense is enabled. Disabled by default.")
                .define("enabled", false);
        ARS_ARMOR_TOUGHNESS_PER_TIER = builder.comment("Extra toughness per piece = armor upgrade tier * this value.",
                "All three native armor sets separately progress through tiers 1/2/3; does not use the armor style or an individual thread/slot level.",
                "No tier-three cap is imposed here. Re-equip after changing at runtime.")
                .defineInRange("per_tier", 1.0, 0.0, 1000.0);
        builder.pop();
        SPEC = builder.build();
    }

    private FusionConfig() {}
    private static <T> T read(ForgeConfigSpec.ConfigValue<T> value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }
    private static boolean validAttribute(Object value) {
        return value instanceof String id && ResourceLocation.tryParse(id) != null;
    }
    public static boolean casterFusionEnabled() {return read(CASTER_FUSION_ENABLED);}
    public static boolean automaticCasterCompatibility() {return read(AUTOMATIC_CASTER_COMPATIBILITY);}
    public static boolean goetyStaffFusionEnabled() {return read(GOETY_STAFF_FUSION_ENABLED);}
    public static synchronized boolean allowsCasterAttribute(String id) {
        var entries=read(CASTER_ATTRIBUTE_BLACKLIST);
        if(!entries.equals(cachedCasterEntries)) {
            var set=new HashSet<String>();for(var entry:entries) {var key=ResourceLocation.tryParse(entry);if(key!=null)set.add(key.toString());}
            cachedCasterEntries=List.copyOf(entries);cachedCasterBlacklist=Set.copyOf(set);
        }
        return !cachedCasterBlacklist.contains(id);
    }
    public static boolean automaticCrossbowCompatibility() {return read(AUTOMATIC_CROSSBOW_COMPATIBILITY);}
    public static boolean automaticShieldCompatibility() {return read(AUTOMATIC_SHIELD_COMPATIBILITY);}
    public static boolean crossbowFusionEnabled() {return read(CROSSBOW_FUSION_ENABLED);}
    public static boolean shieldFusionEnabled() {return read(SHIELD_FUSION_ENABLED);}
    public static boolean bowFusionEnabled() {return read(BOW_FUSION_ENABLED);}
    public static boolean swordFusionEnabled() {return read(SWORD_FUSION_ENABLED);}
    public static boolean armorFusionEnabled() { return read(ARMOR_FUSION_ENABLED); }
    public static boolean reverseArmorFusionEnabled() { return read(REVERSE_ARMOR_FUSION_ENABLED); }
    public static boolean reverseUsesArsDefense() { return read(REVERSE_USES_ARS_DEFENSE); }
    public static boolean reverseTransfersArsMana() { return read(REVERSE_TRANSFERS_ARS_MANA); }
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
