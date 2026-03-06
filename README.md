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
| LuckPerms | ✅ Prefix/suffix in chat, tab list |
| EssentialsPlus | ✅ Colored nicknames + message color |
| Mini-Chat-Formatter | ⚠️ Compatible, but MCF has known issues |

## Installation

1. Download the latest JAR from [Releases](https://github.com/UberMorgott/NickNameChanger/releases)
2. Place in `Hytale/UserData/Mods/`
3. Restart the server
4. Use `/nick` to open the editor

## Configuration

Config file is created automatically at `mods/NickNameChanger_NickNameChanger/config.json`

## Links

- [CurseForge](https://www.curseforge.com/hytale/mods/nick-name-changer)
- [Issues](https://github.com/UberMorgott/NickNameChanger/issues)
