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
    public static void register(net.minecraftforge.eventbus.api.IEventBus bus) {net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(SwordPlatform::attributes);}
    private static void attributes(net.minecraftforge.event.ItemAttributeModifierEvent event) {
        var slot=event.getSlotType();
        if(!SwordFusion.has(event.getItemStack())||slot!=net.minecraft.world.entity.EquipmentSlot.MAINHAND)return;
        var attribute=com.hollingsworth.arsnouveau.api.perk.PerkAttributes.SPELL_DAMAGE_BONUS.get();
        // Ars 4 has no native sword modifier here; retain its implicit Amplify rather than inventing an extra +4.
        for(var modifier:NativeSwordCasting.sword().getDefaultInstance().getAttributeModifiers(slot).get(attribute)) {
            String key="ars_fusion:enchanters_spell_damage/"+modifier.getId();
            event.addModifier(attribute,new net.minecraft.world.entity.ai.attributes.AttributeModifier(java.util.UUID.nameUUIDFromBytes(key.getBytes(java.nio.charset.StandardCharsets.UTF_8)),key,modifier.getAmount(),modifier.getOperation()));
        }
    }
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
        var stack=event.getItemStack();
        if(dev.arsfusion.casting.FusedCasting.has(stack))((com.hollingsworth.arsnouveau.api.item.ICasterTool)dev.arsfusion.casting.FusedCasting.nativeItem(stack))
                .getInformation(stack,event.getEntity()==null?null:event.getEntity().level(),event.getToolTip(),event.getFlags());
        if(dev.arsfusion.shield.ShieldFusion.has(stack))event.getToolTip().add(net.minecraft.network.chat.Component.translatable("tooltip.ars_fusion.shield_utility"));
    }
}
