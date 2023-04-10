package gg.hcfactions.factions.listeners;

import com.mongodb.client.model.Filters;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.IFactionPlayer;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.utils.FactionUtil;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.UUID;

@AllArgsConstructor
public final class FactionPlayerListener implements Listener {
    @Getter public final Factions plugin;

    @EventHandler /* Handles loading FactionPlayer in to memory */
    public void onPlayerLoad(AsyncPlayerPreLoginEvent event) {
        final UUID uniqueId = event.getUniqueId();
        final String username = event.getName();
        final IFactionPlayer cached = plugin.getPlayerManager().getPlayer(uniqueId);

        if (cached != null) {
            if (!cached.getUsername().equals(username)) {
                cached.setUsername(username);
            }

            return;
        }

        IFactionPlayer loaded = plugin.getPlayerManager().loadPlayer(Filters.eq("uuid", uniqueId.toString()), true);
        if (loaded != null) {
            loaded.setUsername(username);
            return;
        }

        // loadPlayer didn't find the account, create a new one and write to repo
        loaded = new FactionPlayer(plugin.getPlayerManager(), uniqueId, username);
        plugin.getPlayerManager().getPlayerRepository().add(loaded);
    }

    @EventHandler /* Handles printing join message for faction members */
    public void onJoinMessage(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player);

        event.setJoinMessage(null);

        if (faction != null) {
            FMessage.printFactionMemberOnline(faction, player.getName());
        }
    }

    @EventHandler /* Handles printing quit message for faction members */
    public void onQuitMessage(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player);

        event.setQuitMessage(null);

        if (faction != null) {
            FMessage.printFactionMemberOffline(faction, player.getName());
        }
    }

    @EventHandler /* Handles setting up player if they need to be reset & configures faction scoreboard */
    public void onPlayerSetup(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player);
        final IFactionPlayer factionPlayer = plugin.getPlayerManager().getPlayer(player);

        if (factionPlayer == null) {
            player.kickPlayer(FMessage.ERROR + FError.P_COULD_NOT_LOAD_P.getErrorDescription());
            return;
        }

        if (!player.hasPlayedBefore() || factionPlayer.isResetOnJoin()) {
            FactionUtil.cleanPlayer(plugin, player);
        }

        if (faction != null) {
            faction.setupScoreboard(player);
        }
    }

    @EventHandler /* Destory scoreboard data when player disconnects */
    public void onPlayerDestory(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player);

        if (faction != null) {
            faction.destroyScoreboard(player);
        }
    }

    @EventHandler /* Clean player when they click the respawn button */
    public void onRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        final IFactionPlayer factionPlayer = plugin.getPlayerManager().getPlayer(player);

        if (!factionPlayer.isResetOnJoin()) {
            FactionUtil.cleanPlayer(plugin, player);
        }
    }

    @EventHandler /* Saves player data and decaches their data */
    public void onDecache(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final IFactionPlayer cached = plugin.getPlayerManager().getPlayer(player);

        if (cached == null) {
            return;
        }

        plugin.getPlayerManager().getPlayerRepository().remove(cached);
        new Scheduler(plugin).async(() -> plugin.getPlayerManager().savePlayer(cached)).run();
    }
}