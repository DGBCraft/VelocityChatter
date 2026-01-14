package online.dgbcraft.velocity.chatter.channel;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import online.dgbcraft.velocity.chatter.VelocityChatter;
import online.dgbcraft.velocity.chatter.util.ComponentUtils;

/**
 * @author Sanluli36li
 */
public abstract class BaseChatChannel implements ChatChannel {
    private final VelocityChatter plugin;
    private boolean loaded;

    public BaseChatChannel(VelocityChatter plugin) {
        this.plugin = plugin;
    }


    @Override
    public void broadcast(Component message) {
        for (Player player : plugin.getProxy().getAllPlayers()) {
            if (isPlayerCanVisitChannel(player)) {
                player.sendMessage(message);
            }
        }
    }

    @Override
    public void load() {
        loaded = true;
    }

    @Override
    public void unload() {
        loaded = false;
    }

    @Override
    public boolean isLoaded() {
        return loaded;
    }

    public VelocityChatter getPlugin() {
        return plugin;
    }

    protected Component buildPlayerMessage(Component format, Player player, String message) {
        RegisteredServer server = player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServer()
            : null;

        return format
            .replaceText(ComponentUtils.getMessageReplacement(message))
            .replaceText(ComponentUtils.getPlayerReplacement(
                player,
                plugin.getConfiguration().getGlobalChat().isPlayerTooltip(),
                plugin.getConfiguration().getGlobalChat().isPlayerClickable()
            ))
            .replaceText(ComponentUtils.getServerNameReplacement(
                server,
                plugin.getConfiguration().getGlobalChat().isServerTooltip(),
                plugin.getConfiguration().getGlobalChat().isServerClickable()
            ))
            .replaceText(ComponentUtils.getServerAliasReplacement(
                server,
                plugin.getConfiguration().getServerAlias(server),
                plugin.getConfiguration().getGlobalChat().isServerTooltip(),
                plugin.getConfiguration().getGlobalChat().isServerClickable()
            ));
    }
}
