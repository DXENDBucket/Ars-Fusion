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


# Verification: 0.1.3 bow fusion (2026-10-30)

- NeoForge 1.21.1 / 21.1.250, Ars 5.13.1, Iron & Nouveau 0.17.32, Conflux 0.1.40, native Cataclysm 3.33 and Lionfish API 3.1: **8 required GameTests passed** (5 bow/API cases and 3 existing sword regressions).
- Forge 1.20.1 / 47.4.0, Ars 4.12.7 without Iron, Conflux or Cataclysm: **8 required tests passed**. Six are substantive bow/sword cases; the two optional Conflux/Cataclysm fixture cases skip when their mods are absent. This verifies standalone classloading, native Forge behavior and optional adapter isolation.
- Native anvil, persistent/network marker and spell codecs, actual native scribe-table rewriting/rejection, real bow release, arrow ammo acceptance, Amplify/Split/Pierce placement and effect on projectile count/piercing, only original ammo consumption/wear, no survival ammo duplication, empty-mana physical fallback, partial-budget centered Split, creative mana bypass, projectile save/load, actual collision/damage continuation, canceled/no-damage hits, later valid same-victim hits, and no secondary inheritance are covered.
- The real Cursed Bow fixture retains native multi-shot Phantom arrow entity classes, native physical damage and wear. An actual native Phantom arrow collision successfully appended the spell. No native shooting code or Cataclysm resources are copied.
- The optional Conflux fixture queues a real native bow release, verifies ammo remains unspent during preparation, acknowledges exactly one successful active release/cooldown, and confirms a cooldown rejection still shoots an ordinary physical arrow. Source context resolution forwards the pre-impact incoming direction through public ResolverContexts.
- A final packaging gate caught an incremental compiler run which omitted all AP mapping outputs after an import-only change. Forge main compilation now disables incremental compilation, while retaining the three named cache outputs.
- After the compile setting fix, a non-Mixin-only source change regenerated mappings successfully. The build directory was then preserved under forge/.gradle/cache-verification-20261030/bow-build-before; a fresh directory restored compileJava FROM-CACHE, all three mapping outputs, and passed verifyRelease.
- Forge release verification checks the production SRG mappings for releaseUsing and the delegated item call, both ammo predicate calls/getProjectile, Pierce setter, projectile save/load, existing sword/scribe hooks, manifest, refmap and MIT license. Both release and source JARs exclude native dependencies and GameTests.
- Forge Ars 4 resets its mana cap when newly spawned entities start tracking. The high-budget arrow fixture therefore configures the real MAX_MANA attribute as well as the current capability; the separate low-budget case deliberately retains a finite budget. This is a test-fixture adjustment, not a change to player mana behavior.
- Full graphical client testing was not run. Native custom projectile tracking/model/particle appearance and GUI/HUD presentation remain for manual observation. Custom projectiles that bypass standard impact/damage events or do not serialize their state are outside the generic guarantee; asynchronous/secondary spawns are intentionally excluded.

Core baseline: 6af5a278122da13766c3d0a1dfcc30d2a94a63e0 (0.1.40); its tracked files remain unchanged. Deployment and artifact hashes are recorded under MinecraftDev/releases/ars-fusion-20261030/0.1.3-bow/manifest.json.


# Verification: 0.1.4 crossbow and shield fusion (2026-10-30)

- Final NeoForge equipment run: **10 required GameTests passed** with native Ars 5.13.1, Conflux 0.1.40, Iron 3.16.3, Cataclysm 3.33, Goety 3.2.2, Lionfish API 3.1 and Patchouli. The native mod fixtures are registry-only tests and are not production dependencies. The pack's Ars 5.13.3 was separately inspected: shield-event item recognition and crossbow scribing/public repairing methods retain the required signatures.
- Forge standalone run: **14 required tests passed** without Iron/Core/Cataclysm. These include five substantive new equipment cases and six existing bow/sword cases; the three optional Core/Cataclysm cases skip. The later default automatic eligibility/config addition was exercised in the NeoForge native-fixture run; the final Forge main/test sources compile and the release passes reobfuscation/refmap checks. Native modded equipment was runtime-tested on NeoForge, not with Forge versions of those native mods.
- Crossbow coverage: native anvil consumption and preservation of receiving stats/enchantments/wear, native and loaded-ammo save/network data, real scribe-table rewriting/invalid-form rejection, native loading with no premature magic payment, main/offhand firing, all three Ars augment-arrow types, inventory-ammo swap after loading, per-projectile magic budget alongside native multi-shot, actual native rockets, and physical firing with empty mana. Native Core preparation retains loaded ammo until one acknowledged firing use; preloading cannot bypass magic cooldowns.
- Shield coverage: actual native player blocking and wear, exact native Ars buff durations/amplifiers, no buff from merely holding the shield, absence of an invented caster, and matching native repair interval/rate/mana cost including empty-mana failure. The headless ServerPlayer fixture advances native spawn-immunity and item-use ticks; no gameplay immunity code is changed.
- Real native mod paths: Iron Autoloader Crossbow loads from inventory ticks and automatically reloads after firing; Goety Revolver Crossbow retains multiple uses per load and its magazine state through persistence/network round trips between shots. Both attach independent spell payloads without dedicated production adapters.
- Cataclysm Bulwark of the Flame, Black Steel Targe and Azure Sea Shield are all eligible. The Bulwark's Item inheritance (not ShieldItem) is covered by actual blocking with Ars buffs, its native sneaking-release dash vector and its original item cooldown. Turning automatic eligibility off requires target tags for otherwise-untagged items. No native Cataclysm implementation or art is copied.
- Shared bow/sword regressions passed in the combined runs. Forge final production checks now include item-use/Item.use, the native Ars shield subscriber's mapped ItemStack.getItem target, and inventoryTick in addition to all previous bow/sword/save/load selectors. Forge incremental compilation remains disabled and named AP cache outputs remain intact.
- Both release and source JARs contain only this project's production files/license. Graphical client rendering/HUD appearance and visual animations were not manually verified; original receiving-item models/animations are retained. Custom asynchronous/direct NPC firing paths or nonstandard event-free collisions/blocking remain outside the generic guarantee.

