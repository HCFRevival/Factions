package gg.hcfactions.factions.models.events.impl.types;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.events.EPalaceLootTier;
import gg.hcfactions.factions.models.events.ILootableEvent;
import gg.hcfactions.factions.models.events.impl.CaptureEventConfig;
import gg.hcfactions.factions.models.events.impl.CaptureRegion;
import gg.hcfactions.factions.models.events.impl.EventSchedule;
import gg.hcfactions.factions.models.events.impl.loot.PalaceLootChest;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PalaceEvent extends KOTHEvent implements ILootableEvent {
    @Getter @Setter public UUID capturingFaction;
    @Getter @Setter public long nextRestockTime;
    @Getter @Setter public int restockInterval;
    @Getter @Setter public Map<EPalaceLootTier, Long> lootUnlockTimes;
    @Getter @Setter public List<PalaceLootChest> lootChests;

    public PalaceEvent(
            Factions plugin,
            UUID owner,
            String name,
            String displayName,
            List<EventSchedule> schedule,
            CaptureRegion captureRegion,
            UUID capturingFaction,
            int restockInterval,
            Map<EPalaceLootTier, Long> lootTierUnlockTimes,
            List<PalaceLootChest> lootChests,
            CaptureEventConfig defaultConfig
    ) {
        super(plugin, owner, name, displayName, schedule, captureRegion, defaultConfig);

        if (capturingFaction != null && plugin.getFactionManager().getFactionById(capturingFaction) != null) {
            this.capturingFaction = capturingFaction;
        }

        this.restockInterval = restockInterval;
        this.lootUnlockTimes = lootTierUnlockTimes;
        this.lootChests = lootChests;
        this.nextRestockTime = Time.now() + (restockInterval*1000L);
    }

    @Override
    public void captureEvent(PlayerFaction faction) {
        super.captureEvent(faction);

        capturingFaction = faction.getUniqueId();

        new Scheduler(plugin).async(() -> {
            final LocalDate date = LocalDate.now();
            final LocalDate monday = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
            final LocalDate wednesday = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY));
            final LocalDate friday = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));

            lootUnlockTimes.put(EPalaceLootTier.T3, monday.atStartOfDay().toEpochSecond(ZoneOffset.UTC));
            lootUnlockTimes.put(EPalaceLootTier.T2, wednesday.atStartOfDay().toEpochSecond(ZoneOffset.UTC));
            lootUnlockTimes.put(EPalaceLootTier.T1, friday.atStartOfDay().toEpochSecond(ZoneOffset.UTC));

            plugin.getEventManager().saveEvent(this);
        }).run();
    }

    @Override
    public void restock() {
        restock(true);
    }

    @Override
    public void restock(boolean broadcast) {
        if (lootChests.isEmpty()) {
            return;
        }

        lootChests.forEach(PalaceLootChest::restock);
        nextRestockTime = Time.now() + (restockInterval*1000L);

        if (broadcast) {
            Bukkit.broadcastMessage(" ");
            Bukkit.broadcastMessage(FMessage.PALACE_PREFIX + displayName + FMessage.LAYER_1 + " has been restocked");
            Bukkit.broadcastMessage(" ");
        }
    }
}
