package com.nsd4rkn3ss;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
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
        
        if (playerComponent == null) {
            return;
        }
        
        String playerName = playerComponent.getDisplayName();
        System.out.println("[AbyssLink Discord] Player died: " + playerName);
        
        // Use the structured Damage API for death info
        Damage deathInfo = component.getDeathInfo();
        DamageCause damageCause = component.getDeathCause();
        
        String causeId = (damageCause != null) ? damageCause.getId() : "unknown";
        float damageAmount = (deathInfo != null) ? deathInfo.getAmount() : 0f;
        
        // Get the canonical death message from the Damage source
        String deathMessageText = "";
        try {
            if (deathInfo != null) {
                Message deathMsg = deathInfo.getDeathMessage(ref, store);
                if (deathMsg != null) {
                    deathMessageText = deathMsg.getRawText();
                }
            }
            // Fall back to the DeathComponent's own message if Damage didn't produce one
            if (deathMessageText.isEmpty()) {
                Message fallbackMsg = component.getDeathMessage();
                if (fallbackMsg != null) {
                    deathMessageText = fallbackMsg.getRawText();
                }
            }
        } catch (Exception e) {
            System.out.println("[AbyssLink Discord] Error retrieving death message: " + e.getMessage());
        }
        
        // Determine source type for richer context
        String sourceType = "environment";
        if (deathInfo != null) {
            Damage.Source source = deathInfo.getSource();
            if (source instanceof Damage.EntitySource) {
                sourceType = "entity";
            } else if (source instanceof Damage.ProjectileSource) {
                sourceType = "projectile";
            } else if (source instanceof Damage.CommandSource) {
                sourceType = "command";
            } else if (source instanceof Damage.EnvironmentSource) {
                sourceType = ((Damage.EnvironmentSource) source).getType();
            }
        }
        
        System.out.println("[AbyssLink Discord] Death cause: " + causeId 
                + " | source: " + sourceType 
                + " | damage: " + damageAmount
                + " | message: " + deathMessageText);
        
        AbyssLink plugin = AbyssLink.getInstance();
        if (plugin != null) {
            plugin.notifyPlayerDeath(playerName, deathMessageText, causeId, sourceType, damageAmount);
        }
    }
}
