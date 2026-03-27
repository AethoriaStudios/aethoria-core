package com.aethoria.core.item;

import com.aethoria.core.AethoriaCorePlugin;
import org.bukkit.NamespacedKey;

public final class ItemKeys {
    private final NamespacedKey itemIdKey;
    private final NamespacedKey requiredClassKey;
    private final NamespacedKey rarityKey;

    public ItemKeys(AethoriaCorePlugin plugin) {
        this.itemIdKey = new NamespacedKey(plugin, "item_id");
        this.requiredClassKey = new NamespacedKey(plugin, "required_class");
        this.rarityKey = new NamespacedKey(plugin, "rarity");
    }

    public NamespacedKey getItemIdKey() {
        return itemIdKey;
    }

    public NamespacedKey getRequiredClassKey() {
        return requiredClassKey;
    }

    public NamespacedKey getRarityKey() {
        return rarityKey;
    }
}
