package gg.hcfactions.factions.battlepass;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.battlepass.factory.BPObjectiveBuilder;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.battlepass.EBPObjectiveType;
import gg.hcfactions.factions.models.battlepass.impl.BPObjective;
import gg.hcfactions.factions.models.battlepass.impl.BPTracker;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.libs.base.consumer.FailablePromise;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.menu.impl.Icon;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class BattlepassManager implements IManager {
    @Getter public final Factions plugin;
    @Getter @Setter public long dailyExpireTimestamp;
    @Getter public BukkitTask expireCheckTask;
    @Getter public final List<BPObjective> objectiveRepository;
    @Getter public final Set<BPTracker> trackerRepository;

    public BattlepassManager(Factions plugin) {
        this.plugin = plugin;
        this.trackerRepository = Sets.newConcurrentHashSet();
        this.objectiveRepository = Lists.newArrayList();
    }

    @Override
    public void onEnable() {
        loadTrackers();
        loadConfiguration();

        expireCheckTask = new Scheduler(plugin).async(() -> {
            if (dailyExpireTimestamp >= Time.now()) {
                return;
            }

            final ZonedDateTime followingMidnight = ZonedDateTime.now()
                    .withHour(0)
                    .withMinute(0)
                    .withSecond(0)
                    .plusDays(1);

            final YamlConfiguration conf = plugin.loadConfiguration("battlepass");
            dailyExpireTimestamp = followingMidnight.toInstant().toEpochMilli();
            conf.set("expire.daily", dailyExpireTimestamp);
            plugin.saveConfiguration("battlepass", conf);

            resetTrackers();
        }).repeat(30 * 20L, 30 * 20L).run();
    }

    @Override
    public void onDisable() {
        saveTrackers();
    }

    public Optional<BPObjective> getObjective(String objId) {
        return objectiveRepository.stream().filter(obj -> obj.getIdentifier().equalsIgnoreCase(objId)).findFirst();
    }

    public List<BPObjective> getMetRequirementObjectives(Player player, Location location) {
        return objectiveRepository.stream().filter(obj -> obj.meetsRequirement(player, location)).collect(Collectors.toList());
    }

    public List<BPObjective> getMetRequirementObjectives(Player player, Entity entity) {
        return objectiveRepository.stream().filter(obj -> obj.meetsRequirement(player, entity)).collect(Collectors.toList());
    }

    private void loadConfiguration() {
        final YamlConfiguration conf = plugin.loadConfiguration("battlepass");

        // configurable values
        dailyExpireTimestamp = conf.getLong("expire.daily");

        // objectives
        for (String objId : Objects.requireNonNull(conf.getConfigurationSection("objectives")).getKeys(false)) {
            final String path = "objectives." + objId + ".";
            final String reqPath = path + "requirements.";
            final String typeName = conf.getString(path + "type");
            final String iconMaterialName = conf.getString(path + "icon.material");
            final String iconName = conf.getString(path + "icon.display_name");
            final List<String> iconLoreUnformatted = conf.getStringList(path + "icon.lore");
            final int amountRequirement = conf.getInt(path + "requirements.amount");
            final int baseExp = conf.getInt(path + "base_exp");

            EBPObjectiveType objectiveType = null;
            Material iconMaterial = null;
            String iconDisplayName = null;
            List<String> iconLore = Lists.newArrayList();

            try {
                objectiveType = EBPObjectiveType.valueOf(typeName);
            } catch (IllegalArgumentException e) {
                plugin.getAresLogger().error("Invalid objective type: " + typeName);
                continue;
            }

            try {
                iconMaterial = Material.valueOf(iconMaterialName);
                iconDisplayName = ChatColor.translateAlternateColorCodes('&', iconName);
                iconLoreUnformatted.forEach(unformattedLine -> iconLore.add(ChatColor.translateAlternateColorCodes('&', unformattedLine)));
            } catch (IllegalArgumentException e) {
                plugin.getAresLogger().error("Invalid icon data", e);
                continue;
            }

            final BPObjectiveBuilder builder = new BPObjectiveBuilder(plugin, objId)
                    .setAmountRequirement(amountRequirement)
                    .setBaseExp(baseExp)
                    .setObjectiveType(objectiveType)
                    .setIcon(new Icon(iconMaterial, iconDisplayName, iconLore));

            if (conf.contains(reqPath + "entity")) {
                final String entityTypeName = conf.getString(reqPath + "entity");
                final EntityType entityType;

                try {
                    entityType = EntityType.valueOf(entityTypeName);
                } catch (IllegalArgumentException e) {
                    plugin.getAresLogger().error("Invalid entity type: " + entityTypeName);
                    continue;
                }

                builder.setEntityRequirement(entityType);
            }

            if (conf.contains(reqPath + "world")) {
                final String envName = conf.getString(reqPath + "world");
                final World.Environment env;

                try {
                    env = World.Environment.valueOf(envName);
                } catch (IllegalArgumentException e) {
                    plugin.getAresLogger().error("Invalid world type: " + envName);
                    continue;
                }

                builder.setWorldRequirement(env);
            }

            if (conf.contains(reqPath + "block")) {
                final String matName = conf.getString(reqPath + "block");
                final Material mat;

                try {
                    mat = Material.valueOf(matName);
                } catch (IllegalArgumentException e) {
                    plugin.getAresLogger().error("Invalid material type: " + matName);
                    continue;
                }

                builder.setBlockRequirement(mat);
            }

            if (conf.contains(reqPath + "claim")) {
                final String claimFactionName = conf.getString(reqPath + "claim");
                final IFaction faction = plugin.getFactionManager().getFactionByName(claimFactionName);

                if (faction == null) {
                    plugin.getAresLogger().error("Invalid faction name: " + claimFactionName);
                    continue;
                }

                builder.setClaimRequirement(faction);
            }

            if (conf.contains(reqPath + "class")) {
                final String className = conf.getString(reqPath + "class");
                final IClass classQuery = plugin.getClassManager().getClassByName(className);

                if (classQuery == null) {
                    plugin.getAresLogger().error("Invalid class name: " + className);
                    continue;
                }

                builder.setClassRequirement(classQuery);
            }

            builder.build(new FailablePromise<>() {
                @Override
                public void resolve(BPObjective bpObjective) {
                    objectiveRepository.add(bpObjective);
                }

                @Override
                public void reject(String s) {
                    plugin.getAresLogger().error("Unable to create Battlepass Objective: " + s);
                }
            });
        }

        plugin.getAresLogger().info("Loaded " + objectiveRepository.size() + " Battlepass Objectives");
    }

    private void loadTrackers() {
        final YamlConfiguration file = plugin.loadConfiguration("bp-progress");

        if (!file.contains("data")) {
            plugin.getAresLogger().warn("Skipped loading Battlepass Trackers");
            return;
        }

        for (String uid : Objects.requireNonNull(file.getConfigurationSection("data")).getKeys(false)) {
            final UUID ownerId = UUID.fromString(uid);

            if (!file.contains("data." + uid)) {
                plugin.getAresLogger().error("Invalid tracker data for " + uid);
                continue;
            }

            final BPTracker tracker = new BPTracker(ownerId);

            for (String objId : Objects.requireNonNull(file.getConfigurationSection("data." + uid)).getKeys(false)) {
                final int currentValue = file.getInt("data." + uid + "." + objId);

                getObjective(objId).ifPresentOrElse(obj ->
                        tracker.getProgression().put(objId, currentValue),
                        () -> plugin.getAresLogger().error("Attempted to load an objective that does not exist: " + objId));
            }

            trackerRepository.add(tracker);
        }

        plugin.getAresLogger().info("Loaded " + trackerRepository.size() + " Battlepass Trackers");
    }

    private void saveTrackers() {
        final YamlConfiguration file = plugin.loadConfiguration("bp-progress");

        trackerRepository.forEach(tracker ->
                tracker.getProgression().forEach((objId, value) ->
                        file.set("data." + tracker.getOwnerId().toString() + "." + objId, value)));

        plugin.saveConfiguration("bp-progress", file);
    }

    private void resetTrackers() {
        final YamlConfiguration file = plugin.loadConfiguration("bp-progress");
        file.set("data", null);
        plugin.saveConfiguration("bp-progress", file);

        trackerRepository.clear();
    }
}
