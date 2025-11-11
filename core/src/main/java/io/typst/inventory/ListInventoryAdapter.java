package io.typst.inventory;

import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;


@Value
@With
public class ListInventoryAdapter<A> implements InventoryAdapter<A> {
    List<A> items;
    A emptyItem;

    public ListInventoryAdapter(List<A> items, A emptyItem) {
        this.items = items;
        this.emptyItem = emptyItem;
    }

    @Override
    public A get(int slot) {
        return items.size() > slot
                ? items.get(slot)
                : emptyItem;
    }

    @Override
    public void set(int slot, A item) {
        if (slot >= items.size()) {
            int diff = slot + 1 - items.size();
            for (int i = 0; i < diff; i++) {
                items.add(emptyItem);
            }
        }
        items.set(slot, item);
    }

    @Override
    public @NotNull Iterator<Map.Entry<Integer, A>> iterator() {
        return IntStream.range(0, items.size())
                .mapToObj(slot -> (Map.Entry<Integer, A>) new AbstractMap.SimpleEntry<>(slot, get(slot)))
                .iterator();
    }
}
