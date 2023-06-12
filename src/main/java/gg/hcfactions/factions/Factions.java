package gg.hcfactions.factions;

import com.comphenix.protocol.ProtocolLibrary;
import com.google.common.collect.Lists;
import gg.hcfactions.cx.CXService;
import gg.hcfactions.factions.claims.ClaimManager;
import gg.hcfactions.factions.claims.subclaims.SubclaimManager;
import gg.hcfactions.factions.classes.ClassManager;
import gg.hcfactions.factions.cmd.*;
import gg.hcfactions.factions.crowbar.CrowbarManager;
import gg.hcfactions.factions.displays.DisplayManager;
import gg.hcfactions.factions.events.EventManager;
import gg.hcfactions.factions.faction.FactionManager;
import gg.hcfactions.factions.items.StarterRod;
import gg.hcfactions.factions.listeners.*;
import gg.hcfactions.factions.loggers.CombatLoggerManager;
import gg.hcfactions.factions.models.stats.EStatisticType;
import gg.hcfactions.factions.player.PlayerManager;
import gg.hcfactions.factions.shops.ShopManager;
import gg.hcfactions.factions.state.ServerStateManager;
import gg.hcfactions.factions.stats.StatsManager;
import gg.hcfactions.factions.timers.TimerManager;
import gg.hcfactions.factions.utils.FRecipes;
import gg.hcfactions.factions.waypoints.WaypointManager;
import gg.hcfactions.libs.acf.PaperCommandManager;
import gg.hcfactions.libs.base.connect.impl.mongo.Mongo;
import gg.hcfactions.libs.bukkit.AresPlugin;
import gg.hcfactions.libs.bukkit.services.impl.account.AccountService;
import gg.hcfactions.libs.bukkit.services.impl.deathbans.DeathbanService;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import gg.hcfactions.libs.bukkit.services.impl.punishments.PunishmentService;
import gg.hcfactions.libs.bukkit.services.impl.ranks.RankService;
import gg.hcfactions.libs.bukkit.services.impl.sync.SyncService;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.List;

public final class Factions extends AresPlugin {
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

    @Override
    public void onEnable() {
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
        registerCommand(new LogoutCommand(this));
        registerCommand(new DebugCommand(this));

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

        // db init
        final Mongo mdb = new Mongo(configuration.getMongoUri(), getAresLogger());
        mdb.openConnection();
        registerConnectable(mdb);

        // protocol lib init
        registerProtocolLibrary(ProtocolLibrary.getProtocolManager());

        // declare services
        final RankService rankService = new RankService(this);
        final CXService commandXService = new CXService(this);
        final CustomItemService customItemService = new CustomItemService(this);
        final AccountService accountService = new AccountService(this, configuration.getMongoDatabaseName());
        final DeathbanService deathbanService = new DeathbanService(this, configuration.getDeathbanConfig());
        final SyncService syncService = new SyncService(this, configuration.getMongoDatabaseName());
        final PunishmentService punishmentService = new PunishmentService(this, configuration.getMongoDatabaseName());

        // register services
        registerService(accountService);
        registerService(deathbanService);
        registerService(customItemService);
        registerService(commandXService);
        registerService(rankService);
        registerService(syncService);
        registerService(punishmentService);
        startServices();

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
        registerListener(new NameplateListener(this));
        registerListener(new CosmeticsListener(this));
        registerListener(new CrowbarListener(this));

        // custom recipes
        new FRecipes(this, configuration.getRecipeConfig()).register();

        // starter kit
        if (configuration.starterKitEnabled) {
            customItemService.registerNewItem(new StarterRod());
        }
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

        playerManager = null;
        factionManager = null;
        claimManager = null;
        timerManager = null;
        statsManager = null;
        loggerManager = null;
        serverStateManager = null;
        classManager = null;
        eventManager = null;
        displayManager = null;
        waypointManager = null;
    }
}
