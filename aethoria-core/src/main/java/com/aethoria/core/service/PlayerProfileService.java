package com.aethoria.core.service;

import com.aethoria.core.AethoriaCorePlugin;
import com.aethoria.core.model.PlayerProfile;
import com.aethoria.core.storage.PlayerDataStore;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class PlayerProfileService {
    private final AethoriaCorePlugin plugin;
    private final PlayerDataStore dataStore;
    private final Map<UUID, PlayerProfile> cachedProfiles = new ConcurrentHashMap<>();

    public PlayerProfileService(AethoriaCorePlugin plugin, PlayerDataStore dataStore) {
        this.plugin = plugin;
        this.dataStore = dataStore;
    }

    public PlayerProfile preload(UUID playerId) {
        return getOrLoad(playerId);
    }

    public PlayerProfile getOrLoad(UUID playerId) {
        return cachedProfiles.computeIfAbsent(playerId, this::loadProfile);
    }

    public void save(UUID playerId) {
        PlayerProfile profile = cachedProfiles.get(playerId);
        if (profile != null) {
            saveProfile(profile);
        }
    }

    public void saveAndEvict(UUID playerId) {
        PlayerProfile profile = cachedProfiles.remove(playerId);
        if (profile != null) {
            saveProfile(profile);
        }
    }

    public void saveAll() {
        Collection<PlayerProfile> profiles = cachedProfiles.values();
        try {
            dataStore.saveAll(profiles);
        } catch (Exception exception) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save player profiles.", exception);
        }
    }

    public String getDefaultClass() {
        return plugin.getConfig().getString("classes.default-class", "WARRIOR").trim().toUpperCase();
    }

    private PlayerProfile loadProfile(UUID playerId) {
        try {
            return dataStore.load(playerId, getDefaultClass());
        } catch (Exception exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to load player profile for " + playerId + ". Using defaults.", exception);
            return PlayerProfile.createDefault(playerId, getDefaultClass());
        }
    }

    private void saveProfile(PlayerProfile profile) {
        try {
            dataStore.save(profile);
        } catch (Exception exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to save player profile for " + profile.getPlayerId() + '.', exception);
        }
    }
}