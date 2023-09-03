package gg.hcfactions.factions.models.classes.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.classes.ClassManager;
import gg.hcfactions.factions.listeners.events.player.TankStaminaChangeEvent;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.classes.IConsumeable;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.builder.impl.ItemBuilder;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.List;

/**
 * TODO:
 * - Handle player disconnect
 * - Handle shield/armor breaking causing desync class message spam
 * - Fix particles on banner change to not look ugly (DONE)
 * - Store effects given in Guard mode in the config and load them in (DONE)
 * - Ensure infinite potion effects are removed upon login (DONE)
 * - Ensure banner is removed using a PersistentDataContainer and checking on login
 */

public final class Tank implements IClass {
    @Getter public ClassManager manager;
    @Getter public final String name = "Guardian";
    @Getter public final String description = "Use your shield to protect nearby allies";
    @Getter public final boolean emptyArmorEnforced = false;
    @Getter public final int warmup;
    @Getter public final Material helmet = null;
    @Getter public final Material chestplate = Material.NETHERITE_CHESTPLATE;
    @Getter public final Material leggings = Material.NETHERITE_LEGGINGS;
    @Getter public final Material boots = Material.NETHERITE_BOOTS;
    @Getter public final Material offhand = Material.SHIELD;
    @Getter public final Map<PotionEffectType, Integer> passiveEffects;
    @Getter public final List<IConsumeable> consumables;
    @Getter public final Set<UUID> activePlayers;

    @Getter public final int shieldWarmup;
    @Getter public final double shieldDamageReduction;
    @Getter public final double staminaDamageDivider;
    @Getter public final int staminaRegenInterval;
    @Getter public final int staminaRegenDelay;
    @Getter public final int staminaHardRegenDelay;
    @Getter public final Set<UUID> guardingPlayers;
    @Getter public final Map<UUID, Double> stamina;
    @Getter public final Map<UUID, PLocatable> guardPoints;
    @Getter public final Map<UUID, Long> nextStaminaRegen;
    @Getter public final Map<PotionEffectType, Integer> guardEffects;

    public Tank(ClassManager manager) {
        this.manager = manager;
        this.warmup = 30;
        this.passiveEffects = Maps.newHashMap();
        this.consumables = Lists.newArrayList();
        this.activePlayers = Sets.newConcurrentHashSet();
        this.guardingPlayers = Sets.newConcurrentHashSet();
        this.shieldWarmup = 2;
        this.shieldDamageReduction = 0.5;
        this.staminaDamageDivider = 3.0;
        this.staminaRegenInterval = 1;
        this.staminaRegenDelay = 5;
        this.staminaHardRegenDelay = 15;
        this.stamina = Maps.newConcurrentMap();
        this.guardPoints = Maps.newConcurrentMap();
        this.nextStaminaRegen = Maps.newConcurrentMap();
        this.guardEffects = Maps.newHashMap();
    }

    public Tank(
            ClassManager manager,
            int warmup,
            int shieldWarmup,
            double shieldDamageReduction,
            double staminaDamageDivider,
            int staminaRegenInterval,
            int staminaRegenDelay,
            int staminaHardRegenDelay
    ) {
        this.manager = manager;
        this.warmup = warmup;
        this.passiveEffects = Maps.newHashMap();
        this.consumables = Lists.newArrayList();
        this.activePlayers = Sets.newConcurrentHashSet();
        this.guardingPlayers = Sets.newConcurrentHashSet();
        this.shieldWarmup = shieldWarmup;
        this.shieldDamageReduction = shieldDamageReduction;
        this.staminaDamageDivider = staminaDamageDivider;
        this.staminaRegenDelay = staminaRegenDelay;
        this.staminaRegenInterval = staminaRegenInterval;
        this.staminaHardRegenDelay = staminaHardRegenDelay;
        this.stamina = Maps.newConcurrentMap();
        this.guardPoints = Maps.newConcurrentMap();
        this.nextStaminaRegen = Maps.newConcurrentMap();
        this.guardEffects = Maps.newHashMap();

        new Scheduler(manager.getPlugin()).sync(this::renderGuardPoints).repeat(0L, 5L).run();
        new Scheduler(manager.getPlugin()).sync(this::regenerateStamina).repeat(0L, 20L).run();
    }

    private void renderGuardPoints() {
        guardPoints.forEach((uuid, point) -> {
            final Player source = Bukkit.getPlayer(uuid);
            final AreaEffectCloud cloud = (AreaEffectCloud) Objects.requireNonNull(point.getBukkitLocation().getWorld())
                    .spawnEntity(point.getBukkitLocation(), EntityType.AREA_EFFECT_CLOUD);

            cloud.setSource(source);
            cloud.setParticle(Particle.SNEEZE, Particle.SMOKE_LARGE);
            cloud.setRadius(6.0f);
            cloud.setDuration(5);
            cloud.setReapplicationDelay(1);
            cloud.setBasePotionData(new PotionData(PotionType.TURTLE_MASTER));
        });
    }

    private void regenerateStamina() {
        final List<UUID> toRemove = Lists.newArrayList();

        for (UUID uuid : nextStaminaRegen.keySet()) {
            final Player player = Bukkit.getPlayer(uuid);
            final long nextTick = nextStaminaRegen.get(uuid);

            if (nextTick > Time.now()) {
                continue;
            }

            final double currentStamina = getStamina(uuid);
            final double newStamina = Math.min((currentStamina + 1.0), 100.0);

            if (player == null || newStamina >= 100.0) {
                toRemove.add(uuid);
            }

            final TankStaminaChangeEvent changeEvent = new TankStaminaChangeEvent(player, currentStamina, newStamina);
            Bukkit.getPluginManager().callEvent(changeEvent);

            if (changeEvent.isCancelled()) {
                continue;
            }

            stamina.put(uuid, changeEvent.getTo());
        }

        toRemove.forEach(nextStaminaRegen::remove);
    }

