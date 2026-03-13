package com.nsd4rkn3ss;

public class DiscordConfig {
    // Discord integration main switch
    private boolean enabled = false;

    // General Discord Settings
    private String botToken = "Bot Token Here";
    private String channelId = "111111111111111";
    private String commandChannelId = "222222222222222";
    private String adminRoleId = "333333333333333";

    // Webhook settings
    private boolean useWebhooks = false;
    private String webhookUrl = "";
    private String serverName = "Server";
    private String serverAvatarUrl = "";
    private String defaultPlayerAvatarUrl = "";
    private int avatarCacheMinutes = 30;

    // Functionality toggles
    private boolean allowOtherBotMessages = false;
    private boolean enableDeathMessages = true;
    private boolean enableJoinLeaveMessages = true;
    private boolean showChatTag = true;
    private String chatTagText = "Linked";
    private boolean enableInGameChat = true;
    private boolean showPlayerCountInTopic = false;
    private String topicPlayerCountFormat = "Players online: {online}";
    
    // Localization
    private String locale = "en_US";

    private ChatTagColors chatTagColors = new ChatTagColors();

    public static class ChatTagColors {
        private String bracketColor = "#808080";
        private String tagColor = "#5865F2";
        private String usernameColor = "#00FFFF";
        private String messageColor = "#FFFFFF";

        public String getBracketColor() { return bracketColor; }
        public String getTagColor() { return tagColor; }
        public String getUsernameColor() { return usernameColor; }
        public String getMessageColor() { return messageColor; }
    }

    public String getBotToken() { return botToken; }
    public void setBotToken(String botToken) { this.botToken = botToken; }
    
    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }
    
    public String getCommandChannelId() { return commandChannelId; }
    public void setCommandChannelId(String commandChannelId) { this.commandChannelId = commandChannelId; }
    
    public String getAdminRoleId() { return adminRoleId; }
    public void setAdminRoleId(String adminRoleId) { this.adminRoleId = adminRoleId; }

    public boolean isAllowOtherBotMessages() { return allowOtherBotMessages; }
    public void setAllowOtherBotMessages(boolean allowOtherBotMessages) { this.allowOtherBotMessages = allowOtherBotMessages; }
    
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isEnableDeathMessages() { return enableDeathMessages; }
    public void setEnableDeathMessages(boolean enableDeathMessages) { this.enableDeathMessages = enableDeathMessages; }
    
    public boolean isEnableJoinLeaveMessages() { return enableJoinLeaveMessages; }
    public void setEnableJoinLeaveMessages(boolean enableJoinLeaveMessages) { this.enableJoinLeaveMessages = enableJoinLeaveMessages; }
    
    public String getChatTagText() { return chatTagText; }
    public void setChatTagText(String chatTagText) { this.chatTagText = chatTagText; }
    
    public boolean isShowChatTag() { return showChatTag; }
    public void setShowChatTag(boolean showChatTag) { this.showChatTag = showChatTag; }
    
    public boolean isEnableInGameChat() { return enableInGameChat; }
    public void setEnableInGameChat(boolean enableInGameChat) { this.enableInGameChat = enableInGameChat; }

    public boolean isShowPlayerCountInTopic() { return showPlayerCountInTopic; }
    public void setShowPlayerCountInTopic(boolean showPlayerCountInTopic) {
        this.showPlayerCountInTopic = showPlayerCountInTopic;
    }

    public String getTopicPlayerCountFormat() { return topicPlayerCountFormat; }
    public void setTopicPlayerCountFormat(String topicPlayerCountFormat) {
        this.topicPlayerCountFormat = topicPlayerCountFormat;
    }

    public ChatTagColors getChatTagColors() { return chatTagColors; }
    
    // Webhook getters and setters
    public boolean isUseWebhooks() { return useWebhooks; }
    public void setUseWebhooks(boolean useWebhooks) { this.useWebhooks = useWebhooks; }
    
    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
    
    public String getServerName() { return serverName; }
    public void setServerName(String serverName) { this.serverName = serverName; }
    
    public String getServerAvatarUrl() { return serverAvatarUrl; }
    public void setServerAvatarUrl(String serverAvatarUrl) { this.serverAvatarUrl = serverAvatarUrl; }
    
    public String getDefaultPlayerAvatarUrl() { return defaultPlayerAvatarUrl; }
    public void setDefaultPlayerAvatarUrl(String defaultPlayerAvatarUrl) { this.defaultPlayerAvatarUrl = defaultPlayerAvatarUrl; }
    
    public int getAvatarCacheMinutes() { return avatarCacheMinutes; }
    public void setAvatarCacheMinutes(int avatarCacheMinutes) { this.avatarCacheMinutes = avatarCacheMinutes; }
    
    public String getLocale() { return locale; }
    public void setLocale(String locale) { this.locale = locale; }
}
