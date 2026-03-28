package com.aethoria.core.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class AuthoredItemsReloadEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final int loadedDefinitions;
    private final int invalidDefinitions;
    private final int warningCount;
    private final boolean createdDefaultFile;

    public AuthoredItemsReloadEvent(int loadedDefinitions, int invalidDefinitions, int warningCount, boolean createdDefaultFile) {
        this.loadedDefinitions = loadedDefinitions;
        this.invalidDefinitions = invalidDefinitions;
        this.warningCount = warningCount;
        this.createdDefaultFile = createdDefaultFile;
    }

    public int getLoadedDefinitions() {
        return loadedDefinitions;
    }

    public int getInvalidDefinitions() {
        return invalidDefinitions;
    }

    public int getWarningCount() {
        return warningCount;
    }

    public boolean isCreatedDefaultFile() {
        return createdDefaultFile;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
