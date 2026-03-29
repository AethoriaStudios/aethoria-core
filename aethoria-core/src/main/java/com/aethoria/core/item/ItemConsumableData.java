package com.aethoria.core.item;

public record ItemConsumableData(
    String effectId,
    double potency,
    double durationSeconds
) {
    private static final ItemConsumableData EMPTY = new ItemConsumableData("", 0.0D, 0.0D);

    public static ItemConsumableData empty() {
        return EMPTY;
    }

    public boolean isEmpty() {
        return effectId == null || effectId.isBlank();
    }
}
