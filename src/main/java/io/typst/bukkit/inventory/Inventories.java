package io.typst.bukkit.inventory;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;

public class Inventories {
    private static boolean checkEmpty(ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    public static void giveItemOrDrop(HumanEntity player, ItemStack item) {
        if (checkEmpty(item)) {
            return;
        }
        player.getInventory().addItem(item)
                .forEach((__, failItem) -> player.getWorld().dropItem(player.getEyeLocation(), failItem));
    }

    // return the given amount
    public static int giveItem(ListIterator<ItemStack> iterator, ItemStack x) {
        int giveAmount = x.getAmount();
        while (iterator.hasNext()) {
            if (giveAmount <= 0) {
                break;
            }
            ItemStack item = iterator.next();
            if (checkEmpty(item)) {
                ItemStack newItem = x.clone();
                newItem.setAmount(Math.min(giveAmount, x.getType().getMaxStackSize()));
                iterator.set(newItem);
                giveAmount -= newItem.getAmount();
            } else if (item.isSimilar(x)) {
                int newAmount = Math.min(item.getAmount() + giveAmount, item.getType().getMaxStackSize());
                int givenAmount = newAmount - item.getAmount();
                if (givenAmount >= 1) {
                    item.setAmount(newAmount);
                    giveAmount -= givenAmount;
                }
            }
        }
        return x.getAmount() - giveAmount;
    }

    public static int hasItems(Iterator<ItemStack> iterator, ItemStack x) {
        if (checkEmpty(x)) {
            return 0;
        }
        int amount = 0;
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (checkEmpty(item)) continue;
            if (item.isSimilar(x)) {
                amount += item.getAmount();
            }
        }
        return amount;
    }

    public static int hasItems(Iterator<ItemStack> iterator, ItemHeader header) {
        int hasAmount = 0;
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (checkEmpty(item)) continue;
            ;
            if (ItemHeader.from(item).equals(header)) {
                hasAmount += item.getAmount();
            }
        }
        return hasAmount;
    }

    public static boolean hasSpace(Iterator<ItemStack> iterator, ItemStack x) {
        if (checkEmpty(x)) {
            return false;
        }
        int amount = 0;
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (checkEmpty(item)) {
                amount += x.getType().getMaxStackSize();
            } else if (item.isSimilar(x)) {
                amount += Math.max(x.getType().getMaxStackSize() - item.getAmount(), 0);
            }
            if (amount >= x.getAmount()) {
                return true;
            }
        }
        return false;
    }

    // return the taken amount
    public static int takeItem(ListIterator<ItemStack> iterator, ItemStack itemToTake) {
        if (itemToTake == null || itemToTake.getType() == Material.AIR) {
            return 0;
        }
        int amount = itemToTake.getAmount();
        int takeAmount = amount;
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (checkEmpty(item) || !item.isSimilar(itemToTake)) continue;
            if (takeAmount <= 0) {
                break;
            }
            if (item.getAmount() > takeAmount) {
                item.setAmount(item.getAmount() - takeAmount);
                takeAmount = 0;
                break;
            } else if (item.getAmount() == takeAmount) {
                iterator.set(null);
                takeAmount = 0;
                break;
            } else {
                iterator.set(null);
                takeAmount -= item.getAmount();
            }
        }
        return amount - takeAmount;
    }

    public static Map.Entry<Integer, List<ItemStack>> takeItem(ListIterator<ItemStack> iterator, ItemHeader header, int count) {
        if (header.getMaterial() == Material.AIR) {
            return new SimpleEntry<>(0, Collections.emptyList());
        }
        List<ItemStack> takenItems = new ArrayList<>();
        int dec = count;
        while (iterator.hasNext()) {
            ItemStack item = iterator.next();
            if (checkEmpty(item) || !ItemHeader.from(item).equals(header)) continue;
            if (dec <= 0) {
                break;
            }
            int amount = item.getAmount();
            item.setAmount(Math.max(amount - dec, 0));
            dec = Math.max(dec - amount, 0);
            int takenAmount = Math.max(amount, dec);
            if (takenAmount >= 1) {
                ItemStack takenItem = new ItemStack(item);
                takenItem.setAmount(takenAmount);
                takenItems.add(takenItem);
            }
        }
        return new SimpleEntry<>(count - dec, takenItems);
    }
}
