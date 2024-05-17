package gg.hcfactions.factions.crowbar;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.crowbar.impl.CrowbarExecutor;
import gg.hcfactions.factions.items.Crowbar;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.crowbar.ECrowbarUseType;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;

public final class CrowbarManager implements IManager {
    @Getter public final Factions plugin;
    @Getter public final CrowbarExecutor executor;

    public CrowbarManager(Factions plugin) {
        this.plugin = plugin;
        this.executor = new CrowbarExecutor(this);
    }

    @Override
    public void onEnable() {
        final CustomItemService cis = (CustomItemService) plugin.getService(CustomItemService.class);

        if (cis == null) {
            plugin.getAresLogger().error("could not obtain custom item service. crowbars will not function correctly");
            return;
        }

        cis.registerNewItem(new Crowbar(plugin));
    }

    @Override
    public void onDisable() {}

    /**
     * Parses lore and returns the remaining uses for the provided itemstack with the given type
     * @param item ItemStack
     * @param type Crowbar Use Type
     * @return Remaining use count
     */
    public int getRemainingUses(ItemStack item, ECrowbarUseType type) {
        final ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta == null || itemMeta.lore() == null) {
            return 0;
        }

        final CustomItemService cis = (CustomItemService) plugin.getService(CustomItemService.class);
        if (cis == null) {
            plugin.getAresLogger().error("Failed to obtain custom item service");
            return 0;
        }

        final Crowbar crowbar = (Crowbar)cis.getItem(item).stream().findFirst().orElse(null);
        if (crowbar == null) {
            return 0;
        }

        Component componentQuery = (type.equals(ECrowbarUseType.SPAWNER) ? Crowbar.MONSTER_SPAWNER_PREFIX_COMPONENT : Crowbar.END_PORTAL_PREFIX_COMPONENT);
        Optional<Component> componentResult = itemMeta.lore().stream().filter(entry -> entry.contains(componentQuery)).findFirst();
        if (componentResult.isEmpty() || componentResult.get().children().isEmpty()) {
            Bukkit.broadcast(Component.text("componentResult empty"));
            return 0;
        }

        Component childComponent = componentResult.get().children().getFirst();
        if (!(childComponent instanceof final TextComponent textComponent)) {
            Bukkit.broadcast(Component.text("Child component empty"));
            return 0;
        }

        String line = textComponent.content();
        Bukkit.broadcast(Component.text("Found value: " + line));

        int remaining;
        try {
            remaining = Integer.parseInt(line);
        } catch (NumberFormatException e) {
            plugin.getAresLogger().warn("Attempted to parse integer from crowbar lore and failed", e);
            return 0;
        }

        return remaining;
    }
}
