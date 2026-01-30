package com.nickname.plugin.commands;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.AddToServerPlayerList;
import com.hypixel.hytale.protocol.packets.interface_.RemoveFromServerPlayerList;
import com.hypixel.hytale.protocol.packets.interface_.ServerPlayerListPlayer;
import com.nickname.plugin.hooks.LuckPermsHook;
import com.nickname.plugin.util.MessageUtil;
import com.nickname.plugin.i18n.Messages;
import com.nickname.plugin.storage.NicknameStorage;
import com.nickname.plugin.ui.NicknameEditorPage;

import javax.annotation.Nonnull;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NickCommand extends AbstractCommand {

    public static final String PERM_USE = "nickname.use";
    public static final String PERM_FORMAT = "nickname.format";

    private final NicknameStorage storage;
    private static final int MIN_LENGTH = 2;
    private static final int MAX_LENGTH = 32;

    public NickCommand(NicknameStorage storage) {
        super("nick", "Set your display nickname");
        this.storage = storage;
        setAllowsExtraArguments(true);
    }

    @Override
    public boolean hasPermission(@Nonnull CommandSender sender) {
        // Default: allowed. Deny only with explicit "-nickname.use"
        return sender.hasPermission(PERM_USE, true);
    }

    @Override
    protected CompletableFuture<Void> execute(@Nonnull CommandContext context) {
        CommandSender sender = context.sender();

        if (!(sender instanceof Player)) {
            context.sendMessage(Message.raw(Messages.get("en-US", Messages.ERROR_PLAYERS_ONLY)).color("#FF5555"));
            return CompletableFuture.completedFuture(null);
        }

        Player player = (Player) sender;
        Ref<EntityStore> ref = player.getReference();

        if (ref == null || !ref.isValid()) {
            context.sendMessage(Message.raw(Messages.get("en-US", Messages.ERROR_NOT_IN_WORLD)).color("#FF5555"));
            return CompletableFuture.completedFuture(null);
        }

        Store<EntityStore> store = ref.getStore();
        EntityStore entityStore = (EntityStore) store.getExternalData();
        World world = entityStore.getWorld();

        // Run in world thread
        return CompletableFuture.runAsync(() -> {
            PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());
            if (playerRef == null) return;

            UUID playerUuid = playerRef.getUuid();
            String username = playerRef.getUsername();

            String nickname = null;
            String fullInput = context.getInputString();
            if (fullInput != null && !fullInput.isEmpty()) {
                String[] parts = fullInput.split("\\s+", 2);
                if (parts.length > 1) {
                    nickname = parts[1].trim();
                }
            }

            if (nickname == null || nickname.isEmpty()) {
                // Open UI editor
                openNicknameEditor(player, ref, store, playerRef);
                return;
            }

            String arg = nickname.trim();

            if (arg.equalsIgnoreCase("reset") || arg.equalsIgnoreCase("clear") ||
                arg.equalsIgnoreCase("off") || arg.equalsIgnoreCase("remove")) {
                resetNickname(ref, store, playerRef, playerUuid, username);
                return;
            }

            setNickname(ref, store, playerRef, playerUuid, username, arg);
        }, world);
    }

    private void showCurrentNickname(@Nonnull PlayerRef playerRef, @Nonnull UUID uuid, @Nonnull String username) {
        String nickname = storage.getNickname(uuid);
        if (nickname != null) {
            playerRef.sendMessage(Message.join(
                Message.raw(Messages.get(playerRef, Messages.CURRENT_NICKNAME) + " ").color("#AAAAAA"),
                Message.raw(nickname).color("#FFFF55")
            ));
        } else {
            playerRef.sendMessage(Message.join(
                Message.raw(Messages.get(playerRef, Messages.CURRENT_NO_NICKNAME) + " ").color("#AAAAAA"),
                Message.raw(username).color("#FFFFFF")
            ));
        }
        playerRef.sendMessage(Message.raw(Messages.get(playerRef, Messages.USAGE)).color("#AAAAAA"));
    }

    private void openNicknameEditor(@Nonnull Player player, @Nonnull Ref<EntityStore> ref,
                                     @Nonnull Store<EntityStore> store, @Nonnull PlayerRef playerRef) {
        NicknameEditorPage editorPage = new NicknameEditorPage(storage, playerRef);
        player.getPageManager().openCustomPage(ref, store, editorPage);
    }

    private void resetNickname(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PlayerRef playerRef, @Nonnull UUID uuid, @Nonnull String username) {
        if (storage.hasNickname(uuid)) {
            // Get original username before clearing
            String originalUsername = storage.getOriginalUsername(uuid);
            if (originalUsername == null) {
                originalUsername = username;
            }

            storage.removeNickname(uuid);

            // Remove nickname from LuckPerms if available
            if (LuckPermsHook.isAvailable()) {
                LuckPermsHook.removeDisplayName(uuid);
            }

            // Reset nameplate to original username
            resetNameplate(ref, store, originalUsername);

            // Reset player list to original username
            updatePlayerList(playerRef, originalUsername);

            playerRef.sendMessage(Message.join(
                Message.raw(Messages.get(playerRef, Messages.RESET_SUCCESS) + " ").color("#55FF55"),
                Message.raw(originalUsername).color("#FFFFFF")
            ));
        } else {
            playerRef.sendMessage(Message.raw(Messages.get(playerRef, Messages.RESET_NO_NICKNAME)).color("#FFFF55"));
        }
    }

    private void setNickname(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull PlayerRef playerRef, @Nonnull UUID uuid, @Nonnull String username, @Nonnull String nickname) {
        // Check format permission if nickname contains markup (default: allowed)
        if (MessageUtil.hasMarkup(nickname) && !PermissionsModule.get().hasPermission(uuid, PERM_FORMAT, true)) {
            playerRef.sendMessage(Message.raw(Messages.get(playerRef, Messages.ERROR_NO_FORMAT_PERM)).color("#FF5555"));
            return;
        }

        // Check length without color tags
        String plainNickname = stripColorTags(nickname);
        if (plainNickname.length() < MIN_LENGTH) {
            playerRef.sendMessage(Message.raw(Messages.get(playerRef, Messages.ERROR_MIN_LENGTH, "min", MIN_LENGTH)).color("#FF5555"));
            return;
        }
        if (plainNickname.length() > MAX_LENGTH) {
            playerRef.sendMessage(Message.raw(Messages.get(playerRef, Messages.ERROR_MAX_LENGTH, "max", MAX_LENGTH)).color("#FF5555"));
            return;
        }

        String filtered = filterNickname(nickname);
        if (filtered.isEmpty()) {
            playerRef.sendMessage(Message.raw(Messages.get(playerRef, Messages.ERROR_INVALID)).color("#FF5555"));
            return;
        }

        // Store original username for reset
        storage.setNickname(uuid, filtered);
        storage.setOriginalUsername(uuid, username);

        // Sync nickname to LuckPerms if available (for chat formatting compatibility)
        if (LuckPermsHook.isAvailable()) {
            LuckPermsHook.setDisplayName(uuid, filtered);
        }

        // Update nameplate (above head)
        updateNameplate(ref, store, filtered);

        // Update player list (map, tab)
        updatePlayerList(playerRef, filtered);

        playerRef.sendMessage(Message.join(
            Message.raw(Messages.get(playerRef, Messages.SET_SUCCESS) + " ").color("#55FF55"),
            MessageUtil.parse(filtered)
        ));
    }

    private void updateNameplate(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull String displayName) {
        // Update Nameplate component (text above head) - same pattern as EntityNameplateCommand
        Nameplate nameplate = store.ensureAndGetComponent(ref, Nameplate.getComponentType());

        // Strip color tags for nameplate text (it doesn't support rich text)
        String plainName = stripColorTags(displayName);
        nameplate.setText(plainName);

        // Update DisplayNameComponent
        DisplayNameComponent displayNameComponent = new DisplayNameComponent(MessageUtil.parse(displayName));
        store.putComponent(ref, DisplayNameComponent.getComponentType(), displayNameComponent);
    }

    @Nonnull
    private String stripColorTags(@Nonnull String input) {
        // Remove TinyMessage tags like <color:#FF0000>, <bold>, etc.
        return input.replaceAll("<[^>]+>", "").replaceAll("</[^>]+>", "");
    }

    private void updatePlayerList(@Nonnull PlayerRef playerRef, @Nonnull String displayName) {
        UUID uuid = playerRef.getUuid();
        UUID worldUuid = playerRef.getWorldUuid();

        // Strip color tags for player list (it may not support rich text)
        String plainName = stripColorTags(displayName);

        // Remove player from list
        RemoveFromServerPlayerList removePacket = new RemoveFromServerPlayerList(new UUID[]{uuid});
        Universe.get().broadcastPacket(removePacket);

        // Add player back with new display name
        ServerPlayerListPlayer playerListEntry = new ServerPlayerListPlayer(
            uuid,
            plainName,
            worldUuid,
            0  // ping will be updated by the ping system
        );
        AddToServerPlayerList addPacket = new AddToServerPlayerList(new ServerPlayerListPlayer[]{playerListEntry});
        Universe.get().broadcastPacket(addPacket);
    }

    private void resetNameplate(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull String originalUsername) {
        // Reset Nameplate to original username
        Nameplate nameplate = store.ensureAndGetComponent(ref, Nameplate.getComponentType());
        nameplate.setText(originalUsername);

        // Remove DisplayNameComponent (use original name)
        store.removeComponentIfExists(ref, DisplayNameComponent.getComponentType());
    }

    @Nonnull
    private String filterNickname(@Nonnull String nickname) {
        // Allow markup tags
        if (MessageUtil.hasMarkup(nickname)) {
            return filterNicknameWithTags(nickname);
        }

        StringBuilder filtered = new StringBuilder();
        for (char c : nickname.toCharArray()) {
            if (Character.isLetterOrDigit(c) || c == ' ' || c == '_' || c == '-' ||
                c == '.' || c == '!' || c == '?' || Character.isLetter(c)) {
                filtered.append(c);
            }
        }
        String result = filtered.toString().trim();
        return result != null ? result : "";
    }

    @Nonnull
    private String filterNicknameWithTags(@Nonnull String nickname) {
        // Allow TinyMessage color tags while filtering regular text
        StringBuilder filtered = new StringBuilder();
        boolean inTag = false;

        for (int i = 0; i < nickname.length(); i++) {
            char c = nickname.charAt(i);

            if (c == '<') {
                inTag = true;
                filtered.append(c);
            } else if (c == '>') {
                inTag = false;
                filtered.append(c);
            } else if (inTag) {
                // Inside a tag, allow all characters
                filtered.append(c);
            } else {
                // Outside a tag, filter normally
                if (Character.isLetterOrDigit(c) || c == ' ' || c == '_' || c == '-' ||
                    c == '.' || c == '!' || c == '?' || Character.isLetter(c)) {
                    filtered.append(c);
                }
            }
        }
        String result = filtered.toString().trim();
        return result != null ? result : "";
    }
}
