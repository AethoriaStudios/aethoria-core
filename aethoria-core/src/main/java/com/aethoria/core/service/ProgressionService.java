package com.aethoria.core.service;

import com.aethoria.core.AethoriaCorePlugin;
import com.aethoria.core.event.PlayerLevelUpEvent;
import com.aethoria.core.model.PlayerProfile;
import java.util.UUID;

public final class ProgressionService {
    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 30;
    private static final int BASE_XP_REQUIREMENT = 100;
    private static final int XP_GROWTH_PER_LEVEL = 25;

    private final AethoriaCorePlugin plugin;
    private final PlayerProfileService profileService;

    public ProgressionService(AethoriaCorePlugin plugin, PlayerProfileService profileService) {
        this.plugin = plugin;
        this.profileService = profileService;
    }

    public int getLevel(UUID playerId) {
        return profileService.getOrLoad(playerId).getAdventurerLevel();
    }

    public int getExperience(UUID playerId) {
        return profileService.getOrLoad(playerId).getAdventurerExperience();
    }

    public int getMaxLevel() {
        return MAX_LEVEL;
    }

    public int getXpToNextLevel(UUID playerId) {
        return getXpToNextLevel(getLevel(playerId));
    }

    public int getXpToNextLevel(int currentLevel) {
        if (currentLevel >= MAX_LEVEL) {
            return 0;
        }
        return BASE_XP_REQUIREMENT + Math.max(0, currentLevel - MIN_LEVEL) * XP_GROWTH_PER_LEVEL;
    }

    public void setLevel(UUID playerId, int level) {
        PlayerProfile profile = profileService.getOrLoad(playerId);
        profile.setAdventurerLevel(Math.min(MAX_LEVEL, Math.max(MIN_LEVEL, level)));
        if (profile.getAdventurerLevel() >= MAX_LEVEL) {
            profile.setAdventurerExperience(0);
        } else {
            profile.setAdventurerExperience(Math.min(profile.getAdventurerExperience(), getXpToNextLevel(profile.getAdventurerLevel()) - 1));
        }
        profileService.save(playerId);
    }

    public void addLevels(UUID playerId, int levels) {
        setLevel(playerId, getLevel(playerId) + levels);
    }

    public void setExperience(UUID playerId, int experience) {
        PlayerProfile profile = profileService.getOrLoad(playerId);
        if (profile.getAdventurerLevel() >= MAX_LEVEL) {
            profile.setAdventurerExperience(0);
        } else {
            int cappedExperience = Math.max(0, experience);
            int xpToNextLevel = getXpToNextLevel(profile.getAdventurerLevel());
            profile.setAdventurerExperience(Math.min(cappedExperience, xpToNextLevel - 1));
        }
        profileService.save(playerId);
    }

    public ProgressionResult addExperience(UUID playerId, int experience) {
        PlayerProfile profile = profileService.getOrLoad(playerId);
        int amount = Math.max(0, experience);
        int levelsGained = 0;

        if (amount == 0 || profile.getAdventurerLevel() >= MAX_LEVEL) {
            if (profile.getAdventurerLevel() >= MAX_LEVEL) {
                profile.setAdventurerExperience(0);
                profileService.save(playerId);
            }
            return new ProgressionResult(profile.getAdventurerLevel(), profile.getAdventurerExperience(), 0);
        }

        int totalExperience = profile.getAdventurerExperience() + amount;
        int currentLevel = profile.getAdventurerLevel();

        while (currentLevel < MAX_LEVEL) {
            int requiredExperience = getXpToNextLevel(currentLevel);
            if (totalExperience < requiredExperience) {
                break;
            }

            totalExperience -= requiredExperience;
            currentLevel++;
            levelsGained++;
        }

        profile.setAdventurerLevel(currentLevel);
        profile.setAdventurerExperience(currentLevel >= MAX_LEVEL ? 0 : totalExperience);
        profileService.save(playerId);
        if (levelsGained > 0) {
            plugin.getServer().getPluginManager().callEvent(new PlayerLevelUpEvent(playerId, currentLevel - levelsGained, currentLevel, levelsGained));
        }
        return new ProgressionResult(profile.getAdventurerLevel(), profile.getAdventurerExperience(), levelsGained);
    }

    public record ProgressionResult(int level, int experience, int levelsGained) {
    }
}
