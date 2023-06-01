package gg.hcfactions.factions.models.waypoint.impl;

import com.google.common.collect.Lists;
import com.lunarclient.bukkitapi.object.LCWaypoint;
import gg.hcfactions.factions.models.waypoint.IWaypoint;
import lombok.Getter;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;

public final class GlobalWaypoint implements IWaypoint {
    @Getter public UUID viewingFactionId;
    @Getter public String name;
    @Getter public Location location;
    @Getter public int color;
    @Getter public LCWaypoint legacyWaypoint;
    @Getter public List<UUID> viewers;

    public GlobalWaypoint(String name, Location location, int color) {
        this.viewingFactionId = null;
        this.name = name;
        this.location = location;
        this.color = color;
        this.legacyWaypoint = new LCWaypoint(name, location, color, true, true);
        this.viewers = Lists.newArrayList();
    }

    public GlobalWaypoint(String name, Location location, int color, boolean visible) {
        this.viewingFactionId = null;
        this.name = name;
        this.location = location;
        this.color = color;
        this.legacyWaypoint = new LCWaypoint(name, location, color, true, visible);
        this.viewers = Lists.newArrayList();
    }
}
