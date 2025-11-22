package io.typst.inventory.bukkit;

import io.typst.inventory.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

// TODO: context -- ItemStackOps
public class BukkitInventories {
    public static InventoryMutator<ItemStack, Player> from(Inventory inventory) {
        ItemStackOps<ItemStack> itemOps = BukkitItemStackOps.INSTANCE;
        return new InventoryMutator<>(
                new BukkitInventoryAdapter(inventory, itemOps.empty()),
                itemOps,
                BukkitPlayerOps.INSTANCE,
                ItemKey.MINECRAFT_EMPTY
        );
    }

    public static InventoryMutator<ItemStack, Player> from(Map<Integer, ItemStack> map) {
        ItemStackOps<ItemStack> itemOps = BukkitItemStackOps.INSTANCE;
        return new InventoryMutator<>(
                new MapInventoryAdapter<>(map, itemOps.empty()),
                itemOps,
                BukkitPlayerOps.INSTANCE,
                ItemKey.MINECRAFT_EMPTY
        );
    }

    public static InventoryMutator<ItemStack, Player> from(List<ItemStack> list) {
        ItemStackOps<ItemStack> itemOps = BukkitItemStackOps.INSTANCE;
        return new InventoryMutator<>(
                new ListInventoryAdapter<>(list, itemOps.empty()),
                itemOps,
                BukkitPlayerOps.INSTANCE,
                ItemKey.MINECRAFT_EMPTY
        );
    }

    public static InventoryPatch<ItemStack> failurePatch() {
        return InventoryPatch.failure(BukkitItemStacks.getEmpty());
    }
}
