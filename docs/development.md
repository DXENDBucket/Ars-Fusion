# Development and verification

## Layout

- `common/src/main/java/dev/arsfusion/armor/ArmorFusion.java`: fusion policy, item identity, donor validation, naming and tier toughness.
- `common/src/main/java/dev/arsfusion/compat/IronArmor.java`: the armor-specific class linking Iron APIs. Native preset initialization and spell-container copying stay here.
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

## Bow fusion (0.1.3)

`bow/BowFusion` owns tag-based anvil policy. `BowCasting` scopes the receiving item's real `releaseUsing` with try/finally, snapshots its caster/ammunition, and observes only entities synchronously spawned during that release. After the native release finishes, attachment requires the projectile to be present in the server entity index and owned by the actual shooter. A canceled/no-shot release costs no mana. The original bow retains ammo, spread, damage, enchantments, physical wear and its native projectile type.

`SpellCarryingProjectile` stores a per-instance payload. Entity-level save/load return hooks preserve that payload even when custom Projectile subclasses omit super calls in their own additional-data methods. No client renderer/entity type is added. Payloads use copied native caster data plus original caster UUID; already-prepared contexts are cloned per hit. Reloads reconstruct from the snapshot, and unavailable/dead/cross-dimension casters cannot trigger a continuation.

Native `SpellArrow.modifySpell` handles the augment placement differences between effect Amplify and form Split/Pierce. Only fused bows extend player ammo predicates. Standard arrow Pierce is additive, using the mapped private-method invoker. Split serializes the original registered arrow type, strips UUID/payload, preserves native fields through the native save/load path, and makes extra arrows non-pickup in survival. Every accepted extra must pay; canceled extras cost nothing, unfunded extras are discarded, and the actual funded fan is recentered. Do not substitute a new Ars entity or copy another mod's shooting implementation.

`BowImpacts` observes native ProjectileImpactEvent and successful damage events. It records the direction before the native collision can stop/deflect the arrow. Only damage whose direct source is the projectile and whose victim matches the selected entity can acknowledge that impact; unrelated explosion victims do not duplicate spells. End-of-tick dispatch checks the final impact cancellation state; Forge also checks ImpactResult and final LivingDamageEvent cancellation. Block impacts need no damage acknowledgement. Pending records are cleared on server shutdown; this uses no world/entity scan. Later valid impacts can trigger again without a lifetime target set.

`compat/ConfluxBowCasting` is isolated behind mod presence and uses only public `ActiveCasts` and `ResolverContexts` APIs. It queues the original shot with its existing preparation mechanism, acknowledges accepted spell payloads once per active use for provider cooldowns, and forwards actual incoming direction. No Core source change or hard dependency is introduced. Compile-only Core JARs are external ignored inputs; optional runtime fixtures stay in ignored directories. The native bow scribing recognition, tooltips, naming and mana HUD opt-in share the existing stack-specific sword extension points.

The native Cataclysm Cursed Bow fixture is enabled only with `-PwithCataclysm=true` on NeoForge. It references the existing native test JAR and Lionfish API in the sibling project's ignored libs; neither is packaged or made a production dependency. Tests exercise its real release, native multi-shot Phantom arrow classes, damage/wear and actual impact continuation. Native arrows' tracking/animation visuals still need a client observation.

### Forge annotation-processor outputs

The bow release check exposed an additional incremental-build case: changing only a non-Mixin class (even an unused import) may leave the Mixin processor uninvoked after Gradle cleans its previous mapping outputs. Forge main compileJava therefore disables incremental compilation. Keep all three named AP outputs for build-cache restoration as well. verifyRelease rejects missing mappings, and a preserved-build-directory cache recovery is checked after this wiring changes; do not work around it by shipping a JAR that lacks the refmap.


Targeted bow + sword regression commands (PowerShell requires quotes around the comma-separated property):

```powershell
# NeoForge; Core/Iron coexistence fixture plus the optional native Cursed Bow fixture
./gradlew.bat runGameTestServer jar sourcesJar '-PgameTestNamespaces=ars_fusion_bow,ars_fusion_sword' -PwithCataclysm=true --offline --console=plain --no-daemon
# Forge, Java 17, in forge/; optional fixture cases skip without their mods
./gradlew.bat runGameTestServer jar sourcesJar verifyRelease -PwithoutIron=true '-PgameTestNamespaces=ars_fusion_bow,ars_fusion_sword' --offline --console=plain --no-daemon
```


## Crossbow and shield fusion (0.1.4)

`casting/FusedCasting` is the shared stack-specific recognition used by native scribing, caster registration previews, spell tooltips, names and the mana HUD. Shield binding belongs to `bound`, not `has`: shields have native utility but no caster. All creation paths reject another existing equipment binding. Components/NBT for the new markers and loaded-ammunition inspection live in EquipmentPlatform.

`crossbow/CrossbowCasting` wraps the real ItemStack use -> Item.use call only for loaded fused crossbows. Loading/release remains native, with no magic charge at loading time. At firing, it snapshots the native loaded ammunition and caster, prepares each loaded arrow's native augment through BowPlatform, and observes synchronous original spawns through the existing generic BowCasting.capture supplier scope. Return values, exceptions and nested scopes retain their original control flow; every scope is restored in finally. Canceled spawn events remain in the observation sequence so later mixed loaded ammo does not shift indices, but attachment/payment still requires accepted server entity membership and matching ownership. The native item owns ammunition clearing, physical enchantments, special projectile classes and wear. Native payload persistence also accepts crossbow markers. Split preserves each original arrow's pre-augmentation Pierce level even if the added value saturates.

