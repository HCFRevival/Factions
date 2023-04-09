package gg.hcfactions.factions.faction.impl;

import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.events.faction.FactionCreateEvent;
import gg.hcfactions.factions.events.faction.FactionDisbandEvent;
import gg.hcfactions.factions.faction.FactionManager;
import gg.hcfactions.factions.faction.IFactionExecutor;
import gg.hcfactions.factions.models.claim.EClaimBufferType;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.IFactionPlayer;
import gg.hcfactions.libs.base.consumer.Promise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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

        final PlayerFaction faction = new PlayerFaction(factionName);
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

        // TODO: Create disband confirmation menu
        // https://github.com/ares-network/factions/blob/cfeaea32865456da4faac0d9fe4dd742f3d24e9e/src/main/java/com/playares/factions/faction/handler/FactionManagerHandler.java#L82
    }

    @Override
    public void disbandFaction(Player player, String factionName, Promise promise) {

    }

    @Override
    public void createInvite(Player player, String playerName, Promise promise) {

    }

    @Override
    public void revokeInvite(Player player, String playerName, Promise promise) {

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

    }

    @Override
    public void showFactionInfo(Player player, String name) {

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

    }
}
