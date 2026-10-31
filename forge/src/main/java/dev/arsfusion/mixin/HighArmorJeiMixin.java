package dev.arsfusion.mixin;
import com.hollingsworth.arsnouveau.api.registry.PerkRegistry;
import com.hollingsworth.arsnouveau.api.enchanting_apparatus.ArmorUpgradeRecipe;
import dev.arsfusion.platform.HighArmorPlatform;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import java.util.List;
@Pseudo
@Mixin(targets="com.hollingsworth.arsnouveau.client.jei.ArmorUpgradeRecipeCategory",remap=false)
public abstract class HighArmorJeiMixin {
    @Redirect(method="setRecipe",at=@At(value="INVOKE",target="Lcom/hollingsworth/arsnouveau/api/registry/PerkRegistry;getPerkProviderItems()Ljava/util/List;"))
    private List<Item> arsFusion$supported(@Coerce Object builder,ArmorUpgradeRecipe recipe,@Coerce Object focuses) {
        return PerkRegistry.getPerkProviderItems().stream().filter(i->HighArmorPlatform.supportsTier(new ItemStack(i),recipe.tier))
                .filter(i->!(recipe instanceof dev.arsfusion.armor.HighArmorRecipe)||dev.arsfusion.armor.ArmorLayouts.managed(new ItemStack(i))).toList();
    }
}
