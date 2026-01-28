package com.nickname.plugin.i18n;

import com.hypixel.hytale.server.core.modules.i18n.I18nModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Localization helper for NicknameChanger.
 * Uses Hytale's I18nModule for translations based on player's language.
 */
public final class Messages {

    private static final String PREFIX = "nickname.";

    // Message keys
    public static final String COMMAND_DESCRIPTION = PREFIX + "command.description";
    public static final String ERROR_PLAYERS_ONLY = PREFIX + "error.playersOnly";
    public static final String ERROR_NOT_IN_WORLD = PREFIX + "error.notInWorld";
    public static final String ERROR_MIN_LENGTH = PREFIX + "error.minLength";
    public static final String ERROR_MAX_LENGTH = PREFIX + "error.maxLength";
    public static final String ERROR_INVALID = PREFIX + "error.invalid";
    public static final String CURRENT_NICKNAME = PREFIX + "current.nickname";
    public static final String CURRENT_NO_NICKNAME = PREFIX + "current.noNickname";
    public static final String USAGE = PREFIX + "usage";
    public static final String RESET_SUCCESS = PREFIX + "reset.success";
    public static final String RESET_NO_NICKNAME = PREFIX + "reset.noNickname";
    public static final String SET_SUCCESS = PREFIX + "set.success";
    public static final String WELCOME_NICKNAME = PREFIX + "welcome.nickname";
    public static final String WELCOME_RESET_HINT = PREFIX + "welcome.resetHint";

    private Messages() {
        // Utility class
    }

    /**
     * Get translated message for player's language.
     *
     * @param playerRef The player to get language from
     * @param key       The translation key
     * @return Translated message or key if not found
     */
    @Nonnull
    public static String get(@Nonnull PlayerRef playerRef, @Nonnull String key) {
        return get(playerRef.getLanguage(), key);
    }

    /**
     * Get translated message for specific language.
     *
     * @param language The language code (e.g., "en-US", "ru-RU")
     * @param key      The translation key
     * @return Translated message or key if not found
     */
    @Nonnull
    public static String get(@Nullable String language, @Nonnull String key) {
        I18nModule i18n = I18nModule.get();
        if (i18n == null) {
            return key;
        }

        String message = i18n.getMessage(language, key);
        return message != null ? message : key;
    }

    /**
     * Get translated message with placeholder replacement.
     *
     * @param playerRef    The player to get language from
     * @param key          The translation key
     * @param placeholders Pairs of placeholder names and values
     * @return Translated message with placeholders replaced
     */
    @Nonnull
    public static String get(@Nonnull PlayerRef playerRef, @Nonnull String key, Object... placeholders) {
        return get(playerRef.getLanguage(), key, placeholders);
    }

    /**
     * Get translated message with placeholder replacement.
     *
     * @param language     The language code
     * @param key          The translation key
     * @param placeholders Pairs of placeholder names and values
     * @return Translated message with placeholders replaced
     */
    @Nonnull
    public static String get(@Nullable String language, @Nonnull String key, Object... placeholders) {
        String message = get(language, key);

        if (placeholders != null && placeholders.length >= 2) {
            for (int i = 0; i < placeholders.length - 1; i += 2) {
                String placeholder = "{" + placeholders[i] + "}";
                String value = String.valueOf(placeholders[i + 1]);
                message = message.replace(placeholder, value);
            }
        }

        return message != null ? message : key;
    }
}
