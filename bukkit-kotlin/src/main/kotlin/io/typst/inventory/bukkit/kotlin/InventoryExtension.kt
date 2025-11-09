package io.typst.inventory.bukkit.kotlin

import io.typst.inventory.*
import io.typst.inventory.bukkit.BukkitInventories
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

// adapter
fun Inventory.toAdapter(): InventoryAdapter<ItemStack> = BukkitInventories.adapterFrom(this)

fun Map<Int, ItemStack>.toAdapter(): InventoryAdapter<ItemStack> =
    BukkitInventories.adapterFrom(this)

fun List<ItemStack>.toAdapter(): InventoryAdapter<ItemStack> = BukkitInventories.adapterFrom(this)

// snapshot (mutable)
fun Inventory.toView(): InventorySnapshotView<ItemStack> =
    BukkitInventories.viewFrom(this)

fun Map<Int, ItemStack>.toView(): InventorySnapshotView<ItemStack> =
    BukkitInventories.viewFrom(this)

fun List<ItemStack>.toView(): InventorySnapshotView<ItemStack> =
    BukkitInventories.viewFrom(this)

// sub
fun InventoryAdapter<ItemStack>.subInventory(slots: Iterable<Int>): InventoryAdapter<ItemStack> =
    BukkitInventories.subInventory(this, slots)

// mutator
fun Inventory.toMutator(): InventoryMutator<ItemStack, Player> = BukkitInventories.from(this)

fun Map<Int, ItemStack>.toMutator(): InventoryMutator<ItemStack, Player> = BukkitInventories.from(this)

fun List<ItemStack>.toMutator(): InventoryMutator<ItemStack, Player> = BukkitInventories.from(this)

// transaction
fun Inventory.toTransaction(): InventoryTransaction<ItemStack> = BukkitInventories.transactionFrom(this)

fun Map<Int, ItemStack>.toTransaction(): InventoryTransaction<ItemStack> = BukkitInventories.transactionFrom(this)

fun List<ItemStack>.toTransaction(): InventoryTransaction<ItemStack> = BukkitInventories.transactionFrom(this)

fun <A> InventoryTransaction<A>.updating(f: (InventorySnapshotView<A>).() -> InventoryPatch<A>): InventoryTransaction<A> =
    updated { f(it) }