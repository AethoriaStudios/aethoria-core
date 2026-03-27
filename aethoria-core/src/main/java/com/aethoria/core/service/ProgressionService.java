package com.aethoria.core.service;

import com.aethoria.core.model.PlayerProfile;
import java.util.UUID;

public final class ProgressionService {
    private final PlayerProfileService profileService;

    public ProgressionService(PlayerProfileService profileService) {
        this.profileService = profileService;
    }

    public int getLevel(UUID playerId) {
        return profileService.getOrLoad(playerId).getAdventurerLevel();
    }

    public int getExperience(UUID playerId) {
        return profileService.getOrLoad(playerId).getAdventurerExperience();
    }

    public void setLevel(UUID playerId, int level) {
        PlayerProfile profile = profileService.getOrLoad(playerId);
        profile.setAdventurerLevel(level);
        profileService.save(playerId);
    }

    public void addLevels(UUID playerId, int levels) {
        PlayerProfile profile = profileService.getOrLoad(playerId);
        profile.setAdventurerLevel(profile.getAdventurerLevel() + levels);
        profileService.save(playerId);
    }

    public void setExperience(UUID playerId, int experience) {
        PlayerProfile profile = profileService.getOrLoad(playerId);
        profile.setAdventurerExperience(experience);
        profileService.save(playerId);
    }
}
