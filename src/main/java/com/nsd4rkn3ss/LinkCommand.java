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

public class LinkCommand extends AbstractPlayerCommand {

    public LinkCommand() {
        super("link", "Link your Discord account to your in-game account", false);
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
        
        if (parts.length < 2) {
            player.sendMessage(Message.raw(locale.getMessage("command.link.usage")));
            player.sendMessage(Message.raw(locale.getMessage("command.link.getcode")));
            return;
        }

        String code = parts[1];
        LinkCodeManager.LinkRequest request = plugin.getLinkCodeManager().verifyCode(code, player.getUuid(), player.getUsername());

        if (request == null) {
            player.sendMessage(Message.raw(locale.getMessage("command.link.invalid")));
            player.sendMessage(Message.raw(locale.getMessage("command.link.expired")));
            return;
        }

        PlayerData playerData = plugin.getPlayerDataStorage().getPlayerData(player.getUuid());
        if (playerData == null) {
            player.sendMessage(Message.raw(locale.getMessage("command.link.error.playerdata")));
            return;
        }

        if (playerData.getDiscordId() != null) {
            player.sendMessage(Message.raw(locale.getMessage("command.link.already_linked")));
            player.sendMessage(Message.raw(locale.getMessage("command.link.linked_to", "discordUsername", request.discordUsername)));
            return;
        }

        playerData.setDiscordId(request.discordId);
        plugin.getPlayerDataStorage().updatePlayerData(player.getUuid(), playerData);
        plugin.getPlayerDataStorage().saveAllPlayers();

        player.sendMessage(Message.raw(locale.getMessage("command.link.success")));
        player.sendMessage(Message.raw(locale.getMessage("command.link.discord_user", "discordUsername", request.discordUsername)));

        plugin.notifyDiscordLink(request.discordId, player.getUsername(), true);

        System.out.println("[AbyssLink Discord] " + player.getUsername() + " linked to Discord: " + request.discordUsername);
    }
}
