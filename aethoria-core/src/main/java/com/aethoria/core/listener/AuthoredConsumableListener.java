package com.aethoria.core.listener;

import com.aethoria.core.AethoriaCorePlugin;
import com.aethoria.core.item.ItemConsumableData;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public final class AuthoredConsumableListener implements Listener {
    private static final double DEFAULT_MAX_HEALTH = 20.0D;

    private final AethoriaCorePlugin plugin;

    public AuthoredConsumableListener(AethoriaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        int playerLevel = plugin.getProgressionService().getLevel(event.getPlayer().getUniqueId());
        if (plugin.getItemFactory().isRestrictedForLevel(event.getItem(), playerLevel)) {
            int requiredLevel = plugin.getItemFactory().getLevelRequirement(event.getItem()).orElse(1);
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "You need adventurer level " + requiredLevel + " to use that item.");
            return;
        }

        ItemConsumableData consumableData = plugin.getItemFactory().getConsumableData(event.getItem()).orElse(null);
        if (consumableData == null) {
            return;
        }

        if (!"HEAL".equals(consumableData.effectId())) {
            return;
        }

        Player player = event.getPlayer();
        double healingAmount = Math.max(0.0D, consumableData.potency());
        if (healingAmount <= 0.0D) {
            return;
        }

        // Apply the authored effect after vanilla consumption handling completes.
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            double maxHealth = getMaxHealth(player);
            double healedHealth = Math.min(maxHealth, player.getHealth() + healingAmount);
            if (healedHealth <= player.getHealth()) {
                return;
            }

            player.setHealth(healedHealth);
            player.sendMessage(ChatColor.GREEN + "You recover " + formatNumber(healingAmount) + " health.");
        });
    }

    private double getMaxHealth(Player player) {
        AttributeInstance maxHealthAttribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        return maxHealthAttribute == null ? DEFAULT_MAX_HEALTH : maxHealthAttribute.getValue();
    }

    private String formatNumber(double value) {
        if (Math.floor(value) == value) {
            return Long.toString((long) value);
        }
        return String.format(java.util.Locale.US, "%.2f", value);
    }
}
