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
    F_UNABLE_TO_DISBAND("Failed to disband faction"),
    F_NOT_FOUND("Faction not found"),
    F_NOT_ALLOWED_RAIDABLE("You are not able to perform this action while your faction is raid-able"),
    F_NOT_ALLOWED_WHILE_FROZEN("You are not able to perform this action while your faction power is frozen"),
    P_ALREADY_IN_FAC("You are already in a faction"),
    P_NOT_IN_FAC("You are not in a faction"),
    P_COULD_NOT_LOAD_F("Could not load your faction information"),
    P_NOT_ENOUGH_PERMS("You do not have permission to perform this action");

    @Getter public final String errorDescription;
}
