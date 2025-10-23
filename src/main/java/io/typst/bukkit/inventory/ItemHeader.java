package io.typst.bukkit.inventory;

import lombok.Value;
import lombok.With;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@Value
@With
public class ItemHeader {
    Material material;
    String name;

    public static ItemHeader from(ItemStack x) {
        ItemMeta meta = x.getItemMeta();
        String name = meta != null && meta.hasDisplayName()
                ? meta.getDisplayName()
                : "";
        return new ItemHeader(x.getType(), name);
    }

    public static Map<ItemHeader, ItemStack> fromItems(Collection<ItemStack> items) {
        Map<ItemHeader, ItemStack> ret = new HashMap<>();
        for (ItemStack item : items) {
            ItemHeader header = ItemHeader.from(item);
            ItemStack theItem = ret.get(header);
            ItemStack newItem = theItem != null ? theItem : new ItemStack(item);
            int theAmount = theItem != null ? theItem.getAmount() : 0;
            newItem.setAmount(newItem.getAmount() + theAmount);
            ret.put(header, newItem);
        }
        return ret;
    }

    public static Map<ItemHeader, ItemStack> fromInventory(Inventory inv) {
        List<ItemStack> items = Arrays.asList(inv.getContents());
        return fromItems(items);
    }
}
