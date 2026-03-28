package com.aethoria.core.event;

import java.util.UUID;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PlayerLevelUpEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID playerId;
    private final int previousLevel;
    private final int newLevel;
    private final int levelsGained;

    public PlayerLevelUpEvent(UUID playerId, int previousLevel, int newLevel, int levelsGained) {
        this.playerId = playerId;
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
        this.levelsGained = levelsGained;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public int getPreviousLevel() {
        return previousLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public int getLevelsGained() {
        return levelsGained;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
