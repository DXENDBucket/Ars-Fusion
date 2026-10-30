# Ars Fusion

Equipment fusion for Ars Nouveau, by EndXiom. MIT licensed.

The first release moves optional armor fusion out of Iron & Nouveau. Minecraft 1.21.1 / NeoForge and 1.20.1 / Forge share version **0.1.0**, common policy, compatibility code and integration checks.

## Armor fusion

Enable `armor_fusion.enabled` in `ars_fusion-server.toml` (NeoForge 1.21.1: `config/`; Forge 1.20.1: the world's `serverconfig/`).

Use a normal anvil: **Ars armor on the left + same-slot Iron armor on the right**. Fusion consumes both inputs and one XP level. Each piece can be fused once.

The result remains the original Ars armor. Its appearance, dye, enchantments, durability, threads and Ars upgrades are preserved. The donor contributes attribute modifiers other than armor and armor toughness, and its native Iron spell container if supported. The Iron arcane anvil and casting selection continue to work. Donor enchantments, durability and item-class-specific special passives do not transfer. Renamed armor retains its custom name.

Turning fusion off prevents new fusions; existing equipment retains its data and bonuses.

## Optional tier toughness

Independent of fusion, enable `ars_armor_toughness.enabled`. Extra toughness **per piece** is:

`actual displayed Ars armor tier × ars_armor_toughness.per_tier`

Sorcerer, Arcanist and Battlemage are three parallel armor sets. Each independently starts at tier 1 and can be upgraded twice to tiers 2 and 3. At `per_tier = 1.0`, an unupgraded / once-upgraded / twice-upgraded piece gives +1 / +2 / +3 toughness. This is the armor upgrade tier, not the tier of an individual thread or the choice of armor set. This calculation has no tier-three cap. A higher-tier addon must provide valid native Ars armor/perk data for its extra tiers; Ars Fusion does not add tiers or change native thread-slot limits. Re-equip armor after changing these values at runtime.

Both features are **disabled by default**. Pack authors can set the settings below in the loader's active server config and `defaultconfigs/ars_fusion-server.toml` for new-world defaults:

```toml
[armor_fusion]
enabled = true

[ars_armor_toughness]
enabled = true
per_tier = 1.0
```

## Dependencies and migration

- Ars Nouveau is required. Ars Conflux is not required.
- Iron's Spells 'n Spellbooks is optional; fusion needs it, tier toughness does not.
- Iron & Nouveau is optional. When installed alongside Ars Fusion, use **0.17.32 or newer**; older versions can apply their own fusion modifiers. This minimum is enforced by loader metadata.
- Old `irons_nouveau:armor_fusion` components / NBT remain readable. NeoForge registers the historical codec only when its former owner is absent. New fusions use `ars_fusion:armor_fusion`.
- Iron & Nouveau 0.17.32 retains old-item behavior on its own and hands off to Ars Fusion when installed. Its historical creation/toughness settings no longer enable the migrated features.

Ars Fusion ships no copied Iron or Ars art assets and no original mod JARs. Its only visual change is the combined armor name; armor keeps Ars rendering.

## Development

See [building and verification](docs/development.md). Shared policy and optional Iron adapter live in `common/`; components/NBT, events and configuration live in the platform source trees. Future weapon fusion is outside the first release.
