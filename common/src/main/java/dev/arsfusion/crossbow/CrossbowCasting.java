package dev.arsfusion.crossbow;

import dev.arsfusion.bow.BowCasting;
import dev.arsfusion.platform.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

/** Loading stays native. The actual firing use is the one active transaction. */
public final class CrossbowCasting {
    private CrossbowCasting() {}
    public static InteractionResultHolder<ItemStack> use(Item item,Level world,Player actor,InteractionHand hand) {
        var stack=actor.getItemInHand(hand);
        if(!CrossbowFusion.has(stack)||world.isClientSide||EquipmentPlatform.chargedAmmo(stack).isEmpty())return item.use(world,actor,hand);
        return BowPlatform.activeUse(stack.copy(),actor,hand,()->{
            // Read loaded ammo now, never from the player's inventory after it was consumed on loading.
            var ammo=EquipmentPlatform.chargedAmmo(stack);if(ammo.isEmpty())return item.use(world,actor,hand);var tool=stack.copy();
            var prepared=ammo.stream().map(a->BowPlatform.prepare(tool,actor,hand,a)).toList();
            return BowCasting.capture(actor,i->prepared.get(Math.min(i,prepared.size()-1)),()->item.use(world,actor,hand));
        },()->item.use(world,actor,hand),InteractionResultHolder.consume(stack));
    }
}
