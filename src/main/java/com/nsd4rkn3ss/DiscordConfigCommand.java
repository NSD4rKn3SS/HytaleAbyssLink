package com.nsd4rkn3ss;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.io.File;

public class DiscordConfigCommand extends AbstractPlayerCommand {

    public DiscordConfigCommand() {
        super("discord", "Manage Discord integration settings", false);
        this.setAllowsExtraArguments(true);
    }
    
    @Override
    protected boolean canGeneratePermission() {
        return true;
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef player,
            @Nonnull World world
    ) {
        LocaleManager locale = AbyssLink.getInstance().getLocaleManager();
        
        // Get the player entity from the store to access hasPermission()
        com.hypixel.hytale.server.core.entity.entities.Player playerEntity = 
            (com.hypixel.hytale.server.core.entity.entities.Player) store.getComponent(ref, 
                com.hypixel.hytale.server.core.entity.entities.Player.getComponentType());
        
        if (playerEntity == null || !playerEntity.hasPermission("discordintegration.discord")) {
            player.sendMessage(Message.raw(locale.getMessage("command.discord.no_permission")));
            player.sendMessage(Message.raw(locale.getMessage("command.discord.required_permission")));
            return;
        }
        
        String input = context.getInputString().trim();
        String[] args = input.split("\\s+", 3);
        
        if (args.length <= 1) {
            showConfigHelp(player);
            return;
        }

        String action = args[1].toLowerCase();
        
        if ("get".equals(action) && args.length >= 3) {
            getConfigValue(player, args[2]);
        } else if ("set".equals(action) && args.length >= 3) {
            String[] setArgs = args[2].split("\\s+", 2);
            if (setArgs.length >= 2) {
                setConfigValue(player, setArgs[0], setArgs[1]);
            } else {
                player.sendMessage(Message.raw(locale.getMessage("command.discord.set.usage")));
            }
        } else if ("list".equals(action)) {
            listConfigValues(player);
        } else if ("reload".equals(action)) {
            reloadConfig(player);
        } else {
            showConfigHelp(player);
        }
    }

    private void showConfigHelp(PlayerRef player) {
        LocaleManager locale = AbyssLink.getInstance().getLocaleManager();
        player.sendMessage(Message.raw(locale.getMessage("command.discord.help.title")));
        player.sendMessage(Message.raw(locale.getMessage("command.discord.help.get")));
        player.sendMessage(Message.raw(locale.getMessage("command.discord.help.set")));
        player.sendMessage(Message.raw(locale.getMessage("command.discord.help.list")));
        player.sendMessage(Message.raw(locale.getMessage("command.discord.help.reload")));
        player.sendMessage(Message.raw(locale.getMessage("command.discord.help.fields")));
    }

    private void getConfigValue(PlayerRef player, String fieldName) {
        DiscordConfig config = AbyssLink.getInstance().config;
        LocaleManager locale = AbyssLink.getInstance().getLocaleManager();
        
        try {
            Object value = getFieldValue(config, fieldName);
            player.sendMessage(Message.raw(locale.getMessage("command.discord.get.value", "field", fieldName, "value", String.valueOf(value))));
        } catch (Exception e) {
            player.sendMessage(Message.raw(locale.getMessage("command.discord.get.error", "field", fieldName, "error", e.getMessage())));
        }
    }

    private void setConfigValue(PlayerRef player, String fieldName, String value) {
        DiscordConfig config = AbyssLink.getInstance().config;
        LocaleManager locale = AbyssLink.getInstance().getLocaleManager();
        
        try {
            setFieldValue(config, fieldName, value);
            saveConfig(config);
            player.sendMessage(Message.raw(locale.getMessage("command.discord.set.success", "field", fieldName, "value", value)));
            System.out.println("[AbyssLink Discord] Config updated in-game: " + fieldName + " = " + value);
        } catch (Exception e) {
            player.sendMessage(Message.raw(locale.getMessage("command.discord.set.error", "field", fieldName, "error", e.getMessage())));
        }
    }

