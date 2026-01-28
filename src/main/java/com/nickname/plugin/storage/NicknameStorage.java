package com.nickname.plugin.storage;

import java.io.*;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

/**
 * Handles persistent storage of player nicknames.
 */
public class NicknameStorage {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private final Map<UUID, String> nicknames = new ConcurrentHashMap<>();
    private final Map<UUID, String> originalUsernames = new ConcurrentHashMap<>();
    private final Path storageFile;
    private final Path originalsFile;

    public NicknameStorage(Path dataFolder) {
        this.storageFile = dataFolder.resolve("nicknames.json");
        this.originalsFile = dataFolder.resolve("originals.json");
        load();
    }

    public String getNickname(UUID uuid) {
        return nicknames.get(uuid);
    }

    public void setNickname(UUID uuid, String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            nicknames.remove(uuid);
        } else {
            nicknames.put(uuid, nickname);
        }
        save();
    }

    public void removeNickname(UUID uuid) {
        nicknames.remove(uuid);
        originalUsernames.remove(uuid);
        save();
    }

    public void setOriginalUsername(UUID uuid, String username) {
        if (!originalUsernames.containsKey(uuid)) {
            originalUsernames.put(uuid, username);
            save();
        }
    }

    public String getOriginalUsername(UUID uuid) {
        return originalUsernames.get(uuid);
    }

    public boolean hasNickname(UUID uuid) {
        return nicknames.containsKey(uuid);
    }

    public String getDisplayName(UUID uuid, String defaultName) {
        String nickname = nicknames.get(uuid);
        return nickname != null ? nickname : defaultName;
    }

    private void load() {
        // Load nicknames
        if (Files.exists(storageFile)) {
            try (Reader reader = Files.newBufferedReader(storageFile)) {
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, String> loaded = GSON.fromJson(reader, type);
                if (loaded != null) {
                    for (Map.Entry<String, String> entry : loaded.entrySet()) {
                        try {
                            UUID uuid = UUID.fromString(entry.getKey());
                            nicknames.put(uuid, entry.getValue());
                        } catch (IllegalArgumentException ignored) {}
                    }
                }
            } catch (IOException e) {
                System.err.println("[NicknameChanger] Failed to load nicknames: " + e.getMessage());
            }
        }

        // Load original usernames
        if (Files.exists(originalsFile)) {
            try (Reader reader = Files.newBufferedReader(originalsFile)) {
                Type type = new TypeToken<Map<String, String>>(){}.getType();
                Map<String, String> loaded = GSON.fromJson(reader, type);
                if (loaded != null) {
                    for (Map.Entry<String, String> entry : loaded.entrySet()) {
                        try {
                            UUID uuid = UUID.fromString(entry.getKey());
                            originalUsernames.put(uuid, entry.getValue());
                        } catch (IllegalArgumentException ignored) {}
                    }
                }
            } catch (IOException e) {
                System.err.println("[NicknameChanger] Failed to load original usernames: " + e.getMessage());
            }
        }
    }

    private void save() {
        try {
            Files.createDirectories(storageFile.getParent());

            // Save nicknames
            Map<String, String> toSave = new ConcurrentHashMap<>();
            for (Map.Entry<UUID, String> entry : nicknames.entrySet()) {
                toSave.put(entry.getKey().toString(), entry.getValue());
            }
            try (Writer writer = Files.newBufferedWriter(storageFile)) {
                GSON.toJson(toSave, writer);
            }

            // Save original usernames
            Map<String, String> originalsToSave = new ConcurrentHashMap<>();
            for (Map.Entry<UUID, String> entry : originalUsernames.entrySet()) {
                originalsToSave.put(entry.getKey().toString(), entry.getValue());
            }
            try (Writer writer = Files.newBufferedWriter(originalsFile)) {
                GSON.toJson(originalsToSave, writer);
            }
        } catch (IOException e) {
            System.err.println("[NicknameChanger] Failed to save data: " + e.getMessage());
        }
    }
}
