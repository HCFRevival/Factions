package gg.hcfactions.factions.models.state;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum EServerState {
    SOTW("Start of the World"),
    NORMAL("Normal"),
    EOTW_PHASE_1("End of the World: Phase #1"),
    EOTW_PHASE_2("End of the World: Phase #2");

    @Getter public final String displayName;

    public static EServerState fromString(String name) {
        for (EServerState state : values()) {
            if (state.name().equalsIgnoreCase(name)) {
                return state;
            }
        }

        return null;
    }
}
