package dev.arsfusion;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.GameType;
import java.util.UUID;
public final class TestPlayers {
    static ServerPlayer player(GameTestHelper h) {
        if(dev.arsfusion.platform.FusionPlatform.ironLoaded())io.redspace.ironsspellbooks.api.config.SpellConfigManager.onDatapackSync(
                new net.neoforged.neoforge.event.OnDatapackSyncEvent(h.getLevel().getServer().getPlayerList(), null));
        var cookie = CommonListenerCookie.createInitial(new com.mojang.authlib.GameProfile(UUID.randomUUID(), "ScrollStudent"), false);
        var player = new ServerPlayer(h.getLevel().getServer(), h.getLevel(), cookie.gameProfile(), cookie.clientInformation());
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new io.netty.channel.embedded.EmbeddedChannel(connection);
        // Real ServerPlayer crafting identity, without pretending a headless test performed the client mod handshake.
        player.connection = new ServerGamePacketListenerImpl(h.getLevel().getServer(), connection, player, cookie) {
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
            @Override public void send(net.minecraft.network.protocol.Packet<?> packet, net.minecraft.network.PacketSendListener listener) {}
        };
        player.setPos(h.absoluteVec(new net.minecraft.world.phys.Vec3(1, 2, 1)));
        player.setGameMode(GameType.SURVIVAL); return player;
    }
}
