package com.aethoria.core.item;

import org.bukkit.Material;

public record AethoriaItemDefinition(
    String id,
    String displayName,
    ItemRarity rarity,
    ItemType type,
    Material material,
    String requiredClass,
    int levelRequirement,
    Integer customModelData,
    ItemStats stats
) {
    public boolean hasClassRestriction() {
        return requiredClass != null && !requiredClass.isBlank();
    }
}
