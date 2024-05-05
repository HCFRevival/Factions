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
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
public final class DisplayManager implements IManager {
    public record DisplayColorConfig(@Getter TextColor titleColor, @Getter TextColor positionColor, @Getter TextColor contentColor, @Getter TextColor valueColor) {}

    public Factions plugin;
    public DisplayExecutor executor;
    public List<IDisplayable> displayRepository;
    public BukkitTask displayUpdaterTask;
    public DisplayColorConfig colorConfig;

    public DisplayManager(Factions plugin) {
        this.plugin = plugin;
        this.executor = new DisplayExecutor(this);
        this.colorConfig = null;
    }

    @Override
    public void onEnable() {
        displayRepository = Lists.newArrayList();

        // load + spawn
        loadDisplays();
        displayRepository.forEach(IDisplayable::spawn);

        // start updating task
        displayUpdaterTask = new Scheduler(plugin).sync(() ->
                displayRepository.forEach(IDisplayable::update)).repeat(0L, 60*20L).run(); // TODO: Make configurable
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

        final String titleColorHex = conf.getString("style.title_color");
        final String positionColorHex = conf.getString("style.position_color");
        final String contentColorHex = conf.getString("style.content_color");
        final String valueColorHex = conf.getString("style.value_color");
        final TextColor titleColor = (titleColorHex != null ? TextColor.fromCSSHexString(titleColorHex) : NamedTextColor.GOLD);
        final TextColor positionColor = (positionColorHex != null ? TextColor.fromCSSHexString(positionColorHex) : NamedTextColor.GOLD);
        final TextColor contentColor = (contentColorHex != null ? TextColor.fromCSSHexString(contentColorHex) : NamedTextColor.GOLD);
        final TextColor valueColor = (valueColorHex != null ? TextColor.fromCSSHexString(valueColorHex) : NamedTextColor.GOLD);
        colorConfig = new DisplayColorConfig(titleColor, positionColor, contentColor, valueColor);

        if (conf.get("data") == null) {
            plugin.getAresLogger().warn("no displays found in displays.yml. skipping...");
            return;
        }

        for (String displayId : Objects.requireNonNull(conf.getConfigurationSection("data")).getKeys(false)) {
            final String key = "data." + displayId + ".";
            final UUID uniqueId = UUID.fromString(displayId);
            final PLocatable origin = Configs.parsePlayerLocation(conf, key + "origin");

            // leaderboard displays
            if (conf.get(key + "stat_type") != null) {
                final EStatisticType type = EStatisticType.valueOf(conf.getString(key + "stat_type"));
                final LeaderboardDisplayable lbd = new LeaderboardDisplayable(plugin, uniqueId, type, origin);

                displayRepository.add(lbd);
            }
        }

        plugin.getAresLogger().info("Loaded {} displays", displayRepository.size());
    }

    public void saveDisplays() {
        final YamlConfiguration conf = plugin.loadConfiguration("displays");

        for (IDisplayable displayable : displayRepository) {
            final String key = "data." + displayable.getUniqueId().toString() + ".";
            Configs.writePlayerLocation(conf, key + "origin", displayable.getOrigin());

            if (displayable instanceof final LeaderboardDisplayable lbd) {
                conf.set(key + "stat_type", lbd.getType().name());
            }
        }

        plugin.saveConfiguration("displays", conf);
        plugin.getAresLogger().info("Saved {} displays to file", displayRepository.size());
    }

    public void deleteDisplays(IDisplayable displayable) {
        final YamlConfiguration conf = plugin.loadConfiguration("displays");
        conf.set("data." + displayable.getUniqueId().toString(), null);
        plugin.saveConfiguration("displays", conf);
    }
}
