package com.nickname.plugin.listeners;

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.Message;
import com.nickname.plugin.chat.ChatFormatParser;
import com.nickname.plugin.compat.EssentialsPlusCompat;
import com.nickname.plugin.compat.MiniChatFormatterCompat;
import com.nickname.plugin.config.PluginConfig;
import com.nickname.plugin.hooks.LuckPermsHook;
import com.nickname.plugin.util.MessageUtil;
import com.nickname.plugin.util.PlayerRefUtil;
import com.nickname.plugin.storage.NicknameStorage;

import javax.annotation.Nonnull;
import java.util.UUID;

public class ChatListener {

    private final NicknameStorage storage;
    private final PluginConfig config;
    private final ChatFormatParser formatParser;
    private final MiniChatFormatterCompat mcfCompat;
    private final EssentialsPlusCompat epCompat;

    public ChatListener(@Nonnull NicknameStorage storage, @Nonnull PluginConfig config,
                        @Nonnull MiniChatFormatterCompat mcfCompat, @Nonnull EssentialsPlusCompat epCompat) {
        this.storage = storage;
        this.config = config;
        this.formatParser = new ChatFormatParser(config.chatFormat);
        this.mcfCompat = mcfCompat;
        this.epCompat = epCompat;
    }

    /**
     * FIRST priority handler: sets plain nickname into PlayerRef.username BEFORE any
     * external formatters (EP at NORMAL, MCF at priority 1) read it.
     *
     * PlayerRef.username MUST contain ONLY plain text — tags break player lookup,
     * world store operations, and other systems that match by username string.
     */
    public void onPlayerChatEarly(@Nonnull PlayerChatEvent event) {
        if (event.isCancelled()) return;

        PlayerRef sender = event.getSender();
        UUID senderUuid = sender.getUuid();

        // Save original username BEFORE nickname logic changes it —
        // event content was generated with the original name
        String originalUsername = sender.getUsername();

        if (storage.isShowInChat() && storage.hasNickname(senderUuid)) {
            String nickname = storage.getNickname(senderUuid);
            if (epCompat.isAvailable() && MessageUtil.hasMarkup(nickname)) {
                // EP's ColoredTextParser will render these tags
                String epFormatted = MessageUtil.convertToEPFormat(nickname);
                PlayerRefUtil.setUsername(sender, epFormatted);
            } else {
                // Standalone/MCF: plain text only (tags break world store)
                String plainNick = MessageUtil.stripTags(nickname);
                PlayerRefUtil.setUsername(sender, plainNick);
            }
        }

        // Wrap message content in EP color tags for message color support
        if (epCompat.isAvailable()) {
            String msgColor = storage.getMessageColor(senderUuid);
            if (msgColor != null && !msgColor.isEmpty()) {
                String content = event.getContent();
                // Sanitize: remove any < > from message to prevent tag injection
                String safeMessage = content.replace("<", "").replace(">", "");
                // Wrap entire content in EP color format
                String coloredMessage;
                if (msgColor.startsWith("gradient:")) {
                    String[] parts = msgColor.split(":");
                    if (parts.length == 3) {
                        coloredMessage = "<gradient:" + parts[1] + ":" + parts[2] + ">" + safeMessage + "</gradient>";
                    } else {
                        coloredMessage = safeMessage;
                    }
                } else {
                    // Solid hex color like #FF5555
                    coloredMessage = "<" + msgColor + ">" + safeMessage + "</" + msgColor + ">";
                }
                event.setContent(coloredMessage);
            }
        }
    }

