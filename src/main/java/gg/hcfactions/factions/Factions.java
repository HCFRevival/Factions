package gg.hcfactions.factions;

import com.comphenix.protocol.ProtocolLibrary;
import gg.hcfactions.cx.CXService;
import gg.hcfactions.factions.claims.ClaimManager;
import gg.hcfactions.factions.claims.subclaims.SubclaimManager;
import gg.hcfactions.factions.classes.ClassManager;
import gg.hcfactions.factions.cmd.*;
import gg.hcfactions.factions.events.EventManager;
import gg.hcfactions.factions.faction.FactionManager;
import gg.hcfactions.factions.listeners.*;
import gg.hcfactions.factions.loggers.CombatLoggerManager;
import gg.hcfactions.factions.player.PlayerManager;
import gg.hcfactions.factions.state.ServerStateManager;
import gg.hcfactions.factions.stats.StatsManager;
import gg.hcfactions.factions.timers.TimerManager;
import gg.hcfactions.libs.acf.PaperCommandManager;
import gg.hcfactions.libs.base.connect.impl.mongo.Mongo;
import gg.hcfactions.libs.bukkit.AresPlugin;
import gg.hcfactions.libs.bukkit.services.impl.account.AccountService;
import gg.hcfactions.libs.bukkit.services.impl.deathbans.DeathbanService;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import lombok.Getter;

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
        registerCommand(new DebugCommand());

        // db init
        final Mongo mdb = new Mongo(configuration.getMongoUri(), getAresLogger());
        mdb.openConnection();
        registerConnectable(mdb);

        // protocol lib init
        registerProtocolLibrary(ProtocolLibrary.getProtocolManager());

        // declare services
        final AccountService accountService = new AccountService(this);
        final DeathbanService deathbanService = new DeathbanService(this, configuration.getDeathbanConfig());
        final CustomItemService customItemService = new CustomItemService(this);
        final CXService commandXService = new CXService(this);

        // register services
        registerService(accountService);
        registerService(deathbanService);
        registerService(customItemService);
        registerService(commandXService);
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
        registerListener(new EventBuilderListener(this));
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

        playerManager = null;
        factionManager = null;
        claimManager = null;
        timerManager = null;
        statsManager = null;
        loggerManager = null;
        serverStateManager = null;
        classManager = null;
        eventManager = null;
    }
}
