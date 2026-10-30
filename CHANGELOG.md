# Changelog

## 0.1.0

- Extract optional same-slot anvil armor fusion from Iron & Nouveau. Preserve Ars identity, appearance, threads, upgrades and enchantments; transfer donor attributes except armor/toughness and supported native Iron spell data.
- Add independent, opt-in toughness per armor upgrade tier. All three Ars armor sets each progress through tiers 1–3; the calculation supports valid future tiers without a cap at three. Thread slots are not modified.
- Read historical Iron & Nouveau fusion data. Cooperate with Iron & Nouveau 0.17.32+ to prevent duplicate modifiers/names.
- Support NeoForge 1.21.1 and Forge 1.20.1 from one shared-source repository. Require Ars only; Iron and Iron & Nouveau remain optional.
- Default both gameplay switches off. The 万法归一 test pack enables both.
