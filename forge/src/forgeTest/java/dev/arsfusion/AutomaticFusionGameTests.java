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
import net.minecraftforge.gametest.*;

@GameTestHolder("ars_fusion") @PrefixGameTestTemplate(false)
public final class AutomaticFusionGameTests {
    @GameTest(template="empty") public static void automaticModifierTransfer(GameTestHelper h) {
        var donor=new ItemStack(Items.DIAMOND_CHESTPLATE);
        donor.addAttributeModifier(Attributes.ARMOR,new AttributeModifier(UUID.randomUUID(),"auto defense",30,AttributeModifier.Operation.ADDITION),EquipmentSlot.CHEST);
        donor.addAttributeModifier(Attributes.ARMOR_TOUGHNESS,new AttributeModifier(UUID.randomUUID(),"auto toughness",9,AttributeModifier.Operation.ADDITION),EquipmentSlot.CHEST);
        donor.addAttributeModifier(Attributes.MAX_HEALTH,new AttributeModifier(UUID.randomUUID(),"auto health",7,AttributeModifier.Operation.ADDITION),EquipmentSlot.CHEST);
        donor.addAttributeModifier(Attributes.MOVEMENT_SPEED,new AttributeModifier(UUID.randomUUID(),"auto speed",0.15,AttributeModifier.Operation.MULTIPLY_TOTAL),EquipmentSlot.CHEST);
        donor.addAttributeModifier(Attributes.MAX_HEALTH,new AttributeModifier(UUID.randomUUID(),"wrong slot",100,AttributeModifier.Operation.ADDITION),EquipmentSlot.HEAD);
        AutomaticFusionCases.verify(h,donor,AutomaticFusionGameTests::stats,s->{
            var saved=ItemStack.of(s.save(new net.minecraft.nbt.CompoundTag()));
            var buf=new net.minecraft.network.FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
            try {buf.writeItem(saved);return buf.readItem();} finally {buf.release();}
        });
    }
    private static List<AutomaticFusionCases.Stat> stats(ItemStack stack) {
        var result=new ArrayList<AutomaticFusionCases.Stat>();
        stack.getAttributeModifiers(ArmorFusion.slot(stack)).forEach((a,m)->result.add(new AutomaticFusionCases.Stat(
                BuiltInRegistries.ATTRIBUTE.getKey(a).toString(),m.getAmount(),m.getOperation().ordinal(),a==Attributes.ARMOR||a==Attributes.ARMOR_TOUGHNESS)));
        return result;
    }
    @GameTest(template="empty") public static void netheriteAndConfigurableBlacklist(GameTestHelper h) {
        AutomaticFusionCases.netheriteAndBlacklist(h,AutomaticFusionGameTests::stats);
    }
}
