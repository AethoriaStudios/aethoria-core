package com.aethoria.core.service;

import com.aethoria.core.AethoriaCorePlugin;
import com.aethoria.core.model.PlayerProfile;
import java.time.LocalDate;
import java.util.UUID;

public final class DungeonService {
    private final AethoriaCorePlugin plugin;
    private final PlayerProfileService profileService;
    private final CurrencyService currencyService;

    public DungeonService(AethoriaCorePlugin plugin, PlayerProfileService profileService, CurrencyService currencyService) {
        this.plugin = plugin;
        this.profileService = profileService;
        this.currencyService = currencyService;
    }

    public int getConfiguredDailyBonus() {
        return plugin.getConfig().getInt("currencies.dungeon-daily-bonus", 15);
    }

    public boolean canClaimDailyBonus(UUID playerId) {
        PlayerProfile profile = profileService.getOrLoad(playerId);
        return profile.canClaimDungeonDailyBonus(LocalDate.now());
    }

    public boolean claimDailyBonus(UUID playerId, int dungeonCoins) {
        PlayerProfile profile = profileService.getOrLoad(playerId);
        LocalDate today = LocalDate.now();
        if (!profile.canClaimDungeonDailyBonus(today)) {
            return false;
        }

        currencyService.depositDungeonCoins(playerId, dungeonCoins);
        profile.setLastDungeonDailyBonus(today);
        profileService.save(playerId);
        return true;
    }

    public void resetDailyBonus(UUID playerId) {
        PlayerProfile profile = profileService.getOrLoad(playerId);
        profile.setLastDungeonDailyBonus(null);
        profileService.save(playerId);
    }
}