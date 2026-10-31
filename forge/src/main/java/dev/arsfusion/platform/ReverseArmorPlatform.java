package dev.arsfusion.platform;

import com.hollingsworth.arsnouveau.api.registry.PerkRegistry;
import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.api.perk.ArmorPerkHolder;
import com.hollingsworth.arsnouveau.api.perk.IPerkHolder;
import com.hollingsworth.arsnouveau.api.perk.ITickablePerk;
import com.hollingsworth.arsnouveau.api.perk.PerkSlot;
import com.hollingsworth.arsnouveau.api.perk.PerkAttributes;
import com.hollingsworth.arsnouveau.common.perk.RepairingPerk;
import dev.arsfusion.armor.ArmorFusion;
import dev.arsfusion.armor.ReverseArmorFusion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import java.util.List;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

public final class ReverseArmorPlatform {
    private static final String KEY="ars_fusion:reverse_armor_fusion";
    private ReverseArmorPlatform() { }
    public static boolean has(ItemStack stack) {return stack.hasTag()&&stack.getTag().contains(KEY,10);}
    public static ItemStack donor(ItemStack stack) {return has(stack)?ItemStack.of(stack.getTag().getCompound(KEY)):ItemStack.EMPTY;}
    public static boolean hasNativePerks(ItemStack stack) {
        return PerkRegistry.getPerkProvider(stack.getItem())!=null || PerkUtil.getPerkHolder(stack)!=null;
    }
    public static boolean validDonor(ItemStack donor) {
        var holder=PerkUtil.getPerkHolder(donor);
        if(!(holder instanceof ArmorPerkHolder)||holder.getTier()<0)return false;
        try {return !holder.getSlotsForTier().isEmpty();}catch(IndexOutOfBoundsException invalid){return false;}
    }
    public static void transfer(ItemStack result,ItemStack donor) {
        var snapshot=donor.copy();snapshot.setCount(1);
        result.getOrCreateTag().put(KEY,snapshot.save(new CompoundTag()));
        // Copy only the native thread-layer tag, never the donor's enchantments or other item data.
        var holder=PerkUtil.getPerkHolder(donor);
        var tag=new CompoundTag();((ArmorPerkHolder)holder).writeToNBT(tag);
        result.getOrCreateTag().put("an_stack_perks",tag);
    }
    public static IPerkHolder<ItemStack> holder(ItemStack stack) {
        var template=ReverseArmorFusion.donor(stack);
        if(template.isEmpty()||PerkRegistry.getPerkProvider(template.getItem())==null)return null;
        return new ArmorPerkHolder(stack,List.of()) {
            @Override public List<PerkSlot> getSlotsForTier() {
                var probe=template.copy();var nativeHolder=PerkUtil.getPerkHolder(probe);
                if(!(nativeHolder instanceof ArmorPerkHolder))return List.of();
                nativeHolder.setTier(getTier());
                try {return nativeHolder.getSlotsForTier();}catch(IndexOutOfBoundsException invalid){return List.of();}
            }
        };
    }
    private static ItemStack manaTemplate(ItemStack stack) {
        var probe=ReverseArmorFusion.donor(stack);var live=PerkUtil.getPerkHolder(stack);
        if(probe.isEmpty()||live==null)return ItemStack.EMPTY;
        var original=PerkUtil.getPerkHolder(probe);if(original==null)return ItemStack.EMPTY;
        original.setTier(live.getTier());original.setPerks(List.of());
        return probe;
    }
    public static void register(IEventBus bus) {
        MinecraftForge.EVENT_BUS.addListener(net.minecraftforge.eventbus.api.EventPriority.LOWEST,(net.minecraftforge.event.ItemAttributeModifierEvent event)->{
            var stack=event.getItemStack();
            if(!ReverseArmorFusion.has(stack)||event.getSlotType()!=ArmorFusion.slot(stack))return;
            var holder=PerkUtil.getPerkHolder(stack);if(holder==null)return;
            var defense=ReverseArmorFusion.defense(stack);
            if(defense!=null) {
                event.removeAttribute(Attributes.ARMOR);
                event.removeAttribute(Attributes.ARMOR_TOUGHNESS);
                String slot=event.getSlotType().getName();
                String armorId="ars_fusion:reverse_armor/"+slot,toughnessId="ars_fusion:reverse_toughness/"+slot;
                event.addModifier(Attributes.ARMOR,new AttributeModifier(UUID.nameUUIDFromBytes(armorId.getBytes(StandardCharsets.UTF_8)),armorId,defense.armor(),AttributeModifier.Operation.ADDITION));
                event.addModifier(Attributes.ARMOR_TOUGHNESS,new AttributeModifier(UUID.nameUUIDFromBytes(toughnessId.getBytes(StandardCharsets.UTF_8)),toughnessId,defense.toughness(),AttributeModifier.Operation.ADDITION));
            }
            if(dev.arsfusion.config.FusionConfig.reverseTransfersArsMana()) {
                var probe=manaTemplate(stack);
                if(!probe.isEmpty())probe.getAttributeModifiers(event.getSlotType()).forEach((attribute,modifier)->{
                    if(attribute!=PerkAttributes.MAX_MANA.get()&&attribute!=PerkAttributes.MANA_REGEN_BONUS.get())return;
                    String owned="ars_fusion:reverse_mana/"+event.getSlotType().getName()+"/"+modifier.getId();
                    event.addModifier(attribute,new AttributeModifier(UUID.nameUUIDFromBytes(owned.getBytes(StandardCharsets.UTF_8)),owned,modifier.getAmount(),modifier.getOperation()));
                });
            }
            for(var instance:holder.getPerkInstances())instance.getPerk()
                    .getModifiers(event.getSlotType(),stack,instance.getSlot().value).forEach(event::addModifier);
        });
        MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.entity.player.ItemTooltipEvent event)->{
            var stack=event.getItemStack();if(!ReverseArmorFusion.has(stack))return;
            var holder=PerkUtil.getPerkHolder(stack);if(holder==null)return;
            event.getToolTip().add(Component.translatable("tooltip.ars_fusion.thread_tier",holder.getTier()+1)
                    .withStyle(net.minecraft.ChatFormatting.GOLD));
            holder.appendPerkTooltip(event.getToolTip(),stack);
        });
        MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.TickEvent.PlayerTickEvent event)->{
            var player=event.player;
            if(event.phase!=net.minecraftforge.event.TickEvent.Phase.END||player.level().isClientSide)return;
            for(var stack:player.getArmorSlots()) {
                if(!ReverseArmorFusion.has(stack))continue;
                var holder=PerkUtil.getPerkHolder(stack);if(holder==null)continue;
                if(player.level().getGameTime()%200==0 && PerkUtil.countForPerk(RepairingPerk.INSTANCE,player)>0)
                    RepairingPerk.attemptRepair(stack,player);
                for(var instance:holder.getPerkInstances())if(instance.getPerk() instanceof ITickablePerk tickable)
                    tickable.tick(stack,player.level(),player,instance);
            }
        });
    }
}
