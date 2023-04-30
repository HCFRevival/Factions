package gg.hcfactions.factions.models.events.impl.types;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.events.*;
import gg.hcfactions.factions.models.events.impl.CaptureEventConfig;
import gg.hcfactions.factions.models.events.impl.CaptureRegion;
import gg.hcfactions.factions.models.events.impl.EventSchedule;
import gg.hcfactions.factions.models.events.impl.KOTHSession;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

public class KOTHEvent implements IEvent, ICaptureEvent, IScheduledEvent {
    @Getter public final Factions plugin;
    @Getter @Setter public UUID owner;
    @Getter @Setter public String name;
    @Getter @Setter public String displayName;
    @Getter public final List<EventSchedule> schedule;
    @Getter @Setter public BLocatable captureChestLocation;
    @Getter @Setter public CaptureRegion captureRegion;
    @Getter @Setter public CaptureEventConfig eventConfig;
    @Getter @Setter KOTHSession session;

    public KOTHEvent(
            Factions plugin,
            UUID owner,
            String name,
            String displayName,
            List<EventSchedule> schedule,
            BLocatable captureChestLocation,
            CaptureRegion captureRegion,
            CaptureEventConfig eventConfig
    ) {
        this.plugin = plugin;
        this.owner = owner;
        this.name = name;
        this.displayName = displayName;
        this.schedule = schedule;
        this.captureChestLocation = captureChestLocation;
        this.captureRegion = captureRegion;
        this.eventConfig = eventConfig;
        this.session = null;
    }

    @Override
    public void captureEvent(PlayerFaction faction) {
        session.setActive(false);
        session.setCaptureChestUnlockTime(Time.now() + (30 * 1000L));
        session.setCapturingFaction(faction);
        FMessage.broadcastCaptureEventMessage(displayName + FMessage.LAYER_1 + " has been captured by " + FMessage.LAYER_2 + faction.getName());
    }

    @Override
    public void startEvent() {
        startEvent(eventConfig.defaultTicketsNeededToWin(), eventConfig.defaultTimerDuration());
    }

    @Override
    public void startEvent(int ticketsNeededToWin, int timerDuration) {
        session = new KOTHSession(this, ticketsNeededToWin, timerDuration);
        session.setActive(true);
        FMessage.broadcastCaptureEventMessage(displayName + FMessage.LAYER_1 + " can now be contested");
    }

    @Override
    public void stopEvent() {
        session = null;
        FMessage.broadcastCaptureEventMessage(displayName + FMessage.LAYER_1 + " can no longer be contested");
    }

    @Override
    public boolean isActive() {
        return session != null && session.isActive();
    }

    @Override
    public void setActive(boolean b) {
        if (session != null) {
            session.setActive(false);
        }
    }
}