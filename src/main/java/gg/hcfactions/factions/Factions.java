package gg.hcfactions.factions;

import com.google.common.collect.Lists;
import gg.hcfactions.cx.CXService;
import gg.hcfactions.factions.anticlean.AnticleanManager;
import gg.hcfactions.factions.battlepass.BattlepassManager;
import gg.hcfactions.factions.bosses.BossManager;
import gg.hcfactions.factions.claims.ClaimManager;
import gg.hcfactions.factions.claims.subclaims.SubclaimManager;
import gg.hcfactions.factions.classes.ClassManager;
import gg.hcfactions.factions.cmd.*;
import gg.hcfactions.factions.crowbar.CrowbarManager;
import gg.hcfactions.factions.displays.DisplayManager;
import gg.hcfactions.factions.events.EventManager;
import gg.hcfactions.factions.faction.FactionManager;
import gg.hcfactions.factions.items.Crowbar;
import gg.hcfactions.factions.items.StarterRod;
import gg.hcfactions.factions.items.Sugarcube;
import gg.hcfactions.factions.items.horn.impl.BerserkBattleHorn;
import gg.hcfactions.factions.items.horn.impl.ChargeBattleHorn;
import gg.hcfactions.factions.items.horn.impl.CleanseBattleHorn;
import gg.hcfactions.factions.items.horn.impl.RetreatBattleHorn;
import gg.hcfactions.factions.items.mythic.impl.*;
import gg.hcfactions.factions.listeners.*;
import gg.hcfactions.factions.loggers.CombatLoggerManager;
import gg.hcfactions.factions.models.stats.EStatisticType;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.outposts.OutpostManager;
import gg.hcfactions.factions.player.PlayerManager;
import gg.hcfactions.factions.shops.ShopManager;
import gg.hcfactions.factions.state.ServerStateManager;
import gg.hcfactions.factions.stats.StatsManager;
import gg.hcfactions.factions.timers.TimerManager;
import gg.hcfactions.factions.waypoints.WaypointManager;
import gg.hcfactions.libs.acf.PaperCommandManager;
import gg.hcfactions.libs.base.connect.impl.mongo.Mongo;
import gg.hcfactions.libs.base.connect.impl.redis.Redis;
import gg.hcfactions.libs.bukkit.AresPlugin;
import gg.hcfactions.libs.bukkit.services.impl.account.AccountService;
import gg.hcfactions.libs.bukkit.services.impl.alts.AltService;
import gg.hcfactions.libs.bukkit.services.impl.automod.AutomodService;
import gg.hcfactions.libs.bukkit.services.impl.deathbans.DeathbanService;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import gg.hcfactions.libs.bukkit.services.impl.punishments.PunishmentService;
import gg.hcfactions.libs.bukkit.services.impl.ranks.RankService;
import gg.hcfactions.libs.bukkit.services.impl.recipe.CustomRecipeService;
import gg.hcfactions.libs.bukkit.services.impl.reports.ReportService;
import gg.hcfactions.libs.bukkit.services.impl.sync.SyncService;
import gg.hcfactions.libs.bukkit.services.impl.xp.XPService;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;

import java.util.List;

public final class Factions extends AresPlugin {
    @Getter public static Factions instance;

    @Getter public final NamespacedKey namespacedKey = new NamespacedKey(this, "factions");
    @Getter public FConfig configuration;
    @Getter public PlayerManager playerManager;
    @Getter public FactionManager factionManager;
    @Getter public ClaimManager claimManager;
    @Getter public SubclaimManager subclaimManager;
    @Getter public TimerManager timerManager;
    @Getter public ServerStateManager serverStateManager;
    @Getter public StatsManager statsManager;
    @Getter public CombatLoggerManager loggerManager;
    @Getter public ClassManager classManager;
    @Getter public EventManager eventManager;
    @Getter public ShopManager shopManager;
    @Getter public DisplayManager displayManager;
    @Getter public WaypointManager waypointManager;
    @Getter public CrowbarManager crowbarManager;
    @Getter public OutpostManager outpostManager;
    @Getter public BattlepassManager battlepassManager;
    @Getter public BossManager bossManager;
    @Getter public AnticleanManager anticleanManager;

    @Override
    public void onLoad() {
        super.onLoad();
        registerPacketEvents();
    }

