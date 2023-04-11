package gg.hcfactions.factions.models.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum FError {
    A_SERVER_STATE_SAME("Server state is already set to this value"),
    G_GENERIC_ERROR("An error has occurred"),
    G_PAGE_NOT_FOUND("Page not found"),
    P_CAN_NOT_CHANGE_WORLDS_CTAG("You can not change worlds while you are combat-tagged"),
    P_CAN_NOT_CHANGE_WORLDS_PVP_PROT("You can not change worlds while you have PvP protection"),
    P_CAN_NOT_ATTACK_PVP_PROT("You can not attack others while you have PvP protection"),
    P_CAN_NOT_ATTACK_PVP_PROT_OTHER("This player has PvP protection"),
    P_NO_INV_TO_F("You do not have an invitation to join this faction"),
    P_CAN_NOT_JOIN_RAIDABLE("You can not join this faction because they are raid-able"),
    P_CAN_NOT_JOIN_FROZEN("You can not join this faction because their power is frozen"),
    P_CAN_NOT_JOIN_FULL("You can not join this faction because it is full"),
    P_CAN_NOT_JOIN_NO_REINV("You can not join this faction because you left recently and they are out of re-invites"),
    F_NAME_TOO_SHORT("Faction name is too short"),
    F_NAME_TOO_LONG("Faction name is too long"),
    F_NAME_INVALID("Faction name is invalid"),
    F_NAME_IN_USE("Faction name is already in use"),
    F_UNABLE_TO_CREATE("Failed to create faction"),
    F_UNABLE_TO_DISBAND("Failed to disband faction"),
    F_NOT_FOUND("Faction not found"),
    F_NOT_FROZEN("Faction power is not frozen"),
    F_NOT_ALLOWED_RAIDABLE("You are not able to perform this action while your faction is raid-able"),
    F_NOT_ALLOWED_WHILE_FROZEN("You are not able to perform this action while your faction power is frozen"),
    F_NOT_ALLOWED_COOLDOWN("Please wait a moment before performing this action again"),
    F_REASSIGN_LEADER("You must reassign leadership to an Officer of the faction before leaving"),
    F_EXIT_CLAIM_BEFORE_LEAVE("You must exit your faction's claims before leaving the faction"),
    F_HOME_UNSET("Your faction home is not set"),
    F_CANT_WARP_IN_CLAIM("You can not warp out of this claim"),
    F_HIGHER_RANK("This player has equal or a higher rank than you"),
    F_RANK_NOT_FOUND("Rank not found"),
    F_NOT_STANDING_IN_CLAIM("You are not standing in your faction's land"),
    P_ALREADY_IN_FAC("You are already in a faction"),
    P_NOT_IN_FAC("You are not in a faction"),
    P_NOT_FOUND("Player not found"),
    P_ALREADY_IN_F("Player is already in a faction"),
    P_ALREADY_IN_OWN_F("Player is already in your faction"),
    P_NOT_IN_OWN_F("Player is not in your faction"),
    P_ALREADY_HAS_INV_F("Player has a pending invitation"),
    P_COULD_NOT_LOAD_F("Could not load your faction information"),
    P_COULD_NOT_LOAD_P("Could not load your player information"),
    P_NOT_INSIDE_CLAIM("You are not standing inside claimed land"),
    P_NOT_ENOUGH_PERMS("You do not have permission to perform this action"),
    P_TIMER_ALREADY_STARTED("This timer is already running"),
    P_CAN_NOT_AFFORD("You can not afford to perform this action");

    @Getter public final String errorDescription;
}
