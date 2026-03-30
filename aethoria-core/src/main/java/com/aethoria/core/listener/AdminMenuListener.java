package com.aethoria.core.listener;

import com.aethoria.core.AethoriaCorePlugin;
import com.aethoria.core.item.AethoriaItemDefinition;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class AdminMenuListener implements Listener {
    private static final String ADMIN_MENU_ITEM_ID = "admin_menu_star";
    private static final String ADMIN_MENU_TITLE = ChatColor.DARK_AQUA + "Admin Menu";
    private static final String AUTHORED_ITEMS_TITLE_PREFIX = ChatColor.DARK_GREEN + "Authored Items ";
    private static final int ITEMS_PER_PAGE = 45;

    private final AethoriaCorePlugin plugin;

    public AdminMenuListener(AethoriaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        Player player = event.getPlayer();
        if (!player.hasPermission("aethoria.admin")) {
            return;
        }

        String itemId = plugin.getItemFactory().getItemId(event.getItem()).orElse("");
        if (!ADMIN_MENU_ITEM_ID.equals(itemId)) {
            return;
        }

        event.setCancelled(true);
        player.openInventory(createAdminMenu());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!player.hasPermission("aethoria.admin")) {
            return;
        }

        String title = event.getView().getTitle();
        if (!ADMIN_MENU_TITLE.equals(title) && !title.startsWith(AUTHORED_ITEMS_TITLE_PREFIX)) {
            return;
        }

        event.setCancelled(true);
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) {
            return;
        }

        if (ADMIN_MENU_TITLE.equals(title)) {
            handleAdminMenuClick(player, event.getSlot());
            return;
        }

        handleAuthoredItemsClick(player, event.getSlot(), title);
    }

    private void handleAdminMenuClick(Player player, int slot) {
        switch (slot) {
            case 11 -> {
                plugin.reloadAethoria();
                player.sendMessage(ChatColor.GREEN + "Aethoria Core reloaded from the admin menu.");
                player.openInventory(createAdminMenu());
            }
            case 13 -> player.openInventory(createAuthoredItemsMenu(0));
            case 15 -> {
                player.setGameMode(GameMode.CREATIVE);
                player.sendMessage(ChatColor.GREEN + "Game mode updated to Creative.");
            }
            case 16 -> {
                player.setGameMode(GameMode.SURVIVAL);
                player.sendMessage(ChatColor.GREEN + "Game mode updated to Survival.");
            }
            default -> {
            }
        }
    }

    private void handleAuthoredItemsClick(Player player, int slot, String title) {
        int page = parsePage(title);
        if (slot == 45 && page > 0) {
            player.openInventory(createAuthoredItemsMenu(page - 1));
            return;
        }
        if (slot == 49) {
            player.openInventory(createAdminMenu());
            return;
        }
        if (slot == 53 && hasNextPage(page)) {
            player.openInventory(createAuthoredItemsMenu(page + 1));
            return;
        }
        if (slot < 0 || slot >= ITEMS_PER_PAGE) {
            return;
        }

        List<AethoriaItemDefinition> definitions = new ArrayList<>(plugin.getItemRegistryService().getDefinitions());
        int itemIndex = (page * ITEMS_PER_PAGE) + slot;
        if (itemIndex >= definitions.size()) {
            return;
        }

        AethoriaItemDefinition definition = definitions.get(itemIndex);
        ItemStack itemStack = plugin.getItemFactory().createItem(definition.id(), 1).orElse(null);
        if (itemStack == null) {
            player.sendMessage(ChatColor.RED + "Failed to create authored item for " + definition.id() + '.');
            return;
        }

        player.getInventory().addItem(itemStack).values().forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
        player.sendMessage(ChatColor.GREEN + "Granted 1x " + definition.id() + '.');
    }

    private Inventory createAdminMenu() {
        Inventory inventory = Bukkit.createInventory(null, 27, ADMIN_MENU_TITLE);
        fillBorder(inventory, Material.BLUE_STAINED_GLASS_PANE, " ");
        inventory.setItem(11, createButton(Material.REDSTONE, ChatColor.RED + "Reload Core", ChatColor.GRAY + "Reload Aethoria Core config and items"));
        inventory.setItem(13, createButton(Material.CHEST, ChatColor.GOLD + "Authored Items", ChatColor.GRAY + "Browse current and future authored items"));
        inventory.setItem(15, createButton(Material.GRASS_BLOCK, ChatColor.GREEN + "Creative Mode", ChatColor.GRAY + "Switch to Creative mode"));
        inventory.setItem(16, createButton(Material.IRON_SWORD, ChatColor.YELLOW + "Survival Mode", ChatColor.GRAY + "Switch to Survival mode"));
        return inventory;
    }

    private Inventory createAuthoredItemsMenu(int page) {
        Inventory inventory = Bukkit.createInventory(null, 54, AUTHORED_ITEMS_TITLE_PREFIX + (page + 1));
        List<AethoriaItemDefinition> definitions = new ArrayList<>(plugin.getItemRegistryService().getDefinitions());
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, definitions.size());
        for (int index = startIndex; index < endIndex; index++) {
            int inventorySlot = index - startIndex;
            AethoriaItemDefinition definition = definitions.get(index);
            ItemStack preview = plugin.getItemFactory().createItem(definition.id(), 1).orElse(new ItemStack(definition.material()));
            ItemMeta itemMeta = preview.getItemMeta();
            if (itemMeta != null) {
                List<String> lore = itemMeta.hasLore() ? itemMeta.getLore() : new java.util.ArrayList<>();
                lore.add("");
                lore.add(ChatColor.YELLOW + "Click to grant this item");
                itemMeta.setLore(lore);
                preview.setItemMeta(itemMeta);
            }
            inventory.setItem(inventorySlot, preview);
        }

        inventory.setItem(45, createButton(Material.ARROW, ChatColor.GREEN + "Previous Page", ChatColor.GRAY + "Go to the previous page"));
        inventory.setItem(49, createButton(Material.NETHER_STAR, ChatColor.AQUA + "Back to Admin Menu", ChatColor.GRAY + "Return to the main admin menu"));
        inventory.setItem(53, createButton(Material.ARROW, ChatColor.GREEN + "Next Page", ChatColor.GRAY + "Go to the next page"));
        return inventory;
    }

    private boolean hasNextPage(int page) {
        return ((page + 1) * ITEMS_PER_PAGE) < plugin.getItemRegistryService().getDefinitions().size();
    }

    private int parsePage(String title) {
        String rawPage = ChatColor.stripColor(title).replace("Authored Items ", "").trim();
        try {
            return Math.max(0, Integer.parseInt(rawPage) - 1);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private void fillBorder(Inventory inventory, Material material, String name) {
        ItemStack filler = createButton(material, name);
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (slot < 9 || slot >= 18 || slot % 9 == 0 || slot % 9 == 8) {
                inventory.setItem(slot, filler);
            }
        }
    }

    private ItemStack createButton(Material material, String name, String... loreLines) {
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            itemMeta.setDisplayName(name);
            if (loreLines.length > 0) {
                itemMeta.setLore(List.of(loreLines));
            }
            itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }
}
