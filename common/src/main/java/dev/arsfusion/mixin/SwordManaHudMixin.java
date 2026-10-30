package dev.arsfusion.mixin;
import com.hollingsworth.arsnouveau.client.gui.GuiManaHUD;
import dev.arsfusion.sword.SwordFusion;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(value=GuiManaHUD.class,remap=false)
public abstract class SwordManaHudMixin {
    @Inject(method="shouldDisplayBar",at=@At("RETURN"),cancellable=true)
    private static void arsFusion$heldTool(CallbackInfoReturnable<Boolean> cir) {
        var mc=Minecraft.getInstance();
        if(!cir.getReturnValue()&&!mc.options.hideGui&&mc.player!=null
                &&(SwordFusion.has(mc.player.getMainHandItem())||SwordFusion.has(mc.player.getOffhandItem())||dev.arsfusion.bow.BowFusion.has(mc.player.getMainHandItem())||dev.arsfusion.bow.BowFusion.has(mc.player.getOffhandItem())))cir.setReturnValue(true);
    }
}
