package online.dgbcraft.velocity.chatter.channel;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import online.dgbcraft.velocity.chatter.Config;
import online.dgbcraft.velocity.chatter.VelocityChatter;
import online.dgbcraft.velocity.chatter.command.GlobalChatCommand;
import online.dgbcraft.velocity.chatter.packet.GlobalChatPacketListener;
import online.dgbcraft.velocity.chatter.util.DependencyUtil;

import static online.dgbcraft.velocity.chatter.util.DependencyUtil.PACKET_EVENTS;

/**
 * @author Sanluli36li
 */
public class GlobalChatChannel extends BaseChatChannel {
    private static final String MCDR_COMMAND_PREFIX = "!!";

    private final GlobalChatCommand command;
    private PacketListenerCommon packetListener;

    private Component globalChatMessage;

    public GlobalChatChannel(VelocityChatter plugin) {
        super(plugin);

        command = new GlobalChatCommand(this);

        if (DependencyUtil.isDependencyLoaded(PACKET_EVENTS)) {
            packetListener = new GlobalChatPacketListener(this).getCommon();
        }
    }

    @Override
    public boolean isPlayerCanVisitChannel(Player player) {
        return true;
    }

    @Override
    public boolean isPlayerCanSendMessage(Player player) {
        return true;
    }

    @Override
    public void onPlayerChat(Player player, String message) {
        sendPlayerMessage(player, message, false);
    }

    @Override
    public void load() {
        globalChatMessage = getPlugin().getConfiguration().getGlobalChat().getMessageFormat();


        if (getPlugin().getConfiguration().getGlobalChat().getChatMode() == Config.GlobalChatMode.COMMAND) {
            // 使用命令模式时不监听聊天
            command.register();
        } else {
            // 使用命令模式时不监听聊天
            getPlugin().getProxy().getEventManager().register(getPlugin(), this);
        }

        if (DependencyUtil.isDependencyLoaded(PACKET_EVENTS)
            && getPlugin().getConfiguration().getGlobalChat().getChatMode() == Config.GlobalChatMode.BROADCAST
        ) {
            // 需要 PacketEvents 并开启广播模式: 监听并阻止所有连接到代理的玩家聊天包回传
            PacketEvents.getAPI().getEventManager().registerListener(packetListener);
        }
        super.load();
    }

    @Override
    public void unload() {
        command.unregister();
        getPlugin().getProxy().getEventManager().unregisterListener(getPlugin(), this);

        if (DependencyUtil.isDependencyLoaded(PACKET_EVENTS)) {
            PacketEvents.getAPI().getEventManager().unregisterListener(packetListener);
        }
        super.unload();
    }

    @Override
    public boolean shouldEnable() {
        return getPlugin().getConfiguration().getGlobalChat().isEnable();
    }

    public void sendPlayerMessage(Player player, String message, boolean forward) {
        RegisteredServer server = player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServer()
            : null;
        Component messageComponent = buildPlayerMessage(globalChatMessage, player, message);
        if (forward) {
            for (Player player2 : getPlugin().getProxy().getAllPlayers()) {
                if (!(player2.getCurrentServer().isPresent()
                    && player2.getCurrentServer().get().getServer() == server)) {
                    player2.sendMessage(messageComponent);
                }
            }
        } else {
            broadcast(buildPlayerMessage(globalChatMessage, player, message));
        }
    }

    @Subscribe
    public void onPlayerChat(PlayerChatEvent event) {
        if (!event.getResult().isAllowed() || (getPlugin().getConfiguration().getGlobalChat().isIgnoreMcdrCommand()
            && event.getMessage().startsWith(MCDR_COMMAND_PREFIX))
        ) {
            // 忽略已拦截 / MCDR命令
            // 如果同时安装了 PacketEvents 并开启广播模式, MCDR命令将像普通命令一样隐式执行
            return;
        }

        Player player = event.getPlayer();
        String message = event.getMessage();

        if (getPlugin().getConfiguration().getGlobalChat().getChatMode() == Config.GlobalChatMode.FORWARD) {
            // 转发模式: 将玩家的消息发送至其他服务器上的玩家
            sendPlayerMessage(player, message, true);
        } else if (getPlugin().getConfiguration().getGlobalChat().getChatMode() == Config.GlobalChatMode.BROADCAST) {
            if (!DependencyUtil.isDependencyLoaded(PACKET_EVENTS)
                && getPlugin().getConfiguration().getGlobalChat().isMcdrCompatible()
                && message.startsWith(MCDR_COMMAND_PREFIX)
            ) {
                // MCDR兼容: 当识别到玩家的消息以"!!"开头时, 认为此条消息为MCDR命令, 改为使用转发模式

                // 当有PacketEvents时, 不再阻止聊天信息发送到后端, 而转为阻止后端服务器将聊天回传到客户端
                // 因为聊天仍会发送至后端服务器(客户端看不见), 所以MCDR不会受到影响, 故MCDR兼容模式无需激活
                sendPlayerMessage(player, message, true);
            } else {
                sendPlayerMessage(player, message, false);

                if (!DependencyUtil.isDependencyLoaded(PACKET_EVENTS)
                    && event.getPlayer().getProtocolVersion().lessThan(ProtocolVersion.MINECRAFT_1_19_1)) {
                    // 如果没有安装PacketEvents, 且使用1.19.1以前的版本, 则使用Velocity原生的事件拦截消息
                    event.setResult(PlayerChatEvent.ChatResult.denied());
                }
            }
        }
    }
}