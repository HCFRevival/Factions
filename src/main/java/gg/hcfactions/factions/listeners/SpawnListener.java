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
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Objects;
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
     * Returns true if the provided player has been warned about changing worlds recently
     * @param player Player
     * @return True if the player has been warned recently
     */
    private boolean hasBeenRecentlyWarned(Player player) {
        return recentlyWarned.contains(player.getUniqueId());
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

        if (!Objects.requireNonNull(from.getWorld()).getEnvironment().equals(Objects.requireNonNull(Objects.requireNonNull(to).getWorld()).getEnvironment())) {
            final UUID uniqueId = player.getUniqueId();
            final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);

            if (factionPlayer.hasTimer(ETimerType.PROTECTION) || factionPlayer.hasTimer(ETimerType.COMBAT)) {
                event.setCancelled(true);

                if (!recentlyWarned.contains(player.getUniqueId())) {
                    if (factionPlayer.hasTimer(ETimerType.COMBAT)) {
                        player.sendMessage(FMessage.ERROR + FError.P_CAN_NOT_CHANGE_WORLDS_CTAG.getErrorDescription());
                    } else if (factionPlayer.hasTimer(ETimerType.PROTECTION)) {
                        player.sendMessage(FMessage.ERROR + FError.P_CAN_NOT_CHANGE_WORLDS_PVP_PROT.getErrorDescription());
                    }

                    recentlyWarned.add(uniqueId);
                    new Scheduler(plugin).sync(() -> recentlyWarned.remove(uniqueId)).delay(20L).run();
                }

                return;
            }
        }

        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL)) {
            new Scheduler(plugin).sync(() -> player.teleport(Objects.requireNonNull(to.getWorld()).getSpawnLocation())).delay(1L).run();
        }
    }
}
