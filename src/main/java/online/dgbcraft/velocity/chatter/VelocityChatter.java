package online.dgbcraft.velocity.chatter;

import com.github.retrooper.packetevents.PacketEvents;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import io.github.retrooper.packetevents.velocity.factory.VelocityPacketEventsBuilder;
import online.dgbcraft.velocity.chatter.channel.ChatChannel;
import online.dgbcraft.velocity.chatter.channel.GlobalChatChannel;
import online.dgbcraft.velocity.chatter.channel.PlayerNotifyChannel;
import online.dgbcraft.velocity.chatter.command.TellCommand;
import online.dgbcraft.velocity.chatter.command.VChatterCommand;
import online.dgbcraft.velocity.chatter.handler.PlayerAvailableCommandsEventHandler;
import online.dgbcraft.velocity.chatter.handler.ServerPostConnectEventHandler;
import online.dgbcraft.velocity.chatter.util.DependencyUtil;
import online.dgbcraft.velocity.chatter.util.L10nUtil;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Sanluli36li
 */
@Plugin(
    id = "velocitychatter",
    name = "VelocityChatter",
    version = BuildConstants.VERSION,
    url = "https://github.com/DGBCraft/VelocityChatter",
    authors = {"Sanluli36li"},
    dependencies = {
        @Dependency(id = "velocitymanager", optional = true),
        @Dependency(id = "packetevents", optional = true)
    }
)
public class VelocityChatter {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataPath;
    private final Path configPath;
    Map<String, ChatChannel> channels = new HashMap<>();
    private TellCommand globalTell;
    private Config configuration;

    @Inject
    public VelocityChatter(ProxyServer proxy, Logger logger, @DataDirectory Path dataDir) {
        this.proxy = proxy;
        this.logger = logger;
        dataPath = dataDir;
        configPath = dataPath.resolve("config.json");

        channels.put("globalChat", new GlobalChatChannel(this));
        channels.put("playerNotify", new PlayerNotifyChannel(this));
    }

    public ProxyServer getProxy() {
        return proxy;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataPath() {
        return dataPath;
    }

    public Config getConfiguration() {
        return configuration;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        L10nUtil.registerTranslations();
        globalTell = new TellCommand(this);

        if (DependencyUtil.isDependencyLoaded(DependencyUtil.PACKET_EVENTS)) {
            PacketEvents.setAPI(VelocityPacketEventsBuilder.build(
                proxy,
                proxy.getPluginManager().ensurePluginContainer(this),
                logger,
                dataPath
            ));
            PacketEvents.getAPI().load();
            PacketEvents.getAPI().init();
        }

        load();

        new VChatterCommand(this).register();
        proxy.getEventManager()
            .register(this, PlayerAvailableCommandsEvent.class, new PlayerAvailableCommandsEventHandler(this));
        proxy.getEventManager().register(this, ServerPostConnectEvent.class, new ServerPostConnectEventHandler(this));
    }

    public void load() {
        try {
            if (!Files.exists(dataPath)) {
                Files.createDirectories(dataPath);
            }
            if (Files.exists(configPath)) {
                try (BufferedReader bufferedReader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                    configuration = GSON.fromJson(bufferedReader, Config.class);
                } catch (Throwable e) {
                    logger.warn("Read config FAIL! Use default config", e);
                    configuration = Config.defaultConfig();
                }
            } else {
                configuration = saveDefaultConfig();
            }

        } catch (IOException e2) {
            logger.warn("Unable create data directory!", e2);
            logger.warn("Velocity Chatter Initialize fail!");
        }

        for (ChatChannel channel : channels.values()) {
            if (channel.shouldEnable()) {
                channel.load();
            }
        }

        if (configuration.getGlobalTell().isEnable()) {
            globalTell.load();
        }

    }

    public void unload() {
        for (ChatChannel channel : channels.values()) {
            channel.unload();
        }

        globalTell.unload();
    }

    private Config saveDefaultConfig() {
        Config config = Config.defaultConfig();
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
            GSON.toJson(config, bufferedWriter);
        } catch (Throwable e) {
            logger.warn("Can't create default config.json!", e);
        }
        return config;
    }


}