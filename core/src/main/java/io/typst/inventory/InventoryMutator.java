package io.typst.inventory;

import lombok.Value;
import lombok.With;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Applies {@link InventoryPatch}es and high-level operations to a mutable
 * {@link InventoryAdapter}-backed inventory.
 *
 * <p>This class is responsible for performing the actual mutation. All
 * heavy logic (space calculation, validation, combining operations) is
 * delegated to {@link InventorySnapshotView}, {@link InventoryPatch}.
 *
 * <p>Methods such as {@link #takeItems(Iterable)} are atomic with respect
 * to this mutator: they compute a patch first and only write to the
 * underlying inventory if it is fully successful.</p>
 */
@With
@Value
public class InventoryMutator<I, E> {
    InventoryAdapter<I> inventory;
    ItemStackOps<I> itemOps;
    EntityOps<E, I> entityOps;
    ItemKey emptyItemKey;

    public InventoryMutator(InventoryAdapter<I> inventory, ItemStackOps<I> itemOps, EntityOps<E, I> entityOps, ItemKey emptyItemKey) {
        this.inventory = inventory;
        this.itemOps = itemOps;
        this.entityOps = entityOps;
        this.emptyItemKey = emptyItemKey;
    }

    public InventorySnapshotView<I> toSnapshotView() {
        return new InventorySnapshotView<>(inventory, itemOps, emptyItemKey);
    }

    public InventoryMutator<I, E> copy() {
        Map<Integer, I> map = new LinkedHashMap<>();
        for (Map.Entry<Integer, I> pair : inventory) {
            map.put(pair.getKey(), pair.getValue());
        }
        return withInventory(new MapInventoryAdapter<>(map, itemOps.empty()));
    }

    public void giveItemOrDrop(E entity, I item) {
        InventorySnapshotView<I> inv = toSnapshotView();
        InventoryPatch<I> patch = inv.giveItems(item);
        patch.getModifiedItems().forEach(inventory::set);
        patch.getFailure().getGiveLeftoverItems().forEach(a -> entityOps.dropItem(entity, a));
    }

    public boolean giveItem(Iterable<I> items) {
        InventorySnapshotView<I> inv = toSnapshotView();
        InventoryPatch<I> patch = inv.giveItems(items);
        if (patch.isSuccess()) {
            patch.getModifiedItems().forEach(inventory::set);
            return true;
        } else {
            return false;
        }
    }

    @SafeVarargs
    public final boolean giveItem(I... items) {
        return giveItem(List.of(items));
    }

    public boolean takeItems(Iterable<I> items) {
        InventoryPatch<I> patch = toSnapshotView().takeItems(items);
        if (patch.isSuccess()) {
            patch.getModifiedItems().forEach(inventory::set);
            return true;
        }
        return false;
    }

    @SafeVarargs
    public final boolean takeItems(I... items) {
        return takeItems(List.of(items));
    }

    public boolean takeItem(int count, ItemKey key) {
        InventoryPatch<I> patch = toSnapshotView().takeItem(count, key);
        if (patch.isSuccess()) {
            patch.getModifiedItems().forEach(inventory::set);
            return true;
        }
        return false;
    }

    public InventoryMutator<I, E> subInventory(Iterable<Integer> slots) {
        return withInventory(new SubInventoryAdapter<>(inventory, itemOps.empty(), slots));
    }

    public InventoryMutator<I, E> subInventory(Integer... slots) {
        return withInventory(new SubInventoryAdapter<>(inventory, itemOps.empty(), List.of(slots)));
    }

    public void forEach(BiConsumer<Integer, I> f) {
        for (Map.Entry<Integer, I> pair : inventory) {
            f.accept(pair.getKey(), pair.getValue());
        }
    }
}
