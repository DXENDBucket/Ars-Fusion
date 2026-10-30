package dev.arsfusion.platform;

import dev.arsfusion.caster.*;
import dev.arsfusion.compat.*;
import dev.arsfusion.config.FusionConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import java.util.*;

public final class CasterPlatform {
    private static final String KEY="ars_fusion:caster_fusion";
    private CasterPlatform() {}
    public static boolean goetyLoaded() {return net.minecraftforge.fml.ModList.get().isLoaded("goety");}
    public static boolean has(ItemStack stack) {return stack.hasTag()&&stack.getTag().contains(KEY,Tag.TAG_COMPOUND);}
    public static ItemStack donor(ItemStack stack) {return has(stack)?ItemStack.of(stack.getTag().getCompound(KEY).getCompound("Donor")):ItemStack.EMPTY;}
    public static boolean goetyTraits(ItemStack stack) {return has(stack)&&stack.getTag().getCompound(KEY).getBoolean("GoetyStaff");}
    public static void set(ItemStack stack,ItemStack donor,boolean goety) {var data=new CompoundTag();data.put("Donor",donor.save(new CompoundTag()));data.putBoolean("GoetyStaff",goety);stack.getOrCreateTag().put(KEY,data);}
    public static boolean ironBook(ItemStack stack) {return FusionPlatform.ironLoaded()&&IronCaster.book(stack);}
    public static boolean specialDonor(ItemStack donor) {
        return FusionPlatform.ironLoaded()&&IronCaster.supports(donor)
                ||goetyLoaded()&&FusionConfig.goetyStaffFusionEnabled()&&GoetyCaster.supports(donor);
    }
    public static List<CasterBonus> bonuses(ItemStack donor,EquipmentSlot slot) {
        var result=new ArrayList<CasterBonus>();donor.getAttributeModifiers(slot).forEach((attribute,modifier)->{
            var id=BuiltInRegistries.ATTRIBUTE.getKey(attribute);
            if(id!=null)result.add(new CasterBonus(id.toString(),"hand/"+modifier.getId(),modifier.getAmount(),modifier.getOperation().ordinal()));
        });return result;
    }
    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(CasterPlatform::attributes);}
    private static void attributes(ItemAttributeModifierEvent event) {
        var donor=CasterFusion.donor(event.getItemStack());var slot=event.getSlotType();
        if(donor.isEmpty()||slot!=EquipmentSlot.MAINHAND&&slot!=EquipmentSlot.OFFHAND)return;
        for(var bonus:bonuses(donor,slot)) {
            if(!FusionConfig.allowsCasterAttribute(bonus.attribute()))continue;
            var attribute=BuiltInRegistries.ATTRIBUTE.get(ResourceLocation.tryParse(bonus.attribute()));if(attribute==null)continue;
            String key="ars_fusion:caster/"+slot.getName()+"/"+bonus.attribute()+"/"+bonus.identity();
            event.addModifier(attribute,new AttributeModifier(UUID.nameUUIDFromBytes(key.getBytes(java.nio.charset.StandardCharsets.UTF_8)),key,bonus.amount(),AttributeModifier.Operation.values()[bonus.operation()]));
        }
    }
}
