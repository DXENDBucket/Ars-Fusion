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

- Sword-style fusion is a separate casting layer; retain only the native spell-damage modifier, never physical weapon modifiers, and do not install caster/scribe interfaces on every Item. Use per-stack markers and the scoped scribe recognition redirect.
- Keep native Enchanter's Sword as the executor so optional Conflux hooks are naturally honored. NativeSwordCasting's thread-local scope suppresses only the delegated SwordItem physical fallback; the original receiving item owns its normal hit/wear behavior. Always clear scope in finally.
- Default targets are swords/axes/pickaxes/shovels/hoes via tags, max stack size one, blacklist takes precedence, and existing Ars casting is never overwritten. No right-click casting, bow/crossbow/shield behavior, donor discount or numeric weapon properties are implied.

- Bow fusion observes the actual receiving item's release and native projectiles; never replace special arrows with EntitySpellArrow or copy a donor's shooting implementation. Keep launch scopes in try/finally and only attach to accepted, shooter-owned synchronous launches.
- Use native SpellArrow.modifySpell for Ars augment ammo. Split clones the native registered arrow through native serialization; do not duplicate survival ammo. Standard Pierce must not override a custom entity's own lifecycle. Delayed/secondary spawns do not inherit payloads automatically.
- Entity-hit continuations require successful direct projectile damage to the selected target; canceled impacts/no-damage hits do not trigger. Capture incoming direction before native onHit mutates movement. Use optional public Conflux APIs for preparation/cooldown/context, never edit Core to implement an equipment integration.

- Crossbow loading remains native. Capture actual item-use firing with the loaded ammunition snapshot, not inventory ammo; magic preparation/payment belongs to firing and honors the real hand. Do not claim generic NPC/direct performShooting or asynchronous-spawn support.
- Shield fusion is native utility, not spell storage. Use the scoped native shield-event recognition and native RepairingPerk call after the original item tick. Preserve original blocking/rendering and keep shield markers out of caster registry/scribing recognition. FusedCasting centralizes recognition for all equipment paths.

- Default automatic crossbow eligibility uses CrossbowItem inheritance; shield eligibility uses the actual block action, not ShieldItem inheritance or upstream tags. Their automatic_compatibility switches can require explicit target tags instead; blacklists always win. Preserve native autoloader/revolver magazine and sneaking shield abilities by retaining original use/release/inventory paths.

- Caster attribute fusion augments an existing Ars caster without replacing its native caster data. Ars spellbooks are valid recipients; Iron spellbooks are not recipients or donors, and no spellbooks are staff materials. Keep slot-specific attribute transfer in CasterPlatform, using its independent blacklist and stable namespaced modifier IDs.
- Optional Goety staff integration exposes intrinsic donor item identity through native WandUtil only. Never transplant focus contents/enchantments or replace the receiver's native use loop. Preserve main-hand-first priority and guard optional classes. IronCaster handles staff recognition/book exclusion only; no second Curios spellbook panel is transferred.

- SwordPlatform retains native Enchanter sword SPELL_DAMAGE_BONUS modifiers under stable owned IDs in the native main-hand slot. Read native values/operations; do not hardcode +4 into Forge Ars 4, whose implicit Amplify is already preserved. Existing sword markers gain this bonus without new stored data.

- Reverse armor fusion keeps non-Ars armor on the left and transplants only the native Ars thread layer from a same-slot right item. Use ReverseArmorFusion and platform ReverseArmorPlatform. Scope perk-provider adaptation to marked stacks; never register all items as native Ars armor. Preserve native thread tier/state, alteration and upgrades, without native armor base Mana/regen/toughness. Only player worn-armor ticks are delegated; no duplicate base armor tick, item identity swap or donor modifier copying. Both fusion directions reject already-fused donors/receivers. reverse_enabled defaults true under the master armor switch; existing results do not depend on crafting toggles.
