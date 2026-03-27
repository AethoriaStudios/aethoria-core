package com.aethoria.core.listener;

import com.aethoria.core.AethoriaCorePlugin;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FireworkExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;

public final class TestStaffAbilityListener implements Listener {
    private static final String TEST_STAFF_ITEM_ID = "mage_starter_staff";
    private static final String TEST_ROCKET_TAG = "aethoria_test_staff_rocket";
    private static final double TEST_ROCKET_DAMAGE = 200.0D;
    private static final double TEST_ROCKET_RADIUS = 4.0D;
    private static final int TEST_ROCKET_LIFETIME_TICKS = 30;

    private final AethoriaCorePlugin plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public TestStaffAbilityListener(AethoriaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || !event.getAction().isRightClick()) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        String itemId = plugin.getItemFactory().getItemId(itemStack).orElse("");
        if (!TEST_STAFF_ITEM_ID.equals(itemId)) {
            return;
        }

        long cooldownMillis = getCooldownMillis();
        long now = System.currentTimeMillis();
        long availableAt = cooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (cooldownMillis > 0 && now < availableAt) {
            double secondsRemaining = (availableAt - now) / 1000.0D;
            player.sendMessage(ChatColor.RED + "Test staff is on cooldown for " + String.format(java.util.Locale.US, "%.1f", secondsRemaining) + "s.");
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        if (cooldownMillis > 0) {
            cooldowns.put(player.getUniqueId(), now + cooldownMillis);
        }
        launchTestRocket(player);
    }

    @EventHandler
    public void onFireworkExplode(FireworkExplodeEvent event) {
        Firework firework = event.getEntity();
        if (!firework.getScoreboardTags().contains(TEST_ROCKET_TAG)) {
            return;
        }

        Entity shooter = firework.getShooter() instanceof Entity entity ? entity : null;
        damageNearbyTargets(firework.getLocation(), shooter);
        firework.getWorld().spawnParticle(Particle.EXPLOSION, firework.getLocation(), 3, 0.2D, 0.2D, 0.2D, 0.01D);
        firework.getWorld().playSound(firework.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1.0F, 0.9F);
    }

    private void launchTestRocket(Player player) {
        Location spawnLocation = player.getEyeLocation().add(player.getLocation().getDirection().normalize().multiply(0.5D));
        Firework firework = player.getWorld().spawn(spawnLocation, Firework.class, entity -> {
            entity.addScoreboardTag(TEST_ROCKET_TAG);
            entity.setShotAtAngle(true);
            entity.setShooter(player);

            FireworkMeta meta = entity.getFireworkMeta();
            meta.clearEffects();
            meta.setPower(1);
            meta.addEffect(FireworkEffect.builder()
                .withColor(Color.AQUA)
                .withFade(Color.WHITE)
                .with(FireworkEffect.Type.BALL_LARGE)
                .trail(true)
                .flicker(false)
                .build());
            entity.setFireworkMeta(meta);
        });

        Vector velocity = player.getLocation().getDirection().normalize().multiply(1.8D);
        firework.setVelocity(velocity);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0F, 1.2F);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (firework.isValid() && !firework.isDead()) {
                firework.detonate();
            }
        }, TEST_ROCKET_LIFETIME_TICKS);
    }

    private void damageNearbyTargets(Location center, Entity shooter) {
        Collection<Entity> nearbyEntities = center.getWorld().getNearbyEntities(center, TEST_ROCKET_RADIUS, TEST_ROCKET_RADIUS, TEST_ROCKET_RADIUS);
        int hitCount = 0;
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof LivingEntity livingEntity)) {
                continue;
            }
            if (shooter != null && shooter.getUniqueId().equals(entity.getUniqueId())) {
                continue;
            }

            livingEntity.damage(TEST_ROCKET_DAMAGE, shooter == null ? entity : shooter);
            livingEntity.getWorld().spawnParticle(Particle.CRIT, livingEntity.getLocation().add(0.0D, 1.0D, 0.0D), 12, 0.3D, 0.4D, 0.3D, 0.02D);
            hitCount++;
        }

        if (hitCount > 0 && shooter instanceof Player player) {
            player.sendMessage(ChatColor.AQUA + "Test rocket launched: 100 hearts damage.");
        }
    }

    private long getCooldownMillis() {
        double seconds = Math.max(0.0D, plugin.getConfig().getDouble("testing.test-staff.cooldown-seconds", 2.5D));
        return Math.round(seconds * 1000.0D);
    }
}
