package gg.hcfactions.factions.items.mythic.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.cx.CXService;
import gg.hcfactions.cx.modules.player.combat.EnchantLimitModule;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.mythic.IMythicItem;
import gg.hcfactions.factions.items.mythic.MythicAbility;
import gg.hcfactions.libs.bukkit.utils.Worlds;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public final class AdmiralsEmber implements IMythicItem {
    @Getter public final Factions plugin;
    @Getter public final List<MythicAbility> abilityInfo;

    public AdmiralsEmber(Factions plugin) {
        this.plugin = plugin;
        this.abilityInfo = Lists.newArrayList();
    }

    @Override
    public Material getMaterial() {
        return Material.DIAMOND_SWORD;
    }

    @Override
    public String getName() {
        return ChatColor.RED + "Admiral's Ember";
    }

    @Override
    public List<String> getLore() {
        return getMythicLore();
    }

    @Override
    public int getDurabilityCost() {
        return 4;
    }

    @Override
    public Particle getAbilityParticle() {
        return Particle.FLAME;
    }

    @Override
    public int getAbilityParticleFrames() {
        return 10;
    }

    @Override
    public int getAbilityParticleRate() {
        return 2;
    }

    @Override
    public double getAbilityParticleSpeed() {
        return 0.01;
    }

    @Override
    public int getAbilityParticleCount() {
        return 4;
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

        enchantments.put(Enchantment.FIRE_ASPECT, 3);
        enchantments.put(Enchantment.DAMAGE_ALL, sharpnessLevel);

        return enchantments;
    }

    @Override
    public void onAttack(Player player, LivingEntity attackedEntity) {
        if (attackedEntity.getFireTicks() == 0) {
            spawnAbilityParticles(player);
            Worlds.playSound(player.getLocation(), Sound.ENTITY_GHAST_SHOOT);
        }
    }
}
