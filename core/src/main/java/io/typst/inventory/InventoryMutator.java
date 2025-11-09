package io.typst.inventory;

import lombok.With;

import java.util.List;
import java.util.Map;

/**
 * Applies {@link InventoryPatch}es and high-level operations to a mutable
 * {@link InventoryAdapter}-backed inventory.
 *
 * <p>This class is responsible for performing the actual mutation. All
 * heavy logic (space calculation, validation, combining operations) is
 * delegated to {@link InventorySnapshotView}, {@link InventoryPatch}, and
 * {@link InventoryTransaction}.</p>
 *
 * <p>Methods such as {@link #takeItems(Iterable)} are atomic with respect
 * to this mutator: they compute a patch first and only write to the
 * underlying inventory if it is fully successful.</p>
 */
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

    public InventorySnapshotView<I> toMutableSnapshot() {
        return new InventorySnapshotView<>(inventory, itemOps, emptyItemKey);
    }

//    private List<Integer> inputSlots = List.of(1, 2, 3);
//    private int outputSlot = 4;
//    private List<I> ingredients = List.of();
//    private I output = null;
//
//    public void test3() {
//        InventoryTransaction<I> transaction = InventoryTransaction.from(toImmutable())
//                .updated(inv -> inv.subInventory(inputSlots).takeItems(ingredients))
//                .updated(inv -> inv.subInventory(outputSlot).giveItems(output));
//        if (transaction.isSuccess()) {
//            update(transaction.getPatch());
//        } else {
//            InventoryFailure<I> failure = transaction.getPatch().getFailure();
//
//        }
//    }

    public void update(InventoryPatch<I> patch) {
        for (Map.Entry<Integer, I> pair : patch.getModifiedItems().entrySet()) {
            inventory.set(pair.getKey(), pair.getValue());
        }
    }

    public void giveItemOrDrop(E entity, I item) {
        InventorySnapshotView<I> inv = toMutableSnapshot();
        InventoryPatch<I> patch = inv.giveItems(item);
        patch.getModifiedItems().forEach(inventory::set);
        patch.getFailure().getGiveLeftoverItems().forEach(a -> entityOps.dropItem(entity, a));
    }

    public boolean giveItem(Iterable<I> items) {
        InventorySnapshotView<I> inv = toMutableSnapshot();
        InventoryPatch<I> patch = inv.giveItems(items);
        patch.getModifiedItems().forEach(inventory::set);
        return true;
    }

    @SafeVarargs
    public final boolean giveItem(I... items) {
        return giveItem(List.of(items));
    }

    public boolean takeItems(Iterable<I> items) {
        InventoryPatch<I> patch = toMutableSnapshot().takeItems(items);
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

    public InventoryMutator<I, E> withSubInventory(Iterable<Integer> slots) {
        return withInventory(new SubInventoryAdapter<>(inventory, itemOps, slots));
    }

    public InventoryMutator<I, E> withSubInventory(Integer... slots) {
        return withInventory(new SubInventoryAdapter<>(inventory, itemOps, List.of(slots)));
    }
}
