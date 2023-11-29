package gg.hcfactions.factions.items.mythic;

import com.google.common.collect.Lists;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import gg.hcfactions.libs.bukkit.utils.Colors;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

public interface IMythicItem extends ICustomItem {
    default List<String> getMythicLore() {
        final List<String> res = Lists.newArrayList();
        res.add(Colors.DARK_AQUA.toBukkit() + "\uD83D\uDDE1 Mythic");
        res.add(ChatColor.RESET + " ");
        res.add(ChatColor.GRAY + "This is a " + Colors.DARK_AQUA.toBukkit() + "Mythic Item" + ChatColor.GRAY + ".");
        res.add(ChatColor.GRAY + "It can not be repaired");
        res.add(ChatColor.GRAY + "or enchanted.");
        return res;
    }

    default void onKill(Player player, LivingEntity slainEntity) {}
    default void onAttack(Player player, LivingEntity attackedEntity) {}
}
