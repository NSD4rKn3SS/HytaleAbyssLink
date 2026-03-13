package com.nsd4rkn3ss;

import java.util.LinkedHashMap;
import java.util.Map;

public class MessagesConfig {

    private MessageFormat messageFormat = new MessageFormat();
    private DeathMessages deathMessages = new DeathMessages();

    public static class MessageFormat {
        private String serverToDiscord = "**{player}**: {message}";
        private String discordToServer = "[Discord] <{user}> {message}";
        private String joinMessage = "**{player}** joined the server";
        private String leaveMessage = "**{player}** left the server";
        private String deathMessage = "☠️ **{player}** {message}"; // {player}, {message}, {cause}, {source}, {damage}
        private String serverStartMessage = "🟢 **Server is now online!**";
        private String serverStopMessage = "🔴 **Server is shutting down...**";

        public String getServerToDiscord() { return serverToDiscord; }
        public String getDiscordToServer() { return discordToServer; }
        public String getJoinMessage() { return joinMessage; }
        public String getLeaveMessage() { return leaveMessage; }
        public String getDeathMessage() { return deathMessage; }
        public String getServerStartMessage() { return serverStartMessage; }
        public String getServerStopMessage() { return serverStopMessage; }
    }

    public static class DeathMessages {
        private boolean useCustomDeathMessages = true;

        // Flavor text keyed by cause ID (case-insensitive lookup at runtime).
        // {player} and {source} placeholders available.
        private Map<String, String> byCause = createDefaultCauseMessages();

        // Flavor text keyed by source type when no cause-specific match exists.
        private Map<String, String> bySourceType = createDefaultSourceTypeMessages();

        // Fallback when nothing else matches
        private String fallback = "died";

        private static Map<String, String> createDefaultCauseMessages() {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("drowning", "drowned");
            m.put("fall", "fell to their death");
            m.put("suffocation", "suffocated");
            m.put("out_of_world", "fell out of the world");
            m.put("void", "fell out of the world");
            m.put("fire", "burned to death");
            m.put("lava", "tried to swim in lava");
            m.put("starvation", "starved to death");
            m.put("poison", "was poisoned");
            m.put("magic", "was killed by magic");
            m.put("command", "was killed");
            return m;
        }

        private static Map<String, String> createDefaultSourceTypeMessages() {
            Map<String, String> m = new LinkedHashMap<>();
            m.put("entity", "was slain by {source}");
            m.put("projectile", "was shot by {source}");
            m.put("environment", "was killed by the environment");
            return m;
        }

        public boolean isUseCustomDeathMessages() { return useCustomDeathMessages; }

        public String resolve(String causeId, String sourceName, String sourceType) {
            String cause = (causeId != null) ? causeId.toLowerCase() : "";
            boolean hasSource = sourceName != null && !sourceName.isEmpty();

            // 1. Check cause-specific messages
            if (byCause.containsKey(cause)) {
                String msg = byCause.get(cause);
                if (hasSource) {
                    return msg.replace("{source}", sourceName);
                }
                return msg.replace("{source}", "").trim();
            }

            // 2. If we have a named source, check source-type messages
            if (hasSource && sourceType != null && bySourceType.containsKey(sourceType)) {
                return bySourceType.get(sourceType).replace("{source}", sourceName);
            }

            // 3. Source-type fallback (no named source)
            if (sourceType != null && bySourceType.containsKey(sourceType)) {
                String msg = bySourceType.get(sourceType);
                if (hasSource) {
                    return msg.replace("{source}", sourceName);
                }
                // Don't show "was slain by " with no name — use fallback
                if (msg.contains("{source}")) {
                    return fallback;
                }
                return msg;
            }

            return fallback;
        }

        public String getFallback() { return fallback; }
    }

    public MessageFormat getMessageFormat() { return messageFormat; }
    public DeathMessages getDeathMessages() { return deathMessages; }
}
