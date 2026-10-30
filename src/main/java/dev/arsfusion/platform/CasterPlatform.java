package dev.arsfusion.platform;

import dev.arsfusion.caster.*;
import dev.arsfusion.compat.*;
import dev.arsfusion.config.FusionConfig;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import java.util.*;

public final class CasterPlatform {
    private static final DeferredRegister<DataComponentType<?>> TYPES=DeferredRegister.create(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE,"ars_fusion");
    private static final java.util.function.Supplier<DataComponentType<ItemContainerContents>> TYPE=TYPES.register("caster_fusion",()->DataComponentType.<ItemContainerContents>builder().persistent(ItemContainerContents.CODEC).networkSynchronized(ItemContainerContents.STREAM_CODEC).build());
    private static final java.util.function.Supplier<DataComponentType<Boolean>> GOETY=TYPES.register("caster_goety_staff",()->DataComponentType.<Boolean>builder().persistent(com.mojang.serialization.Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    private CasterPlatform() {}
    public static boolean goetyLoaded() {return net.neoforged.fml.ModList.get().isLoaded("goety");}
    public static boolean has(ItemStack stack) {return stack.has(TYPE.get());}
    public static ItemStack donor(ItemStack stack) {return stack.getOrDefault(TYPE.get(),ItemContainerContents.EMPTY).copyOne();}
    public static boolean goetyTraits(ItemStack stack) {return stack.getOrDefault(GOETY.get(),false);}
    public static void set(ItemStack stack,ItemStack donor,boolean goety) {stack.set(TYPE.get(),ItemContainerContents.fromItems(List.of(donor)));stack.set(GOETY.get(),goety);}
    public static boolean ironBook(ItemStack stack) {return FusionPlatform.ironLoaded()&&IronCaster.book(stack);}
    public static boolean specialDonor(ItemStack donor) {
        return FusionPlatform.ironLoaded()&&IronCaster.supports(donor)
                ||goetyLoaded()&&FusionConfig.goetyStaffFusionEnabled()&&GoetyCaster.supports(donor);
    }
    public static List<CasterBonus> bonuses(ItemStack donor,EquipmentSlot slot) {
        var result=new ArrayList<CasterBonus>();donor.forEachModifier(slot,(attribute,modifier)->{
            var id=BuiltInRegistries.ATTRIBUTE.getKey(attribute.value());
            if(id!=null)result.add(new CasterBonus(id.toString(),"hand/"+modifier.id().toString().replace(':','/'),modifier.amount(),modifier.operation().ordinal()));
        });return result;
    }
    public static void register(net.neoforged.bus.api.IEventBus bus) {
        TYPES.register(bus);net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(CasterPlatform::attributes);
    }
    private static void attributes(ItemAttributeModifierEvent event) {
        var donor=CasterFusion.donor(event.getItemStack());if(donor.isEmpty())return;
        for(var slot:List.of(EquipmentSlot.MAINHAND,EquipmentSlot.OFFHAND))for(var bonus:bonuses(donor,slot)) {
            if(!FusionConfig.allowsCasterAttribute(bonus.attribute()))continue;
            var key=ResourceLocation.tryParse(bonus.attribute());var attribute=BuiltInRegistries.ATTRIBUTE.getHolder(key).orElse(null);if(attribute==null)continue;
            var id=ResourceLocation.fromNamespaceAndPath("ars_fusion","caster/"+slot.getName()+"/"+bonus.attribute().replace(':','/')+"/"+bonus.identity());
            event.addModifier(attribute,new AttributeModifier(id,bonus.amount(),AttributeModifier.Operation.values()[bonus.operation()]),EquipmentSlotGroup.bySlot(slot));
        }
    }
}
