package io.typst.inventory;

import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Map;

@Value
@With
public class MapInventoryAdapter<A> implements InventoryAdapter<A> {
    Map<Integer, A> itemMap;
    A emptyItem;

    public MapInventoryAdapter(Map<Integer, A> itemMap, A emptyItem) {
        this.itemMap = itemMap;
        this.emptyItem = emptyItem;
    }

    @Override
    public A get(int slot) {
        A item = itemMap.get(slot);
        return item != null ? item : emptyItem;
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
