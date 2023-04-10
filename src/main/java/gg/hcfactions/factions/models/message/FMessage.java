package gg.hcfactions.factions.models.message;

import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.libs.base.util.Time;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class FMessage {
    public static final ChatColor LAYER_1 = ChatColor.YELLOW;
    public static final ChatColor LAYER_2 = ChatColor.GOLD;
    public static final ChatColor SUCCESS = ChatColor.GREEN;
    public static final ChatColor ERROR = ChatColor.RED;
    public static final ChatColor INFO = ChatColor.BLUE;
    public static final ChatColor P_NAME = ChatColor.RESET;

    public static final String T_EPEARL_UNLOCKED = SUCCESS + "Your enderpearls have been unlocked";
    public static final String T_CTAG_EXPIRE = SUCCESS + "Your combat-tag has expired";
    public static final String T_CRAPPLE_UNLOCKED = SUCCESS + "Your crapples have been unlocked";
    public static final String T_GAPPLE_UNLOCKED = SUCCESS + "Your gapples have been unlocked";
    public static final String T_HOME_EXPIRE = SUCCESS + "You have been returned to your faction home";
    public static final String T_STUCK_EXPIRE = SUCCESS + "You have been teleported to safety";
    public static final String T_LOGOUT_EXPIRE = SUCCESS + "You have been disconnected safely";
    public static final String T_PROTECTION_EXPIRE = SUCCESS + "Your combat protection has expired";
    public static final String T_FREEZE_EXPIRE = SUCCESS + "Your faction will now begin regenerating power";

    public static void broadcastFactionCreated(String factionName, String playerName) {
        Bukkit.broadcastMessage(LAYER_1 + "Faction " + INFO + factionName + LAYER_1 + " has been " + SUCCESS + "created" + LAYER_1 + " by " + P_NAME + playerName);
    }

    public static void broadcastFactionDisbanded(String factionName, String playerName) {
        Bukkit.broadcastMessage(LAYER_1 + "Faction " + INFO + factionName + LAYER_1 + " has been " + ERROR + "disbanded" + LAYER_1 + " by " + P_NAME + playerName);
    }

    public static void printDepositReceived(Player player, double amount) {
        player.sendMessage(ChatColor.DARK_GREEN + "$" + String.format("%.2f", amount) + LAYER_1 + " has been " + SUCCESS + "deposited" + LAYER_1 + " to your personal balance");
    }

    public static void printWithdrawReceived(Player player, double amount) {
        player.sendMessage(ChatColor.DARK_GREEN + "$" + String.format("%.2f", amount) + LAYER_1 + " has been " + ERROR + "withdrawn" + LAYER_1 + " from your personal balance");
    }

    public static void printFactionMemberOnline(PlayerFaction faction, String username) {
        faction.sendMessage(ChatColor.YELLOW + "Member " + SUCCESS + "Online" + LAYER_1 + ": " + P_NAME + username);
    }

    public static void printFactionMemberOffline(PlayerFaction faction, String username) {
        faction.sendMessage(ChatColor.YELLOW + "Member " + ERROR + "Offline" + LAYER_1 + ": " + P_NAME + username);
    }

    public static void printCombatTag(Player player, long duration) {
        player.sendMessage(ERROR + "Combat Tag: " + INFO + Time.convertToHHMMSS(duration));
    }

    public static void printTimerCancelled(Player player, String timerName, String reason) {
        player.sendMessage(ERROR + "Your " + timerName + " timer was cancelled because you " + reason);
    }

    public static void printLockedTimer(Player player, String itemName, long duration) {
        final String asDecimal = Time.convertToDecimal(duration);
        player.sendMessage(ERROR + "Your " + itemName + " are locked for " + ERROR + "" + ChatColor.BOLD + asDecimal + ERROR + "s");
    }
}
