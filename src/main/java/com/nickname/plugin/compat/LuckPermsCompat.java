package com.nickname.plugin.compat;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;

import java.util.UUID;

/**
 * Direct LuckPerms API implementation.
 * This class is ONLY loaded by the JVM when LuckPerms is present on the server.
 * Never reference this class directly — use {@link com.nickname.plugin.hooks.LuckPermsHook} instead.
 */
public class LuckPermsCompat {

    private static LuckPerms luckPerms;

    public static void init() {
        luckPerms = LuckPermsProvider.get();
    }

    public static String getPrefix(UUID uuid) {
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) return null;
        CachedMetaData metaData = user.getCachedData().getMetaData();
        return metaData.getPrefix();
    }

    public static String getSuffix(UUID uuid) {
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) return null;
        CachedMetaData metaData = user.getCachedData().getMetaData();
        return metaData.getSuffix();
    }

    public static String getPrimaryGroup(UUID uuid) {
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) return null;
        return user.getPrimaryGroup();
    }

    public static String getDisplayName(UUID uuid) {
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) return null;
        CachedMetaData metaData = user.getCachedData().getMetaData();
        return metaData.getMetaValue("display-name");
    }

    public static boolean hasPermission(UUID uuid, String permission) {
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null) return false;
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    public static void setDisplayName(UUID uuid, String displayName) {
        UserManager userManager = luckPerms.getUserManager();
        userManager.modifyUser(uuid, user -> {
            user.data().clear(NodeType.META.predicate(mn -> mn.getMetaKey().equals("display-name")));
            if (displayName != null && !displayName.isEmpty()) {
                MetaNode node = MetaNode.builder("display-name", displayName).build();
                user.data().add(node);
            }
        });
    }
}
