package gg.hcfactions.factions.models.claim.impl;

import gg.hcfactions.factions.models.claim.IShield;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public final class ProtectionShield implements IShield {
    @Getter public final Player viewer;
    @Getter public final Material material;
    @Getter public final BLocatable location;
    @Getter @Setter
    public boolean drawn;

    public ProtectionShield(Player viewer, BLocatable location) {
        this.viewer = viewer;
        this.material = Material.BLUE_STAINED_GLASS;
        this.location = location;
        this.drawn = false;
    }
}
