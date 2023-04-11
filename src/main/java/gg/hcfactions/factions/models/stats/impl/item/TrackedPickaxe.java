package gg.hcfactions.factions.models.stats.impl.item;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.models.stats.ITrackedItem;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public final class TrackedPickaxe implements ITrackedItem<TrackedPickaxe> {
    public static final ImmutableSet<Material> VALID_ITEMS = ImmutableSet.of(
            Material.WOODEN_PICKAXE,
            Material.STONE_PICKAXE,
            Material.IRON_PICKAXE,
            Material.GOLDEN_PICKAXE,
            Material.DIAMOND_PICKAXE,
            Material.NETHERITE_PICKAXE
    );

    public static final ImmutableMap<Material, Material> MAT_CONVERSIONS =
            new ImmutableMap.Builder<Material, Material>()
                    .put(Material.DEEPSLATE_COAL_ORE, Material.COAL_ORE)
                    .put(Material.DEEPSLATE_IRON_ORE, Material.IRON_ORE)
                    .put(Material.DEEPSLATE_REDSTONE_ORE, Material.REDSTONE_ORE)
                    .put(Material.DEEPSLATE_LAPIS_ORE, Material.LAPIS_ORE)
                    .put(Material.DEEPSLATE_GOLD_ORE, Material.GOLD_ORE)
                    .put(Material.DEEPSLATE_DIAMOND_ORE, Material.DIAMOND_ORE)
                    .put(Material.DEEPSLATE_EMERALD_ORE, Material.EMERALD_ORE)
                    .build();

    @Getter public ItemStack item;
    @Getter public final Map<Material, Integer> values;

    public TrackedPickaxe() {
        this.item = null;
        this.values = Maps.newHashMap();
        
        this.values.put(Material.COAL_ORE, 0);
        this.values.put(Material.IRON_ORE, 0);
        this.values.put(Material.REDSTONE_ORE, 0);
        this.values.put(Material.LAPIS_ORE, 0);
        this.values.put(Material.GOLD_ORE, 0);
        this.values.put(Material.DIAMOND_ORE, 0);
        this.values.put(Material.EMERALD_ORE, 0);
        this.values.put(Material.ANCIENT_DEBRIS, 0);
    }

    public void addOre(Material type, int amount) {
        if (MAT_CONVERSIONS.containsKey(type)) {
            type = MAT_CONVERSIONS.get(type);
        }

        if (!values.containsKey(type)) {
            throw new IllegalArgumentException("bad material type for tracked pickaxe (" + type.name() + ")");
        }
        
        final int existing = values.getOrDefault(type, 0);
        values.put(type, existing + amount);
    }

    @Override
    public TrackedPickaxe fromItem(ItemStack item) {
        final ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return this;
        }
        
        this.item = item;

        final List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) {
            return this;
        }

        for (String entry : lore) {
            try {
                if (entry.contains(ChatColor.DARK_GRAY + "Coal")) {
                    values.put(Material.COAL_ORE, Integer.parseInt(entry.replace(ChatColor.DARK_GRAY + "Coal" + ChatColor.YELLOW + ": ", "")));
                }

                if (entry.contains(ChatColor.GRAY + "Iron")) {
                    values.put(Material.IRON_ORE, Integer.parseInt(entry.replace(ChatColor.GRAY + "Iron" + ChatColor.YELLOW + ": ", "")));
                }

                if (entry.contains(ChatColor.RED + "Redstone")) {
                    values.put(Material.REDSTONE_ORE, Integer.parseInt(entry.replace(ChatColor.RED + "Redstone" + ChatColor.YELLOW + ": ", "")));
                }

                if (entry.contains(ChatColor.BLUE + "Lapis")) {
                    values.put(Material.LAPIS_ORE, Integer.parseInt(entry.replace(ChatColor.BLUE + "Lapis" + ChatColor.YELLOW + ": ", "")));
                }

                if (entry.contains(ChatColor.GOLD + "Gold")) {
                    values.put(Material.GOLD_ORE, Integer.parseInt(entry.replace(ChatColor.GOLD + "Gold" + ChatColor.YELLOW + ": ", "")));
                }

                if (entry.contains(ChatColor.AQUA + "Diamond")) {
                    values.put(Material.DIAMOND_ORE, Integer.parseInt(entry.replace(ChatColor.AQUA + "Diamond" + ChatColor.YELLOW + ": ", "")));
                }

                if (entry.contains(ChatColor.GREEN + "Emerald")) {
                    values.put(Material.EMERALD_ORE, Integer.parseInt(entry.replace(ChatColor.GREEN + "Emerald" + ChatColor.YELLOW + ": ", "")));
                }

                if (entry.contains("Ancient Debris")) {
                    // We have to use a funky method to circumvent custom RGB color codes
                    values.put(Material.ANCIENT_DEBRIS, Integer.parseInt(entry.split(": ")[1].replaceAll("\\D", "")));
                }
            } catch (NumberFormatException ignored) {}
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

        lore.add(ChatColor.DARK_GRAY + "Coal" + ChatColor.YELLOW + ": " + values.getOrDefault(Material.COAL_ORE, 0));
        lore.add(ChatColor.GRAY + "Iron" + ChatColor.YELLOW + ": " + values.getOrDefault(Material.IRON_ORE, 0));
        lore.add(ChatColor.RED + "Redstone" + ChatColor.YELLOW + ": " + values.getOrDefault(Material.REDSTONE_ORE, 0));
        lore.add(ChatColor.BLUE + "Lapis" + ChatColor.YELLOW + ": " + values.getOrDefault(Material.LAPIS_ORE, 0));
        lore.add(ChatColor.GOLD + "Gold" + ChatColor.YELLOW + ": " + values.getOrDefault(Material.GOLD_ORE, 0));
        lore.add(ChatColor.AQUA + "Diamond" + ChatColor.YELLOW + ": " + values.getOrDefault(Material.DIAMOND_ORE, 0));
        lore.add(ChatColor.GREEN + "Emerald" + ChatColor.YELLOW + ": " + values.getOrDefault(Material.EMERALD_ORE, 0));
        lore.add(ChatColor.of("#3d222e") + "Ancient Debris" + ChatColor.YELLOW + ": " + values.getOrDefault(Material.ANCIENT_DEBRIS, 0));

        meta.setLore(lore);
        item.setItemMeta(meta);
    }
}
