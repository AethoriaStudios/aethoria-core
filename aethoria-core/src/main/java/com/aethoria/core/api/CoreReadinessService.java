package com.aethoria.core.api;

import com.aethoria.core.AethoriaCorePlugin;

public final class CoreReadinessService {
    private final AethoriaCorePlugin plugin;

    public CoreReadinessService(AethoriaCorePlugin plugin) {
        this.plugin = plugin;
    }

    public CoreReadinessView getReadiness() {
        boolean itemsReady = plugin.getItemRegistryService() != null
            && plugin.getItemFactory() != null
            && !plugin.getItemRegistryService().getDefinitions().isEmpty();
        boolean progressionReady = plugin.getProgressionService() != null && plugin.getClassSwapService() != null;
        boolean currencyReady = plugin.getCurrencyService() != null;
        boolean playersReady = plugin.getProfileService() != null && plugin.getPlayerIdentityService() != null;
        return new CoreReadinessView(
            itemsReady,
            progressionReady,
            currencyReady,
            playersReady,
            itemsReady && progressionReady && currencyReady && playersReady
        );
    }
}
