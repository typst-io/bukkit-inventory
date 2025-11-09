package io.typst.inventory;

import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

/**
 * Read-only view over an {@link InventoryAdapter} with helpers to compute
 * derived inventory operations in an immutable / side-effect-free way.
 *
 * <p>This type never mutates the underlying {@link InventoryAdapter}, and its
 * own fields are immutable after construction. Methods such as
 * {@link #takeItems(Iterable)} and {@link #giveItems(Iterable)} return
 * {@link InventoryPatch} instances describing the changes that <em>would</em>
 * be applied, but do not perform any mutation themselves.</p>
 *
 * <p><strong>Important:</strong> this class does not own a deep copy of the
 * inventory contents. It delegates all reads to the given {@link InventoryAdapter}.
 * If the underlying inventory is modified externally after the snapshot is created,
 * those changes will be observed through this view. If you need a truly frozen
 * snapshot, use {@link #toImmutable()} or wrap a copied map in a {@link MapInventoryAdapter} before constructing
 * this instance.</p>
 */
@Value
@With
public class InventorySnapshotView<A> implements Iterable<Map.Entry<Integer, A>> {
    /**
     * Note that this field can be a mutable
     */
    InventoryAdapter<A> inventory;
    ItemStackOps<A> itemOps;
    ItemKey emptyItemkey;

    public InventorySnapshotView(@NotNull InventoryAdapter<A> inventory, @NotNull ItemStackOps<A> itemOps, ItemKey emptyItemkey) {
        this.inventory = inventory;
        this.itemOps = itemOps;
        this.emptyItemkey = emptyItemkey;
    }

    public InventorySnapshotView<A> toImmutable() {
        Map<Integer, A> map = new LinkedHashMap<>();
        for (Map.Entry<Integer, @NotNull A> pair : inventory) {
            A item = pair.getValue();
            A newItem = itemOps.isEmpty(item) ? itemOps.empty() : item;
            map.put(pair.getKey(), newItem);
        }
        return new InventorySnapshotView<>(new MapInventoryAdapter<>(Map.copyOf(map), itemOps), itemOps, emptyItemkey);
    }

    public InventorySnapshotView<A> updated(Map<Integer, A> modifiedItems) {
        Map<Integer, A> newItems = new LinkedHashMap<>();
        inventory.iterator().forEachRemaining(pair -> newItems.put(pair.getKey(), pair.getValue()));
        newItems.putAll(modifiedItems);
        return withInventory(new MapInventoryAdapter<>(newItems, itemOps));
    }

    @Override
    public @NotNull Iterator<Map.Entry<Integer, A>> iterator() {
        return inventory.iterator();
    }

    @NotNull
    public InventorySnapshotView<A> subInventory(Iterable<Integer> slots) {
        return withInventory(new SubInventoryAdapter<>(inventory, itemOps, slots));
    }

    @NotNull
    public InventorySnapshotView<A> subInventory(Integer... slots) {
        return subInventory(List.of(slots));
    }

    /**
     * Computes how much of the given {@code item} could be inserted into each slot
     * of this snapshot without modifying it.
     *
     * @param item the item to test
     * @return a map of slot index to insertable amount for that slot
     */
    @NotNull
    public Map<Integer, Integer> findSpaces(A item) {
        if (itemOps.isEmpty(item)) {
            return Collections.emptyMap();
        }
        return findSpaces(itemOps.getAmount(item), itemOps.getMaxStackSize(item), a -> itemOps.isSimilar(a, item));
    }

    /**
     * Computes per-slot space for up to {@code amount} items identified by the given key.
     * Uses {@link ItemStackOps#create(ItemKey)} to determine the max stack size.
     *
     * @param key    the key to find space
     * @param amount the amount how much to find
     * @return amounts by slot
     */
    @NotNull
    public Map<Integer, Integer> findSpaces(ItemKey key, int amount) {
        A item = itemOps.create(key);
        int maxStack = item != null
                ? itemOps.getMaxStackSize(item)
                : 0;
        return findSpaces(amount, maxStack, a -> itemOps.getKeyFrom(a).equals(key));
    }

    /**
     * Low-level variant used internally.
     *
     * @param amount    total amount to distribute
     * @param maxStack  max stack size per slot
     * @param predicate matches slots that can stack with the item
     * @return slot {@literal ->} allocatable amount
     */
    @NotNull
    public Map<Integer, Integer> findSpaces(int amount, int maxStack, Predicate<A> predicate) {
        if (amount <= 0 || maxStack <= 0) {
            return Collections.emptyMap();
        }
        Map<Integer, Integer> map = new HashMap<>();
        for (Map.Entry<Integer, A> pair : inventory) {
            if (amount <= 0) {
                break;
            }
            Integer slot = pair.getKey();
            A item = pair.getValue();
            if (itemOps.isEmpty(item)) {
                int spaceAmount = Math.min(maxStack, amount);
                map.put(slot, spaceAmount);
                amount -= spaceAmount;
            } else if (predicate.test(item)) {
                int spaceAmount = Math.min(maxStack - itemOps.getAmount(item), amount);
                if (spaceAmount >= 1) {
                    map.put(slot, spaceAmount);
                    amount -= spaceAmount;
                }
            }
        }
        return map;
    }