    /**
     * LATE priority handler: restores original username in PlayerRef, then either
     * defers to external formatters (EP/MCF) or applies own formatting in standalone mode.
     *
     * LATE (10922) runs after MCF's formatter-setting handler at priority (short)1,
     * but before MCF's LAST handler that checks if its formatter is still active.
     *
     * Event flow with MCF:
     * 1. FIRST: NNC sets plain nickname → PlayerRef.username = "Morgott"
     * 2. (short)1: MCF sets its formatter, reads PlayerRef for username placeholder
     * 3. LATE: NNC restores original username, detects MCF → doesn't set formatter
     * 4. LAST: MCF checks formatter is still MCF's → OK
     *
     * Event flow with EP:
     * 1. FIRST: NNC sets plain nickname → PlayerRef.username = "Morgott"
     * 2. NORMAL: EP reads getUsername() → "Morgott" → formats with group colors → cancels event
     * 3. LATE: NNC restores username, event cancelled → return
     */
    public void onPlayerChat(@Nonnull PlayerChatEvent event) {
        // Always restore original username so it doesn't persist in PlayerRef
        PlayerRef sender = event.getSender();
        UUID senderUuid = sender.getUuid();
        String originalName = storage.getOriginalUsername(senderUuid);
        if (originalName != null && storage.hasNickname(senderUuid)) {
            PlayerRefUtil.setUsername(sender, originalName);
        }

        if (event.isCancelled()) {
            return;
        }

        // Check for external formatters
        boolean mcfActive = mcfCompat.isAvailable();
        boolean epActive = epCompat.isAvailable();
        boolean externalFormatter = mcfActive || epActive;

        if (externalFormatter) {
            // External formatter already has the nickname from FIRST handler
            // Don't set own formatter
            return;
        }

        // Standalone mode — own formatter with LP prefix/suffix + msgcolor
        boolean hasNickname = storage.isShowInChat() && storage.hasNickname(senderUuid);

        boolean hasLuckPerms = LuckPermsHook.isAvailable();
        final String prefix = (hasLuckPerms && config.integrations.luckperms.showPrefix)
                ? LuckPermsHook.getPrefix(senderUuid) : null;
        final String suffix = (hasLuckPerms && config.integrations.luckperms.showSuffix)
                ? LuckPermsHook.getSuffix(senderUuid) : null;

        boolean hasLpData = (prefix != null && !prefix.isEmpty())
                || (suffix != null && !suffix.isEmpty());
        boolean hasMsgColor = storage.getMessageColor(senderUuid) != null;

        // Skip if nothing to contribute
        if (!hasNickname && !hasLpData && !hasMsgColor) {
            return;
        }

        String currentName = sender.getUsername();
        String displayName = hasNickname
                ? storage.getDisplayName(senderUuid, currentName) : currentName;
        final String safeName = displayName != null ? displayName : currentName;

        event.setFormatter((playerRef, message) -> {
            Message result = Message.empty();

            for (ChatFormatParser.Token token : formatParser.getTokens()) {
                if (token.type == ChatFormatParser.TokenType.PLACEHOLDER) {
                    switch (token.value) {
                        case "prefix":
                            if (prefix != null && !prefix.isEmpty()) {
                                result = result.insert(MessageUtil.parse(prefix));
                            }
                            break;
                        case "suffix":
                            if (suffix != null && !suffix.isEmpty()) {
                                result = result.insert(MessageUtil.parse(suffix));
                            }
                            break;
                        case "username":
                            result = result.insert(buildUsername(senderUuid, safeName));
                            break;
                        case "message":
                            String msgColor = storage.getMessageColor(senderUuid);
                            if (msgColor != null) {
                                result = result.insert(buildMessage(message, msgColor));
                            } else {
                                result = result.insert(Message.raw(message).color("#FFFFFF"));
                            }
                            break;
                    }
                } else {
                    String text = token.value;
                    if (MessageUtil.hasMarkup(text)) {
                        result = result.insert(MessageUtil.parse(text));
                    } else {
                        result = result.insert(Message.raw(text).color("#AAAAAA"));
                    }
                }
            }

            return result;
        });
    }

    private Message buildMessage(String message, String colorSpec) {
        if (colorSpec.startsWith("gradient:")) {
            String[] parts = colorSpec.split(":");
            if (parts.length == 3) {
                // Escape angle brackets to prevent tag injection (e.g. player typing "</gradient>")
                String safeMessage = message.replace("<", "").replace(">", "");
                return MessageUtil.parse("<gradient:" + parts[1] + ":" + parts[2] + ">" + safeMessage + "</gradient>");
            }
        }
        // Solid color — Message.raw() treats input as literal text, no injection risk
        return Message.raw(message).color(colorSpec);
    }

    private Message buildUsername(UUID uuid, String name) {
        if (MessageUtil.hasMarkup(name)) {
            return MessageUtil.parse(name);
        } else if (storage.hasNickname(uuid)) {
            return Message.raw(name).color("#FFFF55");
        } else {
            return Message.raw(name).color("#FFFFFF");
        }
    }
}
