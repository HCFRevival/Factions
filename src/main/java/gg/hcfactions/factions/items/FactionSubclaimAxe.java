package gg.hcfactions.factions.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

public final class FactionSubclaimAxe implements ICustomItem {
    @Override
    public Material getMaterial() {
        return Material.GOLDEN_AXE;
    }

    @Override
    public String getName() {
        return ChatColor.GOLD + "Subclaim Creator";
    }

    @Override
    public List<String> getLore() {
        final List<String> lore = Lists.newArrayList();

        lore.add(ChatColor.GOLD + "Left-click" + ChatColor.YELLOW + " to set " + ChatColor.BLUE + "Corner A");
        lore.add(ChatColor.GOLD + "Right-click" + ChatColor.YELLOW + " to set " + ChatColor.BLUE + "Corner B");
        lore.add(ChatColor.RESET + " ");
        lore.add(ChatColor.AQUA + "With both corners set,");
        lore.add(ChatColor.AQUA + "Left-click " + ChatColor.AQUA + "" + ChatColor.UNDERLINE + "while sneaking" + ChatColor.AQUA + " to confirm");
        lore.add(ChatColor.RESET + " ");
        lore.add(ChatColor.RED + "To cancel the claiming process,");
        lore.add(ChatColor.RED + "Right-click " + ChatColor.RED + "" + ChatColor.UNDERLINE + "while sneaking" + ChatColor.RESET);

        return lore;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return Maps.newHashMap();
    }

    @Override
    public boolean isSoulbound() {
        return true;
    }
}
