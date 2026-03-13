package com.nsd4rkn3ss;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.DisplayNameComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
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
        
        // Extract source info: name and type
        String sourceName = "";
        String sourceType = "unknown";
        
        if (deathInfo != null) {
            Damage.Source source = deathInfo.getSource();
            if (source != null) {
                // Check ProjectileSource BEFORE EntitySource (ProjectileSource extends EntitySource)
                if (source instanceof Damage.ProjectileSource) {
                    sourceType = "projectile";
                    sourceName = resolveEntityName(store, ((Damage.ProjectileSource) source).getRef());
                } else if (source instanceof Damage.EntitySource) {
                    sourceType = "entity";
                    sourceName = resolveEntityName(store, ((Damage.EntitySource) source).getRef());
                } else if (source instanceof Damage.EnvironmentSource) {
                    sourceType = "environment";
                    sourceName = formatSourceId(((Damage.EnvironmentSource) source).getType());
                } else if (source instanceof Damage.CommandSource) {
                    sourceType = "command";
                }
            }
        }
        
        // Try to get the composed death message from the DeathComponent (e.g. "You were killed by a Grizzly Bear!")
        String sourceDeathMessage = "";
        try {
            Message deathMsg = component.getDeathMessage();
            if (deathMsg != null) {
                sourceDeathMessage = extractText(deathMsg);
            }
            // Fallback: try the source's death message
            if ((sourceDeathMessage == null || sourceDeathMessage.isEmpty()) 
                    && deathInfo != null && deathInfo.getSource() != null) {
                Message sourceMsg = deathInfo.getSource().getDeathMessage(deathInfo, ref, store);
                if (sourceMsg != null) {
                    sourceDeathMessage = extractText(sourceMsg);
                }
            }
        } catch (Exception e) {
            System.out.println("[AbyssLink Discord] Error getting death message: " + e.getMessage());
        }
        
        // Build the final death message:
        // 1. Custom flavor text from messages.json (if enabled)
        // 2. Game's composed death message (converted to third person)
        // 3. Plain "died" fallback
        String deathMessageText = "";
        AbyssLink plugin = AbyssLink.getInstance();
        if (plugin != null && plugin.messagesConfig != null 
                && plugin.messagesConfig.getDeathMessages().isUseCustomDeathMessages()) {
            deathMessageText = plugin.messagesConfig.getDeathMessages().resolve(causeId, sourceName, sourceType);
        }
        if (deathMessageText == null || deathMessageText.isEmpty()) {
            if (sourceDeathMessage != null && !sourceDeathMessage.isEmpty()) {
                deathMessageText = toThirdPerson(sourceDeathMessage);
            } else {
                deathMessageText = "died";
            }
        }
        
        System.out.println("[AbyssLink Discord] Death cause: " + causeId 
                + " | source type: " + sourceType
                + " | source name: " + sourceName
                + " | damage: " + damageAmount
                + " | message: " + deathMessageText);
        
        if (plugin != null) {
            plugin.notifyPlayerDeath(playerName, deathMessageText, causeId, sourceName, damageAmount);
        }
    }
    
    /**
     * Resolve an entity's display name from a ref.
     * Tries PlayerRef first (for PvP kills), then DisplayNameComponent (for mobs/NPCs).
     */
    private static String resolveEntityName(ComponentAccessor<EntityStore> store, Ref ref) {
        if (ref == null) return "";
        
        // Try PlayerRef for player killers (PvP)
        try {
            PlayerRef playerRef = (PlayerRef) store.getComponent(ref, PlayerRef.getComponentType());
            if (playerRef != null) {
                return playerRef.getUsername();
            }
        } catch (Exception ignored) {}
        
        // Try DisplayNameComponent for mobs/NPCs
        try {
            DisplayNameComponent displayName = (DisplayNameComponent) store.getComponent(ref, DisplayNameComponent.getComponentType());
            if (displayName != null && displayName.getDisplayName() != null) {
                String name = extractText(displayName.getDisplayName());
                if (name != null && !name.isEmpty()) {
                    return name;
                }
            }
        } catch (Exception ignored) {}
        
        return "";
    }
    
    /**
     * Convert a second-person death message to third-person for Discord.
     * e.g. "You were killed by a Grizzly Bear!" -> "was killed by a Grizzly Bear"
     */
    private static String toThirdPerson(String msg) {
        // Strip trailing punctuation (! or .)
        msg = msg.replaceAll("[!.]+$", "");
        
        // "You were ..." -> "was ..."
        if (msg.startsWith("You were ")) {
            return "was " + msg.substring(9);
        }
        // "You have ..." -> "has ..."
        if (msg.startsWith("You have ")) {
            return "has " + msg.substring(9);
        }
        // Generic "You ..." -> strip "You " and keep the rest
        if (msg.startsWith("You ")) {
            return msg.substring(4);
        }
        return msg;
    }

    /**
     * Extract text from a Message object, handling both raw text and translated messages.
     */
    private static String extractText(Message msg) {
        if (msg == null) return "";
        // getRawText() works for raw messages; getAnsiMessage() resolves translations and children
        String text = msg.getRawText();
        if (text != null && !text.isEmpty()) return text;
        text = msg.getAnsiMessage();
        if (text != null && !text.isEmpty()) return text;
        return "";
    }
    
    /**
     * Format an environment source type ID into a readable name.
     * e.g. "spike_trap" -> "Spike Trap", "lava" -> "Lava"
     */
    private static String formatSourceId(String id) {
        if (id == null || id.isEmpty()) return "";
        // Strip namespace prefix (e.g. "hytale:spike_trap" -> "spike_trap")
        int colonIndex = id.indexOf(':');
        if (colonIndex >= 0) {
            id = id.substring(colonIndex + 1);
        }
        String[] parts = id.split("[_\\-]");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                if (sb.length() > 0) sb.append(' ');
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }
}
