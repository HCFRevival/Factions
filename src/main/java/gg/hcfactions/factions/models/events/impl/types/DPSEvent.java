package gg.hcfactions.factions.models.events.impl.types;

import com.google.common.collect.ImmutableList;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.events.event.DPSCaptureEvent;
import gg.hcfactions.factions.events.event.EventStartEvent;
import gg.hcfactions.factions.models.events.EDPSEntityType;
import gg.hcfactions.factions.models.events.IDPSEvent;
import gg.hcfactions.factions.models.events.IEvent;
import gg.hcfactions.factions.models.events.IScheduledEvent;
import gg.hcfactions.factions.models.events.impl.DPSEventConfig;
import gg.hcfactions.factions.models.events.impl.DPSSession;
import gg.hcfactions.factions.models.events.impl.EventSchedule;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.*;

public final class DPSEvent implements IEvent, IDPSEvent, IScheduledEvent {
    public static final ImmutableList<String> DPS_ENTITY_NAMES = ImmutableList.of(
            "Johnzeh", "Symotic", "BrunoTheMiner", "Chayne", "1989TiananmenSqr",
            "AdmiralBang", "Chaseair11", "Burrito Gang", "P2W HCF Server Owner",
            "Archybot", "itsjhalt", "Meezoid", "Blyson", "atrain"
    );

    public static String getRandomEntityName() {
        final Random random = new Random();
        return DPS_ENTITY_NAMES.get(Math.abs(random.nextInt(DPS_ENTITY_NAMES.size())));
    }

    @Getter public final Factions plugin;
    @Getter @Setter public UUID owner;
    @Getter @Setter public String name;
    @Getter @Setter public String displayName;
    @Getter @Setter public DPSSession session;
    @Getter @Setter public DPSEventConfig eventConfig;
    @Getter public final List<BLocatable> spawnpoints;
    @Getter public final List<EventSchedule> schedule;

    public DPSEvent(
            Factions plugin,
            UUID owner,
            String name,
            String displayName,
            DPSEventConfig eventConfig,
            List<BLocatable> spawnpoints,
            List<EventSchedule> schedule
    ) {
        this.plugin = plugin;
        this.owner = owner;
        this.name = name;
        this.displayName = displayName;
        this.eventConfig = eventConfig;
        this.spawnpoints = spawnpoints;
        this.schedule = schedule;
        this.session = null;
    }

    @Override
    public void startEvent(EDPSEntityType entityType, long duration, int tokenReward) {
        session = new DPSSession(this, entityType, duration, tokenReward);
        session.setActive(true);
        session.getDpsEntity().spawn();

        Bukkit.getPluginManager().callEvent(new EventStartEvent(this));

        FMessage.broadcastDpsEventMessage(displayName + FMessage.LAYER_1 + " can now be contested");
    }

    @Override
    public void startEvent() {
        startEvent(EDPSEntityType.ZOMBIE, eventConfig.getDefaultDuration(), eventConfig.getTokenReward());
    }

    @Override
    public void captureEvent() {
        session.getDpsEntity().despawn();

        if (session.getLeaderboard().isEmpty()) {
            stopEvent();
            return;
        }

        final UUID leaderId = Collections.max(session.getLeaderboard().entrySet(), Map.Entry.comparingByValue()).getKey();
        final PlayerFaction faction = plugin.getFactionManager().getPlayerFactionById(leaderId);

        if (faction == null) {
            stopEvent();
            return;
        }

        session.setActive(false);
        faction.addTokens(session.getTokenReward());

        plugin.getPlayerManager().getPlayerRepository().forEach(p -> {
            if (p.getScoreboard() != null && !p.getScoreboard().isHidden()) {
                final int index = p.getScoreboard().findIndex(ChatColor.stripColor(displayName));
                p.getScoreboard().removeLine(index);
                p.getScoreboard().removeLine(index - 1);
            }
        });

        FMessage.broadcastDpsEventMessage(displayName + FMessage.LAYER_1 + " has been captured by " + FMessage.LAYER_2 + faction.getName());
        Bukkit.getPluginManager().callEvent(new DPSCaptureEvent(this, faction));
    }

    @Override
    public void stopEvent() {
        session.getDpsEntity().despawn();
        session = null;

        plugin.getPlayerManager().getPlayerRepository().forEach(p -> {
            if (p.getScoreboard() != null && !p.getScoreboard().isHidden()) {
                final int index = p.getScoreboard().findIndex(ChatColor.stripColor(displayName));
                p.getScoreboard().removeLine(index);
                p.getScoreboard().removeLine(index - 1);
            }
        });

        FMessage.broadcastDpsEventMessage(displayName + FMessage.LAYER_1 + " can no longer be contested");
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
}
