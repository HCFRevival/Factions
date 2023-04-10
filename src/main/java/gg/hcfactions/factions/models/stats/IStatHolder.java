package gg.hcfactions.factions.models.stats;

import java.util.Map;
import java.util.UUID;

public interface IStatHolder {
    /**
     * @return UUID
     */
    UUID getUniqueId();

    /**
     * @return Name
     */
    String getName();

    /**
     * Server map number
     * @return
     */
    double getMapNumber();

    /**
     * @return Statistic Values
     */
    Map<EStatisticType, Long> getValues();

    /**
     * Set the username for this holder
     * @param s Name
     */
    void setName(String s);

    default long getStatistic(EStatisticType type) {
        return getValues().getOrDefault(type, 0L);
    }

    default void setStatistic(EStatisticType type, long amount) {
        getValues().put(type, amount);
    }

    default void addToStatistic(EStatisticType type, long amount) {
        final long current = getStatistic(type);
        getValues().put(type, (current + amount));
    }
}
