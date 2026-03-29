package com.aethoria.core.item;

import com.aethoria.core.AethoriaCorePlugin;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
            return new ReloadResult(0, 0, 0, createdDefaultFile);
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

        List<String> validationWarnings = collectValidationWarnings(loadedDefinitions.values());
        for (String warning : validationWarnings) {
            plugin.getLogger().warning("[Item Validation Warning] " + warning);
        }

        definitions = Collections.unmodifiableMap(loadedDefinitions);
        plugin.getLogger().info(
            "Loaded " + definitions.size() + " item definitions from items.yml"
                + (invalidDefinitions > 0 ? " with " + invalidDefinitions + " invalid entries skipped" : "")
                + (validationWarnings.isEmpty() ? "." : " and " + validationWarnings.size() + " validation warnings.")
        );
        return new ReloadResult(definitions.size(), invalidDefinitions, validationWarnings.size(), createdDefaultFile);
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
        if (itemId.isBlank()) {
            throw new IllegalArgumentException("Item id cannot be blank.");
        }

        String displayName = section.getString("display-name", itemId);
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("display-name cannot be blank.");
        }

        ItemRarity rarity = parseEnum(section.getString("rarity", "COMMON"), ItemRarity.class, "rarity");
        ItemType type = parseEnum(section.getString("type", "MATERIAL"), ItemType.class, "type");
        Material material = Material.matchMaterial(section.getString("material", "STONE"));
        if (material == null || material.isAir()) {
            throw new IllegalArgumentException("Invalid material.");
        }

        String requiredClass = section.getString("required-class", "").trim().toUpperCase(Locale.ROOT);
        validateRequiredClass(itemId, requiredClass);
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

        ConfigurationSection consumableSection = section.getConfigurationSection("consumable");
        ItemConsumableData consumableData = new ItemConsumableData(
            getString(consumableSection, "effect-id").toUpperCase(Locale.ROOT),
            getDouble(consumableSection, "potency"),
            getDouble(consumableSection, "duration-seconds")
        );

        return new AethoriaItemDefinition(itemId, displayName, rarity, type, material, requiredClass, levelRequirement, customModelData, stats, consumableData);
    }

    private double getDouble(ConfigurationSection section, String path) {
        return section == null ? 0.0D : section.getDouble(path, 0.0D);
    }

    private String getString(ConfigurationSection section, String path) {
        return section == null ? "" : section.getString(path, "").trim();
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

    private void validateRequiredClass(String itemId, String requiredClass) {
        if (requiredClass.isBlank()) {
            return;
        }

        Set<String> supportedClasses = plugin.getConfig().getStringList("classes.available").stream()
            .map(classId -> classId.trim().toUpperCase(Locale.ROOT))
            .filter(classId -> !classId.isBlank())
            .collect(java.util.stream.Collectors.toSet());

        if (!supportedClasses.contains(requiredClass)) {
            throw new IllegalArgumentException(
                "Invalid required-class '" + requiredClass + "' for item '" + itemId + "'. Allowed classes: " + String.join(", ", supportedClasses)
            );
        }
    }

    private List<String> collectValidationWarnings(Collection<AethoriaItemDefinition> definitions) {
        List<String> warnings = new ArrayList<>();
        Map<String, List<String>> byClassAndType = new HashMap<>();
        Map<String, List<String>> byClassTypeAndMaterial = new HashMap<>();
        Map<String, List<String>> byNormalizedDisplayName = new HashMap<>();

        for (AethoriaItemDefinition definition : definitions) {
            String normalizedDisplayName = definition.displayName().trim().toLowerCase(Locale.ROOT);
            byNormalizedDisplayName.computeIfAbsent(normalizedDisplayName, ignored -> new ArrayList<>()).add(definition.id());

            if (definition.type() != ItemType.MATERIAL && definition.type() != ItemType.CONSUMABLE && definition.stats().isEmpty()) {
                warnings.add("Item '" + definition.id() + "' has no authored stats even though it is a " + definition.type().name() + ".");
            }

            if (definition.type() == ItemType.CONSUMABLE && !definition.hasConsumableData()) {
                warnings.add("Consumable item '" + definition.id() + "' is missing its consumable section.");
            }

            if (definition.type() != ItemType.CONSUMABLE && definition.hasConsumableData()) {
                warnings.add("Item '" + definition.id() + "' defines consumable data but is typed as " + definition.type().name() + ".");
            }

            if (!definition.hasClassRestriction()) {
                continue;
            }

            String classTypeKey = definition.requiredClass() + '|' + definition.type().name();
            byClassAndType.computeIfAbsent(classTypeKey, ignored -> new ArrayList<>()).add(definition.id());

            String classTypeMaterialKey = classTypeKey + '|' + definition.material().name();
            byClassTypeAndMaterial.computeIfAbsent(classTypeMaterialKey, ignored -> new ArrayList<>()).add(definition.id());
        }

        for (Map.Entry<String, List<String>> entry : byClassAndType.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            String requiredClass = parts[0];
            String itemType = parts[1];
            List<String> itemIds = entry.getValue();

            if (itemIds.size() > 1) {
                warnings.add("Duplicate starter role detected for class " + requiredClass + " and type " + itemType + ": " + String.join(", ", itemIds));
            }
        }

        for (Map.Entry<String, List<String>> entry : byClassTypeAndMaterial.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            String requiredClass = parts[0];
            String itemType = parts[1];
            String material = parts[2];
            List<String> itemIds = entry.getValue();

            if (itemIds.size() > 1) {
                warnings.add("Duplicate material detected for class " + requiredClass + ", type " + itemType + ", material " + material + ": " + String.join(", ", itemIds));
            }
        }

        for (Map.Entry<String, List<String>> entry : byNormalizedDisplayName.entrySet()) {
            List<String> itemIds = entry.getValue();
            if (itemIds.size() > 1) {
                warnings.add("Duplicate display name detected across authored items: '" + entry.getKey() + "' used by " + String.join(", ", itemIds));
            }
        }

        return warnings;
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

    public record ReloadResult(int loadedDefinitions, int invalidDefinitions, int warningCount, boolean createdDefaultFile) {
    }
}
