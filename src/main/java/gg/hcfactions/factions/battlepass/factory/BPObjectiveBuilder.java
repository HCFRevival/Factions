package gg.hcfactions.factions.battlepass.factory;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.battlepass.EBPObjectiveType;
import gg.hcfactions.factions.models.battlepass.impl.BPObjective;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.libs.base.consumer.FailablePromise;
import gg.hcfactions.libs.bukkit.menu.impl.Icon;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;

public final class BPObjectiveBuilder {
    private final BPObjective pendingObjective;

    public BPObjectiveBuilder(Factions plugin, String id) {
        this.pendingObjective = new BPObjective(plugin, id);
    }

    public BPObjectiveBuilder setIcon(Icon icon) {
        pendingObjective.setIcon(icon);
        return this;
    }

    public BPObjectiveBuilder setObjectiveType(EBPObjectiveType type) {
        pendingObjective.setObjectiveType(type);
        return this;
    }

    public BPObjectiveBuilder setBlockRequirement(Material material) {
        pendingObjective.setBlockRequirement(material);
        return this;
    }

    public BPObjectiveBuilder setEntityRequirement(EntityType entityType) {
        pendingObjective.setEntityRequirement(entityType);
        return this;
    }

    public BPObjectiveBuilder setClaimRequirement(IFaction faction) {
        pendingObjective.setClaimRequirement(faction.getUniqueId());
        return this;
    }

    public BPObjectiveBuilder setWorldRequirement(World.Environment environment) {
        pendingObjective.setWorldRequirement(environment);
        return this;
    }

    public BPObjectiveBuilder setClassRequirement(IClass playerClass) {
        pendingObjective.setClassRequirement(playerClass);
        return this;
    }

    public BPObjectiveBuilder setAmountRequirement(int amount) {
        pendingObjective.setAmountRequirement(amount);
        return this;
    }

    public BPObjectiveBuilder setBaseExp(int amount) {
        pendingObjective.setBaseExp(amount);
        return this;
    }

    public void build(FailablePromise<BPObjective> promise) {
        if (pendingObjective.getIcon() == null) {
            promise.reject("Icon can not be null");
            return;
        }

        if (pendingObjective.getAmountRequirement() <= 0) {
            promise.reject("Amount can not be zero");
            return;
        }

        if (pendingObjective.getBaseExp() <= 0) {
            promise.reject("Base experience value can not be zero");
            return;
        }

        promise.resolve(pendingObjective);
    }
}
