package gg.hcfactions.factions.events;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.events.impl.CaptureEventConfig;
import gg.hcfactions.factions.models.events.impl.CaptureRegion;
import gg.hcfactions.factions.models.events.impl.EventSchedule;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public class EventTest {
    @Test
    public void testScheduler() {
        final List<EventSchedule> schedule = Lists.newArrayList();
        final ZoneId tz = ZoneId.of("America/New_York");
        final LocalDateTime now = LocalDateTime.now(tz);
        final int day = now.getDayOfWeek().getValue();
        final int hour = now.getHour();
        final int min = now.getMinute();

        schedule.add(new EventSchedule(hour, min, day));

        final KOTHEvent event = new KOTHEvent(
                Factions.getInstance(),
                UUID.randomUUID(),
                "TestEvent",
                "Test Event",
                schedule,
                new CaptureRegion(new BLocatable("", 0, 0, 0), new BLocatable("", 0, 0, 0)),
                new CaptureEventConfig(0, 0, 0, 0, 0, 0));

        Assertions.assertTrue(event.shouldStart());
    }
}
