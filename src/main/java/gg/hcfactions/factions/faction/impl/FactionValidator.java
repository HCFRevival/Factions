package gg.hcfactions.factions.faction.impl;

import gg.hcfactions.factions.faction.FactionManager;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FError;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.Locale;

public record FactionValidator(@Getter FactionManager manager) {
    @Nullable
    public FError isValidName(String name, boolean skipRegex) {
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

        if (!skipRegex && !name.matches("^[A-Za-z0-9_.]+$")) {
            return FError.F_NAME_INVALID;
        }

        return null;
    }

    @Nullable
    public FError isUnraidable(PlayerFaction faction, boolean considerFreeze) {
        if (faction.getDtr() <= 0.0) {
            return FError.F_NOT_ALLOWED_RAIDABLE;
        }

        if (considerFreeze && faction.isFrozen()) {
            return FError.F_NOT_ALLOWED_WHILE_FROZEN;
        }

        return null;
    }
}
