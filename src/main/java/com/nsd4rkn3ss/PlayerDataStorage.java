package com.nsd4rkn3ss;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataStorage {
    private final File dataFolder;
    private final File playerDataFile;
    private final Gson gson;
    private final Map<UUID, PlayerData> playerDataCache;

    public PlayerDataStorage(File dataFolder) {
        this.dataFolder = dataFolder;
        this.playerDataFile = new File(dataFolder, "players.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.playerDataCache = new ConcurrentHashMap<>();
        
        loadAllPlayers();
    }

    private void loadAllPlayers() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        if (!playerDataFile.exists()) {
            System.out.println("[AbyssLink Discord] No player data file found, creating new one");
            try {
                playerDataFile.createNewFile();
                saveAllPlayers();
                System.out.println("[AbyssLink Discord] Created player data file at: " + playerDataFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("[AbyssLink Discord] Error creating player data file: " + e.getMessage());
                e.printStackTrace();
            }
            return;
        }

        try (FileReader reader = new FileReader(playerDataFile)) {
            Type type = new TypeToken<Map<String, PlayerData>>(){}.getType();
            Map<String, PlayerData> loadedData = gson.fromJson(reader, type);
            
            if (loadedData != null) {
                for (Map.Entry<String, PlayerData> entry : loadedData.entrySet()) {
                    UUID uuid = UUID.fromString(entry.getKey());
                    playerDataCache.put(uuid, entry.getValue());
                }
                System.out.println("[AbyssLink Discord] Loaded " + playerDataCache.size() + " player records");
            }
        } catch (IOException e) {
            System.err.println("[AbyssLink Discord] Error loading player data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void saveAllPlayers() {
        try {
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            Map<String, PlayerData> saveData = new HashMap<>();
            for (Map.Entry<UUID, PlayerData> entry : playerDataCache.entrySet()) {
                saveData.put(entry.getKey().toString(), entry.getValue());
            }

            try (FileWriter writer = new FileWriter(playerDataFile)) {
                gson.toJson(saveData, writer);
            }
            
            System.out.println("[AbyssLink Discord] Saved " + playerDataCache.size() + " player records");
        } catch (IOException e) {
            System.err.println("[AbyssLink Discord] Error saving player data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataCache.get(uuid);
    }

    public PlayerData getOrCreatePlayerData(UUID uuid, String username) {
        return playerDataCache.computeIfAbsent(uuid, k -> {
            System.out.println("[AbyssLink Discord] Creating new player data for " + username);
            return new PlayerData(uuid, username);
        });
    }

    public void updatePlayerData(UUID uuid, PlayerData data) {
        playerDataCache.put(uuid, data);
    }

    public PlayerData getPlayerByDiscordId(String discordId) {
        for (PlayerData data : playerDataCache.values()) {
            if (discordId.equals(data.getDiscordId())) {
                return data;
            }
        }
        return null;
    }

    public void linkDiscordAccount(UUID uuid, String discordId) {
        PlayerData data = playerDataCache.get(uuid);
        if (data != null) {
            data.setDiscordId(discordId);
            System.out.println("[AbyssLink Discord] Linked " + data.getUsername() + " to Discord ID: " + discordId);
        }
    }

    public Map<UUID, PlayerData> getAllPlayers() {
        return new HashMap<>(playerDataCache);
    }
}
