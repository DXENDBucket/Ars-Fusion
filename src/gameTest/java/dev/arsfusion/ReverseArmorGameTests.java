package dev.arsfusion;
import dev.arsfusion.armor.ArmorFusion;
import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.common.perk.MagicCapacityPerk;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import java.util.List;
import net.neoforged.neoforge.gametest.*;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import com.hollingsworth.arsnouveau.common.crafting.recipes.*;
import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
@GameTestHolder("ars_fusion") @PrefixGameTestTemplate(false)
public final class ReverseArmorGameTests {
    private static ItemStack source(boolean threaded) {
        var stack=BuiltInRegistries.ITEM.stream().map(ItemStack::new).filter(s->ArmorFusion.ars(s)&&((ArmorItem)s.getItem()).getType()==ArmorItem.Type.CHESTPLATE).findFirst().orElseThrow();
        stack.set(DataComponentRegistry.ARMOR_PERKS,PerkUtil.getPerkHolder(stack).setTier(1).setPerks(threaded?List.of(MagicCapacityPerk.INSTANCE):List.of()));return stack;
    }
    private static double mana(ItemStack stack) {
        double[] total={0};stack.forEachModifier(ArmorFusion.slot(stack),(a,m)->{if(a.value()==PerkAttributes.MAX_MANA.get())total[0]+=m.amount();});return total[0];
    }
    @GameTest(template="empty") public static void nativeAnvilUpgradeAndRoundTrip(GameTestHelper h) {
        ReverseArmorCases.verify(h,TestPlayers.player(h),source(true),source(false),
            s->new ArmorUpgradeRecipe(List.of(),0,2).assemble(new ApparatusRecipeInput(s,List.of(),null),h.getLevel().registryAccess()),
            s->{var saved=ItemStack.parse(h.getLevel().registryAccess(),s.save(h.getLevel().registryAccess())).orElseThrow();
                var buf=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),h.getLevel().registryAccess());
                try{ItemStack.STREAM_CODEC.encode(buf,saved);return ItemStack.STREAM_CODEC.decode(buf);}finally{buf.release();}},ReverseArmorGameTests::mana);
    }
    @GameTest(template="empty") public static void nativeAlterationRoundTrip(GameTestHelper h) {ReverseArmorCases.alteration(h,TestPlayers.player(h),source(true));}
    @GameTest(template="empty") public static void reverseManaPolicy(GameTestHelper h) {
        var left=new ItemStack(Items.NETHERITE_CHESTPLATE);
        left.set(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,left.getItem().getDefaultAttributeModifiers(left)
                .withModifierAdded(PerkAttributes.MAX_MANA,new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ars_fusion","test_receiver_mana"),37,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.CHEST)
                .withModifierAdded(PerkAttributes.MANA_REGEN_BONUS,new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                        net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ars_fusion","test_receiver_regen"),5,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.CHEST));
        ReverseManaCases.verify(h,left,source(true),source(false),
                s->new ArmorUpgradeRecipe(List.of(),0,2).assemble(new ApparatusRecipeInput(s,List.of(),null),h.getLevel().registryAccess()),
                s->{double[] v=new double[6];s.forEachModifier(ArmorFusion.slot(s),(a,m)->{
                    int offset=a.value()==PerkAttributes.MAX_MANA.get()?0:a.value()==PerkAttributes.MANA_REGEN_BONUS.get()?3:-1;
                    if(offset>=0)v[offset+m.operation().ordinal()]+=m.amount();
                });return v;});
    }
    @GameTest(template="empty") public static void reverseDefensePolicy(GameTestHelper h) {
        var donor=source(false);var empty=donor.copy();
        donor.set(DataComponentRegistry.ARMOR_PERKS,PerkUtil.getPerkHolder(donor)
                .setPerks(List.of(com.hollingsworth.arsnouveau.common.perk.ToughnessPerk.INSTANCE)));
        ReverseDefenseCases.verify(h,donor,empty,
                s->new ArmorUpgradeRecipe(List.of(),0,2).assemble(new ApparatusRecipeInput(s,List.of(),null),h.getLevel().registryAccess()),
                s->{double[] v={0,0,0};s.forEachModifier(ArmorFusion.slot(s),(a,m)->{
                    if(a.equals(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR))v[0]+=m.amount();
                    if(a.equals(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS))v[1]+=m.amount();
                    if(a.equals(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE))v[2]+=m.amount();
                });return new ReverseDefenseCases.Values(v[0],v[1],v[2]);});
    }
}
