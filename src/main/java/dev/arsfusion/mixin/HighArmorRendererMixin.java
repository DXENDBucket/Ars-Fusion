package dev.arsfusion.mixin;

import com.hollingsworth.arsnouveau.client.renderer.tile.AlterationTableRenderer;
import com.hollingsworth.arsnouveau.common.block.tile.AlterationTile;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.arsfusion.platform.HighArmorPlatform;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keep Ars rendering, transforms and assets; fit extra rows inside its existing three-row display. */
@Mixin(value=AlterationTableRenderer.class,remap=false)
public abstract class HighArmorRendererMixin {
    @Unique private int arsFusion$rows=3;
    @Inject(method="renderPerks",at=@At("HEAD"))
    private void arsFusion$perkRows(AlterationTile tile,PoseStack pose,MultiBufferSource buffers,int light,int overlay,CallbackInfo ci) {
        arsFusion$rows=Math.max(3,HighArmorPlatform.slotCount(tile.armorStack));
    }
    @Inject(method="renderSlate",at=@At("HEAD"))
    private void arsFusion$slateRows(AlterationTile tile,PoseStack pose,MultiBufferSource buffers,int light,CallbackInfo ci) {
        arsFusion$rows=Math.max(3,HighArmorPlatform.slotCount(tile.armorStack));
    }
    @ModifyConstant(method="renderPerks",constant=@Constant(intValue=3))
    private int arsFusion$perkCount(int value) {return arsFusion$rows;}
    // The first 3 in renderSlate is the existing bone-name array length, not the display limit.
    @ModifyConstant(method="renderSlate",constant=@Constant(intValue=3,ordinal=1))
    private int arsFusion$slateCount(int value) {return arsFusion$rows;}
    @ModifyConstant(method={"renderPerks","renderSlate"},constant=@Constant(doubleValue=0.175))
    private double arsFusion$rowSpacing(double value) {return arsFusion$rows<=3?value:0.35/(arsFusion$rows-1);}
    @ModifyConstant(method="renderPerks",constant=@Constant(floatValue=0.18f))
    private float arsFusion$itemScale(float value) {return value*3.0f/arsFusion$rows;}
    @ModifyConstant(method="renderSlate",constant=@Constant(floatValue=-0.02f))
    private float arsFusion$textScale(float value) {return value*3.0f/arsFusion$rows;}
    @ModifyArg(method="renderPerks",at=@At(value="INVOKE",target="Lsoftware/bernie/geckolib/model/GeoModel;getBone(Ljava/lang/String;)Ljava/util/Optional;",ordinal=1),index=0)
    private String arsFusion$perkAnchor(String name) {return arsFusion$rows>3?"top_1":name;}
    @ModifyArg(method="renderSlate",at=@At(value="INVOKE",target="Lsoftware/bernie/geckolib/model/GeoModel;getBone(Ljava/lang/String;)Ljava/util/Optional;",ordinal=2),index=0)
    private String arsFusion$slateAnchor(String name) {return arsFusion$rows>3?"top_1":name;}
}
