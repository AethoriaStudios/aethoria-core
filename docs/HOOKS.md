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

## Player Identity Hook

**Owner:** `aethoria-core`  
**Type:** Service  
**Class:** `com.aethoria.core.api.CorePlayerIdentityService`

### Purpose

Provides a stable way for other plugins to resolve players and lightweight player identity data.

### What it does

- find online players by exact name
- read lightweight identity data by UUID
- convert online players into reusable identity views

### What it should NOT do

- replace Bukkit player APIs entirely
- own command parsing logic
- own social systems or friend systems

### Intended usage

Other plugins should use this when they need a small shared player-resolution helper instead of re-implementing the same Core-side logic repeatedly.

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

## Player Progression Lookup Hook

**Owner:** `aethoria-core`  
**Type:** Service  
**Class:** `com.aethoria.core.api.CorePlayerProgressionLookupService`

### Purpose

Provides a stable way for other plugins to read shared player progression state.

### What it does

- read active class
- read adventurer level
- read current adventurer XP
- read XP required to next level
- read max level
- get a bundled progression view for one player

### What it should NOT do

- own quest progression logic
- own dungeon unlock logic
- own skill trees
- own class combat systems

### Intended usage

Other plugins such as:

- Market
- Dungeons
- Quests
- Tutorials

should use this hook when they need to read player progression data without reaching into Core internals directly.

### Notes

This hook is intentionally read-focused.

---

## Currency Hook

**Owner:** `aethoria-core`  
**Type:** Service  
**Class:** `com.aethoria.core.api.CoreCurrencyService`

### Purpose

Provides a stable way for other plugins to read shared currency balances and perform simple Core-owned currency mutations.

### What it does

- read primary currency balance
- read Dungeon Coins balance
- read both balances together in a snapshot view
- perform simple deposit / withdraw / set operations
- return structured mutation results with updated balance snapshots

### What it should NOT do

- own market pricing rules
- own taxes or auction logic
- own dungeon reward balancing rules
- own advanced economy systems outside Core currencies

### Intended usage

Other plugins such as:

- Market
- Dungeons
- Quests
- Shops

should use this hook whenever they need Core-owned balance reads or simple balance mutations without depending directly on internal currency service implementation.

### Notes

This hook only covers currencies that Core already owns.

---

## Class Change Event Hook

**Owner:** `aethoria-core`  
**Type:** Event  
**Class:** `com.aethoria.core.event.PlayerClassChangeEvent`

### Purpose

Fires when a player's active class changes through Core.

### Intended usage

Other plugins can listen to react to class changes without polling Core repeatedly.

---

## Level-Up Event Hook

**Owner:** `aethoria-core`  
**Type:** Event  
**Class:** `com.aethoria.core.event.PlayerLevelUpEvent`

### Purpose

Fires when a player gains one or more adventurer levels through the Core progression service.

### Intended usage

Other plugins can listen for unlocks, tutorials, rewards, or progression milestones.

---

## Authored Item Reload Event Hook

**Owner:** `aethoria-core`  
**Type:** Event  
**Class:** `com.aethoria.core.event.AuthoredItemsReloadEvent`

### Purpose

Fires after authored item reload completes successfully.

### Intended usage

Other plugins can refresh caches or invalidate item-related state after Core reloads item definitions.

---

## Core Service Registry Hook

**Owner:** `aethoria-core`  
**Type:** Service Registry  
**Class:** `com.aethoria.core.api.CoreServices`

### Purpose

Provides a stable access pattern for dependent plugins to retrieve Core-owned APIs from one place.

### What it contains

- player identity helper API
- authored item lookup API
- player progression lookup API
- currency API

### Intended usage

Dependent plugins can ask Core for `getCoreServices()` and then access the APIs they need without reaching into implementation services directly.

---

## Rule for future hooks

When a new hook is added:

1. keep it lightweight
2. keep feature logic out of Core
3. document it here
4. explain what other plugins should use it for
