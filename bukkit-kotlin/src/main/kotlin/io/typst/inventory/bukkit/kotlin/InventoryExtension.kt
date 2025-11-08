package io.typst.inventory.bukkit.kotlin

import io.typst.inventory.ImmutableInventory
import io.typst.inventory.InventoryAdapter
import io.typst.inventory.InventoryMutator
import io.typst.inventory.bukkit.BukkitInventories
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

// adapter
fun Inventory.toAdapter(): InventoryAdapter<ItemStack> = BukkitInventories.adapterFrom(this)

fun Map<Int, ItemStack>.toAdapter(): InventoryAdapter<ItemStack> =
    BukkitInventories.adapterFrom(this)

fun List<ItemStack>.toAdapter(): InventoryAdapter<ItemStack> = BukkitInventories.adapterFrom(this)


// immutable
fun Inventory.toImmutable(): ImmutableInventory<ItemStack> =
    BukkitInventories.immutableFrom(this)

fun Map<Int, ItemStack>.toImmutableInventory(): ImmutableInventory<ItemStack> =
    BukkitInventories.immutableFrom(this)

fun List<ItemStack>.toImmutableInventory(): ImmutableInventory<ItemStack> =
    BukkitInventories.immutableFrom(this)

// sub
fun InventoryAdapter<ItemStack>.subInventory(slots: Iterable<Int>): InventoryAdapter<ItemStack> =
    BukkitInventories.subInventory(this, slots)

// mutator
fun Inventory.toMutator(): InventoryMutator<ItemStack, Player> = BukkitInventories.from(this)

fun Map<Int, ItemStack>.toMutator(): InventoryMutator<ItemStack, Player> = BukkitInventories.from(this)

fun List<ItemStack>.toMutator(): InventoryMutator<ItemStack, Player> = BukkitInventories.from(this)
