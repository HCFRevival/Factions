package gg.hcfactions.factions.models.timer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@AllArgsConstructor
public enum ETimerType {
    GRAPPLE(ChatColor.GOLD + "" + ChatColor.BOLD + "Grapple",
            ChatColor.GOLD + "Grapple",
            8,
            true,
            true,
            false
    ),
    GUARD(
            ChatColor.GOLD + "" + ChatColor.BOLD + "Guard",
            ChatColor.GOLD + "Guard",
            8,
            true,
            true,
            false
    ),
    CHORUS_FRUIT(
            ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Chorus Fruit",
            ChatColor.LIGHT_PURPLE + "Chorus Fruit",
            13,
            true,
            true,
            false
    ),
    TRIDENT(
      ChatColor.AQUA + "" + ChatColor.BOLD + "Riptide",
      ChatColor.AQUA + "Riptide",
      8,
      true,
      true,
      false
    ),
    ENDERPEARL(
            ChatColor.YELLOW + "" + ChatColor.BOLD + "Enderpearl",
            ChatColor.YELLOW + "Enderpearl",
            14,
            true,
            true,
            true
    ),
    HOME(
            ChatColor.AQUA + "" + ChatColor.BOLD + "Home",
            ChatColor.AQUA + "Home",
            11,
            true,
            true,
            false
    ),
    STUCK(ChatColor.BLUE + "" + ChatColor.BOLD + "Stuck",
            ChatColor.BLUE + "Stuck",
            10,
            true,
            true,
            false
    ),
    CRAPPLE(ChatColor.GOLD + "" + ChatColor.BOLD + "Crapple",
            ChatColor.GOLD + "Crapple",
            6,
            true,
            true,
            false
    ),
    GAPPLE(ChatColor.GOLD + "" + ChatColor.BOLD + "Gapple",
            ChatColor.GOLD + "Gapple",
            4,
            true,
            false,
            true),
    LOGOUT(ChatColor.BLUE+ "" + ChatColor.BOLD + "Logout",
            ChatColor.BLUE + "Logout",
            12,
            true,
            true,
            false
    ),
    COMBAT(ChatColor.RED + "" + ChatColor.BOLD + "Combat Tag",
            ChatColor.RED + "Combat Tag",
            16,
            true,
            true,
            true
    ),
    PROTECTION(ChatColor.GREEN + "" + ChatColor.BOLD + "Protection",
            ChatColor.GREEN + "Protection",
            17,
            true,
            false,
            true
    ),
    CLASS(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Class",
            ChatColor.DARK_AQUA + "Class",
            15,
            true,
            true,
            false
    ),
    TOTEM(ChatColor.GOLD + "" + ChatColor.BOLD + "Totem",
            ChatColor.GOLD + "Totem",
            5,
            true,
            false,
            true
    ),
    ARCHER_MARK(ChatColor.GOLD + "" + ChatColor.BOLD + "Mark",
            ChatColor.GOLD + "Mark",
            7,
            true,
            true,
            false
    ),
    FREEZE(ChatColor.AQUA + "" + ChatColor.BOLD + "Freeze",
            null,
            0,
            false,
            false,
            true
    ),
    RALLY(ChatColor.BLUE + "" + ChatColor.BOLD + "Rally",
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
