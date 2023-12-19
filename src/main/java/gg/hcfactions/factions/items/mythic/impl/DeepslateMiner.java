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

public final class DeepslateMiner implements IMythicItem {
    @Getter public final Factions plugin;
    @Getter public final List<MythicAbility> abilityInfo;

    public DeepslateMiner(Factions plugin) {
        this.plugin = plugin;
        this.abilityInfo = Lists.newArrayList();
    }

    @Override
    public Material getMaterial() {
        return Material.NETHERITE_PICKAXE;
    }

    @Override
    public String getName() {
        return ChatColor.DARK_GRAY + "Deepslate Miner";
    }

    @Override
    public List<String> getLore() {
        return getMythicLore();
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        final Map<Enchantment, Integer> enchantments = Maps.newHashMap();
        enchantments.put(Enchantment.DIG_SPEED, 8);
        enchantments.put(Enchantment.DURABILITY, 3);
        enchantments.put(Enchantment.MENDING, 1);
        return enchantments;
    }
}
