package com.aethoria.core.api;

public record CurrencyMutationResult(
    boolean success,
    String errorMessage,
    CurrencySnapshot snapshot
) {
    public static CurrencyMutationResult success(CurrencySnapshot snapshot) {
        return new CurrencyMutationResult(true, null, snapshot);
    }

    public static CurrencyMutationResult failure(String errorMessage, CurrencySnapshot snapshot) {
        return new CurrencyMutationResult(false, errorMessage, snapshot);
    }
}
