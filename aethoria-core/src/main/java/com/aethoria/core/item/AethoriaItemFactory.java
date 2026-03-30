package com.aethoria.core.item;

import com.aethoria.core.AethoriaCorePlugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class AethoriaItemFactory {
    private final AethoriaCorePlugin plugin;
    private final ItemRegistryService registryService;
    private final ItemKeys itemKeys;

    public AethoriaItemFactory(AethoriaCorePlugin plugin, ItemRegistryService registryService, ItemKeys itemKeys) {
        this.plugin = plugin;
        this.registryService = registryService;
        this.itemKeys = itemKeys;
    }

    public Optional<ItemStack> createItem(String itemId, int amount) {
        return registryService.getDefinition(itemId).map(definition -> createItem(definition, amount));
    }

    public Optional<AethoriaItemDefinition> getDefinition(ItemStack itemStack) {
        return getItemId(itemStack).flatMap(registryService::getDefinition);
    }

    public Optional<String> getItemId(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR || !itemStack.hasItemMeta()) {
            return Optional.empty();
        }

        ItemMeta itemMeta = itemStack.getItemMeta();
        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        String itemId = container.get(itemKeys.getItemIdKey(), PersistentDataType.STRING);
        return itemId == null || itemId.isBlank() ? Optional.empty() : Optional.of(itemId);
    }

    public boolean isRestrictedForClass(ItemStack itemStack, String activeClass) {
        if (plugin.getClassSwapService().isOmniTestClass(activeClass)) {
            return false;
        }

        Optional<AethoriaItemDefinition> definition = getDefinition(itemStack);
        if (definition.isEmpty() || !definition.get().hasClassRestriction()) {
            return false;
        }
        return !definition.get().requiredClass().equals(normalizeClass(activeClass));
    }

    public boolean isRestrictedForLevel(ItemStack itemStack, int playerLevel) {
        Optional<AethoriaItemDefinition> definition = getDefinition(itemStack);
        return definition.isPresent() && playerLevel < definition.get().levelRequirement();
    }

    public Optional<Integer> getLevelRequirement(ItemStack itemStack) {
        return getDefinition(itemStack).map(AethoriaItemDefinition::levelRequirement);
    }

    public ItemStats getGameplayStats(ItemStack itemStack, String activeClass, int playerLevel) {
        if (isRestrictedForClass(itemStack, activeClass) || isRestrictedForLevel(itemStack, playerLevel)) {
            return ItemStats.empty();
        }

        return getDefinition(itemStack)
            .map(AethoriaItemDefinition::stats)
            .orElse(ItemStats.empty());
    }

    public Optional<ItemConsumableData> getConsumableData(ItemStack itemStack) {
        return getDefinition(itemStack)
            .map(AethoriaItemDefinition::consumableData)
            .filter(consumableData -> consumableData != null && !consumableData.isEmpty());
    }

    private ItemStack createItem(AethoriaItemDefinition definition, int amount) {
        ItemStack itemStack = new ItemStack(definition.material(), Math.max(1, amount));
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(definition.rarity().getColor() + ChatColor.translateAlternateColorCodes('&', definition.displayName()));
        itemMeta.setLore(buildLore(definition));
        if (definition.type() == ItemType.CONSUMABLE) {
            itemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        }

        if (definition.customModelData() != null) {
            itemMeta.setCustomModelData(definition.customModelData());
        }

        PersistentDataContainer container = itemMeta.getPersistentDataContainer();
        container.set(itemKeys.getItemIdKey(), PersistentDataType.STRING, definition.id());
        container.set(itemKeys.getRarityKey(), PersistentDataType.STRING, definition.rarity().name());
        if (definition.hasClassRestriction()) {
            container.set(itemKeys.getRequiredClassKey(), PersistentDataType.STRING, definition.requiredClass());
        }

        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private List<String> buildLore(AethoriaItemDefinition definition) {
        List<String> lore = new ArrayList<>();
        lore.add(definition.rarity().getColor() + "" + ChatColor.BOLD + definition.rarity().getDisplayName());
        lore.add(ChatColor.DARK_GRAY + "Type: " + ChatColor.GRAY + formatEnum(definition.type().name()));
        if (definition.hasClassRestriction()) {
            lore.add(ChatColor.DARK_GRAY + "Class: " + ChatColor.WHITE + formatEnum(definition.requiredClass()));
        }
        lore.add(ChatColor.DARK_GRAY + "Level: " + ChatColor.WHITE + definition.levelRequirement());

        if (!definition.stats().isEmpty()) {
            lore.add("");
            definition.stats().asDisplayMap().forEach((stat, value) -> lore.add(ChatColor.GRAY + stat + ": " + ChatColor.GREEN + "+" + formatNumber(value)));
        }

        if (definition.hasConsumableData()) {
            lore.add("");
            lore.add(ChatColor.GRAY + "Effect: " + ChatColor.AQUA + formatEnum(definition.consumableData().effectId()));
            lore.add(ChatColor.GRAY + "Potency: " + ChatColor.GREEN + formatNumber(definition.consumableData().potency()));
            if (definition.consumableData().durationSeconds() > 0.0D) {
                lore.add(ChatColor.GRAY + "Duration: " + ChatColor.WHITE + formatNumber(definition.consumableData().durationSeconds()) + "s");
            }
        }

        lore.add("");
        lore.add(ChatColor.DARK_GRAY + "Aethoria ID: " + definition.id());
        return lore;
    }

    private String formatEnum(String value) {
        String normalized = value.toLowerCase(Locale.ROOT).replace('_', ' ');
        String[] words = normalized.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.toString();
    }

    private String formatNumber(double value) {
        if (Math.floor(value) == value) {
            return Long.toString((long) value);
        }
        return String.format(Locale.US, "%.2f", value);
    }

    private String normalizeClass(String classId) {
        if (classId == null || classId.isBlank()) {
            return "";
        }
        return classId.trim().toUpperCase(Locale.ROOT);
    }
}
