package io.typst.bukkit.inventory.kotlin

import io.typst.bukkit.inventory.ItemStacks
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

val ItemStack.max: ItemStack
    get() = ItemStacks.maximize(this)

fun Collection<ItemStack>.toNormalized(): List<ItemStack> = ItemStacks.normalize(this)

fun item(
    material: Material,
    amount: Int = 1,
    displayName: String = "",
    lore: List<String> = emptyList(),
    customModelData: Int = -1,
): ItemStack =
    ItemStack(material, amount).apply {
        if (displayName.isNotEmpty() || lore.isNotEmpty() || customModelData >= 0) {
            itemMeta = itemMeta?.apply {
                if (customModelData >= 0) {
                    setCustomModelData(customModelData)
                }
                if (displayName.isNotEmpty()) {
                    setDisplayName(displayName)
                }
                if (lore.isNotEmpty()) {
                    setLore(lore)
                }
            }
        }
    }
