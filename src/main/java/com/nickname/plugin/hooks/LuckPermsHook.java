package com.nickname.plugin.hooks;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class LuckPermsHook {

    private static boolean initialized = false;
    private static boolean available = false;
    private static LuckPerms luckPerms = null;

    /**
     * Initial check at startup (may fail if LP loads later).
     */
    public static void init() {
        tryInit();
    }

    /**
     * Lazy initialization - tries to get LuckPerms if not yet available.
     * This handles the case where LuckPerms loads after NicknameChanger.
     */
    private static void tryInit() {
        if (available && luckPerms != null) {
            return; // Already initialized successfully
        }
        try {
            Class.forName("net.luckperms.api.LuckPermsProvider");
            luckPerms = LuckPermsProvider.get();
            boolean wasUnavailable = !available;
            available = true;
            if (!initialized) {
                System.out.println("[NicknameChanger] LuckPerms integration enabled!");
                initialized = true;
            } else if (wasUnavailable) {
                // LP became available after initial failed check
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
        tryInit(); // Lazy check
        return available;
    }

    @Nullable
    public static String getPrefix(@Nonnull UUID uuid) {
        tryInit();
        if (!available || luckPerms == null) {
            return null;
        }
        try {
            User user = luckPerms.getUserManager().getUser(uuid);
            if (user == null) {
                return null;
            }
            CachedMetaData metaData = user.getCachedData().getMetaData();
            return metaData.getPrefix();
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static String getSuffix(@Nonnull UUID uuid) {
        tryInit();
        if (!available || luckPerms == null) {
            return null;
        }
        try {
            User user = luckPerms.getUserManager().getUser(uuid);
            if (user == null) {
                return null;
            }
            CachedMetaData metaData = user.getCachedData().getMetaData();
            return metaData.getSuffix();
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static String getPrimaryGroup(@Nonnull UUID uuid) {
        tryInit();
        if (!available || luckPerms == null) {
            return null;
        }
        try {
            User user = luckPerms.getUserManager().getUser(uuid);
            if (user == null) {
                return null;
            }
            return user.getPrimaryGroup();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the display-name meta value from LuckPerms if set.
     */
    @Nullable
    public static String getDisplayName(@Nonnull UUID uuid) {
        tryInit();
        if (!available || luckPerms == null) {
            return null;
        }
        try {
            User user = luckPerms.getUserManager().getUser(uuid);
            if (user == null) {
                return null;
            }
            CachedMetaData metaData = user.getCachedData().getMetaData();
            return metaData.getMetaValue("display-name");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Set the display-name meta value in LuckPerms.
     * This allows LuckPerms chat formatting to pick up the nickname.
     */
    public static void setDisplayName(@Nonnull UUID uuid, @Nullable String displayName) {
        tryInit();
        if (!available || luckPerms == null) {
            return;
        }
        try {
            UserManager userManager = luckPerms.getUserManager();
            userManager.modifyUser(uuid, user -> {
                // Remove existing display-name meta nodes
                user.data().clear(NodeType.META.predicate(mn -> mn.getMetaKey().equals("display-name")));

                // Add new display-name if provided
                if (displayName != null && !displayName.isEmpty()) {
                    MetaNode node = MetaNode.builder("display-name", displayName).build();
                    user.data().add(node);
                }
            });
        } catch (Exception e) {
            System.out.println("[NicknameChanger] Failed to set LuckPerms display-name: " + e.getMessage());
        }
    }

    /**
     * Remove the display-name meta value from LuckPerms.
     */
    public static void removeDisplayName(@Nonnull UUID uuid) {
        setDisplayName(uuid, null);
    }
}
