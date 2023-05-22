package gg.hcfactions.factions.events.menu;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.events.IEvent;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.events.impl.types.PalaceEvent;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.builder.impl.ItemBuilder;
import gg.hcfactions.libs.bukkit.menu.impl.Clickable;
import gg.hcfactions.libs.bukkit.menu.impl.GenericMenu;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class EventMenu extends GenericMenu {
    @Getter public Factions plugin;

    public EventMenu(Factions plugin, Player player) {
        super(plugin, player, "Events", plugin.getEventManager().getEventRepository().size() % 9);
        this.plugin = plugin;
    }

    @Override
    public void open() {
        super.open();

        addUpdater(() -> {
            final List<IEvent> events = plugin.getEventManager().getEventsAlphabeticalOrder();
            int cursor = 0;

            clear();

            for (IEvent event : events) {
                final ItemBuilder builder = new ItemBuilder().setName(event.getDisplayName());
                final List<String> lore = Lists.newArrayList();

                if (event instanceof final KOTHEvent kothEvent) {
                    final boolean isActive = kothEvent.isActive();

                    builder.setMaterial(isActive ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);

                    lore.add(FMessage.LAYER_2 + "Type" + FMessage.LAYER_1 + ": " + FMessage.INFO + "King of the Hill");
                    lore.add(FMessage.LAYER_2 + "Status" + FMessage.LAYER_1 + ": " + (isActive ? ChatColor.GREEN + "Active" : ChatColor.RED + "Inactive"));
                    lore.add(FMessage.LAYER_2 + "Location" + FMessage.LAYER_1 + ": " + FMessage.INFO + kothEvent.getCaptureRegion().getCornerA().toString());

                    if (isActive) {
                        lore.add(ChatColor.RESET + " ");
                        lore.add(FMessage.LAYER_2 + "Tickets Needed" + FMessage.LAYER_1 + ": " + kothEvent.getSession().getTicketsNeededToWin());
                        lore.add(FMessage.LAYER_2 + "Timer Duration" + FMessage.LAYER_1 + ": " + Time.convertToRemaining(kothEvent.getSession().getTimerDuration()*1000L));
                        lore.add(FMessage.LAYER_2 + "Token Reward" + FMessage.LAYER_1 + ": " + kothEvent.getSession().getTokenReward());
                    }

                    lore.add(ChatColor.RESET + " ");

                    if (isActive) {
                        if (kothEvent.getSession().getCapturingFaction() != null) {
                            final PlayerFaction playerFaction = kothEvent.getSession().getCapturingFaction();
                            lore.add(FMessage.LAYER_2 + "Controlled By" + FMessage.LAYER_1 + ": " + playerFaction.getName());
                        }

                        lore.add(FMessage.LAYER_2 + "Remaining Time" + FMessage.LAYER_1 + ": " + Time.convertToHHMMSS(kothEvent.getSession().getTimer().getRemaining()));

                        if (kothEvent.getSession().getTicketsNeededToWin() > 1) {
                            final Map<UUID, Integer> leaderboard = kothEvent.getSession().getSortedLeaderboard();
                            int position = 1;

                            lore.add(ChatColor.RESET + " ");
                            lore.add(FMessage.LAYER_2 + "Leaderboard");

                            if (!leaderboard.isEmpty()) {
                                for (UUID factionId : leaderboard.keySet()) {
                                    final int tickets = leaderboard.getOrDefault(factionId, -1);
                                    final PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFactionById(factionId);

                                    if (playerFaction != null) {
                                        lore.add(FMessage.LAYER_2 + "" + position + ". " + FMessage.LAYER_1 + playerFaction.getName() + FMessage.INFO + " (" + tickets + ")");
                                        position += 1;
                                    }
                                }
                            } else {
                                lore.add(FMessage.LAYER_1 + "No factions have earned a ticket yet");
                            }
                        }
                    } else {
                        if (kothEvent instanceof final PalaceEvent palace) {
                            final PlayerFaction capturingFaction = plugin.getFactionManager().getPlayerFactionById(palace.getCapturingFaction());

                            if (capturingFaction != null) {
                                lore.add(FMessage.LAYER_2 + "Captured By" + FMessage.LAYER_1 + ": " + capturingFaction.getName());
                                lore.add(FMessage.LAYER_2 + "Next Restock" + FMessage.LAYER_1 + ": " + (palace.getTimeUntilNextRestock() > 0 ? Time.convertToRemaining(palace.getTimeUntilNextRestock()) : "Restocking..."));
                                lore.add(ChatColor.RESET + " ");
                            }
                        }

                        lore.add(ChatColor.GRAY + "This event will activate:");
                        lore.add(ChatColor.WHITE + ((kothEvent.getRemainingTimeUntilNextSchedule() != -1) ? Time.convertToRemaining(kothEvent.getRemainingTimeUntilNextSchedule()) : "Unscheduled"));
                    }
                }

                builder.addLore(lore);

                addItem(new Clickable(builder.build(), cursor, click -> {
                    if (event.getOwner() != null) {
                        final ServerFaction serverFaction = plugin.getFactionManager().getServerFactionById(event.getOwner());

                        if (serverFaction != null) {
                            plugin.getFactionManager().getExecutor().showFactionInfo(player, serverFaction.getName());
                        }
                    }
                }));

                cursor += 1;
            }
        }, 20L);
    }
}
