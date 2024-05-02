package gg.hcfactions.factions.bosses;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.manager.IManager;
import lombok.Getter;

@Getter
public final class BossManager implements IManager {
    public final Factions plugin;
    public final BossLootManager lootManager;

    public BossManager(Factions plugin) {
        this.plugin = plugin;
        this.lootManager = new BossLootManager(this);
    }

    @Override
    public void onEnable() {
        lootManager.onEnable();
    }

    @Override
    public void onDisable() {
        lootManager.onDisable();
    }
}
