package com.aethoria.core.listener;

import com.aethoria.core.AethoriaCorePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public final class ItemRestrictionListener implements Listener {
    private final AethoriaCorePlugin plugin;

    public ItemRestrictionListener(AethoriaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        validateEquipmentLater(event.getPlayer());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            validateEquipmentLater(player);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            validateEquipmentLater(player);
        }
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        validateEquipmentLater(event.getPlayer());
    }

    private void validateEquipmentLater(Player player) {
        plugin.getServer().getScheduler().runTask(plugin, () -> validateEquipment(player));
    }

    private void validateEquipment(Player player) {
        String activeClass = plugin.getClassSwapService().getActiveClass(player.getUniqueId());
        int playerLevel = plugin.getProgressionService().getLevel(player.getUniqueId());
        boolean removedAny = false;

        ItemStack[] armor = player.getInventory().getArmorContents();
        for (int index = 0; index < armor.length; index++) {
            ItemStack armorPiece = armor[index];
            if (!plugin.getItemFactory().isRestrictedForClass(armorPiece, activeClass) && !plugin.getItemFactory().isRestrictedForLevel(armorPiece, playerLevel)) {
                continue;
            }

            armor[index] = null;
            returnItem(player, armorPiece);
            removedAny = true;
        }
        player.getInventory().setArmorContents(armor);

        if (removedAny) {
            player.sendMessage(org.bukkit.ChatColor.RED + "Some equipped items were removed because they do not meet class or level requirements.");
        }

        plugin.getGameplayStatService().refresh(player);
    }

    private void returnItem(Player player, ItemStack itemStack) {
        if (itemStack == null) {
            return;
        }

        player.getInventory().addItem(itemStack).values().forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
    }
}
