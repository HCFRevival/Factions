package gg.hcfactions.factions.models.stats.impl.item;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import gg.hcfactions.factions.models.stats.ITrackedItem;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class TrackedWeapon implements ITrackedItem<TrackedWeapon> {
    public static final ImmutableSet<Material> VALID_ITEMS = ImmutableSet.of(
            Material.WOODEN_AXE,
            Material.WOODEN_SWORD,
            Material.STONE_AXE,
            Material.STONE_SWORD,
            Material.IRON_AXE,
            Material.IRON_SWORD,
            Material.GOLDEN_AXE,
            Material.GOLDEN_SWORD,
            Material.DIAMOND_AXE,
            Material.DIAMOND_SWORD,
            Material.NETHERITE_AXE,
            Material.NETHERITE_SWORD,
            Material.TRIDENT
    );

    @Getter public ItemStack item;
    @Getter public List<String> entries;
    @Getter @Setter public int killCount;

    public TrackedWeapon() {
        this.item = null;
        this.entries = Lists.newArrayList();
        this.killCount = 0;
    }

    /**
     * Add a new kill entry to this tracked sword
     * @param killerUsername Killer username
     * @param slainUsername Slain username
     */
    public void addEntry(String killerUsername, String slainUsername) {
        if (entries.size() >= 10) {
            entries.remove(9);
        }

        this.killCount += 1;

        entries.add(0, ChatColor.GOLD + slainUsername + ChatColor.RED + " slain by " + ChatColor.GOLD + killerUsername);
    }

    @Override
    public TrackedWeapon fromItem(ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return this;
        }

        this.item = item;

        final List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) {
            return this;
        }

        final String killLine = lore.get(0);

        try {
            killCount = Integer.parseInt(killLine.replace(ChatColor.DARK_RED + "Kills" + ChatColor.RED + ": ", ""));
        } catch (NumberFormatException ex) {
            return this;
        }

        // Found kill entries
        if (lore.size() >= 3) {
            for (int i = 2; i < lore.size(); i++) {
                entries.add(lore.get(i));
            }
        }

        return this;
    }

    @Override
    public void updateItem() {
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        final List<String> lore = Lists.newArrayList();
        lore.add(ChatColor.DARK_RED + "Kills" + ChatColor.RED + ": " + killCount);

        if (!entries.isEmpty()) {
            lore.add(ChatColor.RESET + " ");
            lore.addAll(entries);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
    }
}
