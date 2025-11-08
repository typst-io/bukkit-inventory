package io.typst.inventory.bukkit;

import io.typst.inventory.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public class BukkitInventories {
    public static InventoryAdapter<ItemStack> adapterFrom(Inventory inventory) {
        return new BukkitInventoryAdapter(inventory);
    }

    public static InventoryAdapter<ItemStack> adapterFrom(Map<Integer, ItemStack> map) {
        return new MapInventoryAdapter<>(map, BukkitItemStackOps.INSTANCE);
    }

    public static InventoryAdapter<ItemStack> adapterFrom(List<ItemStack> list) {
        return new ListInventoryAdapter<>(list, BukkitItemStackOps.INSTANCE);
    }

    public static ImmutableInventory<ItemStack> immutableFrom(Inventory inventory) {
        return ImmutableInventory.from(adapterFrom(inventory), BukkitItemStackOps.INSTANCE, ItemKey.MINECRAFT_EMPTY);
    }

    public static ImmutableInventory<ItemStack> immutableFrom(Map<Integer, ItemStack> map) {
        return ImmutableInventory.from(adapterFrom(map), BukkitItemStackOps.INSTANCE, ItemKey.MINECRAFT_EMPTY);
    }

    public static ImmutableInventory<ItemStack> immutableFrom(List<ItemStack> list) {
        return ImmutableInventory.from(adapterFrom(list), BukkitItemStackOps.INSTANCE, ItemKey.MINECRAFT_EMPTY);
    }

    public static InventoryAdapter<ItemStack> subInventory(InventoryAdapter<ItemStack> delegate, Iterable<Integer> slots) {
        return new SubInventoryAdapter<>(delegate, BukkitItemStackOps.INSTANCE, slots);
    }

    public static InventoryMutator<ItemStack, Player> from(Inventory inventory) {
        return new InventoryMutator<>(adapterFrom(inventory), BukkitItemStackOps.INSTANCE, BukkitPlayerOps.INSTANCE, ItemKey.MINECRAFT_EMPTY);
    }

    public static InventoryMutator<ItemStack, Player> from(Map<Integer, ItemStack> map) {
        return new InventoryMutator<>(adapterFrom(map), BukkitItemStackOps.INSTANCE, BukkitPlayerOps.INSTANCE, ItemKey.MINECRAFT_EMPTY);
    }

    public static InventoryMutator<ItemStack, Player> from(List<ItemStack> list) {
        return new InventoryMutator<>(adapterFrom(list), BukkitItemStackOps.INSTANCE, BukkitPlayerOps.INSTANCE, ItemKey.MINECRAFT_EMPTY);
    }
}
