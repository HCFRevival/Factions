package gg.hcfactions.factions;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.List;

public final class FConfig {
    // world
    @Getter public Location overworldSpawn;
    @Getter public Location netherSpawn;
    @Getter public Location endSpawn;

    // faction naming
    @Getter public int minFactionNameLength;
    @Getter public int maxFactionNameLength;
    @Getter public List<String> disallowedFactionNames;

    // claiming
    @Getter public int defaultServerFactionBuildBuffer;
    @Getter public int defaultServerFactionClaimBuffer;
    @Getter public int defaultPlayerFactionClaimBuffer;

    // timers
    @Getter public int attackerCombatTagDuration;
    @Getter public int attackedCombatTagDuration;
    @Getter public int enderpearlDuration;
    @Getter public int crappleDuration;
    @Getter public int gappleDuration;
    @Getter public int totemDuration;

    // TODO: load values from config
    public void loadConfig() {
        overworldSpawn = new Location(Bukkit.getWorld("world"), 0.0, 64.0, 0.0);
        netherSpawn = new Location(Bukkit.getWorld("world_nether"), 0.0, 64.0, 0.0);
        endSpawn = new Location(Bukkit.getWorld("world_the_end"), 0.0, 64.0, 0.0);

        minFactionNameLength = 3;
        maxFactionNameLength = 16;
        disallowedFactionNames = Arrays.asList("test");

        defaultServerFactionBuildBuffer = 16;
        defaultServerFactionClaimBuffer = 32;
        defaultPlayerFactionClaimBuffer = 1;

        attackerCombatTagDuration = 30;
        attackedCombatTagDuration = 10;
        enderpearlDuration = 16;
        crappleDuration = 10;
        gappleDuration = 3600;
        totemDuration = 3600;
    }
}
