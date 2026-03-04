package com.nickname.plugin.listeners;

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.Message;
import com.nickname.plugin.chat.ChatFormatParser;
import com.nickname.plugin.config.PluginConfig;
import com.nickname.plugin.hooks.LuckPermsHook;
import com.nickname.plugin.util.MessageUtil;
import com.nickname.plugin.storage.NicknameStorage;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ChatListener {

    private final NicknameStorage storage;
    private final PluginConfig config;
    private final ChatFormatParser formatParser;

    public ChatListener(@Nonnull NicknameStorage storage, @Nonnull PluginConfig config) {
        this.storage = storage;
        this.config = config;
        this.formatParser = new ChatFormatParser(config.chatFormat);
    }

    public CompletableFuture<String> onPlayerChat(@Nonnull PlayerChatEvent event) {
        PlayerRef sender = event.getSender();
        UUID senderUuid = sender.getUuid();
        String originalName = sender.getUsername();

        boolean hasNickname = storage.isShowInChat() && storage.hasNickname(senderUuid);
        boolean hasLuckPerms = LuckPermsHook.isAvailable();

        // Pre-fetch LP prefix/suffix so we can decide whether to override the formatter.
        final String prefix = (hasLuckPerms && config.integrations.luckperms.showPrefix)
                ? LuckPermsHook.getPrefix(senderUuid) : null;
        final String suffix = (hasLuckPerms && config.integrations.luckperms.showSuffix)
                ? LuckPermsHook.getSuffix(senderUuid) : null;

        boolean hasLpData = (prefix != null && !prefix.isEmpty())
                || (suffix != null && !suffix.isEmpty());
        boolean hasMsgColor = storage.getMessageColor(senderUuid) != null;

        // Only replace the formatter when we have something to contribute.
        // If the player has no nickname, LP has no prefix/suffix for them,
        // and no message color is set — let other chat plugins handle formatting.
        if (!hasNickname && !hasLpData && !hasMsgColor) {
            String content = event.getContent();
            return CompletableFuture.completedFuture(content != null ? content : "");
        }

        String displayName = hasNickname
                ? storage.getDisplayName(senderUuid, originalName) : originalName;
        final String safeName = displayName != null ? displayName : originalName;

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

        String content = event.getContent();
        return CompletableFuture.completedFuture(content != null ? content : "");
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
