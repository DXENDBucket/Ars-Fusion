package dev.arsfusion.platform;

import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import dev.arsfusion.armor.ArmorFusion;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class FusionPlatform {
    private static final String KEY="ars_fusion:armor_fusion";
    private FusionPlatform() {}
    public static boolean ironLoaded() {return net.minecraftforge.fml.ModList.get().isLoaded("irons_spellbooks");}

    public static boolean has(ItemStack stack){return stack.hasTag()&&(stack.getTag().contains(KEY,net.minecraft.nbt.Tag.TAG_COMPOUND)||stack.getTag().contains("irons_nouveau:armor_fusion",net.minecraft.nbt.Tag.TAG_COMPOUND));}
    public static ItemStack donor(ItemStack stack){return has(stack)?ItemStack.of(stack.getTag().getCompound(stack.getTag().contains(KEY,net.minecraft.nbt.Tag.TAG_COMPOUND)?KEY:"irons_nouveau:armor_fusion")):ItemStack.EMPTY;}
    public static void set(ItemStack stack,ItemStack donor){stack.getOrCreateTag().put(KEY,donor.save(new net.minecraft.nbt.CompoundTag()));}
    public static boolean customName(ItemStack stack){return stack.hasCustomHoverName();}
    public static int tier(ItemStack stack){var perks=PerkUtil.getPerkHolder(stack);return perks==null?0:perks.getTier()+1;}
    public static void register(IEventBus bus) {
        dev.arsfusion.api.ArmorDonors.installDefaults();BowPlatform.register(bus);MinecraftForge.EVENT_BUS.addListener(FusionPlatform::anvil);MinecraftForge.EVENT_BUS.addListener(FusionPlatform::attributes);MinecraftForge.EVENT_BUS.addListener(SwordPlatform::tooltip);}
    private static void anvil(AnvilUpdateEvent event) {
        var output=ArmorFusion.fuse(event.getLeft(),event.getRight());
        if(output.isEmpty())output=dev.arsfusion.sword.SwordFusion.fuse(event.getLeft(),event.getRight());
        if(output.isEmpty())output=dev.arsfusion.bow.BowFusion.fuse(event.getLeft(),event.getRight());
        if(output.isEmpty())return;
        String name=event.getName();
        if(name!=null&&!name.isBlank()&&!name.equals(event.getLeft().getHoverName().getString()))output.setHoverName(Component.literal(name));
        event.setOutput(output);event.setCost(1);event.setMaterialCost(1);
    }
    private static void attributes(ItemAttributeModifierEvent event) {
        var stack=event.getItemStack();if(!ArmorFusion.ars(stack)||event.getSlotType()!=ArmorFusion.slot(stack))return;
        var slot=ArmorFusion.slot(stack);var donor=ArmorFusion.donor(stack);
        if(!donor.isEmpty())donor.getAttributeModifiers(slot).forEach((attribute,modifier)->{
            var key=BuiltInRegistries.ATTRIBUTE.getKey(attribute);
            if(key==null||!dev.arsfusion.config.FusionConfig.allowsAttribute(key.toString()))return;
            String id="ars_fusion:armor_fusion/"+slot.getName()+"/"+BuiltInRegistries.ATTRIBUTE.getKey(attribute)+"/"+modifier.getId();
            event.addModifier(attribute,new AttributeModifier(UUID.nameUUIDFromBytes(id.getBytes(StandardCharsets.UTF_8)),id,modifier.getAmount(),modifier.getOperation()));
        });
        double toughness=ArmorFusion.toughness(stack);
        if(toughness!=0)event.addModifier(Attributes.ARMOR_TOUGHNESS,new AttributeModifier(UUID.nameUUIDFromBytes(("ars_fusion:ars_toughness/"+slot.getName()).getBytes(StandardCharsets.UTF_8)),"Ars armor tier toughness",toughness,AttributeModifier.Operation.ADDITION));
    }
}
