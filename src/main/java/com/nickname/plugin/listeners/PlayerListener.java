package com.nickname.plugin.listeners;

import com.hypixel.hytale.server.core.event.events.player.PlayerReadyEvent;
import com.hypixel.hytale.server.core.entity.nameplate.Nameplate;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.protocol.packets.interface_.AddToServerPlayerList;
import com.hypixel.hytale.protocol.packets.interface_.RemoveFromServerPlayerList;
import com.hypixel.hytale.protocol.packets.interface_.ServerPlayerListPlayer;
import com.nickname.plugin.hooks.TinyMessageHook;
import com.nickname.plugin.i18n.Messages;
import com.nickname.plugin.storage.NicknameStorage;

import javax.annotation.Nonnull;
import java.util.UUID;

public class PlayerListener {

    private final NicknameStorage storage;

    public PlayerListener(NicknameStorage storage) {
        this.storage = storage;
    }

    public void onPlayerReady(@Nonnull PlayerReadyEvent event) {
        Ref<EntityStore> ref = event.getPlayerRef();
        Store<EntityStore> store = ref.getStore();
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        if (playerRef == null) return;

        UUID uuid = playerRef.getUuid();

        String nickname = storage.getNickname(uuid);
        if (nickname != null) {
            // Apply nickname to nameplate (above head)
            updateNameplate(ref, store, nickname);

            // Update player list (inventory header, map, tab)
            updatePlayerList(playerRef, nickname);

            Message nicknameMsg = TinyMessageHook.isAvailable() && TinyMessageHook.hasColorTags(nickname)
                ? TinyMessageHook.parse(nickname)
                : Message.raw(nickname).color("#FFFF55");
            playerRef.sendMessage(Message.join(
                Message.raw(Messages.get(playerRef, Messages.WELCOME_NICKNAME) + " ").color("#55FF55"),
                nicknameMsg
            ));
            playerRef.sendMessage(Message.raw(Messages.get(playerRef, Messages.WELCOME_RESET_HINT)).color("#AAAAAA"));
        }
    }

    private void updateNameplate(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull String displayName) {
        // Update Nameplate component (text above head)
        Nameplate nameplate = store.ensureAndGetComponent(ref, Nameplate.getComponentType());
        nameplate.setText(stripColorTags(displayName));

        // Update DisplayNameComponent with TinyMessage parsing
        Message displayMessage = TinyMessageHook.isAvailable() && TinyMessageHook.hasColorTags(displayName)
            ? TinyMessageHook.parse(displayName)
            : Message.raw(displayName).color("#FFFF55");
        DisplayNameComponent displayNameComponent = new DisplayNameComponent(displayMessage);
        store.putComponent(ref, DisplayNameComponent.getComponentType(), displayNameComponent);
    }

    private String stripColorTags(String input) {
        return input.replaceAll("<[^>]+>", "").replaceAll("</[^>]+>", "");
    }

    private void updatePlayerList(PlayerRef playerRef, String displayName) {
        UUID uuid = playerRef.getUuid();
        UUID worldUuid = playerRef.getWorldUuid();

        // Remove player from list
        RemoveFromServerPlayerList removePacket = new RemoveFromServerPlayerList(new UUID[]{uuid});
        Universe.get().broadcastPacket(removePacket);

        // Add player back with new display name
        ServerPlayerListPlayer playerListEntry = new ServerPlayerListPlayer(
            uuid,
            displayName,
            worldUuid,
            0  // ping will be updated by the ping system
        );
        AddToServerPlayerList addPacket = new AddToServerPlayerList(new ServerPlayerListPlayer[]{playerListEntry});
        Universe.get().broadcastPacket(addPacket);
    }
}
