package gg.hcfactions.factions.timers;

import gg.hcfactions.cx.CXService;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.classes.EEffectScoreboardMapping;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.classes.impl.Tank;
import gg.hcfactions.factions.models.events.impl.ConquestZone;
import gg.hcfactions.factions.models.events.impl.types.ConquestEvent;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import gg.hcfactions.libs.base.timer.impl.GenericTimer;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.remap.ERemappedEffect;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collections;
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

            else if (plugin.getClassManager().getCurrentClass(player) != null) {
                final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

                if (playerClass.getConsumables().stream().anyMatch(c -> c.hasCooldown(player))) {
                    hasUI = true;
                }

                if (playerClass instanceof Tank) {
                    hasUI = true;
                }
            }

            if (!hasUI) {
                if (fp.getScoreboard() != null && !fp.getScoreboard().isHidden()) {
                    fp.getScoreboard().hide();
                }
            } else {
                renderSidebar(player, fp);
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

    private void renderSidebar(Player player, FactionPlayer factionPlayer) {
        final CXService commandXService = (CXService)plugin.getService(CXService.class);
        final String SPACER = ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 24);
        boolean hasEntries = false;

        for (FTimer rt : factionPlayer.getTimers().stream().filter(t -> t.getType().isRender()).collect(Collectors.toList())) {
            final String time = (rt.getType().isDecimal() && rt.getRemainingSeconds() < 10)
                    ? Time.convertToDecimal(rt.getRemaining()) + "s"
                    : Time.convertToHHMMSS(rt.getRemaining());

            factionPlayer.getScoreboard().setLine(
                    rt.getType().getScoreboardPosition(),
                    rt.getType().getDisplayName() + ChatColor.RED + ": " + time
            );

            hasEntries = true;
        }

        int eventCursor = 16;
        for (KOTHEvent kothEvent : plugin.getEventManager().getActiveKothEvents()) {
            if (eventCursor >= 19) {
                break;
            }

            if (kothEvent.getSession() == null) {
                continue;
            }

            final long remainingMillis = kothEvent.getSession().getTimer().getRemaining();
            final int remainingSeconds = (int)(remainingMillis/1000L);
            int capturingFactionsTickets = 0;

            if (kothEvent.getSession().getCapturingFaction() != null) {
                capturingFactionsTickets = kothEvent.getSession().getLeaderboard().getOrDefault(kothEvent.getSession().getCapturingFaction().getUniqueId(), 0);
            }

            String displayed = (remainingSeconds < 10 ? Time.convertToDecimal(remainingMillis) + "s" : Time.convertToHHMMSS(remainingMillis));

            if (kothEvent.getSession().getTicketsNeededToWin() > 1 && capturingFactionsTickets > 0) {
                displayed = displayed + ChatColor.BLUE + " [" + capturingFactionsTickets + "]";
            }

            if (remainingMillis <= 0) {
                displayed = "Capturing...";
            }

            else if (kothEvent.getSession().isContested()) {
                displayed = "Contested";
            }

            factionPlayer.getScoreboard().setLine(eventCursor, kothEvent.getDisplayName() + ChatColor.RED + ": " + displayed);
            eventCursor += 1;
            hasEntries = true;
        }

        final ConquestEvent conquestEvent = plugin.getEventManager().getActiveConquestEvent();
        if (conquestEvent != null) {
            final String indent = ChatColor.RESET + " " + ChatColor.RESET + " ";
            factionPlayer.getScoreboard().setLine(23, conquestEvent.getDisplayName());

            int conquestCursor = 19;

            List<ConquestZone> zones = conquestEvent.getZonesByAlphabetical();
            Collections.reverse(zones);

            for (ConquestZone zone : zones) {
                final long remainingMillis = zone.getTimer().getRemaining();
                final long remainingSeconds = zone.getTimer().getRemainingSeconds();
                String displayed = (remainingSeconds < 10 ? Time.convertToDecimal(remainingMillis) + "s" : Time.convertToHHMMSS(remainingMillis));

                if (remainingMillis <= 0) {
                    displayed = "Capturing...";
                }

                else if (zone.isContested()) {
                    displayed = "Contested";
                }

                factionPlayer.getScoreboard().setLine(conquestCursor, indent + zone.getDisplayName() + ChatColor.RED + ": " + displayed);
                conquestCursor += 1;
                hasEntries = true;
            }
        }

        if (commandXService != null) {
            if (commandXService.getVanishManager().isVanished(player)) {
                factionPlayer.getScoreboard().setLine(24, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Vanished");
                hasEntries = true;
            } else {
                factionPlayer.getScoreboard().removeLine(24);
            }

            if (commandXService.getRebootModule().isEnabled() && commandXService.getRebootModule().isRebootInProgress()) {
                factionPlayer.getScoreboard().setLine(25, ChatColor.DARK_RED + "" + ChatColor.BOLD + "" + "Restart" + ChatColor.RED + ": "  + Time.convertToHHMMSS(commandXService.getRebootModule().getTimeUntilReboot()));
                hasEntries = true;
            } else {
                factionPlayer.getScoreboard().removeLine(25);
            }
        }

        if (plugin.getClassManager().getCurrentClass(player) != null) {
            final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

            if (playerClass instanceof final Tank tankClass) {
                factionPlayer.getScoreboard().setLine(53, ChatColor.AQUA + "" + ChatColor.BOLD + "Stamina" + ChatColor.RED + ": " + String.format("%.2f", tankClass.getStamina(player)));
            }

            if (playerClass.getConsumables().stream().anyMatch(c -> c.getCooldowns().containsKey(player.getUniqueId()))) {
                factionPlayer.getScoreboard().setLine(52, ChatColor.GOLD + "" + ChatColor.BOLD + playerClass.getName() + " Effects" + ChatColor.YELLOW + ":");

                if (hasEntries) {
                    factionPlayer.getScoreboard().setLine(29, ChatColor.RESET + " " + ChatColor.RESET + " ");
                } else {
                    factionPlayer.getScoreboard().removeLine(29);
                }
            }

            playerClass.getConsumables().stream().filter(c -> c.getCooldowns().containsKey(player.getUniqueId())).forEach(cd -> {
                final ERemappedEffect remapped = ERemappedEffect.getRemappedEffect(cd.getEffectType());
                final EEffectScoreboardMapping mapping = EEffectScoreboardMapping.getByRemappedEffect(remapped);
                final String effectName = StringUtils.capitalize(remapped.name().toLowerCase().replaceAll("_", " "));
                final long remainingTime = cd.getCooldowns().get(player.getUniqueId()) - Time.now();
                final int remainingSeconds = (int)remainingTime / 1000;

                // we do not set hasEntries here
                if (mapping != null) {
                    factionPlayer.getScoreboard().setLine(mapping.getScoreboardPosition(), ChatColor.RESET + " " + ChatColor.RESET + " " + mapping.getColor() + "" + net.md_5.bungee.api.ChatColor.BOLD
                            + effectName + ChatColor.RED + ": " + (remainingSeconds > 10 ? Time.convertToHHMMSS(remainingTime) : Time.convertToDecimal(remainingTime) + "s"));
                }
            });
        }

        factionPlayer.getScoreboard().setLine(0, SPACER);
        factionPlayer.getScoreboard().setLine(2, ChatColor.RESET + " ");
        factionPlayer.getScoreboard().setLine(1, plugin.getConfiguration().getScoreboardFooter());
        factionPlayer.getScoreboard().setLine(63, SPACER + ChatColor.RESET);
    }
}
