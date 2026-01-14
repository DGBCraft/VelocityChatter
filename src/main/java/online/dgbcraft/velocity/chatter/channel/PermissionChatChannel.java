package online.dgbcraft.velocity.chatter.channel;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import online.dgbcraft.velocity.chatter.VelocityChatter;
import online.dgbcraft.velocity.chatter.util.ComponentUtils;

public class PermissionChatChannel extends BaseChatChannel{
    private final String command;
    private final String visitPermission;
    private final String sendPermission;
    private final Component messageFormat;

    public PermissionChatChannel(VelocityChatter plugin, Component messageFormat, String command, String permission) {
        this(plugin, messageFormat, command, permission, permission);
    }

    public PermissionChatChannel(VelocityChatter plugin, Component messageFormat, String command, String visitPermission, String sendPermission) {
        super(plugin);
        this.command = command;
        this.visitPermission = visitPermission;
        this.sendPermission = sendPermission;
        this.messageFormat = messageFormat;
    }

    @Override
    public boolean isPlayerCanVisitChannel(Player player) {
        // 发送权限应当包含查看权限, 避免玩家能往此频道发信息, 但自己看不见的情况
        return player.hasPermission(visitPermission) || player.hasPermission(sendPermission);
    }

    @Override
    public boolean isPlayerCanSendMessage(Player player) {
        return player.hasPermission(sendPermission);
    }

    @Override
    public void onPlayerChat(Player player, String message) {
        broadcast(buildPlayerMessage(messageFormat, player, message));
    }


}
