package dev.arsfusion;
import com.Polarice3.Goety.utils.WandUtil;
import com.Polarice3.Goety.api.items.magic.IWand;
import com.Polarice3.Goety.common.magic.spells.frost.IceSpikeSpell;
import com.Polarice3.Goety.common.entities.projectiles.IceSpear;
import com.Polarice3.Goety.common.items.handler.SoulUsingItemHandler;
import dev.arsfusion.caster.CasterFusion;
import dev.arsfusion.compat.GoetyCaster;
import dev.arsfusion.config.FusionConfig;
import dev.arsfusion.platform.CasterPlatform;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import java.util.*;
public final class GoetyCasterCases {
    public static void run(GameTestHelper h,ServerPlayer player) {
        var staff=CasterFusionCases.item("goety:frost_staff");var focus=CasterFusionCases.item("goety:ice_spike_focus");
        SoulUsingItemHandler.get(staff).insertItem(focus);
        h.assertTrue(!IWand.getFocus(staff).isEmpty(),"Native donor actually contains a focus");
        var fused=CasterFusion.fuse(CasterFusionCases.wand(),staff);h.assertTrue(CasterFusion.has(fused)&&CasterPlatform.goetyTraits(fused),"Goety manual trait data stored");
        var saved=SwordChecks.roundTrip(h,fused);h.assertTrue(CasterPlatform.goetyTraits(saved),"Native staff identity survives persistent/network round trips");
        h.getLevel().addNewPlayer(player);
        try {
            var spell=new IceSpikeSpell();player.setItemInHand(InteractionHand.MAIN_HAND,saved);player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
            var view=WandUtil.findWand(player);h.assertTrue(view.is(staff.getItem())&&spell.rightStaff(view)&&IWand.getFocus(view).isEmpty(),"Native Goety lookup sees intrinsic frost-staff identity, without transplanted focus");
            h.assertTrue(WandUtil.findWandOnHand(player,InteractionHand.MAIN_HAND).is(staff.getItem()),"Hand-specific lookup agrees");
            spell.SpellResult(h.getLevel(),player,view,spell.defaultStats());
            var projectiles=new ArrayList<IceSpear>();for(var entity:h.getLevel().getAllEntities())if(entity instanceof IceSpear spear&&spear.getOwner()==player)projectiles.add(spear);
            h.assertTrue(projectiles.size()==1,"Actual native Ice Spike behavior produces Ice Spear with fused frost staff");projectiles.forEach(net.minecraft.world.entity.Entity::discard);
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);player.setItemInHand(InteractionHand.OFF_HAND,saved);
            h.assertTrue(WandUtil.findWand(player).is(staff.getItem())&&WandUtil.findWandOnHand(player,InteractionHand.OFF_HAND).is(staff.getItem()),"Offhand fused caster is recognized");
            var other=CasterFusionCases.item("goety:nether_staff");player.setItemInHand(InteractionHand.MAIN_HAND,other);
            h.assertTrue(WandUtil.findWand(player)==other,"Real main-hand staff retains native priority over fused offhand");
            player.setItemInHand(InteractionHand.MAIN_HAND,saved);player.setItemInHand(InteractionHand.OFF_HAND,other);
            h.assertTrue(WandUtil.findWand(player).is(staff.getItem()),"Fused main hand retains native priority over real offhand");
            boolean old=FusionConfig.GOETY_STAFF_FUSION_ENABLED.get();
            try {FusionConfig.GOETY_STAFF_FUSION_ENABLED.set(false);var automatic=CasterFusion.fuse(CasterFusionCases.wand(),staff);h.assertTrue(CasterFusion.has(automatic)&&!CasterPlatform.goetyTraits(automatic)&&GoetyCaster.view(automatic).isEmpty(),"Disabling manual integration creates attribute-only tagged fusion");h.assertTrue(!GoetyCaster.view(saved).isEmpty(),"Existing manual fusion data remains usable");}
            finally {FusionConfig.GOETY_STAFF_FUSION_ENABLED.set(old);}
            h.succeed();
        } finally {player.discard();}
    }
}
