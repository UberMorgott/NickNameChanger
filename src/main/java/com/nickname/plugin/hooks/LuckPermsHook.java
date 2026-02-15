package com.nickname.plugin.hooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Safe facade for optional LuckPerms integration.
 * All direct LuckPerms API references are in {@link com.nickname.plugin.compat.LuckPermsCompat},
 * which the JVM only loads when LuckPerms is actually present.
 */
public class LuckPermsHook {

    private static volatile boolean initialized = false;
    private static volatile boolean available = false;
    private static volatile boolean configEnabled = true;

    public static void init(boolean enabled) {
        configEnabled = enabled;
        if (!enabled) {
            initialized = true;
            available = false;
            System.out.println("[NicknameChanger] LuckPerms integration disabled in config.");
            return;
        }
        tryInit();
    }

    private static void tryInit() {
        if (available || !configEnabled) {
            return;
        }
        try {
            Class.forName("net.luckperms.api.LuckPermsProvider");
            com.nickname.plugin.compat.LuckPermsCompat.init();
            boolean wasUnavailable = !available;
            available = true;
            if (!initialized) {
                System.out.println("[NicknameChanger] LuckPerms integration enabled!");
                initialized = true;
            } else if (wasUnavailable) {
                System.out.println("[NicknameChanger] LuckPerms integration enabled (late binding)!");
            }
        } catch (ClassNotFoundException | IllegalStateException | NoClassDefFoundError e) {
            if (!initialized) {
                System.out.println("[NicknameChanger] LuckPerms not found, running without it.");
                initialized = true;
            }
            available = false;
        }
    }

    public static boolean isAvailable() {
        tryInit();
        return available;
    }

    private static void disableHook(Throwable e) {
        if (available) {
            available = false;
            System.out.println("[NicknameChanger] LuckPerms became unavailable, disabling integration: " + e.getMessage());
        }
    }

    @Nullable
    public static String getPrefix(@Nonnull UUID uuid) {
        if (!isAvailable()) return null;
        try {
            return com.nickname.plugin.compat.LuckPermsCompat.getPrefix(uuid);
        } catch (NoClassDefFoundError | Exception e) {
            disableHook(e);
            return null;
        }
    }

    @Nullable
    public static String getSuffix(@Nonnull UUID uuid) {
        if (!isAvailable()) return null;
        try {
            return com.nickname.plugin.compat.LuckPermsCompat.getSuffix(uuid);
        } catch (NoClassDefFoundError | Exception e) {
            disableHook(e);
            return null;
        }
    }

    public static void setDisplayName(@Nonnull UUID uuid, @Nullable String displayName) {
        if (!isAvailable()) return;
        try {
            com.nickname.plugin.compat.LuckPermsCompat.setDisplayName(uuid, displayName);
        } catch (NoClassDefFoundError | Exception e) {
            disableHook(e);
        }
    }

    public static void removeDisplayName(@Nonnull UUID uuid) {
        setDisplayName(uuid, null);
    }
}
