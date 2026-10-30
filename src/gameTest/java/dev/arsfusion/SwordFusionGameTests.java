package dev.arsfusion;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder("ars_fusion_sword") @PrefixGameTestTemplate(false)
public final class SwordFusionGameTests {
    @GameTest(template="empty") public static void policiesAnvilAndPersistence(GameTestHelper h) {SwordFusionCases.rulesAndAnvil(h,TestPlayers.player(h));}
    @GameTest(template="empty") public static void nativeSpellAttributeAndOldItems(GameTestHelper h) {SwordFusionCases.nativeSpellBonus(h,TestPlayers.player(h));}
    @GameTest(template="empty") public static void actualScribeBlock(GameTestHelper h) {SwordFusionCases.nativeScribing(h,TestPlayers.player(h));}
    @GameTest(template="empty") public static void nativeCastingPaymentAndPhysicalWear(GameTestHelper h) {SwordFusionCases.nativeCastAndDurability(h,TestPlayers.player(h));}
}
