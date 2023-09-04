package gg.hcfactions.factions.battlepass;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.battlepass.impl.BPObjective;
import lombok.Getter;

import java.util.List;

public class BattlepassManager {
    @Getter public final Factions plugin;
    @Getter public final List<BPObjective> objectiveRepository;

    public BattlepassManager(Factions plugin) {
        this.plugin = plugin;
        this.objectiveRepository = Lists.newArrayList();
    }


}
