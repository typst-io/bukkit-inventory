package io.typst.inventory.bukkit;

import io.typst.inventory.GiveResult;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BukkitItemStacks {
    public static boolean isEmpty(ItemStack item) {
        return BukkitItemStackOps.INSTANCE.isEmpty(item);
    }

    /**
     * Adds the given {@code item} to the {@code target}. If there is insufficient space,
     * as much as possible is added, and the amounts of both {@code target} and {@code item} are updated.
     *
     * @param target the target to which the {@code item} is added
     * @param item   the item to add
     * @return the item that was added, or empty if no space was available or the material does not match
     */
    public static Optional<ItemStack> addedItem(@Nullable ItemStack target, @NotNull ItemStack item) {
        ItemStack targetItem = target != null ? target : BukkitItemStackOps.INSTANCE.empty();
        GiveResult<ItemStack> result = BukkitInventories.immutableFrom(Map.of(0, targetItem)).giveItem(item);
        ItemStack leftoverItem = result.getLeftoverItem();
        item.setAmount(leftoverItem != null ? leftoverItem.getAmount() : item.getAmount());
        return Optional.ofNullable(result.getModifiedItems().get(0));
    }

    public static List<ItemStack> collapseItems(Collection<ItemStack> items) {
        return BukkitItemStackOps.INSTANCE.collapseItems(items);
    }
}
