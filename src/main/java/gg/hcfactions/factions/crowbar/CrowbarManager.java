package gg.hcfactions.factions.crowbar;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.crowbar.impl.CrowbarExecutor;
import gg.hcfactions.factions.items.Crowbar;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.crowbar.ECrowbarUseType;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

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
        final CustomItemService cis = (CustomItemService) plugin.getService(CustomItemService.class);

        if (item.getItemMeta() == null || item.getItemMeta().getLore() == null) {
            return 0;
        }

        if (cis == null) {
            plugin.getAresLogger().error("failed to obtain custom item service");
            return 0;
        }

        final Crowbar crowbar = (Crowbar)cis.getItem(item).stream().findFirst().orElse(null);
        if (crowbar == null) {
            return 0;
        }

        final String line = item.getItemMeta().getLore().get(type.getLorePosition());
        final String replaced = (type.equals(ECrowbarUseType.SPAWNER)
                ? line.replaceAll(Crowbar.MONSTER_SPAWNER_PREFIX, "")
                : line.replaceAll(Crowbar.END_PORTAL_PREFIX, ""));

        int remaining;
        try {
            remaining = Integer.parseInt(replaced);
        } catch (NumberFormatException e) {
            plugin.getAresLogger().warn("attempted to parse integer from crowbar lore and failed", e);
            return 0;
        }

        return remaining;
    }
}
