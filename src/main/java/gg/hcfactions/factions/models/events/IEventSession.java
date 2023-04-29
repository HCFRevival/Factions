package gg.hcfactions.factions.models.events;

public interface IEventSession {
    /**
     * @return If true this event is actively running
     */
    boolean isActive();

    /**
     * @param b Set active state for this event
     */
    void setActive(boolean b);
}
