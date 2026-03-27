package com.aethoria.core.service;

import com.aethoria.core.AethoriaCorePlugin;
import com.aethoria.core.item.ItemStats;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class GameplayStatService {
    private static final double BASE_MAX_HEALTH = 20.0D;
    private static final double MIN_MAX_HEALTH = 1.0D;

    private final AethoriaCorePlugin plugin;
    private final Map<UUID, ItemStats> appliedStats = new ConcurrentHashMap<>();

    public GameplayStatService(AethoriaCorePlugin plugin) {
        this.plugin = plugin;
    }

    public void refreshLater(Player player) {
        if (player == null) {
            return;
        }

        plugin.getServer().getScheduler().runTask(plugin, () -> refresh(player));
    }

    public void refresh(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }

        UUID playerId = player.getUniqueId();
        String activeClass = plugin.getClassSwapService().getActiveClass(playerId);
        int playerLevel = plugin.getProgressionService().getLevel(playerId);
        ItemStats aggregatedStats = collectEquippedStats(player, activeClass, playerLevel);
        appliedStats.put(playerId, aggregatedStats);
        applyHealth(player, aggregatedStats);
    }

    public ItemStats getStats(UUID playerId) {
        return appliedStats.getOrDefault(playerId, ItemStats.empty());
    }

    public void clear(UUID playerId) {
        appliedStats.remove(playerId);
    }

    private ItemStats collectEquippedStats(Player player, String activeClass, int playerLevel) {
        ItemStats total = ItemStats.empty();
        total = total.add(plugin.getItemFactory().getGameplayStats(player.getInventory().getItemInMainHand(), activeClass, playerLevel));

        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            total = total.add(plugin.getItemFactory().getGameplayStats(armorPiece, activeClass, playerLevel));
        }

        return total;
    }

    private void applyHealth(Player player, ItemStats aggregatedStats) {
        AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttribute == null) {
            return;
        }

        double maxHealth = Math.max(MIN_MAX_HEALTH, BASE_MAX_HEALTH + aggregatedStats.health());
        maxHealthAttribute.setBaseValue(maxHealth);

        if (!player.isDead() && player.getHealth() > maxHealth) {
            player.setHealth(maxHealth);
        }
    }
}
