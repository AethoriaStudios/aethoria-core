package com.aethoria.core.command;

import com.aethoria.core.AethoriaCorePlugin;
import com.aethoria.core.item.AethoriaItemDefinition;
import com.aethoria.core.item.ItemStats;
import com.aethoria.core.service.ClassSwapService;
import com.aethoria.core.service.CurrencyService;
import com.aethoria.core.service.DungeonService;
import com.aethoria.core.service.ProgressionService;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class AethoriaCommand implements CommandExecutor, TabCompleter {
    private final AethoriaCorePlugin plugin;

    public AethoriaCommand(AethoriaCorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        return switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> handleReload(sender);
            case "status" -> handleStatus(sender, args);
            case "aethor" -> handleAethor(sender, args);
            case "dungeoncoins" -> handleDungeonCoins(sender, args);
            case "class" -> handleClass(sender, args);
            case "dailybonus" -> handleDailyBonus(sender, args);
            case "level" -> handleLevel(sender, args);
            case "item" -> handleItem(sender, args);
            default -> {
                sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
                sendHelp(sender);
                yield true;
            }
        };
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(List.of("reload", "status", "aethor", "dungeoncoins", "class", "dailybonus", "level", "item"), args[0]);
        }

        String subcommand = args[0].toLowerCase(Locale.ROOT);
        return switch (subcommand) {
            case "aethor" -> completeCurrencyCommand(args, List.of("get", "set", "add", "remove"));
            case "dungeoncoins" -> completeCurrencyCommand(args, List.of("get", "set", "add", "remove"));
            case "class" -> completeClassCommand(args);
            case "level" -> completeLevelCommand(args);
            case "item" -> completeItemCommand(args);
            case "status", "dailybonus" -> args.length == 2 ? filter(getOnlinePlayerNames(), args[1]) : List.of();
            default -> List.of();
        };
    }

    private boolean handleReload(CommandSender sender) {
        boolean reloaded = plugin.reloadAethoria();
        if (reloaded) {
            sender.sendMessage(ChatColor.GREEN + "Aethoria Core reloaded.");
        } else {
            sender.sendMessage(ChatColor.RED + "Aethoria Core reload failed. Check the console.");
        }
        return true;
    }

    private boolean handleStatus(CommandSender sender, String[] args) {
        Player target = resolvePlayer(sender, args, 1, true);
        if (target == null) {
            return true;
        }

        CurrencyService currencyService = plugin.getCurrencyService();
        DungeonService dungeonService = plugin.getDungeonService();
        ClassSwapService classSwapService = plugin.getClassSwapService();

        sender.sendMessage(ChatColor.GOLD + "Aethoria Status: " + ChatColor.YELLOW + target.getName());
        sender.sendMessage(ChatColor.GRAY + "Class: " + ChatColor.WHITE + classSwapService.getActiveClass(target.getUniqueId()));
        sender.sendMessage(ChatColor.GRAY + "Adventurer Level: " + ChatColor.WHITE + plugin.getProgressionService().getLevel(target.getUniqueId()));
        sender.sendMessage(ChatColor.GRAY + currencyService.getPrimaryCurrencyName() + ": " + ChatColor.WHITE + formatDecimal(currencyService.getAethor(target.getUniqueId())));
        sender.sendMessage(ChatColor.GRAY + "Dungeon Coins: " + ChatColor.WHITE + currencyService.getDungeonCoins(target.getUniqueId()));
        sender.sendMessage(ChatColor.GRAY + "Daily Bonus: " + (dungeonService.canClaimDailyBonus(target.getUniqueId()) ? ChatColor.GREEN + "Available" : ChatColor.RED + "Already claimed"));
        sender.sendMessage(ChatColor.GRAY + "Equipped Stats: " + ChatColor.WHITE + formatStats(plugin.getGameplayStatService().getStats(target.getUniqueId())));
        AttributeInstance maxHealthAttribute = target.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHealthAttribute != null) {
            sender.sendMessage(ChatColor.GRAY + "Max Health: " + ChatColor.WHITE + formatDecimal(maxHealthAttribute.getBaseValue()));
        }
        return true;
    }

    private boolean handleAethor(CommandSender sender, String[] args) {
        return handleCurrency(sender, args, true);
    }

    private boolean handleDungeonCoins(CommandSender sender, String[] args) {
        return handleCurrency(sender, args, false);
    }

    private boolean handleCurrency(CommandSender sender, String[] args, boolean primaryCurrency) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /aethoria " + args[0] + " <get|set|add|remove> <player> [amount]");
            return true;
        }

        String action = args[1].toLowerCase(Locale.ROOT);
        Player target = resolvePlayer(sender, args, 2, false);
        if (target == null) {
            return true;
        }

        CurrencyService currencyService = plugin.getCurrencyService();
        String currencyName = primaryCurrency ? currencyService.getPrimaryCurrencyName() : "Dungeon Coins";

        if (action.equals("get")) {
            String value = primaryCurrency
                ? formatDecimal(currencyService.getAethor(target.getUniqueId()))
                : Integer.toString(currencyService.getDungeonCoins(target.getUniqueId()));
            sender.sendMessage(ChatColor.GOLD + target.getName() + ChatColor.GRAY + " has " + ChatColor.WHITE + value + ChatColor.GRAY + " " + currencyName + '.');
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /aethoria " + args[0] + " <set|add|remove> <player> <amount>");
            return true;
        }

        if (primaryCurrency) {
            Double amount = parseDouble(sender, args[3]);
            if (amount == null) {
                return true;
            }
            if (amount < 0.0D) {
                sender.sendMessage(ChatColor.RED + "Amount must be non-negative.");
                return true;
            }

            switch (action) {
                case "set" -> currencyService.setAethor(target.getUniqueId(), amount);
                case "add" -> currencyService.depositAethor(target.getUniqueId(), amount);
                case "remove" -> {
                    if (!currencyService.withdrawAethor(target.getUniqueId(), amount)) {
                        sender.sendMessage(ChatColor.RED + "Player does not have enough " + currencyName + '.');
                        return true;
                    }
                }
                default -> {
                    sender.sendMessage(ChatColor.RED + "Unknown currency action.");
                    return true;
                }
            }

            sender.sendMessage(ChatColor.GREEN + "Updated " + currencyName + " for " + target.getName() + ". New balance: " + formatDecimal(currencyService.getAethor(target.getUniqueId())));
            return true;
        }

        Integer amount = parseInteger(sender, args[3]);
        if (amount == null) {
            return true;
        }
        if (amount < 0) {
            sender.sendMessage(ChatColor.RED + "Amount must be non-negative.");
            return true;
        }

        switch (action) {
            case "set" -> currencyService.setDungeonCoins(target.getUniqueId(), amount);
            case "add" -> currencyService.depositDungeonCoins(target.getUniqueId(), amount);
            case "remove" -> {
                if (!currencyService.withdrawDungeonCoins(target.getUniqueId(), amount)) {
                    sender.sendMessage(ChatColor.RED + "Player does not have enough " + currencyName + '.');
                    return true;
                }
            }
            default -> {
                sender.sendMessage(ChatColor.RED + "Unknown currency action.");
                return true;
            }
        }

        sender.sendMessage(ChatColor.GREEN + "Updated " + currencyName + " for " + target.getName() + ". New balance: " + currencyService.getDungeonCoins(target.getUniqueId()));
        return true;
    }

    private boolean handleClass(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /aethoria class <get|set|swap> <player> [class]");
            return true;
        }

        String action = args[1].toLowerCase(Locale.ROOT);
        Player target = resolvePlayer(sender, args, 2, false);
        if (target == null) {
            return true;
        }

        ClassSwapService classSwapService = plugin.getClassSwapService();
        if (action.equals("get")) {
            sender.sendMessage(ChatColor.GOLD + target.getName() + ChatColor.GRAY + " is using class " + ChatColor.WHITE + classSwapService.getActiveClass(target.getUniqueId()) + ChatColor.GRAY + '.');
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /aethoria class <set|swap> <player> <class>");
            return true;
        }

        String classId = classSwapService.normalizeClassId(args[3]);
        if (!classSwapService.isSupportedClass(classId)) {
            sender.sendMessage(ChatColor.RED + "Unsupported class. Available: " + String.join(", ", classSwapService.getSupportedClasses()));
            return true;
        }

        switch (action) {
            case "set" -> {
                classSwapService.setActiveClass(target.getUniqueId(), classId);
                plugin.getGameplayStatService().refreshLater(target);
                sender.sendMessage(ChatColor.GREEN + "Set active class for " + target.getName() + " to " + classId + '.');
            }
            case "swap" -> {
                double cost = classSwapService.getConfiguredSwapCost();
                if (!classSwapService.swapClass(target.getUniqueId(), classId, cost)) {
                    sender.sendMessage(ChatColor.RED + "Swap failed. The player may not have enough " + plugin.getCurrencyService().getPrimaryCurrencyName() + '.');
                    return true;
                }
                plugin.getGameplayStatService().refreshLater(target);
                sender.sendMessage(ChatColor.GREEN + "Swapped class for " + target.getName() + " to " + classId + " for " + formatDecimal(cost) + ' ' + plugin.getCurrencyService().getPrimaryCurrencyName() + '.');
            }
            default -> {
                sender.sendMessage(ChatColor.RED + "Unknown class action.");
                return true;
            }
        }

        return true;
    }

    private boolean handleDailyBonus(CommandSender sender, String[] args) {
        Player target = resolvePlayer(sender, args, 1, false);
        if (target == null) {
            return true;
        }

        DungeonService dungeonService = plugin.getDungeonService();
        int configuredBonus = dungeonService.getConfiguredDailyBonus();
        if (!dungeonService.claimDailyBonus(target.getUniqueId(), configuredBonus)) {
            sender.sendMessage(ChatColor.RED + "Daily dungeon bonus already claimed for " + target.getName() + '.');
            return true;
        }

        sender.sendMessage(ChatColor.GREEN + "Granted " + configuredBonus + " Dungeon Coins to " + target.getName() + '.');
        return true;
    }

    private boolean handleLevel(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /aethoria level <get|set|add|setxp> <player> [amount]");
            return true;
        }

        String action = args[1].toLowerCase(Locale.ROOT);
        Player target = resolvePlayer(sender, args, 2, false);
        if (target == null) {
            return true;
        }

        ProgressionService progressionService = plugin.getProgressionService();
        if (action.equals("get")) {
            sender.sendMessage(ChatColor.GOLD + target.getName() + ChatColor.GRAY + " is level " + ChatColor.WHITE + progressionService.getLevel(target.getUniqueId()) + ChatColor.GRAY + " with " + ChatColor.WHITE + progressionService.getExperience(target.getUniqueId()) + ChatColor.GRAY + " XP.");
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /aethoria level <set|add|setxp> <player> <amount>");
            return true;
        }

        Integer amount = parseInteger(sender, args[3]);
        if (amount == null) {
            return true;
        }

        switch (action) {
            case "set" -> progressionService.setLevel(target.getUniqueId(), amount);
            case "add" -> progressionService.addLevels(target.getUniqueId(), amount);
            case "setxp" -> progressionService.setExperience(target.getUniqueId(), amount);
            default -> {
                sender.sendMessage(ChatColor.RED + "Unknown level action.");
                return true;
            }
        }

        plugin.getGameplayStatService().refreshLater(target);

        sender.sendMessage(ChatColor.GREEN + "Updated progression for " + target.getName() + ": level " + progressionService.getLevel(target.getUniqueId()) + ", XP " + progressionService.getExperience(target.getUniqueId()) + '.');
        return true;
    }

    private boolean handleItem(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /aethoria item <list|give|inspect>");
            return true;
        }

        return switch (args[1].toLowerCase(Locale.ROOT)) {
            case "list" -> handleItemList(sender);
            case "give" -> handleItemGive(sender, args);
            case "inspect" -> handleItemInspect(sender, args);
            default -> {
                sender.sendMessage(ChatColor.RED + "Unknown item action.");
                yield true;
            }
        };
    }

    private boolean handleItemList(CommandSender sender) {
        List<AethoriaItemDefinition> definitions = new ArrayList<>(plugin.getItemRegistryService().getDefinitions());
        if (definitions.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No item definitions are loaded.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "Loaded Aethoria Items:");
        for (AethoriaItemDefinition definition : definitions) {
            sender.sendMessage(ChatColor.YELLOW + "- " + definition.id() + ChatColor.GRAY + " (" + definition.rarity().getDisplayName() + ", " + definition.type().name() + ")");
        }
        return true;
    }

    private boolean handleItemGive(CommandSender sender, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "Usage: /aethoria item give <player> <itemId> [amount]");
            return true;
        }

        Player target = resolvePlayer(sender, args, 2, false);
        if (target == null) {
            return true;
        }

        String itemId = args[3];
        int amount = 1;
        if (args.length >= 5) {
            Integer parsedAmount = parseInteger(sender, args[4]);
            if (parsedAmount == null || parsedAmount <= 0) {
                sender.sendMessage(ChatColor.RED + "Amount must be a positive integer.");
                return true;
            }
            amount = parsedAmount;
        }

        ItemStack itemStack = plugin.getItemFactory().createItem(itemId, amount).orElse(null);
        if (itemStack == null) {
            sender.sendMessage(ChatColor.RED + "Unknown item id: " + itemId);
            return true;
        }

        target.getInventory().addItem(itemStack).values().forEach(leftover -> target.getWorld().dropItemNaturally(target.getLocation(), leftover));
        sender.sendMessage(ChatColor.GREEN + "Granted " + amount + "x " + itemId + " to " + target.getName() + '.');
        return true;
    }

    private boolean handleItemInspect(CommandSender sender, String[] args) {
        Player target = resolvePlayer(sender, args, 2, true);
        if (target == null) {
            return true;
        }

        ItemStack heldItem = target.getInventory().getItemInMainHand();
        if (heldItem == null || heldItem.getType().isAir()) {
            sender.sendMessage(ChatColor.RED + "That player is not holding an item.");
            return true;
        }

        AethoriaItemDefinition definition = plugin.getItemFactory().getDefinition(heldItem).orElse(null);
        if (definition == null) {
            sender.sendMessage(ChatColor.RED + "Held item is not an authored Aethoria item.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "Item Inspect: " + ChatColor.YELLOW + definition.id());
        sender.sendMessage(ChatColor.GRAY + "Name: " + ChatColor.WHITE + ChatColor.stripColor(heldItem.getItemMeta() == null ? definition.displayName() : heldItem.getItemMeta().getDisplayName()));
        sender.sendMessage(ChatColor.GRAY + "Rarity: " + definition.rarity().getColor() + definition.rarity().getDisplayName());
        sender.sendMessage(ChatColor.GRAY + "Type: " + ChatColor.WHITE + definition.type().name());
        sender.sendMessage(ChatColor.GRAY + "Required Class: " + ChatColor.WHITE + (definition.hasClassRestriction() ? definition.requiredClass() : "None"));
        sender.sendMessage(ChatColor.GRAY + "Level Requirement: " + ChatColor.WHITE + definition.levelRequirement());
        sender.sendMessage(ChatColor.GRAY + "Item Stats: " + ChatColor.WHITE + formatStats(definition.stats()));
        return true;
    }

    private Player resolvePlayer(CommandSender sender, String[] args, int index, boolean allowSelf) {
        if (args.length <= index) {
            if (allowSelf && sender instanceof Player player) {
                return player;
            }
            sender.sendMessage(ChatColor.RED + "You must provide a player name.");
            return null;
        }

        Player target = Bukkit.getPlayerExact(args[index]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found or not online: " + args[index]);
            return null;
        }
        return target;
    }

    private Double parseDouble(CommandSender sender, String rawValue) {
        try {
            return Double.parseDouble(rawValue);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatColor.RED + "Invalid number: " + rawValue);
            return null;
        }
    }

    private Integer parseInteger(CommandSender sender, String rawValue) {
        try {
            return Integer.parseInt(rawValue);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatColor.RED + "Invalid integer: " + rawValue);
            return null;
        }
    }

    private String formatDecimal(double value) {
        return String.format(Locale.US, "%.2f", value);
    }

    private String formatStats(ItemStats stats) {
        if (stats == null || stats.isEmpty()) {
            return "None";
        }

        return stats.asDisplayMap().entrySet().stream()
            .map(this::formatStatEntry)
            .collect(Collectors.joining(", "));
    }

    private String formatStatEntry(Map.Entry<String, Double> entry) {
        return entry.getKey() + " +" + formatDecimal(entry.getValue());
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Aethoria Core " + ChatColor.GRAY + "v" + plugin.getPluginMeta().getVersion());
        sender.sendMessage(ChatColor.YELLOW + "/aethoria reload" + ChatColor.GRAY + " - Reload plugin configuration");
        sender.sendMessage(ChatColor.YELLOW + "/aethoria status [player]" + ChatColor.GRAY + " - Show current class and balances");
        sender.sendMessage(ChatColor.YELLOW + "/aethoria aethor <get|set|add|remove> <player> [amount]");
        sender.sendMessage(ChatColor.YELLOW + "/aethoria dungeoncoins <get|set|add|remove> <player> [amount]");
        sender.sendMessage(ChatColor.YELLOW + "/aethoria class <get|set|swap> <player> [class]");
        sender.sendMessage(ChatColor.YELLOW + "/aethoria dailybonus <player>" + ChatColor.GRAY + " - Grant the configured daily dungeon bonus");
        sender.sendMessage(ChatColor.YELLOW + "/aethoria level <get|set|add|setxp> <player> [amount]" + ChatColor.GRAY + " - Manage adventurer progression");
        sender.sendMessage(ChatColor.YELLOW + "/aethoria item <list|give|inspect> ..." + ChatColor.GRAY + " - Manage authored Aethoria items");
    }

    private List<String> completeCurrencyCommand(String[] args, List<String> actions) {
        if (args.length == 2) {
            return filter(actions, args[1]);
        }
        if (args.length == 3) {
            return filter(getOnlinePlayerNames(), args[2]);
        }
        return List.of();
    }

    private List<String> completeClassCommand(String[] args) {
        if (args.length == 2) {
            return filter(List.of("get", "set", "swap"), args[1]);
        }
        if (args.length == 3) {
            return filter(getOnlinePlayerNames(), args[2]);
        }
        if (args.length == 4) {
            return filter(plugin.getClassSwapService().getSupportedClasses(), args[3]);
        }
        return List.of();
    }

    private List<String> completeItemCommand(String[] args) {
        if (args.length == 2) {
            return filter(List.of("list", "give", "inspect"), args[1]);
        }
        if (args.length == 3 && args[1].equalsIgnoreCase("give")) {
            return filter(getOnlinePlayerNames(), args[2]);
        }
        if (args.length == 3 && args[1].equalsIgnoreCase("inspect")) {
            return filter(getOnlinePlayerNames(), args[2]);
        }
        if (args.length == 4 && args[1].equalsIgnoreCase("give")) {
            List<String> itemIds = plugin.getItemRegistryService().getDefinitions().stream().map(AethoriaItemDefinition::id).toList();
            return filter(itemIds, args[3]);
        }
        return List.of();
    }

    private List<String> completeLevelCommand(String[] args) {
        if (args.length == 2) {
            return filter(List.of("get", "set", "add", "setxp"), args[1]);
        }
        if (args.length == 3) {
            return filter(getOnlinePlayerNames(), args[2]);
        }
        return List.of();
    }

    private List<String> getOnlinePlayerNames() {
        List<String> names = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            names.add(player.getName());
        }
        return names;
    }

    private List<String> filter(List<String> values, String input) {
        String normalizedInput = input.toLowerCase(Locale.ROOT);
        return values.stream()
            .filter(value -> value.toLowerCase(Locale.ROOT).startsWith(normalizedInput))
            .collect(Collectors.toList());
    }
}
