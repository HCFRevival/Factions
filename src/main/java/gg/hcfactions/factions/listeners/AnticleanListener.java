package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.listeners.events.faction.FactionJoinEvent;
import gg.hcfactions.factions.listeners.events.faction.FactionLeaveEvent;
import gg.hcfactions.factions.models.player.EScoreboardEntryType;
import gg.hcfactions.libs.bukkit.events.impl.PlayerDamagePlayerEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public final class AnticleanListener implements Listener {
    @Getter public final Factions plugin;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getAnticleanManager().getActiveSessions().forEach(session -> session.update(player));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        plugin.getPlayerManager().getPlayerRepository().forEach(factionPlayer ->
                factionPlayer.removeFromScoreboard(player, EScoreboardEntryType.OBFUSCATED));
    }

    @EventHandler
    public void onPlayerDamagePlayer(PlayerDamagePlayerEvent event) {
        if (plugin.getEventManager().getActiveEvents().isEmpty()) {
            return;
        }

        plugin.getAnticleanManager().mergeOrCreateSession(event.getDamager(), event.getDamaged());
    }

    @EventHandler
    public void onFactionJoin(FactionJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getAnticleanManager().getSession(event.getFaction()).ifPresent(session -> session.update(player));
    }

    @EventHandler
    public void onFactionLeave(FactionLeaveEvent event) {
        Player player = event.getPlayer();
        plugin.getAnticleanManager().getSession(event.getFaction()).ifPresent(session -> session.update(player));
    }
}
