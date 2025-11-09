package io.typst.inventory;

import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Value
@With
public class SubInventoryAdapter<A> implements InventoryAdapter<A> {
    InventoryAdapter<A> delegate;
    ItemStackOps<A> itemOps;
    Set<Integer> slots;

    public SubInventoryAdapter(InventoryAdapter<A> delegate, ItemStackOps<A> itemOps, Iterable<Integer> slots) {
        this.delegate = delegate;
        this.itemOps = itemOps;
        Set<Integer> newSlots = new LinkedHashSet<>();
        slots.forEach(newSlots::add);
        this.slots = Set.copyOf(newSlots);
    }

    @Override
    public A get(int slot) {
        if (!slots.contains(slot)) {
            return itemOps.empty();
        }
        return delegate.get(slot);
    }

    @Override
    public void set(int slot, A item) {
        if (!slots.contains(slot)) {
            return;
        }
        delegate.set(slot, item);
    }

    @Override
    public @NotNull Iterator<Map.Entry<Integer, A>> iterator() {
        return slots.stream()
                .map(slot -> (Map.Entry<Integer, A>) new AbstractMap.SimpleEntry<>(slot, get(slot)))
                .iterator();
    }
}
