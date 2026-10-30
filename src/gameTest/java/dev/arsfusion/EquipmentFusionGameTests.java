package dev.arsfusion;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("ars_fusion_equipment") @PrefixGameTestTemplate(false)
public final class EquipmentFusionGameTests {
 @GameTest(template="empty") public static void rulesAndNativeScribing(GameTestHelper h) {EquipmentFusionCases.rulesAndScribing(h,TestPlayers.player(h));}
 @GameTest(template="empty") public static void loadedArrowsAndAugments(GameTestHelper h) {EquipmentFusionCases.crossbowArrows(h,TestPlayers.player(h));}
 @GameTest(template="empty") public static void nativeMultiShotFireworksAndMana(GameTestHelper h) {EquipmentFusionCases.multishotFireworksAndEmptyMana(h,TestPlayers.player(h));}
 @GameTest(template="empty") public static void actualShieldBlock(GameTestHelper h) {EquipmentFusionCases.shieldBlocking(h,TestPlayers.player(h));}
 @GameTest(template="empty",timeoutTicks=300) public static void nativeShieldRepair(GameTestHelper h) {EquipmentFusionCases.shieldRepair(h,TestPlayers.player(h));}
 @GameTest(template="empty") public static void optionalConfluxOffhandFire(GameTestHelper h) {if(!net.neoforged.fml.ModList.get().isLoaded("ars_conflux")) {h.succeed();return;}ConfluxCrossbowCases.run(h,TestPlayers.player(h));}
 @GameTest(template="empty") public static void nativeIronAutoloader(GameTestHelper h) {OptionalEquipmentCases.autoloader(h,TestPlayers.player(h));}
 @GameTest(template="empty") public static void nativeGoetyRevolver(GameTestHelper h) {OptionalEquipmentCases.revolver(h,TestPlayers.player(h));}
 @GameTest(template="empty") public static void nativeCataclysmShieldsAndDash(GameTestHelper h) {OptionalEquipmentCases.cataclysmPolicyAndDash(h,TestPlayers.player(h));}
 @GameTest(template="empty") public static void nativeCataclysmShieldBlock(GameTestHelper h) {OptionalEquipmentCases.cataclysmActualBlock(h,TestPlayers.player(h));}
}
