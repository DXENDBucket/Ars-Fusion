package dev.arsfusion;
import com.hollingsworth.arsnouveau.api.enchanting_apparatus.ArmorUpgradeRecipe;
import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import com.hollingsworth.arsnouveau.common.perk.*;
import dev.arsfusion.armor.HighArmorRecipe;
import dev.arsfusion.armor.ArmorFusion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
import java.util.List;
@GameTestHolder("ars_fusion") @PrefixGameTestTemplate(false)
public final class HighArmorGameTests {
    private static HighArmorRecipe recipe(GameTestHelper h,int tier) {
        return (HighArmorRecipe)h.getLevel().getRecipeManager().byKey(new ResourceLocation("ars_fusion","armor_upgrade_"+tier)).orElseThrow();
    }
    @GameTest(template="empty") public static void nativeTierUpgradeAndFusion(GameTestHelper h) {
        var four=recipe(h,4);var five=recipe(h,5);
        h.assertTrue(four.tier==3&&four.getSourceCost()==7500&&five.tier==4&&five.getSourceCost()==10000,"Loaded recipe data has actual destination tiers and source costs");
        var serializer=new HighArmorRecipe.Serializer();var buf=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        try{serializer.toNetwork(buf,five);h.assertTrue(serializer.fromNetwork(five.getId(),buf).tier==4,"Recipe network codec retains its destination tier");}finally{buf.release();}
        HighArmorCases.upgrades(h,s->new ArmorUpgradeRecipe(List.of(),0,2).getResult(List.of(),s,null),s->four.getResult(List.of(),s,null),s->five.getResult(List.of(),s,null));
    }
    @GameTest(template="empty") public static void fiveThreadsAndHigherNumericStrength(GameTestHelper h) {
        var source=new ItemStack(BuiltInRegistries.ITEM.get(new ResourceLocation("ars_nouveau","sorcerer_robes")));
        for(int tier=3;tier<=4;tier++) {
            var stack=source.copy();var holder=PerkUtil.getPerkHolder(stack);holder.setTier(tier);holder.setPerks(List.of(MagicCapacityPerk.INSTANCE));
            double[] amount={0};stack.getAttributeModifiers(ArmorFusion.slot(stack)).forEach((a,m)->{if(a==PerkAttributes.MAX_MANA.get()&&m.getOperation()==net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_TOTAL)amount[0]+=m.getAmount();});
            h.assertTrue(Math.abs(amount[0]-.1*(tier+1))<.00001,"Capacity thread uses actual IV/V slot strength");
        }
        PerkUtil.getPerkHolder(source).setTier(4);
        HighArmorCases.alteration(h,TestPlayers.player(h),source,s->{var buf=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try{buf.writeItem(s);return buf.readItem();}finally{buf.release();}});
    }
}
