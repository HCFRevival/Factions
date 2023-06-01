package gg.hcfactions.factions.waypoints;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lunarclient.bukkitapi.LunarClientAPI;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.waypoint.IWaypoint;
import gg.hcfactions.factions.models.waypoint.impl.FactionWaypoint;
import gg.hcfactions.factions.models.waypoint.impl.GlobalWaypoint;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class WaypointManager implements IManager {
    @Getter public Factions plugin;
    @Getter public Set<IWaypoint> waypointRepository;

    public WaypointManager(Factions plugin) {
        this.plugin = plugin;
        waypointRepository = Sets.newConcurrentHashSet();
    }

    @Override
    public void onEnable() {
        if (LunarClientAPI.getInstance() == null || !LunarClientAPI.getInstance().isEnabled()) {
            plugin.getAresLogger().error("failed to initialize waypoint manager: instance is null");
            return;
        }

        initializeGlobalWaypoints();
        initializeFactionHomeWaypoints();

        plugin.getAresLogger().info("initialized " + waypointRepository.size() + " waypoints");
    }

    @Override
    public void onDisable() {
        waypointRepository.forEach(wp -> wp.hideAll(plugin.getConfiguration().useLegacyLunarAPI));
    }

    /**
     * Initializes global waypoints
     */
    private void initializeGlobalWaypoints() {
        // Spawn waypoints
        final GlobalWaypoint spawnWaypoint = new GlobalWaypoint("Spawn", plugin.getConfiguration().getOverworldSpawn(), Color.GREEN.getRGB());
        final GlobalWaypoint endSpawnWaypoint = new GlobalWaypoint("End Spawn", plugin.getConfiguration().getEndSpawn(), Color.MAGENTA.getRGB());
        waypointRepository.add(spawnWaypoint);
        waypointRepository.add(endSpawnWaypoint);

        // KOTH waypoints
        plugin.getEventManager().getEventRepository().stream().filter(event -> event instanceof KOTHEvent).forEach(koth -> {
            final KOTHEvent kothEvent = (KOTHEvent) koth;
            final Color color;

            if (kothEvent.getCaptureRegion().getCornerA().getBukkitBlock().getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
                color = Color.CYAN;
            } else if (kothEvent.getCaptureRegion().getCornerA().getBukkitBlock().getWorld().getEnvironment().equals(World.Environment.NETHER)) {
                color = Color.RED;
            } else {
                color = Color.MAGENTA;
            }

            final GlobalWaypoint kothWaypoint = new GlobalWaypoint(
                    ChatColor.stripColor(kothEvent.getDisplayName()),
                    kothEvent.getCaptureRegion().getCenter().getBukkitBlock().getLocation(),
                    color.getRGB(),
                    false
            );

            waypointRepository.add(kothWaypoint);
        });

        // Set waypoint for The Nether, since we don't track nether spawn in our own code
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment().equals(World.Environment.NETHER)) {
                final GlobalWaypoint netherSpawn = new GlobalWaypoint("Nether Spawn", world.getSpawnLocation(), Color.RED.getRGB());
                waypointRepository.add(netherSpawn);
                break;
            }
        }
    }

    /**
     * Iterates over all factions that have homes set and creates a waypoint
     */
    private void initializeFactionHomeWaypoints() {
        plugin.getFactionManager().getPlayerFactions().forEach(pf -> {
            if (pf.getHomeLocation() != null) {
                final FactionWaypoint fp = new FactionWaypoint(pf, "Home", pf.getHomeLocation().getBukkitLocation(), Color.GREEN.getRGB());
                waypointRepository.add(fp);
            }
        });
    }

    /**
     * Query a waypoint by name
     * @param waypointName Waypoint name
     * @return Optional of IWaypoint
     */
    public Optional<IWaypoint> getWaypoint(String waypointName) {
        return waypointRepository.stream().filter(w -> w instanceof GlobalWaypoint && w.getName().equalsIgnoreCase(waypointName)).findFirst();
    }

    /**
     * Query all global waypoints
     * @return Immutable List of Waypoints
     */
    public ImmutableList<GlobalWaypoint> getGlobalWaypoints() {
        final List<GlobalWaypoint> res = Lists.newArrayList();

        waypointRepository
                .stream()
                .filter(w -> w instanceof GlobalWaypoint)
                .forEach(gwp -> res.add(((GlobalWaypoint) gwp)));

        return ImmutableList.copyOf(res);
    }

    /**
     * Query all waypoints being shown to the provided player
     * @param player Player
     * @return Immutable List of Waypoints
     */
    public ImmutableList<IWaypoint> getVisibleWaypoints(Player player) {
        return ImmutableList.copyOf(waypointRepository.stream().filter(wp -> wp.canSee(player)).collect(Collectors.toList()));
    }

    /**
     * Query all waypoints for a Player Faction
     * @param faction Player Faction
     * @return Immutable List of Waypoints
     */
    public ImmutableList<FactionWaypoint> getWaypoints(PlayerFaction faction) {
        final List<FactionWaypoint> res = Lists.newArrayList();

        waypointRepository
                .stream()
                .filter(w -> w instanceof FactionWaypoint && w.getViewingFactionId() != null && w.getViewingFactionId().equals(faction.getUniqueId()))
                .forEach(fwp -> res.add(((FactionWaypoint)fwp)));

        return ImmutableList.copyOf(res);
    }

    /**
     * Send all global waypoints to the provided player
     * @param player Player
     */
    public void sendGlobalWaypoints(Player player) {
        getGlobalWaypoints().forEach(gwp -> gwp.send(player, plugin.getConfiguration().useLegacyLunarAPI));
    }
}
