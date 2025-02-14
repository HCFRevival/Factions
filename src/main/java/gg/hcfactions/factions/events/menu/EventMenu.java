package gg.hcfactions.factions.events.menu;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.events.IEvent;
import gg.hcfactions.factions.models.events.impl.ConquestZone;
import gg.hcfactions.factions.models.events.impl.types.ConquestEvent;
import gg.hcfactions.factions.models.events.impl.types.DPSEvent;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.events.impl.types.PalaceEvent;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.libs.base.util.Strings;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.builder.impl.ItemBuilder;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
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
        super(plugin, player, "Events", 3);
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

                /// Start DPS ///
                if (event instanceof final DPSEvent dpsEvent) {
                    final boolean isActive = dpsEvent.isActive();

                    builder.setMaterial(isActive ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);

                    lore.add(FMessage.LAYER_2 + "Type" + FMessage.LAYER_1 + ": " + FMessage.INFO + "DPS Check");
                    lore.add(FMessage.LAYER_2 + "Status" + FMessage.LAYER_1 + ": " + (isActive ? ChatColor.GREEN + "Active" : ChatColor.RED + "Inactive"));

                    if (!isActive) {
                        final BLocatable spawnpoint = dpsEvent.getRandomSpawnpoint();
                        final String worldName = Strings.capitalize(spawnpoint.getBukkitBlock().getWorld().getEnvironment().name().toLowerCase().replaceAll("_", " "));
                        final int x = (int)Math.round(spawnpoint.getX());
                        final int y = (int)Math.round(spawnpoint.getY());
                        final int z = (int)Math.round(spawnpoint.getZ());
                        lore.add(FMessage.LAYER_2 + "Location" + FMessage.LAYER_1 + ": " + worldName + ", " + x + ", " + y + ", " + z);
                    }

                    if (isActive) {
                        final Map<UUID, Long> leaderboard = dpsEvent.getSession().getSortedLeaderboard();
                        final String worldName = Strings.capitalize(dpsEvent.getSession().getDpsEntity().getEntity().getWorld().getEnvironment().name().toLowerCase().replaceAll("_", " "));
                        final int x = dpsEvent.getSession().getDpsEntity().getEntity().getLocation().getBlockX();
                        final int y = dpsEvent.getSession().getDpsEntity().getEntity().getLocation().getBlockY();
                        final int z = dpsEvent.getSession().getDpsEntity().getEntity().getLocation().getBlockZ();

                        lore.add(FMessage.LAYER_2 + "Location" + FMessage.LAYER_1 + ": " + worldName + ", " + x + ", " + y + ", " + z);

                        lore.add(ChatColor.RESET + " ");
                        lore.add(FMessage.LAYER_2 + "Damage Needed" + FMessage.LAYER_1 + ": Most Damage Wins");
                        lore.add(FMessage.LAYER_2 + "Token Reward" + FMessage.LAYER_1 + ": " + dpsEvent.getSession().getTokenReward());
                        lore.add(ChatColor.RESET + " ");

                        if (dpsEvent.getSession().getMostRecentDamager() != null) {
                            lore.add(FMessage.LAYER_2 + "Controlled By" + FMessage.LAYER_1 + ": " + dpsEvent.getSession().getMostRecentDamager().getName());
                        }

                        if (!leaderboard.isEmpty()) {
                            int position = 1;

                            for (UUID factionId : leaderboard.keySet()) {
                                final long damage = leaderboard.getOrDefault(factionId, -1L);
                                final PlayerFaction playerFaction = plugin.getFactionManager().getPlayerFactionById(factionId);

                                if (playerFaction != null) {
                                    final String formattedValue = String.format("%,d", damage);
                                    lore.add(FMessage.LAYER_2 + "" + position + ". " + FMessage.INFO + playerFaction.getName() + FMessage.LAYER_1 + ": " + formattedValue);
                                    position += 1;
                                }
                            }
                        } else {
                            lore.add(FMessage.LAYER_1 + "No factions have contributed damage yet");
                        }
                    }
                }
                /// End DPS ///

                /// Start Conquest ///
                if (event instanceof final ConquestEvent conqEvent) {
                    final boolean isActive = conqEvent.isActive();

                    builder.setMaterial(isActive ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE);

                    lore.add(FMessage.LAYER_2 + "Type" + FMessage.LAYER_1 + ": " + FMessage.INFO + "Conquest");
                    lore.add(FMessage.LAYER_2 + "Status" + FMessage.LAYER_1 + ": " + (isActive ? ChatColor.GREEN + "Active" : ChatColor.RED + "Inactive"));

                    if (!conqEvent.getZones().isEmpty()) {
                        lore.add(FMessage.LAYER_2 + "Zones" + FMessage.LAYER_1 + ":");
                        conqEvent.getZones().forEach(zone -> lore.add(ChatColor.RESET + " " + FMessage.LAYER_1 + " - " + zone.getDisplayName() + FMessage.LAYER_1 + ": " + FMessage.INFO + zone.getCaptureRegion().getCenter()));
                    }

                    if (isActive) {
                        lore.add(ChatColor.RESET + " ");
                        lore.add(FMessage.LAYER_2 + "Tickets Needed" + FMessage.LAYER_1 + ": " + conqEvent.getSession().getTicketsNeededToWin());
                        lore.add(FMessage.LAYER_2 + "Tickets per Tick" + FMessage.LAYER_1 + ": " + conqEvent.getSession().getTicketsPerTick());
                        lore.add(FMessage.LAYER_2 + "Timer Duration" + FMessage.LAYER_1 + ": " + conqEvent.getSession().getTimerDuration());
                        lore.add(FMessage.LAYER_2 + "Token Reward" + FMessage.LAYER_1 + ": " + conqEvent.getSession().getTokenReward());
                    }

                    lore.add(ChatColor.RESET + " ");

                    if (isActive) {
                        for (ConquestZone zone : conqEvent.getZones()) {
                            lore.add(zone.getDisplayName() + FMessage.LAYER_1 + ": " + Time.convertToRemaining(zone.getTimer().getRemaining()));
                        }

                        final Map<UUID, Integer> leaderboard = conqEvent.getSession().getSortedLeaderboard();
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
                    } else {
                        final PlayerFaction capturingFaction = plugin.getFactionManager().getPlayerFactionById(conqEvent.getCapturingFaction());

                        if (capturingFaction != null) {
                            lore.add(FMessage.LAYER_2 + "Captured By" + FMessage.LAYER_1 + ": " + capturingFaction.getName());
                            lore.add(FMessage.LAYER_2 + "Next Restock" + FMessage.LAYER_1 + ": " + (conqEvent.getTimeUntilNextRestock() > 0 ? Time.convertToRemaining(conqEvent.getTimeUntilNextRestock()) : "Restocking..."));
                            lore.add(ChatColor.RESET + " ");
                        }

                        lore.add(ChatColor.GRAY + "This event will activate:");
                        lore.add(ChatColor.WHITE + ((conqEvent.getRemainingTimeUntilNextSchedule() != -1) ? Time.convertToRemaining(conqEvent.getRemainingTimeUntilNextSchedule()) : "Unscheduled"));
                    }
                }
                /// End Conquest ///

                // Start KOTH ///
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
                        final List<Integer> tickCheckpoints = kothEvent.getSession().getTickCheckpoints();

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
                                        if (!tickCheckpoints.isEmpty()) {
                                            final StringBuilder progressDisplay = new StringBuilder();

                                            for (int i = 0; i < tickCheckpoints.size(); i++) {
                                                final int currentCheckpoint = tickCheckpoints.get(i);
                                                final int nextCheckpoint = (tickCheckpoints.size() > (i + 1)) ? tickCheckpoints.get(i + 1) : kothEvent.getSession().getTicketsNeededToWin();
                                                final boolean firstCheckpoint = (i == 0);
                                                final boolean greaterThanCurrent = currentCheckpoint <= tickets;
                                                final boolean greaterThanNext = nextCheckpoint <= tickets;

                                                if (!firstCheckpoint) {
                                                    progressDisplay.append(ChatColor.GRAY + "/");
                                                }

                                                // greater than current, but not the next
                                                if (greaterThanCurrent && !greaterThanNext) {
                                                    progressDisplay.append(ChatColor.WHITE + "" + tickets);
                                                    continue;
                                                }

                                                // greater than current AND next
                                                if (greaterThanCurrent) {
                                                    progressDisplay.append(ChatColor.WHITE + "" + currentCheckpoint);
                                                    continue;
                                                }

                                                // not greater than current or next
                                                progressDisplay.append(ChatColor.GRAY + "" + (firstCheckpoint ? tickets : currentCheckpoint));
                                            }

                                            lore.add(FMessage.LAYER_2 + "" + position + ". " + FMessage.LAYER_1 + playerFaction.getName() + " - " + progressDisplay);
                                        } else {
                                            lore.add(FMessage.LAYER_2 + "" + position + ". " + FMessage.LAYER_1 + playerFaction.getName() + " - " + FMessage.INFO + tickets);
                                        }

                                        position += 1;
                                    }
                                }
                            } else {
                                lore.add(FMessage.LAYER_1 + "There are no entries on the scoreboard.");
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
