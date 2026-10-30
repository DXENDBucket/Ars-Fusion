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

## Donor adapters and automatic fallback (0.1.1)

`api/ArmorFusionAdapter` provides `supports`, `prepareDonor` and `transferData`. Register it through `ArmorDonors.register` on both sides during mod setup; later, more-specific registrations take priority. Shared code still performs attribute copying and filtering. Built-in Iron support registers only if Iron is present.

`ArmorFusion.canFuse` checks recipient identity, standard ArmorItem donor, same slot, and one fusion only. A manual adapter can make a supported donor available with automatic compatibility disabled. With master+auto enabled, `disable_manual_compatibility` bypasses manual preparation/data transfer; modifiers still follow the same engine. Reading existing fused snapshots does not depend on current crafting toggles.

Both platforms consult `FusionConfig.allowsAttribute` when copying donor modifiers. The ID blacklist is synchronized server policy, cached as a normalized set and permits unknown well-formed IDs. Defaults exclude `minecraft:generic.armor` and `minecraft:generic.armor_toughness`; an empty list permits defense modifiers too. Source Ars modifiers and optional native-tier toughness are independent of the donor blacklist.

New tests cover an unregistered vanilla armor item with custom modifiers and a wrong-slot modifier, unchanged inputs, save/network round trips, policy toggles, vanilla netherite, a custom blacklist and an empty blacklist. Iron tests run with automatic compatibility off, and separately verify forced automatic mode skips native spell containers only under the requested master+auto combination.

### Attribute ownership

Manual compatibility does not own a second attribute path. IronArmor only recognizes its supported donor, initializes a native spell container on the donor snapshot, and copies that container to the Ars result. Both dedicated and automatic donors then use the same platform attribute event handler and shared blacklist policy. Automatic compatibility controls eligibility of unregistered donors, not whether registered donors receive attributes. Force-auto skips manual preparation/data transfer while keeping that common attribute path.

## Sword fusion (0.1.2)

Shared policy is in sword/SwordFusion; platforms own native caster serialization and the marker (immutable boolean component on NeoForge, owned NBT key on Forge). Spell data uses native Ars storage. Anvil output remains a copy of the receiving item; only the sword marker and current spell metadata are added.

FusedSwordHitMixin injects at ItemStack's native item hit callback, before receiving-item wear. NativeSwordCasting invokes the actual registered EnchantersSword.hurtEnemy with the actual tool/attacker/target; optional Conflux's existing native-sword hook is thereby reused without a hard dependency. NativeSwordPhysicalMixin suppresses the delegated call's final SwordItem fallback only within the thread-local scope. The original ItemStack callback then proceeds normally. This retains last-durability hit timing and leaves native unfused swords unchanged.

Scribe recognition is deliberately scoped to ItemStack.getItem calls within the native ScribesBlock interaction. Only fused stacks appear as the native sword to those recognition checks; the stack passed to native onScribe and its item identity remain original. No broad Item interface mixin is introduced. Original table events, multipart dispatch, validation, implicit Touch insertion and synchronization are retained.

Client-only mana HUD opt-in is listed separately in the Mixin configuration. Tooltips delegate native spell information; Ars 5 also supplies the native glyph tooltip image and stack-specific hasCaster recognition. Existing right-click interactions and physical attributes stay with the original item.

Targeted GameTests run under ars_fusion_sword and exercise actual anvil consumption, normal and overridden tags, max-stack constraints, caster collision/repeat rejection, attributes/enchantments/durability, persistent/network round trips, names, real scribe-block rewriting/rejection, native casting versus an unfused sword, mana failure and original-tool wear. Test resources intentionally include full tag definitions because same-path resources inside one mod's main/test classpath override rather than merge.

Ars 4's native Enchanter's Sword scribing additionally appends Amplify, whereas the checked Ars 5 version only supplies Touch. This integration preserves each native rule. The scribing test compares the bound tool against an actual unfused native sword rather than imposing one platform's recipe length on the other.
