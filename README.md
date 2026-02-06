# NickNameChanger

[![en](https://img.shields.io/badge/lang-English-blue)](README.md) [![ru](https://img.shields.io/badge/lang-Русский-green)](README.ru.md)

A server-side plugin for Hytale that allows players to customize their display nickname with colors, gradients, and text styles.

![Nickname Editor](img/editor_gradient.png)

## Features

- **UI Editor** — Graphical interface for nickname customization
  - 12 preset colors + custom color picker
  - 8 gradient presets + custom gradient colors
  - Text styles: Bold, Italic, Underline
  - Live preview
- **Chat** — Custom nickname displayed in chat messages
- **Nameplate** — Nickname shown above player's head
- **Map** — Custom name displayed on the world map
- **Tab List** — Nickname visible in the player list
- **Configuration** — Toggle where nicknames appear via `config.json`
- **LuckPerms** — Prefix/suffix in chat with hex color support (`<#RRGGBB>`)
- **API** — Public API for other plugins to read player nicknames

## Commands

| Command | Description |
|---------|-------------|
| `/nick` | Open the nickname editor UI |
| `/nick <name>` | Set a nickname via command |
| `/nick reset` | Reset to your original username |

## Installation

1. Place `NickNameChanger-x.x.x.jar` into `Hytale\UserData\Mods`
2. Launch the game and open world settings
3. Enable the mod in the Mods section
4. Load the world

## Permissions

All permissions are **allowed by default** — the mod works out of the box with no configuration.

| Permission | Description | Default |
|---|---|---|
| `nickname.use` | Access to `/nick` command | ✅ Allowed |
| `nickname.format` | Use colors, gradients, bold/italic/underline | ✅ Allowed |

To **deny** a permission, use the `-` prefix:

**With LuckPerms:**
```
/lp group default permission set -nickname.format true    # deny formatting for default group
/lp user Steve permission set -nickname.use true          # deny /nick for specific player
```

**Without LuckPerms** (`permissions.json`):
```json
{
  "groups": {
    "Default": ["-nickname.format"],
    "VIP": ["nickname.format"]
  }
}
```

## Configuration

The plugin generates a `config.json` in its data folder on first run. You can customize where nicknames are displayed:

```json
{
  "chatFormat": "{prefix}<{username}>{suffix} {message}",
  "display": {
    "showInChat": true,
    "showOnNameplate": true,
    "showInTabList": true
  },
  "integrations": {
    "luckperms": {
      "enabled": true,
      "showPrefix": true,
      "showSuffix": true
    }
  }
}
```

Set any `display` option to `false` to disable nicknames in that context.

## API for Developers

Other plugins can read nickname data through the `NicknameAPI` class (`com.nickname.plugin.api.NicknameAPI`).

Add NickNameChanger as a `compileOnly` dependency in your `build.gradle.kts`:

```kotlin
compileOnly(fileTree("libs") { include("NickNameChanger-*.jar") })
```

### Methods

#### `getNickname(UUID uuid): String?`

Returns the raw nickname string with markup tags (e.g. `<color:#FF5555>Steve</color>`), or `null` if the player has no nickname.

```java
String nick = NicknameAPI.getNickname(playerUuid);
if (nick != null) {
    System.out.println("Player has nickname: " + nick);
}
```

#### `getDisplayName(UUID uuid, String defaultName): String`

Returns the player's nickname if set, otherwise returns `defaultName`. Never returns `null`.

```java
String name = NicknameAPI.getDisplayName(playerUuid, player.getUsername());
// "name" is always a valid string — either the nickname or the fallback
```

#### `hasNickname(UUID uuid): boolean`

Returns `true` if the player has a custom nickname set.

```java
if (NicknameAPI.hasNickname(playerUuid)) {
    // player is using a custom nickname
}
```

#### `getOriginalUsername(UUID uuid): String?`

Returns the player's real username before the nickname was set, or `null` if unknown.

```java
String original = NicknameAPI.getOriginalUsername(playerUuid);
// useful for logging, moderation, etc.
```

### Notes

- All methods are **static** — no instance needed
- All methods are **thread-safe**
- The API is read-only — nicknames can only be set through `/nick` command or the UI
- If NickNameChanger is not loaded, all methods return `null` / `false` / `defaultName` safely
- Nickname strings may contain markup: `<color:...>`, `<gradient:...>`, `<b>`, `<i>`, `<u>`

## Compatibility

- **[LuckPerms](https://luckperms.net/)** — Full support:
  - Nicknames sync to LuckPerms `display-name` meta
  - Chat displays LuckPerms prefix/suffix alongside the nickname
  - Hex color prefixes (`<#RRGGBB>`) are fully supported
  - Permissions can be managed through LuckPerms
- **EtherealPerms** — Compatible. The plugin preserves other chat formatters when a player has no nickname set.
- **Other chat plugins** — NickNameChanger only overrides chat formatting for players with active nicknames or LuckPerms integration.

## Limitations

- **Inventory header** — Cannot be changed as it is rendered client-side

## Localization

- English (en-US)
- Russian (ru-RU)

## Credits

- **Author:** Morgott
