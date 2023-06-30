package gg.hcfactions.factions.classes;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.player.ClassDeactivateEvent;
import gg.hcfactions.factions.listeners.events.player.ClassReadyEvent;
import gg.hcfactions.factions.listeners.events.player.ClassUnreadyEvent;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.classes.EConsumableApplicationType;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.classes.IHoldableClass;
import gg.hcfactions.factions.models.classes.impl.*;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Objects;

public final class ClassManager implements IManager {
    @Getter public final Factions plugin;
    @Getter public final List<IClass> classes;

    private BukkitTask classValidationTask;

    public ClassManager(Factions plugin) {
        this.plugin = plugin;
        this.classes = Lists.newArrayList();
    }

    @Override
    public void onEnable() {
        loadClasses();
        classValidationTask = new Scheduler(plugin).sync(() -> Bukkit.getOnlinePlayers().forEach(this::validateClass)).repeat(10*20L, 10*20L).run();
    }

    @Override
    public void onDisable() {
        classes.clear();

        classValidationTask.cancel();
        classValidationTask = null;
    }

    @Override
    public void onReload() {
        loadClasses();
    }

    private void loadClasses() {
        final YamlConfiguration conf = plugin.loadConfiguration("classes");

        if (!classes.isEmpty()) {
            classes.clear();
        }

        for (String className : Objects.requireNonNull(conf.getConfigurationSection("data")).getKeys(false)) {
            final String path = "data." + className + ".";
            final int warmup = conf.getInt(path + "warmup");
            IClass playerClass = null;

            if (className.equalsIgnoreCase("archer")) {
                final double maxDealtDamage = conf.getDouble(path + "damage_values.max_damage");
                final double consecutiveBase = conf.getDouble(path + "damage_values.consecutive_base");
                final double consecutiveMulti = conf.getDouble(path + "damage_values.consecutive_multiplier");
                final double damagePerBlock = conf.getDouble(path + "damage_values.per_block");
                final int markDuration = conf.getInt(path + "mark.duration");
                final double markPercent = conf.getDouble(path + "mark.increase_percent");

                playerClass = new Archer(this, warmup, maxDealtDamage, consecutiveBase, consecutiveMulti, damagePerBlock, markDuration, markPercent);
            }

            else if (className.equalsIgnoreCase("bard")) {
                final double bardRange = conf.getDouble(path + ".range");
                playerClass = new Bard(this, warmup, bardRange);
            }

            else if (className.equalsIgnoreCase("rogue")) {
                final int backstabCooldown = conf.getInt(path + "backstab.cooldown");
                final int backstabTickrate = conf.getInt(path + "backstab.tickrate");
                final double backstabDamage = conf.getDouble(path + "backstab.damage");

                playerClass = new Rogue(this, warmup, backstabDamage, backstabTickrate, backstabCooldown);
            }

            else if (className.equalsIgnoreCase("diver")) {
                final double damageMultiplier = conf.getDouble(path + "damage_values.damage_multiplier");
                final double minimumDistance = conf.getDouble(path + "damage_values.minimum_distance");
                final int seaCallCooldown = conf.getInt(path + "sea_call_cooldown");
                final int seaCallDuration = conf.getInt(path + "sea_call_duration");
                playerClass = new Diver(this, warmup, damageMultiplier, minimumDistance, seaCallCooldown, seaCallDuration);
            }

            else if (className.equalsIgnoreCase("miner")) {
                playerClass = new Miner(this, warmup);
            }

            if (playerClass == null) {
                plugin.getAresLogger().error("invalid player class entry: " + className);
                continue;
            }

            if (conf.get(path + "passive") != null) {
                for (String passiveName : Objects.requireNonNull(conf.getConfigurationSection(path + "passive")).getKeys(false)) {
                    final PotionEffectType passiveType = PotionEffectType.getByName(passiveName);
                    final int amplifier = conf.getInt(path + "passive." + passiveName);

                    if (passiveType == null) {
                        plugin.getAresLogger().error("invalid passive for " + className + ": " + passiveName);
                        continue;
                    }

                    playerClass.getPassiveEffects().put(passiveType, (amplifier - 1));
                }

                plugin.getAresLogger().info("loaded " + playerClass.getPassiveEffects().size() + " passive effects for " + playerClass.getName());
            }

            if (conf.get(path + "holdables") != null && playerClass instanceof final IHoldableClass holdableClass) {
                final int holdableUpdateRate = conf.getInt(path + "holdable_update_rate");
                holdableClass.setHoldableUpdateRate(holdableUpdateRate);

                for (String effectName : Objects.requireNonNull(conf.getConfigurationSection(path + "holdables")).getKeys(false)) {
                    final String hPath = path + "holdables." + effectName + ".";
                    final String materialName = conf.getString(hPath + "material");
                    final int amplifier = conf.getInt(hPath + "amplifier");
                    final int duration = conf.getInt(hPath + "duration");
                    final PotionEffectType effect = PotionEffectType.getByName(effectName);
                    final Material material;

                    try {
                        material = Material.valueOf(materialName);
                    } catch (IllegalArgumentException e) {
                        plugin.getAresLogger().error("invalid mat", e);
                        continue;
                    }

                    final Holdable holdable = new Holdable(plugin, material, effect, amplifier, duration);
                    holdableClass.getHoldables().add(holdable);
                }

                plugin.getAresLogger().info("loaded " + holdableClass.getHoldables().size() + " holdables for " + playerClass.getName());
            }

            if (conf.get(path + "consumables") != null) {
                for (String activeName : Objects.requireNonNull(conf.getConfigurationSection(path + "consumables")).getKeys(false)) {
                    final String actvPath = path + "consumables." + activeName + ".";
                    final String materialName = conf.getString(actvPath + "material");
                    final int duration = conf.getInt(actvPath + "duration");
                    final int amplifier = conf.getInt(actvPath + "amplifier");
                    final int cooldown = conf.getInt(actvPath + "cooldown");
                    final PotionEffectType effect = PotionEffectType.getByName(activeName);
                    final Material material;
                    final EConsumableApplicationType applicationType;

                    try {
                        material = Material.valueOf(materialName);
                    } catch (IllegalArgumentException ex) {
                        plugin.getAresLogger().error("invalid mat", ex);
                        continue;
                    }

                    try {
                        applicationType = EConsumableApplicationType.valueOf(conf.getString(actvPath + "application"));
                    } catch (IllegalArgumentException ex) {
                        plugin.getAresLogger().error("invalid application type", ex);
                        continue;
                    }

                    final Consumable consumable = new Consumable(plugin, material, applicationType, effect, duration, cooldown, amplifier);
                    playerClass.getConsumables().add(consumable);
                }

                plugin.getAresLogger().info("loaded " + playerClass.getConsumables().size() + " consumables for " + playerClass.getName());
            }

            classes.add(playerClass);
        }
    }

