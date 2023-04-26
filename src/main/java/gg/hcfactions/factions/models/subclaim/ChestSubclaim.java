package gg.hcfactions.factions.models.subclaim;

import com.google.common.collect.Sets;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.Set;

public final class ChestSubclaim {
    @Getter public final Block chest;
    @Getter public final Set<String> members;

    public ChestSubclaim(Block chest, Sign sign) {
        this.chest = chest;
        this.members = Sets.newHashSet();

        for (int i = 1; i <= 3; i++) {
            final String username = sign.getLine(i);

            if (username.length() > 0) {
                members.add(username);
            }
        }
    }

    public boolean canAccess(Player player) {
        return members.stream().anyMatch(m -> m.equalsIgnoreCase(player.getName()));
    }
}
