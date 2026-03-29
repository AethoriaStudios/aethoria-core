package com.aethoria.core.api;

public record CoreReadinessView(
    boolean itemsReady,
    boolean progressionReady,
    boolean currencyReady,
    boolean playersReady,
    boolean ready
) {
}
