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

Other plugins can read nickname data through the `NicknameAPI` class:

```java
import com.nickname.plugin.api.NicknameAPI;

// Get the formatted nickname (with colors/styles)
Message nickname = NicknameAPI.getNickname(player);

// Get the full display name (prefix + nick + suffix)
Message displayName = NicknameAPI.getDisplayName(player);

// Check if a player has a custom nickname
boolean hasNick = NicknameAPI.hasNickname(player);

// Get the original username
String original = NicknameAPI.getOriginalUsername(player);
```

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
