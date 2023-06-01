package gg.hcfactions.factions.models.events.impl;

import gg.hcfactions.libs.bukkit.location.IRegion;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public final class CaptureRegion implements IRegion {
    @Getter @Setter public BLocatable cornerA;
    @Getter @Setter public BLocatable cornerB;

    public BLocatable getCenter() {
        final double minX = Math.min(cornerA.getX(), cornerB.getX());
        final double maxX = Math.max(cornerA.getX(), cornerB.getX());
        final double minZ = Math.min(cornerA.getZ(), cornerB.getZ());
        final double maxZ = Math.max(cornerA.getZ(), cornerB.getZ());
        final double minY = Math.min(cornerA.getY(), cornerB.getY());

        final double centerX = maxX - ((maxX - minX) / 2);
        final double centerZ = maxZ - ((maxZ - minZ) / 2);

        return new BLocatable(cornerA.getWorldName(), centerX, minY, centerZ);
    }
}
