# Verification: 0.1.1 (2026-10-30)

- NeoForge 1.21.1 / 21.1.250, Ars 5.13.1, Iron 1.21.1-3.16.3, Iron & Nouveau 0.17.32 and Conflux 0.1.40: **8 required GameTests passed**.
- Forge 1.20.1 / 47.4.0, Ars 4.12.7, with Iron, Iron & Nouveau and Conflux absent: **3 required GameTests passed**. This exercises optional-mod classloading and the Forge modifier/NBT implementation.
- Both production JARs and sources JARs built successfully. Forge `verifyRelease` checked the packaged production getHoverName refmap target, MixinConfigs manifest and MIT license after reobfuscation.

Coverage: native anvil consumption/policies, native Iron arcane-anvil infusion, native Ars upgrades and network/persistent serialization, all four armor slots, historical fusion data, one set of modifiers/names with both mods installed, all twelve native Ars armor pieces at their own upgrade tiers 1/2/3, a valid future-tier fixture through tier 6, automatic modifiers and wrong-slot filtering, unchanged input stacks, vanilla netherite, configurable/empty blacklists, policy toggles and conditional manual-adapter bypass. The current Forge absence run covers the armor-tier and automatic/blacklist cases; manual native menus were also verified on Forge during the 0.1.0 extraction baseline.

Full graphical client testing was not repeated. Armor appearance remains native Ars; no custom armor renderer or copied donor art is added. The 万法归一 deployment uses the NeoForge JAR and explicitly enables tier toughness in addition to the default-on fusion/automatic compatibility.

Production artifacts contain only this project's classes/resources and its license; native mod JARs are not bundled. Iron and Conflux are present only in the NeoForge coexistence fixture, not declared hard requirements of Ars Fusion.


# Verification: 0.1.2 sword fusion (2026-10-30)

- NeoForge 1.21.1 with Iron & Nouveau 0.17.32 and Conflux 0.1.40: **3 targeted sword GameTests passed**.
- Forge 1.20.1 without Iron or Conflux: **3 targeted sword GameTests passed**. Actual native scribing is compared to an unfused native sword, preserving Ars 4's additional implicit Amplify.
- Checks cover tag-defined mining tools and a non-SwordItem addition, blacklist precedence, maximum stack size, existing caster collisions, actual anvil consumption, original attributes/enchantments/durability, persistent/network data, custom/default names, native scribe-block rewriting and invalid-form rejection, actual native magic damage and mana use, empty-mana failure, and exactly the original physical tool wear.
- Production Forge refmap checks include the enclosing item-hit, sword-hit and scribe-use methods and all Minecraft invocation selectors. Named annotation-processor outputs were added. The generated build directory was preserved under ignored .gradle cache-verification storage, then a fresh build restored compileJava FROM-CACHE and passed verifyRelease with the refmap present.
- The installed Ars Nouveau 5.13.3 JAR was additionally checked: native sword dispatch/physical fallback and component-based caster fallback match the adapter's targets. Development integration tests use the documented 5.13.1 dependency.
- GUI rendering, mana HUD appearance and native tooltip image have not been manually checked in a graphical client. No donor art or model is copied; item identity/rendering remains the receiver's.
