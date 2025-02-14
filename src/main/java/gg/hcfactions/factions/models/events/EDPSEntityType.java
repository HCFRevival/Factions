package gg.hcfactions.factions.models.events;

public enum EDPSEntityType {
    ZOMBIE,
    RAVAGER;

    public static EDPSEntityType getByName(String name) {
        for (EDPSEntityType entry : values()) {
            if (entry.name().equalsIgnoreCase(name)) {
                return entry;
            }
        }

        return null;
    }
}
