package gg.hcfactions.factions.models.boss;

import org.bukkit.entity.EntityType;

public interface IBossEntity {
    EntityType getBukkitType();
    void registerGoals();
    void spawn();
    void despawn();
}
