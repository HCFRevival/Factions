package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.stats.EStatisticType;
import gg.hcfactions.factions.models.stats.impl.PlayerStatHolder;
import gg.hcfactions.libs.bukkit.events.impl.ProcessedChatEvent;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public record ChatListener(@Getter Factions plugin) implements Listener {
    @EventHandler(priority = EventPriority.MONITOR) /* Adds factions chat formatting and implements chat macros */
    public void onProcessedChat(ProcessedChatEvent event) {
        final Player player = event.getPlayer();
        final String displayName = event.getDisplayName();
        final String message = event.getMessage();

        if (event.isCancelled()) {
            return;
        }

        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player);
        final PlayerStatHolder stats = plugin.getStatsManager().getPlayerStatistics(player.getUniqueId());
        final long kills = (stats != null) ? stats.getStatistic(EStatisticType.KILL) : 0L;

        event.setCancelled(true);

        if (faction != null) {
            final PlayerFaction.Member member = faction.getMember(player.getUniqueId());

            if (member != null) {
                // Print to public chat when in public channel and does not prefix with @ or #
                if (member.getChannel().equals(PlayerFaction.ChatChannel.PUBLIC) && !message.startsWith("@") && !message.startsWith("#")) {
                    event.getRecipients().forEach(p -> p.sendMessage(FMessage.getPublicFormat(faction, displayName, kills, message, p)));
                    return;
                }

                // Print to public chat when using the ! macro
                if (message.startsWith("!") && !member.getChannel().equals(PlayerFaction.ChatChannel.PUBLIC)) {
                    event.getRecipients().forEach(p -> p.sendMessage(FMessage.getPublicFormat(faction, displayName, kills, message.replaceFirst("!", ""), p)));
                    return;
                }

                // Print to faction chat if in faction channel
                if (member.getChannel().equals(PlayerFaction.ChatChannel.FACTION) && !message.startsWith("!")) {
                    event.getRecipients().stream().filter(p -> faction.isMember(p.getUniqueId())).forEach(m -> m.sendMessage(FMessage.getFactionFormat(displayName, message)));
                    return;
                }

                // Print to ally chat if in ally channel
                if (member.getChannel().equals(PlayerFaction.ChatChannel.ALLY) && !message.startsWith("!")) {
                    event.getRecipients().stream().filter(p -> faction.isMember(p.getUniqueId())).forEach(m -> m.sendMessage(FMessage.getAllyFormat(displayName, message)));
                    event.getRecipients().stream().filter(faction::isAlly).forEach(ally -> ally.sendMessage(FMessage.getAllyFormat(displayName, message)));
                    return;
                }

                // @Test - Prints in faction chat, Test - Prints in public
                if (message.startsWith("@") && !member.getChannel().equals(PlayerFaction.ChatChannel.FACTION)) {
                    event.getRecipients().stream().filter(p -> faction.isMember(p.getUniqueId())).forEach(m -> m.sendMessage(FMessage.getFactionFormat(displayName, message.replaceFirst("@", ""))));
                    return;
                }

                // #Test - Prints in ally chat, Test - Prints in public
                if (message.startsWith("#") && !member.getChannel().equals(PlayerFaction.ChatChannel.ALLY)) {
                    final String pruned = message.replaceFirst("#", "");
                    event.getRecipients().stream().filter(p -> faction.isMember(p.getUniqueId())).forEach(m -> m.sendMessage(FMessage.getAllyFormat(displayName, pruned)));
                    event.getRecipients().stream().filter(faction::isAlly).forEach(ally -> ally.sendMessage(FMessage.getAllyFormat(displayName, pruned)));
                    return;
                }
            }
        }

        event.getRecipients().forEach(p -> p.sendMessage(FMessage.getPublicFormat(faction, displayName, kills, message, p)));
    }
}
