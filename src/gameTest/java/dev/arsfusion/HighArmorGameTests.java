package dev.arsfusion;
import com.hollingsworth.arsnouveau.common.crafting.recipes.*;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import com.hollingsworth.arsnouveau.common.perk.*;
import dev.arsfusion.armor.HighArmorRecipe;
import dev.arsfusion.armor.ArmorFusion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import java.util.List;
@GameTestHolder("ars_fusion") @PrefixGameTestTemplate(false)
public final class HighArmorGameTests {
    private static HighArmorRecipe recipe(GameTestHelper h,int tier) {
        return (HighArmorRecipe)h.getLevel().getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("ars_fusion","armor_upgrade_"+tier)).orElseThrow().value();
    }
    private static ItemStack apply(GameTestHelper h,ArmorUpgradeRecipe r,ItemStack stack) {
        return r.assemble(new ApparatusRecipeInput(stack,List.of(),null),h.getLevel().registryAccess());
    }
    @GameTest(template="empty") public static void nativeTierUpgradeAndFusion(GameTestHelper h) {
        var four=recipe(h,4);var five=recipe(h,5);
        h.assertTrue(four.tier==3&&four.sourceCost()==7500&&five.tier==4&&five.sourceCost()==10000,"Loaded recipe data has actual destination tiers and source costs");
        var encoded=HighArmorRecipe.Serializer.CODEC.codec().encodeStart(com.mojang.serialization.JsonOps.INSTANCE,five).getOrThrow();
        h.assertTrue(HighArmorRecipe.Serializer.CODEC.codec().parse(com.mojang.serialization.JsonOps.INSTANCE,encoded).getOrThrow().tier==4,"Recipe codec retains the subclass and tier");
        HighArmorCases.upgrades(h,s->apply(h,new ArmorUpgradeRecipe(List.of(),0,2),s),s->apply(h,four,s),s->apply(h,five,s));
    }
    @GameTest(template="empty") public static void fiveThreadsAndHigherNumericStrength(GameTestHelper h) {
        var source=new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("ars_nouveau","sorcerer_robes")));
        for(int tier=3;tier<=4;tier++) {
            var stack=source.copy();stack.set(DataComponentRegistry.ARMOR_PERKS,PerkUtil.getPerkHolder(stack).setTier(tier).setPerks(List.of(MagicCapacityPerk.INSTANCE)));
            double[] amount={0};stack.forEachModifier(ArmorFusion.slot(stack),(a,m)->{if(a.value()==PerkAttributes.MAX_MANA.get()&&m.operation()==net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)amount[0]+=m.amount();});
            h.assertTrue(Math.abs(amount[0]-.1*(tier+1))<.00001,"Capacity thread uses actual IV/V slot strength");
        }
        source.set(DataComponentRegistry.ARMOR_PERKS,PerkUtil.getPerkHolder(source).setTier(4));
        HighArmorCases.alteration(h,TestPlayers.player(h),source,s->{var buf=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),h.getLevel().registryAccess());
            try{ItemStack.STREAM_CODEC.encode(buf,s);return ItemStack.STREAM_CODEC.decode(buf);}finally{buf.release();}});
    }
}
