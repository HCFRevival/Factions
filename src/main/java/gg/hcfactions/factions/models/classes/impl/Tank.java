package gg.hcfactions.factions.models.classes.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.classes.ClassManager;
import gg.hcfactions.factions.listeners.events.player.TankStaminaChangeEvent;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.classes.IConsumeable;
import gg.hcfactions.libs.bukkit.builder.impl.ItemBuilder;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.*;
import java.util.List;

public final class Tank implements IClass {
    @Getter public ClassManager manager;
    @Getter public final String name = "Guardian";
    @Getter public final String description = "";
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
    @Getter public final Set<UUID> guardingPlayers;
    @Getter public final Map<UUID, Double> stamina;
    @Getter public final Map<UUID, PLocatable> guardPoints;

    public Tank(ClassManager manager) {
        this.manager = manager;
        this.warmup = 30;
        this.passiveEffects = Maps.newHashMap();
        this.consumables = Lists.newArrayList();
        this.activePlayers = Sets.newConcurrentHashSet();
        this.guardingPlayers = Sets.newConcurrentHashSet();
        this.shieldWarmup = 2;
        this.stamina = Maps.newConcurrentMap();
        this.guardPoints = Maps.newConcurrentMap();
    }

    public Tank(
            ClassManager manager,
            int warmup,
            int shieldWarmup
    ) {
        this.manager = manager;
        this.warmup = warmup;
        this.passiveEffects = Maps.newHashMap();
        this.consumables = Lists.newArrayList();
        this.activePlayers = Sets.newConcurrentHashSet();
        this.guardingPlayers = Sets.newConcurrentHashSet();
        this.shieldWarmup = shieldWarmup;
        this.stamina = Maps.newConcurrentMap();
        this.guardPoints = Maps.newConcurrentMap();

        new Scheduler(manager.getPlugin()).sync(this::renderGuardPoints).repeat(0L, 5L).run();
    }

    private void renderGuardPoints() {
        guardPoints.forEach((uuid, point) -> {
            final Player source = Bukkit.getPlayer(uuid);
            final AreaEffectCloud cloud = (AreaEffectCloud) Objects.requireNonNull(point.getBukkitLocation().getWorld())
                    .spawnEntity(point.getBukkitLocation(), EntityType.AREA_EFFECT_CLOUD);

            cloud.setSource(source);
            cloud.setParticle(Particle.SNEEZE, Particle.SNEEZE.getDataType());
            cloud.setRadius(4.0f);
            cloud.setDuration(5);
            cloud.setBasePotionData(new PotionData(PotionType.TURTLE_MASTER));
        });
    }

    @Override
    public void activate(Player player, boolean printMessage) {
        IClass.super.activate(player, printMessage);

        if (player.getEquipment() == null) {
            player.sendMessage(ChatColor.RED + "Failed to apply Stamina Banner");
            return;
        }

        stamina.put(player.getUniqueId(), 100.0);
        player.getEquipment().setHelmet(getBanner(player));
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
    }

    public void activateShield(Player player) {
        guardingPlayers.add(player.getUniqueId());
        guardPoints.put(player.getUniqueId(), new PLocatable(player));
    }

    public void deactivateShield(Player player) {
        deactivateShield(player.getUniqueId());
    }

    public void deactivateShield(UUID uniqueId) {
        guardingPlayers.remove(uniqueId);
        guardPoints.remove(uniqueId);
    }

    public ItemStack getBanner(Player player) {
        final double stamina = getStamina(player);
        final ItemBuilder builder = new ItemBuilder();

        if (stamina > 75.0) {
            builder.setMaterial(Material.GREEN_BANNER);
        } else if (stamina > 50.0) {
            builder.setMaterial(Material.YELLOW_BANNER);
        } else if (stamina > 25.0) {
            builder.setMaterial(Material.RED_BANNER);
        }

        builder.setName(ChatColor.AQUA + "Stamina");

        return builder.build();
    }

    public double getStamina(UUID uniqueId) {
        return getStamina().getOrDefault(uniqueId, -1.0);
    }

    public double getStamina(Player player) {
        return getStamina(player.getUniqueId());
    }

    public void damageStamina(Player player, double amount) {
        if (!stamina.containsKey(player.getUniqueId())) {
            manager.getPlugin().getAresLogger().error("attempted to adjust stamina for a player not in Tank class");
            return;
        }

        final double current = getStamina(player);
        final double updated = Math.min((current - amount), 0.0);

        final TankStaminaChangeEvent event = new TankStaminaChangeEvent(player, current, updated);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return;
        }

        stamina.put(player.getUniqueId(), updated);
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
