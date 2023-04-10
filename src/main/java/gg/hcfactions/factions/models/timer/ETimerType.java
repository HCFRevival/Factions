package gg.hcfactions.factions.models.timer;

import gg.hcfactions.libs.bukkit.utils.Colors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

@AllArgsConstructor
public enum ETimerType {
    ENDERPEARL(Colors.PURPLE.toBukkit() + "" + ChatColor.BOLD + "Enderpearl", true, true),
    HOME(Colors.AQUA.toBukkit() + "" + ChatColor.BOLD + "Home", true, true),
    STUCK(Colors.BLUE.toBukkit() + "" + ChatColor.BOLD + "Stuck", true, true),
    CRAPPLE(Colors.GOLD.toBukkit() + "" + ChatColor.BOLD + "Crapple", true, true),
    GAPPLE(Colors.GOLD.toBukkit() + "" + ChatColor.BOLD + "Gapple", true, false),
    LOGOUT(Colors.BLUE.toBukkit() + "" + ChatColor.BOLD + "Logout", true, true),
    COMBAT(Colors.RED.toBukkit() + "" + ChatColor.BOLD + "Combat Tag", true, true),
    PROTECTION(Colors.GREEN.toBukkit() + "" + ChatColor.BOLD + "Protection", true, false),
    CLASS(Colors.DARK_AQUA.toBukkit() + "" + ChatColor.BOLD + "Class", true, true),
    TOTEM(Colors.GOLD.toBukkit() + "" + ChatColor.BOLD + "Totem", true, false),
    FREEZE(Colors.AQUA.toBukkit() + "" + ChatColor.BOLD + "Freeze", false, false),
    RALLY(Colors.BLUE.toBukkit() + "" + ChatColor.BOLD + "Rally", false, false);

    @Getter public final String displayName;
    @Getter public final boolean render;
    @Getter public final boolean decimal;

    public static ETimerType fromString(String name) {
        for (ETimerType v : values()) {
            if (v.name().equalsIgnoreCase(name)) {
                return v;
            }
        }

        return null;
    }
}
