# Changelog

## [0.0.2] - 2026-01-29

### Added
- **UI Editor** — New graphical interface for nickname customization (`/nick` without arguments)
  - Color picker with preset colors (12 colors)
  - Gradient mode with 8 preset gradients and custom color pickers
  - Text styles: Bold, Italic, Underline
  - Live preview
- **Localization** — Full Russian translation for UI
- **LuckPerms integration** — Lazy initialization (works even if LuckPerms loads after NickNameChanger)

### Fixed
- Cyrillic characters in UI title (replaced `$C.@Title` template with plain Label for Unicode support)
- ColorPicker brightness handling (inverted brightness byte)

### Changed
- Removed TinyMessageHook (using built-in MessageUtil)
- Cleaned up debug output

## [0.0.1] - 2026-01-19

Initial release
- Basic `/nick` command
- Chat, nameplate, map, tab list support
- English and Russian localization
