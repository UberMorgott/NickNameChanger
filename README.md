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
- **LuckPerms** — Optional integration for chat formatting compatibility

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

## Optional Dependencies

- **[LuckPerms](https://luckperms.net/)** — If installed, nicknames will sync to LuckPerms `display-name` meta for chat formatting compatibility

## Limitations

- **Inventory header** — Cannot be changed as it is rendered client-side

## Localization

- English (en-US)
- Russian (ru-RU)

## Credits

- **Author:** Morgott
