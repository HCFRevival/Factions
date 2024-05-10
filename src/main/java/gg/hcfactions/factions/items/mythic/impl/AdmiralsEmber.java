package gg.hcfactions.factions.items.mythic.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gg.hcfactions.cx.CXService;
import gg.hcfactions.cx.modules.player.combat.EnchantLimitModule;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.mythic.EMythicAbilityType;
import gg.hcfactions.factions.items.mythic.IMythicItem;
import gg.hcfactions.factions.items.mythic.MythicAbility;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.utils.FactionUtil;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public final class AdmiralsEmber implements IMythicItem {
    public record AdmiralsEmberConfig(
            @Getter int overheatRequirement,
            @Getter int overheatExpireRate,
            @Getter int overheatFireTicks,
            @Getter int overheatCooldown,
            @Getter double ablazeRange,
            @Getter int ablazeFireTicks) {}

    public record OverheatData(@Getter UUID uniqueId, @Getter UUID attackerId, @Getter UUID attackedId) {}

    public static final TextColor ADMIRALS_EMBER_COLOR = NamedTextColor.RED;

    @Getter public final Factions plugin;
    @Getter public final AdmiralsEmberConfig config;
    @Getter public final List<MythicAbility> abilityInfo;
    @Getter public final Set<UUID> overheatCooldown;
    @Getter public final List<OverheatData> overheatDataRepository;

    public AdmiralsEmber(Factions plugin, AdmiralsEmberConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.abilityInfo = Lists.newArrayList();
        this.overheatCooldown = Sets.newConcurrentHashSet();
        this.overheatDataRepository = Lists.newArrayList();

        addAbilityInfo(
                ChatColor.GOLD + "Overheat",
                "Attacking an enemy " + config.getOverheatRequirement() + " times within " + config.getOverheatExpireRate() + " seconds will ignite them for " + (config.getOverheatFireTicks()/20) + " seconds. (30 second cooldown)",
                EMythicAbilityType.ON_HIT
        );

        addAbilityInfo(
                ChatColor.RED + "Set Ablaze",
                "Ignite all enemies within " + config.getAblazeRange() + " blocks for " + (config.getAblazeFireTicks()/20) + " seconds upon slaying an enemy.",
                EMythicAbilityType.ON_KILL
        );
    }

    @Override
    public Material getMaterial() {
        return Material.NETHERITE_SWORD;
    }

    @Override
    public Map.Entry<NamespacedKey, String> getIdentifier() {
        return Map.entry(plugin.getNamespacedKey(), "AdmiralsEmber");
    }

    @Override
    public Component getDisplayNameComponent() {
        return Component.text(getName()).color(getColor());
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
        return "Admiral's Ember";
    }

    @Override
    public TextColor getColor() {
        return ADMIRALS_EMBER_COLOR;
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

        if (enchantLimitModule.getEnchantLimits().containsKey(Enchantment.SHARPNESS)) {
            sharpnessLevel = enchantLimitModule.getMaxEnchantmentLevel(Enchantment.SHARPNESS);
        }

        enchantments.put(Enchantment.SHARPNESS, sharpnessLevel);

        return enchantments;
    }

    @Override
    public void onAttack(Player player, LivingEntity attackedEntity) {
        if (overheatCooldown.contains(player.getUniqueId())) {
            return;
        }

        addAttackEntry(player, attackedEntity);
        final List<OverheatData> existingEntries = getOverheatData(player);

        if (existingEntries.size() >= config.getOverheatRequirement()) {
            FMessage.printAdmiralsEmberOverheatAttacker(player, attackedEntity, (config.getOverheatFireTicks()/20));

            if (attackedEntity instanceof final Player attackedPlayer) {
                FMessage.printAdmiralsEmberOverheatVictim(attackedPlayer, player, (config.getOverheatFireTicks()/20));
            }

            spawnAbilityParticles(player);

            attackedEntity.setFireTicks(attackedEntity.getFireTicks() + config.getOverheatFireTicks());
            overheatDataRepository.removeIf(data -> data.getAttackerId().equals(player.getUniqueId()));

            final UUID playerId = player.getUniqueId();
            overheatCooldown.add(playerId);
            new Scheduler(plugin).sync(() -> overheatCooldown.remove(playerId)).delay(config.getOverheatCooldown()*20L).run();
        }
    }

    @Override
    public void onKill(Player player, LivingEntity slainEntity) {
        final List<Player> nearbyEnemies = FactionUtil.getNearbyEnemies(plugin, player, slainEntity.getLocation(), config.getAblazeRange());

        if (nearbyEnemies.isEmpty()) {
            return;
        }

        FMessage.printAdmiralsEmberAblazeAttacker(player, (config.getAblazeFireTicks()/20));

        nearbyEnemies.forEach(enemy -> {
            enemy.setFireTicks(enemy.getFireTicks() + config.getAblazeFireTicks());
            FMessage.printAdmiralsEmberAblazeVictim(enemy, (config.getAblazeFireTicks()/20));
        });
    }

    private List<OverheatData> getOverheatData(Player attacker) {
        return overheatDataRepository.stream().filter(data -> data.attackerId().equals(attacker.getUniqueId())).collect(Collectors.toList());
    }

    private void addAttackEntry(Player attacker, LivingEntity attacked) {
        final OverheatData data = new OverheatData(UUID.randomUUID(), attacker.getUniqueId(), attacked.getUniqueId());

        overheatDataRepository.add(data);
        overheatDataRepository.removeIf(existing -> existing.getAttackerId().equals(attacker.getUniqueId()) && !existing.getAttackedId().equals(attacked.getUniqueId()));

        new Scheduler(plugin).sync(() ->
                        overheatDataRepository.removeIf(entry -> entry.getUniqueId().equals(data.getUniqueId())))
                .delay(config.getOverheatExpireRate()*20L)
                .run();
    }
}
