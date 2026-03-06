package com.nickname.plugin.compat;

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
            try {
                Class.forName("de.fof1092.essentialsplus.EssentialsPlus");
                available = true;
                System.out.println("[NicknameChanger] EssentialsPlus detected, will defer chat formatting to EssentialsPlus.");
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
