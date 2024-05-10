package gg.hcfactions.factions.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

public final class Sugarcube implements ICustomItem {
    @Getter public final Factions plugin;

    public Sugarcube(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public Material getMaterial() {
        return Material.WHITE_CONCRETE_POWDER;
    }

    @Override
    public Map.Entry<NamespacedKey, String> getIdentifier() {
        return Map.entry(plugin.getNamespacedKey(), "Sugarcube");
    }

    @Override
    public Component getDisplayNameComponent() {
        return Component.text("Sugar Cube", NamedTextColor.AQUA);
    }

    @Override
    public List<String> getLore() {
        final List<String> res = Lists.newArrayList();

        res.add(ChatColor.GRAY + "Right-click a Horse while holding this item");
        res.add(ChatColor.GRAY + "to upgrade its movement speed");

        return res;
    }

    @Override
    public List<Component> getLoreComponents() {
        final List<Component> res = Lists.newArrayList();
        res.add(Component.keybind("key.use").color(NamedTextColor.LIGHT_PURPLE).appendSpace().append(Component.text("a Horse while holding this item")));
        res.add(Component.text("to upgrade its movement speed").color(NamedTextColor.GRAY));
        return res;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return Maps.newHashMap();
    }

    @Override
    public boolean isSoulbound() {
        return false;
    }
}
