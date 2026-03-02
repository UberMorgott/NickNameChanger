package com.nickname.plugin.compat;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;

import com.nickname.plugin.util.MessageUtil;

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

    /**
     * Resolves a LuckPerms User by UUID.
     * When allowBlocking is false, only checks the in-memory cache (safe for hot paths like chat).
     * When allowBlocking is true, falls back to loading from storage if not cached.
     */
    private static User resolveUser(UUID uuid, boolean allowBlocking) {
        User user = luckPerms.getUserManager().getUser(uuid);
        if (user == null && allowBlocking) {
            try {
                user = luckPerms.getUserManager().loadUser(uuid).join();
            } catch (Exception e) {
                return null;
            }
        }
        return user;
    }

    public static String getPrefix(UUID uuid) {
        User user = resolveUser(uuid, false);
        if (user == null) return null;
        CachedMetaData metaData = user.getCachedData().getMetaData();
        return metaData.getPrefix();
    }

    public static String getSuffix(UUID uuid) {
        User user = resolveUser(uuid, false);
        if (user == null) return null;
        CachedMetaData metaData = user.getCachedData().getMetaData();
        return metaData.getSuffix();
    }

    public static void setDisplayName(UUID uuid, String displayName) {
        UserManager userManager = luckPerms.getUserManager();
        userManager.modifyUser(uuid, user -> {
            user.data().clear(NodeType.META.predicate(mn -> mn.getMetaKey().equals("display-name")));
            if (displayName != null && !displayName.isEmpty()) {
                String cleanName = MessageUtil.stripTags(displayName);
                MetaNode node = MetaNode.builder("display-name", cleanName).build();
                user.data().add(node);
            }
        });
    }
}
