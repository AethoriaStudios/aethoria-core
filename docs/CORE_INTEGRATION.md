# Aethoria Core Integration Guide

This document explains what `aethoria-core` is responsible for, what other plugins should request from Core, and what should remain owned by feature plugins.

---

## Role of Core

`aethoria-core` is the shared communication layer of the Aethoria plugin ecosystem.

Core should own:

- shared player identity access
- authored item identity and metadata access
- shared player progression reads
- shared currency reads and simple mutations for Core-owned currencies
- lightweight cross-plugin events
- reusable shared reward flow

Core should **not** own the full feature logic for:

- market listing systems
- quests and quest state machines
- dungeon orchestration
- profession/crafting gameplay systems
- NPC behavior systems

Those plugins should call Core hooks where shared data or shared actions are needed.

---

## Shared services currently exposed by Core

Through `AethoriaCorePlugin#getCoreServices()` dependent plugins can access:

- `players()` → `CorePlayerIdentityService`
- `authoredItems()` → `CoreAuthoredItemLookupService`
- `progression()` → `CorePlayerProgressionLookupService`
- `currency()` → `CoreCurrencyService`

Additional direct service hooks also exist for:

- `getPlayerRewardService()`

---

## What other plugins should ask Core for

### Player identity

Use Core when you need:

- online player resolution by exact name
- safe identity lookups by UUID
- lightweight player identity views for integration logic

### Authored items

Use Core when you need:

- authored item id resolution from an `ItemStack`
- authored item metadata lookup by item id
- checks for whether an item belongs to the authored item system

### Progression

Use Core when you need:

- active class
- adventurer level
- adventurer XP
- XP to next level

### Currency

Use Core when you need:

- current balance reads
- simple deposit / withdraw / set operations for Core-owned currencies

### Reward execution

Use Core when you need to grant:

- authored items
- adventurer XP

through one shared reward flow.

---

## What feature plugins should still own themselves

### Market plugin should own

- listings
- listing validation rules beyond basic item identity
- taxes and market fees
- listing history
- market UI and flows

### Quest plugin should own

- quest definitions
- quest state tracking
- objective completion logic
- NPC progression and narrative rules

### Dungeon plugin should own

- dungeon lifecycle
- instance logic
- encounter sequencing
- dungeon-specific reward decisions

Core can help those plugins, but should not replace them.

---

## Event hooks currently available

Core now emits:

- `PlayerClassChangeEvent`
- `PlayerLevelUpEvent`
- `AuthoredItemsReloadEvent`

Use these when your plugin needs to react to changes instead of polling Core continuously.

---

## Integration rule of thumb

If multiple plugins need the same shared data or lightweight shared action, it probably belongs in Core.

If the logic defines the behavior of one feature domain, it probably belongs in that feature plugin.

---

## Recommended usage pattern

For future plugins:

1. get a reference to `AethoriaCorePlugin`
2. use `getCoreServices()` for shared reads and simple shared actions
3. listen to Core events where appropriate
4. keep feature-specific business logic in the feature plugin itself
