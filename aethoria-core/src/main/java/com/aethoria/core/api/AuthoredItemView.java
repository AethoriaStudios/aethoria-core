package com.aethoria.core.api;

import com.aethoria.core.item.AethoriaItemDefinition;
import com.aethoria.core.item.ItemStats;
import com.aethoria.core.item.ItemType;

public record AuthoredItemView(
    String itemId,
    String displayName,
    String rarity,
    String material,
    String requiredClass,
    int levelRequirement,
    ItemType type,
    Integer customModelData,
    ItemStats stats
) {
    public static AuthoredItemView fromDefinition(AethoriaItemDefinition definition) {
        return new AuthoredItemView(
            definition.id(),
            definition.displayName(),
            definition.rarity().name(),
            definition.material().name(),
            definition.requiredClass(),
            definition.levelRequirement(),
            definition.type(),
            definition.customModelData(),
            definition.stats()
        );
    }

    public boolean hasClassRestriction() {
        return requiredClass != null && !requiredClass.isBlank();
    }
}
