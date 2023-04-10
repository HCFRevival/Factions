package gg.hcfactions.factions.models.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum EStatisticType {
    KILL("Kills"),
    DEATH("Deaths"),
    EVENT_CAPTURES("Event Captures"),
    LONGSHOT("Longest Bow Shot"),
    PLAYTIME("Playtime"),
    EXP_EARNED("Experience Earned");

    @Getter public final String displayName;
}
