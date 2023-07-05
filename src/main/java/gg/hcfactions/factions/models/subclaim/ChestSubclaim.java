package gg.hcfactions.factions.models.subclaim;

import com.google.common.collect.Sets;
import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

public final class ChestSubclaim {
    @Getter public final List<Block> chests;
    @Getter public final Set<String> members;

    public ChestSubclaim(List<Block> chests, Sign sign) {
        this.chests = chests;
        this.members = Sets.newHashSet();

        for (int i = 1; i <= 3; i++) {
            final String username = sign.getLine(i);

            if (username.length() > 0) {
                members.add(username);
            }
        }
    }

    public boolean isBlock(Block block) {
        return chests.stream().anyMatch(chest -> chest.getLocation().equals(block.getLocation()));
    }

    public boolean canAccess(Player player) {
        return members.stream().anyMatch(m -> m.equalsIgnoreCase(player.getName()));
    }
}
