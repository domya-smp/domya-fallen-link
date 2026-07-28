# domya-fallen-link

Paper plugin for synchronizing Minecraft player data with the Domya website.

## Requirements

- Java 21
- Paper 1.21.x

## Build

```
./gradlew build
```

Output: `build/libs/domya-fallen-link-1.1.0.jar`

## Commands

- `/domyasync status`
- `/domyasync reload`
- `/domyasync sync`
- `/domyasync pause`
- `/domyasync resume`
- `/link <code>`

`pause` stops periodic and event-driven synchronization as well as account-link
data exchange. The state is saved in `sync-enabled` and survives reloads and
server restarts. The admin command remains available so synchronization can be
resumed without reloading the plugin.

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
