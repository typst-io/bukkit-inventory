package io.typst.inventory;

import lombok.Value;
import lombok.With;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Aggregates failure information from inventory operations.
 *
 * <ul>
 *   <li>{@code takeRemainingItems}: items that could not be fully taken,</li>
 *   <li>{@code giveLeftoverItems}: items that could not be fully inserted.</li>
 * </ul>
 *
 * <p>Instances are immutable and can be combined via {@link #plus(InventoryFailure)},
 * which concatenates both lists.</p>
 */
@Value
@With
public class InventoryFailure<A> {
    List<A> takeRemainingItems;
    List<A> giveLeftoverItems;
    @SuppressWarnings("rawtypes")
    private static final InventoryFailure EMPTY = new InventoryFailure<>(List.of(), List.of());

    public InventoryFailure(List<A> takeRemainingItems, List<A> giveLeftoverItems) {
        this.takeRemainingItems = List.copyOf(new LinkedList<>(takeRemainingItems));
        this.giveLeftoverItems = List.copyOf(new LinkedList<>(giveLeftoverItems));
    }

    @SuppressWarnings("unchecked")
    public static <A> InventoryFailure<A> empty() {
        return (InventoryFailure<A>) EMPTY;
    }

    public static <A> InventoryFailure<A> failure(A emptyItem) {
        return InventoryFailure.<A>empty().withGiveLeftoverItems(Collections.singletonList(emptyItem));
    }

    public InventoryFailure<A> plus(InventoryFailure<A> another) {
        List<A> newTakeRemainingItems = new LinkedList<>(takeRemainingItems);
        newTakeRemainingItems.addAll(another.getTakeRemainingItems());
        List<A> newGiveLeftoverItems = new LinkedList<>(giveLeftoverItems);
        newGiveLeftoverItems.addAll(another.getGiveLeftoverItems());
        return new InventoryFailure<>(newTakeRemainingItems, newGiveLeftoverItems);
    }

    public boolean isEmpty() {
        return getGiveLeftoverItems().isEmpty() && getTakeRemainingItems().isEmpty();
    }
}
