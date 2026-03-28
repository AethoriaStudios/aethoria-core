package com.aethoria.core.api;

import com.aethoria.core.service.CurrencyService;
import java.util.UUID;

public final class CoreCurrencyService {
    private final CurrencyService currencyService;

    public CoreCurrencyService(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    public CurrencySnapshot getSnapshot(UUID playerId) {
        return new CurrencySnapshot(
            currencyService.getPrimaryCurrencyName(),
            currencyService.getAethor(playerId),
            currencyService.getDungeonCoins(playerId)
        );
    }

    public double getPrimaryBalance(UUID playerId) {
        return currencyService.getAethor(playerId);
    }

    public int getDungeonCoins(UUID playerId) {
        return currencyService.getDungeonCoins(playerId);
    }

    public String getPrimaryCurrencyName() {
        return currencyService.getPrimaryCurrencyName();
    }

    public CurrencyMutationResult depositPrimary(UUID playerId, double amount) {
        if (amount <= 0.0D) {
            return CurrencyMutationResult.failure("Deposit amount must be positive.", getSnapshot(playerId));
        }
        currencyService.depositAethor(playerId, amount);
        return CurrencyMutationResult.success(getSnapshot(playerId));
    }

    public CurrencyMutationResult withdrawPrimary(UUID playerId, double amount) {
        if (amount < 0.0D) {
            return CurrencyMutationResult.failure("Withdrawal amount must not be negative.", getSnapshot(playerId));
        }
        if (!currencyService.withdrawAethor(playerId, amount)) {
            return CurrencyMutationResult.failure("Player does not have enough " + getPrimaryCurrencyName() + '.', getSnapshot(playerId));
        }
        return CurrencyMutationResult.success(getSnapshot(playerId));
    }

    public CurrencyMutationResult setPrimary(UUID playerId, double amount) {
        if (amount < 0.0D) {
            return CurrencyMutationResult.failure("Primary currency amount must not be negative.", getSnapshot(playerId));
        }
        currencyService.setAethor(playerId, amount);
        return CurrencyMutationResult.success(getSnapshot(playerId));
    }

    public CurrencyMutationResult depositDungeonCoins(UUID playerId, int amount) {
        if (amount <= 0) {
            return CurrencyMutationResult.failure("Dungeon Coin deposit must be positive.", getSnapshot(playerId));
        }
        currencyService.depositDungeonCoins(playerId, amount);
        return CurrencyMutationResult.success(getSnapshot(playerId));
    }

    public CurrencyMutationResult withdrawDungeonCoins(UUID playerId, int amount) {
        if (amount < 0) {
            return CurrencyMutationResult.failure("Dungeon Coin withdrawal must not be negative.", getSnapshot(playerId));
        }
        if (!currencyService.withdrawDungeonCoins(playerId, amount)) {
            return CurrencyMutationResult.failure("Player does not have enough Dungeon Coins.", getSnapshot(playerId));
        }
        return CurrencyMutationResult.success(getSnapshot(playerId));
    }

    public CurrencyMutationResult setDungeonCoins(UUID playerId, int amount) {
        if (amount < 0) {
            return CurrencyMutationResult.failure("Dungeon Coin amount must not be negative.", getSnapshot(playerId));
        }
        currencyService.setDungeonCoins(playerId, amount);
        return CurrencyMutationResult.success(getSnapshot(playerId));
    }
}
