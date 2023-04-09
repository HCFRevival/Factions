package gg.hcfactions.factions.models.message;

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
}
