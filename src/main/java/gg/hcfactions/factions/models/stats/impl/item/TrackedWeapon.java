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
    public static final String KILL_HEADER_PREFIX = ChatColor.DARK_RED + "Kills" + ChatColor.RED + ": ";

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
            Material.TRIDENT,
            Material.MACE
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
        this.killCount += 1;
        entries.add(0, ChatColor.GOLD + slainUsername + ChatColor.RED + " slain by " + ChatColor.GOLD + killerUsername);
    }

    private boolean hasEntryLore(List<String> itemLore) {
        return itemLore.stream().anyMatch(entry -> entry.startsWith(KILL_HEADER_PREFIX));
    }

    private List<String> getEntriesFromLore(List<String> itemLore) {
        if (!hasEntryLore(itemLore)) {
            return Lists.newArrayList();
        }

        for (int i = 0; i < itemLore.size(); i++) {
            final String entry = itemLore.get(i);

            if (entry.startsWith(KILL_HEADER_PREFIX)) {
                return itemLore.subList(i + 1, itemLore.size());
            }
        }

        return Lists.newArrayList();
    }

    private int getHeaderIndex(List<String> entries) {
        for (int i = 0; i < entries.size(); i++) {
            final String line = entries.get(i);
            if (line.startsWith(KILL_HEADER_PREFIX)) {
                 return i;
            }
        }

        return -1;
    }

    private int getKillCount(List<String> itemLore) {
        final int index = getHeaderIndex(itemLore);

        if (index == -1) {
            return 0;
        }

        try {
            return Integer.parseInt(ChatColor.stripColor(itemLore.get(index).replace(KILL_HEADER_PREFIX, "")));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public TrackedWeapon fromItem(ItemStack item) {
        this.item = item;

        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return this;
        }

        final List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) {
            return this;
        }

        this.entries = getEntriesFromLore(lore);
        this.killCount = getKillCount(lore);
        return this;
    }

    @Override
    public void updateItem() {
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }

        final List<String> result = Lists.newArrayList();

        if (meta.getLore() == null) {
            result.add(KILL_HEADER_PREFIX + killCount);
            result.addAll(entries);
            meta.setLore(result);
            item.setItemMeta(meta);
            return;
        }

        final int headerIndex = getHeaderIndex(meta.getLore());
        int entryLimit = 10;

        // lore found but no kill header
        if (headerIndex == -1) {
            result.addAll(meta.getLore());
            result.add(ChatColor.RESET + " ");
        } else if (headerIndex > 0) {
            // lore found and kill header exists
            final List<String> beforeEntries = meta.getLore().subList(0, (headerIndex - 1));
            result.addAll(beforeEntries);
            result.add(ChatColor.RESET + " ");

            // Prevents extremely long lore (like mythics)
            if (beforeEntries.size() >= 5) {
                entryLimit = 5;
            }
        }

        result.add(KILL_HEADER_PREFIX + killCount);
        result.addAll(entries.size() > entryLimit ? entries.subList(0, entryLimit) : entries);

        if (entries.size() > entryLimit) {
            final int remainder = entries.size() - entryLimit;
            result.add(ChatColor.GRAY + "...and " + remainder + " more");
        }

        meta.setLore(result);
        item.setItemMeta(meta);
    }
}
