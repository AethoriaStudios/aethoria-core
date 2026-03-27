package com.aethoria.core.item;

import org.bukkit.ChatColor;

public enum ItemRarity {
    COMMON(ChatColor.WHITE, "Common"),
    UNCOMMON(ChatColor.GREEN, "Uncommon"),
    RARE(ChatColor.AQUA, "Rare"),
    EPIC(ChatColor.LIGHT_PURPLE, "Epic"),
    LEGENDARY(ChatColor.GOLD, "Legendary");

    private final ChatColor color;
    private final String displayName;

    ItemRarity(ChatColor color, String displayName) {
        this.color = color;
        this.displayName = displayName;
    }

    public ChatColor getColor() {
        return color;
    }

    public String getDisplayName() {
        return displayName;
    }
}
