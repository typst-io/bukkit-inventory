package io.typst.bukkit.inventory;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

public class HashInventoryIterator implements ListIterator<ItemStack> {
    private final Map<Integer, ItemStack> items;
    private final List<Integer> keys;
    private int nextIndex = 0;
    private int lastIndex = 0;

    public HashInventoryIterator(Map<Integer, ItemStack> items) {
        this.items = items;
        this.keys = new ArrayList<>(items.keySet());
    }

    @Override
    public boolean hasNext() {
        return nextIndex < keys.size();
    }

    @Override
    public ItemStack next() {
        int index = nextIndex++;
        lastIndex = index;
        return items.get(keys.get(index));
    }

    @Override
    public boolean hasPrevious() {
        return nextIndex > 0;
    }

    @Override
    public ItemStack previous() {
        int index = --nextIndex;
        lastIndex = index;
        return items.get(keys.get(index));
    }

    @Override
    public int nextIndex() {
        return keys.get(nextIndex);
    }

    @Override
    public int previousIndex() {
        return keys.get(nextIndex - 1);
    }

    @Override
    public void set(ItemStack itemStack) {
        if (itemStack != null) {
            items.put(keys.get(lastIndex), itemStack);
        } else {
            items.remove(keys.get(lastIndex));
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(ItemStack itemStack) {
        throw new UnsupportedOperationException();
    }
}
