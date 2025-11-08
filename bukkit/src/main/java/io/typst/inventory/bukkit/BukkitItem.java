package io.typst.inventory.bukkit;

import lombok.Builder;
import lombok.Value;
import lombok.With;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.List;

@Value
@With
@Builder
public class BukkitItem {
    Material material;
    @Builder.Default
    int amount = 1;
    @Builder.Default
    String displayName = "";
    @Builder.Default
    List<String> lore = Collections.emptyList();
    @Builder.Default
    int customModelData = -1;

    public ItemStack create() {
        ItemStack newItem = new ItemStack(material, amount);
        if (!displayName.isEmpty() || !lore.isEmpty() || customModelData >= 0) {
            ItemMeta meta = newItem.hasItemMeta()
                    ? newItem.getItemMeta()
                    : null;
            if (meta != null) {
                if (customModelData >= 0) {
                    meta.setCustomModelData(customModelData);
                }
                if (!displayName.isEmpty()) {
                    meta.setDisplayName(displayName);
                }
                if (!lore.isEmpty()) {
                    meta.setLore(lore);
                }
                newItem.setItemMeta(meta);
            }
        }
        return newItem;
    }
}
