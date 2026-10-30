package dev.arsfusion.platform;

import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import dev.arsfusion.armor.ArmorFusion;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FusionPlatform {
    private static final DeferredRegister<DataComponentType<?>> TYPES=DeferredRegister.create(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE,"ars_fusion");
    // ItemStack itself is mutable and cannot be a component value. Vanilla's
    // immutable snapshot supplies stack-aware equality, hashing and both codecs.
    private static final java.util.function.Supplier<DataComponentType<ItemContainerContents>> TYPE=TYPES.register("armor_fusion",()->DataComponentType.<ItemContainerContents>builder()
            .persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC).build());
    // Register the historical codec only when its original owner is absent, so old saves
    // remain readable even after Iron & Nouveau is removed. Never register the same ID twice.
    private static final DeferredRegister<DataComponentType<?>> LEGACY=DeferredRegister.create(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE,"irons_nouveau");
    private static DataComponentType<ItemContainerContents> legacyType() {
        @SuppressWarnings("unchecked") var type=(DataComponentType<ItemContainerContents>)BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.fromNamespaceAndPath("irons_nouveau","armor_fusion"));
        return type;
    }
    private FusionPlatform() {}
    public static boolean ironLoaded() {return net.neoforged.fml.ModList.get().isLoaded("irons_spellbooks");}

    public static boolean has(ItemStack stack){var legacy=legacyType();return stack.has(TYPE.get())||(legacy!=null&&stack.has(legacy));}
    public static ItemStack donor(ItemStack stack){var contents=stack.get(TYPE.get());var legacy=legacyType();return contents!=null?contents.copyOne():legacy==null?ItemStack.EMPTY:stack.getOrDefault(legacy,ItemContainerContents.EMPTY).copyOne();}
    public static void set(ItemStack stack,ItemStack donor){stack.set(TYPE.get(),ItemContainerContents.fromItems(java.util.List.of(donor)));}
    public static boolean customName(ItemStack stack){return stack.has(DataComponents.CUSTOM_NAME);}
    public static int tier(ItemStack stack){var perks=PerkUtil.getPerkHolder(stack);return perks==null?0:perks.getTier()+1;}
    public static void register(IEventBus bus) {
        dev.arsfusion.api.ArmorDonors.installDefaults();BowPlatform.register(bus);
        SwordPlatform.register(bus);
        TYPES.register(bus);
        if(!net.neoforged.fml.ModList.get().isLoaded("irons_nouveau")) {
            LEGACY.register("armor_fusion",()->DataComponentType.<ItemContainerContents>builder().persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC).build());
            LEGACY.register(bus);
        }
        NeoForge.EVENT_BUS.addListener(FusionPlatform::anvil);NeoForge.EVENT_BUS.addListener(FusionPlatform::attributes);NeoForge.EVENT_BUS.addListener(SwordPlatform::tooltip);
    }
    private static void anvil(AnvilUpdateEvent event) {
        var output=ArmorFusion.fuse(event.getLeft(),event.getRight());
        if(output.isEmpty())output=dev.arsfusion.sword.SwordFusion.fuse(event.getLeft(),event.getRight());
        if(output.isEmpty())output=dev.arsfusion.bow.BowFusion.fuse(event.getLeft(),event.getRight());
        if(output.isEmpty())return;
        String name=event.getName();
        if(name!=null&&!name.isBlank()&&!name.equals(event.getLeft().getHoverName().getString()))output.set(DataComponents.CUSTOM_NAME,Component.literal(name));
        event.setOutput(output);event.setCost(1);event.setMaterialCost(1);
    }
    private static void attributes(ItemAttributeModifierEvent event) {
        var stack=event.getItemStack();if(!ArmorFusion.ars(stack))return;
        var slot=ArmorFusion.slot(stack);var group=EquipmentSlotGroup.bySlot(slot);var donor=ArmorFusion.donor(stack);
        if(!donor.isEmpty())donor.forEachModifier(slot,(attribute,modifier)->{
            var key=BuiltInRegistries.ATTRIBUTE.getKey(attribute.value());
            if(key==null||!dev.arsfusion.config.FusionConfig.allowsAttribute(key.toString()))return;
            var original=modifier.id();
            var id=ResourceLocation.fromNamespaceAndPath("ars_fusion","fusion/"+slot.getName()+"/"+key.getNamespace()+"/"+key.getPath()+"/"+original.getNamespace()+"/"+original.getPath());
            event.addModifier(attribute,new AttributeModifier(id,modifier.amount(),modifier.operation()),group);
        });
        double toughness=ArmorFusion.toughness(stack);
        if(toughness!=0)event.addModifier(Attributes.ARMOR_TOUGHNESS,new AttributeModifier(ResourceLocation.fromNamespaceAndPath("ars_fusion","ars_toughness/"+slot.getName()),toughness,AttributeModifier.Operation.ADD_VALUE),group);
    }
}
