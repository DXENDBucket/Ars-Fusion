package dev.arsfusion;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.spell.method.MethodSelf;
import com.hollingsworth.arsnouveau.common.spell.method.MethodProjectile;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.*;
public final class TestPlayers {
    static net.minecraft.server.level.ServerPlayer player(GameTestHelper h) {
        var world = h.getLevel(); var server = world.getServer();
        if(dev.arsfusion.platform.FusionPlatform.ironLoaded())io.redspace.ironsspellbooks.api.config.SpellConfigManager.onDatapackSync(new net.minecraftforge.event.OnDatapackSyncEvent(server.getPlayerList(), null));
        var player = new net.minecraft.server.level.ServerPlayer(server, world,
            new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "ForgeStudent")) {
                @Override public void displayClientMessage(net.minecraft.network.chat.Component message, boolean actionBar) { System.out.println("TEST MESSAGE: " + message.getString()); }
            };
        var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        player.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(server, connection, player) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet, net.minecraft.network.PacketSendListener listener) {}
        };
        player.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(1, 3, 1)));
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        CapabilityRegistry.getMana(player).ifPresent(m -> { m.setMaxMana(1000); m.setMana(1000); });
        if(dev.arsfusion.platform.FusionPlatform.ironLoaded())player.getAttribute(io.redspace.ironsspellbooks.api.registry.AttributeRegistry.MAX_MANA.get()).setBaseValue(10000);
        if(dev.arsfusion.platform.FusionPlatform.ironLoaded())io.redspace.ironsspellbooks.api.magic.MagicData.getPlayerMagicData(player).setMana(1000);
        return player;
    }
}
