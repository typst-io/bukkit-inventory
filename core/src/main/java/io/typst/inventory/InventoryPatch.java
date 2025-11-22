package io.typst.inventory;

import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Describes a (potential) result of applying one or more inventory operations.
 *
 * <p>A patch consists of:</p>
 * <ul>
 *   <li>{@code modifiedItems}: the slot {@literal ->} new item mapping,</li>
 *   <li>{@code failure}: accumulated information about any shortages or leftovers.</li>
 * </ul>
 *
 * <p>Patches are immutable and can be combined via {@link #plus(InventoryPatch)}.
 * The combined patch behaves as if all original operations were applied in order:
 * later patches override earlier slot changes, and failures are accumulated.</p>
 *
 * <p>Call {@link #isSuccess()} to check whether the patch represents a fully
 * successful operation (no remaining required items, no leftover outputs).
 * Callers should only commit {@code modifiedItems} to a real inventory if
 * {@code isSuccess() == true}.</p>
 */
@Value
@With
public class InventoryPatch<A> {
    Map<Integer, A> modifiedItems;
    List<Map.Entry<Integer, A>> diff;
    InventoryFailure<A> failure;
    @SuppressWarnings("rawtypes")
    private static final InventoryPatch EMPTY = new InventoryPatch<>(Map.of(), List.of(), InventoryFailure.empty());

    public InventoryPatch(Map<Integer, A> modifiedItems, List<Map.Entry<Integer, A>> diff, InventoryFailure<A> failure) {
        this.modifiedItems = Map.copyOf(new LinkedHashMap<>(modifiedItems));
        this.diff = List.copyOf(new ArrayList<>(diff));
        this.failure = failure;
    }

    @SuppressWarnings("unchecked")
    public static <A> InventoryPatch<A> empty() {
        return (InventoryPatch<A>) EMPTY;
    }

    public static <A> InventoryPatch<A> failure(A emptyItem) {
        return InventoryPatch.<A>empty().withFailure(InventoryFailure.failure(emptyItem));
    }

    public boolean isSuccess() {
        return !modifiedItems.isEmpty() && failure.isEmpty();
    }

    public InventoryPatch<A> plus(InventoryPatch<A> another) {
        Map<Integer, A> newItems = new LinkedHashMap<>(modifiedItems);
        List<Map.Entry<Integer, A>> newDiff = new ArrayList<>(diff);
        newItems.putAll(another.modifiedItems);
        newDiff.addAll(another.diff);
        InventoryFailure<A> newFailure = failure.plus(another.getFailure());
        return new InventoryPatch<>(newItems, newDiff, newFailure);
    }

    public static <A> InventoryPatch<A> fromTakeResult(Map<Integer, A> modifiedItems, List<Map.Entry<Integer, A>> diff, @Nullable A remainingItem) {
        List<A> remainingItems = remainingItem != null
                ? List.of(remainingItem)
                : List.of();
        return new InventoryPatch<>(modifiedItems, diff, new InventoryFailure<>(remainingItems, List.of()));
    }

    public static <A> InventoryPatch<A> fromGiveResult(Map<Integer, A> modifiedItems, List<Map.Entry<Integer, A>> diff, @Nullable A giveLeftoverItem) {
        List<A> leftoverItems = giveLeftoverItem != null
                ? List.of(giveLeftoverItem)
                : List.of();
        return new InventoryPatch<>(modifiedItems, diff, new InventoryFailure<>(List.of(), leftoverItems));
    }
}
