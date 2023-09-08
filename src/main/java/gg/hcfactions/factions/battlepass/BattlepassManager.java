package gg.hcfactions.factions.battlepass;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.battlepass.factory.BPObjectiveBuilder;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.battlepass.EBPObjectiveType;
import gg.hcfactions.factions.models.battlepass.EBPState;
import gg.hcfactions.factions.models.battlepass.impl.BPObjective;
import gg.hcfactions.factions.models.battlepass.impl.BPTracker;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.libs.base.consumer.FailablePromise;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.menu.impl.Icon;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.ranks.RankService;
import gg.hcfactions.libs.bukkit.services.impl.ranks.model.impl.AresRank;
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

public final class BattlepassManager implements IManager {
    @Getter public final Factions plugin;
    @Getter @Setter public boolean enabled;
    @Getter @Setter public long dailyExpireTimestamp;
    @Getter @Setter public long weeklyExpireTimestamp;
    @Getter public BukkitTask expireCheckTask;
    @Getter public final List<BPObjective> dailyObjectiveRepository;
    @Getter public final List<BPObjective> weeklyObjectiveRepository;
    @Getter public final Set<BPTracker> trackerRepository;
    @Getter public final Map<AresRank, Double> rankMultipliers;

    public BattlepassManager(Factions plugin) {
        this.plugin = plugin;
        this.enabled = false;
        this.trackerRepository = Sets.newConcurrentHashSet();
        this.dailyObjectiveRepository = Lists.newArrayList();
        this.weeklyObjectiveRepository = Lists.newArrayList();
        this.rankMultipliers = Maps.newHashMap();
    }

    @Override
    public void onEnable() {
        loadConfiguration();
        loadObjectives(true);
        loadObjectives(false);

        // TODO: This is for testing, remove
        final RankService rankService = (RankService) plugin.getService(RankService.class);
        rankService.getRankRepository().forEach(rank -> rankMultipliers.put(rank, 1.5));
        // end test

        expireCheckTask = new Scheduler(plugin).async(() -> {
            if (dailyExpireTimestamp >= Time.now()) {
                return;
            }

            if (dailyExpireTimestamp <= Time.now()) {
                final ZonedDateTime followingMidnight = ZonedDateTime.now()
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .plusDays(1);

                final YamlConfiguration conf = plugin.loadConfiguration("battlepass");
                dailyExpireTimestamp = followingMidnight.toInstant().toEpochMilli();
                conf.set("expire.daily", dailyExpireTimestamp);
                plugin.saveConfiguration("battlepass", conf);

                resetTrackers(false);
                getNewObjectives(false);
            }

            if (weeklyExpireTimestamp <= Time.now()) {
                final ZonedDateTime followingWeek = ZonedDateTime.now()
                        .withHour(0)
                        .withMinute(0)
                        .withSecond(0)
                        .plusWeeks(1);

                final YamlConfiguration conf = plugin.loadConfiguration("battlepass");
                weeklyExpireTimestamp = followingWeek.toInstant().toEpochMilli();
                conf.set("expire.weekly", weeklyExpireTimestamp);
                plugin.saveConfiguration("battlepass", conf);

                resetTrackers(true);
                getNewObjectives(true);
            }
        }).repeat(30 * 20L, 30 * 20L).run();
    }

    @Override
    public void onDisable() {
        if (expireCheckTask != null) {
            expireCheckTask.cancel();
            expireCheckTask = null;
        }

        saveTrackers();
    }

    public boolean isObjectiveActive(BPObjective objective) {
        return isObjectiveActive(objective.getIdentifier());
    }

    public boolean isObjectiveActive(String objId) {
        return getActiveObjectives().stream().anyMatch(obj -> obj.getIdentifier().equalsIgnoreCase(objId));
    }

    public boolean isBeingTracked(Player player) {
        return trackerRepository.stream().anyMatch(t -> t.getOwnerId().equals(player.getUniqueId()));
    }

    public boolean isBeingTracked(UUID uid) {
        return trackerRepository.stream().anyMatch(t -> t.getOwnerId().equals(uid));
    }

    public List<BPObjective> getObjectiveRepository() {
        final List<BPObjective> res = Lists.newArrayList();
        res.addAll(dailyObjectiveRepository);
        res.addAll(weeklyObjectiveRepository);
        return res;
    }

