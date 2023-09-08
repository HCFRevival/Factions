package gg.hcfactions.factions.models.battlepass.impl;

import com.google.common.collect.Maps;
import gg.hcfactions.factions.listeners.events.player.BattlepassCompleteEvent;
import gg.hcfactions.factions.listeners.events.player.BattlepassIncrementEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public final class BPTracker {
    @Getter public UUID ownerId;
    @Getter public Map<String, Integer> progression;

    public BPTracker(UUID ownerId) {
        this.ownerId = ownerId;
        this.progression = Maps.newConcurrentMap();
    }

    public boolean isEmpty() {
        for (String objId : progression.keySet()) {
            final int currentValue = progression.get(objId);

            if (currentValue <= 0) {
                continue;
            }

            return false;
        }

        return true;
    }

    public boolean hasObjective(BPObjective objective) {
        return progression.containsKey(objective.getIdentifier());
    }

    public boolean hasObjective(String objId) {
        return progression.containsKey(objId);
    }

    public int getProgress(BPObjective objective) {
        return progression.getOrDefault(objective.getIdentifier(), 0);
    }

    public int getProgress(String objId) {
        return progression.getOrDefault(objId, 0);
    }

    public boolean hasCompleted(BPObjective objective) {
        final int progress = progression.getOrDefault(objective.getIdentifier(), 0);
        return progress >= objective.getAmountRequirement();
    }

    public void addToObjective(BPObjective objective, int amount) {
        if (hasCompleted(objective)) {
            return;
        }

        final Player bukkitPlayer = Bukkit.getPlayer(ownerId);

        if (bukkitPlayer != null) {
            final BattlepassIncrementEvent incrEvent = new BattlepassIncrementEvent(bukkitPlayer, objective, amount);
            Bukkit.getPluginManager().callEvent(incrEvent);
        }

        if (!hasObjective(objective)) {
            progression.put(objective.getIdentifier(), amount);
            return;
        }

        final int existing = progression.getOrDefault(objective.getIdentifier(), 0);
        int newValue = existing + amount;

        if (newValue >= objective.getAmountRequirement()) {
            if (bukkitPlayer != null) {
                final BattlepassCompleteEvent completeEvent = new BattlepassCompleteEvent(bukkitPlayer, objective);
                Bukkit.getPluginManager().callEvent(completeEvent);
            }

            newValue = objective.getAmountRequirement();
        }

        if (newValue != existing) {
            progression.put(objective.getIdentifier(), newValue);
        }
    }
}
