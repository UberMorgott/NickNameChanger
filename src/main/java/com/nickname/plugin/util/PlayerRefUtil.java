package com.nickname.plugin.util;

import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public class PlayerRefUtil {

    private static Field usernameField;
    private static boolean available = false;

    public static void init() {
        try {
            usernameField = PlayerRef.class.getDeclaredField("username");
            usernameField.setAccessible(true);
            available = true;
            System.out.println("[NicknameChanger] PlayerRef reflection initialized.");
        } catch (NoSuchFieldException | SecurityException e) {
            available = false;
            System.err.println("[NicknameChanger] Failed to access PlayerRef.username field: " + e.getMessage());
            System.err.println("[NicknameChanger] Map nicknames and LuckPerms tab list fix will be unavailable.");
        }
    }

    public static boolean isAvailable() {
        return available;
    }

    public static void setUsername(@Nonnull PlayerRef playerRef, @Nonnull String username) {
        if (!available) return;
        try {
            usernameField.set(playerRef, username);
        } catch (IllegalAccessException e) {
            System.err.println("[NicknameChanger] Failed to set PlayerRef username: " + e.getMessage());
        }
    }
}
