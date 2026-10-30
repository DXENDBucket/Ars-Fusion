package dev.arsfusion;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectHarm;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectIgnite;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.common.spell.method.MethodProjectile;
import dev.arsfusion.sword.SwordFusion;
import dev.arsfusion.config.FusionConfig;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import java.util.List;

public final class SwordFusionCases {
    private SwordFusionCases() {}
    public static ItemStack donor() {
        var stack=dev.arsfusion.sword.NativeSwordCasting.sword().getDefaultInstance();
        SwordChecks.write(stack,true,false);return stack;
    }
    public static void rulesAndAnvil(GameTestHelper h,ServerPlayer player) {
        boolean old=FusionConfig.SWORD_FUSION_ENABLED.get();
        try {
            FusionConfig.SWORD_FUSION_ENABLED.set(true);var donor=donor();
            for(var item:List.of(Items.IRON_SWORD,Items.DIAMOND_AXE,Items.STONE_PICKAXE,Items.WOODEN_SHOVEL,Items.NETHERITE_HOE,Items.FLINT_AND_STEEL))
                h.assertTrue(SwordFusion.canFuse(item.getDefaultInstance(),donor),"Allow tag-driven weapon/tool, including custom additions: "+item);
            for(var item:List.of(Items.STICK,Items.BOW,Items.CROSSBOW,Items.SHIELD,Items.GOLDEN_SWORD))
                h.assertTrue(!SwordFusion.canFuse(item.getDefaultInstance(),donor),"Max-stack and blacklist take precedence: "+item);
            var reserved=Items.DIAMOND_SWORD.getDefaultInstance();SwordChecks.write(reserved,true,false);
            h.assertTrue(!SwordFusion.canFuse(reserved,donor),"Never overwrite an existing Ars caster");
            var left=Items.DIAMOND_AXE.getDefaultInstance();left.setDamageValue(17);SwordChecks.enchant(h,left);
            var attributes=SwordChecks.stats(left);player.experienceLevel=10;
            var menu=new AnvilMenu(1,player.getInventory(),ContainerLevelAccess.NULL);
            menu.getSlot(0).set(left.copy());menu.getSlot(1).set(donor.copy());menu.createResult();
            var result=menu.getSlot(2).getItem().copy();
            h.assertTrue(result.is(left.getItem())&&result.getDamageValue()==17&&result.isEnchanted()&&SwordChecks.stats(result).equals(attributes),"Only casting is transferred; retain original item, enchantments, durability and all physical modifiers");
            h.assertTrue(!dev.arsfusion.platform.SwordPlatform.existingCaster(left),"Input is unchanged");
            h.assertTrue(SwordChecks.recipe(result).equals(SwordChecks.recipe(donor)),"Copy current native spell");
            h.assertTrue(result.getHoverName().getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents name&&name.getKey().equals("item.ars_fusion.enchanters_tool"),"Enchanter's original-name localization");
            h.assertTrue(menu.getCost()==1,"Costs one level");menu.getSlot(2).onTake(player,result);
            h.assertTrue(player.experienceLevel==9&&menu.getSlot(0).getItem().isEmpty()&&menu.getSlot(1).getItem().isEmpty(),"Both items and XP are consumed once");
            h.assertTrue(!SwordFusion.canFuse(result,donor),"One binding only");
            var saved=SwordChecks.roundTrip(h,result);
            h.assertTrue(SwordFusion.has(saved)&&SwordChecks.recipe(saved).equals(SwordChecks.recipe(result)),"Marker and native casting data survive persistence and network codecs");
            SwordChecks.rename(saved,Component.literal("Personal tool"));h.assertTrue(saved.getHoverName().getString().equals("Personal tool"),"Respect custom names");
            FusionConfig.SWORD_FUSION_ENABLED.set(false);
            h.assertTrue(SwordFusion.fuse(left,donor).isEmpty()&&SwordFusion.has(saved),"Disable creation without stripping existing bindings");
        } finally {FusionConfig.SWORD_FUSION_ENABLED.set(old);}
        h.succeed();
    }
    public static void nativeScribing(GameTestHelper h,ServerPlayer player) {
        var result=SwordFusion.fuse(Items.DIAMOND_SWORD.getDefaultInstance(),donor());
        SwordChecks.scribe(h,player,result,false);
        var recipe=SwordChecks.recipe(result);
        var control=donor();SwordChecks.scribe(h,player,control,false);
        h.assertTrue(recipe.size()>=2&&recipe.get(0)==MethodTouch.INSTANCE&&recipe.get(1)==EffectIgnite.INSTANCE
                &&recipe.equals(SwordChecks.recipe(control)),"Real scribe block matches the native sword, including platform-native implicit augments");
        SwordChecks.scribe(h,player,result,true);
        h.assertTrue(SwordChecks.recipe(result).equals(recipe),"Keep native sword rejection of explicit forms");
        h.succeed();
    }
    public static void nativeCastAndDurability(GameTestHelper h,ServerPlayer player) {
        var nativeSword=donor();var bound=SwordFusion.fuse(Items.DIAMOND_AXE.getDefaultInstance(),nativeSword);
        var a=h.spawn(EntityType.PIG,new net.minecraft.core.BlockPos(1,2,1));
        var b=h.spawn(EntityType.PIG,new net.minecraft.core.BlockPos(3,2,1));
        var c=h.spawn(EntityType.PIG,new net.minecraft.core.BlockPos(5,2,1));
        SwordChecks.mana(player,1000);player.setItemInHand(InteractionHand.MAIN_HAND,nativeSword);
        float before=a.getHealth();SwordChecks.hit(nativeSword,a,player);float nativeDamage=before-a.getHealth();
        h.assertTrue(nativeDamage>0,"Native sword baseline actually casts");
        var plain=Items.DIAMOND_AXE.getDefaultInstance();player.setItemInHand(InteractionHand.MAIN_HAND,plain);SwordChecks.hit(plain,c,player);int wear=plain.getDamageValue();
        SwordChecks.mana(player,1000);double mana=SwordChecks.mana(player);player.setItemInHand(InteractionHand.MAIN_HAND,bound);
        before=b.getHealth();SwordChecks.hit(bound,b,player);
        h.assertTrue(Math.abs((before-b.getHealth())-nativeDamage)<0.001,"Exactly one native spell dispatch using real attacker/tool context");
        h.assertTrue(SwordChecks.mana(player)<mana,"Native Ars mana payment still applies");
        h.assertTrue(bound.getDamageValue()==wear,"Only original tool durability applies, without native donor wear");
        h.assertTrue(!dev.arsfusion.sword.NativeSwordCasting.delegating(bound),"Delegation scope is cleaned up");
        var d=h.spawn(EntityType.PIG,new net.minecraft.core.BlockPos(7,2,1));
        SwordChecks.mana(player,0);before=d.getHealth();SwordChecks.hit(bound,d,player);
        h.assertTrue(d.getHealth()==before,"Out of mana prevents spell, without canceling physical tool callback");
        h.succeed();
    }
}
