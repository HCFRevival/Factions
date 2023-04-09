package gg.hcfactions.factions.faction.impl;

import gg.hcfactions.factions.faction.FactionManager;
import gg.hcfactions.factions.models.message.FError;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.Locale;

@AllArgsConstructor
public final class FactionValidator {
    @Getter public final FactionManager manager;

    @Nullable
    public FError isValidName(String name) {
        if (name.length() < manager.getPlugin().getConfiguration().getMinFactionNameLength()) {
            return FError.F_NAME_TOO_SHORT;
        }

        if (name.length() > manager.getPlugin().getConfiguration().getMaxFactionNameLength()) {
            return FError.F_NAME_TOO_LONG;
        }

        if (manager.getPlugin().getConfiguration().getDisallowedFactionNames().contains(name.toLowerCase(Locale.ROOT))) {
            return FError.F_NAME_INVALID;
        }

        if (manager.getFactionByName(name) != null) {
            return FError.F_NAME_IN_USE;
        }

        return null;
    }
}
