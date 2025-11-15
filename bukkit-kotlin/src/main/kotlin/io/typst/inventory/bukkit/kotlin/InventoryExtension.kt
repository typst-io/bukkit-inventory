package io.typst.inventory.bukkit.kotlin

import io.typst.inventory.InventoryMutator
import io.typst.inventory.InventoryPatch
import io.typst.inventory.bukkit.BukkitInventories
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

// mutator
fun Inventory.toMutator(): InventoryMutator<ItemStack, Player> = BukkitInventories.from(this)

fun Map<Int, ItemStack>.toMutator(): InventoryMutator<ItemStack, Player> = BukkitInventories.from(this)

fun List<ItemStack>.toMutator(): InventoryMutator<ItemStack, Player> = BukkitInventories.from(this)

fun failureInventoryPatch(): InventoryPatch<ItemStack> = BukkitInventories.failurePatch()

// util
fun Inventory.copy(): InventoryMutator<ItemStack, Player> = toMutator().copy()

fun Inventory.subInventory(vararg slots: Int): InventoryMutator<ItemStack, Player> = toMutator().subInventory(slots.toList())

fun Inventory.subInventory(slots: Iterable<Int>): InventoryMutator<ItemStack, Player> = toMutator().subInventory(slots)
