package com.nickname.plugin;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;

import com.nickname.plugin.commands.NickCommand;
import com.nickname.plugin.hooks.LuckPermsHook;
import com.nickname.plugin.hooks.TinyMessageHook;
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
        this.storage = new NicknameStorage(dataFolder);

        // Initialize optional integrations
        LuckPermsHook.init();
        TinyMessageHook.init();

        this.chatListener = new ChatListener(storage);
        this.playerListener = new PlayerListener(storage);

        getCommandRegistry().registerCommand(new NickCommand(storage));
        getEventRegistry().registerGlobal(PlayerChatEvent.class, chatListener::onPlayerChat);
        getEventRegistry().registerGlobal(PlayerReadyEvent.class, playerListener::onPlayerReady);
    }

    @Override
    protected void start() {
        // Plugin enabled
    }

    @Override
    protected void shutdown() {
        // Plugin disabled
    }

    public NicknameStorage getStorage() {
        return storage;
    }
}
