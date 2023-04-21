package gg.hcfactions.factions.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.claims.ClaimBuilderManager;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

public final class FactionClaimingStick implements ICustomItem {
    @Getter public final ClaimBuilderManager claimBuilderManager;
    @Getter public Material material = Material.STICK;
    @Getter public String name = ChatColor.GREEN + "Faction Claiming Stick";

    public FactionClaimingStick(ClaimBuilderManager builderManager) {
        this.claimBuilderManager = builderManager;
    }

    @Override
    public List<String> getLore() {
        final List<String> lore = Lists.newArrayList();

        lore.add(ChatColor.GOLD + "Left-click" + ChatColor.YELLOW + " to set " + ChatColor.BLUE + "Corner A");
        lore.add(ChatColor.GOLD + "Right-click" + ChatColor.YELLOW + " to set " + ChatColor.BLUE + "Corner B");
        lore.add(ChatColor.RESET + " ");
        lore.add(ChatColor.AQUA + "With both corners set,");
        lore.add(ChatColor.AQUA + "Left-click " + ChatColor.AQUA + "" + ChatColor.UNDERLINE + "while sneaking" + ChatColor.AQUA + " to confirm");
        lore.add(ChatColor.RESET + " ");
        lore.add(ChatColor.RED + "To cancel the claiming process,");
        lore.add(ChatColor.RED + "Right-click " + ChatColor.RED + "" + ChatColor.UNDERLINE + "while sneaking" + ChatColor.RESET);
        lore.add(ChatColor.RESET + " ");
        lore.add(ChatColor.DARK_PURPLE + "Tips" + ChatColor.LIGHT_PURPLE + ": ");
        lore.add(ChatColor.YELLOW + " - " + ChatColor.GOLD + "All claims must be connected");
        lore.add(ChatColor.YELLOW + " - " + ChatColor.GOLD + "Claims must be " + claimBuilderManager.getClaimManager().getPlugin().getConfiguration().getDefaultPlayerFactionClaimBuffer() + " blocks away from other faction claims");
        lore.add(ChatColor.YELLOW + " - " + ChatColor.GOLD + "Claims can not be near Server Claims");
        lore.add(ChatColor.YELLOW + " - " + ChatColor.GOLD + "Claims can only be created in the Overworld");

        return lore;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return Maps.newHashMap();
    }

    @Override
    public boolean isSoulbound() {
        return true;
    }
}
