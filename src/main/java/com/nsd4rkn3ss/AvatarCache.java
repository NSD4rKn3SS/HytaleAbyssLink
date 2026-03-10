package com.nsd4rkn3ss;

import java.util.HashMap;
import java.util.Map;

/**
 * Caches Discord avatar URLs to reduce API calls and stay within rate limits.
 * Each avatar URL is cached with a timestamp and expires after a configurable duration.
 */
public class AvatarCache {
    private static Map<String, Entry> cache = new HashMap<>();
    private static long expireTimeMs = 1800000; // Default: 30 minutes

    private record Entry(String url, long timestamp) {}

    /**
     * Sets the cache expiration time.
     * @param minutes The time in minutes before a cached avatar expires
     */
    public static synchronized void setExpireTime(int minutes) {
        expireTimeMs = minutes * 60000L;
    }

    /**
     * Retrieves a cached avatar URL if it exists and hasn't expired.
     * @param discordId The Discord user ID
     * @return The cached avatar URL, or null if not found or expired
     */
    public static synchronized String get(String discordId) {
        Entry entry = cache.get(discordId);
        if (entry != null) {
            if (System.currentTimeMillis() - entry.timestamp < expireTimeMs) {
                return entry.url;
            } else {
                cache.remove(discordId); // Remove expired entry
            }
        }
        return null;
    }

    /**
     * Stores an avatar URL in the cache with the current timestamp.
     * @param discordId The Discord user ID
     * @param avatarUrl The avatar URL to cache
     */
    public static synchronized void put(String discordId, String avatarUrl) {
        cache.put(discordId, new Entry(avatarUrl, System.currentTimeMillis()));
    }

    /**
     * Clears all cached avatar URLs.
     */
    public static synchronized void clear() {
        cache.clear();
    }

    /**
     * Removes expired entries from the cache.
     */
    public static synchronized void cleanExpired() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> 
            now - entry.getValue().timestamp >= expireTimeMs
        );
    }
}
