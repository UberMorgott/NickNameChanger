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

## Links

- [CurseForge](https://www.curseforge.com/hytale/mods/nick-name-changer)
- [Issues](https://github.com/UberMorgott/NickNameChanger/issues)
