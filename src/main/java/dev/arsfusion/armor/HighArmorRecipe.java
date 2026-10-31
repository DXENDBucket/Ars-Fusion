package dev.arsfusion.armor;

import com.hollingsworth.arsnouveau.common.crafting.recipes.ArmorUpgradeRecipe;
import com.hollingsworth.arsnouveau.common.crafting.recipes.ApparatusRecipeInput;
import com.hollingsworth.arsnouveau.common.crafting.recipes.CheatSerializer;
import com.mojang.serialization.MapCodec;
import dev.arsfusion.platform.HighArmorPlatform;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import java.util.List;

/** Native Ars upgrading, scoped to the three native styles and their fused counterparts. */
public final class HighArmorRecipe extends ArmorUpgradeRecipe {
    public HighArmorRecipe(List<Ingredient> inputs,int sourceCost,int tier) {super(inputs,sourceCost,tier);}
    @Override public boolean doesReagentMatch(ApparatusRecipeInput input,Level level,Player player) {
        return ArmorLayouts.managed(input.catalyst())&&HighArmorPlatform.supportsTier(input.catalyst(),tier)&&super.doesReagentMatch(input,level,player);
    }
    @Override public RecipeSerializer<?> getSerializer() {return HighArmorPlatform.UPGRADE_SERIALIZER.get();}
    public static final class Serializer implements RecipeSerializer<HighArmorRecipe> {
        public static final MapCodec<HighArmorRecipe> CODEC=ArmorUpgradeRecipe.Serializer.CODEC.xmap(
                r->new HighArmorRecipe(r.pedestalItems(),r.sourceCost(),r.tier),r->r);
        private static final StreamCodec<RegistryFriendlyByteBuf,HighArmorRecipe> STREAM=CheatSerializer.create(CODEC);
        @Override public MapCodec<HighArmorRecipe> codec() {return CODEC;}
        @Override public StreamCodec<RegistryFriendlyByteBuf,HighArmorRecipe> streamCodec() {return STREAM;}
    }
}
