package com.nickname.plugin.util;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.logging.Level;

public class PlayerRefUtil {

    private static final HytaleLogger LOGGER = HytaleLogger.get("NicknameChanger");

    private static Field usernameField;
    private static boolean available = false;

    public static void init() {
        try {
            usernameField = PlayerRef.class.getDeclaredField("username");
            usernameField.setAccessible(true);
            if (usernameField.getType() != String.class) {
                LOGGER.at(Level.WARNING).log("PlayerRef.username field type changed, map display disabled.");
                available = false;
                return;
            }
            available = true;
            LOGGER.at(Level.INFO).log("PlayerRef reflection initialized.");
        } catch (NoSuchFieldException | SecurityException e) {
            available = false;
            LOGGER.at(Level.SEVERE).withCause(e).log("Failed to access PlayerRef.username field");
            LOGGER.at(Level.WARNING).log("Map nicknames and LuckPerms tab list fix will be unavailable.");
        }
    }

    public static boolean isAvailable() {
        return available;
    }

    public static void setUsername(@Nonnull PlayerRef playerRef, @Nonnull String username) {
        if (!available) return;
        try {
            usernameField.set(playerRef, username);
        } catch (Exception e) {
            LOGGER.at(Level.SEVERE).withCause(e).log("Failed to set PlayerRef username");
        }
    }
}
