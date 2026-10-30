package dev.arsfusion;
import dev.arsfusion.armor.ArmorFusion;
import net.minecraft.gametest.framework.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder("ars_fusion") @PrefixGameTestTemplate(false)
public final class AutomaticFusionGameTests {
    @GameTest(template="empty") public static void automaticModifierTransfer(GameTestHelper h) {
        var donor=new ItemStack(Items.DIAMOND_CHESTPLATE);
        var chest=net.minecraft.world.entity.EquipmentSlotGroup.CHEST;
        var modifiers=net.minecraft.world.item.component.ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,new AttributeModifier(id("defense"),30,AttributeModifier.Operation.ADD_VALUE),chest)
                .add(Attributes.ARMOR_TOUGHNESS,new AttributeModifier(id("toughness"),9,AttributeModifier.Operation.ADD_VALUE),chest)
                .add(Attributes.MAX_HEALTH,new AttributeModifier(id("health"),7,AttributeModifier.Operation.ADD_VALUE),chest)
                .add(Attributes.MOVEMENT_SPEED,new AttributeModifier(id("speed"),0.15,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),chest)
                .add(Attributes.MAX_HEALTH,new AttributeModifier(id("wrong_slot"),100,AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.HEAD).build();
        donor.set(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,modifiers);
        AutomaticFusionCases.verify(h,donor,AutomaticFusionGameTests::stats,s->{
            var saved=ItemStack.parse(h.getLevel().registryAccess(),s.save(h.getLevel().registryAccess())).orElseThrow();
            var buf=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),h.getLevel().registryAccess());
            try {ItemStack.STREAM_CODEC.encode(buf,saved);return ItemStack.STREAM_CODEC.decode(buf);} finally {buf.release();}
        });
    }
    private static net.minecraft.resources.ResourceLocation id(String name) {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("ars_fusion_test",name);
    }
    private static List<AutomaticFusionCases.Stat> stats(ItemStack stack) {
        var result=new ArrayList<AutomaticFusionCases.Stat>();
        stack.forEachModifier(ArmorFusion.slot(stack),(a,m)->result.add(new AutomaticFusionCases.Stat(
                BuiltInRegistries.ATTRIBUTE.getKey(a.value()).toString(),m.amount(),m.operation().ordinal(),a.value()==Attributes.ARMOR.value()||a.value()==Attributes.ARMOR_TOUGHNESS.value())));
        return result;
    }
    @GameTest(template="empty") public static void netheriteAndConfigurableBlacklist(GameTestHelper h) {
        AutomaticFusionCases.netheriteAndBlacklist(h,AutomaticFusionGameTests::stats);
    }
}
