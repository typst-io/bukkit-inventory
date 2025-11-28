# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a lightweight, generalized library for representing inventories in any game and performing operations on them. The library is published to Maven Central under the `io.typst` group ID.

**Multi-module Gradle project:**
- `inventory-core`: Core library with game-agnostic inventory operations
- `inventory-bukkit`: Minecraft Bukkit API implementation (Java)
- `inventory-bukkit-kotlin`: Kotlin extensions for Bukkit

## Build Commands

```bash
# Build all modules
./gradlew build

# Run tests
./gradlew test

# Run tests for a specific module
./gradlew :inventory-core:test
./gradlew :inventory-bukkit:test

# Run a single test class
./gradlew :inventory-core:test --tests "io.typst.inventory.InventoryViewTest"

# Clean build
./gradlew clean build

# Generate Javadocs
./gradlew javadoc
```

## Architecture

### Core Concept: Separation of Calculation and Mutation

The library is built around a functional architecture that separates pure calculations from side effects:

1. **ItemStackOps<A>** - Defines operations on item type A (empty, copy, compare, get/set amount, max stack size)
2. **InventoryAdapter<A>** - Adapts any storage structure (Map, List, Bukkit Inventory) into a uniform slot-based view
3. **InventorySnapshotView<A>** - Pure, read-only operations that compute what *would* happen without mutating anything
4. **InventoryPatch<A>** - Immutable result describing changes (modifiedItems map and failure info)
5. **InventoryMutator<I,E>** - Applies patches to actual inventories and handles side effects (like dropping items)

### Key Flow

```
User data → InventoryAdapter → InventorySnapshotView → operations (give/take/has)
→ InventoryPatch → InventoryMutator → apply changes
```

### InventorySnapshotView (Pure Layer)

This is the calculation engine. Methods return `InventoryPatch` or query results without mutating:
- `giveItems(A...)` / `takeItems(A...)` → returns `InventoryPatch<A>`
- `hasItems(A)` → boolean check
- `countItems(ItemKey)` → count available items
- `findSpaces(A)` / `findSlots(A)` → returns `Map<Integer, Integer>` (slot → amount)
- `subInventory(slots...)` → returns view of subset of slots
- `toImmutable()` → creates frozen snapshot
- `updated(Map)` → returns new immutable view with changes applied

**Important:** InventorySnapshotView does NOT own a deep copy by default - it reads from the live adapter. For a frozen snapshot, use `toImmutable()` or wrap a copied map.

### InventoryPatch (Result Type)

Represents the result of operations:
- `modifiedItems`: Map<Integer, A> of slots to update
- `diff`: List of entries showing what changed
- `failure`: Contains `giveLeftoverItems` (couldn't insert) or `takeRemainingItems` (couldn't take)
- `isSuccess()`: true if no failures
- `plus(patch)`: combines patches (later patch overwrites earlier slot changes)

Patches are immutable and composable. Only apply `modifiedItems` to inventory if `isSuccess()` is true.

### InventoryMutator (Mutation Layer)

Wraps an InventoryAdapter and provides atomic operations that compute a patch first, then apply it only if successful:
- `giveItem(items...)` → boolean (true if success, inventory updated)
- `takeItems(items...)` → boolean (true if success)
- `takeItem(int count, ItemKey)` → boolean
- `giveItemOrDrop(Entity, Item)` → always succeeds, drops leftovers
- `copy()` → creates a mutable copy using MapInventoryAdapter
- `subInventory(slots...)` → returns mutator for subset of slots

All operations are atomic: either fully succeed and mutate, or fail and leave inventory unchanged.

### Adapter Implementations

- **MapInventoryAdapter<A>**: Backed by `Map<Integer, A>`
- **ListInventoryAdapter<A>**: Backed by `List<A>` (index = slot)
- **BukkitInventoryAdapter**: Wraps Bukkit `Inventory` objects
- **SubInventoryAdapter<A>**: Provides filtered view over specific slots of another adapter

## Bukkit Integration

### Java API (inventory-bukkit)
- `BukkitInventories.from(Inventory)` → `InventoryMutator<ItemStack, Player>`
- `BukkitInventories.from(Map<Integer, ItemStack>)` → mutator for map-based inventory
- `BukkitInventories.from(List<ItemStack>)` → mutator for list-based inventory
- `BukkitItemStacks` - utility class for ItemStack operations
- `BukkitItemStackOps.INSTANCE` - implementation of ItemStackOps for Bukkit
- `BukkitPlayerOps.INSTANCE` - implementation of EntityOps for dropping items

### Kotlin API (inventory-bukkit-kotlin)
Extension functions in:
- `InventoryExtension.kt` - extensions on Bukkit Inventory
- `ItemStackExtension.kt` - extensions on Bukkit ItemStack

Kotlin extensions provide the same functionality as `BukkitInventories` but with a more idiomatic Kotlin style (e.g., `inventory.giveItemOrDrop(player, item)`)

## Testing

The project uses JUnit 5. Tests are in `core/src/test/java/io/typst/inventory/`.

Mock implementations for testing:
- `MockItem` - simple test item class
- `MockItemOps` - ItemStackOps implementation for MockItem

Tests demonstrate:
- Pure operation semantics (no side effects)
- Patch composition
- Atomic operations
- Immutable snapshots

## Module Dependencies

```
inventory-bukkit-kotlin
    └── inventory-bukkit
            └── inventory-core
```

- `inventory-core` has no game dependencies (only Lombok, JetBrains annotations, JUnit 5)
- `inventory-bukkit` depends on Spigot API 1.16.5 (compileOnly)
- `inventory-bukkit-kotlin` uses Kotlin 2.2.21 and depends on inventory-bukkit

## Java Version

All modules target Java 11 (toolchain configured in build.gradle).
