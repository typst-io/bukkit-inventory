package io.typst.inventory.bukkit.kotlin

import io.typst.inventory.bukkit.BukkitItem
import io.typst.inventory.bukkit.BukkitItemStacks
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.jvm.optionals.getOrNull

val ItemStack?.isEmptyItem: Boolean get() = BukkitItemStacks.isEmpty(this)

fun ItemStack?.addedItem(item: ItemStack): ItemStack? = BukkitItemStacks.addedItem(this, item).getOrNull()

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