Conflux integration uses the existing public active-use API with the actual firing hand. It queues the whole native use, acknowledges only real accepted magic payloads, and prevents preloading extra crossbows from bypassing the magic cooldown. A denial falls back to an ordinary physical shot, consistently with fused bows. Direct NPC performShooting calls or asynchronous custom launch paths are not implicitly intercepted; no global scanning or projectile-owner heuristic is installed.

`shield/ShieldFusion` adds a boolean utility marker only. FusedShieldBlockMixin redirects the item-identity read strictly inside Ars EventHandler.shieldEvent, so the existing Ars subscriber performs the exact native buff logic and other mods' shield-block event processing stays in place. It never changes the item returned to the physical blocking system. FusedShieldRepairMixin runs after the original receiving ItemStack inventory tick and invokes the public native RepairingPerk.attemptRepair, preserving original item ticks and Ars' own interval, cost, config and perk accounting. Neither native implementation nor native art is copied.

Targeted cases are in `ars_fusion_equipment` (add it to gameTestNamespaces). The dedicated real-shield-block fixture advances the headless ServerPlayer's spawn-immunity and item-use ticks through native tick/doTick methods, because it has no real packet listener tick; gameplay invulnerability rules are not modified.


### Native mod equipment coverage

Automatic crossbow compatibility defaults to CrossbowItem inheritance; automatic shield compatibility uses the native shield-block action rather than ShieldItem inheritance. Both have a creation-policy toggle to require the target tags instead, and blacklists always win. This matters for untagged Goety crossbows and Cataclysm's Bulwark of the Flame, which extends Item directly. Do not require another mod to supply our preferred common tags just to use the default automatic path.

The optional `-PwithEquipmentFixtures=true` NeoForge fixture loads native Goety 3.2.2 and Patchouli from ignored libs/equipment (copied locally from the actual pack/native dependencies, never distributed). Iron is already in the normal coexistence fixture; combine with `-PwithCataclysm=true` for the shield samples. Registry-only test references verify Iron's autoloader inventory reload before/after firing, Goety's multi-use revolver magazine and persistence between shots, eligibility of Cataclysm Bulwark/black-steel targe/azure sea shield, actual blocking on its non-ShieldItem Bulwark, and the original sneaking-release dash/cooldown. Production has no hard dependency or special-case implementation for any of these items.


## Casting-equipment attributes (0.1.5)

`caster/CasterFusion` adds one donor snapshot to an existing Ars caster; it does not replace the native caster or use the weapon-binding markers. Ars spellbooks are explicitly valid recipients. Iron spellbooks are explicitly rejected on both sides; other spellbooks are rejected as donors. Recipients retain every native slot, selected slot, name, color and editing/upgrade behavior because the actual receiving item and native casting storage remain untouched. Default recipients additionally include wand, mirror and the three active Enchanter weapons. Tags can extend eligible existing casters, but cannot bypass the book-material restriction or blacklists.

`CasterPlatform` owns immutable snapshot components + staff-trait boolean on NeoForge, and an owned donor/flag compound on Forge. Both normalize donor hand-slot modifiers to `CasterBonus`, filter through the separate caster blacklist, and install namespaced stable identities per hand/attribute/source modifier. Native attributes remain on the receiving item; source inventory/Curios/armor slots are never converted to held modifiers. Generic recognition is tag-based, with native Iron StaffItem and enabled Goety IWand adapters independent of the automatic fallback toggle. Guard optional class entry behind actual mod presence. No Curios spellbook panel import is implemented.

`compat/GoetyCaster` exposes only a default instance of the donor's native staff item for intrinsic type checks. `OptionalGoetyCasterMixin` hooks native WandUtil.findWand and findWandOnHand, preserving main-hand priority including actual native staffs. No focus, enchantments, native right-click loop or source art is copied. This works for both native Goety and Goety & Nouveau callers of that shared utility, with no dependency on the latter. New fusion settings cannot strip already-stored staff traits.

Targeted tests use `ars_fusion_caster`. They exercise native anvil consumption, persistence/network serialization, native ten-slot Ars book retention, hand-slot attributes and equipped-player removal, configurable blacklists, actual mirror scribing/casting against its native baseline, native Iron staffs and all registered Iron book exclusions, and native Goety Frost Staff -> Ice Spike -> Ice Spear dispatch without transplanted focus contents. Ars 5 mirror use sends a client packet, so its headless casting fixture invokes that packet's native caster server dispatch; Ars 4 uses the real item use path. Use `-PwithEquipmentFixtures=true` on NeoForge for native Goety; Forge's optional `-PwithGoety=true` enables its ignored deobfuscated fixture. Run without optional mods to cover absent-class loading. Native client appearance remains a separate manual check.


## Native sword spell-damage attribute (0.1.6)

Both SwordPlatform implementations register a narrowly filtered attribute event handler for existing sword-fusion markers. They read only the native Enchanter sword's SPELL_DAMAGE_BONUS modifiers for MAINHAND, retaining native amount/operation and assigning stable ars_fusion-owned identities. Recipient attributes and physical item modifiers stay intact; repeated event queries cannot accumulate copies. No new marker or per-item migration is required, so previously fused items benefit after re-equipping.

The checked Ars 5.13.1 sword has ADD_VALUE +4 in the main hand. Ars 4.12.7 has no such attribute; its scribing adds implicit Amplify, and the integration deliberately adds no synthetic +4 on top. Native SpellBow, SpellCrossbow, EnchantersShield, Wand and EnchantersMirror do not define comparable permanent spell-damage modifiers in the checked JARs; discounts and conditional buffs are separate mechanisms.

The sword namespace now also checks actual equipped attributes, preservation of an existing magical bonus, old boolean markers, save/network round trips, repeated queries, offhand exclusion and unequipping. Its casting comparison explicitly advances equipment ticks, so native and fused attacks use real held attributes rather than an unticked headless inventory swap.
