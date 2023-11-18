package gg.hcfactions.factions.models.events.impl;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.models.events.EDPSEntityType;
import gg.hcfactions.factions.models.events.IDPSEntity;
import gg.hcfactions.factions.models.events.impl.entity.DPSPhantom;
import gg.hcfactions.factions.models.events.impl.entity.DPSRavager;
import gg.hcfactions.factions.models.events.impl.entity.DPSZombie;
import gg.hcfactions.factions.models.events.impl.types.DPSEvent;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.libs.base.util.Time;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.*;

public class DPSSession {
    @Getter public final DPSEvent event;
    @Getter @Setter public IDPSEntity dpsEntity;
    @Getter @Setter public boolean active;
    @Getter @Setter public int tokenReward;
    @Getter @Setter public long eventEndTimestamp;
    @Getter @Setter public PlayerFaction mostRecentDamager;
    @Getter public final long eventDuration;
    @Getter public final Map<UUID, Long> leaderboard;

    public DPSSession(DPSEvent event, EDPSEntityType entityType, long duration, int tokenReward) {
        this.event = event;
        this.tokenReward = tokenReward;
        this.eventDuration = duration;
        this.eventEndTimestamp = Time.now() + duration;
        this.leaderboard = Maps.newConcurrentMap();

        if (entityType.equals(EDPSEntityType.ZOMBIE)) {
            dpsEntity = new DPSZombie(event, new Location(Bukkit.getWorld("world"), 0.0, 0.0, 0.0));
        } else if (entityType.equals(EDPSEntityType.PHANTOM)) {
            dpsEntity = new DPSPhantom(event, new Location(Bukkit.getWorld("world"), 0, 128, 0));
        } else if (entityType.equals(EDPSEntityType.RAVAGER)) {
            dpsEntity = new DPSRavager(event, new Location(Bukkit.getWorld("world"), 0, 0.0, 0));
        }
    }

    public long getRemainingTime() {
        return eventEndTimestamp - Time.now();
    }

    public long getDamage(PlayerFaction faction) {
        return leaderboard.getOrDefault(faction.getUniqueId(), 0L);
    }

    public ImmutableMap<UUID, Long> getSortedLeaderboard() {
        final List<Map.Entry<UUID, Long>> collected = new LinkedList<>(leaderboard.entrySet());
        collected.sort(Map.Entry.comparingByValue());
        Collections.reverse(collected);

        final Map<UUID, Long> sorted = Maps.newLinkedHashMap();
        collected.forEach(entry -> sorted.put(entry.getKey(), entry.getValue()));

        return ImmutableMap.copyOf(sorted);
    }

    public void addDamage(PlayerFaction faction, int amount) {
        final long current = getDamage(faction);
        final long newValue = current + amount;

        leaderboard.put(faction.getUniqueId(), newValue);

        if (mostRecentDamager == null || !mostRecentDamager.getUniqueId().equals(faction.getUniqueId())) {
            setMostRecentDamager(faction);
        }
    }
}
