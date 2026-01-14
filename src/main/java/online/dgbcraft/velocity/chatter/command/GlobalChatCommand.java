package online.dgbcraft.velocity.chatter.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import online.dgbcraft.velocity.chatter.channel.GlobalChatChannel;

/**
 * @author Sanluli36li
 */
public class GlobalChatCommand {
    private static final String COMMAND_NAME = "g";
    private static final String COMMAND_MESSAGE_ARGUMENT_NODE = "message";
    private final GlobalChatChannel channel;
    BrigadierCommand cmd;
    CommandMeta meta;

    public GlobalChatCommand(GlobalChatChannel channel) {
        this.channel = channel;
    }

    private BrigadierCommand buildCommand() {
        LiteralCommandNode<CommandSource> rootNode = BrigadierCommand.literalArgumentBuilder(COMMAND_NAME)
            .requires(source -> source instanceof Player).build();
        ArgumentCommandNode<CommandSource, String> messageArgumentNode = BrigadierCommand
            .requiredArgumentBuilder(COMMAND_MESSAGE_ARGUMENT_NODE, StringArgumentType.greedyString())
            .executes(context -> {
                String message = StringArgumentType.getString(context, COMMAND_MESSAGE_ARGUMENT_NODE);
                if (context.getSource() instanceof Player) {
                    channel.onPlayerChat((Player) context.getSource(), message);
                    return 1;
                }
                return 0;
            }).build();
        rootNode.addChild(messageArgumentNode);

        return new BrigadierCommand(rootNode);
    }

    public void register() {
        if (cmd == null) {
            cmd = buildCommand();
            meta = channel.getPlugin().getProxy().getCommandManager().metaBuilder(cmd).build();
        }

        channel.getPlugin().getProxy().getCommandManager().register(meta, cmd);
    }

    public void unregister() {
        if (meta != null) {
            channel.getPlugin().getProxy().getCommandManager().unregister(meta);
        }
    }
}