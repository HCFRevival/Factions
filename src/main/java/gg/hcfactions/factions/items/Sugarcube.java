package gg.hcfactions.factions.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

public final class Sugarcube implements ICustomItem {
    @Override
    public Material getMaterial() {
        return Material.WHITE_CONCRETE_POWDER;
    }

    @Override
    public String getName() {
        return ChatColor.AQUA + "Sugar Cube";
    }

    @Override
    public List<String> getLore() {
        final List<String> res = Lists.newArrayList();

        res.add(ChatColor.GRAY + "Right-click a Horse while holding this item");
        res.add(ChatColor.GRAY + "to upgrade its movement speed");

        return res;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return Maps.newHashMap();
    }
}
