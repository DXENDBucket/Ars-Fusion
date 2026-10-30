# Development and verification

## Layout

- `common/src/main/java/dev/arsfusion/armor/ArmorFusion.java`: fusion policy, item identity, donor validation, naming and tier toughness.
- `common/src/main/java/dev/arsfusion/compat/IronArmor.java`: the only production class linking Iron APIs. Native preset initialization and spell-container copying stay here.
- `src/main/java/.../platform/FusionPlatform.java`: NeoForge immutable components, historical codec ownership, anvil and attribute events.
- `forge/src/main/java/.../platform/FusionPlatform.java`: equivalent Forge NBT and event implementation.
- `common/src/gameTest` and platform test trees: real anvil menus, Iron infusion, Ars upgrades, save/network codecs, donor attributes and historical items.

There is no copied Conflux build or hard dependency on another Nouveau addon. Optional compatibility is tested independently from the glyph bridge.

## Local prerequisites

Java 21 for NeoForge; Java 17 for Forge. Local dependencies in ignored `libs/` and `forge/libs/` are runtime/compile inputs only, never bundled.

NeoForge dependencies: Ars 5.13.1, Iron 1.21.1-3.16.3 and their native libraries. Forge: Ars 4.12.7, Iron 1.20.1-3.16.3 and their native libraries. Loader targets: NeoForge 21.1.250 and Forge 47.4.0.

## Commands

```powershell
# NeoForge, Java 21
./gradlew.bat :runGameTestServer :jar :sourcesJar --offline --console=plain --no-daemon
./gradlew.bat :runGameTestServer -PwithoutIron=true --offline --console=plain --no-daemon

# Forge, Java 17, from forge/
./gradlew.bat :runGameTestServer :jar :sourcesJar --offline --console=plain --no-daemon
./gradlew.bat :runGameTestServer -PwithoutIron=true --offline --console=plain --no-daemon
```

The first namespace (`ars_fusion`) tests standalone configuration and live tiers 1–6. Its fixture temporarily extends native Ars perk-slot registration, modeling a valid higher-tier addon, then restores it. Native Ars armor provides three tiers; merely writing tier six without matching native slots is invalid native data.

The Iron namespace (`ars_fusion_iron`) checks native anvil consumption, donor attribute filtering, spell infusion, native Ars upgrade retention, persistent/network serialization, and historical fusion data. It is not enabled when Iron is absent.

For coexistence, put newly built Iron & Nouveau and Conflux NeoForge release JARs in ignored `libs/coexist/`. Forge uses ignored `forge/libs/irons-nouveau-coexist.jar` and `ars-conflux-coexist.jar` with `-Pcoexist=true` so ForgeGradle can deobfuscate them. Coexistence tests must assert exactly one set of added modifiers and one combined name.

The Forge final JAR must contain `ars_fusion.refmap.json`, including the mapped `ItemStack.getHoverName` target, and `MixinConfigs` manifest registration. `jar` finalizes `reobfJar`. Never use the intermediate dev artifact in a real pack.

## Migration contract

New data uses `ars_fusion:armor_fusion`. Both platforms also read `irons_nouveau:armor_fusion`. On NeoForge the old data component codec is registered here only when Iron & Nouveau is absent; otherwise its original owner registers it. Values are immutable `ItemContainerContents`, not mutable `ItemStack` components.

Iron & Nouveau 0.17.32 disables migrated crafting/toughness and suppresses legacy attribute/name handlers when Ars Fusion is installed. It still reads pre-existing fused armor on its own. Ars Fusion's optional version constraint prevents older bridge versions from double-applying modifiers.

Toughness is based on `PerkUtil.getPerkHolder(stack).getTier() + 1`, multiplied by a synchronized server setting; no cap at three or lookup table is used here.

## Armor tier is not armor style or an individual thread-slot level

Sorcerer, Arcanist and Battlemage are parallel sets, each starting at armor tier 1 and supporting two upgrades. Native Ars stores that armor upgrade tier in its perk-holder data; it simultaneously controls mana regeneration and native thread-slot progression. Reading `getTier()+1` therefore reads the actual armor upgrade tier. Do not map the three sets to three different tiers, or infer armor tier from the highest inserted thread/slot.

Ars Fusion does not change thread slots. The temporary six-tier slot/provider extension exists only in the test fixture so native Ars can safely process simulated future armor upgrades above its current three tiers. Tests cover all twelve native armor pieces at all three normal upgrade tiers, as well as that future-tier fixture.
