package dev.arsfusion;

import com.hollingsworth.arsnouveau.api.registry.PerkRegistry;
import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.common.block.tile.AlterationTile;
import com.hollingsworth.arsnouveau.common.perk.*;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import dev.arsfusion.armor.ArmorFusion;
import dev.arsfusion.armor.ReverseArmorFusion;
import dev.arsfusion.config.FusionConfig;
import dev.arsfusion.platform.HighArmorPlatform;
import dev.arsfusion.platform.FusionPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.List;
import java.util.function.UnaryOperator;

public final class HighArmorCases {
    private HighArmorCases() { }
    public static void upgrades(GameTestHelper h,UnaryOperator<ItemStack> tierThree,
                                UnaryOperator<ItemStack> four,UnaryOperator<ItemStack> five) {
        boolean fusion=FusionConfig.ARMOR_FUSION_ENABLED.get(),reverse=FusionConfig.REVERSE_ARMOR_FUSION_ENABLED.get(),auto=FusionConfig.AUTOMATIC_ARMOR_COMPATIBILITY.get();
        try {
            FusionConfig.ARMOR_FUSION_ENABLED.set(true);FusionConfig.REVERSE_ARMOR_FUSION_ENABLED.set(true);FusionConfig.AUTOMATIC_ARMOR_COMPATIBILITY.set(true);
            for(var item:BuiltInRegistries.ITEM) {
                var original=new ItemStack(item);if(!dev.arsfusion.armor.ArmorLayouts.managed(original))continue;
                var before=tierThree.apply(original);before.setDamageValue(7);
                h.assertTrue(HighArmorPlatform.slotCount(before)==3,"Native tier three still has three slots");
                var fourth=four.apply(before.copy());var fifth=five.apply(fourth.copy());
                h.assertTrue(FusionPlatform.tier(fourth)==4&&HighArmorPlatform.slotCount(fourth)==4,"Tier four uses real native data and four slots");
                h.assertTrue(FusionPlatform.tier(fifth)==5&&HighArmorPlatform.slotCount(fifth)==5,"Tier five uses real native data and five slots");
                h.assertTrue(fifth.is(item)&&fifth.getDamageValue()==7,"Native upgrade keeps item identity and wear");
                h.assertTrue(!HighArmorPlatform.supportsTier(fifth,5),"No unsupported sixth tier is exposed");
                if(ArmorFusion.slot(before)==net.minecraft.world.entity.EquipmentSlot.CHEST) {
                    var forward=ArmorFusion.fuse(before,new ItemStack(Items.NETHERITE_CHESTPLATE));
                    var reversed=ReverseArmorFusion.fuse(new ItemStack(Items.DIAMOND_CHESTPLATE),before);
                    h.assertTrue(ArmorFusion.has(five.apply(four.apply(forward))),"Forward fusion survives both upgrades");
                    var upgraded=five.apply(four.apply(reversed));
                    h.assertTrue(ReverseArmorFusion.has(upgraded)&&upgraded.is(Items.DIAMOND_CHESTPLATE)&&HighArmorPlatform.slotCount(upgraded)==5,"Reverse fusion follows the donor provider through both upgrades");
                }
            }
        } finally {
            FusionConfig.ARMOR_FUSION_ENABLED.set(fusion);FusionConfig.REVERSE_ARMOR_FUSION_ENABLED.set(reverse);FusionConfig.AUTOMATIC_ARMOR_COMPATIBILITY.set(auto);
        }
        h.succeed();
    }
    public static void alteration(GameTestHelper h,ServerPlayer player,ItemStack fifth,UnaryOperator<ItemStack> roundTrip) {
        var pos=new BlockPos(2,2,2);h.setBlock(pos,BlockRegistry.ALTERATION_TABLE.get().defaultBlockState());
        var table=(AlterationTile)h.getBlockEntity(pos);table.setArmorStack(fifth.copy(),player);
        for(var perk:List.of(MagicCapacityPerk.INSTANCE,SpellDamagePerk.INSTANCE,ToughnessPerk.INSTANCE,KnockbackResistPerk.INSTANCE,StarbunclePerk.INSTANCE))
            table.addPerkStack(new ItemStack(PerkRegistry.getPerkItemMap().get(perk.getRegistryName())),player);
        h.assertTrue(table.perkList.size()==5,"Native table accepts fourth and fifth threads");
        table.addPerkStack(new ItemStack(PerkRegistry.getPerkItemMap().get(FeatherPerk.INSTANCE.getRegistryName())),player);
        h.assertTrue(table.perkList.size()==5,"Real slot count still prevents a sixth thread");
        table.removeArmorStack(player);
        var returned=player.getInventory().items.stream().filter(s->s.is(fifth.getItem())).findFirst().orElseThrow();
        var saved=roundTrip.apply(returned);
        h.assertTrue(PerkUtil.getPerkHolder(saved).getPerks().size()==5&&HighArmorPlatform.slotCount(saved)==5,"All five native threads survive storage codecs");
        h.succeed();
    }
}
