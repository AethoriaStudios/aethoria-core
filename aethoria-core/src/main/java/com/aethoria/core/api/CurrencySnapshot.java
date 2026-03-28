package com.aethoria.core.api;

public record CurrencySnapshot(
    String primaryCurrencyName,
    double primaryBalance,
    int dungeonCoins
) {
}
