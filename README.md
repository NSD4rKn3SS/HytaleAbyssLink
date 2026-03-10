# AbyssLink a Hytale Discord Integration

## Overview

Two-way chat synchronization for Hytale servers with Discord communities. 

Originally forked from [dkoz/HytaleDiscordIntegration](https://github.com/dkoz/HytaleDiscordIntegration), this project focuses on simplicity and maintainability with support for the latest server binary versions.

## Requirements

- **Java 25+**
- **Hytale Server API** (`HytaleServer.jar`)

## Features

| Feature | Description |
|---------|-------------|
| **Chat Sync** | Real-time message synchronization between Hytale and Discord |
| **Player Notifications** | Join/leave event alerts in Discord |
| **Commands** | Execute server commands through Discord |
| **Player List** | Keep Discord updated with active players |
| **Server Status** | Live status updates to Discord channels |
| **Customization** | Flexible message formatting and channel configuration |
| **Latest API Support** | Compatible with the newest Hytale Server API versions |
| **Multilanguage support** | Added support for multiple languages |
| **Permission system** | Set permissions for using mod functionality |
| **Custom default avatars** | For both unlinked players and the bot |
| **Smart avatar caching** | Reduces requests and respects Discord rate limits (with fallback) |
| **Webhook-based messages** | For seamless chatting in Discord |


## Languages

* Pre-translated locales can be found in the repository (translation made by AI), the mod ships with English only by default
* Currently supported are:
  * English, German, Czech, French, Spanish, Hungarian

## Tutorial
### 1. Download and place the mod

1. Download the latest (`AbyssLink-1.0.1.jar`) release file.
2. Stop your Hytale server if it is running.
3. Copy the `.jar` into your server’s **mods** folder.

Example:
```text
<your-server>/
└─ mods/
  └─ AbyssLink-<version>.jar
```

### 2. Start the server once (generate config)

1. Start the server.
2. Wait until startup is complete.
3. Stop the server again.

On first run, AbyssLink creates its configuration directory inside the **mods** folder.

Example:
```text
<your-server>/
└─ mods/
  └─ AbyssLink/
    └─ locales
      └─ en_US.json
    └─ config.yml
```

### 3. Configure AbyssLink

1. Open the generated config file in a text editor.
2. Fill in your Discord settings (bot token, guild/server ID, channel IDs).
3. Set message/formatting options as needed.
4. (Optionally) Set the language you'd like to use (don't forget to copy the language file into the locales folder)
5. Save the file.

### 4. Discord bot setup checklist

- Create a Discord application and bot in the Discord Developer Portal.
- Copy the bot token into the config.
- Invite the bot to your server with required permissions:
  - Read Messages / View Channels  
  - Send Messages  
  - Manage Webhooks (if used)  
  - Message History access
- Enable any required intents in the bot settings (for member/player sync features).

### 5. Start and verify

1. Start the Hytale server again.
2. Check server logs for successful AbyssLink startup.
3. Send a test message in-game and in Discord to confirm two-way sync.
4. Confirm join/leave notifications and command features work in the configured channel.

### 6. Updating the mod

1. Stop the server.
2. Replace the old AbyssLink `.jar` in `mods/` with the new version.
3. Start the server and review logs for config migration warnings.
4. Re-check config values if new options were added.

### Troubleshooting

- **No config generated:** verify the `.jar` is directly in `mods/` and server started without load errors.
- **Bot not responding:** recheck token, channel IDs, and bot permissions.
- **No chat sync:** confirm the configured channels are correct and not blocked by Discord role permissions.
- **Startup errors after update:** compare your config with the latest example/default config and add missing fields.