package dev.arsfusion;

import dev.arsfusion.armor.ArmorFusion;
import dev.arsfusion.config.FusionConfig;
import dev.arsfusion.platform.FusionPlatform;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.gui.arcane_anvil.ArcaneAnvilMenu;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.function.UnaryOperator;

/** Exercise real native menus and upgrade recipes; no duplicate crafting or casting implementation. */
public final class ArmorFusionCases {
    private ArmorFusionCases() {}
    static ItemStack ars(ArmorItem.Type type) {
        return BuiltInRegistries.ITEM.stream().map(ItemStack::new)
                .filter(s -> ArmorFusion.ars(s) && ((ArmorItem)s.getItem()).getType() == type)
                .findFirst().orElseThrow();
    }
    static ItemStack iron(ArmorItem.Type type) {
        return BuiltInRegistries.ITEM.stream().map(ItemStack::new)
                .filter(s -> ArmorFusion.iron(s) && ((ArmorItem)s.getItem()).getType() == type
                        && s.getItem() instanceof io.redspace.ironsspellbooks.item.armor.ImbuableChestplateArmorItem)
                .findFirst().orElseThrow();
    }
    static void enabled(Runnable test) {
        boolean old = FusionConfig.ARMOR_FUSION_ENABLED.get();
        boolean auto = FusionConfig.AUTOMATIC_ARMOR_COMPATIBILITY.get();
        try { FusionConfig.ARMOR_FUSION_ENABLED.set(true); FusionConfig.AUTOMATIC_ARMOR_COMPATIBILITY.set(false); test.run(); }
        finally { FusionConfig.ARMOR_FUSION_ENABLED.set(old); FusionConfig.AUTOMATIC_ARMOR_COMPATIBILITY.set(auto); }
    }
    public static void anvil(GameTestHelper h, ServerPlayer player) {
        var left = ars(ArmorItem.Type.CHESTPLATE);
        var right = iron(ArmorItem.Type.CHESTPLATE);
        h.assertTrue(FusionConfig.ARMOR_FUSION_ENABLED.getDefault()
                && FusionConfig.TOUGHNESS_ENABLED.getDefault() == false, "Fusion is enabled by default; toughness is opt-in");
        boolean old = FusionConfig.ARMOR_FUSION_ENABLED.get();
        try {
            FusionConfig.ARMOR_FUSION_ENABLED.set(false);
            var disabled = new AnvilMenu(1, player.getInventory(), ContainerLevelAccess.NULL);
            disabled.getSlot(0).set(left.copy()); disabled.getSlot(1).set(right.copy()); disabled.createResult();
            h.assertTrue(disabled.getSlot(2).getItem().isEmpty(), "Disabled fusion has no native anvil output");
        } finally { FusionConfig.ARMOR_FUSION_ENABLED.set(old); }
        enabled(() -> {
            h.assertTrue(!ArmorFusion.canFuse(right, left), "Ars must be the left input");
            h.assertTrue(!ArmorFusion.canFuse(left, iron(ArmorItem.Type.HELMET)), "Different armor slots cannot fuse");
            h.assertTrue(!ArmorFusion.canFuse(new ItemStack(Items.IRON_CHESTPLATE), right), "Ordinary armor cannot fuse");
            left.setDamageValue(17);
            var menu = new AnvilMenu(2, player.getInventory(), ContainerLevelAccess.NULL);
            player.experienceLevel = 10;
            menu.getSlot(0).set(left.copy()); menu.getSlot(1).set(right.copy()); menu.createResult();
            var result = menu.getSlot(2).getItem().copy();
            h.assertTrue(!result.isEmpty() && result.is(left.getItem()) && result.getDamageValue() == 17,
                    "Native anvil preserves Ars identity, model and durability");
            h.assertTrue(menu.getCost() == 1 && menu.getSlot(2).mayPickup(player), "Fusion costs one level");
            menu.getSlot(2).onTake(player, result);
            h.assertTrue(menu.getSlot(0).getItem().isEmpty() && menu.getSlot(1).getItem().isEmpty()
                    && player.experienceLevel == 9, "Taking output consumes both inputs and one level");
            h.assertTrue(ArmorFusion.has(result) && !ArmorFusion.canFuse(result, right), "Cannot fuse twice");
            h.assertTrue(result.getHoverName().getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents name
                    && name.getKey().equals("item.ars_fusion.fused_armor"), "Combined display name");
        });
        h.succeed();
    }
    public static void infusionAndUpgrade(GameTestHelper h, ServerPlayer player,
                                          UnaryOperator<ItemStack> upgrade, UnaryOperator<ItemStack> roundTrip) {
        enabled(() -> {
            var source = ars(ArmorItem.Type.CHESTPLATE);
            var result = ArmorFusion.fuse(source, iron(ArmorItem.Type.CHESTPLATE));
            h.assertTrue(Utils.canImbue(result) && ISpellContainer.get(result).getMaxSpellCount() == 1,
                    "Iron native infusion recognizes the Ars chestplate");
            var menu = new ArcaneAnvilMenu(3, player.getInventory(), ContainerLevelAccess.NULL);
            var scroll = new ItemStack(ItemRegistry.SCROLL.get());
            ISpellContainer.set(scroll, ISpellContainer.createScrollContainer(SpellRegistry.FIREBOLT_SPELL.get(), 1, scroll));
            menu.getSlot(0).set(result); menu.getSlot(1).set(scroll); menu.createResult();
            var imbued = menu.getSlot(2).getItem().copy();
            h.assertTrue(!imbued.isEmpty() && ISpellContainer.get(imbued).getActiveSpellCount() == 1,
                    "Native arcane anvil imbues the fused armor");
            h.assertTrue(ISpellContainer.get(imbued).getSpellAtIndex(0).getSpell() == SpellRegistry.FIREBOLT_SPELL.get(),
                    "Native spell data remains usable");
            var upgraded = upgrade.apply(imbued);
            h.assertTrue(upgraded.is(source.getItem()) && FusionPlatform.tier(upgraded) == 2
                    && ArmorFusion.has(upgraded) && ISpellContainer.get(upgraded).getActiveSpellCount() == 1,
                    "Native Ars upgrade retains fusion and injected Iron spell");
            var saved = roundTrip.apply(upgraded);
            h.assertTrue(ArmorFusion.has(saved) && ArmorFusion.donor(saved).is(ArmorFusion.donor(result).getItem())
                    && FusionPlatform.tier(saved) == 2 && ISpellContainer.get(saved).getActiveSpellCount() == 1,
                    "Fusion, Ars tier and Iron spell survive persistent/network round-trip");
            // Turning off future crafting must never strip existing equipment.
            FusionConfig.ARMOR_FUSION_ENABLED.set(false);
            h.assertTrue(ArmorFusion.has(saved) && ISpellContainer.get(saved).getActiveSpellCount() == 1,
                    "Disabling fusion only prevents new recipes");
        });
        h.succeed();
    }
    public static void manualCompatibilityOverride(GameTestHelper h) {
        enabled(() -> {
            var source=ars(ArmorItem.Type.CHESTPLATE);var donor=iron(ArmorItem.Type.CHESTPLATE);
            boolean auto=FusionConfig.AUTOMATIC_ARMOR_COMPATIBILITY.get();
            boolean disabled=FusionConfig.DISABLE_MANUAL_COMPATIBILITY.get();
            try {
                FusionConfig.AUTOMATIC_ARMOR_COMPATIBILITY.set(true);FusionConfig.DISABLE_MANUAL_COMPATIBILITY.set(false);
                var manual=ArmorFusion.fuse(source,donor);
                h.assertTrue(ISpellContainer.get(manual)!=null&&ISpellContainer.get(manual).getMaxSpellCount()==1,"Default uses dedicated Iron spell-container integration");
                FusionConfig.DISABLE_MANUAL_COMPATIBILITY.set(true);
                var automatic=ArmorFusion.fuse(source,donor);var spells=ISpellContainer.get(automatic);
                h.assertTrue(!automatic.isEmpty()&&(spells==null||spells.getMaxSpellCount()==0),"Force automatic mode skips native Iron spell-container transfer");
                h.assertTrue(ISpellContainer.get(manual).getMaxSpellCount()==1,"Switch never erases data from existing manual fusions");
                FusionConfig.AUTOMATIC_ARMOR_COMPATIBILITY.set(false);
                var fallback=ArmorFusion.fuse(source,donor);
                h.assertTrue(ISpellContainer.get(fallback)!=null&&ISpellContainer.get(fallback).getMaxSpellCount()==1,"Disable-manual flag is inactive unless automatic compatibility is also enabled");
                FusionConfig.ARMOR_FUSION_ENABLED.set(false);
                h.assertTrue(ArmorFusion.fuse(source,donor).isEmpty(),"Master switch still prevents every fusion route");
            } finally {
                FusionConfig.AUTOMATIC_ARMOR_COMPATIBILITY.set(auto);FusionConfig.DISABLE_MANUAL_COMPATIBILITY.set(disabled);
            }
        });
        h.succeed();
    }
}
