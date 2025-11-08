package io.typst.inventory;

import lombok.Value;
import lombok.With;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Value
@With
public class GiveResult<A> {
    Map<Integer, A> modifiedItems;
    @Nullable
    A leftoverItem;
    @SuppressWarnings("rawtypes")
    private static final GiveResult EMPTY = new GiveResult<>(Collections.emptyMap(), null);

    public Optional<A> getLeftoverItemOptional() {
        return Optional.ofNullable(getLeftoverItem());
    }

    @SuppressWarnings("unchecked")
    public static <A> GiveResult<A> empty() {
        return (GiveResult<A>) EMPTY;
    }
}
