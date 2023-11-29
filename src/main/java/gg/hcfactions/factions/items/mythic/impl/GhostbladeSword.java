package gg.hcfactions.factions.items.mythic.impl;

import com.google.common.collect.Maps;
import gg.hcfactions.cx.CXService;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.mythic.IMythicItem;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.utils.FactionUtil;
import gg.hcfactions.libs.bukkit.utils.Players;
import gg.hcfactions.libs.bukkit.utils.Worlds;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.List;
import java.util.Map;

public final class GhostbladeSword implements IMythicItem {
    @Getter public final Factions plugin;

    public GhostbladeSword(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public Material getMaterial() {
        return Material.NETHERITE_SWORD;
    }

    @Override
    public String getName() {
        return ChatColor.LIGHT_PURPLE + "Ghostblade";
    }

    @Override
    public List<String> getLore() {
        final net.md_5.bungee.api.ChatColor speedColor = net.md_5.bungee.api.ChatColor.of(String.format("#%02x%02x%02x",
                PotionType.SPEED.getEffectType().getColor().getRed(),
                PotionType.SPEED.getEffectType().getColor().getGreen(),
                PotionType.SPEED.getEffectType().getColor().getBlue())
        );

        final List<String> res = getMythicLore();
        res.add(ChatColor.RESET + " ");
        res.add(ChatColor.GOLD + "Active" + ChatColor.YELLOW + ": Slaying a player");
        res.add(ChatColor.YELLOW + "will grant you and your nearby");
        res.add(ChatColor.YELLOW + "faction members a " + speedColor + "Speed Boost");
        res.add(ChatColor.YELLOW + "for " + ChatColor.GOLD + "20 seconds" + ChatColor.YELLOW + ".");

        return res;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        final CXService cxs = (CXService) plugin.getService(CXService.class);
        final Map<Enchantment, Integer> enchantments = Maps.newHashMap();
        final int sharpLevel = cxs.getEnchantLimitModule().getMaxEnchantmentLevel(Enchantment.DAMAGE_ALL);

        enchantments.put(Enchantment.DAMAGE_ALL, (sharpLevel == -1 ? 5 : sharpLevel));
        return enchantments;
    }

    @Override
    public void onKill(Player player, LivingEntity slainEntity) {
        if (!(slainEntity instanceof Player)) {
            return;
        }

        final PotionEffect speedEffect = new PotionEffect(PotionEffectType.SPEED, 20*20, 2);
        final PotionEffect hasteEffect = new PotionEffect(PotionEffectType.FAST_DIGGING, 20*20, 0);

        Players.giveTemporaryEffect(plugin, player, speedEffect);
        Players.giveTemporaryEffect(plugin, player, hasteEffect);
        Worlds.playSound(player.getLocation(), Sound.ENTITY_ALLAY_ITEM_THROWN);

        FMessage.printGhostblade(player, player);

        FactionUtil.getNearbyFriendlies(plugin, player, 16).forEach(nearbyFriendly -> {
            Players.giveTemporaryEffect(plugin, nearbyFriendly, speedEffect);
            Players.giveTemporaryEffect(plugin, nearbyFriendly, hasteEffect);
            FMessage.printGhostblade(nearbyFriendly, player);
        });
    }
}
