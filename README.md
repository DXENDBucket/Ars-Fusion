# Ars Fusion

Equipment fusion for Ars Nouveau, by EndXiom. MIT licensed.

Ars Fusion includes equipment casting fusion and the optional armor fusion moved out of Iron & Nouveau. Minecraft 1.21.1 / NeoForge and 1.20.1 / Forge share version **0.1.7**, common policy, compatibility code and integration checks.

## Staff attributes on Ars casting equipment

Normal anvil: **Ars spellbook, wand, mirror or other supported Ars caster on the left + staff on the right**, one XP level. Enabled by default through `caster_fusion.enabled`; each recipient accepts one staff. Ars spellbooks remain normal spellbooks, retaining all spell slots, editing, upgrades, casting and appearance. **Iron spellbooks are not fusion materials**: their Curios bonuses cannot be duplicated this way. Ars spellbooks are not staff materials either.

The donor adds its item attribute modifiers for the same native hand slot, preserving amounts and operations. The default caster blacklist excludes attack damage, attack speed, armor and armor toughness. It has its own configuration and does not change the armor-fusion blacklist. Donor enchantments, durability, focus contents, spell slots and item-specific use actions do not transfer. The left item retains its native mana discount, if any; the source staff's non-attribute discounts do not transfer automatically.

Native Iron staffs are recognized directly. Other materials use `ars_fusion:caster_fusion_materials`, which includes optional common/Forge staff/wand tags. Automatic compatibility defaults on and can be disabled independently. Default recipients include native Ars spellbooks, wand, mirror, Enchanter's Sword, Bow and Crossbow; datapacks can extend `ars_fusion:caster_fusion_targets` to other existing nonstackable Ars casters. Target and material blacklist tags always win.

