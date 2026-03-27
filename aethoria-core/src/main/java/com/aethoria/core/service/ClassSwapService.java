package com.aethoria.core.service;

import com.aethoria.core.AethoriaCorePlugin;
import com.aethoria.core.model.PlayerProfile;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ClassSwapService {
    private final AethoriaCorePlugin plugin;
    private final PlayerProfileService profileService;
    private final CurrencyService currencyService;

    public ClassSwapService(AethoriaCorePlugin plugin, PlayerProfileService profileService, CurrencyService currencyService) {
        this.plugin = plugin;
        this.profileService = profileService;
        this.currencyService = currencyService;
    }

    public String getActiveClass(UUID playerId) {
        return profileService.getOrLoad(playerId).getActiveClass();
    }

    public double getConfiguredSwapCost() {
        return plugin.getConfig().getDouble("currencies.class-swap-cost", 250.0D);
    }

    public List<String> getSupportedClasses() {
        List<String> configuredClasses = plugin.getConfig().getStringList("classes.available");
        if (configuredClasses.isEmpty()) {
            return List.of(profileService.getDefaultClass());
        }

        return configuredClasses.stream()
            .map(this::normalizeClassId)
            .filter(classId -> !classId.isBlank())
            .distinct()
            .collect(Collectors.toList());
    }

    public boolean isSupportedClass(String classId) {
        String normalizedClass = normalizeClassId(classId);
        return !normalizedClass.isBlank() && getSupportedClasses().contains(normalizedClass);
    }

    public String getOmniTestClass() {
        return normalizeClassId(plugin.getConfig().getString("classes.omni-test-class", "TESTER"));
    }

    public boolean isOmniTestClass(String classId) {
        String normalizedClass = normalizeClassId(classId);
        return !normalizedClass.isBlank() && normalizedClass.equals(getOmniTestClass());
    }

    public boolean setActiveClass(UUID playerId, String classId) {
        String normalizedClass = normalizeClassId(classId);
        if (!isSupportedClass(normalizedClass)) {
            return false;
        }

        PlayerProfile profile = profileService.getOrLoad(playerId);
        profile.setActiveClass(normalizedClass);
        profileService.save(playerId);
        return true;
    }

    public boolean swapClass(UUID playerId, String classId, double cost) {
        String normalizedClass = normalizeClassId(classId);
        if (!isSupportedClass(normalizedClass)) {
            return false;
        }

        if (!currencyService.withdrawAethor(playerId, cost)) {
            return false;
        }

        PlayerProfile profile = profileService.getOrLoad(playerId);
        profile.setActiveClass(normalizedClass);
        profileService.save(playerId);
        return true;
    }

    public String normalizeClassId(String classId) {
        if (classId == null || classId.isBlank()) {
            return "";
        }
        return classId.trim().toUpperCase(Locale.ROOT);
    }
}
