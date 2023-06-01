package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public final class WaypointListener implements Listener {
    @Getter public Factions plugin;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player);

        new Scheduler(plugin).sync(() -> {
            plugin.getWaypointManager().sendGlobalWaypoints(player);

            if (faction != null) {
                plugin.getWaypointManager().getWaypoints(faction).forEach(fwp -> fwp.send(player, plugin.getConfiguration().useLegacyLunarAPI));
            }
        }).delay(20L).run();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        plugin.getWaypointManager().getVisibleWaypoints(player).forEach(wp -> wp.hide(player, plugin.getConfiguration().useLegacyLunarAPI));
    }
}
