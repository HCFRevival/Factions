package gg.hcfactions.factions.models.state;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum EServerState {
    SOTW("Start of the World", "sotw"),
    NORMAL("Normal", "normal"),
    EOTW_PHASE_1("End of the World: Phase #1", "eotw1"),
    EOTW_PHASE_2("End of the World: Phase #2", "eotw2"),
    KITMAP("Kit Map", "kitmap");

    @Getter public final String displayName;
    @Getter public final String simpleName;

    public static EServerState fromString(String name) {
        for (EServerState v : values()) {
            if (v.getSimpleName().equalsIgnoreCase(name)) {
                return v;
            }
        }

        return null;
    }
}
