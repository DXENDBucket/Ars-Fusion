package dev.arsfusion.armor;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/** Final five-slot layouts. Native tiers 1-3 remain owned by Ars; only tiers 4/5 use prefixes. */
public final class ArmorLayouts {
    private ArmorLayouts() { }
    public static boolean managed(ItemStack stack) {
        var source=ReverseArmorFusion.has(stack)?ReverseArmorFusion.donor(stack):stack;
        var id=BuiltInRegistries.ITEM.getKey(source.getItem());
        return id.getNamespace().equals("ars_nouveau")&&id.getPath().matches("(sorcerer|arcanist|battlemage)_(hood|robes|leggings|boots)");
    }
    public static List<Integer> at(String style,String part,int tier) {
        if(tier!=4&&tier!=5)throw new IllegalArgumentException("Expected armor tier 4 or 5");
        boolean body=part.equals("robes")||part.equals("leggings");
        List<Integer> layout=switch(style) {
            case "sorcerer" -> body?List.of(2,2,3,4,5):List.of(1,2,3,4,5);
            case "arcanist" -> switch(part) {
                case "hood" -> List.of(1,1,3,3,4);
                case "boots" -> List.of(1,2,2,3,4);
                default -> List.of(1,2,3,4,5);
            };
            case "battlemage" -> switch(part) {
                case "robes" -> List.of(1,1,3,4,5);
                case "leggings" -> List.of(1,1,3,3,4);
                default -> List.of(1,1,2,2,3);
            };
            default -> throw new IllegalArgumentException("Unknown Ars armor style: "+style);
        };
        return layout.subList(0,tier);
    }
}
