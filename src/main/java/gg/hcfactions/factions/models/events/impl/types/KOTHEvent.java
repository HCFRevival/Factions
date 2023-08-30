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
import org.bukkit.ChatColor;

import java.util.List;
import java.util.UUID;

public class KOTHEvent implements IEvent, ICaptureEvent, IScheduledEvent {
    @Getter public final Factions plugin;
    @Getter @Setter public UUID owner;
    @Getter @Setter public String name;
    @Getter @Setter public String displayName;
    @Getter public final List<EventSchedule> schedule;
    @Getter @Setter public CaptureRegion captureRegion;
    @Getter @Setter public CaptureEventConfig eventConfig;
    @Getter @Setter KOTHSession session;

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
        faction.addTokens(session.getTokenReward());

        plugin.getPlayerManager().getPlayerRepository().forEach(p -> {
            if (p.getScoreboard() != null && !p.getScoreboard().isHidden()) {
                final int index = p.getScoreboard().findIndex(ChatColor.stripColor(displayName));
                p.getScoreboard().removeLine(index);
                p.getScoreboard().removeLine(index - 1);
            }
        });

        FMessage.broadcastCaptureEventMessage(displayName + FMessage.LAYER_1 + " has been captured by " + FMessage.LAYER_2 + faction.getName());

        Bukkit.getPluginManager().callEvent(new KOTHCaptureEvent(this, faction));
    }

    @Override
    public void startEvent() {
        startEvent(eventConfig.getDefaultTicketsNeededToWin(), eventConfig.getDefaultTimerDuration(), eventConfig.getTokenReward(), eventConfig.getTickCheckpointInterval());
    }

    @Override
    public void startEvent(int ticketsNeededToWin, int timerDuration, int tokenReward, int tickCheckpointInterval) {
        session = new KOTHSession(this, ticketsNeededToWin, timerDuration, tokenReward, tickCheckpointInterval);
        session.setActive(true);

        Bukkit.getPluginManager().callEvent(new EventStartEvent(this));

        FMessage.broadcastCaptureEventMessage(displayName + FMessage.LAYER_1 + " can now be contested");
    }

    @Override
    public void stopEvent() {
        session = null;

        plugin.getPlayerManager().getPlayerRepository().forEach(p -> {
            if (p.getScoreboard() != null && !p.getScoreboard().isHidden()) {
                final int index = p.getScoreboard().findIndex(ChatColor.stripColor(displayName));
                p.getScoreboard().removeLine(index);
                p.getScoreboard().removeLine(index - 1);
            }
        });

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
