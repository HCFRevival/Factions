package gg.hcfactions.factions.classes;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.classes.EConsumableApplicationType;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.classes.IConsumeable;
import gg.hcfactions.factions.models.classes.impl.*;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ClassManager implements IManager {
    @Getter public final Factions plugin;
    @Getter public final List<IClass> classes;
    @Getter @Setter public BukkitTask passiveUpdateTask;

    public ClassManager(Factions plugin) {
        this.plugin = plugin;
        this.classes = Lists.newArrayList();
    }

    @Override
    public void onEnable() {
        final Archer archer = new Archer(this);
        final Rogue rogue = new Rogue(this);
        final Bard bard = new Bard(this);
        final Diver diver = new Diver(this);
        final Miner miner = new Miner(this);

        archer.getPassiveEffects().put(PotionEffectType.SPEED, 3);
        archer.getPassiveEffects().put(PotionEffectType.JUMP, 2);

        classes.add(archer);
        classes.add(rogue);
        classes.add(bard);
        classes.add(diver);
        classes.add(miner);
    }

    @Override
    public void onDisable() {
        classes.clear();
    }

    public IClass getCurrentClass(Player player) {
        return classes.stream().filter(c -> c.getActivePlayers().contains(player.getUniqueId())).findFirst().orElse(null);
    }

    public IClass getClassByArmor(Player player) {
        return classes.stream().filter(c -> c.hasArmorRequirements(player)).findFirst().orElse(null);
    }

    public IClass getClassByName(String className) {
        return classes.stream().filter(c -> c.getName().equalsIgnoreCase(className)).findFirst().orElse(null);
    }

    public int getFactionClassCount(PlayerFaction faction, IClass playerClass) {
        int count = 0;

        for (PlayerFaction.Member member : faction.getOnlineMembers()) {
            final IClass currentClass = getCurrentClass(member.getBukkit());

            if (currentClass != null && currentClass == playerClass) {
                count += 1;
            }
        }

        return count;
    }
}
