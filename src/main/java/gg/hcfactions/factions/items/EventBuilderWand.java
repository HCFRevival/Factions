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

public final class EventBuilderWand implements ICustomItem {
    @Override
    public Material getMaterial() {
        return Material.DIAMOND_AXE;
    }

    @Override
    public String getName() {
        return ChatColor.YELLOW + "Event Wand";
    }

    @Override
    public Component getDisplayNameComponent() {
        return Component.text("Event Wand", NamedTextColor.YELLOW);
    }

    @Override
    public List<String> getLore() {
        final List<String> lore = Lists.newArrayList();
        lore.add(ChatColor.BLUE + "Punch a block while holding this item");
        lore.add(ChatColor.BLUE + "to set locations for the event");
        lore.add(ChatColor.BLUE + "you are currently building.");
        lore.add(ChatColor.RESET + " ");
        lore.add(ChatColor.RED + "If you are not building an event, drop this item.");
        return lore;
    }

    @Override
    public List<Component> getLoreComponents() {
        final List<Component> res = Lists.newArrayList();
        res.add(Component.keybind("key.attack").appendSpace().append(Component.text("to set points").color(NamedTextColor.YELLOW)));
        return res;
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
