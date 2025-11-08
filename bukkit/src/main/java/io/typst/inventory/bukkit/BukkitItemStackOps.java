package io.typst.inventory.bukkit;

import io.typst.inventory.ItemKey;
import io.typst.inventory.ItemStackOps;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class BukkitItemStackOps implements ItemStackOps<ItemStack> {
    public static final BukkitItemStackOps INSTANCE = new BukkitItemStackOps();
    private static final ItemStack EMPTY_ITEM = new ItemStack(Material.AIR);

    @Override
    public boolean isEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR || item.getAmount() <= 0;
    }

    @Override
    public ItemKey getKeyFrom(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        String name = meta != null && meta.hasDisplayName()
                ? meta.getDisplayName()
                : "";
        NamespacedKey key = item.getType().getKey();
        return new ItemKey(key.toString(), name);
    }

    @Override
    public Map<ItemKey, ItemStack> getHeaderMapFrom(Iterable<ItemStack> iterable) {
        Map<ItemKey, ItemStack> map = new HashMap<>();
        for (ItemStack item : iterable) {
            ItemKey header = getKeyFrom(item);
            ItemStack theItem = map.get(header);
            ItemStack newItem = theItem != null ? theItem : new ItemStack(item);
            int theAmount = theItem != null ? theItem.getAmount() : 0;
            newItem.setAmount(newItem.getAmount() + theAmount);
            map.put(header, newItem);
        }
        return map;
    }

    @Override
    public List<ItemStack> collapseItems(Collection<ItemStack> items) {
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

    @Override
    public int getAmount(ItemStack item) {
        return item.getAmount();
    }

    @Override
    public void setAmount(ItemStack item, int amount) {
        item.setAmount(amount);
    }

    @Override
    public ItemStack copy(ItemStack item) {
        return new ItemStack(item);
    }

    @Override
    public int getMaxStackSize(ItemStack item) {
        return item.getMaxStackSize();
    }

    @Override
    public ItemStack create(ItemKey key) {
        NamespacedKey namespacedKey = NamespacedKey.fromString(key.getId());
        Material material = namespacedKey != null
                ? Registry.MATERIAL.get(namespacedKey)
                : null;
        return material != null
                ? new ItemStack(material)
                : null;
    }

    @Override
    public ItemStack empty() {
        return EMPTY_ITEM;
    }

    @Override
    public boolean isSimilar(ItemStack itemStack, ItemStack b) {
        return false;
    }
}
