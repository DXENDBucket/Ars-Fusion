package dev.arsfusion;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("ars_fusion_bow") @PrefixGameTestTemplate(false)
public final class BowFusionGameTests {
 @GameTest(template="empty") public static void policiesAndScribing(GameTestHelper h) {BowFusionCases.policiesAndScribing(h,TestPlayers.player(h));}
 @GameTest(template="empty") public static void nativeReleaseAndAugmentArrows(GameTestHelper h) {BowFusionCases.nativeReleaseAndAugmentArrows(h,TestPlayers.player(h));}
 @GameTest(template="empty") public static void impactsAndPartialBudget(GameTestHelper h) {BowFusionCases.impactsAndPartialBudget(h,TestPlayers.player(h));}
 @GameTest(template="empty") public static void cataclysmNativeProjectiles(GameTestHelper h) {BowFusionCases.cataclysmNativeProjectiles(h,TestPlayers.player(h));}
 @GameTest(template="empty") public static void optionalConfluxChantAndCooldown(GameTestHelper h) {if(!net.minecraftforge.fml.ModList.get().isLoaded("ars_conflux")) {h.succeed();return;}ConfluxBowCases.run(h,TestPlayers.player(h));}
}
