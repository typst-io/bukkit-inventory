package io.typst.inventory.bukkit;

import io.typst.inventory.InventoryMutator;
import io.typst.inventory.InventoryPatch;
import io.typst.inventory.ItemStackOps;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
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
    public static Optional<ItemStack> addedItem(@Nullable ItemStack target, @NotNull ItemStack item, @NotNull ItemStackOps<ItemStack> ops) {
        ItemStack targetItem = target != null ? target : ops.empty();
        LinkedHashMap<Integer, ItemStack> map = new LinkedHashMap<>();
        map.put(0, targetItem);
        InventoryMutator<ItemStack, Player> mutator = BukkitInventories.from(map).withItemOps(ops);
        if (mutator.giveItem(item)) {
            ItemStack ret = map.get(0);
            return Optional.ofNullable(ret != null && !isEmpty(ret) ? ret : null);
        }
        return Optional.empty();
    }

    public static Optional<ItemStack> addedItem(@Nullable ItemStack target, @NotNull ItemStack item) {
        return addedItem(target, item, BukkitItemStackOps.INSTANCE);
    }

    public static List<ItemStack> collapseItems(Collection<ItemStack> items) {
        return BukkitItemStackOps.INSTANCE.collapseItems(items);
    }

    public static ItemStack getEmpty() {
        return BukkitItemStackOps.INSTANCE.empty();
    }
}
