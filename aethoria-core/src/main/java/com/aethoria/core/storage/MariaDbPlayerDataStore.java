package com.aethoria.core.storage;

import com.aethoria.core.AethoriaCorePlugin;
import com.aethoria.core.model.PlayerProfile;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

public final class MariaDbPlayerDataStore implements PlayerDataStore {
    private final AethoriaCorePlugin plugin;
    private Connection connection;

    public MariaDbPlayerDataStore(AethoriaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() throws Exception {
        Class.forName("org.mariadb.jdbc.Driver");
        connection = DriverManager.getConnection(buildJdbcUrl(), plugin.getConfig().getString("database.username"), plugin.getConfig().getString("database.password"));
        createSchema();
    }

    @Override
    public PlayerProfile load(UUID playerId, String defaultClass) throws Exception {
        ensureConnection();
        String sql = "SELECT aethor, dungeon_coins, adventurer_level, adventurer_experience, active_class, last_dungeon_daily_bonus FROM " + getTableName() + " WHERE player_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return PlayerProfile.createDefault(playerId, defaultClass);
                }

                Date lastBonusValue = resultSet.getDate("last_dungeon_daily_bonus");
                LocalDate lastBonus = lastBonusValue == null ? null : lastBonusValue.toLocalDate();
                return new PlayerProfile(
                    playerId,
                    resultSet.getDouble("aethor"),
                    resultSet.getInt("dungeon_coins"),
                    resultSet.getInt("adventurer_level"),
                    resultSet.getInt("adventurer_experience"),
                    resultSet.getString("active_class"),
                    lastBonus
                );
            }
        }
    }

    @Override
    public void save(PlayerProfile profile) throws Exception {
        ensureConnection();
        String sql = "INSERT INTO " + getTableName() + " (player_id, aethor, dungeon_coins, adventurer_level, adventurer_experience, active_class, last_dungeon_daily_bonus) VALUES (?, ?, ?, ?, ?, ?, ?) "
            + "ON DUPLICATE KEY UPDATE aethor = VALUES(aethor), dungeon_coins = VALUES(dungeon_coins), adventurer_level = VALUES(adventurer_level), adventurer_experience = VALUES(adventurer_experience), active_class = VALUES(active_class), last_dungeon_daily_bonus = VALUES(last_dungeon_daily_bonus)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, profile.getPlayerId().toString());
            statement.setDouble(2, profile.getAethor());
            statement.setInt(3, profile.getDungeonCoins());
            statement.setInt(4, profile.getAdventurerLevel());
            statement.setInt(5, profile.getAdventurerExperience());
            statement.setString(6, profile.getActiveClass());
            if (profile.getLastDungeonDailyBonus() == null) {
                statement.setDate(7, null);
            } else {
                statement.setDate(7, Date.valueOf(profile.getLastDungeonDailyBonus()));
            }
            statement.executeUpdate();
        }
    }

    @Override
    public void saveAll(Collection<PlayerProfile> profiles) throws Exception {
        for (PlayerProfile profile : profiles) {
            save(profile);
        }
    }

    @Override
    public String getStorageName() {
        return "MariaDB";
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private void createSchema() throws SQLException {
        ensureConnection();
        String sql = "CREATE TABLE IF NOT EXISTS " + getTableName() + " ("
            + "player_id VARCHAR(36) NOT NULL PRIMARY KEY, "
            + "aethor DOUBLE NOT NULL DEFAULT 0, "
            + "dungeon_coins INT NOT NULL DEFAULT 0, "
            + "adventurer_level INT NOT NULL DEFAULT 1, "
            + "adventurer_experience INT NOT NULL DEFAULT 0, "
            + "active_class VARCHAR(32) NOT NULL, "
            + "last_dungeon_daily_bonus DATE NULL"
            + ")";

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
            statement.execute("ALTER TABLE " + getTableName() + " ADD COLUMN IF NOT EXISTS adventurer_level INT NOT NULL DEFAULT 1");
            statement.execute("ALTER TABLE " + getTableName() + " ADD COLUMN IF NOT EXISTS adventurer_experience INT NOT NULL DEFAULT 0");
        }
    }

    private void ensureConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("MariaDB connection is not available.");
        }
    }

    private String buildJdbcUrl() {
        String host = plugin.getConfig().getString("database.host", "localhost");
        int port = plugin.getConfig().getInt("database.port", 3306);
        String database = plugin.getConfig().getString("database.name", "aethoria");
        boolean useSsl = plugin.getConfig().getBoolean("database.use-ssl", false);
        return "jdbc:mariadb://" + host + ':' + port + '/' + database + "?useSsl=" + useSsl;
    }

    private String getTableName() {
        String prefix = plugin.getConfig().getString("database.table-prefix", "aethoria_").replaceAll("[^A-Za-z0-9_]", "");
        if (prefix.isBlank()) {
            prefix = "aethoria_";
        }
        return prefix + "player_profiles";
    }
}