    private void listConfigValues(PlayerRef player) {
        DiscordConfig config = AbyssLink.getInstance().config;
        LocaleManager locale = AbyssLink.getInstance().getLocaleManager();
        
        player.sendMessage(Message.raw(locale.getMessage("command.discord.list.title")));
        player.sendMessage(Message.raw("enabled: " + config.isEnabled()));
        player.sendMessage(Message.raw("allowOtherBotMessages: " + config.isAllowOtherBotMessages()));
        player.sendMessage(Message.raw("showChatTag: " + config.isShowChatTag()));
        player.sendMessage(Message.raw("enableInGameChat: " + config.isEnableInGameChat()));
        player.sendMessage(Message.raw("enableDeathMessages: " + config.isEnableDeathMessages()));
        player.sendMessage(Message.raw("enableJoinLeaveMessages: " + config.isEnableJoinLeaveMessages()));
        player.sendMessage(Message.raw("chatTagText: " + config.getChatTagText()));
        player.sendMessage(Message.raw("channelId: " + config.getChannelId()));
        player.sendMessage(Message.raw("commandChannelId: " + config.getCommandChannelId()));
        player.sendMessage(Message.raw("adminRoleId: " + config.getAdminRoleId()));
        player.sendMessage(Message.raw("showPlayerCountInTopic: " + config.isShowPlayerCountInTopic()));
        player.sendMessage(Message.raw("topicPlayerCountFormat: " + config.getTopicPlayerCountFormat()));
    }

    private void reloadConfig(PlayerRef player) {
        LocaleManager locale = AbyssLink.getInstance().getLocaleManager();
        try {
            AbyssLink.getInstance().loadConfig();
            AbyssLink.getInstance().loadMessagesConfig();
            player.sendMessage(Message.raw(locale.getMessage("command.discord.reload.success")));
        } catch (Exception e) {
            player.sendMessage(Message.raw(locale.getMessage("command.discord.reload.error", "error", e.getMessage())));
        }
    }

    private Object getFieldValue(DiscordConfig config, String fieldName) throws Exception {
        switch (fieldName.toLowerCase()) {
            case "enabled":
                return config.isEnabled();
            case "allowotherbotmessages":
                return config.isAllowOtherBotMessages();
            case "showchattag":
                return config.isShowChatTag();
            case "enableingamechat":
                return config.isEnableInGameChat();
            case "enabledeathmessages":
                return config.isEnableDeathMessages();
            case "enablejoinleavemessages":
                return config.isEnableJoinLeaveMessages();
            case "chattagtext":
                return config.getChatTagText();
            case "channelid":
                return config.getChannelId();
            case "commandchannelid":
                return config.getCommandChannelId();
            case "adminroleid":
                return config.getAdminRoleId();
            case "showPlayerCountInTopic":
                return config.isShowPlayerCountInTopic();
            case "topicPlayerCountFormat":
                return config.getTopicPlayerCountFormat();
            default:
                LocaleManager locale = AbyssLink.getInstance().getLocaleManager();
                throw new Exception(locale.getMessage("command.discord.field.unknown", "field", fieldName));
        }
    }

    private void setFieldValue(DiscordConfig config, String fieldName, String value) throws Exception {
        switch (fieldName.toLowerCase()) {
            case "enabled":
                config.setEnabled(Boolean.parseBoolean(value));
                break;
            case "allowotherbotmessages":
                config.setAllowOtherBotMessages(Boolean.parseBoolean(value));
                break;
            case "showchattag":
                config.setShowChatTag(Boolean.parseBoolean(value));
                break;
            case "enableingamechat":
                config.setEnableInGameChat(Boolean.parseBoolean(value));
                break;
            case "enabledeathmessages":
                config.setEnableDeathMessages(Boolean.parseBoolean(value));
                break;
            case "enablejoinleavemessages":
                config.setEnableJoinLeaveMessages(Boolean.parseBoolean(value));
                break;
            case "chattagtext":
                config.setChatTagText(value.replace("\"", ""));
                break;
            case "channelid":
                config.setChannelId(value.replace("\"", ""));
                break;
            case "commandchannelid":
                config.setCommandChannelId(value.replace("\"", ""));
                break;
            case "adminroleid":
                config.setAdminRoleId(value.replace("\"", ""));
                break;
            case "showPlayerCountInTopic":
                config.setShowPlayerCountInTopic(Boolean.parseBoolean(value));
                break;
            case "topicPlayerCountFormat":
                config.setTopicPlayerCountFormat(value.replace("\"", ""));
                break;
            default:
                LocaleManager locale = AbyssLink.getInstance().getLocaleManager();
                throw new Exception(locale.getMessage("command.discord.field.unknown", "field", fieldName));
        }
    }

    private void saveConfig(DiscordConfig config) {
        File configFile = new File("mods/AbyssLink/config.json");
        AbyssLink.getInstance().saveConfig(configFile);
    }
}
