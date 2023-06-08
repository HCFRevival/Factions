package gg.hcfactions.factions.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

public final class Crowbar implements ICustomItem {
    public static final String MONSTER_SPAWNER_PREFIX = ChatColor.YELLOW + "Monster Spawners: " + ChatColor.BLUE;
    public static final String END_PORTAL_PREFIX = ChatColor.YELLOW + "End Portal Frames: " + ChatColor.BLUE;

    @Override
    public Material getMaterial() {
        return Material.DIAMOND_HOE;
    }

    @Override
    public String getName() {
        return ChatColor.DARK_RED + "Crowbar";
    }

    @Override
    public List<String> getLore() {
        final List<String> lore = Lists.newArrayList();
        lore.add(MONSTER_SPAWNER_PREFIX + 1);
        lore.add(END_PORTAL_PREFIX + 6);
        return lore;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return Maps.newHashMap();
    }

    @Override
    public boolean isRepairable() {
        return false;
    }

    @Override
    public boolean isSoulbound() {
        return false;
    }
}
