package dev.arsfusion.platform;

import com.hollingsworth.arsnouveau.api.documentation.DocAssets;
import com.hollingsworth.arsnouveau.api.perk.PerkSlot;
import com.hollingsworth.arsnouveau.api.registry.PerkRegistry;
import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import dev.arsfusion.armor.ArmorLayouts;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.ArrayList;
import java.util.List;

public final class HighArmorPlatform {
    private static final DeferredRegister.Items ITEMS=DeferredRegister.createItems("ars_fusion");
    private static final DeferredRegister<net.minecraft.world.item.crafting.RecipeSerializer<?>> SERIALIZERS=DeferredRegister.create(net.minecraft.core.registries.Registries.RECIPE_SERIALIZER,"ars_fusion");
    public static final java.util.function.Supplier<net.minecraft.world.item.crafting.RecipeSerializer<dev.arsfusion.armor.HighArmorRecipe>> UPGRADE_SERIALIZER=SERIALIZERS.register("armor_upgrade",dev.arsfusion.armor.HighArmorRecipe.Serializer::new);
    public static final java.util.function.Supplier<Item> WILDEN_ESSENCE=ITEMS.register("wilden_essence",()->new Item(new Item.Properties()));
    public static final java.util.function.Supplier<Item> NETHER_STAR_FRAGMENT=ITEMS.register("nether_star_fragment",()->new Item(new Item.Properties()));
    public static final PerkSlot FOUR=slot("four",4),FIVE=slot("five",5);
    private static PerkSlot slot(String name,int value) {
        return new PerkSlot(ResourceLocation.fromNamespaceAndPath("ars_fusion",name),value,
                new DocAssets.BlitInfo(ResourceLocation.fromNamespaceAndPath("ars_fusion","textures/gui/thread_tier_"+value+".png"),7,7));
    }
    private HighArmorPlatform() { }
    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        SERIALIZERS.register(bus);
        bus.addListener((net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent event)->event.enqueueWork(HighArmorPlatform::install));
        bus.addListener((net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent event)->{
            if(event.getTabKey()==CreativeModeTabs.INGREDIENTS) {event.accept(WILDEN_ESSENCE.get());event.accept(NETHER_STAR_FRAGMENT.get());}
        });
    }
    public static void install() {
        PerkSlot.PERK_SLOTS.put(FOUR.id(),FOUR);PerkSlot.PERK_SLOTS.put(FIVE.id(),FIVE);
        for(String style:List.of("sorcerer","arcanist","battlemage"))for(String part:List.of("hood","robes","leggings","boots")) {
            var item=BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("ars_nouveau",style+"_"+part));
            var original=PerkRegistry.getPerkProvider(item);if(original==null||original.size()<3)continue;
            var extended=new ArrayList<>(original);
            while(extended.size()<5)extended.add(ArmorLayouts.at(style,part,extended.size()+1).stream().map(HighArmorPlatform::slot).toList());
            PerkRegistry.registerPerkProvider(item,List.copyOf(extended));
        }
    }
    private static PerkSlot slot(int value) {return switch(value){case 1->PerkSlot.ONE;case 2->PerkSlot.TWO;case 3->PerkSlot.THREE;case 4->FOUR;case 5->FIVE;default->throw new IllegalArgumentException();};}
    public static boolean supportsTier(ItemStack stack,int nativeTier) {
        var provider=PerkRegistry.getPerkProvider(stack);return nativeTier>=0&&provider!=null&&provider.size()>nativeTier;
    }
    public static int slotCount(ItemStack stack) {
        var holder=PerkUtil.getPerkHolder(stack);return holder==null?0:holder.getSlotsForTier(stack).size();
    }
}
