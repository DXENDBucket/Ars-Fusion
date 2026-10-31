package dev.arsfusion.platform;

import com.hollingsworth.arsnouveau.api.perk.ArmorPerkHolder;
import com.hollingsworth.arsnouveau.api.perk.PerkSlot;
import com.hollingsworth.arsnouveau.api.registry.PerkRegistry;
import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import dev.arsfusion.armor.ArmorLayouts;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class HighArmorPlatform {
    private static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,"ars_fusion");
    private static final DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> SERIALIZERS=DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS,"ars_fusion");
    public static final java.util.function.Supplier<net.minecraft.world.item.crafting.RecipeSerializer<dev.arsfusion.armor.HighArmorRecipe>> UPGRADE_SERIALIZER=SERIALIZERS.register("armor_upgrade",dev.arsfusion.armor.HighArmorRecipe.Serializer::new);
    public static final java.util.function.Supplier<Item> WILDEN_ESSENCE=ITEMS.register("wilden_essence",()->new Item(new Item.Properties()));
    public static final java.util.function.Supplier<Item> NETHER_STAR_FRAGMENT=ITEMS.register("nether_star_fragment",()->new Item(new Item.Properties()));
    public static final PerkSlot FOUR=new PerkSlot(new ResourceLocation("ars_fusion","four"),4),FIVE=new PerkSlot(new ResourceLocation("ars_fusion","five"),5);
    private HighArmorPlatform() { }
    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        SERIALIZERS.register(bus);
        bus.addListener((net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent event)->event.enqueueWork(HighArmorPlatform::install));
        bus.addListener((net.minecraftforge.event.BuildCreativeModeTabContentsEvent event)->{
            if(event.getTabKey()==CreativeModeTabs.INGREDIENTS) {event.accept(WILDEN_ESSENCE.get());event.accept(NETHER_STAR_FRAGMENT.get());}
        });
    }
    public static void install() {
        PerkSlot.PERK_SLOTS.put(FOUR.id,FOUR);PerkSlot.PERK_SLOTS.put(FIVE.id,FIVE);
        for(String style:List.of("sorcerer","arcanist","battlemage"))for(String part:List.of("hood","robes","leggings","boots")) {
            var item=BuiltInRegistries.ITEM.get(new ResourceLocation("ars_nouveau",style+"_"+part));
            var original=PerkRegistry.getPerkProvider(item);if(original==null)continue;
            PerkRegistry.registerPerkProvider(item,stack->new ArmorPerkHolder(stack,List.of()) {
                @Override public List<PerkSlot> getSlotsForTier() {
                    // Existing native/other-addon tiers keep their original provider semantics.
                    try {return original.getPerkHolder(stack.copy()).getSlotsForTier();}
                    catch(IndexOutOfBoundsException outsideNativeTiers) {
                        int tier=getTier()+1;
                        if(tier!=4&&tier!=5)throw outsideNativeTiers;
                        var slots=new ArrayList<>(ArmorLayouts.at(style,part,tier).stream().map(HighArmorPlatform::slot).toList());
                        slots.sort(Comparator.comparingInt(s->-s.value));return slots;
                    }
                }
            });
        }
    }
    private static PerkSlot slot(int value) {return switch(value){case 1->PerkSlot.ONE;case 2->PerkSlot.TWO;case 3->PerkSlot.THREE;case 4->FOUR;case 5->FIVE;default->throw new IllegalArgumentException();};}
    public static boolean supportsTier(ItemStack stack,int nativeTier) {
        if(nativeTier<0)return false;
        var probe=stack.copy();var holder=PerkUtil.getPerkHolder(probe);if(holder==null)return false;
        holder.setTier(nativeTier);
        try{return !holder.getSlotsForTier().isEmpty();}catch(IndexOutOfBoundsException invalid){return false;}
    }
    public static int slotCount(ItemStack stack) {
        var holder=PerkUtil.getPerkHolder(stack);return holder==null?0:holder.getSlotsForTier().size();
    }
}
