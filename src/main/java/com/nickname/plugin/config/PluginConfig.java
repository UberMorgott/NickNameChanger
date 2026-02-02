package com.nickname.plugin.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.*;

public class PluginConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public String pluginName = "NicknameChanger";
    public String version = "0.0.7";
    public boolean debugMode = false;
    public String chatFormat = "{prefix}<{username}>{suffix} {message}";
    public Integrations integrations = new Integrations();

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
                if (cfg != null) return cfg;
            } catch (IOException e) {
                System.err.println("[NicknameChanger] Failed to load config: " + e.getMessage());
            }
        }

        return new PluginConfig();
    }
}