    /**
     * Finds how many items similar to {@code item} can be taken from each slot,
     * up to {@code itemOps.getAmount(item)} in total.
     *
     * @return map of slot index to taken amount
     */
    @NotNull
    public Map<Integer, Integer> findSlots(A item) {
        return findSlots(itemOps.getAmount(item), a -> itemOps.isSimilar(a, item));
    }

    /**
     * Finds slots contributing up to {@code amount} items matching the given key.
     */
    @NotNull
    public Map<Integer, Integer> findSlots(ItemKey key, int amount) {
        return findSlots(amount, a -> itemOps.getKeyFrom(a).equals(key));
    }

    /**
     * Low-level variant that accumulates up to {@code count} items
     * from slots matching the predicate.
     *
     * @param count     the count to find
     * @param predicate the predicator to find
     * @return slot {@literal ->} amount taken from that slot
     */
    @NotNull
    public Map<Integer, Integer> findSlots(int count, Predicate<A> predicate) {
        if (count <= 0) {
            return Collections.emptyMap();
        }
        Map<Integer, Integer> ret = new HashMap<>();
        for (Map.Entry<Integer, A> pair : inventory) {
            if (count <= 0) {
                break;
            }
            Integer slot = pair.getKey();
            A x = pair.getValue();
            if (predicate.test(x)) {
                int amount = Math.min(count, itemOps.getAmount(x));
                if (amount >= 1) {
                    count -= amount;
                    ret.put(slot, amount);
                }
            }
        }
        return ret;
    }

    @NotNull
    public InventoryPatch<A> takeItems(Iterable<A> items) {
        InventoryPatch<A> acc = InventoryPatch.empty();
        for (A item : items) {
            InventoryPatch<A> result = itemOps.isEmpty(item)
                    ? InventoryPatch.empty()
                    : takeItem(itemOps.getAmount(item), item, a -> itemOps.isSimilar(a, item));
            acc = acc.plus(result);
        }
        return acc;
    }

    @SafeVarargs
    public final InventoryPatch<A> takeItems(A... items) {
        return takeItems(List.of(items));
    }

    /**
     * Tries to take up to {@code count} items matching the given predicate.
     *
     * @param count     the requested amount
     * @param predicate matches items to be taken
     * @return a result containing:
     * <ul>
     *   <li>the updated slot contents for affected slots,</li>
     *   <li>{@code remainingCount}: the part of {@code count} that could
     *       not be fulfilled (0 if fully satisfied).</li>
     * </ul>
     */
    @NotNull
    public InventoryPatch<A> takeItem(int count, A baseItem, Predicate<A> predicate) {
        Map<Integer, Integer> slots = findSlots(count, predicate);
        if (slots.isEmpty()) {
            A remainItem = itemOps.copy(baseItem);
            itemOps.setAmount(remainItem, count);
            return InventoryPatch.fromTakeResult(Map.of(), remainItem);
        }

        Map<Integer, A> ret = new HashMap<>();
        for (Map.Entry<Integer, Integer> pair : slots.entrySet()) {
            Integer slot = pair.getKey();
            Integer amount = pair.getValue();
            A theItem = inventory.get(slot);
            int theAmount = itemOps.getAmount(theItem);
            int newAmount = theAmount - amount;
            A newItem = itemOps.copy(theItem);
            if (newAmount <= 0) {
                ret.put(slot, itemOps.empty());
            } else {
                itemOps.setAmount(newItem, theAmount - amount);
                ret.put(slot, newItem);
            }
            count -= amount;
        }
        A remainingItem = null;
        if (count >= 1) {
            remainingItem = itemOps.copy(baseItem);
            itemOps.setAmount(remainingItem, count);
        }
        return InventoryPatch.fromTakeResult(ret, remainingItem);
    }

    public boolean hasItems(A x) {
        int amount = itemOps.getAmount(x);
        return amount <= findSlots(x).values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    public int countItems(ItemKey x) {
        return findSlots(x, Integer.MAX_VALUE).values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    public InventoryPatch<A> giveItems(Iterable<A> items) {
        InventoryPatch<A> patch = InventoryPatch.empty();
        for (A item : items) {
            Map<Integer, Integer> spaces = findSpaces(item);
            InventoryPatch<A> thePatch = InventoryPatch.empty();

            if (!spaces.isEmpty()) {
                Map<Integer, A> ret = new HashMap<>();
                int leftoverAmount = itemOps.getAmount(item);
                for (Map.Entry<Integer, Integer> pair : spaces.entrySet()) {
                    Integer slot = pair.getKey();
                    Integer amount = pair.getValue();
                    A theItem = inventory.get(slot);
                    A newItem = itemOps.copy(item);
                    itemOps.setAmount(newItem, itemOps.getAmount(theItem) + amount);
                    ret.put(slot, newItem);
                    leftoverAmount -= amount;
                }
                A leftoverItem = null;
                if (leftoverAmount >= 1) {
                    A leftover = itemOps.copy(item);
                    itemOps.setAmount(leftover, leftoverAmount);
                    leftoverItem = leftover;
                }
                thePatch = InventoryPatch.fromGiveResult(ret, leftoverItem);
            }
            patch = patch.plus(thePatch);
        }
        return patch;
    }

    @SafeVarargs
    public final InventoryPatch<A> giveItems(A... items) {
        return giveItems(List.of(items));
    }
}
