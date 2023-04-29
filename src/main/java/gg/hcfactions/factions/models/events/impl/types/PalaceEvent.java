package gg.hcfactions.factions.models.events.impl.types;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.events.ILootableEvent;
import gg.hcfactions.factions.models.events.impl.CaptureEventConfig;
import gg.hcfactions.factions.models.events.impl.CaptureRegion;
import gg.hcfactions.factions.models.events.impl.EventSchedule;
import gg.hcfactions.factions.models.events.impl.loot.PalaceLootChest;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

public final class PalaceEvent extends KOTHEvent implements ILootableEvent {
    @Getter @Setter public UUID capturingFaction;
    @Getter @Setter public long nextRestockTime;
    @Getter @Setter public int restockInterval;
    @Getter @Setter public List<PalaceLootChest> lootChests;

    public PalaceEvent(
            Factions plugin,
            UUID owner,
            String name,
            String displayName,
            List<EventSchedule> schedule,
            BLocatable captureChestLocation,
            CaptureRegion captureRegion,
            int restockInterval,
            List<PalaceLootChest> lootChests,
            CaptureEventConfig defaultConfig
    ) {
        super(plugin, owner, name, displayName, schedule, captureChestLocation, captureRegion, defaultConfig);
        this.restockInterval = restockInterval;
        this.lootChests = lootChests;
        this.nextRestockTime = Time.now() + (restockInterval*1000L);
    }

    @Override
    public void captureEvent(PlayerFaction faction) {
        super.captureEvent(faction);

        capturingFaction = faction.getUniqueId();
        new Scheduler(plugin).async(() -> plugin.getEventManager().saveEvent(this)).run();
    }

    @Override
    public void restock() {
        if (lootChests.isEmpty()) {
            return;
        }

        lootChests.forEach(PalaceLootChest::restock);
    }
}
