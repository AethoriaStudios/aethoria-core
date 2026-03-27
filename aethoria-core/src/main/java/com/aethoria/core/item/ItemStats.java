package com.aethoria.core.item;

import java.util.LinkedHashMap;
import java.util.Map;

public record ItemStats(
    double damage,
    double defense,
    double magicPower,
    double critChance,
    double critDamage,
    double health
) {
    public boolean isEmpty() {
        return damage == 0.0D
            && defense == 0.0D
            && magicPower == 0.0D
            && critChance == 0.0D
            && critDamage == 0.0D
            && health == 0.0D;
    }

    public Map<String, Double> asDisplayMap() {
        Map<String, Double> values = new LinkedHashMap<>();
        addIfPresent(values, "Damage", damage);
        addIfPresent(values, "Defense", defense);
        addIfPresent(values, "Magic Power", magicPower);
        addIfPresent(values, "Crit Chance", critChance);
        addIfPresent(values, "Crit Damage", critDamage);
        addIfPresent(values, "Health", health);
        return values;
    }

    private static void addIfPresent(Map<String, Double> values, String key, double value) {
        if (value != 0.0D) {
            values.put(key, value);
        }
    }
}
