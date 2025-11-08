package io.typst.inventory;

public interface EntityOps<E, I> {
    void dropItem(E entity, I item);
}
