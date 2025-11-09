package io.typst.inventory;

import java.util.Map;

/**
 * Minimal abstraction over an indexed inventory-like structure.
 *
 * <p>Implementations may be backed by live game inventories, simple collections,
 * or custom views (e.g. {@link SubInventoryAdapter}). No immutability or
 * thread-safety is assumed.</p>
 *
 * <p>{@link InventorySnapshotView} and related utilities will treat this as a
 * read-only source and never call {@link #set(int, Object)} directly.</p>
 */
public interface InventoryAdapter<A> extends Iterable<Map.Entry<Integer, A>> {
    A get(int slot);

    void set(int slot, A item);
}
