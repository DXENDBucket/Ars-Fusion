package dev.arsfusion.platform;
import com.hollingsworth.arsnouveau.api.item.ICasterTool;
import com.hollingsworth.arsnouveau.api.spell.ISpellCasterProvider;
import com.hollingsworth.arsnouveau.api.spell.SpellCaster;
import dev.arsfusion.sword.NativeSwordCasting;
import dev.arsfusion.sword.SwordFusion;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
public final class SwordPlatform {
    private static final String KEY="ars_fusion:sword_fusion";
    private SwordPlatform() {}
    public static boolean has(ItemStack stack) {return stack.hasTag()&&stack.getTag().getBoolean(KEY);}
    public static void mark(ItemStack stack) {stack.getOrCreateTag().putBoolean(KEY,true);}
    public static boolean existingCaster(ItemStack stack) {
        return stack.getItem() instanceof ISpellCasterProvider||stack.getItem() instanceof ICasterTool
                ||(stack.hasTag()&&stack.getTag().contains(new SpellCaster(new net.minecraft.nbt.CompoundTag()).getTagID().toString()));
    }
    public static void copySpell(ItemStack donor,ItemStack result) {
        var from=new SpellCaster(donor);var to=new SpellCaster(result);
        to.setSpell(from.getSpell());to.setSpellName(from.getSpellName());to.setFlavorText(from.getFlavorText());
        to.setColor(from.getColor());to.setSound(from.getCurrentSound());to.setSpellHidden(from.isSpellHidden());to.setHiddenRecipe(from.getHiddenRecipe());
    }
    public static void tooltip(ItemTooltipEvent event) {
        if(SwordFusion.has(event.getItemStack()))NativeSwordCasting.sword().getInformation(event.getItemStack(),event.getEntity()==null?null:event.getEntity().level(),event.getToolTip(),event.getFlags());
    }
}
