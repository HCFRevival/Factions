package gg.hcfactions.factions.models.battlepass.impl;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.battlepass.EBPObjectiveType;
import gg.hcfactions.factions.models.battlepass.IBPObjective;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.libs.bukkit.menu.impl.Icon;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

import java.util.UUID;

public class BPObjective implements IBPObjective {
    @Getter public final Factions plugin;
    @Getter public final String identifier;
    @Getter @Setter public boolean active;
    @Getter @Setter public Icon icon;
    @Getter @Setter public EBPObjectiveType objectiveType;
    @Getter @Setter public Material blockRequirement;
    @Getter @Setter public EntityType entityRequirement;
    @Getter @Setter public UUID claimRequirement;
    @Getter @Setter public World.Environment worldRequirement;
    @Getter @Setter public IClass classRequirement;
    @Getter @Setter public int amountRequirement;
    @Getter @Setter public int baseExp;

    public BPObjective(Factions plugin, String id) {
        this.plugin = plugin;
        this.identifier = id;
    }
}
