# Aethoria Core Hooks

This file documents reusable integration hooks exposed by `aethoria-core` for other plugins.

Core should own shared communication and lightweight shared services.
Feature plugins should use these hooks instead of duplicating Core-owned logic.

---

## Authored Item Lookup Hook

**Owner:** `aethoria-core`  
**Type:** Service  
**Class:** `com.aethoria.core.api.CoreAuthoredItemLookupService`

### Purpose

Provides a stable way for other plugins to:

- look up authored item definitions by item id
- resolve authored item identity from an `ItemStack`
- check whether an item is an authored Aethoria item

### What it does

- `findByItemId(String itemId)` → returns authored item definition view
- `resolve(ItemStack itemStack)` → returns authored item definition view for a live item
- `resolveItemId(ItemStack itemStack)` → returns authored item id only
- `isAuthoredItem(ItemStack itemStack)` → quick authored item check
- `getAllDefinitions()` / `getAllItemIds()` → registry access for integrations

### What it should NOT do

- full item generation logic for feature plugins
- market logic
- dungeon reward logic
- quest logic

### Intended usage

Other plugins such as:

- Market
- Dungeons
- Quests
- Professions

should use this hook whenever they need to understand authored item identity or inspect authored item metadata.

### Notes

This hook returns a lightweight API view (`AuthoredItemView`) instead of exposing internal registry implementation directly.

---

## Reward Hook

**Owner:** `aethoria-core`  
**Type:** Service  
**Class:** `com.aethoria.core.service.PlayerRewardService`

### Purpose

Provides a reusable reward path to grant:

- authored items
- adventurer XP

through one shared Core flow.

### Intended usage

Future plugins like Quests, Tutorials, and NPC systems should build reward bundles and let Core apply them.

---

## Rule for future hooks

When a new hook is added:

1. keep it lightweight
2. keep feature logic out of Core
3. document it here
4. explain what other plugins should use it for
