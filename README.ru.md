# NickNameChanger

[![en](https://img.shields.io/badge/lang-English-blue)](README.md) [![ru](https://img.shields.io/badge/lang-Русский-green)](README.ru.md)

Серверный плагин для Hytale, позволяющий игрокам менять отображаемый никнейм с цветами, градиентами и стилями текста.

![Редактор никнейма](img/editor_gradient.png)

## Возможности

- **UI Редактор** — Графический интерфейс для настройки никнейма
  - 12 готовых цветов + выбор произвольного цвета
  - 8 готовых градиентов + произвольные цвета градиента
  - Стили текста: Жирный, Курсив, Подчёркнутый
  - Превью в реальном времени
- **Чат** — Никнейм отображается в сообщениях чата
- **Табличка над головой** — Имя над персонажем
- **Карта** — Имя на карте мира
- **Таб-лист** — Никнейм в списке игроков
- **Конфигурация** — Настройка отображения никнейма через `config.json`
- **LuckPerms** — Префикс/суффикс в чате с поддержкой hex-цветов (`<#RRGGBB>`)
- **API** — Публичный API для чтения никнеймов другими плагинами

## Команды

| Команда | Описание |
|---------|----------|
| `/nick` | Открыть редактор никнейма |
| `/nick <имя>` | Установить никнейм через команду |
| `/nick reset` | Сбросить на оригинальное имя |

## Установка

1. Поместите `NickNameChanger-x.x.x.jar` в папку `Hytale\UserData\Mods`
2. Запустите игру и откройте настройки мира
3. Включите мод в разделе Mods
4. Загрузите мир

## Права доступа

Все права **разрешены по умолчанию** — мод работает сразу без настройки.

| Право | Описание | По умолчанию |
|---|---|---|
| `nickname.use` | Доступ к команде `/nick` | ✅ Разрешено |
| `nickname.format` | Цвета, градиенты, bold/italic/underline | ✅ Разрешено |

Чтобы **запретить** право, используйте префикс `-`:

**С LuckPerms:**
```
/lp group default permission set -nickname.format true    # запретить форматирование для группы
/lp user Steve permission set -nickname.use true          # запретить /nick конкретному игроку
```

**Без LuckPerms** (`permissions.json`):
```json
{
  "groups": {
    "Default": ["-nickname.format"],
    "VIP": ["nickname.format"]
  }
}
```

## Конфигурация

Плагин создаёт `config.json` в папке данных при первом запуске. Можно настроить, где отображается никнейм:

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

Установите любую опцию `display` в `false`, чтобы отключить никнейм в этом контексте.

## API для разработчиков

Другие плагины могут читать данные никнеймов через класс `NicknameAPI`:

```java
import com.nickname.plugin.api.NicknameAPI;

// Получить отформатированный никнейм (с цветами/стилями)
Message nickname = NicknameAPI.getNickname(player);

// Получить полное отображаемое имя (префикс + ник + суффикс)
Message displayName = NicknameAPI.getDisplayName(player);

// Проверить, есть ли у игрока кастомный никнейм
boolean hasNick = NicknameAPI.hasNickname(player);

// Получить оригинальное имя пользователя
String original = NicknameAPI.getOriginalUsername(player);
```

## Совместимость

- **[LuckPerms](https://luckperms.net/)** — Полная поддержка:
  - Никнеймы синхронизируются с мета-значением `display-name` в LuckPerms
  - В чате отображаются prefix/suffix из LuckPerms рядом с никнеймом
  - Hex-цвета в префиксах (`<#RRGGBB>`) полностью поддерживаются
  - Правами можно управлять через LuckPerms
- **EtherealPerms** — Совместим. Плагин сохраняет форматирование чата других плагинов, если у игрока нет никнейма.
- **Другие чат-плагины** — NickNameChanger перехватывает форматирование только для игроков с активным никнеймом или интеграцией LuckPerms.

## Ограничения

- **Шапка инвентаря** — Невозможно изменить, так как отрисовывается на стороне клиента

## Локализация

- Английский (en-US)
- Русский (ru-RU)

## Credits

- **Автор:** Morgott
