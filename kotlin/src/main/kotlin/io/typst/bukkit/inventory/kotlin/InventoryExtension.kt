package io.typst.bukkit.inventory.kotlin

import io.typst.bukkit.inventory.HashInventoryIterator
import io.typst.bukkit.inventory.Inventories
import io.typst.bukkit.inventory.ItemHeader
import io.typst.bukkit.inventory.SubInventoryIterator
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

// subInventory
fun Inventory.sub(slots: List<Int>): ListIterator<ItemStack> =
    SubInventoryIterator(this, slots)

fun Inventory.sub(vararg slots: Int): ListIterator<ItemStack> =
    sub(slots.toList())

// has item
fun Iterator<ItemStack>.hasItems(item: ItemStack): Int =
    Inventories.hasItems(this, item)

fun Iterable<ItemStack>.hasItems(item: ItemStack): Int =
    iterator().hasItems(item)

fun MutableMap<Int, ItemStack>.hasItems(item: ItemStack): Int =
    HashInventoryIterator(this).hasItems(item)

// has space
fun Iterator<ItemStack>.hasSpace(item: ItemStack): Boolean =
    Inventories.hasSpace(this, item)

fun Iterable<ItemStack>.hasSpace(item: ItemStack): Boolean =
    iterator().hasSpace(item)

fun MutableMap<Int, ItemStack>.hasSpace(item: ItemStack): Boolean =
    HashInventoryIterator(this).hasSpace(item)

// take
fun ListIterator<ItemStack>.takeItem(item: ItemStack): Int =
    Inventories.takeItem(this, item)

fun Inventory.takeItem(item: ItemStack): Int =
    iterator().takeItem(item)

fun MutableMap<Int, ItemStack>.takeItem(item: ItemStack): Int =
    HashInventoryIterator(this).takeItem(item)

// give
fun HumanEntity.giveItemOrDrop(item: ItemStack): Unit =
    Inventories.giveItemOrDrop(this, item)

fun ListIterator<ItemStack>.giveItem(item: ItemStack): Int =
    Inventories.giveItem(this, item)

// header
fun ListIterator<ItemStack>.takeItem(header: ItemHeader, count: Int): Pair<Int, List<ItemStack>> {
    val ret = Inventories.takeItem(this, header, count)
    return ret.key to ret.value
}

fun Iterator<ItemStack>.hasItems(header: ItemHeader): Int =
    Inventories.hasItems(this, header)

fun Inventory.takeItem(header: ItemHeader, count: Int): Pair<Int, List<ItemStack>> {
    return iterator().takeItem(header, count)
}

fun Inventory.hasItems(header: ItemHeader): Int =
    iterator().hasItems(header)
