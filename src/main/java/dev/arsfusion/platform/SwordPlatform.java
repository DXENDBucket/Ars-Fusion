package dev.arsfusion.platform;
import com.hollingsworth.arsnouveau.api.registry.SpellCasterRegistry;
import com.hollingsworth.arsnouveau.api.spell.ItemCasterProvider;
import com.hollingsworth.arsnouveau.api.spell.SpellCaster;
import dev.arsfusion.sword.NativeSwordCasting;
import dev.arsfusion.sword.SwordFusion;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
public final class SwordPlatform {
    private static final DeferredRegister<DataComponentType<?>> TYPES=DeferredRegister.create(net.minecraft.core.registries.Registries.DATA_COMPONENT_TYPE,"ars_fusion");
    private static final java.util.function.Supplier<DataComponentType<Boolean>> TYPE=TYPES.register("sword_fusion",()->DataComponentType.<Boolean>builder().persistent(com.mojang.serialization.Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build());
    private SwordPlatform() {}
    public static void register(net.neoforged.bus.api.IEventBus bus) {TYPES.register(bus);}
    public static boolean has(ItemStack stack) {return stack.getOrDefault(TYPE.get(),false);}
    public static void mark(ItemStack stack) {stack.set(TYPE.get(),true);}
    public static boolean existingCaster(ItemStack stack) {
        return stack.getItem() instanceof ItemCasterProvider||SpellCasterRegistry.hasCaster(stack)||SpellCasterRegistry.from(stack)!=null;
    }
    public static void copySpell(ItemStack donor,ItemStack result) {
        var from=SpellCasterRegistry.from(donor);if(from==null)from=new SpellCaster(1);
        new SpellCaster(1).setSpell(from.getSpell()).setSpellName(from.getSpellName()).setFlavorText(from.getFlavorText())
                .setColor(from.getColor()).setSound(from.getCurrentSound()).setHidden(from.isSpellHidden()).setHiddenRecipe(from.getHiddenRecipe()).saveToStack(result);
    }
    public static void tooltip(ItemTooltipEvent event) {
        var stack=event.getItemStack();
        if(dev.arsfusion.casting.FusedCasting.has(stack))((com.hollingsworth.arsnouveau.api.item.ICasterTool)dev.arsfusion.casting.FusedCasting.nativeItem(stack))
                .getInformation(stack,event.getContext(),event.getToolTip(),event.getFlags());
        if(dev.arsfusion.shield.ShieldFusion.has(stack))event.getToolTip().add(net.minecraft.network.chat.Component.translatable("tooltip.ars_fusion.shield_utility"));
    }
}
