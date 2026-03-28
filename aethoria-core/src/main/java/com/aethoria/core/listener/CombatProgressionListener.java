package com.aethoria.core.listener;

import com.aethoria.core.AethoriaCorePlugin;
import com.aethoria.core.service.ProgressionService;
import java.util.List;
import java.util.Locale;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.Sound;

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
        ConfigurationSection feedbackSection = plugin.getConfig().getConfigurationSection("progression.feedback");
        boolean useActionBar = feedbackSection == null || feedbackSection.getBoolean("use-action-bar", true);
        String xpMessage = ChatColor.AQUA + "+" + xpReward + " Adventurer XP" + ChatColor.GRAY + " from " + formatEntityName(event.getEntity().getType());

        if (useActionBar) {
            String progressText = xpToNext > 0
                ? ChatColor.GRAY + " • " + ChatColor.WHITE + result.experience() + ChatColor.GRAY + "/" + ChatColor.WHITE + xpToNext + " XP"
                : ChatColor.GOLD + " • MAX LEVEL";
            killer.sendActionBar(net.kyori.adventure.text.Component.text().append(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(xpMessage + progressText)).build());
        }

        if (result.levelsGained() > 0) {
            String levelUpMessage = ChatColor.GOLD + "✦ LEVEL UP! " + ChatColor.YELLOW + "You are now Adventurer Level " + result.level() + '.';
            killer.sendMessage(levelUpMessage);
            killer.showTitle(
                net.kyori.adventure.title.Title.title(
                    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(ChatColor.GOLD + "LEVEL UP!"),
                    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(ChatColor.YELLOW + "Adventurer Level " + result.level()),
                    net.kyori.adventure.title.Title.Times.times(java.time.Duration.ofMillis(200), java.time.Duration.ofMillis(1800), java.time.Duration.ofMillis(600))
                )
            );
            killer.playSound(killer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0F, 1.1F);
            if (feedbackSection == null || feedbackSection.getBoolean("broadcast-level-up-action-bar", true)) {
                killer.sendActionBar(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(levelUpMessage));
            }
        }

        if (xpToNext == 0) {
            killer.sendActionBar(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(ChatColor.GOLD + "You have reached the current max adventurer level."));
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

        ConfigurationSection categoriesSection = progressionSection.getConfigurationSection("categories");
        if (categoriesSection != null) {
            for (String categoryKey : categoriesSection.getKeys(false)) {
                ConfigurationSection categorySection = categoriesSection.getConfigurationSection(categoryKey);
                if (categorySection == null) {
                    continue;
                }

                List<String> entityNames = categorySection.getStringList("entities");
                boolean matchesCategory = entityNames.stream().map(name -> name.trim().toUpperCase(Locale.ROOT)).anyMatch(entityKey::equals);
                if (matchesCategory) {
                    return Math.max(0, categorySection.getInt("xp", 0));
                }
            }
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
