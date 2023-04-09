package gg.hcfactions.factions.models.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum FError {
    F_NAME_TOO_SHORT("Faction name is too short"),
    F_NAME_TOO_LONG("Faction name is too long"),
    F_NAME_INVALID("Faction name is invalid"),
    F_NAME_IN_USE("Faction name is already in use"),
    F_UNABLE_TO_CREATE("Failed to create faction"),
    P_ALREADY_IN_FAC("You are already in a faction");

    @Getter public final String errorDescription;
}
