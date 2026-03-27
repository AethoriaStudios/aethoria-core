package com.aethoria.core.item;

import com.aethoria.core.AethoriaCorePlugin;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ItemRegistryService {
    private final AethoriaCorePlugin plugin;
    private Map<String, AethoriaItemDefinition> definitions = Map.of();

    public ItemRegistryService(AethoriaCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        ensureItemsFileExists();
        reload();
    }

    public ReloadResult reload() {
        boolean createdDefaultFile = ensureItemsFileExists();
        File file = new File(plugin.getDataFolder(), "items.yml");
        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection itemsSection = configuration.getConfigurationSection("items");
        if (itemsSection == null) {
            definitions = Map.of();
            plugin.getLogger().warning("items.yml is missing the items section.");
            return new ReloadResult(0, 0, createdDefaultFile);
        }

        Map<String, AethoriaItemDefinition> loadedDefinitions = new LinkedHashMap<>();
        int invalidDefinitions = 0;
        for (String itemId : itemsSection.getKeys(false)) {
            ConfigurationSection section = itemsSection.getConfigurationSection(itemId);
            if (section == null) {
                continue;
            }

            try {
                AethoriaItemDefinition definition = parseDefinition(itemId, section);
                loadedDefinitions.put(definition.id(), definition);
            } catch (IllegalArgumentException exception) {
                invalidDefinitions++;
                plugin.getLogger().log(Level.WARNING, "Skipping invalid item definition '" + itemId + "'.", exception);
            }
        }

        definitions = Collections.unmodifiableMap(loadedDefinitions);
        plugin.getLogger().info("Loaded " + definitions.size() + " item definitions from items.yml" + (invalidDefinitions > 0 ? " with " + invalidDefinitions + " invalid entries skipped." : "."));
        return new ReloadResult(definitions.size(), invalidDefinitions, createdDefaultFile);
    }

    public Optional<AethoriaItemDefinition> getDefinition(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(definitions.get(normalizeId(itemId)));
    }

    public Collection<AethoriaItemDefinition> getDefinitions() {
        return definitions.values();
    }

    private AethoriaItemDefinition parseDefinition(String rawItemId, ConfigurationSection section) {
        String itemId = normalizeId(rawItemId);
        String displayName = section.getString("display-name", itemId);
        ItemRarity rarity = parseEnum(section.getString("rarity", "COMMON"), ItemRarity.class, "rarity");
        ItemType type = parseEnum(section.getString("type", "MATERIAL"), ItemType.class, "type");
        Material material = Material.matchMaterial(section.getString("material", "STONE"));
        if (material == null || material.isAir()) {
            throw new IllegalArgumentException("Invalid material.");
        }

        String requiredClass = section.getString("required-class", "").trim().toUpperCase(Locale.ROOT);
        int levelRequirement = Math.max(0, section.getInt("level-requirement", 1));
        Integer customModelData = section.contains("custom-model-data") ? section.getInt("custom-model-data") : null;

        ConfigurationSection statsSection = section.getConfigurationSection("stats");
        ItemStats stats = new ItemStats(
            getDouble(statsSection, "damage"),
            getDouble(statsSection, "defense"),
            getDouble(statsSection, "magic-power"),
            getDouble(statsSection, "crit-chance"),
            getDouble(statsSection, "crit-damage"),
            getDouble(statsSection, "health")
        );

        return new AethoriaItemDefinition(itemId, displayName, rarity, type, material, requiredClass, levelRequirement, customModelData, stats);
    }

    private double getDouble(ConfigurationSection section, String path) {
        return section == null ? 0.0D : section.getDouble(path, 0.0D);
    }

    private <T extends Enum<T>> T parseEnum(String rawValue, Class<T> type, String fieldName) {
        try {
            return Enum.valueOf(type, rawValue.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Invalid " + fieldName + ": " + rawValue, exception);
        }
    }

    private String normalizeId(String itemId) {
        return itemId.trim().toLowerCase(Locale.ROOT);
    }

    private boolean ensureItemsFileExists() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            throw new IllegalStateException("Could not create plugin data folder for items.yml.");
        }

        File itemsFile = new File(plugin.getDataFolder(), "items.yml");
        if (itemsFile.exists()) {
            return false;
        }

        try {
            plugin.saveResource("items.yml", false);
            plugin.getLogger().info("Created default items.yml in plugin data folder.");
            return true;
        } catch (IllegalArgumentException exception) {
            try {
                if (itemsFile.createNewFile()) {
                    plugin.getLogger().warning("Created empty items.yml because bundled default was unavailable.");
                    return true;
                }
            } catch (IOException ioException) {
                throw new IllegalStateException("Could not create items.yml.", ioException);
            }
            return false;
        }
    }

    public record ReloadResult(int loadedDefinitions, int invalidDefinitions, boolean createdDefaultFile) {
    }
}
