# inventory

![Maven Central Version](https://img.shields.io/maven-central/v/io.typst/inventory-core)

A lightweight, generalized library for representing inventories in any game and running
operations on top of that data.

You can represent inventory data using types such as:

- `Map<Int, MyGameItem>`
- `List<MyGameItem>`
- `MyGameInventory`

and perform operations like: give, take, has, ...

---

## Import

### for Minecraft Bukkit API

Gradle:

```groovy
dependencies {
    // Java
    implementation("io.typst:inventory-bukkit:$THE_LATEST")
    // Kotlin
    implementation("io.typst:inventory-bukkit-kotlin:$THE_LATEST")
}
````

Maven:

```xml
<dependencies>
    <dependency>
        <groupId>io.typst</groupId>
        <artifactId>inventory-bukkit</artifactId>
        <version>$THE_LATEST</version>
    </dependency>
</dependencies>
```

### Core for custom engines

```groovy
dependencies {
    implementation("io.typst:inventory-core:$THE_LATEST")
}
```

---

## Motivation

### Use Cases Beyond the Minecraft Bukkit API

Prerequisite: `io.typst:inventory-bukkit` dependency.

**Example 1: Put an item only into a specific slot (3), drop the rest items on the ground**

```java
// Using inventory-bukkit (Java)
BukkitInventories.from(inventory)
        .withSubInventory(3) // can select a specific slot range
        .giveItemOrDrop(player, item);
```

```kotlin
// Using inventory-bukkit (Kotlin)
inventory.toMutator()
    .withSubInventory(3) // can select a specific slot range
    .giveItemOrDrop(player, item)
```

**Example 2: Only insert if there is enough space**

```java
if (BukkitInventories.from(inventory).giveItems(item)) {
    // Success: inventory updated
} else {
    // Failed: not enough space, inventory unchanged
}
```

**Example 3: Inventory represented as List or Map**

```java
// map: Map<Int, ItemStack>
BukkitInventories.from(map).giveItemOrDrop(player, item);

if (BukkitInventories.from(map).takeItems(items)) {
    // success
}
```

**Example 4: Atomic operation -- take and give**

```java
// inventory: Inventory
// inputItem: ItemStack
// outputItem: ItemStack
// inputSlots: List<Int>
// outputSlot: Int
var transaction = BukkitInventories.transactionFrom(inv)
  .updated(inv -> inv.takeItems(inputItem))
  .updated(inv -> inv.giveItems(outputItem));
if (transaction.isSuccess()) {
  transaction.getPatch().getModifiedItems().forEach(inv::set);
  // some another operations...
}
```

---

## Bukkit API implementation

### Java

* `BukkitInventories`
* `BukkitItemStacks`

### Kotlin

* `InventoryExtensions`
* `ItemStackExtensions`

---

## Concepts

`inventory` is built around three main roles:

1. **Item abstraction (`ItemStackOps<A>`)**
   Defines how to represent “empty”, compare, copy, and create items of type `A`.

2. **Inventory view (`InventoryAdapter<A>`)**
   Adapts any storage structure into a `slot -> item` view.

3. **Pure operations (`InventorySnapshotView<A>`)**
   Computes results such as modified slots and remaining quantities **without**
   directly mutating the underlying inventory.

By composing these three pieces, you can implement inventory logic not only
for Minecraft inventories, but for any game.

---

## Flow

* Wrap your inventory data with `InventoryAdapter<A>`.
* Implement `ItemStackOps<A>` for your item type.
* Create an `InventorySnapshotView<A>` snapshot.
* Call operations like `give` / `take` / `has`.
* Apply the resulting `modifiedItems` back to your game’s inventory state.

---

## Components

### `InventoryMutator<A>`

Applies pure operation results to the actual game inventory.

* Reads results from `InventorySnapshotView`:

  * Writes changes to slots
  * Handles side effects such as dropping leftover items near an entity
* Provides IO helpers for common `give` / `take` patterns

This cleanly separates calculation from effects, e.g.:

* `giveItemOrDrop(Entity, Item)`
* `takeItems(Item...): Boolean`

### `InventorySnapshotView<A>`

A pure operation layer that does **not** directly modify the real inventory:

* `giveItems(A): InventoryPatch<A>`
* `takeItems(A...): InventoryPatch<A>`
* `hasItems(A): Boolean`
* `countItems(ItemKey)`
* `findSpaces(A): Map<Int, Int>`
* `findSlots(A): Map<Int, Int>`
* ...

### `InventoryPatch<A>`

Explicit representations of operation results.

* **InventoryPatch**
  * `modifiedItems`: slots to overwrite in the game inventory
  * `patch`: failure result
    * `giveLeftoverItems`: any part of the item that could not be inserted
    * `takeRemainingItems`: any part of the item that could not be taken
### `InventoryAdapter<A>`

Generalized interface for slot-based access, with implementations such as:

* `ListInventoryAdapter<A>`: list-based inventories
* `MapInventoryAdapter<A>`: map-based inventories
* `BukkitInventoryAdapter<A>`: Bukkit `Inventory`
* `SubInventoryAdapter<A>`: a sliced view of an `InventoryAdapter<A>` over specific slots

### `ItemStackOps<A>`

Common operations for an item type:

* `empty(): A`: representation of an empty item
* `isEmpty(A): Boolean`: whether the item is empty
* `isSimilar(A, A): Boolean`: whether two items are similar/stackable
* `getAmount(A): Int` / `setAmount(A, Int)`: read/write item amount
* `getMaxStackSize(A): Int`: maximum stack size
* `copy(A): A`: copy an item
