package com.aethoria.core.service;

import com.aethoria.core.AethoriaCorePlugin;
import com.aethoria.core.model.PlayerProfile;
import java.util.UUID;

public final class CurrencyService {
    private final AethoriaCorePlugin plugin;
    private final PlayerProfileService profileService;

    public CurrencyService(AethoriaCorePlugin plugin, PlayerProfileService profileService) {
        this.plugin = plugin;
        this.profileService = profileService;
    }

    public String getPrimaryCurrencyName() {
        return plugin.getConfig().getString("currencies.primary-name", "Aethor");
    }

    public double getAethor(UUID playerId) {
        return profileService.getOrLoad(playerId).getAethor();
    }

    public void setAethor(UUID playerId, double amount) {
        PlayerProfile profile = profileService.getOrLoad(playerId);
        profile.setAethor(amount);
        profileService.save(playerId);
    }

    public boolean withdrawAethor(UUID playerId, double amount) {
        PlayerProfile profile = profileService.getOrLoad(playerId);
        if (amount < 0.0D || profile.getAethor() < amount) {
            return false;
        }

        profile.setAethor(profile.getAethor() - amount);
        profileService.save(playerId);
        return true;
    }

    public void depositAethor(UUID playerId, double amount) {
        if (amount <= 0.0D) {
            return;
        }

        PlayerProfile profile = profileService.getOrLoad(playerId);
        profile.setAethor(profile.getAethor() + amount);
        profileService.save(playerId);
    }

    public int getDungeonCoins(UUID playerId) {
        return profileService.getOrLoad(playerId).getDungeonCoins();
    }

    public void setDungeonCoins(UUID playerId, int amount) {
        PlayerProfile profile = profileService.getOrLoad(playerId);
        profile.setDungeonCoins(amount);
        profileService.save(playerId);
    }

    public void depositDungeonCoins(UUID playerId, int amount) {
        if (amount <= 0) {
            return;
        }

        PlayerProfile profile = profileService.getOrLoad(playerId);
        profile.setDungeonCoins(profile.getDungeonCoins() + amount);
        profileService.save(playerId);
    }

    public boolean withdrawDungeonCoins(UUID playerId, int amount) {
        PlayerProfile profile = profileService.getOrLoad(playerId);
        if (amount < 0 || profile.getDungeonCoins() < amount) {
            return false;
        }

        profile.setDungeonCoins(profile.getDungeonCoins() - amount);
        profileService.save(playerId);
        return true;
    }
}