package dev.arsfusion.sword;

import com.hollingsworth.arsnouveau.common.items.EnchantersSword;
import com.hollingsworth.arsnouveau.setup.registry.ItemsRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Invoke the real native sword path, including any optional Conflux hooks, using the real tool. */
public final class NativeSwordCasting {
    private static final ThreadLocal<ItemStack> DELEGATING=new ThreadLocal<>();
    private NativeSwordCasting() {}
    public static EnchantersSword sword() {return (EnchantersSword)ItemsRegistry.ENCHANTERS_SWORD.get();}
    public static boolean delegating(ItemStack stack) {return DELEGATING.get()==stack;}
    public static void onHit(ItemStack stack,LivingEntity target,LivingEntity attacker) {
        if(!SwordFusion.has(stack)||attacker.level().isClientSide||DELEGATING.get()!=null)return;
        DELEGATING.set(stack);
        try {sword().hurtEnemy(stack,target,attacker);}
        finally {DELEGATING.remove();}
    }
    /** Scoped to the scribe block's recognition only; the actual stack and item identity never change. */
    public static Item scribeItem(ItemStack stack) {return SwordFusion.has(stack)?sword():stack.getItem();}
}
