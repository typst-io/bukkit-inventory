package io.typst.inventory;

import lombok.With;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@With
public class InventoryMutator<I, E> {
    private final InventoryAdapter<I> inventory;
    private final ItemStackOps<I> itemOps;
    private final EntityOps<E, I> entityOps;
    private final ItemKey emptyItemKey;

    public InventoryMutator(InventoryAdapter<I> inventory, ItemStackOps<I> itemOps, EntityOps<E, I> entityOps, ItemKey emptyItemKey) {
        this.inventory = inventory;
        this.itemOps = itemOps;
        this.entityOps = entityOps;
        this.emptyItemKey = emptyItemKey;
    }

    public ImmutableInventory<I> toImmutable() {
        return ImmutableInventory.from(inventory, itemOps, emptyItemKey);
    }

    public void giveItemOrDrop(E entity, I item) {
        ImmutableInventory<I> inv = toImmutable();
        GiveResult<I> result = inv.giveItem(item);
        result.getModifiedItems().forEach(inventory::set);
        result.getLeftoverItemOptional().ifPresent(a -> entityOps.dropItem(entity, a));
    }

    public boolean giveItem(Iterable<I> items) {
        ImmutableInventory<I> inv = toImmutable();
        Map<Integer, I> modifiedItems = new HashMap<>();
        for (I item : items) {
            GiveResult<I> result = inv.giveItem(item);
            if (result.getLeftoverItem() != null) {
                return false;
            }
            modifiedItems.putAll(result.getModifiedItems());
        }
        modifiedItems.forEach(inventory::set);
        return true;
    }

    @SafeVarargs
    public final boolean giveItem(I... items) {
        return giveItem(List.of(items));
    }

    public boolean takeItems(Iterable<I> items) {
        TakeResult<I> result = toImmutable().takeItems(items);
        if (result.getRemainingCount() <= 0) {
            result.getModifiedItems().forEach(inventory::set);
            return true;
        }
        return false;
    }

    @SafeVarargs
    public final boolean takeItems(I... items) {
        return takeItems(List.of(items));
    }

    public InventoryMutator<I, E> withSubInventory(Iterable<Integer> slots) {
        return withInventory(new SubInventoryAdapter<>(inventory, itemOps, slots));
    }

    public InventoryMutator<I, E> withSubInventory(Integer... slots) {
        return withInventory(new SubInventoryAdapter<>(inventory, itemOps, List.of(slots)));
    }
}
