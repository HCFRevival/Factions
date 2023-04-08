package gg.hcfactions.factions;

import gg.hcfactions.libs.base.connect.impl.mongo.Mongo;
import gg.hcfactions.libs.bukkit.AresPlugin;
import gg.hcfactions.libs.bukkit.services.impl.account.AccountService;

public final class Factions extends AresPlugin {
    @Override
    public void onEnable() {
        super.onEnable();

        registerLogger("Factions");

        // db init
        final Mongo mdb = new Mongo("mongodb://0.0.0.0:27017/", getAresLogger());
        mdb.openConnection();
        registerConnectable(mdb);

        // declare services
        final AccountService accountService = new AccountService(this);

        // register services
        registerService(accountService);
        startServices();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        stopServices();
    }
}
