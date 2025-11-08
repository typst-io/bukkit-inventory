package io.typst.inventory.bukkit;

import io.typst.inventory.EntityOps;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BukkitPlayerOps implements EntityOps<Player, ItemStack> {
    public static final BukkitPlayerOps INSTANCE = new BukkitPlayerOps();

    @Override
    public void dropItem(Player entity, ItemStack item) {
        entity.getWorld().dropItem(entity.getEyeLocation(), item);
    }
}