Core baseline remains 6af5a278122da13766c3d0a1dfcc30d2a94a63e0; Core tracked files are unchanged. Final hashes, package copies and logs are recorded in MinecraftDev/releases/ars-fusion-20261030/0.1.4-equipment/manifest.json.


# Verification: 0.1.5 casting-equipment attributes (2026-10-30)

- NeoForge 1.21.1, Ars 5.13.1, Iron 3.16.3, native Goety 3.2.2 and Conflux 0.1.40: **5 required caster GameTests passed**.
- Forge 1.20.1, Ars 4.12.7 without Iron, Goety or Conflux: **5 required cases passed**, including three substantive generic cases and two optional-mod cases that skip. This covers absence classloading and the Forge NBT/modifier implementation. Native Goety runtime behavior was exercised on NeoForge, not Forge.
- Native anvil consumes both items and one XP level. Tests cover one-fusion policy, source snapshots, no transplanted enchantments, creation toggles, persistent/network round trips, all native Ars spellbooks as recipients (not donors), ten-slot data and selected last slot, per-hand amounts/operations, default/custom/empty blacklists and actual equipment removal.
- Native mirror scribing matches its unfused counterpart. Ars 5 headless casting uses the same native caster server dispatch as PacketCastSpell; Ars 4 invokes the native item use. Baseline/fused healing and Mana fees match.
- Native Iron Graybeard Staff contributes its actual magic modifiers; every registered native Iron spellbook is rejected. Native Goety WandUtil sees fused Frost Staff identity, and actual IceSpikeSpell produces IceSpear. Main/offhand priority and crafting-toggle persistence are checked. Focus contents are deliberately absent from the exposed native staff view.
- Both binary/source JARs passed MIT/no-native-assets/no-native-JAR/test-class audits. Forge verifyRelease passed after reobfuscation, with all existing production mappings intact. Test-only API differences required fixture corrections, not gameplay changes.
- Deployed NeoForge 0.1.5 to 万法归一; previous 0.1.4 JAR and both config files are backed up. Caster fusion, automatic donors and Goety traits are enabled there; the pack's existing tier toughness remains enabled. No graphical client was started for this change.

Core tracked files remain unchanged at 6af5a278122da13766c3d0a1dfcc30d2a94a63e0. Logs and exact hashes: MinecraftDev/releases/ars-fusion-20261030/0.1.5-caster/manifest.json.


# Verification: 0.1.6 native sword spell damage (2026-10-30)

- NeoForge 1.21.1 / Ars 5.13.1 without Iron, Goety or Conflux: **4 required sword GameTests passed**. Actual equipped source/fused spell attributes and native spell damage match; the recipient's existing magical bonus and physical modifiers remain intact. Main-hand-only behavior, old markers, persistence/network data, repeated modifier queries and unequipping are covered.
- Existing sword anvil, scribing, resource and original-tool-wear cases pass. Casting tests now advance real equipment ticks before native/fused hit comparisons.
- Both platforms compile and package. Forge main/test compilation, reobfuscation and verifyRelease passed; a Forge runtime test was not repeated for this small attribute change. Native Ars 4 bytecode has no sword spell-damage modifier, so the adapter adds none and retains existing implicit Amplify behavior.
- Native SpellBow, SpellCrossbow, EnchantersShield, Wand and EnchantersMirror class implementations in the checked Ars JARs do not add comparable permanent spell-damage attributes. Their discount/conditional-utility mechanisms are separate.
- Final binary/source JARs passed MIT/no-native-assets/no-native-JAR/test-class audits. No Mixin selectors or AP output wiring changed. The NeoForge release was rebuilt after config-comment updates without repeating gameplay checks.
- No graphical client was started. Release files and logs are staged in MinecraftDev/releases/ars-fusion-20261030/0.1.6-sword-bonus/manifest.json. Core tracked files remain unchanged at ca21d6e31f44928841cdb5594fc594ea85497371.


## 0.1.7 reverse armor fusion

NeoForge main + compileGameTestJava + jar + sourcesJar passed. Forge main + compileForgeTestJava + jar/reobfJar + sourcesJar + verifyRelease passed. Two new native-path regression cases per platform cover the anvil, native upgrade/storage codecs, isolation from native armor base stats and the alteration tile; these cases were **compiled only**, not executed. No client/server/GameTest process was launched for this batch. Renderer bytecode was checked to use PerkUtil and standard ArmorItem rendering; actual client visuals remain unverified. Full behavior and limits: [reverse armor](reverse-armor-fusion.md). Build fingerprints: [0.1.7](reverse-armor-0.1.7-build.json).
