package online.dgbcraft.velocity.chatter.channel;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;

/**
 * @author Sanluli36li
 */
public interface ChatChannel {

    boolean isPlayerCanVisitChannel(Player player);

    boolean isPlayerCanSendMessage(Player player);

    void load();

    void unload();

    boolean isLoaded();

    void broadcast(Component component);

    void onPlayerChat(Player player, String message);

    default boolean shouldEnable() {
        return true;
    }
}