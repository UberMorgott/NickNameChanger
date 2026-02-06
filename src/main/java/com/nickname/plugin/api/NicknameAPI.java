package com.nickname.plugin.api;

import com.nickname.plugin.storage.NicknameStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Public API for other plugins to read nickname data.
 * <p>
 * Usage from another plugin:
 * <pre>{@code
 * String nick = NicknameAPI.getNickname(playerUuid);
 * if (nick != null) {
 *     // player has a custom nickname
 * }
 * }</pre>
 */
public final class NicknameAPI {

    private static NicknameStorage storage;

    private NicknameAPI() {}

    /**
     * Called internally by NicknameChanger during setup. Not for external use.
     */
    public static void init(@Nonnull NicknameStorage storageInstance) {
        storage = storageInstance;
    }

    /**
     * Returns the raw nickname string (may contain markup tags like {@code <gradient:...>}),
     * or {@code null} if the player has no nickname set.
     */
    @Nullable
    public static String getNickname(@Nonnull UUID uuid) {
        if (storage == null) return null;
        return storage.getNickname(uuid);
    }

    /**
     * Returns the display name for the player: their nickname if set, otherwise the given default.
     */
    @Nonnull
    public static String getDisplayName(@Nonnull UUID uuid, @Nonnull String defaultName) {
        if (storage == null) return defaultName;
        return storage.getDisplayName(uuid, defaultName);
    }

    /**
     * Returns {@code true} if the player has a custom nickname set.
     */
    public static boolean hasNickname(@Nonnull UUID uuid) {
        if (storage == null) return false;
        return storage.hasNickname(uuid);
    }

    /**
     * Returns the player's original username (before nickname was set),
     * or {@code null} if unknown.
     */
    @Nullable
    public static String getOriginalUsername(@Nonnull UUID uuid) {
        if (storage == null) return null;
        return storage.getOriginalUsername(uuid);
    }
}
