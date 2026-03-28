package com.aethoria.core.listener;

import com.aethoria.core.AethoriaCorePlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public final class ItemRestrictionListener implements Listener {
    private static final long RESTRICTION_MESSAGE_COOLDOWN_MILLIS = 1500L;

    private final AethoriaCorePlugin plugin;
    private final Map<UUID, Long> lastRestrictionMessageAt = new HashMap<>();

    public ItemRestrictionListener(AethoriaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        validateEquipmentLater(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack attemptedMainHandItem = getAttemptedMainHandItem(event, player);
        if (attemptedMainHandItem != null && isRestrictedForUse(attemptedMainHandItem, player)) {
            event.setCancelled(true);
            sendRestrictionMessage(player, attemptedMainHandItem);
            return;
        }

        validateEquipmentLater(player);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int heldSlot = player.getInventory().getHeldItemSlot();
        if (event.getRawSlots().contains(heldSlot + 36) && isRestrictedForUse(event.getOldCursor(), player)) {
            event.setCancelled(true);
            sendRestrictionMessage(player, event.getOldCursor());
            return;
        }

        validateEquipmentLater(player);
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        validateEquipmentLater(event.getPlayer());
    }

    private void validateEquipmentLater(Player player) {
        plugin.getServer().getScheduler().runTask(plugin, () -> validateEquipment(player));
    }

    private void validateEquipment(Player player) {
        boolean removedAny = false;

        ItemStack[] armor = player.getInventory().getArmorContents();
        for (int index = 0; index < armor.length; index++) {
            ItemStack armorPiece = armor[index];
            if (!isRestrictedForUse(armorPiece, player)) {
                continue;
            }

            armor[index] = null;
            returnItem(player, armorPiece);
            removedAny = true;
        }
        player.getInventory().setArmorContents(armor);

        if (removedAny) {
            sendThrottledMessage(player, org.bukkit.ChatColor.RED + "Some equipped items were removed because they do not meet class or level requirements.");
        }

        plugin.getGameplayStatService().refresh(player);
    }

    private ItemStack getAttemptedMainHandItem(InventoryClickEvent event, Player player) {
        int heldSlot = player.getInventory().getHeldItemSlot();
        if (event.getSlotType() == InventoryType.SlotType.QUICKBAR && event.getSlot() == heldSlot) {
            if (event.getCursor() != null && !event.getCursor().getType().isAir()) {
                return event.getCursor();
            }
        }

        if (event.getClick() == ClickType.NUMBER_KEY && event.getHotbarButton() == heldSlot) {
            return event.getCurrentItem();
        }

        return null;
    }

    private boolean isRestrictedForUse(ItemStack itemStack, Player player) {
        String activeClass = plugin.getClassSwapService().getActiveClass(player.getUniqueId());
        int playerLevel = plugin.getProgressionService().getLevel(player.getUniqueId());
        return plugin.getItemFactory().isRestrictedForClass(itemStack, activeClass)
            || plugin.getItemFactory().isRestrictedForLevel(itemStack, playerLevel);
    }

    private void sendRestrictionMessage(Player player, ItemStack itemStack) {
        String activeClass = plugin.getClassSwapService().getActiveClass(player.getUniqueId());
        if (plugin.getItemFactory().isRestrictedForClass(itemStack, activeClass)) {
            sendThrottledMessage(player, org.bukkit.ChatColor.RED + "Your current class cannot use that item.");
            return;
        }

        int requiredLevel = plugin.getItemFactory().getLevelRequirement(itemStack).orElse(1);
        int currentLevel = plugin.getProgressionService().getLevel(player.getUniqueId());
        sendThrottledMessage(player, org.bukkit.ChatColor.RED + "You need adventurer level " + requiredLevel + " to use that item. Your current level is " + currentLevel + '.');
    }

    private void sendThrottledMessage(Player player, String message) {
        long now = System.currentTimeMillis();
        long lastMessageAt = lastRestrictionMessageAt.getOrDefault(player.getUniqueId(), 0L);
        if (now - lastMessageAt < RESTRICTION_MESSAGE_COOLDOWN_MILLIS) {
            return;
        }

        lastRestrictionMessageAt.put(player.getUniqueId(), now);
        player.sendMessage(message);
    }

    private void returnItem(Player player, ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }

        player.getInventory().addItem(itemStack).values().forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
    }
}
