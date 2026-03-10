# DiscordIntegration

Discord Integration offers a two way chat bridge between your Hytale server and Discord server. This mod also offers you the ability to link your in-game account to the discord bot.

## Requirements

- Java 25+
- Hytale Server API (`HytaleServer.jar`)

## A word from me

Using this API list: https://rentry.co/gykiza2m

and this guide: https://hytalemodding.dev/en/docs/guides/plugin/setting-up-env

PS.: I did have to use AI help, because I'm merely a webdev and have only very basic Java experience.

## Additions by this fork, as of latest release

- **Configurable death messages**
  - Toggle death messages on/off with `enableDeathMessages`.
  - Fully customizable format via `deathMessage`, including placeholders like `{player}` and `{cause}`.

- **Discord server status notifications**
  - Optional messages when the server starts and stops, using `serverStartMessage` and `serverStopMessage`.

- **Safe config upgrades**
  - Updating the plugin now only adds new settings; existing values are preserved and not overwritten.

- **Discord quality-of-life improvements**
  - Run account linking directly from the messaging channel (not just the command channel).
  - Optionally display current player count in the messaging channel topic with:
    - `showPlayerCountInTopic` (on/off)
    - `topicPlayerCountFormat` (e.g. `"Players online: {online}"`).

- **Webhook-based message relaying**
  - Enable with `useWebhooks` to relay messages as the player, including their Discord avatar when accounts are linked.
  - Customize server/system message identity:
    - `serverName` for display name
    - `serverAvatarUrl` for bot avatar.
  - Configure a default avatar for unlinked players via `defaultPlayerAvatarUrl`.
  - Smart avatar caching reduces requests and respects Discord rate limits (falls back to Discord’s default avatar if none is set).

- **Permission-gated `/discord` command**
  - New permission: `discordintegration.discord`.
  - Uses Hytale’s built-in permission system (via `/perm`).
  - Supports:
    - `/discord get <field>`
    - `/discord set <field> <value>`
    - `/discord list`
    - `/discord reload`
  - Clear “permission denied” message when users lack the required permission.
- **Full Internationalization (i18n) Support**
  - All user-facing text is now translatable!
  - Configure language via `locale` in config.json (e.g., `"locale": "es_ES"`)
  - Includes complete Spanish and French translations
  - Easy to add new languages - just copy and translate the JSON file
  - Support for dynamic placeholders (e.g., `{player}`, `{message}`)
  - Hot-reload translations with `/discord reload`
  - See **I18N_GUIDE.md** for complete documentation
