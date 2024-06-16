package gg.hcfactions.factions.cmd;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.anticlean.AnticleanSession;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.CommandAlias;
import gg.hcfactions.libs.acf.annotation.CommandCompletion;
import gg.hcfactions.libs.acf.annotation.Description;
import gg.hcfactions.libs.base.consumer.Promise;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Optional;

@AllArgsConstructor
public final class FocusCommand extends BaseCommand {
    @Getter public final Factions plugin;

    @CommandAlias("focus")
    @CommandCompletion("@players")
    @Description("Temporarily highlight a player for everyone in your faction")
    public void onFocus(Player player, String username) {
        final Player toFocus = Bukkit.getPlayer(username);

        if (toFocus == null) {
            player.sendMessage(ChatColor.RED + "Player not found");
            return;
        }

        final FactionPlayer focusedFactionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(toFocus);

        if (focusedFactionPlayer != null) {
            if (!focusedFactionPlayer.hasTimer(ETimerType.COMBAT)) {
                player.sendMessage(Component.text(toFocus.getName() + " is not combat tagged", NamedTextColor.RED));
                return;
            }
        }

        plugin.getFactionManager().getExecutor().focusPlayer(player, toFocus, new Promise() {
            @Override
            public void resolve() {
                player.sendMessage(FMessage.SUCCESS + "You are now focusing " + toFocus.getName());
            }

            @Override
            public void reject(String s) {
                player.sendMessage(FMessage.ERROR + "Failed to focus player: " + s);
            }
        });
    }
}
