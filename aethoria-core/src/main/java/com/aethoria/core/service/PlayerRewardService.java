package com.aethoria.core.service;

import com.aethoria.core.AethoriaCorePlugin;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class PlayerRewardService {
    private final AethoriaCorePlugin plugin;

    public PlayerRewardService(AethoriaCorePlugin plugin) {
        this.plugin = plugin;
    }

    public RewardResult grantRewards(Player player, RewardBundle rewardBundle) {
        List<String> grantedItems = new ArrayList<>();

        for (RewardItem rewardItem : rewardBundle.items()) {
            List<ItemStack> itemStacks = createRewardItems(rewardItem);
            if (itemStacks.isEmpty()) {
                return RewardResult.failure("Unknown authored item id: " + rewardItem.itemId());
            }

            for (ItemStack itemStack : itemStacks) {
                player.getInventory().addItem(itemStack).values().forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
            }
            grantedItems.add(rewardItem.itemId() + " x" + rewardItem.amount());
        }

        ProgressionService.ProgressionResult progressionResult = plugin.getProgressionService().addExperience(player.getUniqueId(), rewardBundle.adventurerXp());
        return RewardResult.success(grantedItems, rewardBundle.adventurerXp(), progressionResult.level(), progressionResult.experience(), progressionResult.levelsGained());
    }

    private List<ItemStack> createRewardItems(RewardItem rewardItem) {
        List<ItemStack> itemStacks = new ArrayList<>();
        for (int index = 0; index < rewardItem.amount(); index++) {
            ItemStack itemStack = plugin.getItemFactory().createItem(rewardItem.itemId(), 1).orElse(null);
            if (itemStack == null) {
                return List.of();
            }
            itemStacks.add(itemStack);
        }
        return itemStacks;
    }

    public record RewardBundle(List<RewardItem> items, int adventurerXp) {
        public RewardBundle {
            items = items == null ? List.of() : List.copyOf(items);
            adventurerXp = Math.max(0, adventurerXp);
        }
    }

    public record RewardItem(String itemId, int amount) {
        public RewardItem {
            itemId = itemId == null ? "" : itemId.trim();
            amount = Math.max(1, amount);
        }
    }

    public record RewardResult(boolean success, String errorMessage, List<String> grantedItems, int grantedXp, int resultingLevel, int resultingExperience, int levelsGained) {
        public static RewardResult success(List<String> grantedItems, int grantedXp, int resultingLevel, int resultingExperience, int levelsGained) {
            return new RewardResult(true, null, List.copyOf(grantedItems), grantedXp, resultingLevel, resultingExperience, levelsGained);
        }

        public static RewardResult failure(String errorMessage) {
            return new RewardResult(false, errorMessage, List.of(), 0, 0, 0, 0);
        }
    }
}
