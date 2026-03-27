package com.aethoria.core.model;

import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

public final class PlayerProfile {
    private static final int MIN_LEVEL = 1;

    private final UUID playerId;
    private double aethor;
    private int dungeonCoins;
    private int adventurerLevel;
    private int adventurerExperience;
    private String activeClass;
    private LocalDate lastDungeonDailyBonus;

    public PlayerProfile(UUID playerId, double aethor, int dungeonCoins, int adventurerLevel, int adventurerExperience, String activeClass, LocalDate lastDungeonDailyBonus) {
        this.playerId = playerId;
        this.aethor = Math.max(0.0D, aethor);
        this.dungeonCoins = Math.max(0, dungeonCoins);
        this.adventurerLevel = Math.max(MIN_LEVEL, adventurerLevel);
        this.adventurerExperience = Math.max(0, adventurerExperience);
        this.activeClass = normalizeClass(activeClass);
        this.lastDungeonDailyBonus = lastDungeonDailyBonus;
    }

    public static PlayerProfile createDefault(UUID playerId, String defaultClass) {
        return new PlayerProfile(playerId, 0.0D, 0, MIN_LEVEL, 0, defaultClass, null);
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public double getAethor() {
        return aethor;
    }

    public void setAethor(double aethor) {
        this.aethor = Math.max(0.0D, aethor);
    }

    public int getDungeonCoins() {
        return dungeonCoins;
    }

    public void setDungeonCoins(int dungeonCoins) {
        this.dungeonCoins = Math.max(0, dungeonCoins);
    }

    public int getAdventurerLevel() {
        return adventurerLevel;
    }

    public void setAdventurerLevel(int adventurerLevel) {
        this.adventurerLevel = Math.max(MIN_LEVEL, adventurerLevel);
    }

    public int getAdventurerExperience() {
        return adventurerExperience;
    }

    public void setAdventurerExperience(int adventurerExperience) {
        this.adventurerExperience = Math.max(0, adventurerExperience);
    }

    public String getActiveClass() {
        return activeClass;
    }

    public void setActiveClass(String activeClass) {
        this.activeClass = normalizeClass(activeClass);
    }

    public LocalDate getLastDungeonDailyBonus() {
        return lastDungeonDailyBonus;
    }

    public void setLastDungeonDailyBonus(LocalDate lastDungeonDailyBonus) {
        this.lastDungeonDailyBonus = lastDungeonDailyBonus;
    }

    public boolean canClaimDungeonDailyBonus(LocalDate today) {
        return lastDungeonDailyBonus == null || !lastDungeonDailyBonus.equals(today);
    }

    private static String normalizeClass(String classId) {
        if (classId == null || classId.isBlank()) {
            return "WARRIOR";
        }
        return classId.trim().toUpperCase(Locale.ROOT);
    }
}
