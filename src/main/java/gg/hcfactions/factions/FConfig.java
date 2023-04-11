package gg.hcfactions.factions;

import gg.hcfactions.factions.stats.StatsConfig;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.services.impl.deathbans.DeathbanConfig;
import gg.hcfactions.libs.bukkit.utils.Configs;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;

public final class FConfig {
    @Getter public Factions plugin;

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
    @Getter public String shopUrl;

    // stats
    @Getter public int mapNumber;

    public FConfig(Factions plugin) {
        this.plugin = plugin;
    }

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
                shopUrl
        );
    }

    /**
     * Parses stats fields in to a StatsConfig object
     * @return StatsConfig
     */
    public StatsConfig getStatsConfig() {
        return new StatsConfig(mapNumber);
    }

    public void loadConfig() {
        final YamlConfiguration conf = plugin.loadConfiguration("config");
        final PLocatable overworldLoc = Configs.parsePlayerLocation(conf, "factions.spawns.overworld");
        final PLocatable netherLoc = Configs.parsePlayerLocation(conf, "factions.spawns.overworld");
        final PLocatable endLoc = Configs.parsePlayerLocation(conf, "factions.spawns.overworld");

        overworldSpawn = overworldLoc.getBukkitLocation();
        netherSpawn = netherLoc.getBukkitLocation();
        endSpawn = endLoc.getBukkitLocation();

        minFactionNameLength = conf.getInt("factions.naming.min_faction_name");
        maxFactionNameLength = conf.getInt("factions.naming.max_faction_name");
        disallowedFactionNames = conf.getStringList("factions.naming.disallowed_names");

        defaultFactionReinvites = conf.getInt("factions.reinvites");

        playerPowerValue = conf.getDouble("factions.power.player_power_value");
        powerTickInterval = conf.getInt("factions.power.power_tick_interval");
        powerTickPlayerModifier = conf.getInt("factions.power_tick_player_modifier");
        reducePowerLossInNether = conf.getBoolean("factions.power.power_loss_reductions.nether");
        reducePowerLossInEnd = conf.getBoolean("factions.power.power_loss_reduction.end");

        challengerRating = conf.getInt("factions.ratings.challenger");
        diamondRating = conf.getInt("factions.ratings.diamond");
        platinumRating = conf.getInt("factions.ratings.platinum");
        goldRating = conf.getInt("factions.ratings.gold");
        silverRating = conf.getInt("factions.ratings.silver");

        defaultServerFactionBuildBuffer = conf.getInt("factions.claiming.default_buffers.server_faction_build");
        defaultServerFactionClaimBuffer = conf.getInt("factions.claiming.default_buffers.server_faction_claim");
        defaultPlayerFactionClaimBuffer = conf.getInt("factions.claiming.default_buffers.player_faction_claim");

        attackerCombatTagDuration = conf.getInt("factions.timers.attacker_tag_duration");
        attackedCombatTagDuration = conf.getInt("factions.timers.attacked_tag_duration");
        enderpearlDuration = conf.getInt("factions.timers.enderpearl_duration");
        crappleDuration = conf.getInt("factions.timers.crapple_duration");
        gappleDuration = conf.getInt("factions.timers.gapple_duration");
        totemDuration = conf.getInt("factions.timers.totem_duration");
        stuckDuration = conf.getInt("factions.timers.stuck_duration");
        rallyDuration = conf.getInt("factions.timers.rally_duration");
        freezeDuration = conf.getInt("factions.timers.freeze_duration");
        homeDuration = conf.getInt("factions.timers.home_duration");

        deathbansEnabled = conf.getBoolean("deathbans.enabled");
        deathbansStandalone = conf.getBoolean("deathbans.standalone");
        sotwMaxDeathbanDuration = conf.getInt("deathbans.max_durations.sotw");
        normalMaxDeathbanDuration = conf.getInt("deathbans.max_durations.normal");
        minDeathbanDuration = conf.getInt("deathbans.min_duration");
        lifeUseDelay = conf.getInt("deathbans.life_use_delay");
        shopUrl = conf.getString("deathbans.shop_url");
    }
}
