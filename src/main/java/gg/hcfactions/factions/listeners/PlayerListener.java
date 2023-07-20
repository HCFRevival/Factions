package gg.hcfactions.factions.listeners;

import com.google.common.collect.Lists;
import com.mongodb.client.model.Filters;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.IFactionPlayer;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.state.EServerState;
import gg.hcfactions.factions.utils.FactionUtil;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.deathbans.DeathbanService;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.List;
import java.util.UUID;

public final class PlayerListener implements Listener {
    @Getter public final Factions plugin;
    @Getter public final List<UUID> recentlyDisconnected;

    public PlayerListener(Factions plugin) {
        this.plugin = plugin;
        this.recentlyDisconnected = Lists.newArrayList();
    }

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

    @EventHandler (priority = EventPriority.HIGHEST) /* Handles enforcing reconnect attempts */
    public void onPlayerReconnectAttempt(AsyncPlayerPreLoginEvent event) {
        final UUID uniqueId = event.getUniqueId();

        if (recentlyDisconnected.contains(uniqueId)) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(FMessage.ERROR + "Please wait a moment before trying to re-connect");
        }
    }

    @EventHandler /* Handles printing join message for faction members */
    public void onJoinMessage(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player);

        event.setJoinMessage(null);

        if (faction != null) {
            FMessage.printFactionMemberOnline(faction, player.getName());

            // delay the message so that it doesn't get meshed with other join message crap
            new Scheduler(plugin).sync(() -> FMessage.printFactionInfo(plugin, player, faction)).delay(5L).run();
        }
    }

    @EventHandler /* Handles applying a cooldown to a player which disallows them from quickly relogging */
    public void onDisconnectCooldownApply(PlayerQuitEvent event) {
        final Player player = event.getPlayer();

        recentlyDisconnected.add(player.getUniqueId());
        new Scheduler(plugin).sync(() -> recentlyDisconnected.remove(player.getUniqueId())).delay(plugin.getConfiguration().getReconnectCooldownDuration() * 20L).run();
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
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);

        if (factionPlayer == null) {
            player.kickPlayer(FMessage.ERROR + FError.P_COULD_NOT_LOAD_P.getErrorDescription());
            return;
        }

        if (!player.hasPlayedBefore() || factionPlayer.isResetOnJoin()) {
            FactionUtil.cleanPlayer(plugin, factionPlayer);
            player.teleport(plugin.getConfiguration().getOverworldSpawn());

            if (!player.hasPlayedBefore() && !plugin.getServerStateManager().getCurrentState().equals(EServerState.KITMAP)) {
                // give first-time lives if they're connecting for the first time this map and it's not a kitmap
                final DeathbanService deathbanService = (DeathbanService) plugin.getService(DeathbanService.class);

                if (deathbanService != null) {
                    deathbanService.giveFirstTimeLives(player);
                }
            }

            if (factionPlayer.isResetOnJoin()) {
                factionPlayer.setResetOnJoin(false);

                // additional inventory wipe to prevent combat logger item dupe
                new Scheduler(plugin).sync(() -> {
                    player.getInventory().clear();
                    player.getInventory().setArmorContents(null);
                }).delay(1L).run();
            }
        }

        factionPlayer.setupScoreboard();

        if (faction != null) {
            faction.getOnlineMembers().forEach(onlineMember -> {
                final FactionPlayer otherFactionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(onlineMember.getUniqueId());

                factionPlayer.addToScoreboard(onlineMember.getBukkit());

                if (otherFactionPlayer != null) {
                    otherFactionPlayer.addToScoreboard(player);
                }
            });
        }
    }

    @EventHandler /* Destory scoreboard data when player disconnects */
    public void onPlayerDestory(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);
        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player);

        if (factionPlayer != null) {
            factionPlayer.destroyScoreboard();
        }

        if (faction != null) {
            faction.getOnlineMembers().forEach(onlineMember -> {
                final FactionPlayer otherFactionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(onlineMember.getUniqueId());

                if (otherFactionPlayer != null && otherFactionPlayer.getScoreboard() != null) {
                    otherFactionPlayer.removeFromScoreboard(player);
                }
            });
        }
    }

    @EventHandler /* Clean player when they click the respawn button */
    public void onRespawn(PlayerRespawnEvent event) {
        if (event.getRespawnReason().equals(PlayerRespawnEvent.RespawnReason.END_PORTAL)) {
            return;
        }

        final Player player = event.getPlayer();
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);
        FactionUtil.cleanPlayer(plugin, factionPlayer);
        event.setRespawnLocation(plugin.getConfiguration().getOverworldSpawn());
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
