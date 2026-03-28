package com.aethoria.core.service;

import com.aethoria.core.item.AethoriaItemDefinition;
import com.aethoria.core.item.ItemRegistryService;
import com.aethoria.core.item.ItemType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class ClassItemSetService {
    private static final List<ItemType> ARMOR_ORDER = List.of(ItemType.HELMET, ItemType.CHESTPLATE, ItemType.LEGGINGS, ItemType.BOOTS);

    private final ItemRegistryService itemRegistryService;

    public ClassItemSetService(ItemRegistryService itemRegistryService) {
        this.itemRegistryService = itemRegistryService;
    }

    public Optional<AethoriaItemDefinition> findPrimaryWeapon(String classId) {
        return itemRegistryService.getDefinitions().stream()
            .filter(definition -> definition.type() == ItemType.WEAPON)
            .filter(definition -> matchesClass(definition, classId))
            .sorted(Comparator.comparing(AethoriaItemDefinition::id))
            .findFirst();
    }

    public Map<ItemType, AethoriaItemDefinition> findArmorSet(String classId) {
        Map<ItemType, AethoriaItemDefinition> armorSet = new EnumMap<>(ItemType.class);
        itemRegistryService.getDefinitions().stream()
            .filter(definition -> definition.type().isArmor())
            .filter(definition -> matchesClass(definition, classId))
            .sorted(Comparator.comparing(AethoriaItemDefinition::id))
            .forEach(definition -> armorSet.putIfAbsent(definition.type(), definition));
        return armorSet;
    }

    public List<AethoriaItemDefinition> findFullStarterLoadout(String classId) {
        List<AethoriaItemDefinition> loadout = new ArrayList<>();
        findPrimaryWeapon(classId).ifPresent(loadout::add);

        Map<ItemType, AethoriaItemDefinition> armorSet = findArmorSet(classId);
        for (ItemType armorType : ARMOR_ORDER) {
            AethoriaItemDefinition definition = armorSet.get(armorType);
            if (definition != null) {
                loadout.add(definition);
            }
        }
        return loadout;
    }

    public List<ItemType> getMissingArmorPieces(String classId) {
        Map<ItemType, AethoriaItemDefinition> armorSet = findArmorSet(classId);
        return ARMOR_ORDER.stream()
            .filter(itemType -> !armorSet.containsKey(itemType))
            .toList();
    }

    private boolean matchesClass(AethoriaItemDefinition definition, String classId) {
        return definition.hasClassRestriction() && definition.requiredClass().equals(normalizeClassId(classId));
    }

    private String normalizeClassId(String classId) {
        return classId == null ? "" : classId.trim().toUpperCase(Locale.ROOT);
    }
}
