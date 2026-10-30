package dev.arsfusion.compat;

import com.Polarice3.Goety.api.items.magic.IWand;
import dev.arsfusion.caster.CasterFusion;
import dev.arsfusion.platform.CasterPlatform;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Optional native staff identity bridge, independent of Goety & Nouveau. No focus or native use loop. */
public final class GoetyCaster {
    private GoetyCaster() {}
    public static boolean supports(ItemStack stack) {return stack.getItem() instanceof IWand;}
    public static ItemStack view(ItemStack stack) {
        if(!CasterFusion.has(stack)||!CasterPlatform.goetyTraits(stack))return ItemStack.EMPTY;
        var donor=CasterFusion.donor(stack);
        // Intrinsic staff traits use its native item/type. Focus contents and enchantments are not transplanted.
        return supports(donor)?donor.getItem().getDefaultInstance():ItemStack.EMPTY;
    }
    /** Preserve native main-hand-first priority, including real staffs in either hand. */
    public static ItemStack selected(LivingEntity actor) {
        if(actor==null)return ItemStack.EMPTY;
        for(var hand:InteractionHand.values()) {
            var held=actor.getItemInHand(hand);
            if(supports(held))return ItemStack.EMPTY; // Keep the original native lookup result.
            var view=view(held);if(!view.isEmpty())return view;
        }
        return ItemStack.EMPTY;
    }
}
