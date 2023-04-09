package gg.hcfactions.factions;

import gg.hcfactions.factions.cmd.FactionCommand;
import gg.hcfactions.factions.faction.FactionManager;
import gg.hcfactions.factions.player.PlayerManager;
import gg.hcfactions.libs.acf.PaperCommandManager;
import gg.hcfactions.libs.base.connect.impl.mongo.Mongo;
import gg.hcfactions.libs.bukkit.AresPlugin;
import gg.hcfactions.libs.bukkit.services.impl.account.AccountService;
import lombok.Getter;

public final class Factions extends AresPlugin {
    @Getter public FConfig configuration;
    @Getter public PlayerManager playerManager;
    @Getter public FactionManager factionManager;

    @Override
    public void onEnable() {
        super.onEnable();

        // config init
        configuration = new FConfig();
        configuration.loadConfig();

        // logger init
        registerLogger("Factions");

        // command init
        final PaperCommandManager cmdMng = new PaperCommandManager(this);
        cmdMng.enableUnstableAPI("help");
        registerCommandManager(cmdMng);
        registerCommand(new FactionCommand(this));

        // db init
        final Mongo mdb = new Mongo("mongodb://0.0.0.0:27017/", getAresLogger());
        mdb.openConnection();
        registerConnectable(mdb);

        // declare services
        final AccountService accountService = new AccountService(this);

        // register services
        registerService(accountService);
        startServices();

        // declare managers
        playerManager = new PlayerManager(this);
        factionManager = new FactionManager(this);

        factionManager.onEnable();
        playerManager.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        // stop services
        stopServices();

        // disable and unregister managers
        playerManager.onDisable();
        factionManager.onDisable();

        playerManager = null;
        factionManager = null;
    }
}