    public void validateClass(Player player) {
        final IClass actualClass = getPlugin().getClassManager().getCurrentClass(player);
        final IClass expectedClass = getPlugin().getClassManager().getClassByArmor(player);

        if (expectedClass != null) {
            if (actualClass != null) {
                final ClassDeactivateEvent deactivateEvent = new ClassDeactivateEvent(player, actualClass);
                Bukkit.getPluginManager().callEvent(deactivateEvent);
                actualClass.deactivate(player);
            }

            final ClassReadyEvent readyEvent = new ClassReadyEvent(player, expectedClass);
            readyEvent.setMessagePrinted(true);
            Bukkit.getPluginManager().callEvent(readyEvent);

            return;
        }

        if (actualClass != null) {
            final ClassDeactivateEvent deactivateEvent = new ClassDeactivateEvent(player, actualClass);
            Bukkit.getPluginManager().callEvent(deactivateEvent);
            actualClass.deactivate(player);
        } else {
            final ClassUnreadyEvent unreadyEvent = new ClassUnreadyEvent(player);
            Bukkit.getPluginManager().callEvent(unreadyEvent);
        }
    }

    /**
     * @param player Bukkit Player
     * @return Currently active class
     */
    public IClass getCurrentClass(Player player) {
        return classes.stream().filter(c -> c.getActivePlayers().contains(player.getUniqueId())).findFirst().orElse(null);
    }

    /**
     * @param player Bukkit Player
     * @return Returns a class the provided player meets armor requirements for
     */
    public IClass getClassByArmor(Player player) {
        return classes.stream().filter(c -> c.hasArmorRequirements(player)).findFirst().orElse(null);
    }

    /**
     * @param className Name of the class
     * @return Class matching the provided name
     */
    public IClass getClassByName(String className) {
        return classes.stream().filter(c -> c.getName().equalsIgnoreCase(className)).findFirst().orElse(null);
    }

    /**
     * @param faction Player Faction
     * @param playerClass Class to perform count on
     * @return Returns the amount of online members in the provided faction with the current class active
     */
    public int getFactionClassCount(PlayerFaction faction, IClass playerClass) {
        int count = 0;

        for (PlayerFaction.Member member : faction.getOnlineMembers()) {
            final IClass currentClass = getCurrentClass(member.getBukkit());

            if (currentClass != null && currentClass == playerClass) {
                count += 1;
            }
        }

        return count;
    }
}
