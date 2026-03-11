package com.nsd4rkn3ss;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;

public class PlayerDeathSystem extends DeathSystems.OnDeathSystem {
    
    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Query.and(Player.getComponentType());
    }
    
    @Override
    public void onComponentAdded(
            @Nonnull Ref ref,
            @Nonnull DeathComponent component,
            @Nonnull Store store,
            @Nonnull CommandBuffer commandBuffer) {
        
        Player playerComponent = (Player) store.getComponent(ref, Player.getComponentType());
        
        if (playerComponent != null) {
            String playerName = playerComponent.getDisplayName();
            System.out.println("[AbyssLink Discord] Player died: " + playerName);
            
            // Extract the death message from the component (what the client displays)
            String cause = "";
            String deathType = "";
            try {
                Message deathMessage = component.getDeathMessage();
                if (deathMessage != null) {
                    String fullMessage = deathMessage.getAnsiMessage();
                    System.out.println("[AbyssLink Discord] Full death message: " + fullMessage);
                    
                    // Extract the cause from the message by removing "You were " prefix
                    // Format is typically: "You were killed by <cause>" or "You died from <cause>" etc.
                    if (fullMessage.contains("You were killed by")) {
                        cause = fullMessage.replace("You were killed by ", "").trim();
                        deathType = "killed";
                    } else if (fullMessage.contains("You were")) {
                        cause = fullMessage.replace("You were ", "").trim();
                        deathType = "killed";
                    } else if (fullMessage.contains("You died")) {
                        cause = fullMessage.replace("You died", "").trim();
                        deathType = "died";
                    } else {
                        cause = fullMessage.trim();
                        deathType = "other";
                    }
                    
                    // Remove color codes if present
                    cause = cause.replaceAll("\\u00a7[0-9a-fA-Fk-oK-O]", "").trim();
                    
                    System.out.println("[AbyssLink Discord] Extracted cause: " + cause + " | death type: " + deathType)");
                }
            } catch (Exception e) {
                System.out.println("[AbyssLink Discord] Error extracting death message: " + e.getMessage());
            }
            
            // Notify Discord via the plugin instance
            AbyssLink plugin = AbyssLink.getInstance();
            if (plugin != null) {
                plugin.notifyPlayerDeath(playerName, cause, deathType);
            }
        }
    }
}
