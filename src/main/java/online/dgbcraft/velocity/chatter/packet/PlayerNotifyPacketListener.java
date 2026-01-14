package online.dgbcraft.velocity.chatter.packet;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import online.dgbcraft.velocity.chatter.channel.PlayerNotifyChannel;
import online.dgbcraft.velocity.chatter.util.ComponentUtils;

import java.util.UUID;

import static online.dgbcraft.velocity.chatter.constant.LocaleKeys.*;

/**
 * @author Sanluli36li
 */
public class PlayerNotifyPacketListener implements PacketListener {
    private static final ResourceLocation LOCATION_CHAT_TYPE_SYSTEM = ResourceLocation.minecraft("system");
    PlayerNotifyChannel channel;

    public PlayerNotifyPacketListener(PlayerNotifyChannel channel) {
        this.channel = channel;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_19)) {
            // 1.19 -> Latest
            if (event.getPacketType() == PacketType.Play.Server.SYSTEM_CHAT_MESSAGE) {
                event.setCancelled(shouldBlock(new WrapperPlayServerSystemChatMessage(event).getMessage()));
            }
        } else {
            if (event.getPacketType() == PacketType.Play.Server.CHAT_MESSAGE) {
                ChatMessage message = new WrapperPlayServerChatMessage(event).getMessage();
                if (LOCATION_CHAT_TYPE_SYSTEM.equals(message.getType().getName())) {
                    event.setCancelled(shouldBlock(message.getChatContent()));
                }
            }
        }
    }

    public boolean shouldBlock(Component message) {
        if (message instanceof TranslatableComponent) {
            if (MULTIPLAYER_PLAYER_JOINED.equals(((TranslatableComponent) message).key())
                || MULTIPLAYER_PLAYER_JOINED_RENAMED.equals(((TranslatableComponent) message).key())
            ) {
                // 仅处理连接到此代理服务器的玩家信息 (应该可以保留假人的进出消息)
                return channel.getPlugin().getProxy().getPlayer(
                    ComponentUtils.getSourceUuidFromArguments(((TranslatableComponent) message))
                ).isPresent();
            } else if (MULTIPLAYER_PLAYER_JOINED_LEFT.equals(((TranslatableComponent) message).key())) {
                UUID id = ComponentUtils.getSourceUuidFromArguments(((TranslatableComponent) message));
                // 离开信息得缓存一下玩家退出时间 1秒内退出服务器的就直接过滤掉
                return System.currentTimeMillis() - channel.getDisconnected().getOrDefault(id, 0L) < 1000;
            }
        }
        return false;
    }

    public PacketListenerCommon getCommon() {
        return this.asAbstract(PacketListenerPriority.NORMAL);
    }
}
