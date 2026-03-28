package com.aethoria.core.api;

public record PlayerProgressionView(
    String activeClass,
    int adventurerLevel,
    int adventurerExperience,
    int xpToNextLevel,
    int maxLevel
) {
    public boolean isMaxLevel() {
        return adventurerLevel >= maxLevel;
    }
}