Optional Goety integration retains the donor staff's intrinsic staff type/identity, enabling native staff-specific spell variants through Goety's existing lookup (for example, Frost Staff's Ice Spear variant). It works independently of Goety & Nouveau and preserves native main-hand-first priority. Focus contents and focus enchantments are not transplanted, and the result still uses Ars controls. Disabling `goety_staff_compatibility` affects new fusions; previously stored staff traits remain usable. With automatic compatibility on, tagged Goety staffs can still contribute attributes alone.

## Enchanter's Crossbow fusion

Normal anvil: **receiving crossbow on the left + Enchanter's Crossbow on the right**, one XP level. `crossbow_fusion.enabled` defaults to true. The result keeps the original item, appearance, loading, ammunition, charging enchantments, multi-shot, piercing, firing, rockets and durability behavior. Scribing uses the native Ars crossbow's rules and implicit Projectile form.

The receiving crossbow loads ammunition normally. Magic is paid **when it fires**, individually for each accepted projectile, using the ammunition already loaded in the crossbow. Swapping inventory ammo after loading cannot change that shot's Ars augment. Its own physical multi-shot remains intact if mana only funds some spell payloads. Native Ars augment arrows work through the same payload engine as bow fusion; rockets remain actual rockets. No ammo-conjuring or donor mana-discount property is copied.

With optional Conflux, preparation and cooldown checks apply to **firing**, including already-loaded crossbows, rather than adding a second preparation to loading. Both hands are supported. A queued firing use keeps the loaded ammunition until release; a magic cooldown rejection permits the ordinary physical shot without extra magic.

Automatic crossbow compatibility defaults to true and permits CrossbowItem receivers even when the source mod supplies no common tags. `ars_fusion:crossbow_fusion_blacklist` always takes precedence. Set `crossbow_fusion.automatic_compatibility = false` to require `ars_fusion:crossbow_fusion_targets` (vanilla/common-tagged crossbows by default). Generic observation covers the native item-use firing path and its synchronous native projectiles. NPC/custom code firing outside that path, asynchronous projectiles, custom collision APIs or state omitted from native serialization may need explicit adapters, as with bow fusion.

## Enchanter's Shield fusion

Normal anvil: **receiving shield on the left + Enchanter's Shield on the right**, one XP level. `shield_fusion.enabled` defaults to true. The result keeps the original shield's appearance, decorations, enchantments, durability, blocking and other native behavior.

The native Ars shield does **not** store a spell. Shield fusion transfers its utility instead: native Mana Regeneration II and Spell Damage II after blocking, plus the native mana-funded repair behavior. Ars owns the buff durations, repair interval/rate and mana fee, including its configuration and repairing-thread interactions. There is no added spell editor or caster container on the shield, and no donor physical properties are copied.

Automatic shield compatibility defaults to true and recognizes the loader's actual shield-block action, including items that do not inherit ShieldItem. `ars_fusion:shield_fusion_blacklist` always takes precedence. Set `shield_fusion.automatic_compatibility = false` to additionally require `ars_fusion:shield_fusion_targets` (vanilla/common-tagged shields by default). Custom blocking code must emit the native shield-block event to gain the native buffs. Custom names take precedence, and disabling new fusion preserves existing results.

## Enchanter's Bow fusion

Normal anvil: **receiving bow on the left + Enchanter's Bow on the right**, one XP level. `bow_fusion.enabled` defaults to true. The result remains the left item, preserving its own charging, ammunition, enchantments, durability and firing code. Rewrite it on a scribe's table as with an Enchanter's Bow: provide effects/augments, and the native bow supplies Projectile.

The bow's actual projectiles carry the spell. They are not replaced by Ars spell arrows, so native multi-shot, projectile classes, tracking and physical damage remain available. Each accepted projectile pays separately; insufficient mana leaves the original physical arrow intact, without added magic. Creative players do not spend mana. Fusion does not grant infinite arrows or the native Ars bow's ammo-conjuring behavior.

**Ars augment arrows are supported:** Amplify modifies the spell through the native arrow's `modifySpell`; Pierce adds standard arrow piercing; Split creates extra arrows of the original registered type. Split extras pay separately, cannot duplicate survival ammo pickups, and form a centered fan according to the number funded. Native projectile lifecycle overrides can still stop an arrow even if it has standard piercing.

Added spells trigger on uncanceled block impacts, or on living-entity impacts after the original projectile successfully deals damage to that exact target. Canceled/immune hits do not trigger extra spells. A piercing arrow can trigger again on later successful hits; there is no permanent per-victim blacklist. Later secondary projectiles do not inherit payloads merely because they share an owner.

When Conflux **0.1.40+** is installed, its active-use preparation/cooldown API handles the shot, with actual incoming projectile direction retained for continuations. A magic cooldown denial still allows an ordinary physical shot. Conflux remains optional.

Targets use `ars_fusion:bow_fusion_targets` and `ars_fusion:bow_fusion_blacklist`. Defaults allow vanilla bows and optional common/Forge bow tags, including both `c:tools/bow` and `c:tools/bows`. The receiving item must extend `ProjectileWeaponItem`, be nonstackable, and have no existing Ars casting. Crossbows are excluded. Custom bows whose projectiles bypass standard impact/damage events, omit their state from native saves, or spawn only asynchronously may need a dedicated adapter. This is automatic compatibility with standard launch/impact paths, not a guarantee for every custom weapon.

## Enchanter's Sword fusion

Use a normal anvil: **receiving weapon/tool on the left + Enchanter's Sword on the right**, consuming both inputs and one XP level. Enabled by default through `sword_fusion.enabled`.

The result stays the left item, retaining its appearance, damage, speed, enchantments, durability and ordinary interactions. It receives the sword's current spell and native scribe-table rewriting ability. Successful melee hits use the native sword casting path, with the real wielder and actual receiving tool. Existing Conflux hooks on Enchanter's Sword therefore still apply when installed; Conflux remains optional. Only the original receiving item's melee durability behavior runs.

Sword fusion also retains the native sword's **main-hand spell-damage attribute**: **+4 in Ars 5 / Minecraft 1.21.1**. It adds to the receiver's existing magical attributes without copying the sword's physical attack damage or speed. Existing fused items gain this bonus automatically; no recrafting is needed. Ars 4 / Minecraft 1.20.1 has no such native attribute and keeps its native implicit Amplify behavior instead.

Default names are `Enchanter's Diamond Sword` / `巫师之钻石剑` (and equivalent original names for other tools). Explicit anvil/custom names take precedence.

Eligibility is stack-specific:

- `ars_fusion:spell_fusion_targets` allows swords, axes, pickaxes, shovels and hoes by default. Datapacks may add mod weapons or other tools without requiring SwordItem inheritance or durability.
- `ars_fusion:spell_fusion_blacklist` takes precedence; defaults exclude bow, crossbow and shield. Ranged weapons and shields are reserved for their respective Ars equipment integrations.
- Maximum stack size and input count must both be one. Existing Ars casting data/providers and already-bound items are rejected.

Put the bound tool on a scribe's table and sneak-use the source spellbook/parchment, as with a native Enchanter's Sword. It remains a single sword-style binding: write effects/augments without a form, and the sword supplies Touch. It does not become a multi-slot spellbook or gain a right-click casting action. Mana HUD and native spell tooltip information recognize the binding. Disabling new fusion or changing eligibility tags does not strip existing bindings.

## Armor fusion

Configure `armor_fusion.enabled` (enabled by default) in `ars_fusion-server.toml` (NeoForge 1.21.1: `config/`; Forge 1.20.1: the world's `serverconfig/`).

Use a normal anvil: **Ars armor on the left + same-slot donor armor on the right**. Fusion consumes both inputs and one XP level. Each piece can be fused once.

The result remains the original Ars armor. Its appearance, dye, enchantments, durability, threads and Ars upgrades are preserved. The donor contributes item attribute modifiers except those listed in the configurable blacklist (armor and armor toughness by default). Explicit integrations can also transfer supported native casting data, such as Iron spell containers. The Iron arcane anvil and casting selection continue to work. Donor enchantments, durability and item-class-specific special passives do not transfer automatically. Renamed armor retains its custom name.

Turning fusion off prevents new fusions; existing equipment retains its data and bonuses.

## Reverse armor fusion

Normal anvil: **other armor on the left + same-slot Ars armor on the right**, one XP level. Enabled by default through `armor_fusion.reverse_enabled`, under the master `armor_fusion.enabled` switch. This direction does not depend on the forward donor attribute blacklist or automatic-compatibility setting.

The result stays the left armor, preserving its rendering, physical attributes, damage, enchantments, components and native abilities. The right item contributes its native thread slots, current upgrade tier, installed threads and their saved state. The normal Ars alteration table can remove/reinstall threads, and the native armor upgrade recipes upgrade the thread layer while keeping the original item. Slot layouts follow the donor's registered Ars provider, including additional tiers supplied by other mods.

Only thread attributes and behavior are added. Ars armor's own base Mana, base regeneration, passive repair without a repairing thread, piglin neutrality and optional base tier-toughness bonus are not copied. The native repairing-thread effect can repair fused armor using its normal mana cost; other thread effects continue through native Ars events and tick callbacks. Equipped player armor receives thread ticking, matching the player armor use case on both platforms.

Both inputs must be single items in the same armor slot. Receivers must be standard `ArmorItem` equipment without an existing Ars perk layer; already-fused armor cannot participate in either direction. Only the specific fused stack receives slots, not every item of that type. Disabling crafting preserves existing fusions. See [implementation and verification boundaries](docs/reverse-armor-fusion.md).

## Optional tier toughness

Independent of fusion, enable `ars_armor_toughness.enabled`. Extra toughness **per piece** is:

`actual displayed Ars armor tier × ars_armor_toughness.per_tier`

Sorcerer, Arcanist and Battlemage are three parallel armor sets. Each independently starts at tier 1 and can be upgraded twice to tiers 2 and 3. At `per_tier = 1.0`, an unupgraded / once-upgraded / twice-upgraded piece gives +1 / +2 / +3 toughness. This is the armor upgrade tier, not the tier of an individual thread or the choice of armor set. This calculation has no tier-three cap. A higher-tier addon must provide valid native Ars armor/perk data for its extra tiers; Ars Fusion does not add tiers or change native thread-slot limits. Re-equip armor after changing these values at runtime.

## Automatic armor compatibility

Enabled by default. Other standard Minecraft `ArmorItem` equipment can be donors without a dedicated integration, using its modifiers for the matching equipment slot. Amounts and modifier operations are preserved. The receiver must still be Ars armor; other Ars sets and already-fused items cannot be donors.

Manual integrations take priority. Iron armor retains its native spell-container handling even if automatic compatibility is disabled. Pure automatic transfer does not copy capabilities, special passives, spell containers or enchantments.

The default attribute blacklist is armor and armor toughness. **Vanilla netherite armor therefore contributes only knockback resistance.** You can add other attribute IDs, or use an empty blacklist to allow every donor modifier. The blacklist applies to donor bonuses in both manual and automatic integrations; it does not suppress the Ars armor's own attributes or the separate tier-toughness rule.

`disable_manual_compatibility = true` forces all donors, including Iron, through attribute-only transfer **only when both fusion and automatic compatibility are enabled**. It defaults to false. This switch affects new fusions; existing native casting data remains intact. Turning off automatic compatibility also leaves existing automatic fusions intact. Changes to the attribute blacklist govern existing donor modifiers too; re-equip equipment after changing it at runtime.

## Configuration defaults

`ars_fusion-server.toml` (NeoForge: `config/`; Forge: world's `serverconfig/`):

```toml
[caster_fusion]
enabled = true
automatic_compatibility = true
goety_staff_compatibility = true
attribute_blacklist = ["minecraft:generic.attack_damage", "minecraft:generic.attack_speed", "minecraft:generic.armor", "minecraft:generic.armor_toughness"]

[crossbow_fusion]
enabled = true
automatic_compatibility = true

[shield_fusion]
enabled = true
automatic_compatibility = true

[bow_fusion]
enabled = true

[sword_fusion]
enabled = true

[armor_fusion]
enabled = true
reverse_enabled = true
automatic_compatibility = true
disable_manual_compatibility = false
attribute_blacklist = ["minecraft:generic.armor", "minecraft:generic.armor_toughness"]

[ars_armor_toughness]
enabled = false
per_tier = 1.0
```

The 万法归一 pack enables tier toughness as well. Pack authors should configure the loader's active server config and `defaultconfigs/ars_fusion-server.toml` for new-world defaults.

## Dependencies and migration

- Ars Nouveau is required. Ars Conflux is not required.
- Iron's Spells 'n Spellbooks is optional. Dedicated Iron armor fusion needs it; generic modifier transfer and tier toughness do not.
- Goety is optional. Native staff identity integration requires it; generic attribute fusion does not.
- Iron & Nouveau is optional. When installed alongside Ars Fusion, use **0.17.32 or newer**; older versions can apply their own fusion modifiers. This minimum is enforced by loader metadata.
- Old `irons_nouveau:armor_fusion` components / NBT remain readable. NeoForge registers the historical codec only when its former owner is absent. New fusions use `ars_fusion:armor_fusion`.
- Iron & Nouveau 0.17.32 retains old-item behavior on its own and hands off to Ars Fusion when installed. Its historical creation/toughness settings no longer enable the migrated features.

Ars Fusion ships no copied Iron or Ars art assets and no original mod JARs. Fused equipment retains the receiver's native rendering; default names and spell information are added.

## Development

See [building and verification](docs/development.md). Shared policy and optional Iron adapter live in `common/`; components/NBT, events and configuration live in the platform source trees. Sword-style binding is separate from armor donor attributes; physical sword damage, attack speed and enchantments are not copied. The native main-hand spell-damage attribute is retained as an explicit exception.
