package io.typst.inventory;

import lombok.Value;
import lombok.With;

@Value
@With
public class ItemKey {
    String id;
    String name;

    public static final ItemKey MINECRAFT_EMPTY = new ItemKey("minecraft:air", "");
}
