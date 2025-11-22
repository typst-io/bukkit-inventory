package io.typst.inventory;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class MockItem {
    String id;
    int amount;
    int maxStack;

    public MockItem(String id, int amount, int maxStack) {
        this.id = id;
        this.amount = amount;
        this.maxStack = maxStack;
    }

    public MockItem(MockItem item) {
        this(item.getId(), item.getAmount(), item.getMaxStack());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MockItem mockItem = (MockItem) o;
        return amount == mockItem.amount && maxStack == mockItem.maxStack && Objects.equals(id, mockItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, maxStack);
    }

    public static ItemKey defaultKey = new ItemKey("air", "air");
    public static MockItem empty = new MockItem("air", 1, 1);
}
