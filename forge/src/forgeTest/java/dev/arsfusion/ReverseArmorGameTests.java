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
    @GameTest(template="empty") public static void reverseManaPolicy(GameTestHelper h) {
        var left=new ItemStack(Items.NETHERITE_CHESTPLATE);
        left.addAttributeModifier(PerkAttributes.MAX_MANA.get(),new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                java.util.UUID.fromString("996d4d5c-eb24-4b10-b85c-f5b18f3f63e1"),"Receiver mana",37,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION),net.minecraft.world.entity.EquipmentSlot.CHEST);
        left.addAttributeModifier(PerkAttributes.MANA_REGEN_BONUS.get(),new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                java.util.UUID.fromString("913ab9e2-44b5-4979-b025-37d90f3f1fe3"),"Receiver regen",5,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION),net.minecraft.world.entity.EquipmentSlot.CHEST);
        ReverseManaCases.verify(h,left,source(true),source(false),s->new ArmorUpgradeRecipe(List.of(),0,2).getResult(List.of(),s,null),
                s->{double[] v=new double[6];s.getAttributeModifiers(ArmorFusion.slot(s)).forEach((a,m)->{
                    int offset=a==PerkAttributes.MAX_MANA.get()?0:a==PerkAttributes.MANA_REGEN_BONUS.get()?3:-1;
                    if(offset>=0)v[offset+m.getOperation().ordinal()]+=m.getAmount();
                });return v;});
    }
    @GameTest(template="empty") public static void reverseDefensePolicy(GameTestHelper h) {
        var donor=source(false);var empty=donor.copy();
        PerkUtil.getPerkHolder(donor).setPerks(List.of(com.hollingsworth.arsnouveau.common.perk.ToughnessPerk.INSTANCE));
        ReverseDefenseCases.verify(h,donor,empty,s->new ArmorUpgradeRecipe(List.of(),0,2).getResult(List.of(),s,null),
                s->{double[] v={0,0,0};s.getAttributeModifiers(ArmorFusion.slot(s)).forEach((a,m)->{
                    if(a==net.minecraft.world.entity.ai.attributes.Attributes.ARMOR)v[0]+=m.getAmount();
                    if(a==net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS)v[1]+=m.getAmount();
                    if(a==net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE)v[2]+=m.getAmount();
                });return new ReverseDefenseCases.Values(v[0],v[1],v[2]);});
    }
}
