package gg.hcfactions.factions.models.stats;

public interface ITrackable {
    /**
     * @return Map number this tracked event occurred on
     */
    double getMapNumber();

    /**
     * @return Time in millis this event occurred at
     */
    long getDate();
}
