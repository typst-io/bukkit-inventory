package io.typst.inventory.bukkit;

import io.typst.inventory.InventoryAdapter;
import io.typst.inventory.ItemStackOps;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.IntStream;

public class BukkitInventoryAdapter implements InventoryAdapter<ItemStack> {
    private final Inventory inventory;
    private final ItemStackOps<ItemStack> itemOps;

    public BukkitInventoryAdapter(Inventory inventory, ItemStackOps<ItemStack> itemOps) {
        this.inventory = inventory;
        this.itemOps = itemOps;
    }

    public BukkitInventoryAdapter(Inventory inventory) {
        this(inventory, BukkitItemStackOps.INSTANCE);
    }

    @Override
    public ItemStack get(int slot) {
        ItemStack item = inventory.getItem(slot);
        return item != null
                ? item
                : itemOps.empty();
    }

    @Override
    public void set(int slot, ItemStack item) {
        try {
            inventory.setItem(slot, item);
        } catch (Exception ex) {
            // Ignore
        }
    }

    @Override
    public @NotNull Iterator<Map.Entry<Integer, ItemStack>> iterator() {
        return IntStream.range(0, inventory.getSize())
                .mapToObj(slot -> (Map.Entry<Integer, ItemStack>) new AbstractMap.SimpleEntry<>(slot, get(slot)))
                .iterator();
    }
}
