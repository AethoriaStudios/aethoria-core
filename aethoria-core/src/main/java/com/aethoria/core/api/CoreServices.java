package com.aethoria.core.api;

public record CoreServices(
    CoreAuthoredItemLookupService authoredItems,
    CorePlayerProgressionLookupService progression,
    CoreCurrencyService currency
) {
}
