# Verification: 0.1.1 (2026-10-30)

- NeoForge 1.21.1 / 21.1.250, Ars 5.13.1, Iron 1.21.1-3.16.3, Iron & Nouveau 0.17.32 and Conflux 0.1.40: **8 required GameTests passed**.
- Forge 1.20.1 / 47.4.0, Ars 4.12.7, with Iron, Iron & Nouveau and Conflux absent: **3 required GameTests passed**. This exercises optional-mod classloading and the Forge modifier/NBT implementation.
- Both production JARs and sources JARs built successfully. Forge `verifyRelease` checked the packaged production getHoverName refmap target, MixinConfigs manifest and MIT license after reobfuscation.

Coverage: native anvil consumption/policies, native Iron arcane-anvil infusion, native Ars upgrades and network/persistent serialization, all four armor slots, historical fusion data, one set of modifiers/names with both mods installed, all twelve native Ars armor pieces at their own upgrade tiers 1/2/3, a valid future-tier fixture through tier 6, automatic modifiers and wrong-slot filtering, unchanged input stacks, vanilla netherite, configurable/empty blacklists, policy toggles and conditional manual-adapter bypass. The current Forge absence run covers the armor-tier and automatic/blacklist cases; manual native menus were also verified on Forge during the 0.1.0 extraction baseline.

Full graphical client testing was not repeated. Armor appearance remains native Ars; no custom armor renderer or copied donor art is added. The 万法归一 deployment uses the NeoForge JAR and explicitly enables tier toughness in addition to the default-on fusion/automatic compatibility.

Production artifacts contain only this project's classes/resources and its license; native mod JARs are not bundled. Iron and Conflux are present only in the NeoForge coexistence fixture, not declared hard requirements of Ars Fusion.
