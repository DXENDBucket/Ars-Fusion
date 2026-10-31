package dev.arsfusion.platform;

import com.hollingsworth.arsnouveau.api.registry.PerkRegistry;
import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.hollingsworth.arsnouveau.api.perk.PerkSlot;
import com.hollingsworth.arsnouveau.api.perk.ITickablePerk;
import com.hollingsworth.arsnouveau.common.perk.RepairingPerk;
import com.hollingsworth.arsnouveau.setup.registry.DataComponentRegistry;
import dev.arsfusion.armor.ArmorFusion;
import dev.arsfusion.armor.ReverseArmorFusion;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.List;

public final class ReverseArmorPlatform {
    private static final DeferredRegister<DataComponentType<?>> TYPES=DeferredRegister.create(
            net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE,"ars_fusion");
    private static final java.util.function.Supplier<DataComponentType<ItemContainerContents>> TYPE=TYPES.register("reverse_armor_fusion",()->
            DataComponentType.<ItemContainerContents>builder().persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC).build());
    private ReverseArmorPlatform() { }
    public static boolean has(ItemStack stack) {return stack.has(TYPE.get());}
    public static ItemStack donor(ItemStack stack) {return stack.getOrDefault(TYPE.get(),ItemContainerContents.EMPTY).copyOne();}
    public static boolean hasNativePerks(ItemStack stack) {
        return PerkUtil.getPerkHolder(stack)!=null || PerkRegistry.getPerkProvider(stack.getItem())!=null;
    }
    public static boolean validDonor(ItemStack donor) {
        var holder=PerkUtil.getPerkHolder(donor);var slots=PerkRegistry.getPerkProvider(donor);
        return holder!=null && slots!=null && holder.getTier()>=0 && holder.getTier()<slots.size();
    }
    public static void transfer(ItemStack result,ItemStack donor) {
        var snapshot=donor.copy();snapshot.setCount(1);
        result.set(TYPE.get(),ItemContainerContents.fromItems(List.of(snapshot)));
        result.set(DataComponentRegistry.ARMOR_PERKS,donor.get(DataComponentRegistry.ARMOR_PERKS));
    }
    public static List<List<PerkSlot>> slots(ItemStack stack) {
        var donor=ReverseArmorFusion.donor(stack);
        var provider=donor.isEmpty()?null:PerkRegistry.getPerkProvider(donor.getItem());
        return provider==null?List.of():provider;
    }
    public static void register(IEventBus bus) {
        TYPES.register(bus);
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.ItemAttributeModifierEvent event)->{
            var stack=event.getItemStack();if(!ReverseArmorFusion.has(stack))return;
            var holder=PerkUtil.getPerkHolder(stack);if(holder==null)return;
            var group=EquipmentSlotGroup.bySlot(ArmorFusion.slot(stack));
            var added=ItemAttributeModifiers.EMPTY;
            for(var instance:holder.getPerkInstances(stack)) added=instance.getPerk()
                    .applyAttributeModifiers(added,stack,instance.getSlot().value(),group);
            for(var entry:added.modifiers())event.addModifier(entry.attribute(),entry.modifier(),entry.slot());
        });
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event)->{
            var stack=event.getItemStack();if(!ReverseArmorFusion.has(stack))return;
            var holder=PerkUtil.getPerkHolder(stack);if(holder==null)return;
            event.getToolTip().add(Component.translatable("tooltip.ars_fusion.thread_tier",holder.getTier()+1)
                    .withStyle(net.minecraft.ChatFormatting.GOLD));
            holder.appendPerkTooltip(event.getToolTip(),stack);
        });
        NeoForge.EVENT_BUS.addListener((net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event)->{
            var player=event.getEntity();if(player.level().isClientSide)return;
            for(var stack:player.getArmorSlots()) {
                if(!ReverseArmorFusion.has(stack))continue;
                var holder=PerkUtil.getPerkHolder(stack);if(holder==null)continue;
                if(player.level().getGameTime()%200==0 && PerkUtil.countForPerk(RepairingPerk.INSTANCE,player)>0)
                    RepairingPerk.attemptRepair(stack,player);
                for(var instance:holder.getPerkInstances(stack))if(instance.getPerk() instanceof ITickablePerk tickable)
                    tickable.tick(stack,player.level(),player,instance);
            }
        });
    }
}
