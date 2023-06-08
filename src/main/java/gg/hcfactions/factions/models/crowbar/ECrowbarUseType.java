package gg.hcfactions.factions.models.crowbar;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ECrowbarUseType {
    SPAWNER(0),
    END_PORTAL(1);

    @Getter public final int lorePosition;
}
