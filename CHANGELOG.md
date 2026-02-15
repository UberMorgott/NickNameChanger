# Changelog

## [0.0.10] - 2026-02-15

> **Requires Hytale Pre-Release** (February 2026+)

### Added
- **Message Color** — Per-player chat message coloring
  - New UI tab "Message" in the nickname editor with color presets, gradient, and custom color picker
  - Command: `/nick msgcolor <hex>`, `/nick msgcolor gradient:#HEX1:#HEX2`, `/nick msgcolor reset`
  - Chat confirmation message with color preview on apply
- **Nickname Validation** — Configurable rules in `config.json`:
  - Min/max length enforcement
  - Banned words list (case-insensitive)
  - Cyrillic character support (configurable)
  - Unique nickname enforcement (no duplicates)
- **Tag Injection Protection** — Only safe markup tags allowed: `color`, `gradient`, `b`, `bold`, `i`, `italic`, `u`, `underline`
- **Map Display** — Separate `showOnMap` config option (independent from tab list)
- **Portuguese (pt-BR)** — Full localization added
- **UI Tab Highlighting** — Active tab shown with gold highlight bar
- **Admin Settings GUI** — `/nick settings` command opens a GUI for administrators to control global nickname display
  - Toggle: Show in Chat, Show on Nameplate, Show in Tab List / Map
  - Changes apply **instantly** to all online players — no restart or rejoin needed
  - Settings persist to `config.json`
- **Permission `nickname.admin`** — Controls access to `/nick settings` (denied by default)
- **API: Display Settings** — New `NicknameAPI` methods: `isShowInChat()`, `isShowOnNameplate()`, `isShowInTabList()`

### Fixed
- **Chat shadow variable bug** — Message color was applied using wrong UUID in some cases
- **Gradient tab state** — UI now opens on correct tab (Color/Gradient) based on saved nickname
- **`/nick reset`** — Now also clears message color and original username mapping
- **Per-file saving** — Nicknames, original usernames, and message colors saved independently (prevents data loss)
- **`removeNickname()`** — No longer removes original username mapping (kept for re-use)

### Changed
- **Recompiled for Hytale Pre-Release** — API changed from `broadcastPacket(Packet)` to `broadcastPacket(ToClientPacket)`
- Display settings are now **global** (admin-controlled) instead of per-player
- Removed dead code: unused LuckPerms methods, unused i18n keys, redundant parsing methods

## [0.0.9] - 2026-02-06

### Fixed
- **Tab list with LuckPerms** — Other players now correctly see nicknames in the player list when LuckPerms is installed
- **Map/minimap** — Nicknames now display on the world map for all players

### Technical
- Added `PlayerRefUtil` — modifies `PlayerRef.username` via reflection so all server systems (map markers, player list, LuckPerms sync) use the nickname natively

## [0.0.8] - 2026-02-06

### Added
- **Display configuration** — `display.showInChat`, `display.showOnNameplate`, `display.showInTabList` options in `config.json`
- **NicknameAPI** — Public API for other plugins: `getNickname()`, `getDisplayName()`, `hasNickname()`, `getOriginalUsername()`

### Fixed
- **LuckPerms hex colors** — Short hex format `<#XXXXXX>` in prefixes/suffixes now parsed correctly
- **Gradient nametag** — Nametags no longer disappear when using gradient nicknames (uses plain text for DisplayNameComponent)
- **EtherealPerms compatibility** — Chat formatter is only replaced when player has a nickname or LuckPerms is active
- **UI Reset button** — Now properly calls `resetNickname()` instead of only resetting UI state
- **Thread safety** — All NicknameStorage methods synchronized, volatile fields in LuckPermsHook
- **Null safety** — Gson deserialization handles missing nested config objects
- **Gradient builder** — Fixed `result = result.insert(ch)` in `buildGradient()`

## [0.0.7] - 2026-02-02

### Added
- **Configurable chat format** — new `chatFormat` field in `config.json` with placeholders: `{prefix}`, `{username}`, `{suffix}`, `{message}`
- **Config loading** — `config.json` is now actually loaded and used at runtime
- Config options `showPrefix` / `showSuffix` now work as intended

### Fixed
- **LuckPerms compatibility** — chat event handler now runs at `LAST` priority to prevent other plugins from overriding the nickname formatter

## [0.0.3] - 2026-01-29

### Fixed
- Plugin now works without LuckPerms (fixed NoClassDefFoundError crash)

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
