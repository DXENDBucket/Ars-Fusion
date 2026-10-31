package dev.arsfusion;
import dev.arsfusion.armor.ArmorFusion;
import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.common.perk.MagicCapacityPerk;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import java.util.List;
import net.minecraftforge.gametest.*;
import com.hollingsworth.arsnouveau.api.enchanting_apparatus.ArmorUpgradeRecipe;
import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
@GameTestHolder("ars_fusion") @PrefixGameTestTemplate(false)
public final class ReverseArmorGameTests {
    private static ItemStack source(boolean threaded) {
        var stack=BuiltInRegistries.ITEM.stream().map(ItemStack::new).filter(s->ArmorFusion.ars(s)&&((ArmorItem)s.getItem()).getType()==ArmorItem.Type.CHESTPLATE).findFirst().orElseThrow();
        var holder=PerkUtil.getPerkHolder(stack);holder.setTier(1);holder.setPerks(threaded?List.of(MagicCapacityPerk.INSTANCE):List.of());return stack;
    }
    private static double mana(ItemStack stack) {
        double[] total={0};stack.getAttributeModifiers(ArmorFusion.slot(stack)).forEach((a,m)->{if(a==PerkAttributes.MAX_MANA.get())total[0]+=m.getAmount();});return total[0];
    }
    @GameTest(template="empty") public static void nativeAnvilUpgradeAndRoundTrip(GameTestHelper h) {
        ReverseArmorCases.verify(h,TestPlayers.player(h),source(true),source(false),
            s->new ArmorUpgradeRecipe(List.of(),0,2).getResult(List.of(),s,null),
            s->{var saved=ItemStack.of(s.save(new net.minecraft.nbt.CompoundTag()));var buf=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
                try{buf.writeItem(saved);return buf.readItem();}finally{buf.release();}},ReverseArmorGameTests::mana);
    }
    @GameTest(template="empty") public static void nativeAlterationRoundTrip(GameTestHelper h) {ReverseArmorCases.alteration(h,TestPlayers.player(h),source(true));}
}
