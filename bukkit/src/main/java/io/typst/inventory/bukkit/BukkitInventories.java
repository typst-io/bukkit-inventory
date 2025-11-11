package io.typst.inventory.bukkit;

import io.typst.inventory.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

// TODO: context -- ItemStackOps
public class BukkitInventories {
    public static InventoryMutator<ItemStack, Player> from(Inventory inventory) {
        ItemStackOps<ItemStack> itemOps = BukkitItemStackOps.INSTANCE;
        return new InventoryMutator<>(
                new BukkitInventoryAdapter(inventory, itemOps.empty()),
                itemOps,
                BukkitPlayerOps.INSTANCE,
                ItemKey.MINECRAFT_EMPTY
        );
    }

    public static InventoryMutator<ItemStack, Player> from(Map<Integer, ItemStack> map) {
        ItemStackOps<ItemStack> itemOps = BukkitItemStackOps.INSTANCE;
        return new InventoryMutator<>(
                new MapInventoryAdapter<>(map, itemOps.empty()),
                itemOps,
                BukkitPlayerOps.INSTANCE,
                ItemKey.MINECRAFT_EMPTY
        );
    }

    public static InventoryMutator<ItemStack, Player> from(List<ItemStack> list) {
        ItemStackOps<ItemStack> itemOps = BukkitItemStackOps.INSTANCE;
        return new InventoryMutator<>(
                new ListInventoryAdapter<>(list, itemOps.empty()),
                itemOps,
                BukkitPlayerOps.INSTANCE,
                ItemKey.MINECRAFT_EMPTY
        );
    }

    // TODO: 사용 클래스 하나만
    // TODO: transaction & commit
    // TODO: 고차함수 X
    public static void main(String[] args) {
        Inventory inventory = null;
        ItemStack item = null;


        if (from(inventory).giveItem(item)) {
            // do
        }
        if (from(inventory).toSnapshotView().hasItems(item)) {
            // do
        }
        if (from(inventory).takeItems(item)) {
            // do
        }

        InventoryMutator<ItemStack, Player> transaction = from(inventory).copy();
        if (transaction.takeItems(item) && transaction.giveItem(item)) {
            transaction.forEach(inventory::setItem);
        }
    }

    public static InventoryPatch<ItemStack> failurePatch() {
        return InventoryPatch.failure(BukkitItemStacks.getEmpty());
    }
}
