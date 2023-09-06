package gg.hcfactions.factions.models.battlepass.impl;

import com.google.common.collect.Maps;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

public final class BPTracker {
    @Getter public UUID ownerId;
    @Getter public Map<String, Integer> progression;

    public BPTracker(UUID ownerId) {
        this.ownerId = ownerId;
        this.progression = Maps.newConcurrentMap();
    }

    public boolean hasObjective(BPObjective objective) {
        return progression.containsKey(objective.getIdentifier());
    }

    public boolean hasObjective(String objId) {
        return progression.containsKey(objId);
    }

    public boolean hasCompleted(BPObjective objective) {
        final int progress = progression.getOrDefault(objective.getIdentifier(), 0);
        return progress >= objective.getAmountRequirement();
    }

    public void addToObjective(BPObjective objective, int amount) {
        if (!hasObjective(objective)) {
            progression.put(objective.getIdentifier(), amount);
            return;
        }

        final int existing = progression.getOrDefault(objective.getIdentifier(), 0);
        final int newValue = existing + amount;

        progression.put(objective.getIdentifier(), newValue);
    }
}
