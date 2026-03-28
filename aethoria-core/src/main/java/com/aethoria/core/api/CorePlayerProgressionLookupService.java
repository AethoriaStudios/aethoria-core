package com.aethoria.core.api;

import com.aethoria.core.service.ClassSwapService;
import com.aethoria.core.service.ProgressionService;
import java.util.UUID;

public final class CorePlayerProgressionLookupService {
    private final ProgressionService progressionService;
    private final ClassSwapService classSwapService;

    public CorePlayerProgressionLookupService(ProgressionService progressionService, ClassSwapService classSwapService) {
        this.progressionService = progressionService;
        this.classSwapService = classSwapService;
    }

    public PlayerProgressionView getProgression(UUID playerId) {
        int level = progressionService.getLevel(playerId);
        int xp = progressionService.getExperience(playerId);
        return new PlayerProgressionView(
            classSwapService.getActiveClass(playerId),
            level,
            xp,
            progressionService.getXpToNextLevel(playerId),
            progressionService.getMaxLevel()
        );
    }

    public int getAdventurerLevel(UUID playerId) {
        return progressionService.getLevel(playerId);
    }

    public int getAdventurerExperience(UUID playerId) {
        return progressionService.getExperience(playerId);
    }

    public int getXpToNextLevel(UUID playerId) {
        return progressionService.getXpToNextLevel(playerId);
    }

    public int getMaxLevel() {
        return progressionService.getMaxLevel();
    }

    public String getActiveClass(UUID playerId) {
        return classSwapService.getActiveClass(playerId);
    }
}
