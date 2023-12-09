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
import gg.hcfactions.libs.bukkit.utils.Colors;
import gg.hcfactions.libs.bukkit.utils.Players;
import gg.hcfactions.libs.bukkit.utils.Worlds;
import lombok.Getter;
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
import java.util.Random;

public final class Ghostblade implements IMythicItem {
    public record GhostbladeConfig(@Getter int killEffectDuration,
                                   @Getter int killEffectRange,
                                   @Getter int refreshEffectMinDuration,
                                   @Getter int refreshEffectMaxDuration,
                                   @Getter float refreshChance) {}

    @Getter public final Factions plugin;
    @Getter public final List<MythicAbility> abilityInfo;
    private final Random random;
    private final GhostbladeConfig config;

    public Ghostblade(Factions plugin, GhostbladeConfig ghostbladeConfig) {
        this.plugin = plugin;
        this.random = new Random();
        this.config = ghostbladeConfig;
        this.abilityInfo = Lists.newArrayList();

        addAbilityInfo(
                Colors.LAVENDAR.toBukkit() + "Excited",
                ChatColor.GRAY + "Slaying an enemy will grant you and nearby faction members Speed III and Haste II for " + ghostbladeConfig.killEffectDuration() + " seconds.",
                EMythicAbilityType.ON_KILL);

        addAbilityInfo(
                ChatColor.WHITE + "Refreshed",
                ChatColor.GRAY + "Attacking an enemy has a 5% chance to extend your active Speed effect by "
                        + ghostbladeConfig.refreshEffectMinDuration() + "-" + ghostbladeConfig.refreshEffectMaxDuration() + " seconds.",
                EMythicAbilityType.ON_HIT
        );
    }

    @Override
    public Material getMaterial() {
        return Material.NETHERITE_SWORD;
    }

    @Override
    public String getName() {
        return ChatColor.LIGHT_PURPLE + "Ghostblade";
    }

    @Override
    public List<String> getLore() {
        return getMythicLore();
    }

    @Override
    public Particle getAbilityParticle() {
        return Particle.CHERRY_LEAVES;
    }

    @Override
    public int getAbilityParticleCount() {
        return 12;
    }

    @Override
    public int getDurabilityCost() {
        return 3;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        final CXService cxs = (CXService) plugin.getService(CXService.class);
        final Map<Enchantment, Integer> enchantments = Maps.newHashMap();
        final int sharpLevel = cxs.getEnchantLimitModule().getMaxEnchantmentLevel(Enchantment.DAMAGE_ALL);

        enchantments.put(Enchantment.DAMAGE_ALL, (sharpLevel == -1 ? 5 : sharpLevel));
        return enchantments;
    }

    @Override
    public void onKill(Player player, LivingEntity slainEntity) {
        if (!(slainEntity instanceof Player)) {
            return;
        }

        final PotionEffect speedEffect = new PotionEffect(PotionEffectType.SPEED, config.killEffectDuration()*20, 2);
        final PotionEffect hasteEffect = new PotionEffect(PotionEffectType.FAST_DIGGING, config.killEffectDuration()*20, 0);

        spawnAbilityParticles(player);
        Players.giveTemporaryEffect(plugin, player, speedEffect);
        Players.giveTemporaryEffect(plugin, player, hasteEffect);
        Worlds.playSound(player.getLocation(), Sound.ENTITY_ALLAY_ITEM_THROWN);
        FMessage.printGhostbladeKill(player, player);

        FactionUtil.getNearbyFriendlies(plugin, player, config.getKillEffectRange()).forEach(nearbyFriendly -> {
            spawnAbilityParticles(nearbyFriendly);
            Players.giveTemporaryEffect(plugin, nearbyFriendly, speedEffect);
            Players.giveTemporaryEffect(plugin, nearbyFriendly, hasteEffect);
            FMessage.printGhostbladeKill(nearbyFriendly, player);
        });
    }

    @Override
    public void onAttack(Player player, LivingEntity attackedEntity) {
        if (!(attackedEntity instanceof Player)) {
            return;
        }

        if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
            return;
        }

        final PotionEffect existingSpeed = player.getPotionEffect(PotionEffectType.SPEED);

        if (existingSpeed == null || existingSpeed.isInfinite()) {
            return;
        }

        final float roll = Math.abs(random.nextFloat(100.0f));

        if (roll <= config.refreshChance()) {
            final int diff = Math.abs(random.nextInt(5)) + 1;
            final int seconds = Math.max(config.refreshEffectMinDuration() + diff, config.refreshEffectMaxDuration());
            final PotionEffect newSpeed = new PotionEffect(PotionEffectType.SPEED, existingSpeed.getDuration() + (seconds*20), existingSpeed.getAmplifier());

            player.addPotionEffect(newSpeed);
            spawnAbilityParticles(player);

            Players.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT);
            FMessage.printGhostbladeRefresh(player, seconds);
        }
    }
}
