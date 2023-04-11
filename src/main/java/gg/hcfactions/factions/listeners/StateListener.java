package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.state.EServerState;
import gg.hcfactions.libs.bukkit.services.impl.deathbans.event.PlayerDeathbanEvent;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public record StateListener(@Getter Factions plugin) implements Listener {
    @EventHandler /* Adjusts deathbans to SOTW/Normal caps */
    public void onPlayerDeathban(PlayerDeathbanEvent event) {
        final int duration = event.getDeathbanDuration();
        final EServerState currentState = plugin.getServerStateManager().getCurrentState();

        if (currentState.equals(EServerState.SOTW) && duration > plugin.getConfiguration().getSotwMaxDeathbanDuration()) {
            event.setDeathbanDuration(plugin.getConfiguration().getSotwMaxDeathbanDuration());
        } else if (currentState.equals(EServerState.NORMAL) && duration > plugin.getConfiguration().getNormalMaxDeathbanDuration()) {
            event.setDeathbanDuration(plugin.getConfiguration().getNormalMaxDeathbanDuration());
        }
    }
}
