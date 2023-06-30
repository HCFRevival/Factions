package gg.hcfactions.factions.models.events;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.models.events.impl.EventSchedule;
import gg.hcfactions.libs.base.util.Time;

import java.util.*;

public interface IScheduledEvent {
    List<EventSchedule> getSchedule();

    default Optional<EventSchedule> getScheduleAt(int dayOfWeek, int hourOfDay, int minuteOfHour) {
        return getSchedule().stream().filter(s -> s.getDay() == dayOfWeek && s.getHour() == hourOfDay && s.getMinute() == minuteOfHour).findFirst();
    }

    default boolean shouldStart() {
        if (getSchedule().isEmpty()) {
            return false;
        }

        final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
        final int day = calendar.get(Calendar.DAY_OF_WEEK);
        final int hour = calendar.get(Calendar.HOUR_OF_DAY) + 1; // incr 1 here because it's odd to schedule everything with 0 index
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
