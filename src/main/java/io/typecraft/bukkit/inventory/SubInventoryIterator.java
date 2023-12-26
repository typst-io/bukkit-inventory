package io.typecraft.bukkit.inventory;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.ListIterator;

public class SubInventoryIterator implements ListIterator<ItemStack> {
    private final Inventory inventory;
    private final List<Integer> slots;
    private int nextIndex = 0;
    private int lastIndex = 0;

    public SubInventoryIterator(Inventory inventory, List<Integer> slots) {
        this.inventory = inventory;
        this.slots = slots;
    }

    @Override
    public boolean hasNext() {
        return nextIndex < slots.size();
    }

    @Override
    public ItemStack next() {
        int index = nextIndex++;
        lastIndex = index;
        return inventory.getItem(slots.get(index));
    }

    @Override
    public boolean hasPrevious() {
        return nextIndex > 0;
    }

    @Override
    public ItemStack previous() {
        int index = --nextIndex;
        lastIndex = index;
        return inventory.getItem(slots.get(index));
    }

    @Override
    public int nextIndex() {
        return slots.get(nextIndex);
    }

    @Override
    public int previousIndex() {
        return slots.get(nextIndex - 1);
    }

    @Override
    public void set(ItemStack itemStack) {
        inventory.setItem(slots.get(lastIndex), itemStack);
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
