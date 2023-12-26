package io.typecraft.bukkit.inventory;

import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ItemStacks {
    public static ItemStack maximize(ItemStack item) {
        ItemStack newItem = item.clone();
        newItem.setAmount(item.getType().getMaxStackSize());
        return newItem;
    }

    public static List<ItemStack> normalize(Collection<ItemStack> items) {
        Map<ItemStack, Integer> map = new HashMap<>(items.size());
        for (ItemStack item : items) {
            ItemStack newItem = item.clone();
            newItem.setAmount(1);
            map.put(newItem, map.getOrDefault(newItem, 0) + item.getAmount());
        }
        List<ItemStack> ret = new ArrayList<>(map.size());
        for (Map.Entry<ItemStack, Integer> pair : map.entrySet()) {
            pair.getKey().setAmount(pair.getValue());
            ret.add(pair.getKey());
        }
        return ret;
    }
}
