package com.aethoria.core.listener;

import com.aethoria.core.AethoriaCorePlugin;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerConnectionListener implements Listener {
    private final AethoriaCorePlugin plugin;

    public PlayerConnectionListener(AethoriaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getProfileService().preload(player.getUniqueId());
        logJoinDebugDetails(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getGameplayStatService().clear(event.getPlayer().getUniqueId());
        plugin.getProfileService().saveAndEvict(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        plugin.getGameplayStatService().clear(event.getPlayer().getUniqueId());
        plugin.getProfileService().saveAndEvict(event.getPlayer().getUniqueId());
    }

    private void logJoinDebugDetails(Player player) {
        if (!plugin.getConfig().getBoolean("systems.debug-logging", false)) {
            return;
        }

        String activeClass = plugin.getClassSwapService().getActiveClass(player.getUniqueId());
        int adventurerLevel = plugin.getProgressionService().getLevel(player.getUniqueId());
        String mainHandItemId = plugin.getItemFactory().getItemId(player.getInventory().getItemInMainHand()).orElse("none");
        String armorItemIds = Arrays.stream(player.getInventory().getArmorContents())
            .map(itemStack -> plugin.getItemFactory().getItemId(itemStack).orElse("none"))
            .collect(Collectors.joining(", "));
        plugin.getLogger().info("[Debug] Player join: name=" + player.getName()
            + ", class=" + activeClass
            + ", level=" + adventurerLevel
            + ", mainHandAuthoredItem=" + mainHandItemId
            + ", armorAuthoredItems=[" + armorItemIds + "]");
    }
}