    @Override
    public void activate(Player player, boolean printMessage) {
        IClass.super.activate(player, printMessage);

        if (player.getEquipment() == null) {
            player.sendMessage(ChatColor.RED + "Failed to apply Stamina Banner");
            return;
        }

        stamina.put(player.getUniqueId(), 100.0);

        new Scheduler(manager.getPlugin()).sync(() -> player.getEquipment().setHelmet(getBanner(player))).delay(1L).run();
    }

    @Override
    public void deactivate(Player player, boolean printMessage) {
        IClass.super.deactivate(player, printMessage);

        // Remove banner
        if (player.getEquipment() != null) {
            player.getEquipment().setHelmet(null);
        }

        guardPoints.remove(player.getUniqueId());
        guardingPlayers.remove(player.getUniqueId());
        stamina.remove(player.getUniqueId());
        nextStaminaRegen.remove(player.getUniqueId());
    }

    public void activateShield(Player player) {
        guardingPlayers.add(player.getUniqueId());
        guardPoints.put(player.getUniqueId(), new PLocatable(player));
        nextStaminaRegen.remove(player.getUniqueId());
    }

    public void deactivateShield(Player player) {
        deactivateShield(player.getUniqueId());
    }

    public void deactivateShield(UUID uniqueId) {
        final double currentStamina = getStamina(uniqueId);

        guardingPlayers.remove(uniqueId);
        guardPoints.remove(uniqueId);

        if (currentStamina < 100.0) {
            final int delay = (currentStamina <= 0.0) ? staminaHardRegenDelay : staminaRegenDelay;
            nextStaminaRegen.put(uniqueId, (Time.now() + (delay * 1000L)));
        }
    }

    public ItemStack getBanner(Player player) {
        final double stamina = getStamina(player);
        return getBanner(stamina);
    }

    public ItemStack getBanner(double stamina) {
        final ItemBuilder builder = new ItemBuilder();

        builder.setName(ChatColor.AQUA + "Stamina");
        builder.addEnchant(Enchantment.BINDING_CURSE, 1);
        builder.addFlag(ItemFlag.HIDE_ENCHANTS);

        if (stamina > 80.0) {
            builder.setMaterial(Material.GREEN_BANNER);
        } else if (stamina > 60.0) {
            builder.setMaterial(Material.LIME_BANNER);
        } else if (stamina > 40.0) {
            builder.setMaterial(Material.YELLOW_BANNER);
        } else if (stamina > 20.0) {
            builder.setMaterial(Material.ORANGE_BANNER);
        } else if (stamina > 0.0) {
            builder.setMaterial(Material.RED_BANNER);
        } else {
            builder.setMaterial(Material.BLACK_BANNER);
        }

        final ItemStack bannerItem = builder.build();
        final ItemMeta meta = bannerItem.getItemMeta();
        final PersistentDataContainer container = Objects.requireNonNull(meta).getPersistentDataContainer();

        container.set(manager.getPlugin().getNamespacedKey(), PersistentDataType.STRING, "removeOnLogin");
        bannerItem.setItemMeta(meta);

        return bannerItem;
    }

    public double getStamina(UUID uniqueId) {
        return getStamina().getOrDefault(uniqueId, -1.0);
    }

    public double getStamina(Player player) {
        return getStamina(player.getUniqueId());
    }

    public boolean canUseStamina(Player player) {
        return getStamina(player) > 40.0;
    }

    public void damageStamina(Player player, double amount) {
        if (!stamina.containsKey(player.getUniqueId())) {
            manager.getPlugin().getAresLogger().error("attempted to adjust stamina for a player not in Tank class");
            return;
        }

        final double current = getStamina(player);
        final double updated = Math.max((current - amount), 0.0);

        final TankStaminaChangeEvent event = new TankStaminaChangeEvent(player, current, updated);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        stamina.put(player.getUniqueId(), updated);

        if (updated <= 0.0) {
            final ItemStack shield = Objects.requireNonNull(player.getEquipment()).getItemInOffHand();
            player.getEquipment().setItemInOffHand(new ItemStack(Material.AIR));

            deactivateShield(player);

            new Scheduler(manager.getPlugin()).sync(() -> {
                player.getInventory().setItemInOffHand(shield);
                player.setCooldown(Material.SHIELD, 20);
            }).delay(10L).run();
        }
    }

    public PLocatable getGuardPoint(Player player) {
        return guardPoints.get(player.getUniqueId());
    }

    public boolean isGuarding(Player player) {
        return guardingPlayers.contains(player.getUniqueId());
    }

    public boolean hasGuardPoint(Player player) {
        return guardPoints.containsKey(player.getUniqueId());
    }

    public void setGuardPoint(Player player) {
        guardPoints.put(player.getUniqueId(), new PLocatable(player));
    }

    public void removeGuardPoint(Player player) {
        removeGuardPoint(player.getUniqueId());
    }

    public void removeGuardPoint(UUID uniqueId) {
        guardPoints.remove(uniqueId);
        guardingPlayers.remove(uniqueId);
    }
}
