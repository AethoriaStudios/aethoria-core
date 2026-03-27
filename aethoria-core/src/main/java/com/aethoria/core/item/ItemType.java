package com.aethoria.core.item;

public enum ItemType {
    WEAPON,
    HELMET,
    CHESTPLATE,
    LEGGINGS,
    BOOTS,
    CONSUMABLE,
    MATERIAL,
    ACCESSORY;

    public boolean isArmor() {
        return this == HELMET || this == CHESTPLATE || this == LEGGINGS || this == BOOTS;
    }
}
