package online.dgbcraft.velocity.chatter.channel;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import online.dgbcraft.velocity.chatter.VelocityChatter;
import online.dgbcraft.velocity.chatter.packet.PlayerNotifyPacketListener;
import online.dgbcraft.velocity.chatter.util.ComponentUtils;
import online.dgbcraft.velocity.chatter.util.DependencyUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static online.dgbcraft.velocity.chatter.util.DependencyUtil.PACKET_EVENTS;

/**
 * @author Sanluli36li
 */
public class PlayerNotifyChannel extends BaseChatChannel {
    private PacketListenerCommon packetListener;

    private Component joinMessage;
    private Component leaveMessage;
    private Component transferMessage;

    private final Map<UUID, Long> disconnected = new HashMap<>();

    public PlayerNotifyChannel(VelocityChatter plugin) {
        super(plugin);

        if (DependencyUtil.isDependencyLoaded(PACKET_EVENTS)) {
            packetListener = new PlayerNotifyPacketListener(this).getCommon();
        }
    }

    @Override
    public boolean isPlayerCanVisitChannel(Player player) {
        return true;
    }

    @Override
    public boolean isPlayerCanSendMessage(Player player) {
        return false;
    }

    @Override
    public void broadcast(Component message) {
        for (Player player : getPlugin().getProxy().getAllPlayers()) {
            player.sendMessage(message);
        }
    }

    @Override
    public void onPlayerChat(Player player, String message) {
    }

    @Override
    public void load() {
        joinMessage = getPlugin().getConfiguration().getPlayerNotify().getJoinMessageFormat();
        leaveMessage = getPlugin().getConfiguration().getPlayerNotify().getLeaveMessageFormat();
        transferMessage = getPlugin().getConfiguration().getPlayerNotify().getTransferMessageFormat();

        getPlugin().getProxy().getEventManager().register(getPlugin(), this);

        if (DependencyUtil.isDependencyLoaded(PACKET_EVENTS)
            && getPlugin().getConfiguration().getPlayerNotify().isBlockBackendServer()
        ) {
            // 禁用后端服务器发送的玩家进入/离开服务器信息, 所有代理服务器上连接/断开/转移的信息将由VelocityChatter接管
            PacketEvents.getAPI().getEventManager().registerListener(packetListener);
        }

        super.load();
    }

    @Override
    public void unload() {
        getPlugin().getProxy().getEventManager().unregisterListener(getPlugin(), this);

        super.unload();
    }

    @Override
    public boolean shouldEnable() {
        return getPlugin().getConfiguration().getPlayerNotify().isEnable();
    }

    private Component buildJoinMessage(Player player, RegisteredServer server) {
        return joinMessage
            .replaceText(ComponentUtils.getPlayerReplacement(
                player,
                getPlugin().getConfiguration().getPlayerNotify().isPlayerTooltip(),
                getPlugin().getConfiguration().getPlayerNotify().isPlayerClickable()
            ))
            .replaceText(ComponentUtils.getServerNameReplacement(
                server,
                getPlugin().getConfiguration().getPlayerNotify().isServerTooltip(),
                getPlugin().getConfiguration().getPlayerNotify().isServerClickable()
            ))
            .replaceText(ComponentUtils.getServerAliasReplacement(
                server,
                getPlugin().getConfiguration().getServerAlias(server),
                getPlugin().getConfiguration().getPlayerNotify().isServerTooltip(),
                getPlugin().getConfiguration().getPlayerNotify().isServerClickable()
            ));
    }

    private Component buildTransferMessage(Player player, RegisteredServer preServer, RegisteredServer server) {
        return transferMessage
            .replaceText(ComponentUtils.getPlayerReplacement(
                player,
                getPlugin().getConfiguration().getPlayerNotify().isPlayerTooltip(),
                getPlugin().getConfiguration().getPlayerNotify().isPlayerClickable()
            ))
            .replaceText(ComponentUtils.getServerNameReplacement(
                "%preServer%",
                preServer,
                getPlugin().getConfiguration().getPlayerNotify().isServerTooltip(),
                getPlugin().getConfiguration().getPlayerNotify().isServerClickable()
            ))
            .replaceText(ComponentUtils.getServerAliasReplacement(
                "%preServerAlias%",
                preServer,
                getPlugin().getConfiguration().getServerAlias(preServer),
                getPlugin().getConfiguration().getPlayerNotify().isServerTooltip(),
                getPlugin().getConfiguration().getPlayerNotify().isServerClickable()
            ))
            .replaceText(ComponentUtils.getServerNameReplacement(
                server,
                getPlugin().getConfiguration().getPlayerNotify().isServerTooltip(),
                getPlugin().getConfiguration().getPlayerNotify().isServerClickable()
            ))
            .replaceText(ComponentUtils.getServerAliasReplacement(
                server,
                getPlugin().getConfiguration().getServerAlias(server),
                getPlugin().getConfiguration().getPlayerNotify().isServerTooltip(),
                getPlugin().getConfiguration().getPlayerNotify().isServerClickable()
            ));
    }

    private Component buildLeaveMessage(Player player, RegisteredServer preServer) {
        return leaveMessage
            .replaceText(ComponentUtils.getPlayerReplacement(
                player,
                getPlugin().getConfiguration().getPlayerNotify().isPlayerTooltip(),
                getPlugin().getConfiguration().getPlayerNotify().isPlayerClickable()
            ))
            .replaceText(ComponentUtils.getServerNameReplacement(
                "%preServer%",
                preServer,
                getPlugin().getConfiguration().getPlayerNotify().isServerTooltip(),
                getPlugin().getConfiguration().getPlayerNotify().isServerClickable()
            ))
            .replaceText(ComponentUtils.getServerAliasReplacement(
                "%preServerAlias%",
                preServer,
                getPlugin().getConfiguration().getServerAlias(preServer),
                getPlugin().getConfiguration().getPlayerNotify().isServerTooltip(),
                getPlugin().getConfiguration().getPlayerNotify().isServerClickable()
            ));
    }

    @Subscribe
    public void onServerPostConnect(ServerPostConnectEvent event) {
        disconnected.remove(event.getPlayer().getUniqueId());

        RegisteredServer server = event.getPlayer().getCurrentServer().isPresent()
            ? event.getPlayer().getCurrentServer().get().getServer() : null;

        if (event.getPreviousServer() == null) {
            // 加入代理服务器
            Component message = buildJoinMessage(event.getPlayer(), server);

            for (Player player2 : getPlugin().getProxy().getAllPlayers()) {
                if (event.getPlayer() != player2) {
                    player2.sendMessage(message);
                }
            }
        } else {
            // 在服务器之间转移
            Component message = buildTransferMessage(event.getPlayer(), event.getPreviousServer(), server);

            for (Player player2 : getPlugin().getProxy().getAllPlayers()) {
                if (event.getPlayer() != player2) {
                    player2.sendMessage(message);
                }
            }
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        if (event.getPlayer().getCurrentServer().isPresent()) {
            broadcast(buildLeaveMessage(event.getPlayer(), event.getPlayer().getCurrentServer().get().getServer()));
            disconnected.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }

    public Map<UUID, Long> getDisconnected() {
        return disconnected;
    }
}
