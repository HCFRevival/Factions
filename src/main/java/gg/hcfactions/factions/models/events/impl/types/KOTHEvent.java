package gg.hcfactions.factions.models.events.impl.types;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.events.event.EventStartEvent;
import gg.hcfactions.factions.events.event.KOTHCaptureEvent;
import gg.hcfactions.factions.models.events.*;
import gg.hcfactions.factions.models.events.impl.CaptureEventConfig;
import gg.hcfactions.factions.models.events.impl.CaptureRegion;
import gg.hcfactions.factions.models.events.impl.EventSchedule;
import gg.hcfactions.factions.models.events.impl.KOTHSession;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.UUID;

@Getter
public class KOTHEvent implements IEvent, ICaptureEvent, IScheduledEvent {
    public final Factions plugin;
    public CaptureEventConfig eventConfig;
    public final List<EventSchedule> schedule;
    @Setter public UUID owner;
    @Setter public String name;
    @Setter public String displayName;
    @Setter public CaptureRegion captureRegion;
    @Setter KOTHSession session;

    public KOTHEvent(
            Factions plugin,
            UUID owner,
            String name,
            String displayName,
            List<EventSchedule> schedule,
            CaptureRegion captureRegion,
            CaptureEventConfig eventConfig
    ) {
        this.plugin = plugin;
        this.owner = owner;
        this.name = name;
        this.displayName = displayName;
        this.schedule = schedule;
        this.captureRegion = captureRegion;
        this.eventConfig = eventConfig;
        this.session = null;
    }

    @Override
    public void captureEvent(PlayerFaction faction) {
        session.setActive(false);
        session.setCapturingFaction(faction);
        session.getTracker().publishTracking();

        faction.addTokens(session.getTokenReward());

        FMessage.broadcastCaptureEventMessage(displayName + FMessage.LAYER_1 + " has been captured by " + FMessage.LAYER_2 + faction.getName());
        Bukkit.getPluginManager().callEvent(new KOTHCaptureEvent(this, faction));
    }

    @Override
    public void startEvent() {
        startEvent(
                eventConfig.getDefaultTicketsNeededToWin(),
                eventConfig.getDefaultTimerDuration(),
                eventConfig.getTokenReward(),
                eventConfig.getTickCheckpointInterval(),
                eventConfig.getContestedThreshold(),
                eventConfig.getOnlinePlayerLimit()
        );
    }

    @Override
    public void startEvent(CaptureEventConfig conf) {
        session = new KOTHSession(this, conf);
        session.setActive(true);
        session.getTracker().startTracking();
        Bukkit.getPluginManager().callEvent(new EventStartEvent(this));
        FMessage.broadcastCaptureEventMessage(displayName + FMessage.LAYER_1 + " can now be contested");
    }

    @Override
    public void startEvent(
            int ticketsNeededToWin,
            int timerDuration,
            int tokenReward,
            int tickCheckpointInterval,
            int contestedThreshold,
            int onlinePlayerLimit
    ) {
        session = new KOTHSession(
                this,
                ticketsNeededToWin,
                timerDuration,
                tokenReward,
                tickCheckpointInterval,
                contestedThreshold,
                onlinePlayerLimit
        );

        session.setActive(true);
        session.getTracker().startTracking();
        Bukkit.getPluginManager().callEvent(new EventStartEvent(this));
        FMessage.broadcastCaptureEventMessage(displayName + FMessage.LAYER_1 + " can now be contested");
    }

    @Override
    public void stopEvent() {
        session.getTracker().stopTracking();
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

    public void setEventConfig(CaptureEventConfig newConfig) {
        this.eventConfig = newConfig;

        if (session != null) {
            session.updateConfig(newConfig);
        }
    }
}
