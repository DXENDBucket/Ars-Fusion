package dev.arsfusion.platform;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.*;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.*;

public final class EquipmentPlatform {
    private static final DeferredRegister<DataComponentType<?>> TYPES=DeferredRegister.create(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE,"ars_fusion");
    private static final java.util.function.Supplier<DataComponentType<Boolean>> CROSSBOW=TYPES.register("crossbow_fusion",()->DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    private static final java.util.function.Supplier<DataComponentType<Boolean>> SHIELD=TYPES.register("shield_fusion",()->DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    private EquipmentPlatform() {}
    public static void register(net.neoforged.bus.api.IEventBus bus) {TYPES.register(bus);}
    public static boolean crossbow(ItemStack stack) {return stack.getOrDefault(CROSSBOW.get(),false);}
    public static boolean shield(ItemStack stack) {return stack.getOrDefault(SHIELD.get(),false);}
    public static void markCrossbow(ItemStack stack) {stack.set(CROSSBOW.get(),true);}
    public static void markShield(ItemStack stack) {stack.set(SHIELD.get(),true);}
    public static boolean canBlock(ItemStack stack) {return stack.canPerformAction(net.neoforged.neoforge.common.ItemAbilities.SHIELD_BLOCK);}
    public static List<ItemStack> chargedAmmo(ItemStack stack) {return stack.getOrDefault(DataComponents.CHARGED_PROJECTILES,ChargedProjectiles.EMPTY).getItems().stream().map(ItemStack::copy).toList();}
}
