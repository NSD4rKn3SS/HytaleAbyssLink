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
import java.text.SimpleDateFormat;
import java.util.Date;

public class ProfileCommand extends AbstractPlayerCommand {

    public ProfileCommand() {
        super("profile", "View your or another player's profile", false);
        this.setAllowsExtraArguments(true);
    }
    
    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void execute(
            @Nonnull CommandContext context,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef player,
            @Nonnull World world
    ) {
        String input = context.getInputString().trim();
        String[] parts = input.split("\\s+");
        
        AbyssLink plugin = AbyssLink.getInstance();
        LocaleManager locale = plugin.getLocaleManager();
        PlayerDataStorage storage = plugin.getPlayerDataStorage();
        PlayerData playerData;
        String targetUsername;
        
        if (parts.length > 1) {
            targetUsername = parts[1];
            playerData = null;
            
            for (PlayerData data : storage.getAllPlayers().values()) {
                if (data.getUsername().equalsIgnoreCase(targetUsername)) {
                    playerData = data;
                    break;
                }
            }
            
            if (playerData == null) {
                player.sendMessage(Message.raw(locale.getMessage("command.profile.not_found", "player", targetUsername)));
                return;
            }
        } else {
            playerData = storage.getPlayerData(player.getUuid());
            targetUsername = player.getUsername();
            
            if (playerData == null) {
                player.sendMessage(Message.raw(locale.getMessage("command.profile.error.playerdata")));
                return;
            }
        }
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
        String firstLoginDate = dateFormat.format(new Date(playerData.getFirstLoginTime()));
        String discordStatus = playerData.getDiscordId() != null 
            ? locale.getMessage("command.profile.discord.linked")
            : locale.getMessage("command.profile.discord.not_linked");
        
        player.sendMessage(Message.raw(locale.getMessage("command.profile.title", "player", targetUsername)));
        player.sendMessage(Message.raw(locale.getMessage("command.profile.playtime", "playtime", playerData.getFormattedPlayTime())));
        player.sendMessage(Message.raw(locale.getMessage("command.profile.first_login", "date", firstLoginDate)));
        player.sendMessage(Message.raw(discordStatus));
    }
}
