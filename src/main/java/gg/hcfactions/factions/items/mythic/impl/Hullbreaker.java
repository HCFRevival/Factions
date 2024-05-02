package gg.hcfactions.factions.items.mythic.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.cx.CXService;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.mythic.EMythicAbilityType;
import gg.hcfactions.factions.items.mythic.IMythicItem;
import gg.hcfactions.factions.items.mythic.MythicAbility;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.utils.FactionUtil;
import gg.hcfactions.factions.utils.StringUtil;
import gg.hcfactions.libs.bukkit.utils.Players;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class Hullbreaker implements IMythicItem {
    public record HullbreakerConfig(
            @Getter int resistanceDuration,
            @Getter int resistanceAmplifier,
            @Getter int requiredAllyDistance,
            @Getter int requiredEnemyCount) {}

    public static final TextColor HULLBREAKER_COLOR = NamedTextColor.GOLD;

    @Getter public final Factions plugin;
    @Getter public final List<MythicAbility> abilityInfo;
    private final HullbreakerConfig config;

    public Hullbreaker(Factions plugin, HullbreakerConfig config) {
        this.plugin = plugin;
        this.abilityInfo = Lists.newArrayList();
        this.config = config;

        addAbilityInfo(
                ChatColor.GOLD + "Last Man Standing",
                "Attacking any enemy with " + config.getRequiredEnemyCount() + " enemies nearby and no nearby allies grants you Resistance "
                        + StringUtil.getRomanNumeral(config.getResistanceAmplifier() + 1) + " for " + config.resistanceDuration() + " seconds.",
                EMythicAbilityType.ON_HIT
        );
    }

    @Override
    public Material getMaterial() {
        return Material.NETHERITE_SWORD;
    }

    @Override
    public String getName() {
        return ChatColor.GOLD + "Hullbreaker";
    }

    @Override
    public List<String> getLore() {
        return List.of();
    }

    @Override
    public List<Component> getLoreComponents() {
        return getMythicLore();
    }

    @Override
    public String getMythicName() {
        return "Hullbreaker";
    }

    @Override
    public TextColor getColor() {
        return HULLBREAKER_COLOR;
    }

    @Override
    public Particle getAbilityParticle() {
        return Particle.WAX_OFF;
    }

    @Override
    public int getAbilityParticleFrames() {
        return 20;
    }

    @Override
    public int getAbilityParticleRate() {
        return 4;
    }

    @Override
    public int getAbilityParticleCount() {
        return 10;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        final CXService cxs = (CXService) plugin.getService(CXService.class);
        final Map<Enchantment, Integer> enchantments = Maps.newHashMap();
        final int sharpLevel = cxs.getEnchantLimitModule().getMaxEnchantmentLevel(Enchantment.SHARPNESS);

        enchantments.put(Enchantment.SHARPNESS, (sharpLevel == -1 ? 5 : sharpLevel));
        return enchantments;
    }

    @Override
    public void onAttack(Player player, LivingEntity attackedEntity) {
        if (!(attackedEntity instanceof Player)) {
            return;
        }

        if (
                player.hasPotionEffect(PotionEffectType.RESISTANCE)
                && Objects.requireNonNull(player.getPotionEffect(PotionEffectType.RESISTANCE)).getDuration() >= config.getResistanceDuration()) {
            return;
        }

        if (!FactionUtil.getNearbyFriendlies(plugin, player, config.getRequiredAllyDistance()).isEmpty()) {
            return;
        }

        final List<Player> nearbyEnemies = FactionUtil.getNearbyEnemies(plugin, player, config.getRequiredAllyDistance());
        if (nearbyEnemies.size() < config.getRequiredEnemyCount()) {
            return;
        }

        final List<Player> nearbyAllies = FactionUtil.getNearbyFriendlies(plugin, player, config.getRequiredAllyDistance());
        if (!nearbyAllies.isEmpty()) {
            return;
        }

        spawnAbilityParticles(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, config.getResistanceDuration()*20, config.getResistanceAmplifier()));
        Players.playSound(player, Sound.AMBIENT_NETHER_WASTES_MOOD);
        FMessage.printHullbreaker(player, config.getResistanceDuration());
    }
}
