package io.typst.inventory.bukkit;

import io.typst.inventory.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

// TODO: context -- ItemStackOps
public class BukkitInventories {
    // adapter (mutable)
    public static BukkitInventoryAdapter adapterFrom(Inventory inventory) {
        return new BukkitInventoryAdapter(inventory);
    }

    public static MapInventoryAdapter<ItemStack> adapterFrom(Map<Integer, ItemStack> map) {
        return new MapInventoryAdapter<>(map, BukkitItemStackOps.INSTANCE);
    }

    public static ListInventoryAdapter<ItemStack> adapterFrom(List<ItemStack> list) {
        return new ListInventoryAdapter<>(list, BukkitItemStackOps.INSTANCE);
    }

    // snapshot (mutable)
    public static InventorySnapshotView<ItemStack> viewFrom(Inventory inventory) {
        return new InventorySnapshotView<>(adapterFrom(inventory), BukkitItemStackOps.INSTANCE, ItemKey.MINECRAFT_EMPTY);
    }

    public static InventorySnapshotView<ItemStack> viewFrom(Map<Integer, ItemStack> map) {
        return new InventorySnapshotView<>(adapterFrom(map), BukkitItemStackOps.INSTANCE, ItemKey.MINECRAFT_EMPTY);
    }

    public static InventorySnapshotView<ItemStack> viewFrom(List<ItemStack> list) {
        return new InventorySnapshotView<>(adapterFrom(list), BukkitItemStackOps.INSTANCE, ItemKey.MINECRAFT_EMPTY);
    }

    // sub
    public static SubInventoryAdapter<ItemStack> subInventory(InventoryAdapter<ItemStack> delegate, Iterable<Integer> slots) {
        return new SubInventoryAdapter<>(delegate, BukkitItemStackOps.INSTANCE, slots);
    }

    // mutator
    public static InventoryMutator<ItemStack, Player> from(Inventory inventory) {
        return new InventoryMutator<>(adapterFrom(inventory), BukkitItemStackOps.INSTANCE, BukkitPlayerOps.INSTANCE, ItemKey.MINECRAFT_EMPTY);
    }

    public static InventoryMutator<ItemStack, Player> from(Map<Integer, ItemStack> map) {
        return new InventoryMutator<>(adapterFrom(map), BukkitItemStackOps.INSTANCE, BukkitPlayerOps.INSTANCE, ItemKey.MINECRAFT_EMPTY);
    }

    public static InventoryMutator<ItemStack, Player> from(List<ItemStack> list) {
        return new InventoryMutator<>(adapterFrom(list), BukkitItemStackOps.INSTANCE, BukkitPlayerOps.INSTANCE, ItemKey.MINECRAFT_EMPTY);
    }

    // transaction (immutables)
    public static InventoryTransaction<ItemStack> transactionFrom(Inventory inventory) {
        return new InventoryTransaction<>(viewFrom(inventory), InventoryPatch.empty());
    }

    public static InventoryTransaction<ItemStack> transactionFrom(Map<Integer, ItemStack> map) {
        return new InventoryTransaction<>(viewFrom(map), InventoryPatch.empty());
    }

    public static InventoryTransaction<ItemStack> transactionFrom(List<ItemStack> list) {
        return new InventoryTransaction<>(viewFrom(list), InventoryPatch.empty());
    }
}
