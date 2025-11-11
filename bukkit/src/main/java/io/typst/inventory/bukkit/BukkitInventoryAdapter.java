package io.typst.inventory.bukkit;

import io.typst.inventory.InventoryAdapter;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.IntStream;

public class BukkitInventoryAdapter implements InventoryAdapter<ItemStack> {
    private final Inventory inventory;
    private final ItemStack emptyItem;

    public BukkitInventoryAdapter(Inventory inventory, ItemStack emptyItem) {
        this.inventory = inventory;
        this.emptyItem = emptyItem;
    }


    @Override
    public ItemStack get(int slot) {
        ItemStack item = inventory.getItem(slot);
        return item != null
                ? item
                : emptyItem;
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
