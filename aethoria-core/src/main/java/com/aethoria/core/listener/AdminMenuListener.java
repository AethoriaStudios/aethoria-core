package com.aethoria.core.listener;

import com.aethoria.core.AethoriaCorePlugin;
import com.aethoria.core.item.AethoriaItemDefinition;
import com.aethoria.core.item.ItemRarity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

public final class AdminMenuListener implements Listener {
    private static final String ADMIN_MENU_ITEM_ID = "admin_menu_star";
    private static final String ADMIN_MENU_TITLE = ChatColor.DARK_AQUA + "Admin Menu";
    private static final String ITEM_CATEGORIES_TITLE = ChatColor.DARK_GREEN + "Item Categories";
    private static final String POTION_TIER_FILTER_TITLE_PREFIX = ChatColor.DARK_PURPLE + "Potion Tiers: ";
    private static final String ITEM_BROWSER_TITLE_PREFIX = ChatColor.DARK_GREEN + "Items: ";
    private static final int ITEMS_PER_PAGE = 45;

    private final AethoriaCorePlugin plugin;

    public AdminMenuListener(AethoriaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || !event.getAction().isRightClick()) {
            return;
        }

        Player player = event.getPlayer();
        if (!canUseAdminMenu(player)) {
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
        if (!canUseAdminMenu(player)) {
            return;
        }

        String title = event.getView().getTitle();
        if (!ADMIN_MENU_TITLE.equals(title)
            && !ITEM_CATEGORIES_TITLE.equals(title)
            && !title.startsWith(POTION_TIER_FILTER_TITLE_PREFIX)
            && !title.startsWith(ITEM_BROWSER_TITLE_PREFIX)) {
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

        if (ITEM_CATEGORIES_TITLE.equals(title)) {
            handleCategoryMenuClick(player, event.getSlot());
            return;
        }

        if (title.startsWith(POTION_TIER_FILTER_TITLE_PREFIX)) {
            handlePotionTierFilterClick(player, event.getSlot(), title);
            return;
        }

        handleItemBrowserClick(player, event.getSlot(), title);
    }

    private void handleAdminMenuClick(Player player, int slot) {
        switch (slot) {
            case 11 -> {
                player.closeInventory();
                boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "reload confirm");
                player.sendMessage(success
                    ? ChatColor.GREEN + "Executed /reload confirm from the admin menu."
                    : ChatColor.RED + "Failed to execute /reload confirm from the admin menu.");
            }
            case 13 -> player.openInventory(createItemCategoriesMenu());
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

    private void handleCategoryMenuClick(Player player, int slot) {
        String categoryId = switch (slot) {
            case 10 -> "warrior-weapons";
            case 11 -> "mage-weapons";
            case 12 -> "paladin-weapons";
            case 14 -> "healing-potions";
            case 15 -> "buff-potions";
            case 19 -> "warrior-armor";
            case 20 -> "mage-armor";
            case 21 -> "paladin-armor";
            case 22 -> "all-items";
            case 26 -> "back";
            default -> null;
        };
        if (categoryId == null) {
            return;
        }
        if ("back".equals(categoryId)) {
            player.openInventory(createAdminMenu());
            return;
        }

        if ("healing-potions".equals(categoryId) || "buff-potions".equals(categoryId)) {
            player.openInventory(createPotionTierFilterMenu(categoryId));
            return;
        }

        player.openInventory(createItemBrowserMenu(categoryId, 0));
    }

    private void handlePotionTierFilterClick(Player player, int slot, String title) {
        String categoryId = parsePotionTierCategory(title);
        String tierId = switch (slot) {
            case 10 -> "common";
            case 11 -> "uncommon";
            case 12 -> "rare";
            case 13 -> "epic";
            case 14 -> "legendary";
            case 15 -> "all";
            case 22 -> "back";
            default -> null;
        };
        if (tierId == null) {
            return;
        }
        if ("back".equals(tierId)) {
            player.openInventory(createItemCategoriesMenu());
            return;
        }

        player.openInventory(createItemBrowserMenu(categoryId + ":" + tierId, 0));
    }

    private void handleItemBrowserClick(Player player, int slot, String title) {
        String categoryId = parseCategoryId(title);
        int page = parsePage(title);
        if (slot == 45 && page > 0) {
            player.openInventory(createItemBrowserMenu(categoryId, page - 1));
            return;
        }
        if (slot == 49) {
            player.openInventory(createItemCategoriesMenu());
            return;
        }
        if (slot == 53 && hasNextPage(categoryId, page)) {
            player.openInventory(createItemBrowserMenu(categoryId, page + 1));
            return;
        }
        if (slot < 0 || slot >= ITEMS_PER_PAGE) {
            return;
        }

        List<AethoriaItemDefinition> definitions = getDefinitionsForCategory(categoryId);
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

    private Inventory createItemCategoriesMenu() {
        Inventory inventory = Bukkit.createInventory(null, 27, ITEM_CATEGORIES_TITLE);
        fillBorder(inventory, Material.BLACK_STAINED_GLASS_PANE, " ");
        inventory.setItem(10, createButton(Material.IRON_SWORD, ChatColor.RED + "Warrior Weapons", ChatColor.GRAY + "Browse warrior authored weapons"));
        inventory.setItem(11, createButton(Material.BLAZE_ROD, ChatColor.LIGHT_PURPLE + "Mage Weapons", ChatColor.GRAY + "Browse mage authored weapons"));
        inventory.setItem(12, createButton(Material.GOLDEN_AXE, ChatColor.GOLD + "Paladin Weapons", ChatColor.GRAY + "Browse paladin authored weapons"));
        inventory.setItem(14, createButton(Material.GLISTERING_MELON_SLICE, ChatColor.GREEN + "Healing Potions", ChatColor.GRAY + "Browse healing authored consumables"));
        inventory.setItem(15, createButton(Material.POTION, ChatColor.AQUA + "Buff Potions", ChatColor.GRAY + "Browse buff authored consumables"));
        inventory.setItem(19, createButton(Material.IRON_CHESTPLATE, ChatColor.RED + "Warrior Armor", ChatColor.GRAY + "Browse warrior authored armor"));
        inventory.setItem(20, createButton(Material.LEATHER_CHESTPLATE, ChatColor.LIGHT_PURPLE + "Mage Armor", ChatColor.GRAY + "Browse mage authored armor"));
        inventory.setItem(21, createButton(Material.GOLDEN_CHESTPLATE, ChatColor.GOLD + "Paladin Armor", ChatColor.GRAY + "Browse paladin authored armor"));
        inventory.setItem(22, createButton(Material.CHEST, ChatColor.YELLOW + "All Authored Items", ChatColor.GRAY + "Browse every authored item"));
        inventory.setItem(26, createButton(Material.NETHER_STAR, ChatColor.AQUA + "Back to Admin Menu", ChatColor.GRAY + "Return to the main admin menu"));
        return inventory;
    }

    private Inventory createPotionTierFilterMenu(String categoryId) {
        Inventory inventory = Bukkit.createInventory(null, 27, buildPotionTierFilterTitle(categoryId));
        fillBorder(inventory, Material.PURPLE_STAINED_GLASS_PANE, " ");
        inventory.setItem(10, createPotionTierButton(ItemRarity.COMMON, "Common", "Show common potions"));
        inventory.setItem(11, createPotionTierButton(ItemRarity.UNCOMMON, "Uncommon", "Show uncommon potions"));
        inventory.setItem(12, createPotionTierButton(ItemRarity.RARE, "Rare", "Show rare potions"));
        inventory.setItem(13, createPotionTierButton(ItemRarity.EPIC, "Epic", "Show epic potions"));
        inventory.setItem(14, createPotionTierButton(ItemRarity.LEGENDARY, "Legendary", "Show legendary potions"));
        inventory.setItem(15, createPotionButton(ChatColor.YELLOW + "All Tiers", ChatColor.GRAY + "Show every potion tier", Color.fromRGB(242, 201, 76)));
        inventory.setItem(22, createButton(Material.NETHER_STAR, ChatColor.AQUA + "Back to Categories", ChatColor.GRAY + "Return to item categories"));
        return inventory;
    }

    private Inventory createItemBrowserMenu(String categoryId, int page) {
        Inventory inventory = Bukkit.createInventory(null, 54, buildItemBrowserTitle(categoryId, page));
        List<AethoriaItemDefinition> definitions = getDefinitionsForCategory(categoryId);
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
        inventory.setItem(49, createButton(Material.NETHER_STAR, ChatColor.AQUA + "Back to Categories", ChatColor.GRAY + "Return to item categories"));
        inventory.setItem(53, createButton(Material.ARROW, ChatColor.GREEN + "Next Page", ChatColor.GRAY + "Go to the next page"));
        return inventory;
    }

    private boolean hasNextPage(String categoryId, int page) {
        return ((page + 1) * ITEMS_PER_PAGE) < getDefinitionsForCategory(categoryId).size();
    }

    private int parsePage(String title) {
        String stripped = ChatColor.stripColor(title);
        int separatorIndex = stripped.lastIndexOf('|');
        String rawPage = separatorIndex >= 0 ? stripped.substring(separatorIndex + 1).trim() : "1";
        try {
            return Math.max(0, Integer.parseInt(rawPage) - 1);
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private String parseCategoryId(String title) {
        String stripped = ChatColor.stripColor(title).replace("Items: ", "");
        int separatorIndex = stripped.lastIndexOf('|');
        String displayName = separatorIndex >= 0 ? stripped.substring(0, separatorIndex).trim() : stripped.trim();
        return switch (displayName) {
            case "Warrior Weapons" -> "warrior-weapons";
            case "Mage Weapons" -> "mage-weapons";
            case "Paladin Weapons" -> "paladin-weapons";
            case "Warrior Armor" -> "warrior-armor";
            case "Mage Armor" -> "mage-armor";
            case "Paladin Armor" -> "paladin-armor";
            case "Healing Potions" -> "healing-potions:all";
            case "Buff Potions" -> "buff-potions:all";
            case "Healing Potions [Common]" -> "healing-potions:common";
            case "Healing Potions [Uncommon]" -> "healing-potions:uncommon";
            case "Healing Potions [Rare]" -> "healing-potions:rare";
            case "Healing Potions [Epic]" -> "healing-potions:epic";
            case "Healing Potions [Legendary]" -> "healing-potions:legendary";
            case "Buff Potions [Common]" -> "buff-potions:common";
            case "Buff Potions [Uncommon]" -> "buff-potions:uncommon";
            case "Buff Potions [Rare]" -> "buff-potions:rare";
            case "Buff Potions [Epic]" -> "buff-potions:epic";
            case "Buff Potions [Legendary]" -> "buff-potions:legendary";
            default -> "all-items";
        };
    }

    private String parsePotionTierCategory(String title) {
        String stripped = ChatColor.stripColor(title).replace("Potion Tiers: ", "").trim();
        return stripped.startsWith("Healing") ? "healing-potions" : "buff-potions";
    }

    private String buildItemBrowserTitle(String categoryId, int page) {
        return ITEM_BROWSER_TITLE_PREFIX + switch (categoryId) {
            case "warrior-weapons" -> "Warrior Weapons";
            case "mage-weapons" -> "Mage Weapons";
            case "paladin-weapons" -> "Paladin Weapons";
            case "warrior-armor" -> "Warrior Armor";
            case "mage-armor" -> "Mage Armor";
            case "paladin-armor" -> "Paladin Armor";
            case "healing-potions:common" -> "Healing Potions [Common]";
            case "healing-potions:uncommon" -> "Healing Potions [Uncommon]";
            case "healing-potions:rare" -> "Healing Potions [Rare]";
            case "healing-potions:epic" -> "Healing Potions [Epic]";
            case "healing-potions:legendary" -> "Healing Potions [Legendary]";
            case "healing-potions:all" -> "Healing Potions";
            case "buff-potions:common" -> "Buff Potions [Common]";
            case "buff-potions:uncommon" -> "Buff Potions [Uncommon]";
            case "buff-potions:rare" -> "Buff Potions [Rare]";
            case "buff-potions:epic" -> "Buff Potions [Epic]";
            case "buff-potions:legendary" -> "Buff Potions [Legendary]";
            case "buff-potions:all" -> "Buff Potions";
            default -> "All Authored Items";
        } + ChatColor.DARK_GRAY + " | " + (page + 1);
    }

    private String buildPotionTierFilterTitle(String categoryId) {
        return POTION_TIER_FILTER_TITLE_PREFIX + ("healing-potions".equals(categoryId) ? "Healing Potions" : "Buff Potions");
    }

    private List<AethoriaItemDefinition> getDefinitionsForCategory(String categoryId) {
        return new ArrayList<>(plugin.getItemRegistryService().getDefinitions()).stream()
            .filter(definition -> matchesCategory(definition, categoryId))
            .sorted(Comparator.comparingInt(AethoriaItemDefinition::levelRequirement).thenComparing(AethoriaItemDefinition::id))
            .toList();
    }

    private boolean matchesCategory(AethoriaItemDefinition definition, String categoryId) {
        return switch (categoryId) {
            case "warrior-weapons" -> definition.type() == com.aethoria.core.item.ItemType.WEAPON && hasRequiredClass(definition, "WARRIOR");
            case "mage-weapons" -> definition.type() == com.aethoria.core.item.ItemType.WEAPON && hasRequiredClass(definition, "MAGE");
            case "paladin-weapons" -> definition.type() == com.aethoria.core.item.ItemType.WEAPON && hasRequiredClass(definition, "PALADIN");
            case "warrior-armor" -> definition.type().isArmor() && hasRequiredClass(definition, "WARRIOR");
            case "mage-armor" -> definition.type().isArmor() && hasRequiredClass(definition, "MAGE");
            case "paladin-armor" -> definition.type().isArmor() && hasRequiredClass(definition, "PALADIN");
            case "healing-potions:common" -> isPotionOfTier(definition, true, ItemRarity.COMMON);
            case "healing-potions:uncommon" -> isPotionOfTier(definition, true, ItemRarity.UNCOMMON);
            case "healing-potions:rare" -> isPotionOfTier(definition, true, ItemRarity.RARE);
            case "healing-potions:epic" -> isPotionOfTier(definition, true, ItemRarity.EPIC);
            case "healing-potions:legendary" -> isPotionOfTier(definition, true, ItemRarity.LEGENDARY);
            case "healing-potions:all", "healing-potions" -> isPotion(definition, true);
            case "buff-potions:common" -> isPotionOfTier(definition, false, ItemRarity.COMMON);
            case "buff-potions:uncommon" -> isPotionOfTier(definition, false, ItemRarity.UNCOMMON);
            case "buff-potions:rare" -> isPotionOfTier(definition, false, ItemRarity.RARE);
            case "buff-potions:epic" -> isPotionOfTier(definition, false, ItemRarity.EPIC);
            case "buff-potions:legendary" -> isPotionOfTier(definition, false, ItemRarity.LEGENDARY);
            case "buff-potions:all", "buff-potions" -> isPotion(definition, false);
            default -> true;
        };
    }

    private boolean isPotion(AethoriaItemDefinition definition, boolean healing) {
        return definition.type() == com.aethoria.core.item.ItemType.CONSUMABLE
            && definition.hasConsumableData()
            && (healing == "HEAL".equals(definition.consumableData().effectId()));
    }

    private boolean isPotionOfTier(AethoriaItemDefinition definition, boolean healing, ItemRarity rarity) {
        return isPotion(definition, healing) && definition.rarity() == rarity;
    }

    private boolean hasRequiredClass(AethoriaItemDefinition definition, String classId) {
        return definition.hasClassRestriction() && definition.requiredClass().equals(classId.toUpperCase(Locale.ROOT));
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

    private ItemStack createPotionTierButton(ItemRarity rarity, String name, String loreLine) {
        Color color = switch (rarity) {
            case COMMON -> Color.fromRGB(240, 240, 240);
            case UNCOMMON -> Color.fromRGB(92, 201, 104);
            case RARE -> Color.fromRGB(74, 196, 255);
            case EPIC -> Color.fromRGB(196, 110, 255);
            case LEGENDARY -> Color.fromRGB(255, 184, 77);
        };
        return createPotionButton(rarity.getColor() + name, ChatColor.GRAY + loreLine, color);
    }

    private ItemStack createPotionButton(String name, String loreLine, Color color) {
        ItemStack itemStack = new ItemStack(Material.POTION);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof PotionMeta potionMeta) {
            potionMeta.setDisplayName(name);
            potionMeta.setLore(List.of(loreLine));
            potionMeta.setColor(color);
            potionMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ATTRIBUTES);
            itemStack.setItemMeta(potionMeta);
            return itemStack;
        }

        return createButton(Material.POTION, name, loreLine);
    }

    private boolean canUseAdminMenu(Player player) {
        return player.isOp() || player.hasPermission("aethoria.admin");
    }
}
