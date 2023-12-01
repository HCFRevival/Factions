package gg.hcfactions.factions.items.mythic;

import lombok.Getter;
import org.bukkit.ChatColor;

public record MythicAbility(@Getter String name,
                            @Getter String description,
                            @Getter EMythicAbilityType abilityType) {

    public String toString() {
        return name + ChatColor.RESET + ": " + description;
    }
}
