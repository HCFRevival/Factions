package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.state.EServerState;
import gg.hcfactions.factions.state.ServerStateManager;
import gg.hcfactions.libs.bukkit.services.impl.deathbans.event.PlayerDeathbanEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

@AllArgsConstructor
public final class DeathbanListener implements Listener {
    @Getter public final Factions plugin;

    // TODO: Reimpl Combat Logger death here

    @EventHandler (priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final UUID uniqueId = player.getUniqueId();
        final ServerStateManager stateManager = plugin.getServerStateManager();

        final PlayerDeathbanEvent de = new PlayerDeathbanEvent(
                uniqueId,
                event.getDeathMessage(),
                30,
                stateManager.getCurrentState().equals(EServerState.SOTW),
                (stateManager.getCurrentState().equals(EServerState.EOTW_PHASE_1) || stateManager.getCurrentState().equals(EServerState.EOTW_PHASE_2))
        );

        Bukkit.getPluginManager().callEvent(de);
    }
}
