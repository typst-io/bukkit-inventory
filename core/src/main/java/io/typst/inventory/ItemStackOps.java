package io.typst.inventory;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public interface ItemStackOps<A> {
    boolean isEmpty(@Nullable A item);

    ItemKey getKeyFrom(A item);

    default Map<ItemKey, A> getHeaderMapFrom(Iterable<A> iterable) {
        Map<ItemKey, A> map = new HashMap<>();
        for (A item : iterable) {
            ItemKey header = getKeyFrom(item);
            A theItem = map.get(header);
            A newItem = theItem != null ? theItem : copy(item);
            int theAmount = theItem != null ? getAmount(theItem) : 0;
            setAmount(newItem, getAmount(newItem) + theAmount);
            map.put(header, newItem);
        }
        return map;
    }

    default List<A> collapseItems(Collection<A> items) {
        Map<A, Integer> map = new HashMap<>(items.size());
        for (A item : items) {
            A newItem = copy(item);
            setAmount(newItem, 1);
            map.put(newItem, map.getOrDefault(newItem, 0) + getAmount(item));
        }
        List<A> ret = new ArrayList<>(map.size());
        for (Map.Entry<A, Integer> pair : map.entrySet()) {
            setAmount(pair.getKey(), pair.getValue());
            ret.add(pair.getKey());
        }
        return ret;
    }

    int getAmount(A item);

    void setAmount(A item, int amount);

    int getMaxStackSize(A item);

    A copy(A item);

    @Nullable
    A create(ItemKey key);

    A empty();

    boolean isSimilar(A a, A b);
}
