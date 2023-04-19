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
        if (name.equalsIgnoreCase("sotw")) {
            return SOTW;
        }

        else if (name.equalsIgnoreCase("normal")) {
            return NORMAL;
        }

        else if (name.equalsIgnoreCase("eotw1")) {
            return EOTW_PHASE_1;
        }

        else if (name.equalsIgnoreCase("eotw2")) {
            return EOTW_PHASE_2;
        }

        return null;
    }
}
