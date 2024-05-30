package gg.hcfactions.factions.events.menu;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.events.impl.CaptureEventConfig;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.libs.bukkit.builder.impl.ItemBuilder;
import gg.hcfactions.libs.bukkit.menu.impl.Clickable;
import gg.hcfactions.libs.bukkit.menu.impl.GenericMenu;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

@Getter
public final class KOTHConfigMenu extends GenericMenu {
    public final Factions plugin;
    public final KOTHEvent event;
    @Setter public CaptureEventConfig config;

    public KOTHConfigMenu(Factions plugin, KOTHEvent event, Player player) {
        super(plugin, player, event.getName() + " Configuration", 3);
        this.plugin = plugin;
        this.event = event;
        this.config = new CaptureEventConfig();
    }

    private void populate() {
        clear();

        final ItemStack incrTicketsToWin = new ItemBuilder()
                .setMaterial(Material.BOOK)
                .setName(Component.text("Tickets to Win", NamedTextColor.AQUA))
                .addLore(Lists.newArrayList(
                        ChatColor.GOLD + "Current: " + ChatColor.AQUA + config.getDefaultTicketsNeededToWin(),
                        ChatColor.RESET + " ",
                        ChatColor.YELLOW + "Left-click" + ChatColor.GRAY + " to increase",
                        ChatColor.YELLOW + "Right-click" + ChatColor.GRAY + " to decrease"
                ))
                .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                .build();

        final ItemStack incrTimerDuration = new ItemBuilder()
                .setMaterial(Material.BOOK)
                .setName(ChatColor.AQUA + "Timer Duration")
                .addLore(Lists.newArrayList(
                        ChatColor.GOLD + "Current: " + ChatColor.AQUA + config.getDefaultTimerDuration(),
                        ChatColor.RESET + " ",
                        ChatColor.YELLOW + "Left-click" + ChatColor.GRAY + " to increase",
                        ChatColor.YELLOW + "Right-click" + ChatColor.GRAY + " to decrease"
                ))
                .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                .build();

        final ItemStack incrTokenReward = new ItemBuilder()
                .setMaterial(Material.BOOK)
                .setName(ChatColor.AQUA + "Token Reward")
                .addLore(Lists.newArrayList(
                        ChatColor.GOLD + "Current: " + ChatColor.AQUA + config.getTokenReward(),
                        ChatColor.RESET + " ",
                        ChatColor.YELLOW + "Left-click" + ChatColor.GRAY + " to increase",
                        ChatColor.YELLOW + "Right-click" + ChatColor.GRAY + " to decrease"
                ))
                .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                .build();

        final ItemStack incrTickCheckpointInterval = new ItemBuilder()
                .setMaterial(Material.BOOK)
                .setName(ChatColor.AQUA + "Tick Checkpoint Interval")
                .addLore(Lists.newArrayList(
                        ChatColor.GOLD + "Current: " + ChatColor.AQUA + config.getTickCheckpointInterval(),
                        ChatColor.RESET + " ",
                        ChatColor.YELLOW + "Left-click" + ChatColor.GRAY + " to increase",
                        ChatColor.YELLOW + "Right-click" + ChatColor.GRAY + " to decrease"
                ))
                .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                .build();

        final ItemStack incrContestedThreshold = new ItemBuilder()
                .setMaterial(Material.BOOK)
                .setName(ChatColor.AQUA + "Contest Threshold")
                .addLore(Lists.newArrayList(
                        ChatColor.GOLD + "Current: " + ChatColor.AQUA + config.getContestedThreshold(),
                        ChatColor.RESET + " ",
                        ChatColor.YELLOW + "Left-click" + ChatColor.GRAY + " to increase",
                        ChatColor.YELLOW + "Right-click" + ChatColor.GRAY + " to decrease"
                ))
                .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                .build();

        final ItemStack incrPlayerOnlineLimit = new ItemBuilder()
                .setMaterial(Material.BOOK)
                .setName(Component.text("Online Player Limit"))
                .addLoreComponents(Lists.newArrayList(
                        Component.text("If this value is greater than 0,", NamedTextColor.GRAY),
                        Component.text("factions that exceed this limit will", NamedTextColor.GRAY),
                        Component.text("not be able to contest this event.", NamedTextColor.GRAY),
                        Component.empty().appendSpace(),
                        Component.text("Current:", NamedTextColor.GOLD).appendSpace().append(Component.text(config.getOnlinePlayerLimit(), NamedTextColor.AQUA)),
                        Component.empty().appendSpace(),
                        Component.text("Left-click", NamedTextColor.YELLOW).appendSpace().append(Component.text("to increase", NamedTextColor.GRAY)),
                        Component.text("Right-click", NamedTextColor.YELLOW).appendSpace().append(Component.text("to decrease", NamedTextColor.GRAY))
                ))
                .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                .build();

        final ItemStack majorityTurnoverEnabled = new ItemBuilder()
                .setMaterial(Material.BOOK)
                .setName(ChatColor.AQUA + "Majority Turnover")
                .addLore(Lists.newArrayList(
                        ChatColor.GRAY + "If this setting is enabled whichever",
                        ChatColor.GRAY + "faction has the most players in",
                        ChatColor.GRAY + "the capzone will be granted control",
                        ChatColor.GRAY + "of the event.",
                        ChatColor.RESET + " ",
                        ChatColor.GOLD + "Current: " + (config.isMajorityTurnoverEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"),
                        ChatColor.RESET + " ",
                        ChatColor.YELLOW + "Left-click" + ChatColor.GRAY + " to increase",
                        ChatColor.YELLOW + "Right-click" + ChatColor.GRAY + " to decrease"
                ))
                .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                .build();

        final ItemStack suddenDeathEnabled = new ItemBuilder()
                .setMaterial(Material.BOOK)
                .setName(ChatColor.AQUA + "Sudden Death")
                .addLore(Lists.newArrayList(
                        ChatColor.GRAY + "If this setting is enabled",
                        ChatColor.GRAY + "alongside " + ChatColor.YELLOW + "Tick Interval" + ChatColor.GRAY + ", the event",
                        ChatColor.GRAY + "throughout the final tick of the event",
                        ChatColor.RESET + " ",
                        ChatColor.GOLD + "Current: " + (config.isSuddenDeathEnabled() ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled"),
                        ChatColor.RESET + " ",
                        ChatColor.YELLOW + "Left-click" + ChatColor.GRAY + " to increase",
                        ChatColor.YELLOW + "Right-click" + ChatColor.GRAY + " to decrease"
                ))
                .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                .build();

        final ItemStack confirmBtn = new ItemBuilder()
                .setMaterial(Material.WRITABLE_BOOK)
                .setName(ChatColor.GREEN + "Confirm")
                .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                .build();

        addItem(new Clickable(incrTicketsToWin, 0, click -> {
            if (click.isLeftClick()) {
                final int newValue = config.getDefaultTicketsNeededToWin() + (click.isShiftClick() ? 5 : 1);
                config.setDefaultTicketsNeededToWin(newValue);
                populate();
                return;
            }

            if (click.isRightClick()) {
                final int newValue = Math.max(config.getDefaultTicketsNeededToWin() - (click.isShiftClick() ? 5 : 1), 1);
                config.setDefaultTicketsNeededToWin(newValue);
                populate();
            }
        }));

        addItem(new Clickable(incrTimerDuration, 1, click -> {
            if (click.isLeftClick()) {
                final int newValue = config.getDefaultTimerDuration() + (click.isShiftClick() ? 30 : 5);
                config.setDefaultTimerDuration(newValue);
                populate();
                return;
            }

            if (click.isRightClick()) {
                final int newValue = Math.max(config.getDefaultTimerDuration() - (click.isShiftClick() ? 30 : 5), 1);
                config.setDefaultTimerDuration(newValue);
                populate();
            }
        }));

        addItem(new Clickable(incrTokenReward, 2, click -> {
            if (click.isLeftClick()) {
                final int newValue = config.getTokenReward() + (click.isShiftClick() ? 50 : 1);
                config.setTokenReward(newValue);
                populate();
                return;
            }

            if (click.isRightClick()) {
                final int newValue = Math.max(config.getTokenReward() - (click.isShiftClick() ? 50 : 1), 1);
                config.setTokenReward(newValue);
                populate();
            }
        }));

        addItem(new Clickable(incrTickCheckpointInterval, 3, click -> {
            if (click.isLeftClick()) {
                final int newValue = config.getTickCheckpointInterval() + 1;
                config.setTickCheckpointInterval(newValue);
                populate();
                return;
            }

            if (click.isRightClick()) {
                final int newValue = Math.max(config.getTickCheckpointInterval() - 1, 1);
                config.setTickCheckpointInterval(newValue);
                populate();
            }
        }));

        addItem(new Clickable(incrContestedThreshold, 4, click -> {
            if (click.isLeftClick()) {
                final int newValue = config.getContestedThreshold() + 1;
                config.setContestedThreshold(newValue);
                populate();
                return;
            }

            if (click.isRightClick()) {
                final int newValue = Math.max(config.getContestedThreshold() - 1, 1);
                config.setContestedThreshold(newValue);
                populate();
            }
        }));

        addItem(new Clickable(incrPlayerOnlineLimit, 5, click -> {
            if (click.isLeftClick()) {
                // final int newValue = config.getTokenReward() + (click.isShiftClick() ? 50 : 1);
                final int newValue = config.getOnlinePlayerLimit() + (click.isShiftClick() ? 5 : 1);
                config.setOnlinePlayerLimit(newValue);
                populate();
                return;
            }

            if (click.isRightClick()) {
                final int newValue = Math.max(config.getOnlinePlayerLimit() - (click.isShiftClick() ? 5 : 1), -1);
                config.setOnlinePlayerLimit(newValue);
                populate();
            }
        }));

        addItem(new Clickable(majorityTurnoverEnabled, 6, click -> {
            config.setMajorityTurnoverEnabled(!config.isMajorityTurnoverEnabled());
            populate();
        }));

        addItem(new Clickable(suddenDeathEnabled, 7, click -> {
            config.setSuddenDeathEnabled(!config.isSuddenDeathEnabled());
            populate();
        }));

        addItem(new Clickable(confirmBtn, 26, click -> {
            if (event.isActive()) {
                event.setEventConfig(config);
            } else {
                event.startEvent(config);
            }

            player.closeInventory();
        }));
    }

    @Override
    public void open() {
        super.open();
        populate();
    }
}
