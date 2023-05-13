package gg.hcfactions.factions.timers;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import gg.hcfactions.cx.CXService;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import gg.hcfactions.libs.base.timer.impl.GenericTimer;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.stream.Collectors;

public final class TimerManager implements IManager {
    @Getter public final Factions plugin;
    @Getter @Setter public BukkitTask updateTask;
    @Getter @Setter public BukkitTask uiTask;

    public TimerManager(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        final CXService commandXService = (CXService)plugin.getService(CXService.class);

        uiTask = new Scheduler(plugin).sync(() -> Bukkit.getOnlinePlayers().forEach(player -> {
            final FactionPlayer fp = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);
            boolean hasUI = false;
            boolean useScoreboard = fp.isPreferScoreboardDisplay();

            if (!fp.getTimers().isEmpty()) {
                hasUI = true;
            }

            else if (commandXService.getRebootModule().isEnabled() && commandXService.getRebootModule().isRebootInProgress()) {
                hasUI = true;
            }

            else if (commandXService.getVanishManager().isVanished(player)) {
                hasUI = true;
            }

            else if (!plugin.getEventManager().getActiveEvents().isEmpty()) {
                hasUI = true;
            }

            if (!hasUI) {
                if (fp.isPreferScoreboardDisplay() && fp.getScoreboard() != null && !fp.getScoreboard().isHidden()) {
                    fp.getScoreboard().hide();
                }
            } else if (useScoreboard) {
                renderSidebar(player, fp);
            } else {
                renderHotbar(player, fp);
            }
        })).repeat(0L, 1L).run();

        updateTask = new Scheduler(plugin).sync(() -> {
            plugin.getPlayerManager().getPlayerRepository()
                    .stream()
                    .filter(p -> !p.getTimers().isEmpty())
                    .forEach(p -> {
                        p.getTimers().stream().filter(GenericTimer::isExpired).forEach(exp -> p.finishTimer(exp.getType()));
            });

            // total lambda nightmare, streams player factions and expires their timers
            plugin.getFactionManager().getFactionRepository()
                    .stream()
                    .filter(f -> f instanceof PlayerFaction)
                    .filter(pf -> !((PlayerFaction)pf).getTimers().isEmpty())
                    .forEach(pf -> ((PlayerFaction) pf).getTimers().stream().filter(GenericTimer::isExpired).forEach(exp -> ((PlayerFaction)pf).finishTimer(exp.getType())));
        }).repeat(0L, 1L).run();
    }

    @Override
    public void onDisable() {
        uiTask.cancel();
        updateTask.cancel();
        uiTask = null;
        updateTask = null;
    }

    private void renderHotbar(Player player, FactionPlayer factionPlayer) {
        final CXService commandXService = (CXService)plugin.getService(CXService.class);
        final List<String> toRender = Lists.newArrayList();

        factionPlayer.getTimers().stream().filter(t -> t.getType().isRender()).forEach(rt -> {
            final String time = (rt.getType().isDecimal() && rt.getRemainingSeconds() < 10)
                    ? Time.convertToDecimal(rt.getRemaining()) + "s"
                    : Time.convertToHHMMSS(rt.getRemaining());

            toRender.add(rt.getType().getDisplayName() + ChatColor.RED + ": " + time);
        });

        plugin.getEventManager().getActiveKothEvents().forEach(kothEvent -> {
            if (kothEvent.getSession() != null) {
                final long remainingMillis = kothEvent.getSession().getTimer().getRemaining();
                final int remainingSeconds = (int)(remainingMillis/1000L);

                String displayed = (remainingSeconds < 10 ? Time.convertToDecimal(remainingMillis) + "s" : Time.convertToHHMMSS(remainingMillis));

                if (remainingMillis <= 0) {
                    displayed = "Capturing...";
                }

                else if (kothEvent.getSession().isContested()) {
                    displayed = "Contested";
                }

                toRender.add(kothEvent.getDisplayName() + ChatColor.RED + ": " + displayed);
            }
        });

        if (commandXService != null) {
            if (commandXService.getRebootModule().isEnabled() && commandXService.getRebootModule().isRebootInProgress()) {
                toRender.add(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Restart" + ChatColor.RED + ": " + Time.convertToHHMMSS(commandXService.getRebootModule().getTimeUntilReboot()));
            }

            if (commandXService.getVanishManager().isVanished(player)) {
                toRender.add(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Vanished");
            }
        }

        if (!toRender.isEmpty()) {
            final String hud = Joiner.on(ChatColor.RESET + " " + ChatColor.RESET + " ").join(toRender);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(hud));
        }
    }

    private void renderSidebar(Player player, FactionPlayer factionPlayer) {
        final CXService commandXService = (CXService)plugin.getService(CXService.class);
        final String SPACER = ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 24);

        for (FTimer rt : factionPlayer.getTimers().stream().filter(t -> t.getType().isRender()).collect(Collectors.toList())) {
            final String time = (rt.getType().isDecimal() && rt.getRemainingSeconds() < 10)
                    ? Time.convertToDecimal(rt.getRemaining()) + "s"
                    : Time.convertToHHMMSS(rt.getRemaining());

            factionPlayer.getScoreboard().setLine(
                    rt.getType().getScoreboardPosition(),
                    rt.getType().getDisplayName() + ChatColor.RED + ": " + time
            );
        }

        int eventCursor = 16;
        for (KOTHEvent kothEvent : plugin.getEventManager().getActiveKothEvents()) {
            if (kothEvent.getSession() == null) {
                continue;
            }

            final long remainingMillis = kothEvent.getSession().getTimer().getRemaining();
            final int remainingSeconds = (int)(remainingMillis/1000L);

            String displayed = (remainingSeconds < 10 ? Time.convertToDecimal(remainingMillis) + "s" : Time.convertToHHMMSS(remainingMillis));

            if (remainingMillis <= 0) {
                displayed = "Capturing...";
            }

            else if (kothEvent.getSession().isContested()) {
                displayed = "Contested";
            }

            factionPlayer.getScoreboard().setLine(eventCursor, kothEvent.getDisplayName() + ChatColor.RED + ": " + displayed);
            eventCursor += 1;
        }

        if (commandXService != null) {
            if (commandXService.getVanishManager().isVanished(player)) {
                factionPlayer.getScoreboard().setLine(24, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Vanished");
            } else {
                factionPlayer.getScoreboard().removeLine(24);
            }

            if (commandXService.getRebootModule().isEnabled() && commandXService.getRebootModule().isRebootInProgress()) {
                factionPlayer.getScoreboard().setLine(25, ChatColor.DARK_RED + "" + ChatColor.BOLD + "" + "Restart" + ChatColor.RED + ": "  + Time.convertToHHMMSS(commandXService.getRebootModule().getTimeUntilReboot()));
            } else {
                factionPlayer.getScoreboard().removeLine(25);
            }
        }

        factionPlayer.getScoreboard().setLine(0, SPACER);
        factionPlayer.getScoreboard().setLine(2, ChatColor.RESET + " ");
        factionPlayer.getScoreboard().setLine(1, plugin.getConfiguration().getScoreboardFooter());
        factionPlayer.getScoreboard().setLine(63, SPACER + ChatColor.RESET);
    }
}
