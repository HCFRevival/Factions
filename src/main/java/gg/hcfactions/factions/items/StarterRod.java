package gg.hcfactions.factions.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

public final class StarterRod implements ICustomItem {
    @Override
    public Material getMaterial() {
        return Material.FISHING_ROD;
    }

    @Override
    public String getName() {
        return ChatColor.GREEN + "Welcome to Revival";
    }

    @Override
    public Component getDisplayNameComponent() {
        return Component.text("Welcome to HCFR").color(NamedTextColor.GOLD);
    }

    @Override
    public List<String> getLore() {
        final List<String> lore = Lists.newArrayList();
        lore.add(ChatColor.RESET + " ");
        lore.add(ChatColor.AQUA + "Use this fishing rod to gather some food");
        lore.add(ChatColor.AQUA + "before you enter out in to the world.");
        lore.add(ChatColor.RESET + " ");
        lore.add(ChatColor.DARK_AQUA + "Good luck, have fun!");
        lore.add(ChatColor.RESET + " " + ChatColor.GOLD + " - Revival Team");
        return lore;
    }

    @Override
    public List<Component> getLoreComponents() {
        final List<Component> res = Lists.newArrayList();
        res.add(Component.text(" "));
        res.add(Component.text("Use this fishing rod to gather some food").color(NamedTextColor.AQUA));
        res.add(Component.text("before you enter the Overworld.").color(NamedTextColor.AQUA));
        res.add(Component.text(" "));
        res.add(Component.text("Good luck, and have fun!").color(NamedTextColor.GOLD));
        return res;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        final Map<Enchantment, Integer> enchantments = Maps.newHashMap();
        enchantments.put(Enchantment.LURE, 1);
        enchantments.put(Enchantment.UNBREAKING, 1);
        return enchantments;
    }

    @Override
    public boolean isSoulbound() {
        return true;
    }
}
