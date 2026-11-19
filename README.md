# HideNames

[![Build](https://github.com/prismaworld-su/hide-names/actions/workflows/build.yml/badge.svg)](https://github.com/prismaworld-su/hide-names/actions/workflows/build.yml)

A Paper plugin that hides the name tags above players' heads. To see who someone is, right-click them and their name appears in the action bar. Staff with permission can use `/esp` to see every name tag, while everyone else still sees none.

## Features

- **Hidden name tags.** No player sees the name tags above other players' heads.
- **Right-click to inspect.** Right-clicking a player shows their name in the action bar. The format is set in the config with [MiniMessage](https://docs.papermc.io/adventure/minimessage/format/) and supports `%displayName%` and [PlaceholderAPI](https://modrinth.com/plugin/placeholderapi) placeholders.
- **`/esp` for staff.** Turns on name tags for the player who runs it and nobody else. The setting is saved, so it stays on after reconnects and restarts.
- **Vanilla behaviour stays the same.** Invisible players stay invisible and are not shown as translucent to other players.

## Requirements

| Dependency | Version | Required |
|---|---|---|
| [Paper](https://papermc.io/) | 26.3 | Yes |
| [PacketEvents](https://modrinth.com/plugin/packetevents) | 2.14.0+ | Yes |
| [PlaceholderAPI](https://modrinth.com/plugin/placeholderapi) | 2.12.3+ | No |

## Installation

1. Install PacketEvents and, if you want placeholders, PlaceholderAPI.
2. Put `hide-names-<version>-all.jar` into the `plugins` folder.
3. Restart the server.
4. Edit `plugins/hide-names/config.yml` if needed, then run `/hidenames reload`.

## Commands and permissions

| Command | Permission | Default | Description |
|---|---|---|---|
| `/esp` | `hidenames.esp` | op | Shows or hides every player's name tag for you only. |
| `/hidenames reload` | `hidenames.reload` | op | Reloads `config.yml`. |

ESP comes back after a reconnect only if the player still has `hidenames.esp`.

## Configuration

```yaml
# Action bar message shown when a player right-clicks another player (MiniMessage).
action-bar-format: "<gray>Игрок: <white>%displayName%"

# Plugin messages (MiniMessage).
messages:
  esp-enabled: "<green>Режим ESP включён: вы видите ники всех игроков."
  esp-disabled: "<yellow>Режим ESP выключен: ники игроков снова скрыты."
  players-only: "<red>Эту команду может выполнить только игрок."
  reloaded: "<green>Конфигурация HideNames перезагружена."
```

### Placeholders in `action-bar-format`

| Placeholder | Value |
|---|---|
| `%displayName%` | Display name of the clicked player |
| `%<placeholder>%` | Any PlaceholderAPI placeholder, resolved for the clicked player, e.g. `%player_name%` or `%vault_prefix%` |
| `%rel_<placeholder>%` | Relational PlaceholderAPI placeholder between the clicking player and the clicked player |

Placeholder values may use MiniMessage styling tags (`<yellow>`, `<bold>`, `<gradient>` and others) and legacy colour codes (`&a`, `§a`, `&#rrggbb`), so a LuckPerms prefix such as `<yellow>⭐</yellow>` is shown in colour. Each value is parsed on its own, so it cannot break the rest of the message, and interactive tags such as `<click>` and `<hover>` in values stay plain text.

## How it works

- Every online player is added to the `hide_names` team on the main scoreboard. Its name tag visibility is set to `never`.
- For players with ESP enabled, a PacketEvents listener changes this team's visibility to `always` in packets sent to them. The server's real scoreboard is not changed.
- Players are removed from the team when they leave, and the team is deleted when the plugin is disabled. No entries are left behind in the scoreboard data.

## Limitations

- The plugin needs the main scoreboard. A plugin that gives players their own scoreboard, or puts them in another team (for example TAB nametags or prefix plugins), will make their name tags visible again.
- If `hidenames.esp` is removed from a player who has ESP on, ESP stays on until they reconnect.

## Building

The build requires JDK 25.

```bash
./gradlew build
```

The plugin jar is written to `build/libs/hide-names-<version>-all.jar`. To start a local test server with PacketEvents and PlaceholderAPI installed, run:

```bash
./gradlew runServer
```
