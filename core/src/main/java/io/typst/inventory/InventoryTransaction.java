package io.typst.inventory;

import lombok.Value;
import lombok.With;

import java.util.function.Function;

/**
 * Helper for composing multiple {@link InventoryPatch}-producing operations
 * against an {@link InventorySnapshotView} in a transactional style.
 *
 * <p>Each {@link #updated(Function)} step:</p>
 * <ul>
 *   <li>runs the given function against the current snapshot,</li>
 *   <li>merges the returned {@link InventoryPatch} into the accumulated patch,</li>
 *   <li>if the merged patch is successful, eagerly applies its modified items
 *       to produce the next snapshot; otherwise keeps the previous snapshot.</li>
 * </ul>
 *
 * <p>The final {@link #isSuccess()} indicates whether all operations were fully
 * satisfied. Only in that case should the resulting {@link #getInventory()} be
 * committed to the underlying inventory via {@link InventoryMutator}.</p>
 */
@Value
@With
public class InventoryTransaction<A> {
    /**
     * Guaranteed immutable
     */
    InventorySnapshotView<A> inventory;
    InventoryPatch<A> patch;

    public InventorySnapshotView<A> getInventory() {
        return inventory;
    }

    public InventoryTransaction<A> updated(Function<InventorySnapshotView<A>, InventoryPatch<A>> f) {
        if (!patch.getFailure().isEmpty()) {
            return this;
        }
        InventoryPatch<A> thePatch = f.apply(inventory);
        InventoryPatch<A> newPatch = patch.plus(thePatch);
        return newPatch.isSuccess()
                ? new InventoryTransaction<>(inventory.updated(newPatch.getModifiedItems()), newPatch)
                : new InventoryTransaction<>(inventory, newPatch);
    }

    public static <A> InventoryTransaction<A> from(InventorySnapshotView<A> inventory) {
        return new InventoryTransaction<>(inventory, InventoryPatch.empty());
    }

    public boolean isSuccess() {
        return patch.isSuccess();
    }
}
