# domya-fallen-link

Paper plugin for synchronizing Minecraft player data with the Domya website.

## Requirements

- Java 21
- Paper 1.21.x

## Build

```
./gradlew build
```

Output: `build/libs/domya-fallen-link-1.0.0.jar`

## Commands

- `/domyasync status`
- `/domyasync reload`
- `/domyasync sync`
- `/link <code>`

## Compatibility

The plugin keeps the old DomyaPlayerSync command and config surface so existing
server configuration can be reused:

- `api-url`
- `link-url`
- `secret-token`
- `sync-interval-seconds`
- `sync-on-join`
- `sync-on-quit`
- `debug`

The real `secret-token` must be configured on the server and is not stored in
this repository.
