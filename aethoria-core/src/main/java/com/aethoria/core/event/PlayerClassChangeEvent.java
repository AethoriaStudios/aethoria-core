package com.aethoria.core.event;

import java.util.UUID;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class PlayerClassChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID playerId;
    private final String previousClass;
    private final String newClass;
    private final boolean swapOperation;

    public PlayerClassChangeEvent(UUID playerId, String previousClass, String newClass, boolean swapOperation) {
        this.playerId = playerId;
        this.previousClass = previousClass;
        this.newClass = newClass;
        this.swapOperation = swapOperation;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getPreviousClass() {
        return previousClass;
    }

    public String getNewClass() {
        return newClass;
    }

    public boolean isSwapOperation() {
        return swapOperation;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
