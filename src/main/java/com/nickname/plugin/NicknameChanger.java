package com.nickname.plugin;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;

import com.nickname.plugin.api.NicknameAPI;
import com.nickname.plugin.commands.NickCommand;
import com.nickname.plugin.config.PluginConfig;
import com.nickname.plugin.hooks.LuckPermsHook;
import com.nickname.plugin.listeners.ChatListener;
import com.nickname.plugin.listeners.PlayerListener;
import com.nickname.plugin.storage.NicknameStorage;
import com.nickname.plugin.util.PlayerRefUtil;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public class NicknameChanger extends JavaPlugin {

    private static NicknameChanger instance;

    private NicknameStorage storage;
    private PluginConfig config;
    private Path dataFolder;
    private ChatListener chatListener;
    private PlayerListener playerListener;

    public NicknameChanger(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    public static NicknameChanger getInstance() {
        return instance;
    }

    @Override
    protected void setup() {
        this.dataFolder = getDataDirectory();
        Path dataFolder = this.dataFolder;
        this.config = PluginConfig.load(dataFolder);
        PlayerRefUtil.init();

        this.storage = new NicknameStorage(dataFolder, config);
        NicknameAPI.init(storage);

        this.chatListener = new ChatListener(storage, config);
        this.playerListener = new PlayerListener(storage, config);

        getCommandRegistry().registerCommand(new NickCommand(storage, config));
        getEventRegistry().registerGlobal(EventPriority.LAST, PlayerChatEvent.class, chatListener::onPlayerChat);
        getEventRegistry().registerGlobal(PlayerReadyEvent.class, playerListener::onPlayerReady);
    }

    @Override
    protected void start() {
        // Initialize optional integrations after all plugins are enabled
        try {
            LuckPermsHook.init(config.integrations.luckperms.enabled);
        } catch (NoClassDefFoundError e) {
            System.out.println("[NicknameChanger] LuckPerms not found, running without it.");
        }
    }

    @Override
    protected void shutdown() {
        // Plugin disabled
    }

    public NicknameStorage getStorage() {
        return storage;
    }

    public PluginConfig getConfig() {
        return config;
    }

    public Path getDataFolder() {
        return dataFolder;
    }
}
