# Ars Fusion

Read README.md and docs/development.md before editing. The shared local workflow is ../Ars-Conflux/docs/development-workflow.md; this project does not depend on or modify Conflux.

- Keep shared armor policy in common/. Only loader/config/component/NBT differences belong in platform directories.
- Iron is optional. Never expose Iron classes in entry points, shared method signatures, or platform registration. Keep native calls in compat/IronArmor and guard entry by mod presence.
- New data belongs to ars_fusion. Historical irons_nouveau:armor_fusion data remains readable with or without Iron & Nouveau. Do not register the historical NeoForge component twice.
- Donor modifier transfer uses the configurable attribute blacklist (default armor/toughness). Do not copy enchantments or item identity. Preserve Ars appearance, threads and upgrades.
- Toughness uses the armor upgrade tier stored in Ars perk data (all three native sets independently have tiers 1/2/3), with no hard-coded maximum of three. Higher-tier tests must also supply enough valid native perk slots.
- Fusion and automatic compatibility default on; extra tier toughness and forced disabling of manual adapters default off. Shared test-pack deployment is coordinated, not an automatic side effect of building.
- Batch relevant checks. Build Forge production refmap and reobfuscated JAR; do not deploy development JARs. No remote publishing or pushes without user authorization.

- Explicit adapters use api/ArmorFusionAdapter and api/ArmorDonors. They take priority over generic attribute transfer. Force-auto only bypasses them during new fusion and only with master+auto enabled. Do not strip stored native casting data when a crafting policy changes.
