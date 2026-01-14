package online.dgbcraft.velocity.chatter.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.VelocityBrigadierMessage;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import online.dgbcraft.velocity.chatter.VelocityChatter;
import online.dgbcraft.velocity.chatter.util.ComponentUtils;

import java.util.Optional;
import java.util.function.Consumer;

import static online.dgbcraft.velocity.chatter.constant.LocaleKeys.ARGUMENT_ENTITY_NOT_FOUND_PLAYER;

/**
 * @author Sanluli36li
 */
public class TellCommand {
    private static final String COMMAND_NAME = "tell";
    private static final String COMMAND_ALIAS_MSG = "msg";
    private static final String COMMAND_ALIAS_W = "w";
    private static final String COMMAND_PLAYER_ARGUMENT_NODE = "target";
    private static final String COMMAND_MESSAGE_ARGUMENT_NODE = "message";
    private static final SimpleCommandExceptionType PLAYER_NOT_FOUNT_EXCEPTION = new SimpleCommandExceptionType(VelocityBrigadierMessage.tooltip(Component.translatable(ARGUMENT_ENTITY_NOT_FOUND_PLAYER)));
    private final VelocityChatter plugin;
    BrigadierCommand cmd;
    CommandMeta meta;
    private Component toMessage;
    private Component fromMessage;

    public TellCommand(VelocityChatter plugin) {
        this.plugin = plugin;
    }

    public void load() {
        if (plugin.getConfiguration().getGlobalTell().isEnable()) {
            toMessage = plugin.getConfiguration().getGlobalTell().getToMessageFormat();
            fromMessage = plugin.getConfiguration().getGlobalTell().getFromMessageFormat();

            register();
        }
    }

    public void unload() {
        unregister();
    }

    private BrigadierCommand buildCommand() {
        LiteralCommandNode<CommandSource> rootNode = BrigadierCommand.literalArgumentBuilder(COMMAND_NAME).build();
        ArgumentCommandNode<CommandSource, String> targetArgumentNode = BrigadierCommand
            .requiredArgumentBuilder(COMMAND_PLAYER_ARGUMENT_NODE, StringArgumentType.word())
            .suggests((context, builder) -> {
                plugin.getProxy().getAllPlayers().forEach(player -> {
                    if (context.getSource() != player) {
                        builder.suggest(player.getUsername());
                    }
                });
                return builder.buildFuture();
            })
            .build();
        ArgumentCommandNode<CommandSource, String> messageArgumentNode = BrigadierCommand
            .requiredArgumentBuilder(COMMAND_MESSAGE_ARGUMENT_NODE, StringArgumentType.greedyString())
            .executes(context ->
                tell(
                    context.getSource(),
                    StringArgumentType.getString(context, COMMAND_PLAYER_ARGUMENT_NODE),
                    StringArgumentType.getString(context, COMMAND_MESSAGE_ARGUMENT_NODE)
                ))
            .build();
        targetArgumentNode.addChild(messageArgumentNode);
        rootNode.addChild(targetArgumentNode);

        return new BrigadierCommand(rootNode);
    }

    public void register() {
        if (cmd == null) {
            cmd = buildCommand();
            meta = plugin.getProxy().getCommandManager().metaBuilder(cmd)
                .aliases(COMMAND_ALIAS_W, COMMAND_ALIAS_MSG).build();
        }

        plugin.getProxy().getCommandManager().register(meta, cmd);
    }

    public void unregister() {
        if (meta != null) {
            plugin.getProxy().getCommandManager().unregister(meta);
        }
    }

    private int tell(CommandSource source, String playerName, String message) throws CommandSyntaxException {
        Consumer<TextReplacementConfig.Builder> fromPlayerNameReplacement;
        Optional<Player> op = plugin.getProxy().getPlayer(playerName);
        if (op.isPresent()) {
            Player player = op.get();
            Consumer<TextReplacementConfig.Builder> toPlayerNameReplacement = ComponentUtils
                .getPlayerReplacement(
                    player,
                    plugin.getConfiguration().getGlobalTell().isPlayerTooltip(),
                    plugin.getConfiguration().getGlobalTell().isPlayerClickable()
                );
            if (source instanceof Player) {
                fromPlayerNameReplacement = ComponentUtils.getPlayerReplacement(
                    (Player) source,
                    plugin.getConfiguration().getGlobalTell().isPlayerTooltip(),
                    plugin.getConfiguration().getGlobalTell().isPlayerClickable()
                );
            } else {
                fromPlayerNameReplacement = ComponentUtils.getMessageReplacement("%player%", "_SERVER_");
            }
            Consumer<TextReplacementConfig.Builder> messageReplacement = ComponentUtils.getMessageReplacement(message);
            source.sendMessage(toMessage.replaceText(toPlayerNameReplacement).replaceText(messageReplacement));
            player.sendMessage(fromMessage.replaceText(fromPlayerNameReplacement).replaceText(messageReplacement));
            return 1;
        }
        throw PLAYER_NOT_FOUNT_EXCEPTION.create();
    }
}