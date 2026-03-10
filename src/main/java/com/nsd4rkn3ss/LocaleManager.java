package com.nsd4rkn3ss;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages localization and translations for the Discord Integration plugin.
 * Supports loading locale files and replacing placeholders in translated messages.
 */
public class LocaleManager {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File localeFolder;
    private final Map<String, String> translations = new HashMap<>();
    private String currentLocale = "en_US";

    public LocaleManager(File dataFolder) {
        this.localeFolder = new File(dataFolder, "locales");
        if (!localeFolder.exists()) {
            localeFolder.mkdirs();
        }
    }

    /**
     * Loads a locale from file. If the file doesn't exist, creates it from defaults.
     * @param locale The locale code (e.g., "en_US", "es_ES", "fr_FR")
     */
    public void loadLocale(String locale) {
        this.currentLocale = locale;
        translations.clear();

        File localeFile = new File(localeFolder, locale + ".json");
        
        // If the locale file doesn't exist, create it from defaults
        if (!localeFile.exists()) {
            System.out.println("[AbyssLink Discord] Locale file not found for " + locale + ", creating default...");
            createDefaultLocaleFile(localeFile);
        }

        try (FileReader reader = new FileReader(localeFile)) {
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            if (json != null) {
                for (String key : json.keySet()) {
                    translations.put(key, json.get(key).getAsString());
                }
                System.out.println("[AbyssLink Discord] Loaded " + translations.size() + " translations for locale: " + locale);
            }
        } catch (IOException e) {
            System.err.println("[AbyssLink Discord] Failed to load locale " + locale + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets a translated message for the given key.
     * @param key The translation key
     * @return The translated message, or the key itself if not found
     */
    public String getMessage(String key) {
        return translations.getOrDefault(key, key);
    }

    /**
     * Gets a translated message with placeholder replacements.
     * Placeholders are in the format {placeholder}.
     * @param key The translation key
     * @param replacements Key-value pairs for placeholder replacement
     * @return The translated message with placeholders replaced
     */
    public String getMessage(String key, Map<String, String> replacements) {
        String message = getMessage(key);
        if (replacements != null) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        return message;
    }

    /**
     * Gets a translated message with placeholder replacements using varargs.
     * @param key The translation key
     * @param replacements Alternating key-value pairs (key1, value1, key2, value2, ...)
     * @return The translated message with placeholders replaced
     */
    public String getMessage(String key, String... replacements) {
        if (replacements.length % 2 != 0) {
            throw new IllegalArgumentException("Replacements must be provided in key-value pairs");
        }
        
        Map<String, String> replacementMap = new HashMap<>();
        for (int i = 0; i < replacements.length; i += 2) {
            replacementMap.put(replacements[i], replacements[i + 1]);
        }
        
        return getMessage(key, replacementMap);
    }

    /**
     * Reloads the current locale from disk.
     */
    public void reloadLocale() {
        loadLocale(currentLocale);
    }

    /**
     * Gets the current locale code.
     * @return The current locale code
     */
    public String getCurrentLocale() {
        return currentLocale;
    }

    /**
     * Creates a default locale file with all plugin messages.
     */
    private void createDefaultLocaleFile(File localeFile) {
        JsonObject translations = new JsonObject();

        // Commands
        translations.addProperty("command.link.usage", "Usage: /link <code>");
        translations.addProperty("command.link.getcode", "Get your code by typing !link in Discord!");
        translations.addProperty("command.link.invalid", "Invalid or expired link code!");
        translations.addProperty("command.link.expired", "Codes expire after 5 minutes. Get a new code from Discord.");
        translations.addProperty("command.link.error.playerdata", "Error: Player data not found!");
        translations.addProperty("command.link.already_linked", "Your account is already linked to Discord!");
        translations.addProperty("command.link.linked_to", "Linked to: {discordUsername}");
        translations.addProperty("command.link.success", "Successfully linked your account to Discord!");
        translations.addProperty("command.link.discord_user", "Discord: {discordUsername}");

        translations.addProperty("command.profile.not_found", "Player '{player}' not found!");
        translations.addProperty("command.profile.error.playerdata", "Error: Your player data not found!");
        translations.addProperty("command.profile.title", "Player Profile: {player}");
        translations.addProperty("command.profile.playtime", "Total Playtime: {playtime}");
        translations.addProperty("command.profile.first_login", "First Login: {date}");
        translations.addProperty("command.profile.discord.linked", "Discord: Linked");
        translations.addProperty("command.profile.discord.not_linked", "Discord: Not linked");

        translations.addProperty("command.discord.no_permission", "§cYou don't have permission to use this command.");
        translations.addProperty("command.discord.required_permission", "§cRequired permission: discordintegration.discord");
        translations.addProperty("command.discord.help.title", "=== Discord Config Commands ===");
        translations.addProperty("command.discord.help.get", "/discord get <field> - Get config value");
        translations.addProperty("command.discord.help.set", "/discord set <field> <value> - Set config value");
        translations.addProperty("command.discord.help.list", "/discord list - Show all config values");
        translations.addProperty("command.discord.help.reload", "/discord reload - Reload config from file");
        translations.addProperty("command.discord.help.fields", "Fields: enabled, allowOtherBotMessages, showChatTag, enableInGameChat, enableDeathMessages, chatTagText, channelId, commandChannelId, adminRoleId, showPlayerCountInTopic, topicPlayerCountFormat");
        translations.addProperty("command.discord.get.value", "{field}: {value}");
        translations.addProperty("command.discord.get.error", "Error getting field '{field}': {error}");
        translations.addProperty("command.discord.set.usage", "Usage: /discord set <field> <value>");
        translations.addProperty("command.discord.set.success", "Set {field} to: {value}");
        translations.addProperty("command.discord.set.error", "Error setting field '{field}': {error}");
        translations.addProperty("command.discord.list.title", "=== Discord Config Values ===");
        translations.addProperty("command.discord.reload.success", "Config reloaded successfully!");
        translations.addProperty("command.discord.reload.error", "Error reloading config: {error}");
        translations.addProperty("command.discord.field.unknown", "Unknown field: {field}");

        // Discord Bot Messages
        translations.addProperty("discord.link.embed.title", "Account Linking");
        translations.addProperty("discord.link.embed.code_field", "Your Link Code");
        translations.addProperty("discord.link.embed.howto_field", "How to Link");
        translations.addProperty("discord.link.embed.howto_value", "Use `/link {code}` in-game");
        translations.addProperty("discord.link.embed.important_field", "Important");
        translations.addProperty("discord.link.embed.important_value", "Code expires in 5 minutes");
        translations.addProperty("discord.link.embed.footer", "Discord Integration");
        translations.addProperty("discord.link.sent.title", "Link Code Sent");
        translations.addProperty("discord.link.sent.description", "Check your DMs for your link code!");
        translations.addProperty("discord.link.dm_failed.title", "DM Failed");
        translations.addProperty("discord.link.dm_failed.description", "Could not send you a DM. Please enable DMs from server members.");

        translations.addProperty("discord.profile.not_found.title", "Player Not Found");
        translations.addProperty("discord.profile.not_found.description", "No player with username '{username}' is linked to Discord.");
        translations.addProperty("discord.profile.usage.title", "Profile Command");
        translations.addProperty("discord.profile.usage.description", "Usage: `!profile <username>`\nExample: `!profile Steve`");
        translations.addProperty("discord.profile.not_linked.title", "Account Not Linked");
        translations.addProperty("discord.profile.not_linked.description", "Your Discord account is not linked to any in-game account.\nUse `!link` to get started!");
        translations.addProperty("discord.profile.embed.title", "Player Profile: {username}");
        translations.addProperty("discord.profile.embed.playtime", "Total Playtime");
        translations.addProperty("discord.profile.embed.first_login", "First Login");
        translations.addProperty("discord.profile.embed.discord_status", "Discord Status");
        translations.addProperty("discord.profile.embed.footer", "Discord Integration");

        translations.addProperty("discord.players.title", "Online Players ({count}/{max})");
        translations.addProperty("discord.players.empty", "No players online");
        translations.addProperty("discord.players.footer", "Discord Integration");

        translations.addProperty("discord.notification.linked.title", "Account Linked!");
        translations.addProperty("discord.notification.linked.description", "Your Discord account has been successfully linked to: **{username}**");
        translations.addProperty("discord.notification.unlinked.title", "Account Unlinked");
        translations.addProperty("discord.notification.unlinked.description", "Your Discord account has been unlinked from: **{username}**");

        try (FileWriter writer = new FileWriter(localeFile)) {
            gson.toJson(translations, writer);
            System.out.println("[AbyssLink Discord] Created default locale file: " + localeFile.getName());
        } catch (IOException e) {
            System.err.println("[AbyssLink Discord] Failed to create default locale file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
