package gg.hcfactions.factions.faction.impl;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.events.faction.FactionCreateEvent;
import gg.hcfactions.factions.events.faction.FactionDisbandEvent;
import gg.hcfactions.factions.faction.FactionManager;
import gg.hcfactions.factions.faction.IFactionExecutor;
import gg.hcfactions.factions.menus.DisbandConfirmationMenu;
import gg.hcfactions.factions.models.claim.EClaimBufferType;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.IFactionPlayer;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import gg.hcfactions.libs.base.consumer.FailablePromise;
import gg.hcfactions.libs.base.consumer.Promise;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.account.AccountService;
import gg.hcfactions.libs.bukkit.services.impl.account.model.AresAccount;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

@AllArgsConstructor
public final class FactionExecutor implements IFactionExecutor {
    @Getter public final FactionManager manager;

    @Override
    public void createPlayerFaction(Player player, String factionName, Promise promise) {
        final FError nameError = manager.getValidator().isValidName(factionName);
        if (nameError != null) {
            promise.reject(nameError.getErrorDescription());
            return;
        }

        if (manager.getPlayerFactionByPlayer(player) != null) {
            promise.reject(FError.P_ALREADY_IN_FAC.getErrorDescription());
            return;
        }

        final FactionCreateEvent event = new FactionCreateEvent(player, factionName);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            promise.reject(event.getCancelMessage() != null ? event.getCancelMessage() : FError.F_UNABLE_TO_CREATE.getErrorDescription());
            return;
        }

        final PlayerFaction faction = new PlayerFaction(manager, factionName);
        faction.addMember(player.getUniqueId(), PlayerFaction.Rank.LEADER);
        faction.setupScoreboard(player);

        final IFactionPlayer factionPlayer = manager.getPlugin().getPlayerManager().getPlayer(player);
        if (factionPlayer != null) {
            faction.addToBalance(factionPlayer.getBalance());
            factionPlayer.setBalance(0.0);
        }

