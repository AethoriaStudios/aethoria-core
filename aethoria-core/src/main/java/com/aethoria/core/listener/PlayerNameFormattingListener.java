package com.aethoria.core.listener;

import com.aethoria.core.AethoriaCorePlugin;
import com.aethoria.core.chat.RankStyleService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class PlayerNameFormattingListener implements Listener {
    private final AethoriaCorePlugin plugin;
    private final RankStyleService rankStyleService;

    public PlayerNameFormattingListener(AethoriaCorePlugin plugin, RankStyleService rankStyleService) {
        this.plugin = plugin;
        this.rankStyleService = rankStyleService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        apply(event.getPlayer());
        plugin.getServer().getScheduler().runTask(plugin, () -> apply(event.getPlayer()));
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> apply(event.getPlayer()), 20L);
    }

    public void apply(Player player) {
        player.displayName(rankStyleService.formattedName(player));
        player.playerListName(rankStyleService.formattedName(player));
        player.customName(rankStyleService.formattedName(player));
    }
}
