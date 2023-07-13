package gg.hcfactions.factions.state.impl;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.state.EServerState;
import gg.hcfactions.factions.models.subclaim.Subclaim;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.state.IServerStateExecutor;
import gg.hcfactions.factions.state.ServerStateManager;
import gg.hcfactions.libs.base.consumer.Promise;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.deathbans.DeathbanService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

@AllArgsConstructor
public final class ServerStateExecutor implements IServerStateExecutor {
    @Getter public ServerStateManager manager;

    @Override
    public void updateState(EServerState state, Promise promise) {
        if (manager.getCurrentState().equals(state)) {
            promise.reject(FError.A_SERVER_STATE_SAME.getErrorDescription());
            return;
        }

        if (state.equals(EServerState.NORMAL) || state.equals(EServerState.SOTW)) {
            manager.setCurrentState(state);
            promise.resolve();
            return;
        }

        if (state.equals(EServerState.EOTW_PHASE_1)) {
            // clear deathbans
            final DeathbanService dbs = (DeathbanService) manager.getPlugin().getService(DeathbanService.class);
            if (dbs != null) {
                new Scheduler(manager.getPlugin()).async(dbs::clearDeathbans).run();
            }

            // remove pvp protection
            manager.getPlugin().getPlayerManager().getPlayerRepository().forEach(factionPlayer -> factionPlayer.finishTimer(ETimerType.PROTECTION));

            // set state and print messages
            manager.setCurrentState(state);
            FMessage.printEotwMessage("Deathbans have been cleared and PvP Protection has been removed");
            promise.resolve();
            return;
        }

        if (state.equals(EServerState.EOTW_PHASE_2)) {
            for (World world : Bukkit.getWorlds()) {
                if (world.getEnvironment().equals(World.Environment.NORMAL)) {
                    final WorldBorder border = world.getWorldBorder();

                    border.setSize(
                            manager.getPlugin().getConfiguration().getEotwBorderShrinkRadius(),
                            manager.getPlugin().getConfiguration().getEotwBorderShrinkRate()
                    );
                }
            }

            Bukkit.getOnlinePlayers().forEach(player -> {
                final boolean bypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);

                if (!player.getWorld().getEnvironment().equals(World.Environment.NORMAL) && !bypass) {
                    player.sendMessage(FMessage.EOTW_PREFIX + "You have been escorted back to the Overworld");
                    player.teleport(manager.getPlugin().getConfiguration().getEndExit());
                }
            });

            manager.setCurrentState(state);

            FMessage.printEotwMessage("All claims have been removed. Claiming is now disabled for the remainder of the map.");
            FMessage.printEotwMessage("The world border will now begin shrinking for the next "
                    + Time.convertToRemaining(manager.getPlugin().getConfiguration().getEotwBorderShrinkRate() * 1000L)
                    + ". Good luck!");

            new Scheduler(manager.getPlugin()).async(() -> {
                manager.getPlugin().getFactionManager().getPlayerFactions().forEach(playerFaction -> {
                    final List<Claim> claims = manager.getPlugin().getClaimManager().getClaimsByOwner(playerFaction);
                    final List<Subclaim> subclaims = manager.getPlugin().getSubclaimManager().getSubclaimsByOwner(playerFaction);

                    claims.forEach(claim -> manager.getPlugin().getClaimManager().deleteClaim(claim));
                    subclaims.forEach(subclaim -> manager.getPlugin().getSubclaimManager().deleteSubclaim(subclaim));
                });

                new Scheduler(manager.getPlugin()).sync(promise::resolve).run();
            }).run();

            final YamlConfiguration conf = manager.getPlugin().loadConfiguration("config");
            conf.set("server_state.current_state", state.getSimpleName());
            manager.getPlugin().saveConfiguration("config", conf);
        }
    }
}
