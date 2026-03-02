package com.nickname.plugin.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class PluginConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public String pluginName = "NicknameChanger";
    public String version = "0.0.12";
    public boolean debugMode = false;
    public String chatFormat = "{prefix}<{username}>{suffix} {message}";
    public DisplayConfig display = new DisplayConfig();
    public NicknameRules nicknames = new NicknameRules();
    public Integrations integrations = new Integrations();

    public static class DisplayConfig {
        public boolean showInChat = true;
        public boolean showOnNameplate = true;
        public boolean showInTabList = true;
        public boolean showOnMap = false;
    }

    public static class NicknameRules {
        public int minLength = 2;
        public int maxLength = 32;
        public boolean allowCyrillic = true;
        public boolean allowUnicode = false;
        public boolean uniqueNicknames = true;
        public List<String> bannedWords = new ArrayList<>(List.of("admin", "moderator", "server", "owner"));
    }

    public static class Integrations {
        public LuckPermsConfig luckperms = new LuckPermsConfig();
    }

    public static class LuckPermsConfig {
        public boolean enabled = true;
        public boolean showPrefix = true;
        public boolean showSuffix = true;
    }

    public static PluginConfig load(Path dataFolder) {
        Path configFile = dataFolder.resolve("config.json");

        if (!Files.exists(configFile)) {
            try (InputStream in = PluginConfig.class.getClassLoader().getResourceAsStream("config.json")) {
                if (in != null) {
                    Files.createDirectories(dataFolder);
                    Files.copy(in, configFile);
                }
            } catch (IOException e) {
                System.err.println("[NicknameChanger] Failed to copy default config: " + e.getMessage());
            }
        }

        if (Files.exists(configFile)) {
            try (Reader reader = Files.newBufferedReader(configFile)) {
                PluginConfig cfg = GSON.fromJson(reader, PluginConfig.class);
                if (cfg != null) {
                    if (cfg.display == null) cfg.display = new DisplayConfig();
                    if (cfg.nicknames == null) cfg.nicknames = new NicknameRules();
                    if (cfg.nicknames.bannedWords == null) cfg.nicknames.bannedWords = new ArrayList<>();
                    if (cfg.integrations == null) cfg.integrations = new Integrations();
                    if (cfg.integrations.luckperms == null) cfg.integrations.luckperms = new LuckPermsConfig();
                    return cfg;
                }
            } catch (IOException e) {
                System.err.println("[NicknameChanger] Failed to load config: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("[NicknameChanger] Corrupted config file: " + e.getMessage());
                // Don't crash — use defaults
            }
        }

        return new PluginConfig();
    }

    public void save(Path dataFolder) {
        Path configFile = dataFolder.resolve("config.json");
        try {
            Files.createDirectories(dataFolder);
            try (Writer writer = Files.newBufferedWriter(configFile)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            System.err.println("[NicknameChanger] Failed to save config: " + e.getMessage());
        }
    }
}
