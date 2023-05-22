package gg.hcfactions.factions;

import gg.hcfactions.factions.models.state.EServerState;
import gg.hcfactions.factions.stats.StatsConfig;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import gg.hcfactions.libs.bukkit.services.impl.deathbans.DeathbanConfig;
import gg.hcfactions.libs.bukkit.utils.Configs;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Objects;

public final class FConfig {
    @Getter public Factions plugin;

    @Getter public String mongoUri;
    @Getter public String redisUri;

    // world
    @Getter @Setter public Location overworldSpawn;
    @Getter @Setter public Location endSpawn;
    @Getter @Setter public Location endExit;

    // faction naming
    @Getter public int minFactionNameLength;
    @Getter public int maxFactionNameLength;
    @Getter public List<String> disallowedFactionNames;

    // faction reinvites
    @Getter public int defaultFactionReinvites;

    // faction power
    @Getter public double playerPowerValue;
    @Getter public double powerCap;
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

    // faction limits
    @Getter public int maxFactionSize;

    // claiming
    @Getter public int defaultServerFactionBuildBuffer;
    @Getter public int defaultServerFactionClaimBuffer;
    @Getter public int defaultPlayerFactionClaimBuffer;
    @Getter public double claimBlockValue;
    @Getter public int claimMinSize;
    @Getter public int claimMaxAmount;
    @Getter public double claimRefundPercent;

    // timers
    @Getter public int attackerCombatTagDuration;
    @Getter public int attackedCombatTagDuration;
    @Getter public int enderpearlDuration;
    @Getter public int crappleDuration;
    @Getter public int gappleDuration;
    @Getter public int totemDuration;
    @Getter public int tridentDuration;
    @Getter public int stuckDuration;
    @Getter public int rallyDuration;
    @Getter public int freezeDuration;
    @Getter public int homeDuration;
    @Getter public int sotwProtectionDuration;
    @Getter public int normalProtectionDuration;

    // classes
    @Getter public int minerClassLimit;
    @Getter public int archerClassLimit;
    @Getter public int diverClassLimit;
    @Getter public int rogueClassLimit;
    @Getter public int bardClassLimit;

    // deathbans
    @Getter public boolean deathbansEnabled;
    @Getter public boolean deathbansStandalone;
    @Getter public int eventDeathbanDuration;
    @Getter public int sotwMaxDeathbanDuration;
    @Getter public int normalMaxDeathbanDuration;
    @Getter public int minDeathbanDuration;
    @Getter public int lifeUseDelay;
    @Getter public String shopUrl;

    // display stuffs
    @Getter public String scoreboardTitle;
    @Getter public String scoreboardFooter;

    // server states
    @Getter public EServerState initialServerState;
    @Getter public double eotwBorderShrinkRadius;
    @Getter public int eotwBorderShrinkRate;

    // stats
    @Getter public int mapNumber;

    // economy
    @Getter public double startingBalance;

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
                eventDeathbanDuration,
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

        final PLocatable endExitLoc = Configs.parsePlayerLocation(conf, "factions.spawns.end_exit");
        final PLocatable endSpawnLoc = Configs.parsePlayerLocation(conf, "factions.spawns.end_spawn");
        final PLocatable overworldSpawnLoc = Configs.parsePlayerLocation(conf, "factions.spawns.overworld_spawn");

        endExit = endExitLoc.getBukkitLocation();
        endSpawn = endSpawnLoc.getBukkitLocation();
        overworldSpawn = overworldSpawnLoc.getBukkitLocation();

        mongoUri = conf.getString("databases.mongodb.uri");
        redisUri = conf.getString("databases.redis.uri");

        minFactionNameLength = conf.getInt("factions.naming.min_faction_name");
        maxFactionNameLength = conf.getInt("factions.naming.max_faction_name");
        disallowedFactionNames = conf.getStringList("factions.naming.disallowed_names");

        defaultFactionReinvites = conf.getInt("factions.reinvites");

        maxFactionSize = conf.getInt("factions.limits.max_faction_size");

        playerPowerValue = conf.getDouble("factions.power.player_power_value");
        powerCap = conf.getDouble("factions.power.max_power");
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
        claimBlockValue = conf.getDouble("factions.claiming.block_value");
        claimMinSize = conf.getInt("factions.claiming.min_claim_size");
        claimMaxAmount = conf.getInt("factions.claiming.max_claims");
        claimRefundPercent = conf.getDouble("factions.claiming.refunded_percent");

        attackerCombatTagDuration = conf.getInt("factions.timers.attacker_tag_duration");
        attackedCombatTagDuration = conf.getInt("factions.timers.attacked_tag_duration");
        enderpearlDuration = conf.getInt("factions.timers.enderpearl_duration");
        crappleDuration = conf.getInt("factions.timers.crapple_duration");
        gappleDuration = conf.getInt("factions.timers.gapple_duration");
        totemDuration = conf.getInt("factions.timers.totem_duration");
        tridentDuration = conf.getInt("factions.timers.trident_duration");
        stuckDuration = conf.getInt("factions.timers.stuck_duration");
        rallyDuration = conf.getInt("factions.timers.rally_duration");
        freezeDuration = conf.getInt("factions.timers.freeze_duration");
        homeDuration = conf.getInt("factions.timers.home_duration");
        sotwProtectionDuration = conf.getInt("factions.timers.protection.sotw");
        normalProtectionDuration = conf.getInt("factions.timers.protection.normal");

        minerClassLimit = conf.getInt("classes.limits.miner");
        archerClassLimit = conf.getInt("classes.limits.archer");
        bardClassLimit = conf.getInt("classes.limits.bard");
        rogueClassLimit = conf.getInt("classes.limits.rogue");
        diverClassLimit = conf.getInt("classes.limits.diver");

        scoreboardTitle = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(conf.getString("scoreboard.title")));
        scoreboardFooter = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(conf.getString("scoreboard.footer")));

        deathbansEnabled = conf.getBoolean("deathbans.enabled");
        deathbansStandalone = conf.getBoolean("deathbans.standalone");
        eventDeathbanDuration = conf.getInt("deathbans.max_durations.event");
        sotwMaxDeathbanDuration = conf.getInt("deathbans.max_durations.sotw");
        normalMaxDeathbanDuration = conf.getInt("deathbans.max_durations.normal");
        minDeathbanDuration = conf.getInt("deathbans.min_duration");
        lifeUseDelay = conf.getInt("deathbans.life_use_delay");
        shopUrl = conf.getString("deathbans.shop_url");

        initialServerState = EServerState.fromString(conf.getString("server_state.current_state"));
        eotwBorderShrinkRadius = conf.getDouble("server_state.eotw.border_shrink_radius");
        eotwBorderShrinkRate = conf.getInt("server_state.eotw.border_shrink_rate");

        startingBalance = conf.getDouble("economy.starting_balance");
    }
}
