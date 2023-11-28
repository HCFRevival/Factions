package gg.hcfactions.factions.items.mythic.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.items.mythic.IMythicItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

public final class KnockbackSword implements IMythicItem {
    @Override
    public Material getMaterial() {
        return Material.IRON_SWORD;
    }

    @Override
    public String getName() {
        return ChatColor.DARK_AQUA + "Knockback Sword";
    }

    @Override
    public List<String> getLore() {
        return getMythicLore();
    }

    @Override
    public short getDurability() {
        return 225;
    }

    @Override
    public boolean isRepairable() {
        return false;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        final Map<Enchantment, Integer> enchantments = Maps.newHashMap();
        enchantments.put(Enchantment.KNOCKBACK, 2);
        return enchantments;
    }
}
