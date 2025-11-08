package io.typst.inventory;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ItemStackOps<A> {
    boolean isEmpty(@Nullable A item);

    ItemKey getKeyFrom(A item);

    Map<ItemKey, A> getHeaderMapFrom(Iterable<A> iterable);

    List<A> collapseItems(Collection<A> items);

    int getAmount(A item);

    void setAmount(A item, int amount);

    int getMaxStackSize(A item);

    A copy(A item);

    @Nullable
    A create(ItemKey key);

    A empty();

    boolean isSimilar(A a, A b);
}
