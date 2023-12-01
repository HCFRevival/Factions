package gg.hcfactions.factions.items.mythic.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.mythic.IMythicItem;
import gg.hcfactions.factions.items.mythic.MythicAbility;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

public final class SymsSong implements IMythicItem {
    @Getter public final Factions plugin;
    @Getter public final List<MythicAbility> abilityInfo;

    public SymsSong(Factions plugin) {
        this.plugin = plugin;
        this.abilityInfo = Lists.newArrayList();
    }

    @Override
    public Material getMaterial() {
        return Material.DIAMOND_SWORD;
    }

    @Override
    public String getName() {
        return ChatColor.DARK_AQUA + "Sym's Song";
    }

    @Override
    public List<String> getLore() {
        return getMythicLore();
    }

    @Override
    public boolean isRepairable() {
        return false;
    }

    @Override
    public int getDurabilityCost() {
        return 5;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        final Map<Enchantment, Integer> enchantments = Maps.newHashMap();
        enchantments.put(Enchantment.KNOCKBACK, 2);
        return enchantments;
    }
}
