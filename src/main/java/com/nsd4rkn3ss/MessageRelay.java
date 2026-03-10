package com.nsd4rkn3ss;

import java.util.UUID;

public class MessageRelay {
    private final DiscordConfig config;

    public MessageRelay(DiscordConfig config) {
        this.config = config;
    }

    public void sendToDiscord(String playerName, String message) {
        sendToDiscord(null, playerName, message);
    }

    public void sendToDiscord(UUID playerUuid, String playerName, String message) {
        System.out.println("[AbyssLink Discord] MessageRelay.sendToDiscord called for: " + playerName);
        DiscordBot bot = DiscordIntegration.getInstance().discordBot;
        if (bot == null) {
            System.out.println("[AbyssLink Discord] Bot is null!");
            return;
        }
        if (!bot.isConnected()) {
            System.out.println("[AbyssLink Discord] Bot is not connected!");
            return;
        }

        // Use webhook dispatch if enabled, otherwise use regular bot message
        if (config.isUseWebhooks()) {
            String fallbackAvatar = config.getDefaultPlayerAvatarUrl();
            bot.dispatchToDiscord(playerUuid, playerName, message, fallbackAvatar);
        } else {
            String formatted = config.getMessageFormat().getServerToDiscord()
                .replace("{player}", playerName)
                .replace("{message}", message);
            System.out.println("[AbyssLink Discord] Sending to Discord: " + formatted);
            bot.sendMessage(formatted);
        }
    }

    public void sendJoinMessage(String playerName) {
        DiscordBot bot = DiscordIntegration.getInstance().discordBot;
        if (bot != null && bot.isConnected()) {
            String formatted = config.getMessageFormat().getJoinMessage()
                .replace("{player}", playerName);
            
            // Use webhook with server identity for join messages
            if (config.isUseWebhooks()) {
                bot.dispatchToDiscord(null, config.getServerName(), formatted, config.getServerAvatarUrl());
            } else {
                bot.sendMessage(formatted);
            }
        }
    }

    public void sendLeaveMessage(String playerName) {
        DiscordBot bot = DiscordIntegration.getInstance().discordBot;
        if (bot != null && bot.isConnected()) {
            String formatted = config.getMessageFormat().getLeaveMessage()
                .replace("{player}", playerName);
            
            // Use webhook with server identity for leave messages
            if (config.isUseWebhooks()) {
                bot.dispatchToDiscord(null, config.getServerName(), formatted, config.getServerAvatarUrl());
            } else {
                bot.sendMessage(formatted);
            }
        }
    }

    public void sendDeathMessage(String playerName, String cause) {
        if (!config.isEnableDeathMessages()) {
            return;
        }

        DiscordBot bot = DiscordIntegration.getInstance().discordBot;
        if (bot != null && bot.isConnected()) {
            String causeText = (cause == null || cause.isEmpty()) ? "unknown" : cause;
            String formatted = config.getMessageFormat().getDeathMessage()
                .replace("{player}", playerName)
                .replace("{cause}", causeText);
            
            // Use webhook with server identity for death messages
            if (config.isUseWebhooks()) {
                bot.dispatchToDiscord(null, config.getServerName(), formatted, config.getServerAvatarUrl());
            } else {
                bot.sendMessage(formatted);
            }
        }
    }

    public void sendServerStartMessage() {
        DiscordBot bot = DiscordIntegration.getInstance().discordBot;
        if (bot != null && bot.isConnected()) {
            String formatted = config.getMessageFormat().getServerStartMessage();
            
            // Use webhook with server identity for server start messages
            if (config.isUseWebhooks()) {
                bot.dispatchToDiscord(null, config.getServerName(), formatted, config.getServerAvatarUrl());
            } else {
                bot.sendMessage(formatted);
            }
        }
    }

    public void sendServerStopMessage() {
        DiscordBot bot = DiscordIntegration.getInstance().discordBot;
        if (bot != null && bot.isConnected()) {
            String formatted = config.getMessageFormat().getServerStopMessage();
            
            // Use webhook with server identity for server stop messages
            if (config.isUseWebhooks()) {
                bot.dispatchToDiscord(null, config.getServerName(), formatted, config.getServerAvatarUrl());
            } else {
                bot.sendMessage(formatted);
            }
        }
    }
}
