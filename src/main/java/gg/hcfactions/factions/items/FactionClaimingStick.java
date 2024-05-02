package gg.hcfactions.factions.items;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.factions.claims.ClaimBuilderManager;
import gg.hcfactions.libs.bukkit.services.impl.items.ICustomItem;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Map;

@Getter
public final class FactionClaimingStick implements ICustomItem {
    public final ClaimBuilderManager claimBuilderManager;
    public Material material = Material.STICK;

    public FactionClaimingStick(ClaimBuilderManager builderManager) {
        this.claimBuilderManager = builderManager;
    }

    @Override
    public String getName() {
        return ChatColor.GREEN + "Faction Claiming Stick";
    }

    @Override
    public Component getDisplayNameComponent() {
        return Component.text("Faction Claiming Stick").color(NamedTextColor.GREEN);
    }

    @Override
    public List<String> getLore() {
        return List.of();
    }

    @Override
    public List<Component> getLoreComponents() {
        final List<Component> res = Lists.newArrayList();
        final Component bullet = Component.text(" - ").color(NamedTextColor.GRAY).appendSpace();
        final double buffer = claimBuilderManager.getClaimManager().getPlugin().getConfiguration().getDefaultPlayerFactionClaimBuffer();

        // key.attack, key.use
        res.add(Component.keybind("key.attack").color(NamedTextColor.GOLD)
                .appendSpace().append(Component.text("to set").color(NamedTextColor.YELLOW)
                .appendSpace().append(Component.text("Point #1").color(NamedTextColor.BLUE))));

        res.add(Component.keybind("key.use").color(NamedTextColor.GOLD)
                .appendSpace().append(Component.text("to set").color(NamedTextColor.YELLOW)
                .appendSpace().append(Component.text("Point #2").color(NamedTextColor.BLUE))));

        res.add(Component.text(" "));

        res.add(Component.text("With both corners set").color(NamedTextColor.GOLD).append(Component.text(":").color(NamedTextColor.YELLOW)));
        res.add(Component.keybind("key.attack").color(NamedTextColor.GOLD)
                .appendSpace().append(Component.text("while sneaking to confirm your claim").color(NamedTextColor.YELLOW)));

        res.add(Component.text(" "));

        res.add(Component.text("To cancel the claiming process").color(NamedTextColor.GOLD).append(Component.text(":").color(NamedTextColor.YELLOW)));
        res.add(Component.keybind("key.use").color(NamedTextColor.GOLD)
                .appendSpace().append(Component.text("while sneaking").color(NamedTextColor.YELLOW)));

        res.add(Component.text(" "));

        res.add(Component.text("Tips").color(NamedTextColor.AQUA));
        res.add(Component.empty().append(bullet).append(Component.text("All claims must be connected").color(NamedTextColor.YELLOW)));
        res.add(Component.empty().append(bullet).append(Component.text("Claims must").color(NamedTextColor.YELLOW)
                .appendSpace().append(Component.text(buffer).color(NamedTextColor.BLUE))).appendSpace().append(Component.text("blocks"))
                .appendSpace().append(Component.text("away from other player faction claims").color(NamedTextColor.YELLOW))
        );
        res.add(Component.empty().append(bullet).append(Component.text("Player Faction claims can not be near Server Faction Claims").color(NamedTextColor.YELLOW)));
        res.add(Component.empty().append(bullet).append(Component.text("Player Faction claims can only exist in the Overworld").color(NamedTextColor.YELLOW)));

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
