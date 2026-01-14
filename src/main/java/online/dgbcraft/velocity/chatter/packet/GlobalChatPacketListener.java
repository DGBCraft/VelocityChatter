package online.dgbcraft.velocity.chatter.packet;

import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerCommon;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.chat.message.ChatMessage_v1_16;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerChatMessage;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSystemChatMessage;
import com.velocitypowered.api.proxy.Player;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import online.dgbcraft.velocity.chatter.channel.GlobalChatChannel;
import online.dgbcraft.velocity.chatter.util.ComponentUtils;
import online.dgbcraft.velocity.chatter.util.DependencyUtil;

import static online.dgbcraft.velocity.chatter.constant.LocaleKeys.CHAT_TYPE_TEXT;
import static online.dgbcraft.velocity.chatter.util.DependencyUtil.VIA_VERSION;

/**
 * @author Sanluli36li
 */
public class GlobalChatPacketListener implements PacketListener {
    private static final ResourceLocation LOCATION_CHAT_TYPE_CHAT = ResourceLocation.minecraft("chat");
    GlobalChatChannel channel;

    public GlobalChatPacketListener(GlobalChatChannel channel) {
        this.channel = channel;
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_19)) {

            if (event.getPacketType() == PacketType.Play.Server.SYSTEM_CHAT_MESSAGE
                && DependencyUtil.isDependencyLoaded(VIA_VERSION)
            ) {
                Player player = event.getPlayer();
                // 只有当玩家使用1.19之后的客户端通过 ViaVersion 连接早于 1.19 的服务器时
                // 才处理被翻译为 SYSTEM_CHAT_MESSAGE 的旧聊天格式
                // 从聊天信息参数中获取玩家判断是否应该拦截
                if (player.getCurrentServer().isPresent() && Via.proxyPlatform().protocolDetectorService()
                    .serverProtocolVersion(player.getCurrentServer().get().getServerInfo().getName())
                    .olderThan(ProtocolVersion.v1_19)
                ) {
                    WrapperPlayServerSystemChatMessage chatMessage = new WrapperPlayServerSystemChatMessage(event);
                    event.setCancelled(shouldBlock(chatMessage.getMessage()));
                    return;
                }
            }
        }

        if (event.getPacketType() == PacketType.Play.Server.CHAT_MESSAGE) {
            // 玩家聊天信息
            WrapperPlayServerChatMessage chatMessage = new WrapperPlayServerChatMessage(event);

            if (event.getServerVersion().isNewerThanOrEquals(ServerVersion.V_1_16)) {
                ChatMessage_v1_16 message = (ChatMessage_v1_16) chatMessage.getMessage();
                if (channel.getPlugin().getProxy().getPlayer(message.getSenderUUID()).isPresent()) {
                    // 只过滤连接到代理的玩家信息
                    event.setCancelled(true);
                }
            } else if (LOCATION_CHAT_TYPE_CHAT.equals(chatMessage.getMessage().getType().getName())) {
                // 1.16之前: 从聊天信息参数中获取玩家判断是否应该拦截
                event.setCancelled(shouldBlock(chatMessage.getMessage().getChatContent()));
            }
        }
    }

    public boolean shouldBlock(Component message) {
        if (message instanceof TranslatableComponent
            && CHAT_TYPE_TEXT.equals(((TranslatableComponent) message).key())
        ) {
            // 仅处理连接到此代理服务器的玩家信息
            return channel.getPlugin().getProxy().getPlayer(
                ComponentUtils.getSourceUuidFromArguments(((TranslatableComponent) message))
            ).isPresent();
        }
        return false;
    }

    public PacketListenerCommon getCommon() {
        return this.asAbstract(PacketListenerPriority.NORMAL);
    }
}