    @Override
    public void onEnable() {
        instance = this;

        super.onEnable();

        // config init
        configuration = new FConfig(this);
        configuration.loadConfig();

        // logger init
        registerLogger("Factions");

        // command init
        final PaperCommandManager cmdMng = new PaperCommandManager(this);
        cmdMng.enableUnstableAPI("help");
        registerCommandManager(cmdMng);

        registerCommand(new FactionCommand(this));
        registerCommand(new StatsCommand(this));
        registerCommand(new StateCommand(this));
        registerCommand(new TimerCommand(this));
        registerCommand(new PvPCommand(this));
        registerCommand(new EventCommand(this));
        registerCommand(new KOTHCommand(this));
        registerCommand(new ShopCommand(this));
        registerCommand(new WalletCommand(this));
        registerCommand(new SpawnCommand(this));
        registerCommand(new DisplayCommand(this));
        registerCommand(new FactionHelpCommand(this));
        registerCommand(new FocusCommand(this));
        registerCommand(new LogoutCommand(this));
        registerCommand(new ClassCommand(this));
        registerCommand(new DebugCommand(this));
        registerCommand(new BattlepassCommand(this));
        registerCommand(new BossCommand(this));

        cmdMng.getCommandCompletions().registerAsyncCompletion("pfactions", ctx -> {
            final List<String> res = Lists.newArrayList();

            if (factionManager == null) {
                return res;
            }

            factionManager.getPlayerFactions().forEach(pf -> res.add(pf.getName()));
            return res;
        });

        cmdMng.getCommandCompletions().registerAsyncCompletion("pfactionsmixed", ctx -> {
            final List<String> res = Lists.newArrayList();

            if (factionManager == null) {
                return res;
            }

            factionManager.getPlayerFactions().forEach(pf -> res.add(pf.getName()));
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                if (!res.contains(onlinePlayer.getName())) {
                    res.add(onlinePlayer.getName());
                }
            });

            return res;
        });

        cmdMng.getCommandCompletions().registerAsyncCompletion("sfactions", ctx -> {
            final List<String> res = Lists.newArrayList();

            if (factionManager == null) {
                return res;
            }

            factionManager.getServerFactions().forEach(sf -> res.add(sf.getName()));
            return res;
        });

        cmdMng.getCommandCompletions().registerAsyncCompletion("events", ctx -> {
            final List<String> res = Lists.newArrayList();

            if (eventManager == null) {
                return res;
            }

            eventManager.getEventRepository().forEach(e -> res.add(e.getName()));
            return res;
        });

        cmdMng.getCommandCompletions().registerAsyncCompletion("stattypes", ctx -> {
            final List<String> res = Lists.newArrayList();

            for (EStatisticType type : EStatisticType.values()) {
                res.add(type.name().toLowerCase());
            }

            return res;
        });

        cmdMng.getCommandCompletions().registerAsyncCompletion("timers", ctx -> {
            final List<String> res = Lists.newArrayList();

            for (ETimerType type : ETimerType.values()) {
                res.add(type.name().toLowerCase());
            }

            return res;
        });

        // db init
        final Mongo mdb = new Mongo(configuration.getMongoUri(), getAresLogger());
        final Redis redis = new Redis(configuration.getRedisUri(), getAresLogger());

        mdb.openConnection();
        redis.openConnection();

        registerConnectable(mdb);
        registerConnectable(redis);

        // declare services
        final RankService rankService = new RankService(this);
        final CXService commandXService = new CXService(this);
        final CustomItemService customItemService = new CustomItemService(this, namespacedKey);
        final AccountService accountService = new AccountService(this, configuration.getMongoDatabaseName());
        final DeathbanService deathbanService = new DeathbanService(this, configuration.getDeathbanConfig());
        final SyncService syncService = new SyncService(this, configuration.getMongoDatabaseName());
        final PunishmentService punishmentService = new PunishmentService(this, configuration.getMongoDatabaseName());
        final AutomodService automodService = new AutomodService(this);
        final ReportService reportService = new ReportService(this);
        final AltService altService = new AltService(this);
        final CustomRecipeService customRecipeService = new CustomRecipeService(this);
        final XPService xpService = new XPService(this, configuration.getMongoDatabaseName(), "xp_players", "xp_transactions");

        // register services
        registerService(accountService);
        registerService(deathbanService);
        registerService(commandXService);
        registerService(rankService);
        registerService(syncService);
        registerService(punishmentService);
        registerService(automodService);
        registerService(reportService);
        registerService(altService);
        registerService(xpService);
        registerService(customRecipeService);

        // custom items
        registerService(customItemService);
        customItemService.registerNewItem(new Sugarcube(this));
        customItemService.registerNewItem(new RetreatBattleHorn(this));
        customItemService.registerNewItem(new CleanseBattleHorn(this));
        customItemService.registerNewItem(new ChargeBattleHorn(this));
        customItemService.registerNewItem(new BerserkBattleHorn(this));
        customItemService.registerNewItem(new StarterRod(this));
        customItemService.registerNewItem(new DeepslateMiner(this));
        customItemService.registerNewItem(new Crowbar(this));

        customItemService.registerNewItem(new Ghostblade(this, new Ghostblade.GhostbladeConfig(
                20,
                16,
                5,
                10,
                5.0f
        )));

        customItemService.registerNewItem(new Hullbreaker(this, new Hullbreaker.HullbreakerConfig(
                5,
                0,
                16,
                3
        )));

        customItemService.registerNewItem(new CrimsonFang(this, new CrimsonFang.CrimsonFangConfig(
                0.25D,
                1,
                10
        )));

        customItemService.registerNewItem(new NeptunesFury(this, new NeptunesFury.NeptunesFuryConfig(
                6.0,
                3.0
        )));

        customItemService.registerNewItem(new SerpentsImpaler(this, new SerpentsImpaler.SerpentsImpalerConfig(
                50.0f,
                3,
                0.5
        )));

        startServices();

        // initialize gson
        registerGson();

        // declare managers
        playerManager = new PlayerManager(this);
        factionManager = new FactionManager(this);
        claimManager = new ClaimManager(this);
        subclaimManager = new SubclaimManager(this);
        timerManager = new TimerManager(this);
        serverStateManager = new ServerStateManager(this);
        statsManager = new StatsManager(this, configuration.getStatsConfig());
        loggerManager = new CombatLoggerManager(this);
        classManager = new ClassManager(this);
        eventManager = new EventManager(this);
        shopManager = new ShopManager(this);
        displayManager = new DisplayManager(this);
        waypointManager = new WaypointManager(this);
        crowbarManager = new CrowbarManager(this);
        outpostManager = new OutpostManager(this);
        battlepassManager = new BattlepassManager(this);
        bossManager = new BossManager(this);
        anticleanManager = new AnticleanManager(this);

        factionManager.onEnable();
        playerManager.onEnable();
        claimManager.onEnable();
        subclaimManager.onEnable();
        timerManager.onEnable();
        serverStateManager.onEnable();
        statsManager.onEnable();
        loggerManager.onEnable();
        classManager.onEnable();
        eventManager.onEnable();
        shopManager.onEnable();
        displayManager.onEnable();
        waypointManager.onEnable();
        crowbarManager.onEnable();
        outpostManager.onEnable();
        battlepassManager.onEnable();
        bossManager.onEnable();
        anticleanManager.onEnable();

        // register listeners
        registerListener(new PlayerListener(this));
        registerListener(new TimerListener(this));
        registerListener(new ChatListener(this));
        registerListener(new CombatListener(this));
        registerListener(new DeathbanListener(this));
        registerListener(new StatsListener(this));
        registerListener(new StateListener(this));
        registerListener(new TrackedItemListener(this));
        registerListener(new CombatLoggerListener(this));
        registerListener(new ClassListener(this));
        registerListener(new FactionListener(this));
        registerListener(new PillarListener(this));
        registerListener(new ClaimBuilderListener(this));
        registerListener(new ClaimListener(this));
        registerListener(new ShieldListener(this));
        registerListener(new SubclaimBuilderListener(this));
        registerListener(new SubclaimListener(this));
        registerListener(new EventListener(this));
        registerListener(new EventBuilderListener(this));
        registerListener(new FoundOreListener(this));
        registerListener(new SpawnListener(this));
        registerListener(new WaypointListener(this));
        registerListener(new CosmeticsListener(this));
        registerListener(new CrowbarListener(this));
        registerListener(new OutpostListener(this));
        registerListener(new HorseListener(this));
        registerListener(new ShopListener(this));
        registerListener(new HornListener(this));
        registerListener(new BattlepassListener(this));
        registerListener(new XPListener(this));
        registerListener(new BossListener(this));
        registerListener(new MythicItemListener(this));
        registerListener(new EventTrackerListener(this));
        registerListener(new AnticleanListener(this));
        // registerListener(new DebugListener()); // TODO: Disable this
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // stop services
        stopServices();

        // disable and unregister managers
        claimManager.onDisable();
        subclaimManager.onDisable();
        playerManager.onDisable();
        factionManager.onDisable();
        timerManager.onDisable();
        serverStateManager.onDisable();
        statsManager.onDisable();
        loggerManager.onDisable();
        classManager.onDisable();
        eventManager.onDisable();
        shopManager.onDisable();
        displayManager.onDisable();
        waypointManager.onDisable();
        crowbarManager.onDisable();
        outpostManager.onDisable();
        battlepassManager.onDisable();
        bossManager.onDisable();
        anticleanManager.onDisable();
    }
}
