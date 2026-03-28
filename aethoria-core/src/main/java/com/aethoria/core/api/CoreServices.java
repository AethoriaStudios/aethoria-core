package com.aethoria.core.api;

public record CoreServices(
    CorePlayerIdentityService players,
    CoreAuthoredItemLookupService authoredItems,
    CorePlayerProgressionLookupService progression,
    CoreCurrencyService currency
) {
}
