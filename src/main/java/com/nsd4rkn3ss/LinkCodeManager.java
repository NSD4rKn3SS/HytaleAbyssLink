package com.nsd4rkn3ss;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LinkCodeManager {
    private final Map<String, LinkRequest> pendingLinks;
    private final Random random;
    private static final long CODE_EXPIRY_MS = 5 * 60 * 1000;

    public LinkCodeManager() {
        this.pendingLinks = new ConcurrentHashMap<>();
        this.random = new Random();
    }

    public String generateCode(String discordId, String discordUsername) {
        String code = String.format("%04d", random.nextInt(10000));
        
        pendingLinks.put(code, new LinkRequest(discordId, discordUsername, System.currentTimeMillis()));
        
        cleanExpiredCodes();
        
        return code;
    }

    public LinkRequest verifyCode(String code, UUID playerUuid, String playerUsername) {
        LinkRequest request = pendingLinks.get(code);
        
        if (request == null) {
            return null;
        }
        
        if (System.currentTimeMillis() - request.timestamp > CODE_EXPIRY_MS) {
            pendingLinks.remove(code);
            return null;
        }
        
        pendingLinks.remove(code);
        return request;
    }

    private void cleanExpiredCodes() {
        long now = System.currentTimeMillis();
        pendingLinks.entrySet().removeIf(entry -> 
            now - entry.getValue().timestamp > CODE_EXPIRY_MS
        );
    }

    public static class LinkRequest {
        public final String discordId;
        public final String discordUsername;
        public final long timestamp;

        public LinkRequest(String discordId, String discordUsername, long timestamp) {
            this.discordId = discordId;
            this.discordUsername = discordUsername;
            this.timestamp = timestamp;
        }
    }
}
