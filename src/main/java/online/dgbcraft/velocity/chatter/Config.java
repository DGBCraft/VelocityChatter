package online.dgbcraft.velocity.chatter;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.net.InetSocketAddress;
import java.util.*;

import static online.dgbcraft.velocity.chatter.constant.LocaleKeys.PLUGIN_COMMAND_MESSAGE_TELL_FROM;
import static online.dgbcraft.velocity.chatter.constant.LocaleKeys.PLUGIN_COMMAND_MESSAGE_TELL_TO;

/* loaded from: Config.class */
public class Config {

    private long openTimestamp = System.currentTimeMillis() / 1000;
    private GlobalChatConfig globalChat = new GlobalChatConfig();
    private GlobalTellConfig globalTell = new GlobalTellConfig();
    private PlayerNotifyConfig playerNotify = new PlayerNotifyConfig();
    private JoinMotdConfig joinMotd = new JoinMotdConfig();
    private Map<String, String> serverAliases = new HashMap<>();
    private Set<String> blockCommandSuggests = Sets.newHashSet("viaver", "viaversion", "vvvelocity", "vmanager", "vchatter");

    public static Config defaultConfig() {
        Config config = new Config();
        config.globalChat.messageFormat = GsonComponentSerializer.gson().serializeToTree(Component.text("[%server%]", TextColor.color(16777045)).append(Component.text("<%player%> %message%", TextColor.color(16777215))));
        config.globalTell.toMessageFormat = GsonComponentSerializer.gson().serializeToTree(Component.translatable(PLUGIN_COMMAND_MESSAGE_TELL_TO, TextColor.color(16733695)).append(Component.text("<%player%> %message%")));
        config.globalTell.fromMessageFormat = GsonComponentSerializer.gson().serializeToTree(Component.translatable(PLUGIN_COMMAND_MESSAGE_TELL_FROM, TextColor.color(16733695)).append(Component.text("<%player%> %message%")));
        config.playerNotify.transferMessageFormat = GsonComponentSerializer.gson().serializeToTree(Component.text("%player%: ", TextColor.color(16777215)).append(Component.text("%preServer%", TextColor.color(16777045))).append(Component.text(" -> ")).append(Component.text("%server%", TextColor.color(16777045))));
        config.playerNotify.joinMessageFormat = GsonComponentSerializer.gson().serializeToTree(Component.text("%player%: ", TextColor.color(16777215)).append(Component.text("*", TextColor.color(11184810))).append(Component.text(" -> ")).append(Component.text("%server%", TextColor.color(16777045))));
        config.playerNotify.leaveMessageFormat = GsonComponentSerializer.gson().serializeToTree(Component.text("%player%: ", TextColor.color(16777215)).append(Component.text("%preServer%", TextColor.color(16777045))).append(Component.text(" -> ")).append(Component.text("Disconnect", TextColor.color(11184810))));
        config.joinMotd.messages.add(VelocityChatter.GSON.toJsonTree("Welcome! You are in %server% now!"));
        return config;
    }

    public long getOpenTimestamp() {
        return openTimestamp;
    }

    public int getDayCount() {
        return ((int) (((System.currentTimeMillis() / 1000) - openTimestamp) / 86400)) + 1;
    }

    public GlobalChatConfig getGlobalChat() {
        return globalChat;
    }

    public GlobalTellConfig getGlobalTell() {
        return globalTell;
    }

    public PlayerNotifyConfig getPlayerNotify() {
        return playerNotify;
    }

    public JoinMotdConfig getJoinMotd() {
        return joinMotd;
    }

    public Map<String, String> getServerAliases() {
        return serverAliases;
    }

    public String getServerAlias(String serverName) {
        return serverAliases.getOrDefault(serverName, serverName);
    }

    public String getServerAlias(RegisteredServer server) {
        return getServerAlias(server.getServerInfo().getName());
    }

    public Set<String> getBlockCommandSuggests() {
        return blockCommandSuggests;
    }

    public enum GlobalChatMode {
        FORWARD,
        BROADCAST,
        COMMAND
    }

    public static class GlobalChatConfig {
        private boolean enable = true;
        private Config.GlobalChatMode chatMode = Config.GlobalChatMode.FORWARD;
        private boolean playerClickable = true;
        private boolean playerTooltip = true;
        private boolean serverClickable = true;
        private boolean serverTooltip = true;
        private boolean ignoreMcdrCommand = false;
        private boolean mcdrCompatible = false;
        private JsonElement messageFormat;

