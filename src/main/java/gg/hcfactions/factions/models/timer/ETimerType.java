package gg.hcfactions.factions.models.timer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

@Getter
@AllArgsConstructor
public enum ETimerType {
    GRAPPLE(Component.text("Grapple").color(NamedTextColor.GOLD),
            Component.text("Grapple").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
            8,
            true,
            true,
            false
    ),
    GUARD(
            Component.text("Guard").color(NamedTextColor.DARK_GREEN),
            Component.text("Guard").color(NamedTextColor.DARK_GREEN).decorate(TextDecoration.BOLD),
            8,
            true,
            true,
            false
    ),
    CHORUS_FRUIT(
            Component.text("Chorus Fruit").color(TextColor.color(0xef85ff)),
            Component.text("Chorus Fruit").color(TextColor.color(0xef85ff)).decorate(TextDecoration.BOLD),
            13,
            true,
            true,
            false
    ),
    TRIDENT(
            Component.text("Trident").color(NamedTextColor.DARK_AQUA),
            Component.text("Trident").color(NamedTextColor.DARK_AQUA).decorate(TextDecoration.BOLD),
      8,
      true,
      true,
      false
    ),
    ENDERPEARL(
            Component.text("Enderpearl").color(NamedTextColor.YELLOW),
            Component.text("Enderpearl").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD),
            14,
            true,
            true,
            true
    ),
    WIND_CHARGE(
            Component.text("Wind Charge").color(NamedTextColor.AQUA),
            Component.text("Wind Charge").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD),
            15,
            true,
            true,
            false
    ),
    HOME(
            Component.text("Home").color(NamedTextColor.BLUE),
            Component.text("Home").color(NamedTextColor.BLUE).decorate(TextDecoration.BOLD),
            11,
            true,
            true,
            false
    ),
    STUCK(
            Component.text("Stuck").color(NamedTextColor.DARK_AQUA),
            Component.text("Stuck").color(NamedTextColor.DARK_AQUA).decorate(TextDecoration.BOLD),
            10,
            true,
            true,
            false
    ),
    CRAPPLE(
            Component.text("Crapple").color(NamedTextColor.GOLD),
            Component.text("Crapple").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
            6,
            true,
            true,
            false
    ),
    GAPPLE(
            Component.text("Gapple").color(NamedTextColor.GOLD),
            Component.text("Gapple").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
            4,
            true,
            false,
            true),
    LOGOUT(
            Component.text("Logout").color(NamedTextColor.BLUE),
            Component.text("Logout").color(NamedTextColor.BLUE).decorate(TextDecoration.BOLD),
            12,
            true,
            true,
            false
    ),
    COMBAT(Component.text("Combat Tag").color(NamedTextColor.RED),
            Component.text("Combat Tag").color(NamedTextColor.RED).decorate(TextDecoration.BOLD),
            17,
            true,
            true,
            true
    ),
    PROTECTION(
            Component.text("Protection").color(NamedTextColor.GREEN),
            Component.text("Protection").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD),
            18,
            true,
            false,
            true
    ),
    CLASS(
            Component.text("Class").color(NamedTextColor.BLUE),
            Component.text("Class").color(NamedTextColor.BLUE).decorate(TextDecoration.BOLD),
            16,
            true,
            true,
            false
    ),
    TOTEM(
            Component.text("Totem").color(NamedTextColor.GOLD),
            Component.text("Totem").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
            5,
            true,
            false,
            true
    ),
    ARCHER_MARK(
            Component.text("Mark").color(NamedTextColor.DARK_RED),
            Component.text("Mark").color(NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD),
            7,
            true,
            true,
            false
    ),
    FREEZE(null,
            null,
            0,
            false,
            false,
            true
    ),
    RALLY(null,
            null,
            0,
            false,
            false,
            false
    ),
    RALLY_WAYPOINT(null,
            null,
            0,
            false,
            false,
            false),
    FOCUS(null,
            null,
            0,
            false,
            false,
            false
    );

    public final Component displayName;
    public final Component scoreboardName;
    public final int scoreboardPosition;
    public final boolean render;
    public final boolean decimal;
    public final boolean persistent;

    public String getLegacyDisplayName() {
        return LegacyComponentSerializer.legacySection().serialize(displayName);
    }

    public String getLegacyScoreboardName() {
        return LegacyComponentSerializer.legacySection().serialize(scoreboardName);
    }

    public static ETimerType fromString(String name) {
        for (ETimerType v : values()) {
            if (v.name().equalsIgnoreCase(name)) {
                return v;
            }
        }

        return null;
    }
}