    public Optional<BPObjective> getObjective(String objId) {
        return getObjectiveRepository().stream().filter(obj -> obj.getIdentifier().equalsIgnoreCase(objId)).findFirst();
    }

    public BPTracker getTracker(Player player) {
        return trackerRepository.stream().filter(t -> t.getOwnerId().equals(player.getUniqueId())).findFirst().orElse(new BPTracker(player.getUniqueId()));
    }

    public BPTracker getTracker(UUID uid) {
        return trackerRepository.stream().filter(t -> t.getOwnerId().equals(uid)).findFirst().orElse(new BPTracker(uid));
    }

    public List<BPObjective> getActiveObjectives() {
        return getObjectiveRepository().stream().filter(obj -> !obj.getState().equals(EBPState.INACTIVE)).collect(Collectors.toList());
    }

    public List<BPObjective> getDailyObjectives() {
        return getDailyObjectiveRepository().stream().filter(obj -> !obj.getState().equals(EBPState.INACTIVE)).collect(Collectors.toList());
    }

    public List<BPObjective> getWeeklyObjectives() {
        return getWeeklyObjectiveRepository().stream().filter(obj -> !obj.getState().equals(EBPState.INACTIVE)).collect(Collectors.toList());
    }

    public List<BPObjective> getMetRequirementObjectives(Player player, Location location) {
        return getActiveObjectives().stream().filter(obj -> obj.meetsRequirement(player, location)).collect(Collectors.toList());
    }

    public List<BPObjective> getMetRequirementObjectives(Player player, Entity entity) {
        return getActiveObjectives().stream().filter(obj -> obj.meetsRequirement(player, entity)).collect(Collectors.toList());
    }

    public void getNewObjectives(boolean weekly) {
        final List<BPObjective> newObjectives = Lists.newArrayList();
        final List<BPObjective> objPool = (weekly ? weeklyObjectiveRepository : dailyObjectiveRepository);
        final int objectiveSize = Math.min(objPool.size(), 3);
        final Random random = new Random();

        objPool.stream().filter(obj -> !obj.getState().equals(EBPState.INACTIVE)).forEach(activeObj -> activeObj.setState(EBPState.INACTIVE));

        while (newObjectives.size() != objectiveSize) {
            final BPObjective draw = objPool.get(random.nextInt(objPool.size()));

            if (newObjectives.contains(draw)) {
                continue;
            }

            newObjectives.add(draw);
        }

        newObjectives.forEach(obj -> obj.setState((weekly ? EBPState.WEEKLY : EBPState.DAILY)));
        saveActiveObjectives();

        plugin.getAresLogger().info("Activated " + newObjectives.size() + (weekly ? " Weekly" : " Daily") + " Objectives");
    }

    public void loadConfiguration() {
        final YamlConfiguration conf = plugin.loadConfiguration("battlepass");

        // configurable values
        enabled = conf.getBoolean("enabled");
        dailyExpireTimestamp = conf.getLong("expire.daily");
        weeklyExpireTimestamp = conf.getLong("expire.weekly");
    }

