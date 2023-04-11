package gg.hcfactions.factions.models.stats;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;

@AllArgsConstructor
public enum EStatisticType {
    KILL("Kills", Material.NETHERITE_SWORD, 0),
    DEATH("Deaths", Material.SKELETON_SKULL, 1),
    EVENT_CAPTURES("Event Captures", Material.NAME_TAG, 8),
    LONGSHOT("Longest Bow Shot", Material.BOW, 3),
    PLAYTIME("Playtime", Material.PLAYER_HEAD, 7),
    EXP_EARNED("Experience Earned", Material.EXPERIENCE_BOTTLE, 5);

    @Getter public final String displayName;
    @Getter public final Material icon;
    @Getter public final int inventoryPosition;
}
