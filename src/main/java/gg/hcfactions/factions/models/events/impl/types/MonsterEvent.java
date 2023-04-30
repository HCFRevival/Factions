package gg.hcfactions.factions.models.events.impl.types;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.events.IEvent;
import gg.hcfactions.factions.models.events.IMonsterEvent;
import gg.hcfactions.factions.models.events.IScheduledEvent;
import gg.hcfactions.factions.models.events.impl.EventSchedule;
import gg.hcfactions.factions.models.events.impl.MonsterEventSession;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public final class MonsterEvent implements IEvent, IMonsterEvent, IScheduledEvent {
    @Getter public final Factions plugin;
    @Getter @Setter public UUID owner;
    @Getter @Setter public String name;
    @Getter @Setter public String displayName;
    @Getter public final List<EventSchedule> schedule;
    @Getter @Setter public BLocatable captureChestLocation;
    @Getter @Setter public MonsterEventSession session;

    public MonsterEvent(
            Factions plugin,
            UUID owner,
            String name,
            String displayName,
            List<EventSchedule> schedule,
            BLocatable captureChestLocation
    ) {
        this.plugin = plugin;
        this.owner = owner;
        this.name = name;
        this.displayName = displayName;
        this.schedule = schedule;
        this.captureChestLocation = captureChestLocation;
        this.session = null;
    }

    @Override
    public boolean isActive() {
        return session != null && session.isActive();
    }

    @Override
    public void setActive(boolean b) {
        if (session != null) {
            session.setActive(true);
        }
    }

    @Override
    public void captureEvent(Player player) {
        session.setActive(false);
        session.setCapturingPlayer(player);
        Bukkit.broadcastMessage(FMessage.PVE_PREFIX + displayName + FMessage.LAYER_1 + " has been captured by " + FMessage.LAYER_2 + player.getName());
    }

    @Override
    public void startEvent() {
        startEvent(3600, 3);
    }

    @Override
    public void startEvent(int uptime, int tokenReward) {
        final String timeDisplay = Time.convertToRemaining(uptime/1000L);
        session = new MonsterEventSession(tokenReward);
        session.setActive(true);
        Bukkit.broadcastMessage(FMessage.PVE_PREFIX + displayName + FMessage.LAYER_1 + " is now open for " + timeDisplay);
    }

    @Override
    public void stopEvent() {
        this.session = null;
        Bukkit.broadcastMessage(FMessage.PVE_PREFIX + displayName + FMessage.LAYER_1 + " is now closed");
    }
}
