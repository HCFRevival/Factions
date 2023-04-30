package gg.hcfactions.factions.models.events;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.models.events.impl.EventSchedule;
import gg.hcfactions.libs.base.consumer.Promise;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import org.bukkit.Bukkit;

import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Future;

public interface IScheduledEvent {
    List<EventSchedule> getSchedule();

    default boolean shouldStart() {
        if (getSchedule().isEmpty()) {
            return false;
        }

        final Calendar calendar = Calendar.getInstance();
        final int day = calendar.get(Calendar.DAY_OF_WEEK);
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int min = calendar.get(Calendar.MINUTE);

        return getSchedule().stream().anyMatch(s -> s.getDay() == day && s.getHour() == hour && s.getMinute() == min);
    }

    default long getRemainingTimeUntilNextSchedule() {
        if (getSchedule().isEmpty()) {
            return -1L;
        }

        final List<Long> times = Lists.newArrayList();
        getSchedule().forEach(s -> times.add(Time.getTimeUntil(s.getDay(), s.getHour(), s.getMinute())));

        if (times.isEmpty()) {
            return -1L;
        }

        times.sort(Comparator.naturalOrder());
        return times.get(0);
    }
}
