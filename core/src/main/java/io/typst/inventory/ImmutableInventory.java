package io.typst.inventory;

import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

/**
 * Immutable snapshot of an inventory plus the operations required
 * to interpret and manipulate its items.
 *
 * <p>This type does not mutate any underlying inventory. Methods like
 * {@code giveItem} and {@code takeItem} return result objects describing
 * the changes (updated slots, leftovers) based on this snapshot.</p>
 */
@Value
@With
public class ImmutableInventory<A> implements Iterable<Map.Entry<Integer, A>> {
    /**
     * Map of slot index to item value.
     * <p>
     * All slots in this snapshot must use the {@link ItemStackOps#empty()} value
     * to represent an empty slot; {@code null} values are not allowed.
     * Callers are responsible for providing a consistent map.
     */
    Map<Integer, A> itemMap;
    ItemStackOps<A> itemOps;
    ItemKey emptyItemkey;

    public ImmutableInventory(@NotNull Map<Integer, @NotNull A> itemMap, @NotNull ItemStackOps<A> itemOps, ItemKey emptyItemkey) {
        this.itemMap = Map.copyOf(itemMap);
        this.itemOps = itemOps;
        this.emptyItemkey = emptyItemkey;
    }

    @NotNull
    public static <A> ImmutableInventory<A> from(Iterable<Map.Entry<Integer, @NotNull A>> entries, ItemStackOps<A> itemOps, ItemKey emptyItemkey) {
        Map<Integer, A> map = new HashMap<>();
        for (Map.Entry<Integer, @NotNull A> pair : entries) {
            A item = pair.getValue();
            A newItem = itemOps.isEmpty(item) ? itemOps.empty() : item;
            map.put(pair.getKey(), newItem);
        }
        return new ImmutableInventory<>(map, itemOps, emptyItemkey);
    }

    @NotNull
    public static <A> ImmutableInventory<A> fromList(List<A> items, ItemStackOps<A> itemOps, ItemKey emptyItemkey) {
        if (items.isEmpty()) {
            return new ImmutableInventory<>(Collections.emptyMap(), itemOps, emptyItemkey);
        }
        Map<Integer, A> itemMap = new HashMap<>();
        for (int i = 0; i < items.size(); i++) {
            A item = items.get(i);
            if (itemOps.isEmpty(item)) {
                itemMap.put(i, itemOps.empty());
            } else {
                itemMap.put(i, item);
            }
        }
        return new ImmutableInventory<>(itemMap, itemOps, emptyItemkey);
    }

    public static <A> Map<Integer, A> subMap(Map<Integer, A> map, Iterable<Integer> slots, A empty) {
        Map<Integer, A> newMap = new HashMap<>();
        for (Integer slot : slots) {
            A item = map.get(slot);
            newMap.put(slot, item != null ? item : empty);
        }
        return newMap;
    }

    @Override
    public @NotNull Iterator<Map.Entry<Integer, A>> iterator() {
        return itemMap.entrySet().iterator();
    }

    @NotNull
    public ImmutableInventory<A> subSnapshot(Iterable<Integer> slots) {
        return withItemMap(subMap(itemMap, slots, itemOps.empty()));
    }

    @NotNull
    public ImmutableInventory<A> subSnapshot(Integer... slots) {
        return withItemMap(subMap(itemMap, List.of(slots), itemOps.empty()));
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
        for (Map.Entry<Integer, A> pair : itemMap.entrySet()) {
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
        for (Map.Entry<Integer, A> pair : itemMap.entrySet()) {
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
    public TakeResult<A> takeItem(A item) {
        if (itemOps.isEmpty(item)) {
            return TakeResult.empty();
        }
        return takeItem(itemOps.getAmount(item), a -> itemOps.isSimilar(a, item));
    }

    @NotNull
    public TakeResult<A> takeItems(Iterable<A> items) {
        TakeResult<A> acc = TakeResult.empty();
        for (A item : items) {
            TakeResult<A> result = takeItem(item);
            acc = acc.plus(result);
        }
        return acc;
    }

    @NotNull
    public TakeResult<A> takeItem(ItemKey key, int amount) {
        if (key.equals(emptyItemkey)) {
            return TakeResult.empty();
        }
        return takeItem(amount, a -> itemOps.getKeyFrom(a).equals(key));
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
    public TakeResult<A> takeItem(int count, Predicate<A> predicate) {
        Map<Integer, Integer> slots = findSlots(count, predicate);
        if (slots.isEmpty()) {
            return TakeResult.empty();
        }

        Map<Integer, A> ret = new HashMap<>();
        for (Map.Entry<Integer, Integer> pair : slots.entrySet()) {
            Integer slot = pair.getKey();
            Integer amount = pair.getValue();
            A theItem = itemMap.get(slot);
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
        return new TakeResult<>(ret, count);
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

    /**
     * Tries to insert the given item into this inventory view.
     *
     * @param x the item to insert
     * @return a result containing:
     * <ul>
     *   <li>the updated slot contents for affected slots,</li>
     *   <li>the leftover item if not all of {@code x} could be inserted,
     *       or {@code null} if fully inserted.</li>
     * </ul>
     * This method does not modify this {@code InventorySnapshot}.
     */
    @NotNull
    public GiveResult<A> giveItem(A x) {
        Map<Integer, Integer> spaces = findSpaces(x);
        if (spaces.isEmpty()) {
            return GiveResult.empty();
        }

        Map<Integer, A> ret = new HashMap<>();
        int leftoverAmount = itemOps.getAmount(x);
        for (Map.Entry<Integer, Integer> pair : spaces.entrySet()) {
            Integer slot = pair.getKey();
            Integer amount = pair.getValue();
            A item = itemMap.get(slot);
            A newItem = itemOps.copy(item);
            itemOps.setAmount(newItem, itemOps.getAmount(item) + amount);
            ret.put(slot, newItem);
            leftoverAmount -= amount;
        }
        A leftoverItem = null;
        if (leftoverAmount >= 1) {
            A leftover = itemOps.copy(x);
            itemOps.setAmount(leftover, leftoverAmount);
            leftoverItem = leftover;
        }
        return new GiveResult<>(ret, leftoverItem);
    }
}