        public boolean isEnable() {
            return true;
        }

        public Config.GlobalChatMode getChatMode() {
            return chatMode;
        }

        public boolean isPlayerClickable() {
            return playerClickable;
        }

        public boolean isPlayerTooltip() {
            return playerTooltip;
        }

        public boolean isServerClickable() {
            return serverClickable;
        }

        public boolean isServerTooltip() {
            return serverTooltip;
        }

        public boolean isIgnoreMcdrCommand() {
            return ignoreMcdrCommand;
        }

        public boolean isMcdrCompatible() {
            return mcdrCompatible;
        }

        public Component getMessageFormat() {
            return GsonComponentSerializer.gson().deserializeFromTree(messageFormat);
        }
    }

    public static class GlobalTellConfig {
        private boolean enable = true;
        private boolean playerClickable = true;
        private boolean playerTooltip = true;
        private JsonElement toMessageFormat;
        private JsonElement fromMessageFormat;

        public boolean isEnable() {
            return enable;
        }

        public boolean isPlayerClickable() {
            return playerClickable;
        }

        public boolean isPlayerTooltip() {
            return playerTooltip;
        }

        public Component getToMessageFormat() {
            return GsonComponentSerializer.gson().deserializeFromTree(toMessageFormat);
        }

        public Component getFromMessageFormat() {
            return GsonComponentSerializer.gson().deserializeFromTree(fromMessageFormat);
        }
    }

    public static class JoinMotdConfig {
        private boolean enable = true;
        private boolean perSwitchServer = true;
        private List<JsonElement> messages = new ArrayList<>();
        private Map<String, List<JsonElement>> forceHosts = new HashMap<>();
        private Map<String, List<JsonElement>> servers = new HashMap<>();

        public Map<String, List<JsonElement>> getForceHosts() {
            return forceHosts;
        }

        public Map<String, List<JsonElement>> getServers() {
            return servers;
        }

        public boolean isEnable() {
            return enable;
        }

        public boolean isPerSwitchServer() {
            return perSwitchServer;
        }

        public List<Component> getMessages(Player player) {
            return getMessages(
                player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServer() : null,
                player.getVirtualHost().isPresent() ? player.getVirtualHost().get() : null
            );
        }

        public List<Component> getMessages(RegisteredServer server, InetSocketAddress host) {
            List<JsonElement> jsons;
            if (server != null && servers.containsKey(server.getServerInfo().getName())) {
                jsons = servers.get(server.getServerInfo().getName());
            } else if (host != null && forceHosts.containsKey(host.getHostName())) {
                jsons = forceHosts.get(host.getHostName());
            } else {
                jsons = messages;
            }

            List<Component> components = new ArrayList<>();
            for (JsonElement json : jsons) {
                components.add(GsonComponentSerializer.gson().deserializeFromTree(json));
            }
            return components;
        }
    }

    public static class PlayerNotifyConfig {
        private boolean enable = true;
        private boolean blockBackendServer = true;
        private boolean playerClickable = true;
        private boolean playerTooltip = true;
        private boolean serverClickable = true;
        private boolean serverTooltip = true;
        private JsonElement transferMessageFormat;
        private JsonElement joinMessageFormat;
        private JsonElement leaveMessageFormat;

        public boolean isEnable() {
            return enable;
        }

        // 阻止后端服务器的加入/离开通知, 需要PacketEvents插件
        public boolean isBlockBackendServer() {
            return blockBackendServer;
        }

        public boolean isPlayerClickable() {
            return playerClickable;
        }

        public boolean isPlayerTooltip() {
            return playerTooltip;
        }

        public boolean isServerClickable() {
            return serverClickable;
        }

        public boolean isServerTooltip() {
            return serverTooltip;
        }

        public Component getTransferMessageFormat() {
            return GsonComponentSerializer.gson().deserializeFromTree(transferMessageFormat);
        }

        public Component getJoinMessageFormat() {
            return GsonComponentSerializer.gson().deserializeFromTree(joinMessageFormat);
        }

        public Component getLeaveMessageFormat() {
            return GsonComponentSerializer.gson().deserializeFromTree(leaveMessageFormat);
        }

    }
}