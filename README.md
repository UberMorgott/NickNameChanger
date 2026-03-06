# NickNameChanger

A Hytale server plugin that lets players customize their display nickname with colors, gradients, and text formatting.

## Features

- **Custom Nicknames**: Set display names with `/nick` command or graphical UI editor
- **Color Support**: Solid colors, gradients, bold, italic, underline
- **Message Color**: Customize your chat message text color
- **Graphical UI**: Full visual editor with color pickers, gradient presets, and live preview
- **LuckPerms Integration**: Displays LP prefixes/suffixes in chat
- **EssentialsPlus Integration**: Nicknames with colors render through EP's chat formatter
- **Mini-Chat-Formatter Support**: Compatible when MCF is active

## Commands

| Command | Description |
|---------|-------------|
| `/nick` | Open the nickname editor UI |
| `/nick <name>` | Set nickname via command |
| `/nick reset` | Reset nickname and message color |
| `/nick msgcolor <#hex>` | Set message text color |
| `/nick msgcolor gradient:#hex1:#hex2` | Set gradient message color |
| `/nick msgcolor reset` | Reset message color only |
| `/nick settings` | Open admin settings (requires `nickname.admin`) |

## Permissions

| Permission | Default | Description |
|-----------|---------|-------------|
| `nickname.use` | true | Use /nick command |
| `nickname.format` | true | Use colors and formatting |
| `nickname.admin` | false | Access settings panel |

## Compatibility

| Mod | Status |
|-----|--------|
| Standalone | ✅ Full support |
| LuckPerms | ✅ Prefix/suffix in chat, tab list, hex colors |
| EssentialsPlus | ✅ Colored nicknames + message color via EP chat |
| Mini-Chat-Formatter | ⚠️ Compatible, but MCF has known issues |
| Hyssential | ✅ Works as-is (picks up nickname automatically) |
| Essentials Core | ✅ Works as-is |

NickNameChanger uses a **decorator pattern** for chat formatting — it preserves existing formatters from other plugins instead of overriding them. Plugin detection is done via native `PluginManager` API.

## Technical Details

- **Native Hytale API**: Uses `PluginManager` for plugin detection, `HytaleLogger` for logging, `BuilderCodec` for configuration
- **Chat compatibility**: Decorator pattern — wraps existing chat formatters instead of replacing them
- **Thread-safe**: All store operations run on WorldThread; commands dispatch correctly via `CompletableFuture`
- **Reflection**: Uses `PlayerRef.username` reflection for map/tab display (no native setter available)

## Installation

1. Download the latest JAR from [Releases](https://github.com/UberMorgott/NickNameChanger/releases)
2. Place in `Hytale/UserData/Mods/`
3. Restart the server
4. Use `/nick` to open the editor

## Admin Settings Panel

The settings panel (`/nick settings`) gives administrators a GUI to control where nicknames appear globally.

- Three checkboxes: **Show in Chat**, **Show on Nameplate**, **Show in Tab List**
- Changes apply **instantly** to all online players — no restart required
- Settings persist across server restarts

> **Note:** "Show on Map" is a **config-only** setting (`Display.ShowOnMap`) — it is not exposed in the admin GUI.

## Configuration

Config file is created automatically at `mods/NickNameChanger_NickNameChanger/config.json`

```json
{
  "PluginName": "NickNameChanger",
  "Version": "0.0.17",
  "DebugMode": false,
  "ChatFormat": "{prefix}<{username}>{suffix} {message}",
  "Display": {
    "ShowInChat": true,
    "ShowOnNameplate": true,
    "ShowInTabList": true,
    "ShowOnMap": false
  },
  "Nicknames": {
    "MinLength": 2,
    "MaxLength": 32,
    "AllowCyrillic": true,
    "AllowUnicode": false,
    "UniqueNicknames": true,
    "BannedWords": ["admin", "moderator", "server", "owner"]
  },
  "Integrations": {
    "Luckperms": {
      "Enabled": true,
      "ShowPrefix": true,
      "ShowSuffix": true
    }
  }
}
```

> **Important:** Config keys use **PascalCase** (e.g. `ShowInChat`, not `showInChat`) — this is required by Hytale's `BuilderCodec` serializer.

## Developer API

Other plugins can read nickname data and display settings via the `NicknameAPI` class.

### Setup

Add NickNameChanger as an optional dependency in your `manifest.json`:

```json
{
  "OptionalDependencies": {
    "NickNameChanger:NickNameChanger": "*"
  }
}
```

### Check if NickNameChanger is loaded

```java
import com.hypixel.hytale.server.core.plugin.PluginManager;
import com.hypixel.hytale.common.plugin.PluginIdentifier;

boolean hasNNC = PluginManager.get().getPlugin(
    new PluginIdentifier("NickNameChanger", "NickNameChanger")) != null;
```

### Using NicknameAPI

```java
import com.nickname.plugin.api.NicknameAPI;
import java.util.UUID;

// Get player UUID from any PlayerRef
UUID uuid = playerRef.getUuid();
```

### Methods

#### `getNickname(UUID uuid) → String | null`

Returns the raw nickname with markup tags, or `null` if no nickname is set.

```java
String nick = NicknameAPI.getNickname(uuid);
// Returns: "<gradient:#FF5555:#55FF55>Morgott" (with color tags)
// Returns: "Morgott" (plain nickname)
// Returns: null (no nickname set)
```

#### `getDisplayName(UUID uuid, String defaultName) → String`

Returns the nickname if set, otherwise returns `defaultName`. Never returns null.

```java
String name = NicknameAPI.getDisplayName(uuid, playerRef.getUsername());
// Returns: "Morgott" (nickname or fallback, always non-null)
```

#### `hasNickname(UUID uuid) → boolean`

```java
if (NicknameAPI.hasNickname(uuid)) {
    // Player has a custom nickname
}
```

#### `getOriginalUsername(UUID uuid) → String | null`

Returns the player's real username before any nickname was set. Returns `null` if unknown.

```java
String realName = NicknameAPI.getOriginalUsername(uuid);
// Returns: "UberMorgott" (original username)
// Returns: null (player never set a nickname, original unknown)
```

#### Display Settings (global, read-only)

```java
NicknameAPI.isShowInChat()      // Are nicknames shown in chat? (default: true)
NicknameAPI.isShowOnNameplate()  // Are nicknames shown above heads? (default: true)
NicknameAPI.isShowInTabList()    // Are nicknames shown in player list? (default: true)
```

### Full Example: Custom Join Message

```java
public void onPlayerReady(PlayerReadyEvent event) {
    PlayerRef playerRef = /* get from event */;
    UUID uuid = playerRef.getUuid();

    String displayName = NicknameAPI.getDisplayName(uuid, playerRef.getUsername());
    String original = NicknameAPI.getOriginalUsername(uuid);

    if (original != null && !displayName.equals(original)) {
        broadcast(displayName + " (aka " + original + ") joined!");
    } else {
        broadcast(displayName + " joined!");
    }
}
```

### Notes

- All methods are **static** and **thread-safe**
- All methods return safe defaults if NickNameChanger is not loaded (`null` or `false`)
- `getNickname()` may contain markup tags (`<color:...>`, `<gradient:...>`, `<bold>`, etc.) — use `getDisplayName()` if you need plain text with fallback
- Add `NickNameChanger:NickNameChanger` to your `OptionalDependencies` to ensure NNC loads before your plugin

## Links

- [CurseForge](https://www.curseforge.com/hytale/mods/nick-name-changer)
- [Issues](https://github.com/UberMorgott/NickNameChanger/issues)
