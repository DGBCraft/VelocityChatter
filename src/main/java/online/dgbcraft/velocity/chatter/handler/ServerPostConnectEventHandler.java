package online.dgbcraft.velocity.chatter.handler;

import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import online.dgbcraft.velocity.chatter.VelocityChatter;
import online.dgbcraft.velocity.chatter.util.ComponentUtils;

import java.util.function.Consumer;

/**
 * @author Sanluli36li
 */
public class ServerPostConnectEventHandler implements EventHandler<ServerPostConnectEvent> {
    VelocityChatter plugin;

    public ServerPostConnectEventHandler(VelocityChatter plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(ServerPostConnectEvent event) {
        // joinMotD
        if (plugin.getConfiguration().getJoinMotd().isEnable()) {

            if (event.getPlayer().getCurrentServer().isPresent()) {
                RegisteredServer server = event.getPlayer().getCurrentServer().get().getServer();

                Consumer<TextReplacementConfig.Builder> dayCountReplacement = ComponentUtils
                    .getDayCountReplacement(plugin.getConfiguration().getDayCount());
                Consumer<TextReplacementConfig.Builder> playerNameReplacement = ComponentUtils
                    .getPlayerReplacement(event.getPlayer(), false, false);
                Consumer<TextReplacementConfig.Builder> serverNameReplacement = ComponentUtils
                    .getServerNameReplacement(server, false, false);
                Consumer<TextReplacementConfig.Builder> serverAliasReplacement = ComponentUtils
                    .getServerAliasReplacement(server, plugin.getConfiguration().getServerAlias(server), false, false);

                if (plugin.getConfiguration().getJoinMotd().isPerSwitchServer() || event.getPreviousServer() == null) {
                    for (Component component : plugin.getConfiguration().getJoinMotd().getMessages(event.getPlayer())) {
                        event.getPlayer().sendMessage(component
                            .replaceText(dayCountReplacement)
                            .replaceText(playerNameReplacement)
                            .replaceText(serverNameReplacement)
                            .replaceText(serverAliasReplacement)
                        );
                    }
                }
            }
        }
    }
}
