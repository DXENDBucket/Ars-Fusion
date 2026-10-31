package dev.arsfusion.mixin;

import com.hollingsworth.arsnouveau.client.renderer.tile.AlterationTableRenderer;
import com.hollingsworth.arsnouveau.common.block.tile.AlterationTile;
import com.hollingsworth.arsnouveau.api.util.PerkUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.arsfusion.platform.HighArmorPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Ars 4 draws slot levels as model variants; high tiers use text in the same native display space. */
@Mixin(value=AlterationTableRenderer.class,remap=false)
public abstract class HighArmorRendererMixin {
    @Unique private int arsFusion$rows=3;
    @Shadow public abstract void setSlateRow(BakedGeoModel model,String name,int tier);
    @Inject(method="renderPerks",at=@At("HEAD"))
    private void arsFusion$rows(AlterationTile tile,PoseStack pose,float ticks,MultiBufferSource buffers,VertexConsumer vertex,int light,int overlay,float partialTick,CallbackInfo ci) {
        arsFusion$rows=Math.max(3,HighArmorPlatform.slotCount(tile.armorStack));
    }
    @ModifyConstant(method="renderPerks",constant=@Constant(intValue=3))
    private int arsFusion$capacity(int original) {return arsFusion$rows;}
    @ModifyConstant(method="renderPerks",constant=@Constant(doubleValue=0.175))
    private double arsFusion$spacing(double original) {return arsFusion$rows<=3?original:0.35/(arsFusion$rows-1);}
    @ModifyConstant(method="renderPerks",constant=@Constant(floatValue=0.18f))
    private float arsFusion$scale(float original) {return original*3.0f/arsFusion$rows;}
    @ModifyArg(method="renderPerks",at=@At(value="INVOKE",target="Lsoftware/bernie/geckolib/model/GeoModel;getBone(Ljava/lang/String;)Ljava/util/Optional;",ordinal=1),index=0)
    private String arsFusion$anchor(String name) {return arsFusion$rows>3?"top_1":name;}
    @Inject(method="renderSlate",at=@At("HEAD"),cancellable=true)
    private void arsFusion$highSlate(BakedGeoModel model,AlterationTile tile,CallbackInfo ci) {
        if(HighArmorPlatform.slotCount(tile.armorStack)<=3)return;
        for(String row:new String[]{"top","mid","bot"})setSlateRow(model,row,0);
        // Native code also does subList(perks.size(),3), which is invalid for four/five slots.
        ci.cancel();
    }
    @Inject(method="renderPerks",at=@At("RETURN"))
    private void arsFusion$labels(AlterationTile tile,PoseStack pose,float ticks,MultiBufferSource buffers,VertexConsumer vertex,int light,int overlay,float partialTick,CallbackInfo ci) {
        if(arsFusion$rows<=3)return;
        var holder=PerkUtil.getPerkHolder(tile.armorStack);if(holder==null)return;
        var slots=holder.getSlotsForTier();var font=Minecraft.getInstance().font;
        // Independent compact legend on the slate, including when no thread items are installed.
        // The original renderer still draws the actual items; no native rendering method is copied.
        pose.pushPose();pose.translate(0.18,1.1,-0.18);pose.mulPose(Axis.XP.rotationDegrees(45));
        float scale=0.045f/arsFusion$rows;pose.scale(-scale,-scale,scale);
        for(int i=0;i<slots.size();i++) {
            var label=Component.translatable("enchantment.level."+slots.get(i).value).withStyle(ChatFormatting.BOLD);
            font.drawInBatch(label,-font.width(label)/2f,i*10,0x5B5B5B,false,pose.last().pose(),buffers,Font.DisplayMode.NORMAL,0,light);
        }
        pose.popPose();
    }
}
