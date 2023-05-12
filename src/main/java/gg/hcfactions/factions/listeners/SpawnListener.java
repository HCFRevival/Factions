package gg.hcfactions.factions.listeners;

import com.google.common.collect.Sets;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Set;
import java.util.UUID;

public final class SpawnListener implements Listener {
    @Getter public final Factions plugin;
    @Getter public final Set<UUID> recentlyWarned;

    public SpawnListener(Factions plugin) {
        this.plugin = plugin;
        this.recentlyWarned = Sets.newConcurrentHashSet();
    }

    /**
     * Handles teleporting players to spawn locations when they change worlds and prevents players
     * with PvP Protection and Combat-tag from changing worlds
     * @param event PlayerTeleportEvent
     */
    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        final Player player = event.getPlayer();
        final Location from = event.getFrom();
        final Location to = event.getTo();

        if (from.getWorld() != null && to != null && to.getWorld() != null) {
            if (!from.getWorld().getEnvironment().equals(to.getWorld().getEnvironment())) {
                final UUID uniqueId = player.getUniqueId();
                final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);

                if (factionPlayer != null && factionPlayer.hasTimer(ETimerType.PROTECTION)) {
                    event.setCancelled(true);

                    if (!recentlyWarned.contains(uniqueId)) {
                        player.sendMessage(FMessage.ERROR + FError.P_CAN_NOT_CHANGE_WORLDS_PVP_PROT.getErrorDescription());
                        recentlyWarned.add(uniqueId);
                        new Scheduler(plugin).sync(() -> recentlyWarned.remove(uniqueId)).delay(20L).run();
                    }
                }
            }
        }
    }

    @EventHandler /* Teleports player to end exit */
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (event.getRespawnReason().equals(PlayerRespawnEvent.RespawnReason.END_PORTAL)) {
            event.setRespawnLocation(plugin.getConfiguration().getEndExit());
        }
    }

    @EventHandler /* Teleports player to end spawn */
    public void onTeleportToSpawn(PlayerTeleportEvent event) {
        final Location from = event.getFrom();
        final Location to = event.getTo();

        if (to == null || to.getWorld() == null || from.getWorld() == null) {
            return;
        }

        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.COMMAND) || event.getCause().equals(PlayerTeleportEvent.TeleportCause.PLUGIN)) {
            return;
        }

        // teleporting in to the end
        if (to.getWorld().getEnvironment().equals(World.Environment.THE_END) && !from.getWorld().getEnvironment().equals(World.Environment.THE_END)) {
            event.setTo(plugin.getConfiguration().getEndSpawn());
        }
    }
}
