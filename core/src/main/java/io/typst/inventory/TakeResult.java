package io.typst.inventory;

import lombok.Value;
import lombok.With;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Value
@With
public class TakeResult<A> {
    Map<Integer, A> modifiedItems;
    /**
     * remainingCount is the part of the requested amount that could not be taken.
     */
    int remainingCount;
    @SuppressWarnings("rawtypes")
    private static final TakeResult EMPTY = new TakeResult<>(Collections.emptyMap(), 0);

    @SuppressWarnings("unchecked")
    public static <A> TakeResult<A> empty() {
        return (TakeResult<A>) EMPTY;
    }

    public TakeResult<A> plus(TakeResult<A> another) {
        Map<Integer, A> newItems = new LinkedHashMap<>(modifiedItems);
        newItems.putAll(another.modifiedItems);
        int newRemainingCount = remainingCount + another.getRemainingCount();
        return new TakeResult<>(Map.copyOf(newItems), newRemainingCount);
    }
}
