package com.nsd4rkn3ss;

import com.google.gson.JsonObject;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class DiscordBot extends ListenerAdapter {
    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();
    
    private final DiscordConfig config;
    private final BiConsumer<String, String> onDiscordMessage;
    private JDA jda;
    private TextChannel textChannel;

    private String lastChannelTopic;
    private long lastChannelTopicUpdateMillis;
    private static final long CHANNEL_TOPIC_MIN_UPDATE_INTERVAL_MILLIS = 30_000;

    public DiscordBot(DiscordConfig config, BiConsumer<String, String> onDiscordMessage) {
        this.config = config;
        this.onDiscordMessage = onDiscordMessage;
    }

    public CompletableFuture<Boolean> start() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        if (!config.isEnabled()) {
            System.out.println("[Discord] Bot is disabled in config");
            future.complete(false);
            return future;
        }

        if ("YOUR_BOT_TOKEN_HERE".equals(config.getBotToken())) {
            System.out.println("[Discord] Please set your bot token in the config!");
            future.complete(false);
            return future;
        }

        try {
            System.out.println("[Discord] Starting Discord bot...");

            jda = JDABuilder.createDefault(config.getBotToken())
                .enableIntents(
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.MESSAGE_CONTENT
                )
                .addEventListeners(this)
                .build();

            jda.awaitReady();

            textChannel = jda.getTextChannelById(config.getChannelId());
            if (textChannel == null) {
                System.out.println("[Discord] Could not find channel with ID: " + config.getChannelId());
                future.complete(false);
                return future;
            }

            System.out.println("[Discord] Bot connected successfully to channel: " + textChannel.getName());
            updatePlayerCount(0, 0);
            future.complete(true);

        } catch (Exception e) {
            System.out.println("[Discord] Failed to start bot: " + e.getMessage());
            e.printStackTrace();
            future.complete(false);
        }

        return future;
    }

    public void shutdown() {
        System.out.println("[Discord] Shutting down Discord bot...");
        if (jda != null) {
            jda.shutdown();
            jda = null;
        }
        textChannel = null;
    }

    public void sendMessage(String message) {
        if (textChannel != null) {
            textChannel.sendMessage(message).queue(
                success -> System.out.println("[Discord] Message sent: " + message),
                error -> System.out.println("[Discord] Failed to send message: " + error.getMessage())
            );
        }
    }

    public void sendMessageBlocking(String message) {
        if (textChannel != null) {
            try {
                textChannel.sendMessage(message).complete();
                System.out.println("[Discord] Message sent (sync): " + message);
            } catch (Exception e) {
                System.out.println("[Discord] Failed to send sync message: " + e.getMessage());
            }
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String channelId = event.getChannel().getId();

        // Always ignore webhook messages to avoid loops/impersonation.
        if (event.isWebhookMessage()) return;

        if (event.getAuthor().isBot()) {
            // Always ignore messages sent by THIS integration's bot account.
            try {
                if (event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) return;
            } catch (Exception ignored) {
                // If self user isn't available for some reason, fall back to config gate below.
            }

            // Only allow other bot messages when explicitly enabled, and only in the relay channel.
            if (!config.isAllowOtherBotMessages() || !channelId.equals(config.getChannelId())) return;
        }

        String username = event.getAuthor().getName();
        String message = event.getMessage().getContentDisplay();

        // Allow !link command in both relay and command channels
        if (channelId.equals(config.getChannelId()) || channelId.equals(config.getCommandChannelId())) {
            if (message.equalsIgnoreCase("!link")) {
                handleLinkCommand(event);
                return;
            }
        }

        if (channelId.equals(config.getCommandChannelId())) {
            if (message.toLowerCase().startsWith("!profile")) {
                handleProfileCommand(event, message);
                return;
            }
            
            if (message.toLowerCase().startsWith("!players")) {
                handlePlayersCommand(event, message);
                return;
            }
        }

        if (channelId.equals(config.getChannelId())) {
            onDiscordMessage.accept(username, message);
        }
    }

    private void handleLinkCommand(MessageReceivedEvent event) {
        String discordId = event.getAuthor().getId();
        String discordUsername = event.getAuthor().getName();
        LocaleManager locale = AbyssLink.getInstance().getLocaleManager();

        LinkCodeManager linkManager = AbyssLink.getInstance().getLinkCodeManager();
        String code = linkManager.generateCode(discordId, discordUsername);

        MessageEmbed embed = new EmbedBuilder()
            .setTitle(locale.getMessage("discord.link.embed.title"))
            .setColor(0x5865F2)
            .addField(locale.getMessage("discord.link.embed.code_field"), "`" + code + "`", false)
            .addField(locale.getMessage("discord.link.embed.howto_field"), locale.getMessage("discord.link.embed.howto_value", "code", code), false)
            .addField(locale.getMessage("discord.link.embed.important_field"), locale.getMessage("discord.link.embed.important_value"), false)
            .setFooter(locale.getMessage("discord.link.embed.footer"), null)
            .build();

        event.getAuthor().openPrivateChannel().queue(privateChannel -> {
            privateChannel.sendMessageEmbeds(embed).queue(
                success -> {
                    MessageEmbed successEmbed = new EmbedBuilder()
                        .setTitle(locale.getMessage("discord.link.sent.title"))
                        .setColor(0x00FF00)
                        .setDescription(locale.getMessage("discord.link.sent.description"))
                        .setFooter(locale.getMessage("discord.link.embed.footer"), null)
                        .build();
                    event.getChannel().sendMessageEmbeds(successEmbed).queue();
                    System.out.println("[AbyssLink Discord] Generated link code for " + discordUsername + ": " + code);
                },
                error -> {
                    MessageEmbed errorEmbed = new EmbedBuilder()
                        .setTitle(locale.getMessage("discord.link.dm_failed.title"))
                        .setColor(0xFF0000)
                        .setDescription(locale.getMessage("discord.link.dm_failed.description"))
                        .setFooter(locale.getMessage("discord.link.embed.footer"), null)
                        .build();
                    event.getChannel().sendMessageEmbeds(errorEmbed).queue();
                    System.out.println("[AbyssLink Discord] Failed to DM link code to " + discordUsername);
                }
            );
        });
    }
    
    private void handleProfileCommand(MessageReceivedEvent event, String message) {
        String[] parts = message.split("\\s+");
        String discordId = event.getAuthor().getId();
        LocaleManager locale = AbyssLink.getInstance().getLocaleManager();
        
        PlayerDataStorage storage = AbyssLink.getInstance().getPlayerDataStorage();
        PlayerData playerData = null;
        String targetUsername = null;
        
        if (parts.length > 1) {
            targetUsername = parts[1];
            for (PlayerData data : storage.getAllPlayers().values()) {
                if (data.getUsername().equalsIgnoreCase(targetUsername)) {
                    playerData = data;
                    break;
                }
            }
            
            if (playerData == null) {
                MessageEmbed embed = new EmbedBuilder()
                    .setTitle(locale.getMessage("discord.profile.not_found.title"))
                    .setColor(0xFF0000)
                    .setDescription(locale.getMessage("discord.profile.not_found.description", "username", targetUsername))
                    .setFooter(locale.getMessage("discord.profile.embed.footer"), null)
                    .build();
                event.getChannel().sendMessageEmbeds(embed).queue();
                return;
            }
        } else {
            playerData = storage.getPlayerByDiscordId(discordId);
            
            if (playerData == null) {
                MessageEmbed embed = new EmbedBuilder()
                    .setTitle(locale.getMessage("discord.profile.not_linked.title"))
                    .setColor(0xFFAA00)
                    .setDescription(locale.getMessage("discord.profile.not_linked.description"))
                    .setFooter(locale.getMessage("discord.profile.embed.footer"), null)
                    .build();
                event.getChannel().sendMessageEmbeds(embed).queue();
                return;
            }
            targetUsername = playerData.getUsername();
        }
        
        String discordTag = playerData.getDiscordId() != null ? "<@" + playerData.getDiscordId() + ">" : locale.getMessage("command.profile.discord.not_linked");
        long firstLogin = playerData.getFirstLoginTime();
        String firstLoginDate = new java.text.SimpleDateFormat("MMM dd, yyyy").format(new java.util.Date(firstLogin));
        
        MessageEmbed embed = new EmbedBuilder()
            .setTitle(locale.getMessage("discord.profile.embed.title", "username", targetUsername))
            .setColor(0x00FF00)
            .addField(locale.getMessage("discord.profile.embed.playtime"), playerData.getFormattedPlayTime(), true)
            .addField(locale.getMessage("discord.profile.embed.first_login"), firstLoginDate, true)
            .addField(locale.getMessage("discord.profile.embed.discord_status"), discordTag, false)
            .setFooter(locale.getMessage("discord.profile.embed.footer"), null)
            .build();
        
        event.getChannel().sendMessageEmbeds(embed).queue();
        System.out.println("[AbyssLink Discord] Profile requested for: " + targetUsername);
    }
    
    private void handlePlayersCommand(MessageReceivedEvent event, String message) {
        if (!hasAdminRole(event)) {
            MessageEmbed errorEmbed = new EmbedBuilder()
                .setTitle("Access Denied")
                .setColor(0xFF0000)
                .setDescription("You need admin permissions to use this command.")
                .setFooter("Discord Integration", null)
                .build();
            event.getChannel().sendMessageEmbeds(errorEmbed).queue();
            return;
        }
        
        com.hypixel.hytale.server.core.universe.Universe universe = 
            com.hypixel.hytale.server.core.universe.Universe.get();
        java.util.Collection<com.hypixel.hytale.server.core.universe.PlayerRef> onlinePlayers = universe.getPlayers();
        int playerCount = onlinePlayers.size();
        
        if (playerCount == 0) {
            MessageEmbed embed = new EmbedBuilder()
                .setTitle("Server Status")
                .setColor(0x00FFFF)
                .setDescription("No players are currently online.")
                .setFooter("Discord Integration", null)
                .build();
            event.getChannel().sendMessageEmbeds(embed).queue();
            return;
        }
        
        java.util.List<String> playerNames = new java.util.ArrayList<>();
        for (com.hypixel.hytale.server.core.universe.PlayerRef player : onlinePlayers) {
            playerNames.add(player.getUsername());
        }
        
        String[] parts = message.split("\\s+");
        int page = 1;
        
        if (parts.length > 1) {
            try {
                page = Integer.parseInt(parts[1]);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                page = 1;
            }
        }
        
        int playersPerPage = 15;
        int totalPages = (int) Math.ceil((double) playerCount / playersPerPage);
        
        if (page > totalPages) {
            page = totalPages;
        }
        
        sendPlayersPage(event, playerNames, page, totalPages, playersPerPage);
        System.out.println("[AbyssLink Discord] Players list page " + page + " requested by admin (" + playerCount + " online)");
    }
    
    private boolean hasAdminRole(MessageReceivedEvent event) {
        String adminRoleId = config.getAdminRoleId();
        if (adminRoleId == null || adminRoleId.isEmpty()) {
            return true;
        }
        
        return event.getMember() != null && 
               event.getMember().getRoles().stream()
                   .anyMatch(role -> role.getId().equals(adminRoleId));
    }
    
    private void sendPlayersPage(MessageReceivedEvent event, java.util.List<String> playerNames, int page, int totalPages, int playersPerPage) {
        LocaleManager locale = AbyssLink.getInstance().getLocaleManager();
        int startIndex = (page - 1) * playersPerPage;
        int endIndex = Math.min(startIndex + playersPerPage, playerNames.size());
        
        StringBuilder playerList = new StringBuilder();
        for (int i = startIndex; i < endIndex; i++) {
            playerList.append("• ").append(playerNames.get(i)).append("\n");
        }
        
        if (playerList.length() == 0) {
            playerList.append(locale.getMessage("discord.players.empty"));
        }
        
        MessageEmbed embed = new EmbedBuilder()
            .setTitle(locale.getMessage("discord.players.title", "count", String.valueOf(playerNames.size()), "max", String.valueOf(playerNames.size())))
            .setColor(0x00FFFF)
            .setDescription(playerList.toString())
            .setFooter("Page " + page + "/" + totalPages + " | Use !players <page> to navigate", null)
            .build();
        
        event.getChannel().sendMessageEmbeds(embed).queue();
    }

    public boolean isConnected() {
        return jda != null && textChannel != null;
    }

    public void updatePlayerCount(int online, int max) {
        if (jda != null) {
            String status = max > 0 ? online + "/" + max + " players online" : online + " players online";
            jda.getPresence().setActivity(Activity.playing(status));
        }

        if (textChannel == null) return;
        if (!config.isShowPlayerCountInTopic()) return;

        String format = config.getTopicPlayerCountFormat();
        if (format == null || format.isBlank()) {
            format = "Players online: {online}";
        }

        String topic = format
            .replace("{online}", String.valueOf(online))
            .replace("{max}", String.valueOf(max));

        long now = System.currentTimeMillis();
        if (lastChannelTopic != null && lastChannelTopic.equals(topic)) return;
        if (now - lastChannelTopicUpdateMillis < CHANNEL_TOPIC_MIN_UPDATE_INTERVAL_MILLIS) return;

        lastChannelTopic = topic;
        lastChannelTopicUpdateMillis = now;

        textChannel.getManager().setTopic(topic).queue(
            success -> System.out.println("[Discord] Updated channel description (topic) to: " + topic),
            error -> System.out.println("[Discord] Failed to update channel topic: " + error.getMessage())
        );
    }

    public void sendWebhookMessageBlocking(String username, String content, String avatarUrl) {
        if (!config.isUseWebhooks() || config.getWebhookUrl() == null || config.getWebhookUrl().isEmpty()) {
            sendMessageBlocking("**" + username + "**: " + content);
            return;
        }

        JsonObject json = new JsonObject();
        json.addProperty("username", username);
        json.addProperty("content", content);
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            json.addProperty("avatar_url", avatarUrl);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getWebhookUrl()))
                .header("Content-Type", "application/json")
                .header("User-Agent", "HytaleDiscordIntegration/1.0")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                System.err.println("[Discord] Webhook Error (sync): " + response.statusCode() + " - " + response.body());
            }
        } catch (Exception ex) {
            System.err.println("[Discord] Webhook Network Error (sync): " + ex.getMessage());
        }
    }

    /**
     * Sends a message to Discord using a webhook with custom username and avatar.
     * @param username The username to display in Discord
     * @param content The message content
     * @param avatarUrl The avatar URL (can be null)
     */
    public void sendWebhookMessage(String username, String content, String avatarUrl) {
        if (!config.isUseWebhooks() || config.getWebhookUrl() == null || config.getWebhookUrl().isEmpty()) {
            // Fallback to regular bot message
            sendMessage("**" + username + "**: " + content);
            return;
        }

        JsonObject json = new JsonObject();
        json.addProperty("username", username);
        json.addProperty("content", content);
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            json.addProperty("avatar_url", avatarUrl);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getWebhookUrl()))
                .header("Content-Type", "application/json")
                .header("User-Agent", "HytaleDiscordIntegration/1.0")
                .POST(HttpRequest.BodyPublishers.ofString(json.toString()))
                .build();

        HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() >= 400) {
                        System.err.println("[Discord] Webhook Error: " + response.statusCode() + " - " + response.body());
                    }
                })
                .exceptionally(ex -> {
                    System.err.println("[Discord] Webhook Network Error: " + ex.getMessage());
                    return null;
                });
    }

    /**
     * Dispatches a message to Discord using webhooks if enabled, with avatar support.
     * Tries the Hytale Tracker in-game avatar first, then falls back to the linked
     * Discord profile picture, then to the configured default avatar.
     * @param playerUuid The player's UUID (can be null for server messages)
     * @param name The display name to use
     * @param content The message content
     * @param fallbackAvatar The avatar URL to use if no better avatar is found (can be null)
     */
    public void dispatchToDiscord(UUID playerUuid, String name, String content, String fallbackAvatar) {
        // If webhooks are disabled, use standard bot message
        if (!config.isUseWebhooks()) {
            if (!name.equals(config.getServerName())) {
                sendMessage("**" + name + "**: " + content);
            } else {
                sendMessage(content);
            }
            return;
        }

        // Initialize avatar cache with configured expiration time
        AvatarCache.setExpireTime(config.getAvatarCacheMinutes());

        try {
            // If we have a player UUID, try Hytale Tracker avatar first
            if (playerUuid != null) {
                String uuidStr = playerUuid.toString();
                String hytaleKey = "hytale:" + uuidStr;
                String cachedHytaleAvatar = AvatarCache.get(hytaleKey);

                if (cachedHytaleAvatar != null) {
                    if (!cachedHytaleAvatar.isEmpty()) {
                        // Hytale Tracker avatar is cached and available
                        sendWebhookMessage(name, content, cachedHytaleAvatar);
                        return;
                    }
                    // Empty string means we already checked and it's unavailable — fall through
                    sendWithDiscordAvatar(playerUuid, name, content, fallbackAvatar);
                    return;
                }

                // Check Hytale Tracker asynchronously via HEAD request
                String hytaleAvatarUrl = "https://hytaletracker.org/avatars/" + uuidStr + ".png";
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(hytaleAvatarUrl))
                        .method("HEAD", HttpRequest.BodyPublishers.noBody())
                        .timeout(java.time.Duration.ofSeconds(5))
                        .build();

                HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                        .thenAccept(response -> {
                            if (response.statusCode() == 200) {
                                AvatarCache.put(hytaleKey, hytaleAvatarUrl);
                                sendWebhookMessage(name, content, hytaleAvatarUrl);
                            } else {
                                AvatarCache.put(hytaleKey, ""); // Mark as unavailable
                                sendWithDiscordAvatar(playerUuid, name, content, fallbackAvatar);
                            }
                        })
                        .exceptionally(throwable -> {
                            System.out.println("[Discord] Failed to check Hytale Tracker avatar for " + uuidStr + ": " + throwable.getMessage());
                            AvatarCache.put(hytaleKey, ""); // Mark as unavailable
                            sendWithDiscordAvatar(playerUuid, name, content, fallbackAvatar);
                            return null;
                        });
                return;
            }

            // No playerUuid (server messages etc.) — send with fallback avatar
            sendWebhookMessage(name, content, fallbackAvatar);
        } catch (Exception e) {
            // Emergency fallback: if anything goes wrong, send the message anyway
            System.err.println("[Discord] Dispatch error: " + e.getMessage());
            e.printStackTrace();
            sendWebhookMessage(name, content, fallbackAvatar);
        }
    }

    /**
     * Sends a webhook message using the player's linked Discord avatar, or the
     * configured fallback if the player is not linked or the lookup fails.
     */
    private void sendWithDiscordAvatar(UUID playerUuid, String name, String content, String fallbackAvatar) {
        final String discordId;
        if (playerUuid != null) {
            PlayerDataStorage storage = AbyssLink.getInstance().getPlayerDataStorage();
            PlayerData playerData = storage.getPlayerData(playerUuid);
            discordId = (playerData != null) ? playerData.getDiscordId() : null;
        } else {
            discordId = null;
        }

        if (discordId == null || jda == null) {
            sendWebhookMessage(name, content, fallbackAvatar);
            return;
        }

        // Try to get Discord avatar from cache
        String cachedAvatar = AvatarCache.get(discordId);
        if (cachedAvatar != null) {
            sendWebhookMessage(name, content, cachedAvatar);
            return;
        }

        // Fetch avatar from Discord API asynchronously
        jda.retrieveUserById(discordId).queue(
                user -> {
                    String avatar = user.getEffectiveAvatarUrl();
                    AvatarCache.put(discordId, avatar);
                    sendWebhookMessage(name, content, avatar);
                },
                throwable -> {
                    System.out.println("[Discord] Failed to fetch avatar for user " + discordId + ": " + throwable.getMessage());
                    sendWebhookMessage(name, content, fallbackAvatar);
                }
        );
    }
}
