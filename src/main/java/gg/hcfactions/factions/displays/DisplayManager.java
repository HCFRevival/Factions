package gg.hcfactions.factions.displays;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.displays.impl.DisplayExecutor;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.displays.IDisplayable;
import gg.hcfactions.factions.models.displays.impl.LeaderboardDisplayable;
import gg.hcfactions.factions.models.stats.EStatisticType;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.utils.Configs;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class DisplayManager implements IManager {
    @Getter public Factions plugin;
    @Getter public DisplayExecutor executor;
    @Getter public List<IDisplayable> displayRepository;
    @Getter public BukkitTask displayUpdaterTask;

    public DisplayManager(Factions plugin) {
        this.plugin = plugin;
        this.executor = new DisplayExecutor(this);
    }

    @Override
    public void onEnable() {
        displayRepository = Lists.newArrayList();

        // load + spawn
        loadDisplays();
        displayRepository.forEach(IDisplayable::spawn);

        // start updating task
        displayUpdaterTask = new Scheduler(plugin).sync(() ->
                displayRepository.forEach(IDisplayable::update)).repeat(0L, 300*20L).run(); // TODO: Make configurable
    }

    @Override
    public void onDisable() {
        displayRepository.forEach(IDisplayable::despawn);
        displayRepository.clear();
        displayRepository = null;
    }

    @Override
    public void onReload() {
        // despawn all and clear memory
        displayRepository.forEach(IDisplayable::despawn);
        displayRepository.clear();

        // reload from file in to memory
        loadDisplays();

        // respawn
        displayRepository.forEach(IDisplayable::spawn);
    }

    public void loadDisplays() {
        final YamlConfiguration conf = plugin.loadConfiguration("displays");

        if (!displayRepository.isEmpty()) {
            displayRepository.forEach(IDisplayable::despawn);
            displayRepository.clear();
        }

        if (conf.get("data") == null) {
            plugin.getAresLogger().warn("no displays found in displays.yml. skipping...");
            return;
        }

        for (String displayId : Objects.requireNonNull(conf.getConfigurationSection("data")).getKeys(false)) {
            final String key = "data." + displayId + ".";
            final UUID uniqueId = UUID.fromString(displayId);
            final PLocatable origin = Configs.parsePlayerLocation(conf, key + "origin");
            final String title = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(conf.getString(key + "title")));

            // leaderboard displays
            if (conf.get(key + "stat_type") != null) {
                final EStatisticType type = EStatisticType.valueOf(conf.getString(key + "stat_type"));
                final LeaderboardDisplayable lbd = new LeaderboardDisplayable(plugin, uniqueId, type, origin);

                displayRepository.add(lbd);
            }
        }

        plugin.getAresLogger().info("loaded " + displayRepository.size() + " displays");
    }

    public void saveDisplays() {
        final YamlConfiguration conf = plugin.loadConfiguration("displays");

        for (IDisplayable displayable : displayRepository) {
            final String key = "data." + displayable.getUniqueId().toString() + ".";

            conf.set(key + "title", displayable.getTitle());
            Configs.writePlayerLocation(conf, key + "origin", displayable.getOrigin());

            if (displayable instanceof final LeaderboardDisplayable lbd) {
                conf.set(key + "stat_type", lbd.getType().name());
            }
        }

        plugin.saveConfiguration("displays", conf);
        plugin.getAresLogger().info("saved " + displayRepository.size() + " displays to file");
    }

    public void deleteDisplays(IDisplayable displayable) {
        final YamlConfiguration conf = plugin.loadConfiguration("displays");
        conf.set("data." + displayable.getUniqueId().toString(), null);
        plugin.saveConfiguration("displays", conf);
    }
}
