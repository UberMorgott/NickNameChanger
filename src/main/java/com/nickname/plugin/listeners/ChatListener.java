package com.nickname.plugin.listeners;

import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.Message;
import com.nickname.plugin.hooks.LuckPermsHook;
import com.nickname.plugin.hooks.TinyMessageHook;
import com.nickname.plugin.storage.NicknameStorage;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ChatListener {

    private final NicknameStorage storage;

    public ChatListener(@Nonnull NicknameStorage storage) {
        this.storage = storage;
    }

    public CompletableFuture<String> onPlayerChat(@Nonnull PlayerChatEvent event) {
        PlayerRef sender = event.getSender();
        UUID senderUuid = sender.getUuid();
        String originalName = sender.getUsername();
        String displayName = storage.getDisplayName(senderUuid, originalName);

        // Ensure displayName is never null
        final String safeName = displayName != null ? displayName : originalName;

        // Get LuckPerms prefix/suffix if available
        final String prefix = LuckPermsHook.isAvailable() ? LuckPermsHook.getPrefix(senderUuid) : null;
        final String suffix = LuckPermsHook.isAvailable() ? LuckPermsHook.getSuffix(senderUuid) : null;

        event.setFormatter((playerRef, message) -> {
            Message result = Message.empty();

            // Add prefix if available (parse with TinyMessage for colors)
            if (prefix != null && !prefix.isEmpty()) {
                if (TinyMessageHook.isAvailable() && TinyMessageHook.hasColorTags(prefix)) {
                    result = result.insert(TinyMessageHook.parse(prefix));
                } else {
                    result = result.insert(Message.raw(prefix));
                }
            }

            // Add opening bracket
            result = result.insert(Message.raw("<").color("#AAAAAA"));

            // Add display name (parse with TinyMessage if has color tags)
            if (TinyMessageHook.isAvailable() && TinyMessageHook.hasColorTags(safeName)) {
                result = result.insert(TinyMessageHook.parse(safeName));
            } else if (storage.hasNickname(senderUuid)) {
                result = result.insert(Message.raw(safeName).color("#FFFF55"));
            } else {
                result = result.insert(Message.raw(safeName).color("#FFFFFF"));
            }

            // Add closing bracket
            result = result.insert(Message.raw(">").color("#AAAAAA"));

            // Add suffix if available (parse with TinyMessage for colors)
            if (suffix != null && !suffix.isEmpty()) {
                if (TinyMessageHook.isAvailable() && TinyMessageHook.hasColorTags(suffix)) {
                    result = result.insert(TinyMessageHook.parse(suffix));
                } else {
                    result = result.insert(Message.raw(suffix));
                }
            }

            // Add space and message
            result = result.insert(Message.raw(" ").color("#AAAAAA"));
            result = result.insert(Message.raw(message).color("#FFFFFF"));

            return result;
        });

        String content = event.getContent();
        CompletableFuture<String> result = CompletableFuture.completedFuture(content != null ? content : "");
        return result;
    }
}
