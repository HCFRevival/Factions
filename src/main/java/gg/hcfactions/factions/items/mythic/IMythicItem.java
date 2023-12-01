package gg.hcfactions.factions.items.mythic;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.utils.StringUtil;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import gg.hcfactions.libs.bukkit.utils.Colors;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

public interface IMythicItem extends ICustomItem {
    List<MythicAbility> getAbilityInfo();

    default List<String> getMythicLore() {
        final List<String> res = Lists.newArrayList();
        res.add(Colors.DARK_AQUA.toBukkit() + "\uD83D\uDDE1 Mythic");
        res.add(ChatColor.RESET + " ");
        res.add(ChatColor.GRAY + "This is a " + Colors.DARK_AQUA.toBukkit() + "Mythic Item" + ChatColor.GRAY + ".");
        res.add(ChatColor.GRAY + "It can not be repaired");
        res.add(ChatColor.GRAY + "or enchanted.");

        for (EMythicAbilityType abilityType : EMythicAbilityType.values()) {
            final List<MythicAbility> abilities = getAbilityInfoByType(abilityType);

            if (abilities.isEmpty()) {
                continue;
            }

            res.add(ChatColor.RESET + " ");
            res.add(abilityType.getDisplayName());
            abilities.forEach(ability -> StringUtil.formatLore(res, ability.toString(), ChatColor.GRAY));
        }

        return res;
    }

    default List<MythicAbility> getAbilityInfoByType(EMythicAbilityType type) {
        return getAbilityInfo().stream().filter(ability -> ability.getAbilityType().equals(type)).collect(Collectors.toList());
    }

    default void addAbilityInfo(String name, String description, EMythicAbilityType type) {
        getAbilityInfo().add(new MythicAbility(name, description, type));
    }

    default void onKill(Player player, LivingEntity slainEntity) {}
    default void onAttack(Player player, LivingEntity attackedEntity) {}
}
