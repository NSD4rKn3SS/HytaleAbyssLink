package com.nsd4rkn3ss;

import java.util.UUID;

public class MessageRelay {
    private final DiscordConfig config;
    private final MessagesConfig messagesConfig;

    public MessageRelay(DiscordConfig config, MessagesConfig messagesConfig) {
        this.config = config;
        this.messagesConfig = messagesConfig;
    }

    public void sendToDiscord(String playerName, String message) {
        sendToDiscord(null, playerName, message);
    }

    public void sendToDiscord(UUID playerUuid, String playerName, String message) {
        System.out.println("[AbyssLink Discord] MessageRelay.sendToDiscord called for: " + playerName);
        DiscordBot bot = AbyssLink.getInstance().discordBot;
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
            String formatted = messagesConfig.getMessageFormat().getServerToDiscord()
                .replace("{player}", playerName)
                .replace("{message}", message);
            System.out.println("[AbyssLink Discord] Sending to Discord: " + formatted);
            bot.sendMessage(formatted);
        }
    }

    public void sendJoinMessage(String playerName) {
        DiscordBot bot = AbyssLink.getInstance().discordBot;
        if (bot != null && bot.isConnected()) {
            String formatted = messagesConfig.getMessageFormat().getJoinMessage()
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
        DiscordBot bot = AbyssLink.getInstance().discordBot;
        if (bot != null && bot.isConnected()) {
            String formatted = messagesConfig.getMessageFormat().getLeaveMessage()
                .replace("{player}", playerName);
            
            // Use webhook with server identity for leave messages
            if (config.isUseWebhooks()) {
                bot.dispatchToDiscord(null, config.getServerName(), formatted, config.getServerAvatarUrl());
            } else {
                bot.sendMessage(formatted);
            }
        }
    }

    public void sendDeathMessage(String playerName, String deathMessageText, String causeId, String sourceName, float damageAmount) {
        if (!config.isEnableDeathMessages()) {
            return;
        }

        DiscordBot bot = AbyssLink.getInstance().discordBot;
        if (bot != null && bot.isConnected()) {
            String messageText = (deathMessageText == null || deathMessageText.isEmpty()) ? "died" : deathMessageText;
            String cause = (causeId == null || causeId.isEmpty()) ? "unknown" : causeId;
            String source = (sourceName == null || sourceName.isEmpty()) ? "" : sourceName;
            
            String formatted = messagesConfig.getMessageFormat().getDeathMessage()
                    .replace("{player}", playerName)
                    .replace("{message}", messageText)
                    .replace("{cause}", cause)
                    .replace("{source}", source)
                    .replace("{damage}", String.format("%.1f", damageAmount));
            
            // Use webhook with server identity for death messages
            if (config.isUseWebhooks()) {
                bot.dispatchToDiscord(null, config.getServerName(), formatted, config.getServerAvatarUrl());
            } else {
                bot.sendMessage(formatted);
            }
        }
    }

    public void sendServerStartMessage() {
        DiscordBot bot = AbyssLink.getInstance().discordBot;
        if (bot != null && bot.isConnected()) {
            String formatted = messagesConfig.getMessageFormat().getServerStartMessage();
            
            // Use webhook with server identity for server start messages
            if (config.isUseWebhooks()) {
                bot.dispatchToDiscord(null, config.getServerName(), formatted, config.getServerAvatarUrl());
            } else {
                bot.sendMessage(formatted);
            }
        }
    }

    public void sendServerStopMessage() {
        DiscordBot bot = AbyssLink.getInstance().discordBot;
        if (bot != null && bot.isConnected()) {
            String formatted = messagesConfig.getMessageFormat().getServerStopMessage();
            
            // Use webhook with server identity for server stop messages
            if (config.isUseWebhooks()) {
                bot.dispatchToDiscord(null, config.getServerName(), formatted, config.getServerAvatarUrl());
            } else {
                bot.sendMessage(formatted);
            }
        }
    }

    public void sendServerStopMessageBlocking() {
        DiscordBot bot = AbyssLink.getInstance().discordBot;
        if (bot != null && bot.isConnected()) {
            String formatted = messagesConfig.getMessageFormat().getServerStopMessage();

            if (config.isUseWebhooks()) {
                bot.sendWebhookMessageBlocking(config.getServerName(), formatted, config.getServerAvatarUrl());
            } else {
                bot.sendMessageBlocking(formatted);
            }
        }
    }
}
