package gg.hcfactions.factions.models.waypoint.impl;

import com.google.common.collect.Lists;
import com.lunarclient.bukkitapi.object.LCWaypoint;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.waypoint.IWaypoint;
import lombok.Getter;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;

public final class FactionWaypoint implements IWaypoint {
    @Getter public UUID viewingFactionId;
    @Getter public String name;
    @Getter public Location location;
    @Getter public int color;
    @Getter public LCWaypoint legacyWaypoint;
    @Getter public List<UUID> viewers;

    public FactionWaypoint(PlayerFaction faction, String name, Location location, int color) {
        this.viewingFactionId = faction.getUniqueId();
        this.name = name;
        this.location = location;
        this.color = color;
        this.legacyWaypoint = new LCWaypoint(name, location, color, true, true);
        this.viewers = Lists.newArrayList();
    }
}
