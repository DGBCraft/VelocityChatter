package online.dgbcraft.velocity.chatter.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import online.dgbcraft.velocity.chatter.VelocityChatter;

import static online.dgbcraft.velocity.chatter.constant.PermissionKeys.PERMISSION_PLUGIN_COMMAND_PLUGIN;

/**
 * @author Sanluli36li
 */
public class VChatterCommand {
    private static final String COMMAND_NAME = "vchatter";
    private static final String COMMAND_RELOAD_NODE = "reload";
    private final VelocityChatter plugin;

    public VChatterCommand(VelocityChatter plugin) {
        this.plugin = plugin;
    }

    public void register() {
        // 构建命令结构
        LiteralCommandNode<CommandSource> rootNode = BrigadierCommand.literalArgumentBuilder(COMMAND_NAME)
            .requires(source -> source.hasPermission(PERMISSION_PLUGIN_COMMAND_PLUGIN)).build();

        LiteralCommandNode<CommandSource> reloadNode = BrigadierCommand.literalArgumentBuilder(COMMAND_RELOAD_NODE)
            .executes(commandContext -> {
                plugin.unload();
                plugin.load();
                commandContext.getSource().sendMessage(Component.text("reloaded!"));
                return 1;
            }).build();
        rootNode.addChild(reloadNode);

        // 注册命令
        BrigadierCommand cmd = new BrigadierCommand(rootNode);
        CommandMeta meta = plugin.getProxy().getCommandManager().metaBuilder(cmd).build();
        plugin.getProxy().getCommandManager().register(meta, cmd);
    }
}
