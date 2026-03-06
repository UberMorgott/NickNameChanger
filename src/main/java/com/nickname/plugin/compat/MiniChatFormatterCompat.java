package com.nickname.plugin.compat;

import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.common.plugin.PluginIdentifier;

/**
 * Detects mini-chat-formatter (MCF) by lucko.
 * When MCF is active, NNC defers LP prefix/suffix formatting to MCF
 * and only overrides the formatter for actual NNC features (nicknames, message colors).
 */
public class MiniChatFormatterCompat {

    private volatile boolean checked = false;
    private volatile boolean available = false;

    /**
     * Eagerly detect MCF. Call from plugin start() after all plugins are loaded.
     */
    public void detect() {
        if (!checked) {
            PluginManager pm = PluginManager.get();
            available = pm != null && pm.getPlugin(new PluginIdentifier("lucko", "MiniChatFormatter")) != null;
            if (available) {
                System.out.println("[NicknameChanger] mini-chat-formatter detected, enabling compatibility mode.");
            }
            checked = true;
        }
    }

    public boolean isAvailable() {
        if (!checked) {
            detect();
        }
        return available;
    }
}
