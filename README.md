# Ars Fusion

Equipment fusion for Ars Nouveau, by EndXiom. MIT licensed.

The first release moves optional armor fusion out of Iron & Nouveau. Minecraft 1.21.1 / NeoForge and 1.20.1 / Forge share version **0.1.1**, common policy, compatibility code and integration checks.

## Armor fusion

Configure `armor_fusion.enabled` (enabled by default) in `ars_fusion-server.toml` (NeoForge 1.21.1: `config/`; Forge 1.20.1: the world's `serverconfig/`).

Use a normal anvil: **Ars armor on the left + same-slot donor armor on the right**. Fusion consumes both inputs and one XP level. Each piece can be fused once.

The result remains the original Ars armor. Its appearance, dye, enchantments, durability, threads and Ars upgrades are preserved. The donor contributes item attribute modifiers except those listed in the configurable blacklist (armor and armor toughness by default). Explicit integrations can also transfer supported native casting data, such as Iron spell containers. The Iron arcane anvil and casting selection continue to work. Donor enchantments, durability and item-class-specific special passives do not transfer automatically. Renamed armor retains its custom name.

Turning fusion off prevents new fusions; existing equipment retains its data and bonuses.

## Optional tier toughness

Independent of fusion, enable `ars_armor_toughness.enabled`. Extra toughness **per piece** is:

`actual displayed Ars armor tier × ars_armor_toughness.per_tier`

Sorcerer, Arcanist and Battlemage are three parallel armor sets. Each independently starts at tier 1 and can be upgraded twice to tiers 2 and 3. At `per_tier = 1.0`, an unupgraded / once-upgraded / twice-upgraded piece gives +1 / +2 / +3 toughness. This is the armor upgrade tier, not the tier of an individual thread or the choice of armor set. This calculation has no tier-three cap. A higher-tier addon must provide valid native Ars armor/perk data for its extra tiers; Ars Fusion does not add tiers or change native thread-slot limits. Re-equip armor after changing these values at runtime.

## Automatic armor compatibility

Enabled by default. Other standard Minecraft `ArmorItem` equipment can be donors without a dedicated integration, using its modifiers for the matching equipment slot. Amounts and modifier operations are preserved. The receiver must still be Ars armor; other Ars sets and already-fused items cannot be donors.

Manual integrations take priority. Iron armor retains its native spell-container handling even if automatic compatibility is disabled. Pure automatic transfer does not copy capabilities, special passives, spell containers or enchantments.

The default attribute blacklist is armor and armor toughness. **Vanilla netherite armor therefore contributes only knockback resistance.** You can add other attribute IDs, or use an empty blacklist to allow every donor modifier. The blacklist applies to donor bonuses in both manual and automatic integrations; it does not suppress the Ars armor's own attributes or the separate tier-toughness rule.

`disable_manual_compatibility = true` forces all donors, including Iron, through attribute-only transfer **only when both fusion and automatic compatibility are enabled**. It defaults to false. This switch affects new fusions; existing native casting data remains intact. Turning off automatic compatibility also leaves existing automatic fusions intact. Changes to the attribute blacklist govern existing donor modifiers too; re-equip equipment after changing it at runtime.

## Configuration defaults

`ars_fusion-server.toml` (NeoForge: `config/`; Forge: world's `serverconfig/`):

```toml
[armor_fusion]
enabled = true
automatic_compatibility = true
disable_manual_compatibility = false
attribute_blacklist = ["minecraft:generic.armor", "minecraft:generic.armor_toughness"]

[ars_armor_toughness]
enabled = false
per_tier = 1.0
```

The 万法归一 pack enables tier toughness as well. Pack authors should configure the loader's active server config and `defaultconfigs/ars_fusion-server.toml` for new-world defaults.

## Dependencies and migration

- Ars Nouveau is required. Ars Conflux is not required.
- Iron's Spells 'n Spellbooks is optional. Dedicated Iron armor fusion needs it; generic modifier transfer and tier toughness do not.
- Iron & Nouveau is optional. When installed alongside Ars Fusion, use **0.17.32 or newer**; older versions can apply their own fusion modifiers. This minimum is enforced by loader metadata.
- Old `irons_nouveau:armor_fusion` components / NBT remain readable. NeoForge registers the historical codec only when its former owner is absent. New fusions use `ars_fusion:armor_fusion`.
- Iron & Nouveau 0.17.32 retains old-item behavior on its own and hands off to Ars Fusion when installed. Its historical creation/toughness settings no longer enable the migrated features.

Ars Fusion ships no copied Iron or Ars art assets and no original mod JARs. Its only visual change is the combined armor name; armor keeps Ars rendering.

## Development

See [building and verification](docs/development.md). Shared policy and optional Iron adapter live in `common/`; components/NBT, events and configuration live in the platform source trees. Future weapon fusion is outside the first release.
