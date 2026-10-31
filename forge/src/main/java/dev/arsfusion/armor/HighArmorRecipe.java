package dev.arsfusion.armor;

import com.hollingsworth.arsnouveau.api.enchanting_apparatus.ArmorUpgradeRecipe;
import com.hollingsworth.arsnouveau.common.block.tile.EnchantingApparatusTile;
import com.google.gson.JsonObject;
import dev.arsfusion.platform.HighArmorPlatform;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import java.util.ArrayList;
import java.util.List;

public final class HighArmorRecipe extends ArmorUpgradeRecipe {
    public HighArmorRecipe(ResourceLocation id,List<Ingredient> inputs,int sourceCost,int tier) {super(id,inputs,sourceCost,tier);}
    @Override public boolean isMatch(List<ItemStack> inputs,ItemStack reagent,EnchantingApparatusTile tile,Player player) {
        return ArmorLayouts.managed(reagent)&&HighArmorPlatform.supportsTier(reagent,tier)&&super.isMatch(inputs,reagent,tile,player);
    }
    @Override public RecipeSerializer<?> getSerializer() {return HighArmorPlatform.UPGRADE_SERIALIZER.get();}
    public static final class Serializer implements RecipeSerializer<HighArmorRecipe> {
        @Override public HighArmorRecipe fromJson(ResourceLocation id,JsonObject json) {
            var inputs=new ArrayList<Ingredient>();for(var value:GsonHelper.getAsJsonArray(json,"pedestalItems"))inputs.add(Ingredient.fromJson(value));
            return new HighArmorRecipe(id,inputs,GsonHelper.getAsInt(json,"sourceCost"),GsonHelper.getAsInt(json,"tier"));
        }
        @Override public HighArmorRecipe fromNetwork(ResourceLocation id,FriendlyByteBuf buf) {
            int count=buf.readVarInt();if(count<0||count>64)throw new IllegalArgumentException("Invalid pedestal ingredient count");
            var inputs=new ArrayList<Ingredient>();for(int i=0;i<count;i++)inputs.add(Ingredient.fromNetwork(buf));
            return new HighArmorRecipe(id,inputs,buf.readVarInt(),buf.readVarInt());
        }
        @Override public void toNetwork(FriendlyByteBuf buf,HighArmorRecipe recipe) {
            buf.writeVarInt(recipe.pedestalItems.size());for(var input:recipe.pedestalItems)input.toNetwork(buf);
            buf.writeVarInt(recipe.sourceCost);buf.writeVarInt(recipe.tier);
        }
    }
}
