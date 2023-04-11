package gg.hcfactions.factions.listeners;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.bukkit.events.impl.ProcessedChatEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@AllArgsConstructor
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

        event.setCancelled(true);

        if (faction != null) {
            final PlayerFaction.Member member = faction.getMember(player.getUniqueId());

            if (member != null) {
                if (member.getChannel().equals(PlayerFaction.ChatChannel.PUBLIC) && !message.startsWith("@")) {
                    event.getRecipients().forEach(p -> p.sendMessage(FMessage.getPublicFormat(faction, displayName, message, p)));
                    return;
                }

                if (message.startsWith("!") && !member.getChannel().equals(PlayerFaction.ChatChannel.PUBLIC)) {
                    event.getRecipients().forEach(p -> p.sendMessage(FMessage.getPublicFormat(faction, displayName, message.replaceFirst("!", ""), p)));
                    return;
                }

                if (member.getChannel().equals(PlayerFaction.ChatChannel.FACTION) && !message.startsWith("!")) {
                    event.getRecipients().stream().filter(p -> faction.isMember(p.getUniqueId())).forEach(m -> m.sendMessage(FMessage.getFactionFormat(displayName, message)));
                    return;
                }

                if (message.startsWith("@") && !member.getChannel().equals(PlayerFaction.ChatChannel.FACTION)) {
                    event.getRecipients().stream().filter(p -> faction.isMember(p.getUniqueId())).forEach(m -> m.sendMessage(FMessage.getFactionFormat(displayName, message.replaceFirst("@", ""))));
                    return;
                }
            }
        }

        event.getRecipients().forEach(p -> p.sendMessage(FMessage.getPublicFormat(faction, displayName, message, p)));
    }


}
