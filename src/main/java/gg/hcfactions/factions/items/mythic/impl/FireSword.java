package gg.hcfactions.factions.items.mythic.impl;

import com.google.common.collect.Maps;
import gg.hcfactions.cx.CXService;
import gg.hcfactions.cx.modules.player.combat.EnchantLimitModule;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.mythic.IMythicItem;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

public final class FireSword implements IMythicItem {
    @Getter public final Factions plugin;

    public FireSword(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public Material getMaterial() {
        return Material.NETHERITE_SWORD;
    }

    @Override
    public String getName() {
        return ChatColor.RED + "Fire Sword";
    }

    @Override
    public List<String> getLore() {
        return getMythicLore();
    }

    @Override
    public short getDurability() {
        return 400;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        final CXService cxService = (CXService)plugin.getService(CXService.class);
        final EnchantLimitModule enchantLimitModule = cxService.getEnchantLimitModule();
        final Map<Enchantment, Integer> enchantments = Maps.newHashMap();
        int sharpnessLevel = 5;

        if (enchantLimitModule.getEnchantLimits().containsKey(Enchantment.DAMAGE_ALL)) {
            sharpnessLevel = enchantLimitModule.getMaxEnchantmentLevel(Enchantment.DAMAGE_ALL);
        }

        enchantments.put(Enchantment.FIRE_ASPECT, 2);
        enchantments.put(Enchantment.DAMAGE_ALL, sharpnessLevel);

        return enchantments;
    }
}
