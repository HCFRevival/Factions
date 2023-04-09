package gg.hcfactions.factions.models.faction.impl;

import gg.hcfactions.factions.models.faction.IFaction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.Locale;
import java.util.UUID;

public final class ServerFaction implements IFaction {
    @Override
    public UUID getUniqueId() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @AllArgsConstructor
    public enum Flag {
        SAFEZONE(ChatColor.GREEN + "Safezone"),
        EVENT(ChatColor.DARK_AQUA + "Event"),
        LANDMARK(ChatColor.LIGHT_PURPLE + "Landmark");

        @Getter public final String displayName;

        /**
         * Returns the Server Faction Flag by name
         * @param name Flag name
         * @return ServerFaction.Flag
         */
        public static Flag getFlagByName(String name) {
            for (Flag v : values()) {
                if (v.name().equalsIgnoreCase(name)) {
                    return v;
                }
            }

            return null;
        }
    }
}
