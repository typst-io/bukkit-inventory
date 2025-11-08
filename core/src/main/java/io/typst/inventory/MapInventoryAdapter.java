package io.typst.inventory;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;

public class MapInventoryAdapter<A> implements InventoryAdapter<A> {
    private final Map<Integer, A> itemMap;
    private final ItemStackOps<A> itemOps;

    public MapInventoryAdapter(Map<Integer, A> itemMap, ItemStackOps<A> itemOps) {
        this.itemMap = itemMap;
        this.itemOps = itemOps;
    }

    @Override
    public A get(int slot) {
        A item = itemMap.get(slot);
        return item != null ? item : itemOps.empty();
    }

    @Override
    public void set(int slot, A item) {
        itemMap.put(slot, item);
    }

    @Override
    public @NotNull Iterator<Map.Entry<Integer, A>> iterator() {
        return itemMap.entrySet().iterator();
    }
}
