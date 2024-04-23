package gg.hcfactions.factions.models.timer;

import gg.hcfactions.libs.bukkit.utils.Colors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@AllArgsConstructor
public enum ETimerType {
    GRAPPLE(Colors.GOLD.toBukkit() + "" + ChatColor.BOLD + "Grapple",
            Colors.GOLD.toBukkit() + "Grapple",
            8,
            true,
            true,
            false
    ),
    GUARD(
            Colors.GOLD.toBukkit() + "" + ChatColor.BOLD + "Guard",
            Colors.GOLD.toBukkit() + "Guard",
            8,
            true,
            true,
            false
    ),
    CHORUS_FRUIT(
            Colors.LAVENDAR.toBukkit() + "" + ChatColor.BOLD + "Chorus Fruit",
            Colors.LAVENDAR.toBukkit() + "Chorus Fruit",
            13,
            true,
            true,
            false
    ),
    TRIDENT(
      Colors.AQUA.toBukkit() + "" + ChatColor.BOLD + "Riptide",
      Colors.AQUA.toBukkit() + "Riptide",
      8,
      true,
      true,
      false
    ),
    ENDERPEARL(
            Colors.DARK_AQUA.toBukkit() + "" + ChatColor.BOLD + "Enderpearl",
            Colors.DARK_AQUA.toBukkit() + "Enderpearl",
            14,
            true,
            true,
            true
    ),
    HOME(
            Colors.AQUA.toBukkit() + "" + ChatColor.BOLD + "Home",
            Colors.AQUA.toBukkit() + "Home",
            11,
            true,
            true,
            false
    ),
    STUCK(Colors.BLUE.toBukkit() + "" + ChatColor.BOLD + "Stuck",
            Colors.BLUE.toBukkit() + "Stuck",
            10,
            true,
            true,
            false
    ),
    CRAPPLE(Colors.GOLD.toBukkit() + "" + ChatColor.BOLD + "Crapple",
            Colors.GOLD.toBukkit() + "Crapple",
            6,
            true,
            true,
            false
    ),
    GAPPLE(Colors.GOLD.toBukkit() + "" + ChatColor.BOLD + "Gapple",
            Colors.GOLD.toBukkit() + "Gapple",
            4,
            true,
            false,
            true),
    LOGOUT(Colors.BLUE.toBukkit() + "" + ChatColor.BOLD + "Logout",
            Colors.BLUE.toBukkit() + "Logout",
            12,
            true,
            true,
            false
    ),
    COMBAT(Colors.RED.toBukkit() + "" + ChatColor.BOLD + "Combat Tag",
            Colors.RED.toBukkit() + "Combat Tag",
            16,
            true,
            true,
            true
    ),
    PROTECTION(Colors.GREEN.toBukkit() + "" + ChatColor.BOLD + "Protection",
            Colors.GREEN.toBukkit() + "Protection",
            17,
            true,
            false,
            true
    ),
    CLASS(Colors.DARK_AQUA.toBukkit() + "" + ChatColor.BOLD + "Class",
            Colors.DARK_AQUA.toBukkit() + "Class",
            15,
            true,
            true,
            false
    ),
    TOTEM(Colors.GOLD.toBukkit() + "" + ChatColor.BOLD + "Totem",
            Colors.GOLD.toBukkit() + "Totem",
            5,
            true,
            false,
            true
    ),
    ARCHER_MARK(Colors.GOLD.toBukkit() + "" + ChatColor.BOLD + "Mark",
            Colors.GOLD.toBukkit() + "Mark",
            7,
            true,
            true,
            false
    ),
    FREEZE(Colors.AQUA.toBukkit() + "" + ChatColor.BOLD + "Freeze",
            null,
            0,
            false,
            false,
            true
    ),
    RALLY(Colors.BLUE.toBukkit() + "" + ChatColor.BOLD + "Rally",
            null,
            0,
            false,
            false,
            false
    ),
    RALLY_WAYPOINT("Rally Waypoint",
            null,
            0,
            false,
            false,
            false),
    FOCUS("Focus",
            null,
            0,
            false,
            false,
            false
    );

    @Getter public final String displayName;
    @Getter public final String scoreboardName;
    @Getter public final int scoreboardPosition;
    @Getter public final boolean render;
    @Getter public final boolean decimal;
    @Getter public final boolean persistent;

    public static ETimerType fromString(String name) {
        for (ETimerType v : values()) {
            if (v.name().equalsIgnoreCase(name)) {
                return v;
            }
        }

        return null;
    }
}
