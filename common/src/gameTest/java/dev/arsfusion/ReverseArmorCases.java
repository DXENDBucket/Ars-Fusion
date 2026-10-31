package dev.arsfusion;

import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.common.block.tile.AlterationTile;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import dev.arsfusion.armor.ArmorFusion;
import dev.arsfusion.armor.ReverseArmorFusion;
import dev.arsfusion.config.FusionConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;

/** Actual menus, native thread data, upgrade recipes and alteration tile; shared on both loaders. */
public final class ReverseArmorCases {
    private ReverseArmorCases() { }
    static void verify(GameTestHelper h,ServerPlayer player,ItemStack threaded,ItemStack emptySameTier,
                       UnaryOperator<ItemStack> upgrade,UnaryOperator<ItemStack> roundTrip,
                       ToDoubleFunction<ItemStack> mana) {
        boolean enabled=FusionConfig.ARMOR_FUSION_ENABLED.get(),reverse=FusionConfig.REVERSE_ARMOR_FUSION_ENABLED.get();
        try {
            FusionConfig.ARMOR_FUSION_ENABLED.set(true);FusionConfig.REVERSE_ARMOR_FUSION_ENABLED.set(true);
            var left=new ItemStack(Items.NETHERITE_CHESTPLATE);left.setDamageValue(17);
            var original=left.copy();var originalDonor=threaded.copy();
            var menu=new AnvilMenu(12,player.getInventory(),ContainerLevelAccess.NULL);player.experienceLevel=10;
            menu.getSlot(0).set(left.copy());menu.getSlot(1).set(threaded.copy());menu.createResult();
            var result=menu.getSlot(2).getItem().copy();
            h.assertTrue(!result.isEmpty()&&result.is(left.getItem())&&result.getDamageValue()==17,"Anvil keeps original armor and damage");
            h.assertTrue(menu.getCost()==1,"Reverse fusion costs one level");menu.getSlot(2).onTake(player,result);
            h.assertTrue(menu.getSlot(0).getItem().isEmpty()&&menu.getSlot(1).getItem().isEmpty()&&player.experienceLevel==9,"Both inputs and one level consumed");
            h.assertTrue(ReverseArmorFusion.has(result)&&!ArmorFusion.ars(result),"Reverse result never enters forward attribute transfer");
            h.assertTrue(PerkUtil.getPerkHolder(result).getTier()==PerkUtil.getPerkHolder(threaded).getTier()
                    &&PerkUtil.getPerkHolder(result).getPerks().equals(PerkUtil.getPerkHolder(threaded).getPerks()),"Native tier and installed threads transfer");
            double expected=mana.applyAsDouble(threaded)-mana.applyAsDouble(emptySameTier);
            h.assertTrue(expected>0&&Math.abs(mana.applyAsDouble(result)-mana.applyAsDouble(left)-expected)<.00001,"Only thread mana transfers, without Ars base mana");
            h.assertTrue(!ReverseArmorFusion.canFuse(result,threaded)&&!ArmorFusion.canFuse(threaded,result)
                    &&!ReverseArmorFusion.canFuse(left,result),"Neither direction can nest fused results");
            h.assertTrue(!ReverseArmorFusion.canFuse(new ItemStack(Items.IRON_HELMET),threaded)
                    &&!ReverseArmorFusion.canFuse(emptySameTier,threaded),"Wrong-slot and existing native Ars equipment rejected");
            h.assertTrue(ItemStack.matches(left,original)&&ItemStack.matches(threaded,originalDonor),"Preview never mutates input stacks");
            var saved=roundTrip.apply(result);
            var upgraded=upgrade.apply(saved.copy());
            h.assertTrue(upgraded.is(left.getItem())&&ReverseArmorFusion.has(upgraded)&&upgraded.getDamageValue()==17,"Native upgrade preserves item and fusion marker");
            h.assertTrue(PerkUtil.getPerkHolder(upgraded).getTier()==PerkUtil.getPerkHolder(saved).getTier()+1
                    &&PerkUtil.getPerkHolder(upgraded).getPerks().equals(PerkUtil.getPerkHolder(saved).getPerks()),"Native upgrade increases tier and retains threads");
            FusionConfig.REVERSE_ARMOR_FUSION_ENABLED.set(false);
            h.assertTrue(ReverseArmorFusion.fuse(left,threaded).isEmpty()&&ReverseArmorFusion.has(saved)
                    &&mana.applyAsDouble(saved)==mana.applyAsDouble(result),"Creation toggle preserves existing thread effects");
            FusionConfig.REVERSE_ARMOR_FUSION_ENABLED.set(true);FusionConfig.ARMOR_FUSION_ENABLED.set(false);
            h.assertTrue(ReverseArmorFusion.fuse(left,threaded).isEmpty(),"Master armor toggle also controls reverse crafting");
            h.assertTrue(PerkUtil.getPerkHolder(new ItemStack(left.getItem()))==null,"Other stacks of the recipient item gain no slots");
        } finally {FusionConfig.ARMOR_FUSION_ENABLED.set(enabled);FusionConfig.REVERSE_ARMOR_FUSION_ENABLED.set(reverse);}
        h.succeed();
    }
    static void alteration(GameTestHelper h,ServerPlayer player,ItemStack threaded) {
        boolean enabled=FusionConfig.ARMOR_FUSION_ENABLED.get(),reverse=FusionConfig.REVERSE_ARMOR_FUSION_ENABLED.get();
        try {
            FusionConfig.ARMOR_FUSION_ENABLED.set(true);FusionConfig.REVERSE_ARMOR_FUSION_ENABLED.set(true);
            var fused=ReverseArmorFusion.fuse(new ItemStack(Items.DIAMOND_CHESTPLATE),threaded);
            var pos=new BlockPos(2,2,2);h.setBlock(pos,BlockRegistry.ALTERATION_TABLE.get().defaultBlockState());
            var tile=(AlterationTile)h.getBlockEntity(pos);
            tile.setArmorStack(fused.copy(),player);
            h.assertTrue(tile.armorStack.is(Items.DIAMOND_CHESTPLATE)&&tile.perkList.size()==1,"Native table extracts the actual installed thread");
            tile.removePerk(player);
            h.assertTrue(tile.perkList.isEmpty(),"Native table can remove the thread");
            var perk=player.getInventory().items.stream().filter(s->s.getItem() instanceof com.hollingsworth.arsnouveau.common.items.PerkItem).findFirst().orElseThrow();
            tile.addPerkStack(perk,player);
            h.assertTrue(tile.perkList.size()==1,"Native table can reinstall the thread using donor slots");
            tile.removeArmorStack(player);
            var returned=player.getInventory().items.stream().filter(ReverseArmorFusion::has).findFirst().orElseThrow();
            h.assertTrue(returned.is(Items.DIAMOND_CHESTPLATE)&&PerkUtil.getPerkHolder(returned).getPerks().size()==1,"Returned original armor retains native thread data");
        } finally {FusionConfig.ARMOR_FUSION_ENABLED.set(enabled);FusionConfig.REVERSE_ARMOR_FUSION_ENABLED.set(reverse);}
        h.succeed();
    }
}
