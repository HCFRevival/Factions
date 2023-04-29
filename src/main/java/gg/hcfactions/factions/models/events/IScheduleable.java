package gg.hcfactions.factions.models.events;

public interface IScheduleable {
    /**
     * @return Calendar day
     */
    int getDay();

    /**
     * @return Calendar hour
     */
    int getHour();

    /**
     * @return Calendar minute
     */
    int getMinute();
}