        manager.getFactionRepository().add(faction);
        promise.resolve();
    }

    @Override
    public void createServerFaction(Player player, String factionName, Promise promise) {
        final FError nameError = manager.getValidator().isValidName(factionName);
        if (nameError != null && !nameError.equals(FError.F_NAME_INVALID)) {
            promise.reject(nameError.getErrorDescription());
            return;
        }

        final FactionCreateEvent event = new FactionCreateEvent(player, factionName);
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

        if (faction.isRaidable() && !player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
            promise.reject(FError.F_NOT_ALLOWED_RAIDABLE.getErrorDescription());
            return;
        }

        final FactionDisbandEvent event = new FactionDisbandEvent(player, faction);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            promise.reject(event.getCancelMessage() != null ? event.getCancelMessage() : FError.F_UNABLE_TO_DISBAND.getErrorDescription());
            return;
        }

        final IFactionPlayer factionPlayer = manager.getPlugin().getPlayerManager().getPlayer(player);

        final DisbandConfirmationMenu menu = new DisbandConfirmationMenu(manager.getPlugin(), player, faction, () -> {
            final List<Claim> claims = manager.getPlugin().getClaimManager().getClaimsByOwner(faction);

            claims.forEach(c -> faction.addToBalance(c.getCost()));

            manager.getFactionRepository().remove(faction);
            claims.forEach(manager.getPlugin().getClaimManager().getClaimRepository()::remove);

            new Scheduler(manager.getPlugin()).async(() -> {
                manager.deleteFaction(faction);
                claims.forEach(c -> manager.getPlugin().getClaimManager().deleteClaim(c));

                if (factionPlayer != null) {
                    new Scheduler(manager.getPlugin()).sync(() -> {
                        factionPlayer.addToBalance(faction.getBalance());
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

            // delete faction, claims and subclaims from repositories
            manager.getFactionRepository().remove(faction);
            claims.forEach(manager.getPlugin().getClaimManager().getClaimRepository()::remove);

            new Scheduler(manager.getPlugin()).async(() -> {
                // delete from db
                manager.deleteFaction(faction);
                claims.forEach(c -> manager.getPlugin().getClaimManager().deleteClaim(c));

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
                            FMessage.printDepositReceived(player, totalClaimValue);
                        }).run();
                    }
                }

                FMessage.broadcastFactionDisbanded(faction.getName(), player.getName());
                new Scheduler(manager.getPlugin()).sync(promise::resolve);
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

        if (!member.getRank().isHigherOrEqual(PlayerFaction.Rank.OFFICER)) {
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

    }

    @Override
    public void leaveFaction(Player player, Promise promise) {

    }

    @Override
    public void kickFromFaction(Player player, String username, Promise promise) {

    }

    @Override
    public void kickFromFaction(Player player, String factionName, String username, Promise promise) {

    }

    @Override
    public void renameFaction(Player player, String newFactionName, Promise promise) {
        final FError nameError = manager.getValidator().isValidName(newFactionName);
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
        final FError nameError = manager.getValidator().isValidName(newFactionName);
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

        faction.setName(newFactionName);

        if (faction instanceof PlayerFaction) {
            final PlayerFaction pf = (PlayerFaction) faction;
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

    }

    @Override
    public void setFactionHome(Player player, String factionName, Promise promise) {

    }

    @Override
    public void unsetFactionHome(Player player, Promise promise) {

    }

    @Override
    public void unsetFactionHome(Player player, String factionName, Promise promise) {

    }

    @Override
    public void showFactionList(Player player, int page) {

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

    }

    @Override
    public void thawFactionPower(Player player, String factionName, Promise promise) {

    }

    @Override
    public void setFactionDTR(Player player, String factionName, double dtr, Promise promise) {

    }

    @Override
    public void setFactionFlag(Player player, String factionName, ServerFaction.Flag flag, Promise promise) {

    }

    @Override
    public void setFactionDisplayName(Player player, String factionName, String displayName, Promise promise) {

    }

    @Override
    public void setFactionBuffer(Player player, String factionName, EClaimBufferType bufferType, int size, Promise promise) {

    }

    @Override
    public void setFactionReinvites(Player player, String factionName, int reinvites, Promise promise) {

    }

    @Override
    public void setFactionAnnouncement(Player player, String announcement, Promise promise) {

    }

    @Override
    public void setFactionRally(Player player, Promise promise) {
        final PlayerFaction playerFaction = manager.getPlayerFactionByPlayer(player);
        if (playerFaction == null) {
            promise.reject(FError.P_NOT_IN_FAC.getErrorDescription());
            return;
        }

        final PlayerFaction.Member member = playerFaction.getMember(player.getUniqueId());
        if (member == null) {
            promise.reject(FError.P_COULD_NOT_LOAD_P.getErrorDescription());
            return;
        }

        if (!member.getRank().isHigherOrEqual(PlayerFaction.Rank.OFFICER) && !player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
            promise.reject(FError.P_NOT_ENOUGH_PERMS.getErrorDescription());
            return;
        }

        if (playerFaction.hasTimer(ETimerType.RALLY)) {
            promise.reject(FError.F_NOT_ALLOWED_COOLDOWN.getErrorDescription());
            return;
        }

        playerFaction.setRallyLocation(new PLocatable(player));
        playerFaction.addTimer(new FTimer(ETimerType.RALLY, manager.getPlugin().getConfiguration().getRallyDuration()));
        playerFaction.setLastRallyUpdate(Time.now());
        FMessage.printRallyUpdate(player, playerFaction);
        promise.resolve();
    }

    @Override
    public void promotePlayer(Player player, String username, Promise promise) {

    }

    @Override
    public void demotePlayer(Player player, String username, Promise promise) {

    }

    @Override
    public void depositMoney(Player player, double amount, Promise promise) {

    }

    @Override
    public void withdrawMoney(Player player, double amount, Promise promise) {

    }

    @Override
    public void showFactionInfo(Player player) {
        // TODO: Remove debug and add error checking
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
    public void showFactionMap(Player player) {

    }

    @Override
    public void startClaiming(Player player, Promise promise) {

    }

    @Override
    public void startClaiming(Player player, String factionName, Promise promise) {

    }

    @Override
    public void startSubclaiming(Player player, String subclaimName, Promise promise) {

    }

    @Override
    public void modifySubclaim(Player player, String subclaimName, String modifier, String username, Promise promise) {

    }

    @Override
    public void unsubclaim(Player player, Promise promise) {

    }

    @Override
    public void unsubclaim(Player player, String subclaimName, Promise promise) {

    }

    @Override
    public void showSubclaimList(Player player) {

    }

    @Override
    public void showSubclaimList(Player player, String factionName) {

    }

    @Override
    public void startHomeTimer(Player player, Promise promise) {

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
}
