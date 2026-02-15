package com.nickname.plugin.storage;

import com.nickname.plugin.config.PluginConfig;
import com.nickname.plugin.util.MessageUtil;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 * Handles persistent storage of player nicknames.
 */
public class NicknameStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Map<UUID, String> nicknames = new HashMap<>();
    private final Map<UUID, String> originalUsernames = new HashMap<>();
    private final Map<UUID, String> messageColors = new HashMap<>();
    private final Path storageFile;
    private final Path originalsFile;
    private final Path messageColorsFile;
    private final PluginConfig config;

    public NicknameStorage(Path dataFolder, PluginConfig config) {
        this.storageFile = dataFolder.resolve("nicknames.json");
        this.originalsFile = dataFolder.resolve("originals.json");
        this.messageColorsFile = dataFolder.resolve("messagecolors.json");
        this.config = config;
        load();
    }

    public synchronized String getNickname(UUID uuid) {
        return nicknames.get(uuid);
    }

    public synchronized void setNickname(UUID uuid, String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            nicknames.remove(uuid);
        } else {
            nicknames.put(uuid, nickname);
        }
        saveNicknames();
    }

    public synchronized void removeNickname(UUID uuid) {
        nicknames.remove(uuid);
        saveNicknames();
        // Keep originalUsernames — needed if player sets nick again
    }

    public synchronized void removeOriginalUsername(UUID uuid) {
        originalUsernames.remove(uuid);
        saveOriginals();
    }

    public synchronized void setOriginalUsername(UUID uuid, String username) {
        if (!originalUsernames.containsKey(uuid)) {
            originalUsernames.put(uuid, username);
            saveOriginals();
        }
    }

    public synchronized String getOriginalUsername(UUID uuid) {
        return originalUsernames.get(uuid);
    }

    public synchronized boolean hasNickname(UUID uuid) {
        return nicknames.containsKey(uuid);
    }

    public synchronized boolean isNicknameTaken(String plainNickname, UUID excludePlayer) {
        String lower = plainNickname.toLowerCase();
        for (Map.Entry<UUID, String> entry : nicknames.entrySet()) {
            if (entry.getKey().equals(excludePlayer)) continue;
            String existing = MessageUtil.stripTags(entry.getValue()).toLowerCase();
            if (existing.equals(lower)) return true;
        }
        return false;
    }

    public synchronized String getDisplayName(UUID uuid, String defaultName) {
        String nickname = nicknames.get(uuid);
        return nickname != null ? nickname : defaultName;
    }

    // --- Message colors ---

    public synchronized String getMessageColor(UUID uuid) {
        return messageColors.get(uuid);
    }

    public synchronized void setMessageColor(UUID uuid, String color) {
        if (color == null || color.isEmpty()) {
            messageColors.remove(uuid);
        } else {
            messageColors.put(uuid, color);
        }
        saveMessageColors();
    }

    public synchronized void removeMessageColor(UUID uuid) {
        if (messageColors.remove(uuid) != null) {
            saveMessageColors();
        }
    }

    // --- Global display settings (read from config) ---

    public boolean isShowInChat() {
        return config.display.showInChat;
    }

    public boolean isShowOnNameplate() {
        return config.display.showOnNameplate;
    }

    public boolean isShowInTabList() {
        return config.display.showInTabList;
    }

    private void load() {
        loadMap(storageFile, nicknames);
        loadMap(messageColorsFile, messageColors);
        loadMap(originalsFile, originalUsernames);
    }

    private void loadMap(Path file, Map<UUID, String> target) {
        if (!Files.exists(file)) return;
        try (Reader reader = Files.newBufferedReader(file)) {
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> loaded = GSON.fromJson(reader, type);
            if (loaded != null) {
                for (Map.Entry<String, String> entry : loaded.entrySet()) {
                    try {
                        target.put(UUID.fromString(entry.getKey()), entry.getValue());
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        } catch (IOException e) {
            System.err.println("[NicknameChanger] Failed to load " + file.getFileName() + ": " + e.getMessage());
        }
    }

    private synchronized void saveMap(Path file, Map<UUID, String> source) {
        try {
            Files.createDirectories(file.getParent());
            Map<String, String> toSave = new HashMap<>();
            for (Map.Entry<UUID, String> entry : source.entrySet()) {
                toSave.put(entry.getKey().toString(), entry.getValue());
            }
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(toSave, writer);
            }
        } catch (IOException e) {
            System.err.println("[NicknameChanger] Failed to save " + file.getFileName() + ": " + e.getMessage());
        }
    }

    private void saveNicknames() {
        saveMap(storageFile, nicknames);
    }

    private void saveOriginals() {
        saveMap(originalsFile, originalUsernames);
    }

    private void saveMessageColors() {
        saveMap(messageColorsFile, messageColors);
    }
}
