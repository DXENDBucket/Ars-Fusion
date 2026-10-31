package dev.arsfusion.mixin;
import com.hollingsworth.arsnouveau.api.documentation.entry.PerkDiagramEntry;
import com.hollingsworth.arsnouveau.api.registry.PerkRegistry;
import com.hollingsworth.arsnouveau.api.perk.PerkSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import java.util.List;
/** The native handbook diagram has three labelled columns; keep those original columns intact. */
@Mixin(value=PerkDiagramEntry.class,remap=false)
public abstract class NativeArmorDiagramMixin {
    @Redirect(method="drawRow",at=@At(value="INVOKE",target="Lcom/hollingsworth/arsnouveau/api/registry/PerkRegistry;getPerkProvider(Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"))
    private List<List<PerkSlot>> arsFusion$nativeColumns(ItemStack stack) {
        var rows=PerkRegistry.getPerkProvider(stack);return rows==null?null:rows.subList(0,Math.min(3,rows.size()));
    }
}
