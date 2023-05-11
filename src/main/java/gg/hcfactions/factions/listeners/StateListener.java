package gg.hcfactions.factions.listeners;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.state.EServerState;
import gg.hcfactions.factions.models.stats.EStatisticType;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.deathbans.event.PlayerDeathbanEvent;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.List;
import java.util.UUID;

public final class StateListener implements Listener {
    @Getter public final Factions plugin;
    private final List<UUID> recentlyWarned;

    public StateListener(Factions plugin) {
        this.plugin = plugin;
        this.recentlyWarned = Lists.newArrayList();
    }

    @EventHandler /* Adjusts deathbans to SOTW/Normal caps */
    public void onPlayerDeathban(PlayerDeathbanEvent event) {
        final int duration = event.getDeathbanDuration();
        final EServerState currentState = plugin.getServerStateManager().getCurrentState();

        // TODO: Do we need this? Is this overwriting a value it shouldn't?
        if (currentState.equals(EServerState.SOTW) && duration > plugin.getConfiguration().getSotwMaxDeathbanDuration()) {
            event.setDeathbanDuration(plugin.getConfiguration().getSotwMaxDeathbanDuration());
        } else if (currentState.equals(EServerState.NORMAL) && duration > plugin.getConfiguration().getNormalMaxDeathbanDuration()) {
            event.setDeathbanDuration(plugin.getConfiguration().getNormalMaxDeathbanDuration());
        }
    }

    @EventHandler /* Handles preventing teleporting to nether/end during EOTW */
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        final Player player = event.getPlayer();

        if (
                event.getCause().equals(PlayerTeleportEvent.TeleportCause.COMMAND)
                || event.getCause().equals(PlayerTeleportEvent.TeleportCause.PLUGIN)) {

            return;
        }

        final Location to = event.getTo();
        if (to == null) {
            return;
        }

        final World toWorld = to.getWorld();
        if (toWorld == null) {
            return;
        }

        if (toWorld.getEnvironment().equals(World.Environment.NORMAL)) {
            return;
        }

        if (player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
            return;
        }

        if (!plugin.getServerStateManager().getCurrentState().equals(EServerState.EOTW_PHASE_2)) {
            return;
        }

        event.setCancelled(true);

        if (!recentlyWarned.contains(player.getUniqueId())) {
            player.sendMessage(FMessage.ERROR + FError.P_CAN_NOT_WARP_EOTW.getErrorDescription());
            recentlyWarned.add(player.getUniqueId());
            new Scheduler(plugin).sync(() -> recentlyWarned.remove(player.getUniqueId())).run();
        }
    }

    @EventHandler /* Handles kicking fresh accounts from the server during EOTW Phase #2 */
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        if (!plugin.getServerStateManager().getCurrentState().equals(EServerState.EOTW_PHASE_2)) {
            return;
        }

        plugin.getStatsManager().getPlayerStatistics(player.getUniqueId(), holder -> {
            if (holder == null) {
                plugin.getAresLogger().warn(player.getName() + " is connected without a stats holder");
                return;
            }

            final int playtimeSeconds = (int)(holder.getStatistic(EStatisticType.PLAYTIME)/1000L);

            if (playtimeSeconds < 300 && !player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
                player.kickPlayer(FMessage.ERROR + FError.P_CAN_NOT_CONNECT_EOTW.getErrorDescription());
            }
        });
    }
}
