package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public final class WalletCommand extends BaseCommand {
    @Getter public final Factions plugin;

    @CommandAlias("balance|bal")
    @Description("View your personal balance")
    public void onBalance(Player player) {
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);

        if (factionPlayer == null) {
            player.sendMessage(FMessage.ERROR + FError.P_COULD_NOT_LOAD_P.getErrorDescription());
            return;
        }

        player.sendMessage(FMessage.LAYER_2 + "Balance" + FMessage.LAYER_1 + ": $" + String.format("%.2f", factionPlayer.getBalance()));
    }

    @CommandAlias("balance|bal")
    @Description("View a players personal balance")
    @Syntax("[player]")
    @CommandCompletion("@players")
    public void onBalanceOther(Player player, String username) {
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(username);

        if (factionPlayer == null) {
            player.sendMessage(FMessage.ERROR + FError.P_NOT_FOUND.getErrorDescription());
            return;
        }

        player.sendMessage(FMessage.LAYER_2 + factionPlayer.getUsername() + "'s Balance" + FMessage.LAYER_1 + ": $" + String.format("%.2f", factionPlayer.getBalance()));
    }

    @CommandAlias("pay")
    @Description("Pay a player money from your balance")
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onPay(Player player, String otherPlayerName, String amountNamed) {
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);
        final FactionPlayer otherFactionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(otherPlayerName);

        if (factionPlayer == null) {
            player.sendMessage(FMessage.ERROR + FError.P_COULD_NOT_LOAD_P.getErrorDescription());
            return;
        }

        if (otherFactionPlayer == null) {
            player.sendMessage(FMessage.ERROR + FError.P_NOT_FOUND.getErrorDescription());
            return;
        }

        if (factionPlayer.getUniqueId().equals(otherFactionPlayer.getUniqueId())) {
            player.sendMessage(FMessage.ERROR + "You can not pay yourself");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountNamed);
        } catch (NumberFormatException e) {
            player.sendMessage(FMessage.ERROR + "Invalid amount");
            return;
        }

        if (!factionPlayer.canAfford(amount)) {
            player.sendMessage(FMessage.ERROR + FError.P_CAN_NOT_AFFORD.getErrorDescription());
            return;
        }

        factionPlayer.subtractFromBalance(amount);
        otherFactionPlayer.addToBalance(amount);

        player.sendMessage(ChatColor.DARK_GREEN + "$" + String.format("%.2f", amount) + FMessage.LAYER_1 + " has been transferred to " + FMessage.P_NAME + otherFactionPlayer.getUsername());
        otherFactionPlayer.sendMessage(FMessage.LAYER_1 + "You have received " + ChatColor.DARK_GREEN + "$" + String.format("%.2f", amount) + FMessage.LAYER_1 + " from " + FMessage.P_NAME + player.getName());
    }
}
