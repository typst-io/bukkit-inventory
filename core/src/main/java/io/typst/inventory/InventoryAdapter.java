package io.typst.inventory;

import java.util.Map;

public interface InventoryAdapter<A> extends Iterable<Map.Entry<Integer, A>> {
    A get(int slot);

    void set(int slot, A item);
}
