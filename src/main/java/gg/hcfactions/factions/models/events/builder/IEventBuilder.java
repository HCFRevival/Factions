package gg.hcfactions.factions.models.events.builder;

import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.items.EventBuilderWand;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.libs.bukkit.services.impl.items.CustomItemService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface IEventBuilder {
    Factions getPlugin();
    UUID getBuilderId();
    ServerFaction getOwner();
    String getName();
    String getDisplayName();

    void setOwner(String factionName);
    void setDisplayName(String name);

    default Player getBuilder() {
        return Bukkit.getPlayer(getBuilderId());
    }

    default void giveWand() {
        final CustomItemService cis = (CustomItemService) getPlugin().getService(CustomItemService.class);
        if (cis == null) {
            getPlugin().getAresLogger().error("attempted to give an event builder wand but returned null");
            return;
        }

        cis.getItem(EventBuilderWand.class).ifPresent(wand -> getBuilder().getInventory().addItem(wand.getItem()));
        getBuilder().sendMessage(ChatColor.YELLOW + "You have received a " + ChatColor.DARK_PURPLE + "Event Builder Wand");
    }
}
