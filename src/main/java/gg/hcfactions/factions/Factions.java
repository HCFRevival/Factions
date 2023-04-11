package gg.hcfactions.factions;

import gg.hcfactions.factions.claims.ClaimManager;
import gg.hcfactions.factions.cmd.FactionCommand;
import gg.hcfactions.factions.cmd.StatsCommand;
import gg.hcfactions.factions.faction.FactionManager;
import gg.hcfactions.factions.listeners.*;
import gg.hcfactions.factions.player.PlayerManager;
import gg.hcfactions.factions.state.ServerStateManager;
import gg.hcfactions.factions.stats.StatsManager;
import gg.hcfactions.factions.timers.TimerManager;
import gg.hcfactions.libs.acf.PaperCommandManager;
import gg.hcfactions.libs.base.connect.impl.mongo.Mongo;
import gg.hcfactions.libs.bukkit.AresPlugin;
import gg.hcfactions.libs.bukkit.services.impl.account.AccountService;
import gg.hcfactions.libs.bukkit.services.impl.deathbans.DeathbanService;
import lombok.Getter;

public final class Factions extends AresPlugin {
    @Getter public FConfig configuration;
    @Getter public PlayerManager playerManager;
    @Getter public FactionManager factionManager;
    @Getter public ClaimManager claimManager;
    @Getter public TimerManager timerManager;
    @Getter public ServerStateManager serverStateManager;
    @Getter public StatsManager statsManager;

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

        // db init
        final Mongo mdb = new Mongo(configuration.getMongoUri(), getAresLogger());
        mdb.openConnection();
        registerConnectable(mdb);

        // declare services
        final AccountService accountService = new AccountService(this);
        final DeathbanService deathbanService = new DeathbanService(this, configuration.getDeathbanConfig());
        // register services
        registerService(accountService);
        registerService(deathbanService);
        startServices();

        // declare managers
        playerManager = new PlayerManager(this);
        factionManager = new FactionManager(this);
        claimManager = new ClaimManager(this);
        timerManager = new TimerManager(this);
        serverStateManager = new ServerStateManager(this);
        statsManager = new StatsManager(this, configuration.getStatsConfig());

        factionManager.onEnable();
        playerManager.onEnable();
        claimManager.onEnable();
        timerManager.onEnable();
        serverStateManager.onEnable();
        statsManager.onEnable();

        // register listeners
        registerListener(new PlayerListener(this));
        registerListener(new TimerListener(this));
        registerListener(new ChatListener(this));
        registerListener(new CombatListener(this));
        registerListener(new DeathbanListener(this));
        registerListener(new StatsListener(this));
        registerListener(new StateListener(this));
        registerListener(new TrackedItemListener(this));
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // stop services
        stopServices();

        // disable and unregister managers
        claimManager.onDisable();
        playerManager.onDisable();
        factionManager.onDisable();
        timerManager.onDisable();
        serverStateManager.onDisable();
        statsManager.onDisable();

        playerManager = null;
        factionManager = null;
        claimManager = null;
        timerManager = null;
        statsManager = null;
    }
}
