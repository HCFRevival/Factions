package gg.hcfactions.factions.models.timer;

import gg.hcfactions.libs.bukkit.utils.Colors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@AllArgsConstructor
public enum ETimerType {
    ENDERPEARL(
            Colors.PURPLE.toBukkit() + "" + ChatColor.BOLD + "Enderpearl",
            Colors.PURPLE.toBukkit() + "Enderpearl",
            1,
            true,
            true,
            true
    ),
    HOME(
            Colors.AQUA.toBukkit() + "" + ChatColor.BOLD + "Home",
            Colors.AQUA.toBukkit() + "Home",
            10,
            true,
            true,
            false
    ),
    STUCK(Colors.BLUE.toBukkit() + "" + ChatColor.BOLD + "Stuck",
            Colors.BLUE.toBukkit() + "Stuck",
            9,
            true,
            true,
            false
    ),
    CRAPPLE(Colors.GOLD.toBukkit() + "" + ChatColor.BOLD + "Crapple",
            Colors.GOLD.toBukkit() + "Crapple",
            8,
            true,
            true,
            false
    ),
    GAPPLE(Colors.GOLD.toBukkit() + "" + ChatColor.BOLD + "Gapple",
            Colors.GOLD.toBukkit() + "Gapple",
            7,
            true,
            false,
            true),
    LOGOUT(Colors.BLUE.toBukkit() + "" + ChatColor.BOLD + "Logout",
            Colors.BLUE.toBukkit() + "Logout",
            6,
            true,
            true,
            false
    ),
    COMBAT(Colors.RED.toBukkit() + "" + ChatColor.BOLD + "Combat Tag",
            Colors.RED.toBukkit() + "Combat Tag",
            2,
            true,
            true,
            true
    ),
    PROTECTION(Colors.GREEN.toBukkit() + "" + ChatColor.BOLD + "Protection",
            Colors.GREEN.toBukkit() + "Protection",
            3,
            true,
            false,
            true
    ),
    CLASS(Colors.DARK_AQUA.toBukkit() + "" + ChatColor.BOLD + "Class",
            Colors.DARK_AQUA.toBukkit() + "Class",
            4,
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
