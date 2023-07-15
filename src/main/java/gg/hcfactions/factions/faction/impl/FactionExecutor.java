package gg.hcfactions.factions.faction.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.listeners.events.faction.*;
import gg.hcfactions.factions.faction.FactionManager;
import gg.hcfactions.factions.faction.IFactionExecutor;
import gg.hcfactions.factions.menus.DisbandConfirmationMenu;
import gg.hcfactions.factions.models.claim.EClaimBufferType;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.claim.impl.MapPillar;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.classes.impl.*;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.IFactionPlayer;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.state.EServerState;
import gg.hcfactions.factions.models.subclaim.Subclaim;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import gg.hcfactions.factions.models.waypoint.impl.FactionWaypoint;
import gg.hcfactions.factions.utils.FactionUtil;
import gg.hcfactions.libs.base.consumer.FailablePromise;
import gg.hcfactions.libs.base.consumer.Promise;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.account.AccountService;
import gg.hcfactions.libs.bukkit.services.impl.account.model.AresAccount;
import gg.hcfactions.libs.bukkit.services.impl.ranks.RankService;
import gg.hcfactions.libs.bukkit.utils.Players;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.codehaus.plexus.util.StringUtils;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public record FactionExecutor(@Getter FactionManager manager) implements IFactionExecutor {
    @Override
    public void createPlayerFaction(Player player, String factionName, Promise promise) {
        final FError nameError = manager.getValidator().isValidName(factionName, false);
        if (nameError != null) {
            promise.reject(nameError.getErrorDescription());
            return;
        }

        if (manager.getPlayerFactionByPlayer(player) != null) {
            promise.reject(FError.P_ALREADY_IN_FAC.getErrorDescription());
            return;
        }

        final FactionCreateEvent event = new FactionCreateEvent(player, factionName, false);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            promise.reject(event.getCancelMessage() != null ? event.getCancelMessage() : FError.F_UNABLE_TO_CREATE.getErrorDescription());
            return;
        }

        final PlayerFaction faction = new PlayerFaction(manager, factionName);
        final FactionPlayer factionPlayer = (FactionPlayer) manager.getPlugin().getPlayerManager().getPlayer(player);

        faction.addMember(player.getUniqueId(), PlayerFaction.Rank.LEADER);

        if (factionPlayer != null) {
            faction.addToBalance(factionPlayer.getBalance());
            factionPlayer.setBalance(0.0);
            factionPlayer.addToScoreboard(player);
        }

        manager.getFactionRepository().add(faction);
        promise.resolve();
    }

    @Override
    public void createServerFaction(Player player, String factionName, Promise promise) {
        final FError nameError = manager.getValidator().isValidName(factionName, true);
        if (nameError != null) {
            promise.reject(nameError.getErrorDescription());
            return;
        }

        final FactionCreateEvent event = new FactionCreateEvent(player, factionName, true);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            promise.reject(event.getCancelMessage() != null ? event.getCancelMessage() : FError.F_UNABLE_TO_CREATE.getErrorDescription());
            return;
        }

        final ServerFaction faction = new ServerFaction(factionName);
        manager.getFactionRepository().add(faction);
        promise.resolve();
    }

    @Override
    public void disbandFaction(Player player, Promise promise) {
        final PlayerFaction faction = manager.getPlayerFactionByPlayer(player);
        final boolean eotw = (manager.getPlugin().getServerStateManager().getCurrentState().equals(EServerState.EOTW_PHASE_1)
                || manager.getPlugin().getServerStateManager().getCurrentState().equals(EServerState.EOTW_PHASE_2));

        if (faction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        final PlayerFaction.Member member = faction.getMember(player.getUniqueId());
        if (member == null) {
            promise.reject(FError.P_COULD_NOT_LOAD_F.getErrorDescription());
            return;
        }

        if (!member.getRank().equals(PlayerFaction.Rank.LEADER) && !player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
            promise.reject(FError.P_NOT_ENOUGH_PERMS.getErrorDescription());
            return;
        }

        if (faction.isRaidable() && !player.hasPermission(FPermissions.P_FACTIONS_ADMIN) && !eotw) {
            promise.reject(FError.F_NOT_ALLOWED_RAIDABLE.getErrorDescription());
            return;
        }

        final IFactionPlayer factionPlayer = manager.getPlugin().getPlayerManager().getPlayer(player);

        final DisbandConfirmationMenu menu = new DisbandConfirmationMenu(manager.getPlugin(), player, faction, () -> {
            final List<Claim> claims = manager.getPlugin().getClaimManager().getClaimsByOwner(faction);
            final List<Subclaim> subclaims = manager.getPlugin().getSubclaimManager().getSubclaimsByOwner(faction.getUniqueId());
            final List<FactionWaypoint> waypoints = manager.getPlugin().getWaypointManager().getWaypoints(faction);

            final FactionDisbandEvent event = new FactionDisbandEvent(player, faction);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                promise.reject(event.getCancelMessage() != null ? event.getCancelMessage() : FError.F_UNABLE_TO_DISBAND.getErrorDescription());
                return;
            }

            waypoints.forEach(wp -> {
                wp.hideAll(manager.getPlugin().getConfiguration().useLegacyLunarAPI);
                manager.getPlugin().getWaypointManager().getWaypointRepository().remove(wp);
            });

            faction.getOnlineMembers().forEach(onlineMember -> {
                final FactionPlayer otherFactionPlayer = (FactionPlayer) manager.getPlugin().getPlayerManager().getPlayer(onlineMember.getUniqueId());

                if (otherFactionPlayer != null) {
                    otherFactionPlayer.removeAllFromScoreboard();
                }
            });

            claims.forEach(c -> faction.addToBalance(c.getCost()));

            manager.getFactionRepository().remove(faction);
            claims.forEach(manager.getPlugin().getClaimManager().getClaimRepository()::remove);
            subclaims.forEach(manager.getPlugin().getSubclaimManager().getSubclaimRepository()::remove);

            new Scheduler(manager.getPlugin()).async(() -> {
                manager.deleteFaction(faction);
                claims.forEach(c -> manager.getPlugin().getClaimManager().deleteClaim(c));
                subclaims.forEach(sc -> manager.getPlugin().getSubclaimManager().deleteSubclaim(sc));

                if (factionPlayer != null) {
                    new Scheduler(manager.getPlugin()).sync(() -> {
                        factionPlayer.addToBalance(faction.getBalance());
                        factionPlayer.removeAllFromScoreboard();
                        FMessage.printDepositReceived(player, faction.getBalance());
                    }).run();
                }

                FMessage.broadcastFactionDisbanded(faction.getName(), player.getName());
                new Scheduler(manager.getPlugin()).sync(promise::resolve).run();
            }).run();
        });

        menu.open();
    }

    @Override
    public void disbandFaction(Player player, String factionName, Promise promise) {
        if (!player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
            promise.reject(FError.P_NOT_ENOUGH_PERMS.getErrorDescription());
        }

        final IFaction faction = manager.getFactionByName(factionName);
        if (faction == null) {
            promise.reject(FError.F_NOT_FOUND.getErrorDescription());
            return;
        }

        final IFactionPlayer factionPlayer = manager.getPlugin().getPlayerManager().getPlayer(player);

        final DisbandConfirmationMenu menu = new DisbandConfirmationMenu(manager.getPlugin(), player, faction, () -> {
            final List<Claim> claims = manager.getPlugin().getClaimManager().getClaimsByOwner(faction);
            final List<Subclaim> subclaims = manager.getPlugin().getSubclaimManager().getSubclaimsByOwner(faction.getUniqueId());

            if (faction instanceof final PlayerFaction playerFaction) {
                final List<FactionWaypoint> waypoints = manager.getPlugin().getWaypointManager().getWaypoints(playerFaction);
                final FactionDisbandEvent event = new FactionDisbandEvent(player, playerFaction);

                Bukkit.getPluginManager().callEvent(event);

                waypoints.forEach(wp -> {
                    wp.hideAll(manager.getPlugin().getConfiguration().useLegacyLunarAPI);
                    manager.getPlugin().getWaypointManager().getWaypointRepository().remove(wp);
                });

                playerFaction.getOnlineMembers().forEach(onlineMember -> {
                    final FactionPlayer otherFactionPlayer = (FactionPlayer) manager.getPlugin().getPlayerManager().getPlayer(onlineMember.getUniqueId());

                    if (otherFactionPlayer != null) {
                        otherFactionPlayer.removeAllFromScoreboard();
                    }
                });
            }

            // delete faction, claims and subclaims from repositories
            manager.getFactionRepository().remove(faction);
            claims.forEach(manager.getPlugin().getClaimManager().getClaimRepository()::remove);
            subclaims.forEach(manager.getPlugin().getSubclaimManager().getSubclaimRepository()::remove);

            new Scheduler(manager.getPlugin()).async(() -> {
                // delete from db
                manager.deleteFaction(faction);
                claims.forEach(c -> manager.getPlugin().getClaimManager().deleteClaim(c));
                subclaims.forEach(sc -> manager.getPlugin().getSubclaimManager().deleteSubclaim(sc));

                // refund balance
                if (faction instanceof PlayerFaction) {
                    double claimValue = 0.0;

                    for (Claim c : claims) {
                        claimValue += c.getCost();
                    }

                    final double totalClaimValue = claimValue;

                    if (factionPlayer != null) {
                        new Scheduler(manager.getPlugin()).sync(() -> {
                            factionPlayer.addToBalance(totalClaimValue);
                            factionPlayer.removeAllFromScoreboard();
                            FMessage.printDepositReceived(player, totalClaimValue);
                        }).run();
                    }
                }

                FMessage.broadcastFactionDisbanded(faction.getName(), player.getName());
                new Scheduler(manager.getPlugin()).sync(promise::resolve).run();
            }).run();
        });

        menu.open();
    }

    @Override
    public void createInvite(Player player, String playerName, Promise promise) {
        final AccountService acs = (AccountService) manager.getPlugin().getService(AccountService.class);

        if (acs == null) {
            promise.reject(FError.G_GENERIC_ERROR.getErrorDescription());
            return;
        }

        final PlayerFaction playerFaction = manager.getPlayerFactionByPlayer(player);
        final boolean bypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);

        if (playerFaction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        final PlayerFaction.Member member = playerFaction.getMember(player.getUniqueId());
        if (member == null) {
            promise.reject(FError.P_COULD_NOT_LOAD_F.getErrorDescription());
            return;
        }

        if (!member.getRank().isHigherOrEqual(PlayerFaction.Rank.OFFICER) && !bypass) {
            promise.reject(FError.P_NOT_ENOUGH_PERMS.getErrorDescription());
            return;
        }

        acs.getAccount(playerName, new FailablePromise<>() {
            @Override
            public void resolve(AresAccount aresAccount) {
                if (aresAccount == null) {
                    promise.reject(FError.P_NOT_FOUND.getErrorDescription());
                    return;
                }

                if (playerFaction.getMember(aresAccount.getUniqueId()) != null) {
                    promise.reject(FError.P_ALREADY_IN_OWN_F.getErrorDescription());
                    return;
                }

                if (playerFaction.getPendingInvites().contains(aresAccount.getUniqueId())) {
                    promise.reject(FError.P_ALREADY_HAS_INV_F.getErrorDescription());
                    return;
                }

                playerFaction.getPendingInvites().add(aresAccount.getUniqueId());
                FMessage.printPlayerInvite(player, playerFaction, aresAccount.getUsername());

                if (playerFaction.isRaidable()) {
                    FMessage.printCanNotJoinWhileRaidable(playerFaction, aresAccount.getUsername());
                } else if (playerFaction.isFrozen()) {
                    FMessage.printCanNotJoinWhileFrozen(playerFaction, aresAccount.getUsername());
                }

                if (playerFaction.getMemberHistory().contains(aresAccount.getUniqueId())) {
                    FMessage.printReinviteWillBeConsumed(playerFaction, aresAccount.getUsername());
                }

                final Player invited = Bukkit.getPlayer(aresAccount.getUniqueId());
                if (invited != null && invited.isOnline()) {
                    invited.spigot().sendMessage(new ComponentBuilder
                            (player.getName())
                            .color(net.md_5.bungee.api.ChatColor.GOLD)
                            .append(" has invited you to join ")
                            .color(net.md_5.bungee.api.ChatColor.YELLOW)
                            .append(playerFaction.getName())
                            .color(net.md_5.bungee.api.ChatColor.AQUA)
                            .append(".")
                            .color(net.md_5.bungee.api.ChatColor.YELLOW)
                            .append(" Type ")
                            .color(net.md_5.bungee.api.ChatColor.YELLOW)
                            .append("/f accept " + playerFaction.getName())
                            .color(net.md_5.bungee.api.ChatColor.GOLD)
                            .append(" or ")
                            .color(net.md_5.bungee.api.ChatColor.YELLOW)
                            .append("click here")
                            .underlined(true)
                            .color(net.md_5.bungee.api.ChatColor.GOLD)
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f accept " + playerFaction.getName()))
                            .append(" to join")
                            .color(net.md_5.bungee.api.ChatColor.YELLOW)
                            .underlined(false)
                            .create());
                }

                promise.resolve();
            }

            @Override
            public void reject(String s) {
                promise.reject(s);
            }
        });
    }

    @Override
    public void revokeInvite(Player player, String playerName, Promise promise) {
        final AccountService acs = (AccountService) manager.getPlugin().getService(AccountService.class);

        if (acs == null) {
            promise.reject(FError.G_GENERIC_ERROR.getErrorDescription());
            return;
        }

        final PlayerFaction playerFaction = manager.getPlayerFactionByPlayer(player.getUniqueId());
        final boolean bypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);

        if (playerFaction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        final PlayerFaction.Member member = playerFaction.getMember(player.getUniqueId());
        if (member == null) {
            promise.reject(FError.P_COULD_NOT_LOAD_F.getErrorDescription());
            return;
        }

        if (!member.getRank().isHigherOrEqual(PlayerFaction.Rank.OFFICER) && !bypass) {
            promise.reject(FError.P_NOT_ENOUGH_PERMS.getErrorDescription());
            return;
        }

        acs.getAccount(playerName, new FailablePromise<>() {
            @Override
            public void resolve(AresAccount aresAccount) {
                if (aresAccount == null) {
                    promise.reject(FError.P_NOT_FOUND.getErrorDescription());
                    return;
                }

                if (!playerFaction.getPendingInvites().contains(aresAccount.getUniqueId())) {
                    promise.reject(aresAccount.getUsername() + " does not have a pending invitation");
                    return;
                }

                playerFaction.getPendingInvites().remove(aresAccount.getUniqueId());
                FMessage.printPlayerUninvite(player, playerFaction, aresAccount.getUsername());
                promise.resolve();
            }

            @Override
            public void reject(String s) {
                promise.reject(s);
            }
        });
    }

    @Override
    public void joinFaction(Player player, String factionName, Promise promise) {
        final PlayerFaction faction = manager.getPlayerFactionByName(factionName);
        final boolean bypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);
        final boolean eotw = (manager.getPlugin().getServerStateManager().getCurrentState().equals(EServerState.EOTW_PHASE_1)
                || manager.getPlugin().getServerStateManager().getCurrentState().equals(EServerState.EOTW_PHASE_2));

        if (manager.getPlayerFactionByPlayer(player) != null) {
            promise.reject(FError.P_ALREADY_IN_FAC.getErrorDescription());
            return;
        }

        if (faction == null) {
            promise.reject(FError.F_NOT_FOUND.getErrorDescription());
            return;
        }

        if (!faction.isInvited(player.getUniqueId()) && !bypass) {
            promise.reject(FError.P_NO_INV_TO_F.getErrorDescription());
            return;
        }

        if (faction.isFrozen() && !eotw && !bypass) {
            promise.reject(FError.P_CAN_NOT_JOIN_FROZEN.getErrorDescription());
            return;
        }

        if (faction.isRaidable() && !eotw && !bypass) {
            promise.reject(FError.P_CAN_NOT_JOIN_RAIDABLE.getErrorDescription());
            return;
        }

        if (faction.getMembers().size() >= manager.getPlugin().getConfiguration().getMaxFactionSize() && !bypass) {
            promise.reject(FError.P_CAN_NOT_JOIN_FULL.getErrorDescription());
            return;
        }

        if (faction.isReinvited(player.getUniqueId()) && faction.getReinvites() <= 0 && !eotw && !bypass) {
            promise.reject(FError.P_CAN_NOT_JOIN_NO_REINV.getErrorDescription());
            return;
        }

        final IClass playerClass = manager.getPlugin().getClassManager().getCurrentClass(player);
        if (playerClass != null) {
            final int count = manager.getPlugin().getClassManager().getFactionClassCount(faction, playerClass);

            if (playerClass instanceof Archer) {
                if (count >= manager.getPlugin().getConfiguration().getArcherClassLimit()) {
                    playerClass.deactivate(player, false);
                    player.sendMessage(FMessage.ERROR + FError.C_CLASS_LIMIT_MET.getErrorDescription());
                }
            }

            if (playerClass instanceof Rogue) {
                if (count >= manager.getPlugin().getConfiguration().getRogueClassLimit()) {
                    playerClass.deactivate(player, false);
                    player.sendMessage(FMessage.ERROR + FError.C_CLASS_LIMIT_MET.getErrorDescription());
                }
            }

            if (playerClass instanceof Bard) {
                if (count >= manager.getPlugin().getConfiguration().getBardClassLimit()) {
                    playerClass.deactivate(player, false);
                    player.sendMessage(FMessage.ERROR + FError.C_CLASS_LIMIT_MET.getErrorDescription());
                }
            }

            if (playerClass instanceof Miner) {
                if (count >= manager.getPlugin().getConfiguration().getMinerClassLimit()) {
                    playerClass.deactivate(player, false);
                    player.sendMessage(FMessage.ERROR + FError.C_CLASS_LIMIT_MET.getErrorDescription());
                }
            }

            if (playerClass instanceof Diver) {
                if (count >= manager.getPlugin().getConfiguration().getDiverClassLimit()) {
                    playerClass.deactivate(player, false);
                    player.sendMessage(FMessage.ERROR + FError.C_CLASS_LIMIT_MET.getErrorDescription());
                }
            }
        }

        faction.getPendingInvites().remove(player.getUniqueId());

        if (!eotw) {
            if (faction.isReinvited(player.getUniqueId())) {
                faction.setReinvites(faction.getReinvites() - 1);
                FMessage.printReinviteConsumed(faction, faction.getReinvites());
            } else {
                faction.getMemberHistory().add(player.getUniqueId());
            }
        }

        final FactionPlayer factionPlayer = (FactionPlayer) manager.getPlugin().getPlayerManager().getPlayer(player);
        final List<FactionWaypoint> waypoints = manager.getPlugin().getWaypointManager().getWaypoints(faction);

        if (!waypoints.isEmpty()) {
            waypoints.forEach(wp -> wp.send(player, manager.getPlugin().getConfiguration().useLegacyLunarAPI));
        }

        faction.addMember(player.getUniqueId());
        faction.getOnlineMembers().forEach(onlineMember -> {
            final FactionPlayer onlineFactionPlayer = (FactionPlayer) manager.getPlugin().getPlayerManager().getPlayer(onlineMember.getUniqueId());

            if (onlineFactionPlayer != null) {
                onlineFactionPlayer.addToScoreboard(player);
            }

            if (factionPlayer != null && onlineFactionPlayer != null) {
                factionPlayer.addToScoreboard(onlineFactionPlayer.getBukkit());
            }
        });

        final FactionJoinEvent joinEvent = new FactionJoinEvent(player, faction);
        Bukkit.getPluginManager().callEvent(joinEvent);

        FMessage.printPlayerJoinedFaction(faction, player);
        promise.resolve();
    }

    @Override
    public void leaveFaction(Player player, Promise promise) {
        final PlayerFaction faction = manager.getPlayerFactionByPlayer(player);
        final Claim insideClaim = manager.getPlugin().getClaimManager().getClaimAt(new PLocatable(player));
        final boolean bypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);
        final boolean eotw = (manager.getPlugin().getServerStateManager().getCurrentState().equals(EServerState.EOTW_PHASE_1)
                || manager.getPlugin().getServerStateManager().getCurrentState().equals(EServerState.EOTW_PHASE_2));

        if (faction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        if (faction.isRaidable() && !eotw && !bypass) {
            promise.reject(FError.F_NOT_ALLOWED_RAIDABLE.getErrorDescription());
            return;
        }

        if (faction.isFrozen() && !eotw && !bypass) {
            promise.reject(FError.F_NOT_ALLOWED_WHILE_FROZEN.getErrorDescription());
            return;
        }

        if (faction.getMember(player.getUniqueId()).getRank().equals(PlayerFaction.Rank.LEADER) && faction.getMembersByRank(PlayerFaction.Rank.LEADER).size() <= 1) {
            promise.reject(FError.F_REASSIGN_LEADER.getErrorDescription());
            return;
        }

        if (insideClaim != null && insideClaim.getOwner().equals(faction.getUniqueId()) && !bypass) {
            promise.reject(FError.F_EXIT_CLAIM_BEFORE_LEAVE.getErrorDescription());
            return;
        }

        final FactionPlayer factionPlayer = (FactionPlayer) manager.getPlugin().getPlayerManager().getPlayer(player);
        final List<FactionWaypoint> waypoints = manager.getPlugin().getWaypointManager().getWaypoints(faction);

        if (factionPlayer != null) {
            factionPlayer.removeAllFromScoreboard();
        }

        faction.getMembers().remove(faction.getMember(player.getUniqueId()));

        if (!waypoints.isEmpty()) {
            waypoints.forEach(wp -> wp.hide(player, manager.getPlugin().getConfiguration().useLegacyLunarAPI));
        }

        faction.getOnlineMembers().forEach(onlineMember -> {
            final FactionPlayer onlineFactionPlayer = (FactionPlayer) manager.getPlugin().getPlayerManager().getPlayer(onlineMember.getUniqueId());
            if (onlineFactionPlayer != null) {
                onlineFactionPlayer.removeFromScoreboard(player);
            }
        });

        final FactionLeaveEvent leaveEvent = new FactionLeaveEvent(player, faction, FactionLeaveEvent.Reason.LEAVE);
        Bukkit.getPluginManager().callEvent(leaveEvent);

        FMessage.printPlayerLeftFaction(faction, player);
        promise.resolve();
    }

    @Override
    public void kickFromFaction(Player player, String username, Promise promise) {
        final AccountService acs = (AccountService)manager.getPlugin().getService(AccountService.class);
        final PlayerFaction faction = manager.getPlayerFactionByPlayer(player.getUniqueId());
        final boolean bypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);
        final boolean eotw = (manager.getPlugin().getServerStateManager().getCurrentState().equals(EServerState.EOTW_PHASE_1)
                || manager.getPlugin().getServerStateManager().getCurrentState().equals(EServerState.EOTW_PHASE_2));

        if (acs == null) {
            promise.reject(FError.G_GENERIC_ERROR.getErrorDescription());
            return;
        }

        if (faction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        final PlayerFaction.Member kickingMember = faction.getMember(player.getUniqueId());

        if (kickingMember == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        acs.getAccount(username, new FailablePromise<>() {
            @Override
            public void resolve(AresAccount kickedProfile) {
                if (kickedProfile == null) {
                    promise.reject(FError.P_NOT_FOUND.getErrorDescription());
                    return;
                }

                if (kickedProfile.getUniqueId().equals(player.getUniqueId())) {
                    promise.reject(FError.P_CANT_PERFORM_SELF.getErrorDescription());
                    return;
                }

                final PlayerFaction.Member kickedMember = faction.getMember(kickedProfile.getUniqueId());

                if (kickedMember == null) {
                    promise.reject(FError.P_NOT_IN_OWN_F.getErrorDescription());
                    return;
                }

                if (!kickingMember.getRank().isHigher(kickedMember.getRank()) && !bypass) {
                    promise.reject(FError.P_NOT_ENOUGH_PERMS.getErrorDescription());
                    return;
                }

                if (faction.isRaidable() && !bypass && !eotw) {
                    promise.reject(FError.F_NOT_ALLOWED_RAIDABLE.getErrorDescription());
                    return;
                }

                if (faction.isFrozen() && !bypass && !eotw) {
                    promise.reject(FError.F_NOT_ALLOWED_WHILE_FROZEN.getErrorDescription());
                    return;
                }

                final Player kicked = Bukkit.getPlayer(kickedProfile.getUniqueId());

                if (kicked != null) {
                    final List<FactionWaypoint> waypoints = manager.getPlugin().getWaypointManager().getWaypoints(faction);

                    final FactionPlayer factionPlayer = (FactionPlayer) manager.getPlugin().getPlayerManager().getPlayer(kicked);
                    if (factionPlayer != null) {
                        factionPlayer.removeAllFromScoreboard();
                    }

                    if (!waypoints.isEmpty()) {
                        waypoints.forEach(wp -> wp.hide(kicked, manager.getPlugin().getConfiguration().useLegacyLunarAPI));
                    }

                    faction.getOnlineMembers().forEach(onlineMember -> {
                        final FactionPlayer onlineFactionPlayer = (FactionPlayer) manager.getPlugin().getPlayerManager().getPlayer(onlineMember.getUniqueId());
                        if (onlineFactionPlayer != null) {
                            onlineFactionPlayer.removeFromScoreboard(kicked);
                        }
                    });

                    kicked.sendMessage(FMessage.F_KICKED_FROM_FAC);
                }

                final FactionLeaveEvent leaveEvent = new FactionLeaveEvent(kicked, faction, FactionLeaveEvent.Reason.LEAVE);
                Bukkit.getPluginManager().callEvent(leaveEvent);

                FMessage.printPlayerKickedFromFaction(faction, player, kickedProfile.getUsername());
                faction.removeMember(kickedProfile.getUniqueId());
                promise.resolve();
            }

            @Override
            public void reject(String s) {
                promise.reject(s);
            }
        });
    }

    @Override
    public void kickFromFaction(Player player, String factionName, String username, Promise promise) {}

    @Override
    public void renameFaction(Player player, String newFactionName, Promise promise) {
        final FError nameError = manager.getValidator().isValidName(newFactionName, false);
        if (nameError != null) {
            promise.reject(nameError.getErrorDescription());
            return;
        }

        if (manager.getFactionByName(newFactionName) != null) {
            promise.reject(FError.F_NAME_IN_USE.getErrorDescription());
            return;
        }

        final PlayerFaction pf = manager.getPlayerFactionByPlayer(player);
        if (pf == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        final PlayerFaction.Member member = pf.getMember(player.getUniqueId());
        if (member == null) {
            promise.reject(FError.P_COULD_NOT_LOAD_F.getErrorDescription());
            return;
        }

        if (!member.getRank().isHigherOrEqual(PlayerFaction.Rank.OFFICER) && !player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
            promise.reject(FError.P_NOT_ENOUGH_PERMS.getErrorDescription());
            return;
        }

        final FactionRenameEvent renameEvent = new FactionRenameEvent(pf, pf.getName(), newFactionName);
        Bukkit.getPluginManager().callEvent(renameEvent);
        if (renameEvent.isCancelled()) {
            promise.reject(renameEvent.getCancelMessage());
            return;
        }

        pf.setName(newFactionName);
        pf.sendMessage(
                FMessage.P_NAME + player.getName()
                        + FMessage.LAYER_1 + " has "
                        + FMessage.LAYER_2 + "renamed"
                        + FMessage.LAYER_1 + " your faction to "
                        + FMessage.INFO + newFactionName);

        promise.resolve();
    }

    @Override
    public void renameFaction(Player player, String currentFactionName, String newFactionName, Promise promise) {
        final FError nameError = manager.getValidator().isValidName(newFactionName, true);
        if (nameError != null && !nameError.equals(FError.F_NAME_INVALID)) {
            promise.reject(nameError.getErrorDescription());
            return;
        }

        if (manager.getFactionByName(newFactionName) != null) {
            promise.reject(FError.F_NAME_IN_USE.getErrorDescription());
            return;
        }

        final IFaction faction = manager.getFactionByName(currentFactionName);
        if (faction == null) {
            promise.reject(FError.F_NOT_FOUND.getErrorDescription());
            return;
        }

        final FactionRenameEvent renameEvent = new FactionRenameEvent(faction, faction.getName(), newFactionName);
        Bukkit.getPluginManager().callEvent(renameEvent);
        if (renameEvent.isCancelled()) {
            promise.reject(renameEvent.getCancelMessage());
            return;
        }

        faction.setName(newFactionName);

        if (faction instanceof final PlayerFaction pf) {
            pf.sendMessage(
                    FMessage.P_NAME + player.getName()
                            + FMessage.LAYER_1 + " has "
                            + FMessage.LAYER_2 + "renamed"
                            + FMessage.LAYER_1 + " your faction to "
                            + FMessage.INFO + newFactionName);
        }

        promise.resolve();
    }

    @Override
    public void setFactionHome(Player player, Promise promise) {
        final PlayerFaction faction = manager.getPlayerFactionByPlayer(player);
        final Claim insideClaim = manager.getPlugin().getClaimManager().getClaimAt(new PLocatable(player));
        final boolean bypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);

        if (faction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        final PlayerFaction.Member account = faction.getMember(player.getUniqueId());

        if (account == null && !bypass) {
            promise.reject(FError.P_COULD_NOT_LOAD_P.getErrorDescription());
            return;
        }

        if (account != null && account.getRank().equals(PlayerFaction.Rank.MEMBER) && !bypass) {
            promise.reject(FError.P_NOT_ENOUGH_PERMS.getErrorDescription());
            return;
        }

        if (insideClaim == null || !insideClaim.getOwner().equals(faction.getUniqueId())) {
            promise.reject(FError.F_NOT_STANDING_IN_CLAIM.getErrorDescription());
            return;
        }

        // remove old waypoint
        final FactionWaypoint existingWaypoint = manager.getPlugin().getWaypointManager().getWaypoints(faction).stream().filter(wp -> wp.getName().equalsIgnoreCase("Home")).findFirst().orElse(null);
        if (existingWaypoint != null) {
            existingWaypoint.hideAll(manager.getPlugin().getConfiguration().useLegacyLunarAPI);
            manager.getPlugin().getWaypointManager().getWaypointRepository().remove(existingWaypoint);
        }

        // set new home waypoint
        final FactionWaypoint homeWaypoint = new FactionWaypoint(faction, "Home", player.getLocation(), Color.GREEN.getRGB());
        manager.getPlugin().getWaypointManager().getWaypointRepository().add(homeWaypoint);
        faction.getOnlineMembers().forEach(onlineMember -> {
            final Player bukkitPlayer = onlineMember.getBukkit();

            if (bukkitPlayer != null) {
                homeWaypoint.send(bukkitPlayer, manager.getPlugin().getConfiguration().useLegacyLunarAPI);
            }
        });

        faction.setHomeLocation(new PLocatable(player));
        FMessage.printHomeUpdate(faction, player, faction.getHomeLocation());
        promise.resolve();
    }

    @Override
    public void setFactionHome(Player player, String factionName, Promise promise) {
        final IFaction faction = manager.getFactionByName(factionName);
        final Claim insideClaim = manager.getPlugin().getClaimManager().getClaimAt(new PLocatable(player));

        if (faction == null) {
            promise.reject(FError.F_NOT_FOUND.getErrorDescription());
            return;
        }

        if (insideClaim == null || !insideClaim.getOwner().equals(faction.getUniqueId())) {
            promise.reject(FError.F_NOT_STANDING_IN_CLAIM.getErrorDescription());
            return;
        }

        if (faction instanceof final PlayerFaction playerFaction) {
            // remove old waypoint
            final FactionWaypoint existingWaypoint = manager.getPlugin().getWaypointManager().getWaypoints(playerFaction).stream().filter(wp -> wp.getName().equalsIgnoreCase("Home")).findFirst().orElse(null);
            if (existingWaypoint != null) {
                existingWaypoint.hideAll(manager.getPlugin().getConfiguration().useLegacyLunarAPI);
                manager.getPlugin().getWaypointManager().getWaypointRepository().remove(existingWaypoint);
            }

            // set new home waypoint
            final FactionWaypoint homeWaypoint = new FactionWaypoint(playerFaction, "Home", player.getLocation(), Color.GREEN.getRGB());
            manager.getPlugin().getWaypointManager().getWaypointRepository().add(homeWaypoint);
            playerFaction.getOnlineMembers().forEach(onlineMember -> {
                final Player bukkitPlayer = onlineMember.getBukkit();

                if (bukkitPlayer != null) {
                    homeWaypoint.send(bukkitPlayer, manager.getPlugin().getConfiguration().useLegacyLunarAPI);
                }
            });

            playerFaction.setHomeLocation(new PLocatable(player));
            FMessage.printHomeUpdate(playerFaction, player, playerFaction.getHomeLocation());
        } else {
            final ServerFaction serverFaction = (ServerFaction) faction;
            serverFaction.setHomeLocation(new PLocatable(player));
        }

        promise.resolve();
    }

    @Override
    public void unsetFactionHome(Player player, Promise promise) {

    }

    @Override
    public void unsetFactionHome(Player player, String factionName, Promise promise) {

    }

    @Override
    public void showFactionList(Player player, int page) {
        // create copy because we're modifying it
        final List<PlayerFaction> factions = Lists.newArrayList(getManager().getPlayerFactions());

        factions.sort(Comparator.comparingInt(f -> f.getOnlineMembers().size()));
        Collections.reverse(factions);

        // Fixes page 0 or negative pages
        page = Math.max(page, 1);

        int finishPos = page * 10;
        int startPos = (page - 1) * 10;

        if (startPos > factions.size()) {
            player.sendMessage(FError.G_PAGE_NOT_FOUND.getErrorDescription());
            return;
        }

        final boolean hasNextPage = finishPos < factions.size();
        final boolean hasPrevPage = page > 1;

        player.sendMessage(FMessage.LAYER_2 + "Faction List (" + FMessage.LAYER_1 + "Page " + page + FMessage.LAYER_2 + ")");

        for (int i = startPos; i < finishPos; i++) {
            if (i >= factions.size()) {
                break;
            }

            final PlayerFaction faction = factions.get(i);

            // Faction List
            //  1. RankXI [6/8 Online] [4.8/5.5DTR]

            net.md_5.bungee.api.ChatColor dtrColor;

            if (faction.getDtr() >= 1.0) {
                dtrColor = net.md_5.bungee.api.ChatColor.GREEN;
            } else if (faction.getDtr() >= 0.0) {
                dtrColor = net.md_5.bungee.api.ChatColor.YELLOW;
            } else {
                dtrColor = net.md_5.bungee.api.ChatColor.RED;
            }

            player.spigot().sendMessage(
                    new ComponentBuilder
                            (" ").color(net.md_5.bungee.api.ChatColor.RESET)
                            .append(" ").color(net.md_5.bungee.api.ChatColor.RESET)
                            .append((i + 1) + "." + " ").color(net.md_5.bungee.api.ChatColor.YELLOW)
                            .append(faction.getName() + " ").color(net.md_5.bungee.api.ChatColor.GOLD)
                            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Click to view more information about " + faction.getName()).color(net.md_5.bungee.api.ChatColor.DARK_PURPLE).create()))
                            .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f who " + faction.getName()))
                            .append("[").color(net.md_5.bungee.api.ChatColor.YELLOW)
                            .append(faction.getOnlineMembers().size() + "").color(net.md_5.bungee.api.ChatColor.GREEN)
                            .append(" / " + faction.getMembers().size() + " online").color(net.md_5.bungee.api.ChatColor.YELLOW)
                            .append("] [").color(net.md_5.bungee.api.ChatColor.YELLOW)
                            .append(String.format("%.2f", faction.getDtr())).color(dtrColor)
                            .append(" / " + String.format("%.2f", faction.getMaxDtr()) + "DTR]").color(net.md_5.bungee.api.ChatColor.YELLOW)
                            .create());
        }

        final ComponentBuilder pagination = new ComponentBuilder(" ").color(net.md_5.bungee.api.ChatColor.RESET);

        if (hasPrevPage) {
            pagination.append("[Previous Page] ")
                    .color(net.md_5.bungee.api.ChatColor.RED)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f list " + (page - 1)))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Visit the previous page").color(net.md_5.bungee.api.ChatColor.GRAY).create()));
        }

        if (hasNextPage) {
            pagination.append("[Next Page]")
                    .color(net.md_5.bungee.api.ChatColor.GREEN)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/f list " + (page + 1)))
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Visit the next page").color(net.md_5.bungee.api.ChatColor.GRAY).create()));
        }

        player.spigot().sendMessage(pagination.create());
    }

    @Override
    public void setFactionChatChannel(Player player, PlayerFaction.ChatChannel channel, Promise promise) {
        final PlayerFaction playerFaction = manager.getPlayerFactionByPlayer(player);

        if (playerFaction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        final PlayerFaction.Member member = playerFaction.getMember(player.getUniqueId());
        if (member == null) {
            promise.reject(FError.P_COULD_NOT_LOAD_F.getErrorDescription());
            return;
        }

        if (channel == null) {
            final PlayerFaction.ChatChannel toChannel = member.getChannel().equals(PlayerFaction.ChatChannel.FACTION)
                    ? PlayerFaction.ChatChannel.PUBLIC
                    : PlayerFaction.ChatChannel.FACTION;

            member.setChannel(toChannel);
            FMessage.printChatChannelChange(player, toChannel);
            promise.resolve();
            return;
        }

        if (member.getChannel().equals(PlayerFaction.ChatChannel.PUBLIC)) {
            member.setChannel(PlayerFaction.ChatChannel.FACTION);
            FMessage.printChatChannelChange(player, PlayerFaction.ChatChannel.FACTION);
            promise.resolve();
            return;
        }

        if (member.getChannel().equals(PlayerFaction.ChatChannel.FACTION)) {
            member.setChannel(PlayerFaction.ChatChannel.PUBLIC);
            FMessage.printChatChannelChange(player, PlayerFaction.ChatChannel.PUBLIC);
            promise.resolve();
        }
    }

    @Override
    public void freezeFactionPower(Player player, String factionName, long duration, Promise promise) {
        final PlayerFaction faction = manager.getPlayerFactionByName(factionName);

        if (faction == null) {
            promise.reject(FError.F_NOT_FOUND.getErrorDescription());
            return;
        }

        faction.addTimer(new FTimer(ETimerType.FREEZE, duration));
        FMessage.printFrozenPower(faction, duration);
        promise.resolve();
    }

    @Override
    public void thawFactionPower(Player player, String factionName, Promise promise) {
        final PlayerFaction faction = manager.getPlayerFactionByName(factionName);

        if (faction == null) {
            promise.reject(FError.F_NOT_FOUND.getErrorDescription());
            return;
        }

        if (!faction.isFrozen()) {
            promise.reject(FError.F_NOT_FROZEN.getErrorDescription());
            return;
        }

        faction.finishTimer(ETimerType.FREEZE);
        promise.resolve();
    }

    @Override
    public void setFactionDTR(Player player, String factionName, double dtr, Promise promise) {
        final PlayerFaction faction = manager.getPlayerFactionByName(factionName);

        if (faction == null) {
            promise.reject(FError.F_NOT_FOUND.getErrorDescription());
            return;
        }

        final double setDtr = Math.min(dtr, faction.getMaxDtr());

        faction.setDtr(setDtr);
        FMessage.printDTRUpdate(faction, setDtr);
        promise.resolve();
    }

    @Override
    public void setFactionFlag(Player player, String factionName, ServerFaction.Flag flag, Promise promise) {
        final ServerFaction faction = manager.getServerFactionByName(factionName);

        if (faction == null) {
            promise.reject(FError.F_NOT_FOUND.getErrorDescription());
            return;
        }

        faction.setFlag(flag);
        promise.resolve();
    }

    @Override
    public void setFactionDisplayName(Player player, String factionName, String displayName, Promise promise) {
        final ServerFaction faction = manager.getServerFactionByName(factionName);

        if (faction == null) {
            promise.reject(FError.F_NOT_FOUND.getErrorDescription());
            return;
        }

        faction.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        promise.resolve();
    }

    @Override
    public void setFactionBuffer(Player player, String factionName, EClaimBufferType bufferType, int size, Promise promise) {
        final ServerFaction faction = manager.getServerFactionByName(factionName);

        if (faction == null) {
            promise.reject(FError.F_NOT_FOUND.getErrorDescription());
            return;
        }

        if (bufferType.equals(EClaimBufferType.BUILD)) {
            faction.setBuildBuffer(size);
        }

        if (bufferType.equals(EClaimBufferType.CLAIM)) {
            faction.setClaimBuffer(size);
        }

        promise.resolve();
    }

    @Override
    public void setFactionReinvites(Player player, String factionName, int reinvites, Promise promise) {
        final PlayerFaction faction = manager.getPlayerFactionByName(factionName);

        if (faction == null) {
            promise.reject(FError.F_NOT_FOUND.getErrorDescription());
            return;
        }

        faction.setReinvites(reinvites);
        FMessage.printReinviteUpdate(faction, faction.getReinvites());
        promise.resolve();
    }

    @Override
    public void setFactionReinvitesBulk(Player player, int reinvites, Promise promise) {
        manager.getPlayerFactions().forEach(pf -> {
            pf.setReinvites(reinvites);
            FMessage.printReinviteUpdate(pf, reinvites);
        });

        promise.resolve();
    }

    @Override
    public void setFactionAnnouncement(Player player, String announcement, Promise promise) {
        final PlayerFaction faction = manager.getPlayerFactionByPlayer(player);
        final boolean bypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);

        if (faction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        final PlayerFaction.Member account = faction.getMember(player.getUniqueId());

        if (account == null && !bypass) {
            promise.reject(FError.P_COULD_NOT_LOAD_F.getErrorDescription());
            return;
        }

        if (account != null && account.getRank().equals(PlayerFaction.Rank.MEMBER) && !bypass) {
            promise.reject(FError.P_NOT_ENOUGH_PERMS.getErrorDescription());
            return;
        }

        faction.setAnnouncement(announcement);
        FMessage.printAnnouncement(faction, announcement);
        promise.resolve();
    }

    @Override
    public void setFactionRally(Player player, Promise promise) {
        final PlayerFaction playerFaction = manager.getPlayerFactionByPlayer(player);
        if (playerFaction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        if (playerFaction.hasTimer(ETimerType.RALLY)) {
            promise.reject(FError.F_NOT_ALLOWED_COOLDOWN.getErrorDescription());
            return;
        }

        // remove old rally
        final FactionWaypoint existing = manager.getPlugin().getWaypointManager().getWaypoints(playerFaction).stream().filter(wp -> wp.getName().equalsIgnoreCase("Rally")).findFirst().orElse(null);
        if (existing != null) {
            existing.hideAll(manager.getPlugin().getConfiguration().useLegacyLunarAPI);
            manager.getPlugin().getWaypointManager().getWaypointRepository().remove(existing);
        }

        // send new rally
        final FactionWaypoint rallyWaypoint = new FactionWaypoint(playerFaction, "Rally", player.getLocation(), Color.ORANGE.getRGB());
        manager.getPlugin().getWaypointManager().getWaypointRepository().add(rallyWaypoint);
        playerFaction.getOnlineMembers().forEach(onlineMember -> {
            final Player bukkitPlayer = onlineMember.getBukkit();
            rallyWaypoint.send(bukkitPlayer, manager.getPlugin().getConfiguration().useLegacyLunarAPI);
        });

        playerFaction.setRallyLocation(new PLocatable(player));
        playerFaction.addTimer(new FTimer(ETimerType.RALLY_WAYPOINT, 60));
        playerFaction.addTimer(new FTimer(ETimerType.RALLY, manager.getPlugin().getConfiguration().getRallyDuration()));
        playerFaction.setLastRallyUpdate(Time.now());
        FMessage.printRallyUpdate(player, playerFaction);
        promise.resolve();
    }

    @Override
    public void promotePlayer(Player player, String username, Promise promise) {
        final AccountService acs = (AccountService) manager.getPlugin().getService(AccountService.class);
        final PlayerFaction faction = manager.getPlayerFactionByPlayer(player.getUniqueId());
        final boolean bypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);

        if (acs == null) {
            promise.reject(FError.G_GENERIC_ERROR.getErrorDescription());
            return;
        }

        if (faction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        final PlayerFaction.Member account = faction.getMember(player.getUniqueId());
        final boolean leader = account.getRank().equals(PlayerFaction.Rank.LEADER);

        if (account.getRank().equals(PlayerFaction.Rank.MEMBER) && !bypass) {
            promise.reject(FError.P_NOT_ENOUGH_PERMS.getErrorDescription());
            return;
        }

        acs.getAccount(username, new FailablePromise<>() {
            @Override
            public void resolve(AresAccount promotedAccount) {
                if (promotedAccount == null) {
                    promise.reject(FError.P_NOT_FOUND.getErrorDescription());
                    return;
                }

                final PlayerFaction.Member otherAccount = faction.getMember(promotedAccount.getUniqueId());
                if (otherAccount == null) {
                    promise.reject(FError.P_NOT_IN_OWN_F.getErrorDescription());
                    return;
                }

                if (otherAccount.getRank().isHigherOrEqual(account.getRank()) && !bypass && !leader) {
                    promise.reject(FError.F_HIGHER_RANK.getErrorDescription());
                    return;
                }

                final PlayerFaction.Rank newRank = otherAccount.getRank().getNext();
                if (newRank == null) {
                    promise.reject(FError.F_RANK_NOT_FOUND.getErrorDescription());
                    return;
                }

                otherAccount.setRank(newRank);
                FMessage.printPromotion(player, promotedAccount.getUsername(), faction, newRank);
                promise.resolve();
            }

            @Override
            public void reject(String error) {
                promise.reject(error);
            }
        });
    }

    @Override
    public void demotePlayer(Player player, String username, Promise promise) {
        final AccountService acs = (AccountService) manager.getPlugin().getService(AccountService.class);
        final PlayerFaction faction = manager.getPlayerFactionByPlayer(player);
        final boolean bypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);

        if (acs == null) {
            promise.reject(FError.G_GENERIC_ERROR.getErrorDescription());
            return;
        }

        if (faction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        final PlayerFaction.Member member = faction.getMember(player.getUniqueId());
        final boolean leader = member.getRank().equals(PlayerFaction.Rank.LEADER);

        if (member.getRank().equals(PlayerFaction.Rank.MEMBER) && !bypass) {
            promise.reject(FError.P_NOT_ENOUGH_PERMS.getErrorDescription());
            return;
        }

        acs.getAccount(username, new FailablePromise<>() {
            @Override
            public void resolve(AresAccount demotedAccount) {
                if (demotedAccount == null) {
                    promise.reject(FError.P_NOT_FOUND.getErrorDescription());
                    return;
                }

                final PlayerFaction.Member otherMember = faction.getMember(demotedAccount.getUniqueId());

                if (otherMember == null) {
                    promise.reject(FError.P_NOT_IN_OWN_F.getErrorDescription());
                    return;
                }

                if (otherMember.getRank().isHigherOrEqual(member.getRank()) && !bypass && !leader) {
                    promise.reject(FError.F_HIGHER_RANK.getErrorDescription());
                    return;
                }

                final PlayerFaction.Rank newRank = otherMember.getRank().equals(PlayerFaction.Rank.LEADER) ? PlayerFaction.Rank.OFFICER : PlayerFaction.Rank.MEMBER;

                otherMember.setRank(newRank);
                FMessage.printDemotion(player, demotedAccount.getUsername(), faction, newRank);
                promise.resolve();
            }

            @Override
            public void reject(String error) {
                promise.reject(error);
            }
        });
    }

    @Override
    public void depositMoney(Player player, double amount, Promise promise) {
        final FactionPlayer factionPlayer = (FactionPlayer) manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());
        final PlayerFaction faction = manager.getPlayerFactionByPlayer(player.getUniqueId());
        final BigDecimal roundedAmount = BigDecimal.valueOf(amount);
        final double finalAmount = roundedAmount.setScale(2, RoundingMode.HALF_UP).doubleValue();

        if (finalAmount <= 0.0) {
            promise.reject(FError.P_CAN_NOT_AFFORD.getErrorDescription());
            return;
        }

        if (faction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        if (factionPlayer.getBalance() < finalAmount) {
            promise.reject(FError.P_CAN_NOT_AFFORD.getErrorDescription());
            return;
        }

        factionPlayer.subtractFromBalance(finalAmount);
        faction.setBalance(faction.getBalance() + finalAmount);

        FMessage.printFactionDeposit(faction, player, finalAmount);
        FMessage.printBalance(player, factionPlayer.getBalance());

        promise.resolve();
    }

    @Override
    public void withdrawMoney(Player player, double amount, Promise promise) {
        final FactionPlayer factionPlayer = (FactionPlayer) manager.getPlugin().getPlayerManager().getPlayer(player.getUniqueId());
        final PlayerFaction faction = manager.getPlayerFactionByPlayer(player.getUniqueId());
        final BigDecimal roundedAmount = BigDecimal.valueOf(amount);
        final double finalAmount = roundedAmount.setScale(2, RoundingMode.HALF_UP).doubleValue();

        if (finalAmount <= 0.0) {
            promise.reject(FError.P_CAN_NOT_AFFORD.getErrorDescription());
            return;
        }

        if (faction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        final PlayerFaction.Member factionMember = faction.getMember(player.getUniqueId());

        if (factionMember == null) {
            promise.reject(FError.P_COULD_NOT_LOAD_F.getErrorDescription());
            return;
        }

        if (factionMember.getRank().equals(PlayerFaction.Rank.MEMBER)) {
            promise.reject(FError.P_NOT_ENOUGH_PERMS.getErrorDescription());
            return;
        }

        if (faction.getBalance() < finalAmount) {
            promise.reject(FError.P_CAN_NOT_AFFORD.getErrorDescription());
            return;
        }

        faction.subtractFromBalance(finalAmount);
        factionPlayer.addToBalance(finalAmount);

        FMessage.printFactionWithdrawn(faction, player, finalAmount);
        FMessage.printBalance(player, factionPlayer.getBalance());

        promise.resolve();
    }

    @Override
    public void setBalance(Player player, String factionName, double amount, Promise promise) {
        final PlayerFaction playerFaction = manager.getPlayerFactionByName(factionName);

        if (playerFaction == null) {
            promise.reject(FError.F_NOT_FOUND.getErrorDescription());
            return;
        }

        if (amount <= 0.0) {
            promise.reject("Invalid amount");
            return;
        }

        playerFaction.setBalance(amount);
        FMessage.printFactionBalanceSet(playerFaction, player, amount);
        promise.resolve();
    }

    @Override
    public void addTokens(Player player, String factionName, int amount, Promise promise) {
        final PlayerFaction faction = manager.getPlayerFactionByName(factionName);

        if (faction == null) {
            promise.reject(FError.F_NOT_FOUND.getErrorDescription());
            return;
        }

        faction.addTokens(amount);
        faction.sendMessage(FMessage.P_NAME + player.getName() + FMessage.LAYER_1 + " adjusted your faction tokens to " + FMessage.INFO + faction.getTokens());
        promise.resolve();
    }

    @Override
    public void subtractTokens(Player player, String factionName, int amount, Promise promise) {
        final PlayerFaction faction = manager.getPlayerFactionByName(factionName);

        if (faction == null) {
            promise.reject(FError.F_NOT_FOUND.getErrorDescription());
            return;
        }

        faction.subtractTokens(Math.max(amount, 0));
        faction.sendMessage(FMessage.P_NAME + player.getName() + FMessage.LAYER_1 + " adjusted your faction tokens to " + FMessage.INFO + faction.getTokens());
        promise.resolve();
    }

    @Override
    public void showFactionInfo(Player player) {
        final PlayerFaction playerFaction = manager.getPlayerFactionByPlayer(player);

        if (playerFaction == null) {
            player.sendMessage(FMessage.ERROR + FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        FMessage.printFactionInfo(manager.getPlugin(), player, playerFaction);
    }

    @Override
    public void showFactionInfo(Player player, String name) {
        // TODO: Validate name is alphanumeric and less than 16 characters
        IFaction faction = manager.getFactionByName(name);

        if (faction != null) {
            FMessage.printFactionInfo(manager.getPlugin(), player, faction);
            return;
        }

        final AccountService acs = (AccountService) manager.getPlugin().getService(AccountService.class);
        if (acs == null) {
            player.sendMessage(FMessage.ERROR + FError.G_GENERIC_ERROR.getErrorDescription());
            return;
        }

        acs.getAccount(name, new FailablePromise<>() {
            @Override
            public void resolve(AresAccount aresAccount) {
                if (aresAccount == null) {
                    player.sendMessage(FMessage.ERROR + FError.F_NOT_FOUND.getErrorDescription());
                    return;
                }

                final PlayerFaction playerFaction = manager.getPlayerFactionByPlayer(aresAccount.getUniqueId());

                if (playerFaction == null) {
                    player.sendMessage(FMessage.ERROR + FError.F_NOT_FOUND.getErrorDescription());
                    return;
                }

                FMessage.printFactionInfo(manager.getPlugin(), player, playerFaction);
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + FError.F_NOT_FOUND.getErrorDescription());
            }
        });
    }

    @Override
    public void showFactionMap(Player player, Promise promise) {
        final FactionPlayer factionPlayer = (FactionPlayer) manager.getPlugin().getPlayerManager().getPlayer(player);

        if (factionPlayer == null) {
            promise.reject(FError.P_COULD_NOT_LOAD_P.getErrorDescription());
            return;
        }

        if (factionPlayer.hasMapPillars()) {
            factionPlayer.hideMapPillars();
            player.sendMessage(FMessage.F_MAP_PILLARS_HIDDEN);
            promise.resolve();
            return;
        }

        final PLocatable location = new PLocatable(player);

        new Scheduler(manager.getPlugin()).async(() -> {
            final Set<IFaction> found = Sets.newHashSet();

            for (Claim claim : manager.getPlugin().getClaimManager().getClaimRepository()) {
                for (BLocatable corner : claim.getCorners()) {
                    if (!corner.getWorldName().equals(location.getWorldName())) {
                        continue;
                    }

                    if (corner.getDistance(location) > 64.0) {
                        continue;
                    }

                    final IFaction owner = manager.getFactionById(claim.getOwner());

                    if (owner == null || found.contains(owner)) {
                        continue;
                    }

                    found.add(owner);
                }
            }

            new Scheduler(manager.getPlugin()).sync(() -> {
                if (found.isEmpty()) {
                    promise.reject(FError.F_NOT_FOUND_MULTIPLE.getErrorDescription());
                    return;
                }

                int pos = 0;

                player.sendMessage(FMessage.LAYER_2 + "Faction Map (" + FMessage.LAYER_1 + found.size() + " in your area" + FMessage.LAYER_2 + ")");

                for (IFaction faction : found) {
                    final List<Claim> claims = manager.getPlugin().getClaimManager().getClaimsByOwner(faction);

                    if (claims.isEmpty()) {
                        continue;
                    }

                    final Material mat = FactionUtil.PILLAR_MATS.get(pos);

                    claims.forEach(c -> {
                        for (BLocatable corner : c.getCorners()) {
                            corner.setY(location.getY() - 5);

                            final MapPillar pillar = new MapPillar(player, mat, corner);
                            factionPlayer.getPillars().add(pillar);
                            pillar.draw();
                        }
                    });

                    player.sendMessage(ChatColor.RESET + " " + ChatColor.RESET + " " + FMessage.LAYER_1 + " - "
                            + FMessage.LAYER_2 + faction.getName() + ChatColor.GRAY + " (" + ChatColor.AQUA
                            + StringUtils.capitalise(mat.name().toLowerCase(Locale.ROOT).replaceAll("_", " ")) + ChatColor.GRAY + ")");

                    pos += 1;

                    promise.resolve();
                }
            }).run();
        }).run();
    }

    @Override
    public void startClaiming(Player player, Promise promise) {
        manager.getPlugin().getClaimManager().getClaimBuilderManager().getExecutor().startClaiming(player, promise);
    }

    @Override
    public void startClaiming(Player player, String factionName, Promise promise) {
        if (!player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
            promise.reject(FError.P_NOT_ENOUGH_PERMS.getErrorDescription());
            return;
        }

        final IFaction faction = manager.getFactionByName(factionName);

        if (faction == null) {
            promise.reject(FError.F_NOT_FOUND.getErrorDescription());
            return;
        }

        manager.getPlugin().getClaimManager().getClaimBuilderManager().getExecutor().startClaiming(player, faction, promise);
    }

    @Override
    public void startSubclaiming(Player player, String subclaimName, Promise promise) {
        manager.getPlugin().getSubclaimManager().getExecutor().startSubclaiming(player, subclaimName, promise);
    }

    @Override
    public void modifySubclaim(Player player, String subclaimName, String modifier, String username, Promise promise) {
        if (modifier.equalsIgnoreCase("a") || modifier.equalsIgnoreCase("add")) {
            manager.getPlugin().getSubclaimManager().getExecutor().addToSubclaim(player, subclaimName, username, promise);
            return;
        }

        if (modifier.equalsIgnoreCase("r") || modifier.equalsIgnoreCase("rem") || modifier.equalsIgnoreCase("remove")) {
            manager.getPlugin().getSubclaimManager().getExecutor().removeFromSubclaim(player, subclaimName, username, promise);
            return;
        }

        promise.reject("Invalid modifier. (add or remove)");
    }

    @Override
    public void unclaim(Player player, Promise promise) {
        final PlayerFaction faction = manager.getPlayerFactionByPlayer(player);
        final Claim inside = manager.getPlugin().getClaimManager().getClaimAt(new PLocatable(player));
        final boolean bypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);

        if (faction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        final List<Claim> claims = getManager().getPlugin().getClaimManager().getClaimsByOwner(faction);
        final PlayerFaction.Member member = faction.getMember(player.getUniqueId());

        if (member == null) {
            promise.reject(FError.P_COULD_NOT_LOAD_F.getErrorDescription());
            return;
        }

        if (member.getRank().equals(PlayerFaction.Rank.MEMBER) && !bypass) {
            promise.reject(FError.P_NOT_ENOUGH_PERMS.getErrorDescription());
            return;
        }

        if (claims.isEmpty()) {
            promise.reject("Your faction does not have any claims");
            return;
        }

        if (inside == null) {
            promise.reject("You are not standing inside of a claim");
            return;
        }

        if (!inside.getOwner().equals(faction.getUniqueId())) {
            promise.reject("Your faction does not own this claim");
            return;
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            if (claims.size() >= 3) {
                final List<Claim> otherClaims = claims.stream().filter(c -> !c.getUniqueId().equals(inside.getUniqueId())).collect(Collectors.toList());

                for (Claim oc : otherClaims) {
                    boolean isValid = false;

                    for (BLocatable perimeter : oc.getPerimeter(64)) {
                        final List<Claim> comparedClaims = claims
                                .stream()
                                .filter(c -> !c.getUniqueId().equals(inside.getUniqueId()) && !c.getUniqueId().equals(oc.getUniqueId()))
                                .collect(Collectors.toList());

                        for (Claim cc : comparedClaims) {
                            if (cc.isTouching(perimeter)) {
                                isValid = true;
                                break;
                            }
                        }
                    }

                    if (!isValid) {
                        new Scheduler(manager.getPlugin()).sync(() -> {
                            promise.reject("Claims would no longer be connected");
                        }).run();

                        return;
                    }
                }
            }

            final List<Subclaim> subclaims = manager.getPlugin().getSubclaimManager().getSubclaimsByOwner(faction.getUniqueId());
            final List<Subclaim> subclaimsToRemove = Lists.newArrayList();

            if (!subclaims.isEmpty()) {
                for (Subclaim s : subclaims) {
                    boolean remove = false;

                    for (BLocatable perimeter : s.getPerimeter(64)) {
                        if (inside.isInside(perimeter, true)) {
                            remove = true;
                            break;
                        }
                    }

                    if (remove) {
                        subclaimsToRemove.add(s);
                    }
                }

                subclaimsToRemove.forEach(removedSubclaim -> {
                    manager.getPlugin().getSubclaimManager().getSubclaimRepository().remove(removedSubclaim);
                    manager.getPlugin().getSubclaimManager().deleteSubclaim(removedSubclaim);
                });
            }

            manager.getPlugin().getClaimManager().getClaimRepository().remove(inside);
            manager.getPlugin().getClaimManager().deleteClaim(inside);

            new Scheduler(manager.getPlugin()).sync(() -> {
                if (faction.getHomeLocation() != null && inside.isInside(faction.getHomeLocation(), true)) {
                    faction.setHomeLocation(null);
                    FMessage.printHomeUnset(faction, player);

                    manager.getPlugin().getWaypointManager().getWaypoints(faction)
                            .stream()
                            .filter(wp -> wp.getName().equalsIgnoreCase("Home"))
                            .findFirst()
                            .ifPresent(homeWaypoint -> {
                                homeWaypoint.hideAll(manager.getPlugin().getConfiguration().useLegacyLunarAPI);
                                manager.getPlugin().getWaypointManager().getWaypointRepository().remove(homeWaypoint);
                            });
                }

                final double refunded = inside.getCost() * manager.getPlugin().getConfiguration().getClaimRefundPercent();
                faction.addToBalance(refunded);
                faction.sendMessage(ChatColor.DARK_GREEN + "$" + String.format("%.2f", refunded) + FMessage.LAYER_1 + " has been refunded to your faction balance");

                if (!subclaimsToRemove.isEmpty()) {
                    faction.sendMessage(FMessage.INFO + "" + subclaimsToRemove.size() + FMessage.LAYER_1 + " subclaims have been removed by unclaiming land");
                }

                promise.resolve();
            }).run();
        }).run();
    }

    @Override
    public void unclaim(Player player, String factionName, Promise promise) {
        final IFaction faction = manager.getFactionByName(factionName);
        final Claim inside = manager.getPlugin().getClaimManager().getClaimAt(new PLocatable(player));
        final boolean bypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);

        if (!bypass) {
            promise.reject(FError.P_NOT_ENOUGH_PERMS.getErrorDescription());
            return;
        }

        if (faction == null) {
            promise.reject(FError.F_NOT_FOUND.getErrorDescription());
            return;
        }

        final List<Claim> claims = getManager().getPlugin().getClaimManager().getClaimsByOwner(faction);

        if (claims.isEmpty()) {
            promise.reject("This faction does not have any claims");
            return;
        }

        if (!inside.getOwner().equals(faction.getUniqueId())) {
            promise.reject("This faction does not own this claim");
            return;
        }

        new Scheduler(manager.getPlugin()).async(() -> {
            if (claims.size() >= 3) {
                final List<Claim> otherClaims = claims.stream().filter(c -> !c.getUniqueId().equals(inside.getUniqueId())).collect(Collectors.toList());

                for (Claim oc : otherClaims) {
                    boolean isValid = false;

                    for (BLocatable perimeter : oc.getPerimeter(64)) {
                        final List<Claim> comparedClaims = claims
                                .stream()
                                .filter(c -> !c.getUniqueId().equals(inside.getUniqueId()) && !c.getUniqueId().equals(oc.getUniqueId()))
                                .collect(Collectors.toList());

                        for (Claim cc : comparedClaims) {
                            if (cc.isTouching(perimeter)) {
                                isValid = true;
                                break;
                            }
                        }
                    }

                    if (!isValid) {
                        new Scheduler(manager.getPlugin()).sync(() -> {
                            promise.reject("Claims would no longer be connected");
                        }).run();

                        return;
                    }
                }
            }

            final List<Subclaim> subclaims = manager.getPlugin().getSubclaimManager().getSubclaimsByOwner(faction.getUniqueId());
            final List<Subclaim> subclaimsToRemove = Lists.newArrayList();

            if (faction instanceof PlayerFaction) {
                if (!subclaims.isEmpty()) {
                    for (Subclaim s : subclaims) {
                        boolean remove = false;

                        for (BLocatable perimeter : s.getPerimeter(64)) {
                            if (inside.isInside(perimeter, true)) {
                                remove = true;
                                break;
                            }
                        }

                        if (remove) {
                            subclaimsToRemove.add(s);
                        }
                    }

                    subclaimsToRemove.forEach(removedSubclaim -> {
                        manager.getPlugin().getSubclaimManager().getSubclaimRepository().remove(removedSubclaim);
                        manager.getPlugin().getSubclaimManager().deleteSubclaim(removedSubclaim);
                    });
                }
            }

            manager.getPlugin().getClaimManager().getClaimRepository().remove(inside);
            manager.getPlugin().getClaimManager().deleteClaim(inside);

            new Scheduler(manager.getPlugin()).sync(() -> {
                if (faction.getHomeLocation() != null && inside.isInside(faction.getHomeLocation(), true)) {
                    faction.setHomeLocation(null);

                    if (faction instanceof PlayerFaction) {
                        manager.getPlugin().getWaypointManager().getWaypoints(((PlayerFaction) faction))
                                .stream()
                                .filter(wp -> wp.getName().equalsIgnoreCase("Home"))
                                .findFirst()
                                .ifPresent(homeWaypoint -> homeWaypoint.hideAll(manager.getPlugin().getConfiguration().useLegacyLunarAPI));

                        FMessage.printHomeUnset((PlayerFaction) faction, player);
                    }
                }

                if (faction instanceof final PlayerFaction playerFaction) {
                    final double refunded = inside.getCost() * manager.getPlugin().getConfiguration().getClaimRefundPercent();
                    playerFaction.addToBalance(refunded);
                    playerFaction.sendMessage(ChatColor.DARK_GREEN + "$" + String.format("%.2f", refunded) + FMessage.LAYER_1 + " has been refunded to your faction balance");

                    if (!subclaimsToRemove.isEmpty()) {
                        playerFaction.sendMessage(FMessage.INFO + "" + subclaimsToRemove.size() + FMessage.LAYER_1 + " subclaims have been removed by unclaiming land");
                    }
                }

                promise.resolve();
            }).run();
        }).run();
    }

    @Override
    public void unsubclaim(Player player, Promise promise) {
        final Subclaim inside = manager.getPlugin().getSubclaimManager().getSubclaimAt(new PLocatable(player));

        if (inside == null) {
            promise.reject(FError.F_NOT_IN_SUBCLAIM.getErrorDescription());
            return;
        }

        manager.getPlugin().getSubclaimManager().getExecutor().unsubclaim(player, inside.getName(), promise);
    }

    @Override
    public void unsubclaim(Player player, String subclaimName, Promise promise) {
        manager.getPlugin().getSubclaimManager().getExecutor().unsubclaim(player, subclaimName, promise);
    }

    @Override
    public void showSubclaimList(Player player) {
        manager.getPlugin().getSubclaimManager().getExecutor().listSubclaims(player, new Promise() {
            @Override
            public void resolve() {}

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to display subclaims: " + s);
            }
        });
    }

    @Override
    public void showSubclaimList(Player player, String factionName) {
        final PlayerFaction faction = manager.getPlayerFactionByName(factionName);
        if (faction == null) {
            player.sendMessage(FMessage.ERROR + FError.F_NOT_FOUND.getErrorDescription());
            return;
        }

        manager.getPlugin().getSubclaimManager().getExecutor().listSubclaims(player, faction, new Promise() {
            @Override
            public void resolve() {}

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to display subclaims: " + s);
            }
        });
    }

    @Override
    public void startHomeTimer(Player player, Promise promise) {
        final FactionPlayer factionPlayer = (FactionPlayer) manager.getPlugin().getPlayerManager().getPlayer(player);
        if (factionPlayer == null) {
            promise.reject(FError.P_COULD_NOT_LOAD_P.getErrorDescription());
            return;
        }

        if (factionPlayer.hasTimer(ETimerType.COMBAT)) {
            promise.reject(FError.P_CANT_PERFORM_WHILE_COMBAT_TAGGED.getErrorDescription());
            return;
        }

        final PlayerFaction faction = manager.getPlayerFactionByPlayer(player);
        if (faction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        if (faction.getHomeLocation() == null) {
            promise.reject(FError.F_HOME_UNSET.getErrorDescription());
            return;
        }

        final Claim insideClaim = manager.getPlugin().getClaimManager().getClaimAt(new PLocatable(player));
        if (insideClaim != null) {
            final IFaction insideFaction = manager.getFactionById(insideClaim.getOwner());

            if (insideFaction != null) {
                if (insideFaction instanceof final ServerFaction sf) {
                    if (sf.getFlag().equals(ServerFaction.Flag.SAFEZONE)) {
                        player.setFallDistance(0); // reset fall distance in case they're falling
                        Players.teleportWithVehicle(manager.getPlugin(), player, faction.getHomeLocation().getBukkitLocation());

                        promise.resolve();
                        return;
                    }

                    if (!sf.getFlag().equals(ServerFaction.Flag.LANDMARK)) {
                        promise.reject(FError.F_CANT_WARP_IN_CLAIM.getErrorDescription());
                        return;
                    }
                }

                else if (insideFaction instanceof final PlayerFaction pf) {
                    if (!pf.isMember(player)) {
                        promise.reject(FError.F_CANT_WARP_IN_CLAIM.getErrorDescription());
                        return;
                    }

                    player.setFallDistance(0); // reset fall distance in case they're falling
                    Players.teleportWithVehicle(manager.getPlugin(), player, faction.getHomeLocation().getBukkitLocation());
                    return;
                }
            }
        }

        factionPlayer.addTimer(new FTimer(ETimerType.HOME, manager.getPlugin().getConfiguration().getHomeDuration()));
        promise.resolve();
    }

    @Override
    public void startStuckTimer(Player player, Promise promise) {
        final FactionPlayer factionPlayer = (FactionPlayer) manager.getPlugin().getPlayerManager().getPlayer(player);
        final Claim inside = manager.getPlugin().getClaimManager().getClaimAt(new PLocatable(player));

        if (factionPlayer == null) {
            promise.reject(FError.P_COULD_NOT_LOAD_P.getErrorDescription());
            return;
        }

        if (inside == null) {
            promise.reject(FError.P_NOT_INSIDE_CLAIM.getErrorDescription());
            return;
        }

        if (factionPlayer.hasTimer(ETimerType.STUCK)) {
            promise.reject(FError.P_TIMER_ALREADY_STARTED.getErrorDescription());
            return;
        }

        factionPlayer.addTimer(new FTimer(ETimerType.STUCK, manager.getPlugin().getConfiguration().getStuckDuration()));
        promise.resolve();
    }

    @Override
    public void printTeamLocate(Player player, Promise promise) {
        final PlayerFaction faction = manager.getPlayerFactionByPlayer(player);
        final RankService rankService = (RankService) manager.getPlugin().getService(RankService.class);
        String displayName = ChatColor.RESET + player.getName();

        if (faction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        if (rankService != null) {
            displayName = rankService.getFormattedName(player);
        }

        final String x = String.format("%.2f", player.getLocation().getX());
        final String y = String.format("%.2f", player.getLocation().getY());
        final String z = String.format("%.2f", player.getLocation().getZ());
        final String world = Objects.requireNonNull(player.getLocation().getWorld()).getEnvironment().name().toLowerCase(Locale.ROOT).replaceAll("_", " ");

        faction.sendMessage(FMessage.getFactionFormat(displayName, "Located at " + x + ", " + y + ", " + z + ", " + world));
        promise.resolve();
    }

    @Override
    public void focusPlayer(Player player, Player toFocus, Promise promise) {
        final PlayerFaction faction = manager.getPlayerFactionByPlayer(player);

        if (faction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        if (faction.hasTimer(ETimerType.FOCUS)) {
            promise.reject("Please wait " + Time.convertToRemaining(faction.getTimer(ETimerType.FOCUS).getRemaining()) + " before performing this action again");
            return;
        }

        if (faction.isMember(toFocus)) {
            promise.reject("You can not focus your own faction members");
            return;
        }

        final FactionFocusEvent focusEvent = new FactionFocusEvent(faction, player, toFocus);
        Bukkit.getPluginManager().callEvent(focusEvent);

        if (focusEvent.isCancelled()) {
            promise.reject("Focus event cancelled");
            return;
        }

        faction.addTimer(new FTimer(ETimerType.FOCUS, 60));
        faction.setFocusedPlayerId(toFocus.getUniqueId());
        FMessage.printFocusing(faction, player, toFocus);
        FMessage.printFocusedByFaction(faction, toFocus);
        promise.resolve();
    }
}
