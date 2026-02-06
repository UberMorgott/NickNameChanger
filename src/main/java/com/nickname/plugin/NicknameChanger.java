package com.nickname.plugin;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.event.EventPriority;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;

import com.nickname.plugin.commands.NickCommand;
import com.nickname.plugin.config.PluginConfig;
import com.nickname.plugin.hooks.LuckPermsHook;
import com.nickname.plugin.listeners.ChatListener;
import com.nickname.plugin.listeners.PlayerListener;
import com.nickname.plugin.storage.NicknameStorage;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public class NicknameChanger extends JavaPlugin {

    private NicknameStorage storage;
    private ChatListener chatListener;
    private PlayerListener playerListener;

    public NicknameChanger(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        Path dataFolder = getDataDirectory();
        PluginConfig config = PluginConfig.load(dataFolder);

        this.storage = new NicknameStorage(dataFolder);

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
            LuckPermsHook.init();
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
}
