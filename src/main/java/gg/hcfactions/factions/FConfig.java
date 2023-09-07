package gg.hcfactions.factions;

import com.google.common.collect.Maps;
import gg.hcfactions.factions.models.state.EServerState;
import gg.hcfactions.factions.stats.StatsConfig;
import gg.hcfactions.factions.utils.FRecipes;
import gg.hcfactions.libs.bukkit.services.impl.deathbans.DeathbanConfig;
import gg.hcfactions.libs.bukkit.utils.Configs;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class FConfig {
    @Getter public Factions plugin;

    // databases
    @Getter public String mongoUri;
    @Getter public String mongoDatabaseName;
    @Getter public String playerFactionCollection;
    @Getter public String serverFactionCollection;
    @Getter public String claimCollection;
    @Getter public String subclaimCollection;
    @Getter public String factionPlayerCollection;
    @Getter public String killStatsCollection;
    @Getter public String deathStatsCollection;
    @Getter public String eventStatsCollection;
    @Getter public String playerStatsCollection;

    @Getter public String redisUri;

    // autosave
    @Getter public int factionAutosaveDelay;
    @Getter public int claimAutosaveDelay;
    @Getter public int subclaimAutosaveDelay;
    @Getter public int playerAutosaveDelay;

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
    @Getter public double powerMax;
    @Getter public double powerMin;
    @Getter public int powerTickInterval;
    @Getter public double netherPowerLossReduction;
    @Getter public double endPowerLossReduction;
    @Getter public double eventPowerLossReduction;

    /* @deprecated */
    @Getter public int powerTickPlayerModifier;

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
    @Getter public int chorusDuration;
    @Getter public int totemDuration;
    @Getter public int tridentDuration;
    @Getter public int stuckDuration;
    @Getter public int rallyDuration;
    @Getter public int outpostRestockDuration;
    @Getter public int freezeDuration;
    @Getter public int reinviteRestockDuration;
    @Getter public int homeDuration;
    @Getter public int logoutDuration;
    @Getter public int sotwProtectionDuration;
    @Getter public int normalProtectionDuration;
    @Getter public int enterEndProtectionDuration;
    @Getter public int reconnectCooldownDuration;

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

    // events
    @Getter public int eventTicketLossPerDeath;
    @Getter public int conquestTicketLossPerDeath;

    // display
    @Getter public String scoreboardTitle;
    @Getter public String scoreboardFooter;
    @Getter public int displayUpdateInterval;

    // starter kit
    @Getter public boolean starterKitEnabled;

    // server states
    @Getter public EServerState initialServerState;
    @Getter public double eotwBorderShrinkRadius;
    @Getter public int eotwBorderShrinkRate;

    // stats
    @Getter public int mapNumber;

    // economy
    @Getter public double startingBalance;

    // lunar
    @Getter public boolean useLegacyLunarAPI;

    // lives
    @Getter public Map<String, Integer> firstJoinLives;

    // xp
    @Getter public int loginBonusRequiredTime;
    @Getter public int loginRewardXp;
    @Getter public int dragonKillRewardXp;
    @Getter public int kothCaptureRewardXp;
    @Getter public int palaceCaptureRewardXp;
    @Getter public int playerKillRewardXp;
    @Getter public int diamondMinedRewardXp;
    @Getter public int netheriteMinedRewardXp;

    // custom crafting recipes
    @Getter public boolean saddleRecipeEnabled;
    @Getter public boolean heartOfTheSeaRecipeEnabled;
    @Getter public boolean tridentRecipeEnabled;
    @Getter public boolean chainmailArmorRecipeEnabled;
    @Getter public boolean totemRecipeEnabled;
    @Getter public boolean gappleRecipeEnabled;
    @Getter public boolean nametagRecipeEnabled;
    @Getter public boolean smithingUpgradeRecipeEnabled;
    @Getter public boolean simpleGlisteningMelonEnabled;

    public FConfig(Factions plugin) {
        this.plugin = plugin;
    }

    /**
     * Parses deathban fields in to a DeathbanConfig object
     * @return DeathbanConfig
     */
    public DeathbanConfig getDeathbanConfig() {
        return new DeathbanConfig(
                mongoDatabaseName,
                deathbansEnabled,
                deathbansStandalone,
                eventDeathbanDuration,
                sotwMaxDeathbanDuration,
                normalMaxDeathbanDuration,
                minDeathbanDuration,
                lifeUseDelay,
                shopUrl,
                firstJoinLives
        );
    }

    /**
     * Parses custom recipe config values and returns a recipe config
     * @return Faction Custom Recipe Config
     */
    public FRecipes.Config getRecipeConfig() {
        return new FRecipes.Config(
                saddleRecipeEnabled,
                heartOfTheSeaRecipeEnabled,
                tridentRecipeEnabled,
                chainmailArmorRecipeEnabled,
                totemRecipeEnabled,
                gappleRecipeEnabled,
                nametagRecipeEnabled,
                smithingUpgradeRecipeEnabled,
                simpleGlisteningMelonEnabled
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

        mongoUri = conf.getString("databases.mongo.uri");
        mongoDatabaseName = conf.getString("databases.mongo.database");
        playerFactionCollection = conf.getString("databases.mongo.collections.player_factions");
        serverFactionCollection = conf.getString("databases.mongo.collections.server_factions");
        claimCollection = conf.getString("databases.mongo.collections.claims");
        subclaimCollection = conf.getString("databases.mongo.collections.subclaims");
        factionPlayerCollection = conf.getString("databases.mongo.collections.faction_players");
        killStatsCollection = conf.getString("databases.mongo.collections.stats_kills");
        deathStatsCollection = conf.getString("databases.mongo.collections.stats_deaths");
        eventStatsCollection = conf.getString("databases.mongo.collections.stats_events");
        playerStatsCollection = conf.getString("databases.mongo.collections.stats_players");

        redisUri = conf.getString("databases.redis.uri");
        plugin.getAresLogger().info("Using MongoDB Database: " + mongoDatabaseName);

        factionAutosaveDelay = conf.getInt("autosave.factions");
        playerAutosaveDelay = conf.getInt("autosave.players");
        claimAutosaveDelay = conf.getInt("autosave.claims");
        subclaimAutosaveDelay = conf.getInt("autosave.subclaims");

        maxFactionSize = conf.getInt("factions.max_faction_size");
        defaultFactionReinvites = conf.getInt("factions.reinvites");
        minFactionNameLength = conf.getInt("factions.name.min_length");
        maxFactionNameLength = conf.getInt("factions.name.max_length");
        disallowedFactionNames = conf.getStringList("factions.name.disallowed");
        plugin.getAresLogger().info("Max Faction Size: " + maxFactionSize);
        plugin.getAresLogger().info("Faction Re-invites: " + defaultFactionReinvites);
        plugin.getAresLogger().info("Minimum Faction Name Length: " + minFactionNameLength);
        plugin.getAresLogger().info("Maximum Faction Name Length: " + maxFactionNameLength);
        plugin.getAresLogger().info("Disallowed Faction Names: " + disallowedFactionNames.size());

        archerClassLimit = conf.getInt("factions.class_limits.archer");
        bardClassLimit = conf.getInt("factions.class_limits.bard");
        rogueClassLimit = conf.getInt("factions.class_limits.rogue");
        minerClassLimit = conf.getInt("factions.class_limits.miner");
        diverClassLimit = conf.getInt("factions.class_limits.diver");
        plugin.getAresLogger().info("Archer Class Limit: " + archerClassLimit);
        plugin.getAresLogger().info("Bard Class Limit: " + bardClassLimit);
        plugin.getAresLogger().info("Rogue Class Limit: " + rogueClassLimit);
        plugin.getAresLogger().info("Diver Class Limit: " + diverClassLimit);
        plugin.getAresLogger().info("Miner Class Limit: " + minerClassLimit);

        powerMax = conf.getDouble("factions.power.max_power");
        powerMin = conf.getDouble("factions.power.min_power");
        playerPowerValue = conf.getDouble("factions.power.player_power_value");
        powerTickInterval = conf.getInt("factions.power.power_tick_interval");
        netherPowerLossReduction = conf.getDouble("factions.power.power_loss_reductions.nether");
        endPowerLossReduction = conf.getDouble("factions.power.power_loss_reductions.end");
        eventPowerLossReduction = conf.getDouble("factions.power.power_loss_reductions.event");
        plugin.getAresLogger().info("Min Faction Power: " + powerMin);
        plugin.getAresLogger().info("Max Faction Power: " + powerMax);
        plugin.getAresLogger().info("Player Power Value: " + playerPowerValue);
        plugin.getAresLogger().info("Power Tick Interval (Base): " + powerTickInterval);
        plugin.getAresLogger().info("Nether Power Loss Reduction: " + netherPowerLossReduction);
        plugin.getAresLogger().info("The End Power Loss Reduction: " + endPowerLossReduction);
        plugin.getAresLogger().info("Event Power Loss Reduction: " + eventPowerLossReduction);

        claimMinSize = conf.getInt("factions.claiming.min_size");
        claimMaxAmount = conf.getInt("factions.claiming.max_claims");
        claimBlockValue = conf.getDouble("factions.claiming.block_value");
        claimRefundPercent = conf.getDouble("factions.claiming.refund_percentage");
        plugin.getAresLogger().info("Minimum Claim Size: " + claimMinSize);
        plugin.getAresLogger().info("Max Claim Amount: " + claimMaxAmount);
        plugin.getAresLogger().info("Claim Block Value: " + claimBlockValue);
        plugin.getAresLogger().info("Claim Refund Percentage: " + claimRefundPercent);

        defaultServerFactionBuildBuffer = conf.getInt("factions.claiming.buffer_values.server_build");
        defaultServerFactionClaimBuffer = conf.getInt("factions.claiming.buffer_values.server_claim");
        defaultPlayerFactionClaimBuffer = conf.getInt("factions.claiming.buffer_values.player_claim");
        plugin.getAresLogger().info("Server Faction Build Buffer: " + defaultServerFactionBuildBuffer);
        plugin.getAresLogger().info("Server Faction Claim Buffer: " + defaultServerFactionClaimBuffer);
        plugin.getAresLogger().info("Player Faction Claim Buffer: " + defaultPlayerFactionClaimBuffer);

        freezeDuration = conf.getInt("factions.timers.freeze");
        rallyDuration = conf.getInt("factions.timers.rally");
        outpostRestockDuration = conf.getInt("factions.timers.outpost");
        reinviteRestockDuration = conf.getInt("factions.timers.reinvite");
        plugin.getAresLogger().info("Faction Freeze Timer Duration: " + freezeDuration);
        plugin.getAresLogger().info("Faction Rally Timer Duration: " + rallyDuration);
        plugin.getAresLogger().info("Outpost Restock Duration: " + outpostRestockDuration);
        plugin.getAresLogger().info("Reinvite Restock Duration: " + reinviteRestockDuration);

        overworldSpawn = Configs.parsePlayerLocation(conf, "spawns.overworld").getBukkitLocation();
        endSpawn = Configs.parsePlayerLocation(conf, "spawns.end_spawn").getBukkitLocation();
        endExit = Configs.parsePlayerLocation(conf, "spawns.end_exit").getBukkitLocation();
        plugin.getAresLogger().info("Overworld Spawn set to: " + overworldSpawn.toString());
        plugin.getAresLogger().info("End Spawn set to: " + endSpawn.toString());
        plugin.getAresLogger().info("End Exit set to: " + endExit.toString());

        starterKitEnabled = conf.getBoolean("starter_kit.enabled");

        attackerCombatTagDuration = conf.getInt("player.timers.combat_tag.attacker");
        attackedCombatTagDuration = conf.getInt("player.timers.combat_tag.attacked");
        enderpearlDuration = conf.getInt("player.timers.enderpearl");
        crappleDuration = conf.getInt("player.timers.crapple");
        gappleDuration = conf.getInt("player.timers.gapple");
        chorusDuration = conf.getInt("player.timers.chorus");
        totemDuration = conf.getInt("player.timers.totem");
        tridentDuration = conf.getInt("player.timers.trident");
        stuckDuration = conf.getInt("player.timers.stuck");
        homeDuration = conf.getInt("player.timers.home");
        logoutDuration = conf.getInt("player.timers.logout");
        normalProtectionDuration = conf.getInt("player.timers.protection.normal");
        sotwProtectionDuration = conf.getInt("player.timers.protection.sotw");
        enterEndProtectionDuration = conf.getInt("player.timers.protection.enter_end");
        reconnectCooldownDuration = conf.getInt("player.reconnect_cooldown");
        plugin.getAresLogger().info("Combat Tag (Attacker) Duration: " + attackerCombatTagDuration);
        plugin.getAresLogger().info("Combat Tag (Attacked) Duration: " + attackedCombatTagDuration);
        plugin.getAresLogger().info("Enderpearl Duration: " + enderpearlDuration);
        plugin.getAresLogger().info("Crapple Duration: " + crappleDuration);
        plugin.getAresLogger().info("Gapple Duration: " + gappleDuration);
        plugin.getAresLogger().info("Chorus Duration: " + chorusDuration);
        plugin.getAresLogger().info("Totem Duration: " + totemDuration);
        plugin.getAresLogger().info("Trident Duration: " + tridentDuration);
        plugin.getAresLogger().info("Stuck Duration: " + stuckDuration);
        plugin.getAresLogger().info("Home Duration: " + homeDuration);
        plugin.getAresLogger().info("Logout Duration: " + logoutDuration);
        plugin.getAresLogger().info("Protection (Normal) Duration: " + normalProtectionDuration);
        plugin.getAresLogger().info("Protection (SOTW) Duration: " + sotwProtectionDuration);
        plugin.getAresLogger().info("Protection (Enter End) Duration: " + enterEndProtectionDuration);
        plugin.getAresLogger().info("Player Reconnect Cooldown Duration: " + reconnectCooldownDuration);

        deathbansEnabled = conf.getBoolean("deathbans.enabled");
        deathbansStandalone = conf.getBoolean("deathbans.standalone");
        sotwMaxDeathbanDuration = conf.getInt("deathbans.ban_durations.sotw");
        normalMaxDeathbanDuration = conf.getInt("deathbans.ban_durations.normal");
        eventDeathbanDuration = conf.getInt("deathbans.ban_durations.event");
        minDeathbanDuration = conf.getInt("deathbans.min_duration");
        lifeUseDelay = conf.getInt("deathbans.life_use_delay");
        shopUrl = conf.getString("deathbans.shop_url");
        plugin.getAresLogger().info("Deathbans Enabled: " + deathbansEnabled);
        plugin.getAresLogger().info("Deathbans running in Standalone: " + deathbansStandalone);
        plugin.getAresLogger().info("Deathban Duration (SOTW): " + sotwMaxDeathbanDuration);
        plugin.getAresLogger().info("Deathban Duration (NORMAL): " + normalMaxDeathbanDuration);
        plugin.getAresLogger().info("Deathban Duration (EVENT): " + eventDeathbanDuration);
        plugin.getAresLogger().info("Minimum Deathban Duration: " + minDeathbanDuration);
        plugin.getAresLogger().info("Life Use Delay: " + lifeUseDelay);
        plugin.getAresLogger().info("Shop URL: " + shopUrl);

        eventTicketLossPerDeath = conf.getInt("events.koth.ticket_loss_per_death");
        conquestTicketLossPerDeath = conf.getInt("events.conquest.ticket_loss_per_death");
        plugin.getAresLogger().info("KOTH Ticket Loss Per Death: " + eventTicketLossPerDeath);

        mapNumber = conf.getInt("stats.map");
        plugin.getAresLogger().info("Stats tracked under Map: " + mapNumber);

        startingBalance = conf.getInt("economy.starting_balance");
        plugin.getAresLogger().info("Economy Starting Balance: " + startingBalance);

        scoreboardTitle = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(conf.getString("scoreboard.title")));
        scoreboardFooter = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(conf.getString("scoreboard.footer")));
        plugin.getAresLogger().info("Scoreboard Title: " + scoreboardTitle);
        plugin.getAresLogger().info("Scoreboard Footer: " + scoreboardFooter);

        initialServerState = EServerState.fromString(conf.getString("state.current"));
        eotwBorderShrinkRadius = conf.getDouble("state.eotw.border_shrink_radius");
        eotwBorderShrinkRate = conf.getInt("state.eotw.border_shrink_rate");
        plugin.getAresLogger().info("Initial Server State: " + initialServerState.name());
        plugin.getAresLogger().info("EOTW Border Shrink Radius: " + eotwBorderShrinkRadius);
        plugin.getAresLogger().info("EOTW Border Shrink Rate: " + eotwBorderShrinkRate);

        useLegacyLunarAPI = conf.getBoolean("lunar_api.use_legacy");
        plugin.getAresLogger().info("Use Legacy Lunar API: " + useLegacyLunarAPI);

        if (conf.get("lives.first_join_lives") != null) {
            final Map<String, Integer> res = Maps.newHashMap();

            for (String rankName : Objects.requireNonNull(conf.getConfigurationSection("lives.first_join_lives")).getKeys(false)) {
                final int amount = conf.getInt("lives.first_join_lives." + rankName);
                res.put(rankName, amount);
            }

            firstJoinLives = res;
            plugin.getAresLogger().info("First-join Lives Found: " + firstJoinLives.size());
        }

        loginBonusRequiredTime = conf.getInt("xp.login.time");
        loginRewardXp = conf.getInt("xp.login.bonus");
        dragonKillRewardXp = conf.getInt("xp.dragon_kill");
        kothCaptureRewardXp = conf.getInt("xp.koth_capture");
        palaceCaptureRewardXp = conf.getInt("xp.palace_capture");
        playerKillRewardXp = conf.getInt("xp.player_kill");
        diamondMinedRewardXp = conf.getInt("xp.diamond_mined");
        netheriteMinedRewardXp = conf.getInt("xp.netherite_mined");
        plugin.getAresLogger().info("Login Bonus Time: " + loginBonusRequiredTime);
        plugin.getAresLogger().info("Login Bonus Amount: " + loginRewardXp);
        plugin.getAresLogger().info("Dragon Kill Bonus Amount: " + dragonKillRewardXp);
        plugin.getAresLogger().info("KOTH Capture Bonus Amount: " + kothCaptureRewardXp);
        plugin.getAresLogger().info("Palace Capture Bonus Amount: " + palaceCaptureRewardXp);
        plugin.getAresLogger().info("Player Kill Bonus Amount: " + playerKillRewardXp);
        plugin.getAresLogger().info("Diamond Mined Bonus Amount: " + diamondMinedRewardXp);
        plugin.getAresLogger().info("Netherite Mined Bonus Amount: " + netheriteMinedRewardXp);

        saddleRecipeEnabled = conf.getBoolean("custom_recipes.saddle");
        heartOfTheSeaRecipeEnabled = conf.getBoolean("custom_recipes.heart_of_the_sea");
        tridentRecipeEnabled = conf.getBoolean("custom_recipes.trident");
        chainmailArmorRecipeEnabled = conf.getBoolean("custom_recipes.chainmail_armor");
        totemRecipeEnabled = conf.getBoolean("custom_recipes.totem");
        gappleRecipeEnabled = conf.getBoolean("custom_recipes.gapple");
        nametagRecipeEnabled = conf.getBoolean("custom_recipes.nametag");
        smithingUpgradeRecipeEnabled = conf.getBoolean("custom_recipes.smithing_upgrade");
        simpleGlisteningMelonEnabled = conf.getBoolean("custom_recipes.easy_glistening_melon");
        plugin.getAresLogger().info("Saddle Recipe: " + saddleRecipeEnabled);
        plugin.getAresLogger().info("Heart of the Sea Recipe: " + heartOfTheSeaRecipeEnabled);
        plugin.getAresLogger().info("Trident Recipe: " + tridentRecipeEnabled);
        plugin.getAresLogger().info("Chainmail Armor Recipe: " + chainmailArmorRecipeEnabled);
        plugin.getAresLogger().info("Totem Recipe: " + totemRecipeEnabled);
        plugin.getAresLogger().info("Gapple Recipe: " + gappleRecipeEnabled);
        plugin.getAresLogger().info("Nametag Recipe: " + nametagRecipeEnabled);
        plugin.getAresLogger().info("Simple Glistening Melon Recipe: " + simpleGlisteningMelonEnabled);
    }
}
