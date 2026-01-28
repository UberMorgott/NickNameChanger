package com.nickname.plugin.hooks;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public class LuckPermsHook {

    private static boolean available = false;
    private static LuckPerms luckPerms = null;

    public static void init() {
        try {
            Class.forName("net.luckperms.api.LuckPermsProvider");
            luckPerms = LuckPermsProvider.get();
            available = true;
            System.out.println("[NicknameChanger] LuckPerms integration enabled!");
        } catch (ClassNotFoundException | IllegalStateException e) {
            available = false;
            System.out.println("[NicknameChanger] LuckPerms not found, integration disabled.");
        }
    }

    public static boolean isAvailable() {
        return available;
    }

    @Nullable
    public static String getPrefix(@Nonnull UUID uuid) {
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
}
