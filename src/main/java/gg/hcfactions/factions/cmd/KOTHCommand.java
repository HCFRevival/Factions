package gg.hcfactions.factions.cmd;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.events.IEvent;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.events.impl.types.PalaceEvent;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.acf.BaseCommand;
import gg.hcfactions.libs.acf.annotation.CommandAlias;
import gg.hcfactions.libs.acf.annotation.Default;
import gg.hcfactions.libs.acf.annotation.Description;
import gg.hcfactions.libs.base.util.Time;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;

@AllArgsConstructor
@CommandAlias("koth")
public final class KOTHCommand extends BaseCommand {
    @Getter public final Factions plugin;

    @Default
    @Description("See which KOTH will go active and when")
    public void onNextKoth(Player player) {
        final List<KOTHEvent> koths = Lists.newArrayList();
        plugin.getEventManager().getEventRepository().stream().filter(event -> event instanceof KOTHEvent).forEach(koth -> koths.add(((KOTHEvent) koth)));
        koths.removeIf(koth -> koth.getSchedule().isEmpty()); // TODO: Refactor: this sucks, let's just filter them in the first place

        if (koths.isEmpty() || koths.stream().noneMatch(kothEvent -> kothEvent.getRemainingTimeUntilNextSchedule() > 0L)) {
            player.sendMessage(ChatColor.RED + "No events found");
            return;
        }

        koths.sort(Comparator.comparingLong(KOTHEvent::getRemainingTimeUntilNextSchedule));

        // Courtyard will go active in 3 hours, 2 minutes and 45 seconds
        final KOTHEvent nextEvent = koths.get(0);
        player.sendMessage(nextEvent.getDisplayName() + FMessage.LAYER_1 + " will go active in " + FMessage.INFO + Time.convertToRemaining(nextEvent.getRemainingTimeUntilNextSchedule()));
    }
}
