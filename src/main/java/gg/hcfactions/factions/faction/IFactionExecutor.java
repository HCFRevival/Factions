package gg.hcfactions.factions.faction;

import gg.hcfactions.factions.models.claim.EClaimBufferType;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.libs.base.consumer.Promise;
import org.bukkit.entity.Player;

public interface IFactionExecutor {
    void createPlayerFaction(Player player, String factionName, Promise promise);
    void createServerFaction(Player player, String factionName, Promise promise);
    void disbandFaction(Player player, Promise promise);
    void disbandFaction(Player player, String factionName, Promise promise);
    void createInvite(Player player, String playerName, Promise promise);
    void revokeInvite(Player player, String playerName, Promise promise);
    void joinFaction(Player player, String factionName, Promise promise);
    void leaveFaction(Player player, Promise promise);
    void kickFromFaction(Player player, String username, Promise promise);
    void kickFromFaction(Player player, String factionName, String username, Promise promise);
    void renameFaction(Player player, String newFactionName, Promise promise);
    void renameFaction(Player player, String currentFactionName, String newFactionName, Promise promise);
    void setFactionHome(Player player, Promise promise);
    void setFactionHome(Player player, String factionName, Promise promise);
    void unsetFactionHome(Player player, Promise promise);
    void unsetFactionHome(Player player, String factionName, Promise promise);
    void showFactionList(Player player, int page);
    void setFactionChatChannel(Player player, PlayerFaction.ChatChannel channel, Promise promise);
    void freezeFactionPower(Player player, String factionName, long duration, Promise promise);
    void thawFactionPower(Player player, String factionName, Promise promise);
    void setFactionDTR(Player player, String factionName, double dtr, Promise promise);
    void setFactionFlag(Player player, String factionName, ServerFaction.Flag flag, Promise promise);
    void setFactionDisplayName(Player player, String factionName, String displayName, Promise promise);
    void setFactionBuffer(Player player, String factionName, EClaimBufferType bufferType, int size, Promise promise);
    void setFactionReinvites(Player player, String factionName, int reinvites, Promise promise);
    void setFactionAnnouncement(Player player, String announcement, Promise promise);
    void setFactionRally(Player player, Promise promise);
    void promotePlayer(Player player, String username, Promise promise);
    void demotePlayer(Player player, String username, Promise promise);
    void depositMoney(Player player, double amount, Promise promise);
    void withdrawMoney(Player player, double amount, Promise promise);
    void setBalance(Player player, String factionName, double amount, Promise promise);
    void addTokens(Player player, String factionName, int amount, Promise promise);
    void subtractTokens(Player player, String factionName, int amount, Promise promise);
    void showFactionInfo(Player player);
    void showFactionInfo(Player player, String name);
    void showFactionMap(Player player, Promise promise);
    void startClaiming(Player player, Promise promise);
    void startClaiming(Player player, String factionName, Promise promise);
    void startSubclaiming(Player player, String subclaimName, Promise promise);
    void modifySubclaim(Player player, String subclaimName, String modifier, String username, Promise promise);
    void unclaim(Player player, Promise promise);
    void unclaim(Player player, String factionName, Promise promise);
    void unsubclaim(Player player, Promise promise);
    void unsubclaim(Player player, String subclaimName, Promise promise);
    void showSubclaimList(Player player);
    void showSubclaimList(Player player, String factionName);
    void startHomeTimer(Player player, Promise promise);
    void startStuckTimer(Player player, Promise promise);
    void printTeamLocate(Player player, Promise promise);
}
