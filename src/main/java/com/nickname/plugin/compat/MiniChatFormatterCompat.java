package com.nickname.plugin.compat;

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
            try {
                Class.forName("me.lucko.minichatformatter.MiniChatFormatterPlugin");
                available = true;
                System.out.println("[NicknameChanger] mini-chat-formatter detected, enabling compatibility mode.");
            } catch (ClassNotFoundException e) {
                available = false;
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
