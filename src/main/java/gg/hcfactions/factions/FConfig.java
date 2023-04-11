package gg.hcfactions.factions;

import gg.hcfactions.factions.stats.StatsConfig;
import gg.hcfactions.libs.bukkit.services.impl.deathbans.DeathbanConfig;
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

    // faction reinvites
    @Getter public int defaultFactionReinvites;

    // faction power
    @Getter public double playerPowerValue;
    @Getter public int powerTickInterval;
    @Getter public int powerTickPlayerModifier;
    @Getter public boolean reducePowerLossInNether;
    @Getter public boolean reducePowerLossInEnd;

    // faction ratings
    @Getter public int challengerRating;
    @Getter public int diamondRating;
    @Getter public int platinumRating;
    @Getter public int goldRating;
    @Getter public int silverRating;

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
    @Getter public int stuckDuration;
    @Getter public int rallyDuration;
    @Getter public int freezeDuration;
    @Getter public int homeDuration;

    // deathbans
    @Getter public boolean deathbansEnabled;
    @Getter public boolean deathbansStandalone;
    @Getter public int sotwMaxDeathbanDuration;
    @Getter public int normalMaxDeathbanDuration;
    @Getter public int minDeathbanDuration;
    @Getter public int lifeUseDelay;

    // stats
    @Getter public int mapNumber;

    /**
     * Parses deathban fields in to a DeathbanConfig object
     * @return DeathbanConfig
     */
    public DeathbanConfig getDeathbanConfig() {
        return new DeathbanConfig(
                deathbansEnabled,
                deathbansStandalone,
                sotwMaxDeathbanDuration,
                normalMaxDeathbanDuration,
                minDeathbanDuration,
                lifeUseDelay,
                "https://hcfactions.gg/shop"
        );
    }

    /**
     * Parses stats fields in to a StatsConfig object
     * @return StatsConfig
     */
    public StatsConfig getStatsConfig() {
        return new StatsConfig(mapNumber);
    }

    // TODO: load values from config
    public void loadConfig() {
        overworldSpawn = new Location(Bukkit.getWorld("world"), 0.0, 64.0, 0.0);
        netherSpawn = new Location(Bukkit.getWorld("world_nether"), 0.0, 64.0, 0.0);
        endSpawn = new Location(Bukkit.getWorld("world_the_end"), 0.0, 64.0, 0.0);

        minFactionNameLength = 3;
        maxFactionNameLength = 16;
        disallowedFactionNames = Arrays.asList("test");

        defaultFactionReinvites = 5;

        playerPowerValue = 0.4;
        powerTickInterval = 60;
        powerTickPlayerModifier = 1;
        reducePowerLossInNether = true;
        reducePowerLossInEnd = true;

        challengerRating = 10000;
        diamondRating = 8000;
        platinumRating = 6500;
        goldRating = 5000;
        silverRating = 3500;

        defaultServerFactionBuildBuffer = 16;
        defaultServerFactionClaimBuffer = 32;
        defaultPlayerFactionClaimBuffer = 1;

        attackerCombatTagDuration = 30;
        attackedCombatTagDuration = 10;
        enderpearlDuration = 16;
        crappleDuration = 10;
        gappleDuration = 3600;
        totemDuration = 3600;
        stuckDuration = 60;
        rallyDuration = 10;
        freezeDuration = 1800;
        homeDuration = 10;

        deathbansEnabled = true;
        deathbansStandalone = true;
        sotwMaxDeathbanDuration = 10;
        normalMaxDeathbanDuration = 30;
        minDeathbanDuration = 5;
        lifeUseDelay = 5;
    }
}
