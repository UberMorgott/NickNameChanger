package com.nickname.plugin.compat;

import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.common.plugin.PluginIdentifier;

/**
 * Detects EssentialsPlus by fof1092.
 * When EP is active, NNC defers chat formatting to EssentialsPlus entirely
 * and only sets PlayerRef.username so EP's {player} placeholder picks up the nickname.
 * EP cancels the chat event and sends messages directly, so NNC cannot override the formatter.
 */
public class EssentialsPlusCompat {

    private volatile boolean checked = false;
    private volatile boolean available = false;

    /**
     * Eagerly detect EssentialsPlus. Call from plugin start() after all plugins are loaded.
     */
    public void detect() {
        if (!checked) {
            PluginManager pm = PluginManager.get();
            available = pm != null && pm.getPlugin(new PluginIdentifier("fof1092", "EssentialsPlus")) != null;
            if (available) {
                System.out.println("[NicknameChanger] EssentialsPlus detected, will defer chat formatting to EssentialsPlus.");
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
