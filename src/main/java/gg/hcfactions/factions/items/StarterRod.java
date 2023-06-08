package gg.hcfactions.factions.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import gg.hcfactions.libs.bukkit.utils.Colors;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

public final class StarterRod implements ICustomItem {
    @Override
    public Material getMaterial() {
        return Material.FISHING_ROD;
    }

    @Override
    public String getName() {
        return Colors.GREEN.toBukkit() + "Welcome to Revival";
    }

    @Override
    public List<String> getLore() {
        final List<String> lore = Lists.newArrayList();
        lore.add(ChatColor.RESET + " ");
        lore.add(ChatColor.AQUA + "Use this fishing rod to gather some food");
        lore.add(ChatColor.AQUA + "before you enter out in to the world.");
        lore.add(ChatColor.RESET + " ");
        lore.add(ChatColor.DARK_AQUA + "Good luck, have fun!");
        lore.add(ChatColor.RESET + " " + ChatColor.GOLD + " - Revival Team");
        return lore;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        final Map<Enchantment, Integer> enchantments = Maps.newHashMap();
        enchantments.put(Enchantment.LURE, 1);
        enchantments.put(Enchantment.DURABILITY, 1);
        return enchantments;
    }

    @Override
    public boolean isSoulbound() {
        return true;
    }
}
