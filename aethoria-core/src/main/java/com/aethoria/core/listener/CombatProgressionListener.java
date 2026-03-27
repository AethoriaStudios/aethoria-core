package com.aethoria.core.listener;

import com.aethoria.core.AethoriaCorePlugin;
import com.aethoria.core.service.ProgressionService;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public final class CombatProgressionListener implements Listener {
    private final AethoriaCorePlugin plugin;

    public CombatProgressionListener(AethoriaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }

        int xpReward = resolveXpReward(event.getEntity());
        if (xpReward <= 0) {
            return;
        }

        ProgressionService.ProgressionResult result = plugin.getProgressionService().addExperience(killer.getUniqueId(), xpReward);
        int xpToNext = plugin.getProgressionService().getXpToNextLevel(killer.getUniqueId());
        killer.sendMessage(ChatColor.AQUA + "+" + xpReward + " Adventurer XP" + ChatColor.GRAY + " from " + formatEntityName(event.getEntity().getType()));

        if (result.levelsGained() > 0) {
            killer.sendMessage(ChatColor.GOLD + "Level Up! " + ChatColor.YELLOW + "You are now Adventurer Level " + result.level() + '.');
        }

        if (xpToNext > 0) {
            killer.sendMessage(ChatColor.GRAY + "XP: " + ChatColor.WHITE + result.experience() + ChatColor.GRAY + "/" + ChatColor.WHITE + xpToNext);
        } else {
            killer.sendMessage(ChatColor.GOLD + "You have reached the current max adventurer level.");
        }
    }

    private int resolveXpReward(LivingEntity entity) {
        ConfigurationSection progressionSection = plugin.getConfig().getConfigurationSection("progression.mob-kill-xp");
        if (progressionSection == null || !progressionSection.getBoolean("enabled", true)) {
            return 0;
        }

        String entityKey = entity.getType().name();

        ConfigurationSection overridesSection = progressionSection.getConfigurationSection("overrides");
        if (overridesSection != null && overridesSection.contains(entityKey)) {
            return Math.max(0, overridesSection.getInt(entityKey, 0));
        }

        ConfigurationSection bossesSection = progressionSection.getConfigurationSection("bosses");
        if (bossesSection != null && bossesSection.contains(entityKey)) {
            return Math.max(0, bossesSection.getInt(entityKey, 0));
        }

        if (entity instanceof Monster) {
            return Math.max(0, progressionSection.getInt("default-hostile", 25));
        }

        return Math.max(0, progressionSection.getInt("default-passive", 5));
    }

    private String formatEntityName(EntityType entityType) {
        String rawName = entityType.name().toLowerCase().replace('_', ' ');
        String[] words = rawName.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.toString();
    }
}
