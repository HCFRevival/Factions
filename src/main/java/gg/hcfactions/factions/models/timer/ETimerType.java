package gg.hcfactions.factions.models.timer;

import gg.hcfactions.libs.bukkit.utils.Colors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@AllArgsConstructor
public enum ETimerType {
    ENDERPEARL(Colors.PURPLE.toBukkit() + "" + ChatColor.BOLD + "Enderpearl", true, true, true),
    HOME(Colors.AQUA.toBukkit() + "" + ChatColor.BOLD + "Home", true, true, false),
    STUCK(Colors.BLUE.toBukkit() + "" + ChatColor.BOLD + "Stuck", true, true, false),
    CRAPPLE(Colors.GOLD.toBukkit() + "" + ChatColor.BOLD + "Crapple", true, true, false),
    GAPPLE(Colors.GOLD.toBukkit() + "" + ChatColor.BOLD + "Gapple", true, false, true),
    LOGOUT(Colors.BLUE.toBukkit() + "" + ChatColor.BOLD + "Logout", true, true, false),
    COMBAT(Colors.RED.toBukkit() + "" + ChatColor.BOLD + "Combat Tag", true, true, true),
    PROTECTION(Colors.GREEN.toBukkit() + "" + ChatColor.BOLD + "Protection", true, false, true),
    CLASS(Colors.DARK_AQUA.toBukkit() + "" + ChatColor.BOLD + "Class", true, true, false),
    TOTEM(Colors.GOLD.toBukkit() + "" + ChatColor.BOLD + "Totem", true, false, true),
    FREEZE(Colors.AQUA.toBukkit() + "" + ChatColor.BOLD + "Freeze", false, false, true),
    RALLY(Colors.BLUE.toBukkit() + "" + ChatColor.BOLD + "Rally", false, false, false);

    @Getter public final String displayName;
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
