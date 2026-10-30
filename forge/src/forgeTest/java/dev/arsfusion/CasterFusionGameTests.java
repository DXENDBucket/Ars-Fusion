package dev.arsfusion;
import net.minecraft.gametest.framework.*;
import net.minecraftforge.gametest.*;
@GameTestHolder("ars_fusion_caster") @PrefixGameTestTemplate(false)
public final class CasterFusionGameTests {
 @GameTest(template="empty") public static void policyAndPersistence(GameTestHelper h) {CasterFusionCases.policyAndPersistence(h,TestPlayers.player(h));}
 @GameTest(template="empty") public static void slotsBlacklistAndEquipment(GameTestHelper h) {CasterFusionCases.slotsBlacklistAndRealEquipment(h,TestPlayers.player(h));}
 @GameTest(template="empty") public static void nativeMirror(GameTestHelper h) {CasterFusionCases.mirrorNativeEditingAndCasting(h,TestPlayers.player(h));}
 @GameTest(template="empty") public static void nativeIronStaff(GameTestHelper h) {CasterFusionCases.nativeIronStaffAndBookBoundary(h,TestPlayers.player(h));}
 @GameTest(template="empty") public static void nativeGoetyStaff(GameTestHelper h) {if(!dev.arsfusion.platform.CasterPlatform.goetyLoaded()) {h.succeed();return;}GoetyCasterCases.run(h,TestPlayers.player(h));}
}
