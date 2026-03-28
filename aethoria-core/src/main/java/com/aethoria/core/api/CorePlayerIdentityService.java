package com.aethoria.core.api;

import com.aethoria.core.AethoriaCorePlugin;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public final class CorePlayerIdentityService {
    private final AethoriaCorePlugin plugin;

    public CorePlayerIdentityService(AethoriaCorePlugin plugin) {
        this.plugin = plugin;
    }

    public Optional<Player> findOnlinePlayer(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(plugin.getServer().getPlayerExact(name));
    }

    public Optional<PlayerIdentityView> findOnlineIdentity(String name) {
        return findOnlinePlayer(name).map(this::toIdentityView);
    }

    public Optional<PlayerIdentityView> findIdentity(UUID playerId) {
        if (playerId == null) {
            return Optional.empty();
        }

        Player onlinePlayer = plugin.getServer().getPlayer(playerId);
        if (onlinePlayer != null) {
            return Optional.of(toIdentityView(onlinePlayer));
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        if (offlinePlayer.getName() == null) {
            return Optional.empty();
        }

        return Optional.of(new PlayerIdentityView(offlinePlayer.getUniqueId(), offlinePlayer.getName(), offlinePlayer.isOnline()));
    }

    public PlayerIdentityView toIdentityView(Player player) {
        return new PlayerIdentityView(player.getUniqueId(), player.getName(), true);
    }
}