    public void loadObjectives(boolean weekly) {
        final YamlConfiguration conf = plugin.loadConfiguration("battlepass");

        final List<String> activeDailyIds = conf.getStringList("active.daily");
        final List<String> activeWeeklyIds = conf.getStringList("active.weekly");

        for (String objId : Objects.requireNonNull(conf.getConfigurationSection("objectives." + (weekly ? "weekly" : "daily"))).getKeys(false)) {
            final String path = "objectives." + (weekly ? "weekly." : "daily.") + objId + ".";
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
                if (typeName == null) {
                    plugin.getAresLogger().error("Invalid objective type (null), obj-id: " + objId);
                    continue;
                }

                objectiveType = EBPObjectiveType.valueOf(typeName);
            } catch (IllegalArgumentException e) {
                plugin.getAresLogger().error("Invalid objective type: " + typeName);
                continue;
            }

            try {
                iconMaterial = Material.valueOf(iconMaterialName);
                iconDisplayName = ChatColor.translateAlternateColorCodes('&', iconName);

                if (!iconLoreUnformatted.isEmpty()) {
                    iconLoreUnformatted.forEach(unformattedLine -> iconLore.add(ChatColor.translateAlternateColorCodes('&', unformattedLine)));
                }
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
                    if (weekly) {
                        if (activeWeeklyIds.contains(bpObjective.getIdentifier())) {
                            bpObjective.setState(EBPState.WEEKLY);
                        }

                        weeklyObjectiveRepository.add(bpObjective);
                        return;
                    }

                    if (activeDailyIds.contains(bpObjective.getIdentifier())) {
                        bpObjective.setState(EBPState.DAILY);
                    }

                    dailyObjectiveRepository.add(bpObjective);
                }

                @Override
                public void reject(String s) {
                    plugin.getAresLogger().error("Unable to create Battlepass Objective: " + s);
                }
            });
        }

        plugin.getAresLogger().info("Loaded " + getObjectiveRepository().size() + " Battlepass Objectives");
    }

    public Optional<BPTracker> loadTracker(Player player) {
        return loadTracker(player.getUniqueId());
    }

    public Optional<BPTracker> loadTracker(UUID uid) {
        final YamlConfiguration file = plugin.loadConfiguration("bp-progress");

        if (!file.contains("data") || !file.contains("data." + uid)) {
            return Optional.empty();
        }

        final BPTracker tracker = new BPTracker(uid);

        for (String objId : Objects.requireNonNull(file.getConfigurationSection("data." + uid)).getKeys(false)) {
            if (!isObjectiveActive(objId)) {
                plugin.getAresLogger().warn("Skipped loading tracker objective: " + objId + " was not active at the time of loading");
                continue;
            }

            final int currentValue = file.getInt("data." + uid + "." + objId);

            getObjective(objId).ifPresentOrElse(obj ->
                            tracker.getProgression().put(objId, currentValue),
                    () -> plugin.getAresLogger().error("Attempted to load an objective that does not exist: " + objId));
        }

        return Optional.of(tracker);
    }

    public void saveTracker(BPTracker tracker) {
        final YamlConfiguration file = plugin.loadConfiguration("bp-progress");
        file.set("data." + tracker.getOwnerId().toString(), null);
        tracker.getProgression().forEach((objId, value) -> file.set("data." + tracker.getOwnerId().toString() + "." + objId, value));
        plugin.saveConfiguration("bp-progress", file);
    }

    private void saveTrackers() {
        final YamlConfiguration file = plugin.loadConfiguration("bp-progress");

        trackerRepository.forEach(tracker ->
                tracker.getProgression().forEach((objId, value) ->
                        file.set("data." + tracker.getOwnerId().toString() + "." + objId, value)));

        plugin.saveConfiguration("bp-progress", file);
    }

    public void resetTracker(BPTracker tracker) {
        final YamlConfiguration file = plugin.loadConfiguration("bp-progress");
        file.set("data." + tracker.getOwnerId().toString(), null);
        plugin.saveConfiguration("bp-progress", file);
    }

    public void resetTrackers(boolean weekly) {
        final YamlConfiguration file = plugin.loadConfiguration("bp-progress");
        final List<BPObjective> toClear = (weekly ? getWeeklyObjectives() : getDailyObjectives());

        toClear.forEach(obj -> trackerRepository.forEach(tracker -> {
            tracker.getProgression().remove(obj.getIdentifier());
            file.set("data." + tracker.getOwnerId() + "." + obj.getIdentifier(), null);
        }));

        plugin.saveConfiguration("bp-progress", file);
    }

    private void saveActiveObjectives() {
        final YamlConfiguration file = plugin.loadConfiguration("battlepass");
        final List<BPObjective> active = getActiveObjectives();
        final List<String> activeWeeklyIds = Lists.newArrayList();
        final List<String> activeDailyIds = Lists.newArrayList();

        active.forEach(obj -> {
            if (obj.getState().equals(EBPState.DAILY)) {
                activeDailyIds.add(obj.getIdentifier());
            }

            else {
                activeWeeklyIds.add(obj.getIdentifier());
            }
        });

        file.set("active.daily", activeDailyIds);
        file.set("active.weekly", activeWeeklyIds);

        plugin.saveConfiguration("battlepass", file);
    }
}
