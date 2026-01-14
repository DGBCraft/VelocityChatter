package online.dgbcraft.velocity.chatter.util;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import online.dgbcraft.velocity.manager.api.VelocityManagerAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import static online.dgbcraft.velocity.chatter.constant.LocaleKeys.PLUGIN_HOVER_SERVER;

/**
 * @author Sanluli36li
 */
public class ComponentUtils {
    private static final Key PLAYER_ENTITY = Key.key("minecraft:player");

    private ComponentUtils() {
    }

    private static Component buildPlayerText(Player player, boolean tooltip, boolean clickable) {
        TextComponent text = Component.text(player.getUsername());

        if (DependencyUtil.isDependencyLoaded(DependencyUtil.VELOCITY_MANAGER)) {
            text = text.color(VelocityManagerAPI.getPlayerColor(player));
        }

        if (tooltip) {
            text = text.hoverEvent(HoverEvent.showEntity(
                PLAYER_ENTITY, player.getUniqueId(),
                Component.text(player.getUsername())
            ));
        }
        if (clickable) {
            text = text.clickEvent(ClickEvent.clickEvent(
                ClickEvent.Action.SUGGEST_COMMAND,
                ClickEvent.Payload.string(String.format("/tell %s ", player.getUsername()))
            ));
        }
        return text;
    }

    private static Component buildServerAliasText(RegisteredServer server, String alias, boolean tooltip, boolean clickable) {
        return buildServerText(server, alias, tooltip, clickable);
    }

    private static Component buildServerNameText(RegisteredServer server, boolean tooltip, boolean clickable) {
        return buildServerText(server, server.getServerInfo().getName(), tooltip, clickable);
    }

    private static Component buildServerText(RegisteredServer server, String alias, boolean tooltip, boolean clickable) {
        if (server == null) {
            return Component.text("?").color(NamedTextColor.GRAY);
        }
        TextComponent text = Component.text(alias);

        if (DependencyUtil.isDependencyLoaded(DependencyUtil.VELOCITY_MANAGER)) {
            text = text.color(VelocityManagerAPI.getServerColor(server));
        }

        if (tooltip) {
            text = text.hoverEvent(HoverEvent.showText(Component.translatable(PLUGIN_HOVER_SERVER)
                .arguments(Component.text(server.getServerInfo().getName()))));
        }
        if (clickable) {
            text = text.clickEvent(ClickEvent.clickEvent(
                ClickEvent.Action.RUN_COMMAND,
                ClickEvent.Payload.string(String.format("/server %s", server.getServerInfo().getName()))
            ));
        }
        return text;
    }

    public static Consumer<TextReplacementConfig.Builder> getMessageReplacement(String message) {
        return getMessageReplacement("%message%", message);
    }

    public static Consumer<TextReplacementConfig.Builder> getMessageReplacement(String placeholder, String message) {
        return builder -> builder.matchLiteral(placeholder).replacement(message);
    }

    public static Consumer<TextReplacementConfig.Builder> getDayCountReplacement(int day) {
        return getDayCountReplacement("%day%", day);
    }

    public static Consumer<TextReplacementConfig.Builder> getDayCountReplacement(String placeholder, int day) {
        return builder -> builder.matchLiteral(placeholder).replacement(Component.text(day));
    }

    public static Consumer<TextReplacementConfig.Builder> getPlayerReplacement(Player player, boolean tooltip, boolean clickable) {
        return getPlayerReplacement("%player%", player, tooltip, clickable);
    }

    public static Consumer<TextReplacementConfig.Builder> getPlayerReplacement(String placeholder, Player player, boolean tooltip, boolean clickable) {
        return builder -> builder.matchLiteral(placeholder).replacement(buildPlayerText(player, tooltip, clickable));
    }

    public static Consumer<TextReplacementConfig.Builder> getServerNameReplacement(RegisteredServer server, boolean tooltip, boolean clickable) {
        return getServerNameReplacement("%server%", server, tooltip, clickable);
    }

    public static Consumer<TextReplacementConfig.Builder> getServerNameReplacement(String placeholder, RegisteredServer server, boolean tooltip, boolean clickable) {
        return builder -> builder.matchLiteral(placeholder).replacement(buildServerNameText(server, tooltip, clickable));
    }

    public static Consumer<TextReplacementConfig.Builder> getServerAliasReplacement(RegisteredServer server, String alias, boolean tooltip, boolean clickable) {
        return getServerAliasReplacement("%serverAlias%", server, alias, tooltip, clickable);
    }

    public static Consumer<TextReplacementConfig.Builder> getServerAliasReplacement(String placeholder, RegisteredServer server, String alias, boolean tooltip, boolean clickable) {
        return builder -> builder.matchLiteral(placeholder).replacement(buildServerAliasText(server, alias, tooltip, clickable));
    }

    public static @Nullable UUID getSourceUuidFromArguments(@NotNull TranslatableComponent component) {
        if (!component.arguments().isEmpty()) {
            return ComponentUtils.getSourceUuidFromHoverEvent(component.arguments().getFirst().asComponent());
        } else {
            return null;
        }
    }

    public static @Nullable UUID getSourceUuidFromHoverEvent(@NotNull Component component) {
        HoverEvent<?> hover = component.hoverEvent();

        if (hover != null && hover.action() == HoverEvent.Action.SHOW_ENTITY
            && Objects.equals(((HoverEvent.ShowEntity) hover.value()).type(), PLAYER_ENTITY)) {
            return ((HoverEvent.ShowEntity) hover.value()).id();
        }
        return null;
    }
}