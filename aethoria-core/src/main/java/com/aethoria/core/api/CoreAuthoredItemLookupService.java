package com.aethoria.core.api;

import com.aethoria.core.item.AethoriaItemFactory;
import com.aethoria.core.item.ItemRegistryService;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.bukkit.inventory.ItemStack;

public final class CoreAuthoredItemLookupService {
    private final ItemRegistryService itemRegistryService;
    private final AethoriaItemFactory itemFactory;

    public CoreAuthoredItemLookupService(ItemRegistryService itemRegistryService, AethoriaItemFactory itemFactory) {
        this.itemRegistryService = itemRegistryService;
        this.itemFactory = itemFactory;
    }

    public Optional<AuthoredItemView> findByItemId(String itemId) {
        return itemRegistryService.getDefinition(itemId).map(AuthoredItemView::fromDefinition);
    }

    public Optional<AuthoredItemView> resolve(ItemStack itemStack) {
        return itemFactory.getDefinition(itemStack).map(AuthoredItemView::fromDefinition);
    }

    public Optional<String> resolveItemId(ItemStack itemStack) {
        return itemFactory.getItemId(itemStack);
    }

    public boolean isAuthoredItem(ItemStack itemStack) {
        return resolveItemId(itemStack).isPresent();
    }

    public Collection<AuthoredItemView> getAllDefinitions() {
        return itemRegistryService.getDefinitions().stream()
            .map(AuthoredItemView::fromDefinition)
            .toList();
    }

    public List<String> getAllItemIds() {
        return itemRegistryService.getDefinitions().stream()
            .map(definition -> definition.id())
            .toList();
    }
}
