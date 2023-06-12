package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.faction.FactionDisbandEvent;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public record EventListener(@Getter Factions plugin) implements Listener {
    /**
     * Wipes existing leaderboard data for KOTH events
     * when a faction disbands
     *
     * @param event FactionDisbandEvent
     */
    @EventHandler
    public void onFactionDisband(FactionDisbandEvent event) {
        plugin.getEventManager().getActiveKothEvents().forEach(kothEvent -> kothEvent.getSession().getLeaderboard().remove(event.getFaction().getUniqueId()));
    }
}
