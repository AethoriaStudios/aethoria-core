package com.aethoria.core.storage;

import com.aethoria.core.AethoriaCorePlugin;
import com.aethoria.core.model.PlayerProfile;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;

public final class YamlPlayerDataStore implements PlayerDataStore {
    private final AethoriaCorePlugin plugin;
    private File dataFile;
    private YamlConfiguration configuration;

    public YamlPlayerDataStore(AethoriaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() throws IOException {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            throw new IOException("Could not create plugin data folder.");
        }

        dataFile = new File(plugin.getDataFolder(), "player-data.yml");
        if (!dataFile.exists() && !dataFile.createNewFile()) {
            throw new IOException("Could not create player-data.yml.");
        }

        configuration = YamlConfiguration.loadConfiguration(dataFile);
    }

    @Override
    public PlayerProfile load(UUID playerId, String defaultClass) {
        String basePath = getBasePath(playerId);
        if (!configuration.contains(basePath)) {
            return PlayerProfile.createDefault(playerId, defaultClass);
        }

        double aethor = configuration.getDouble(basePath + ".aethor", 0.0D);
        int dungeonCoins = configuration.getInt(basePath + ".dungeon-coins", 0);
        int adventurerLevel = configuration.getInt(basePath + ".adventurer-level", 1);
        int adventurerExperience = configuration.getInt(basePath + ".adventurer-experience", 0);
        String activeClass = configuration.getString(basePath + ".active-class", defaultClass);
        String lastBonusRaw = configuration.getString(basePath + ".last-dungeon-daily-bonus");
        LocalDate lastBonus = lastBonusRaw == null || lastBonusRaw.isBlank() ? null : LocalDate.parse(lastBonusRaw);
        return new PlayerProfile(playerId, aethor, dungeonCoins, adventurerLevel, adventurerExperience, activeClass, lastBonus);
    }

    @Override
    public void save(PlayerProfile profile) throws IOException {
        writeProfile(profile);
        configuration.save(dataFile);
    }

    @Override
    public void saveAll(Collection<PlayerProfile> profiles) throws IOException {
        for (PlayerProfile profile : profiles) {
            writeProfile(profile);
        }
        configuration.save(dataFile);
    }

    @Override
    public String getStorageName() {
        return "YAML";
    }

    @Override
    public void close() throws IOException {
        if (configuration != null && dataFile != null) {
            configuration.save(dataFile);
        }
    }

    private void writeProfile(PlayerProfile profile) {
        String basePath = getBasePath(profile.getPlayerId());
        configuration.set(basePath + ".aethor", profile.getAethor());
        configuration.set(basePath + ".dungeon-coins", profile.getDungeonCoins());
        configuration.set(basePath + ".adventurer-level", profile.getAdventurerLevel());
        configuration.set(basePath + ".adventurer-experience", profile.getAdventurerExperience());
        configuration.set(basePath + ".active-class", profile.getActiveClass());
        configuration.set(basePath + ".last-dungeon-daily-bonus", profile.getLastDungeonDailyBonus() == null ? null : profile.getLastDungeonDailyBonus().toString());
    }

    private String getBasePath(UUID playerId) {
        return "players." + playerId;
    }
}
