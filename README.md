# TablistCustomizer

A configurable tablist plugin for **Paper 1.21.x** with **LuckPerms** support.

Created for: **AnonLaLaNI**

## Features

- Configurable header/footer
- Group-based tab formatting
- LuckPerms placeholders (`%group%`, `%lp_prefix%`, `%lp_suffix%`)
- Optional PlaceholderAPI support
- Reload command without restart

## Default Header Name

The default server name in config is:

```yml
- "<gold><bold> Your Network."
```

## Group Placeholders in Config

The config ships with placeholder groups:

- `group_1`
- `group_2`
- `group_3`
- `group_4`
- `group_5`

Replace them with your real LuckPerms group names.

## Commands

- `/tablistcustomizer reload`
- `/tlc reload`
- `/tabreload reload`

## Permission

- `tablistcustomizer.reload` (default: op)

## Build

```bash
mvn package
```
