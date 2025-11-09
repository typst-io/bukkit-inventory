package io.typst.inventory.bukkit.kotlin

import io.typst.inventory.InventoryFailure
import io.typst.inventory.InventoryPatch
import io.typst.inventory.InventorySnapshotView
import io.typst.inventory.InventoryTransaction
import io.typst.inventory.ItemKey
import io.typst.inventory.ItemStackOps
import io.typst.inventory.bukkit.BukkitItem
import io.typst.inventory.bukkit.BukkitItemStackOps
import io.typst.inventory.bukkit.BukkitItemStacks
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.jvm.optionals.getOrNull

val ItemStack?.isEmptyItem: Boolean get() = BukkitItemStacks.isEmpty(this)

fun ItemStack?.addedItem(item: ItemStack, ops: ItemStackOps<ItemStack> = BukkitItemStackOps.INSTANCE): ItemStack? = BukkitItemStacks.addedItem(this, item, ops).getOrNull()

fun Collection<ItemStack>.collapse(): List<ItemStack> = BukkitItemStacks.collapseItems(this)

fun item(
    material: Material,
    amount: Int = 1,
    displayName: String = "",
    lore: List<String> = emptyList(),
    customModelData: Int = -1,
): ItemStack =
    BukkitItem.builder()
        .material(material)
        .amount(amount)
        .displayName(displayName)
        .lore(lore)
        .customModelData(customModelData)
        .build()
        .create()

fun Material.toItemKey(): ItemKey = ItemKey(key.toString(), "")

fun ItemKey.toItemStack(): ItemStack? = BukkitItemStackOps.INSTANCE.create(this)

fun emptyItemStack(): ItemStack = BukkitItemStacks.getEmpty()

fun failurePatch(): InventoryPatch<ItemStack> = InventoryPatch.failure(emptyItemStack())