package gg.hcfactions.factions.models.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum EPalaceLootTier {
    T1("tier1"), T2("tier2"), T3("tier3");

    @Getter public final String name;
}
