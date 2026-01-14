package online.dgbcraft.velocity.chatter.handler;

import com.velocitypowered.api.event.EventHandler;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import online.dgbcraft.velocity.chatter.VelocityChatter;

/**
 * @author Sanluli36li
 */
public class PlayerAvailableCommandsEventHandler implements EventHandler<PlayerAvailableCommandsEvent> {
    VelocityChatter plugin;

    public PlayerAvailableCommandsEventHandler(VelocityChatter plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(PlayerAvailableCommandsEvent event) {
        event.getRootNode().getChildren().removeIf(
            node -> plugin.getConfiguration().getBlockCommandSuggests().contains(node.getName())
        );
    }
}
