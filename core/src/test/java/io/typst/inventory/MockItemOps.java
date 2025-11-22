package io.typst.inventory;

import org.jetbrains.annotations.Nullable;

public class MockItemOps implements ItemStackOps<MockItem> {
    @Override
    public boolean isEmpty(@Nullable MockItem item) {
        return item == null || item.equals(MockItem.empty);
    }

    @Override
    public ItemKey getKeyFrom(MockItem item) {
        return new ItemKey(item.getId(), item.getId());
    }

    @Override
    public int getAmount(MockItem item) {
        return item.getAmount();
    }

    @Override
    public void setAmount(MockItem item, int amount) {
        item.setAmount(amount);
    }

    @Override
    public int getMaxStackSize(MockItem item) {
        return 64;
    }

    @Override
    public MockItem copy(MockItem item) {
        return new MockItem(item);
    }

    @Override
    public @Nullable MockItem create(ItemKey key) {
        if (key.getId().isEmpty()) {
            return null;
        }
        return new MockItem(key.getId(), 1, 64);
    }

    @Override
    public MockItem empty() {
        return MockItem.empty;
    }

    @Override
    public boolean isSimilar(MockItem mockItem, MockItem b) {
        return mockItem.getId().equals(b.getId());
    }
}
