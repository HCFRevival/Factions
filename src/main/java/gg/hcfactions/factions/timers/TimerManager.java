package gg.hcfactions.factions.timers;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import gg.hcfactions.cx.CXService;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.classes.EEffectScoreboardMapping;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.classes.impl.Tank;
import gg.hcfactions.factions.models.events.impl.ConquestZone;
import gg.hcfactions.factions.models.events.impl.types.ConquestEvent;
import gg.hcfactions.factions.models.events.impl.types.DPSEvent;
import gg.hcfactions.factions.models.events.impl.types.KOTHEvent;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.factions.models.timer.ETimerType;
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
import java.util.Objects;
import java.util.UUID;
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

        uiTask = new Scheduler(plugin).async(() -> plugin.getPlayerManager().getPlayerRepository().forEach(fp -> {
            boolean hasUI = false;

            if (!fp.getTimers().isEmpty()) {
                hasUI = true;
            }

            else if (commandXService.getRebootModule().isEnabled() && commandXService.getRebootModule().isRebootInProgress()) {
                hasUI = true;
            }

            else if (commandXService.getVanishManager().isVanished(fp.getUniqueId())) {
                hasUI = true;
            }

            else if (!plugin.getEventManager().getActiveEvents().isEmpty()) {
                hasUI = true;
            }

            else if (plugin.getClassManager().getCurrentClass(fp.getUniqueId()) != null) {
                final IClass playerClass = plugin.getClassManager().getCurrentClass(fp.getUniqueId());

                if (playerClass.getConsumables().stream().anyMatch(c -> c.hasCooldown(fp.getUniqueId()))) {
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
                renderSidebar((FactionPlayer) fp);
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

    private void renderSidebar(FactionPlayer factionPlayer) {
        final Player player = Bukkit.getPlayer(factionPlayer.getUniqueId());

        if (player == null || factionPlayer.getScoreboard() == null) {
            return;
        }

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

        int eventCursor = 17;
        for (KOTHEvent kothEvent : plugin.getEventManager().getActiveKothEvents()) {
            if (eventCursor >= 19) {
                break;
            }

            if (kothEvent.getSession() == null) {
                continue;
            }

            final long remainingMillis = kothEvent.getSession().getTimer().getRemaining();
            final int remainingSeconds = (int)(remainingMillis/1000L);
            String timerDisplay = (remainingSeconds < 10 ? Time.convertToDecimal(remainingMillis) + "s" : Time.convertToHHMMSS(remainingMillis));
            String factionDisplay = null;

            if (remainingMillis <= 0) {
                timerDisplay = "Capturing...";
            }

            else if (kothEvent.getSession().isContested()) {
                timerDisplay = "Contested";
            }

            if (kothEvent.getSession().getCapturingFaction() != null) {
                final PlayerFaction capturingFaction = kothEvent.getSession().getCapturingFaction();
                final int tickets = kothEvent.getSession().getTickets(capturingFaction);
                final List<Integer> tickCheckpoints = kothEvent.getSession().getTickCheckpoints();
                final boolean isFriendly = capturingFaction.isMember(factionPlayer.getUniqueId());
                final String factionName = capturingFaction.getName().length() > 10 ? capturingFaction.getName().substring(0, 10) + "..." : capturingFaction.getName();
                final List<String> tickDisplay = Lists.newArrayList();

                for (int checkpoint : tickCheckpoints) {
                    if (tickets >= checkpoint) {
                        tickDisplay.add(ChatColor.GREEN + "[✔]");
                        continue;
                    }

                    tickDisplay.add(ChatColor.GRAY + "[■]");
                }

                factionDisplay = (isFriendly ? ChatColor.DARK_GREEN : ChatColor.RED) + "" + ChatColor.BOLD + factionName + " "
                        + (tickDisplay.isEmpty() ? "" : Joiner.on(ChatColor.RESET + "").join(tickDisplay) + " ") + ChatColor.BLUE + "(" + tickets + ")";
            }

            factionPlayer.getScoreboard().setLine(eventCursor, kothEvent.getDisplayName() + ChatColor.RED + ": " + timerDisplay);

            if (factionDisplay != null) {
                factionPlayer.getScoreboard().setLine((eventCursor - 1), factionDisplay);
            } else {
                factionPlayer.getScoreboard().removeLine((eventCursor - 1));
            }

            eventCursor += 2;
            hasEntries = true;
        }

        for (DPSEvent dpsEvent : plugin.getEventManager().getActiveDpsEvents()) {
            if (eventCursor >= 19) {
                break;
            }

            if (dpsEvent.getSession() == null) {
                continue;
            }

            final long remainingMillis = dpsEvent.getSession().getRemainingTime();
            final int remainingSeconds = (int)(remainingMillis/1000L);
            PlayerFaction mostRecentFaction = null;
            PlayerFaction topFaction = null;
            String timerDisplay = (remainingSeconds < 10 ? Time.convertToDecimal(remainingMillis) + "s" : Time.convertToHHMMSS(remainingMillis));
            String mostRecentFactionDisplay = null;
            String topFactionDisplay = null;

            if (remainingMillis <= 0) {
                timerDisplay = "Capturing...";
            }

            if (dpsEvent.getSession().getMostRecentDamager() != null) {
                mostRecentFaction = dpsEvent.getSession().getMostRecentDamager();
                final long damage = dpsEvent.getSession().getDamage(mostRecentFaction);
                final boolean isFriendly = mostRecentFaction.isMember(factionPlayer.getUniqueId());
                final String factionName = mostRecentFaction.getName().length() > 10 ? mostRecentFaction.getName().substring(0, 10) + "..." : mostRecentFaction.getName();
                final String formattedValue = String.format("%,d", damage);

                mostRecentFactionDisplay = ChatColor.WHITE + "\uD83D\uDDE1" + " " +
                        (isFriendly ? ChatColor.DARK_GREEN : ChatColor.RED) + "" + ChatColor.BOLD + factionName + " "
                        + ChatColor.BLUE + "(" + ChatColor.AQUA + formattedValue + ChatColor.BLUE + ")";
            }

            if (!dpsEvent.getSession().getLeaderboard().isEmpty()) {
                final UUID topFactionId = dpsEvent.getSession().getSortedLeaderboard().keySet().stream().findFirst().orElse(null);

                if (topFactionId != null) {
                    topFaction = plugin.getFactionManager().getPlayerFactionById(topFactionId);
                    final long damage = dpsEvent.getSession().getDamage(topFaction);
                    final boolean isFriendly = topFaction.isMember(factionPlayer.getUniqueId());
                    final boolean isMostRecent = (mostRecentFaction != null && mostRecentFaction.getUniqueId().equals(topFactionId));
                    final String factionName = topFaction.getName().length() > 10 ? topFaction.getName().substring(0, 10) + "..." : topFaction.getName();
                    final String formattedValue = String.format("%,d", damage);

                    topFactionDisplay = ChatColor.GOLD + "①" + (isMostRecent ? ChatColor.WHITE + "\uD83D\uDDE1" : "") +
                            " " + (isFriendly ? ChatColor.DARK_GREEN : ChatColor.RED) + "" + ChatColor.BOLD + factionName + " " +
                            ChatColor.BLUE + "(" + ChatColor.AQUA + formattedValue + ChatColor.BLUE + ")";
                }
            }

            factionPlayer.getScoreboard().setLine(eventCursor, dpsEvent.getDisplayName() + ChatColor.RED + ": " + timerDisplay);

            if (topFactionDisplay != null) {
                factionPlayer.getScoreboard().setLine((eventCursor - 1), topFactionDisplay);
            } else {
                factionPlayer.getScoreboard().removeLine((eventCursor - 1));
            }

            if (mostRecentFactionDisplay != null && !Objects.requireNonNull(topFaction).getUniqueId().equals(mostRecentFaction.getUniqueId())) {
                factionPlayer.getScoreboard().setLine((eventCursor - 2), mostRecentFactionDisplay);
            } else {
                factionPlayer.getScoreboard().removeLine((eventCursor - 2));
            }

            eventCursor += 2;
            hasEntries = true;
        }

        final ConquestEvent conquestEvent = plugin.getEventManager().getActiveConquestEvent().stream().findFirst().orElse(null);
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
            if (commandXService.getVanishManager().isVanished(factionPlayer.getUniqueId())) {
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
                // we need this here as a catch-all since scoreboard iteration is
                // asynchronous and guard can be caught in a race condition
                if (!factionPlayer.hasTimer(ETimerType.GUARD) && factionPlayer.getScoreboard().getLine(ETimerType.GUARD.getScoreboardPosition()) != null) {
                    factionPlayer.getScoreboard().removeLine(ETimerType.GUARD.getScoreboardPosition());
                }

                factionPlayer.getScoreboard().setLine(53, ChatColor.AQUA + "" + ChatColor.BOLD + "Stamina" + ChatColor.RED + ": " + String.format("%.2f", tankClass.getStamina(factionPlayer.getUniqueId())));
            }

            if (playerClass.getConsumables().stream().anyMatch(c -> c.getCooldowns().containsKey(factionPlayer.getUniqueId()))) {
                factionPlayer.getScoreboard().setLine(52, ChatColor.GOLD + "" + ChatColor.BOLD + playerClass.getName() + " Effects" + ChatColor.YELLOW + ":");

                if (hasEntries) {
                    factionPlayer.getScoreboard().setLine(29, ChatColor.RESET + " " + ChatColor.RESET + " ");
                } else {
                    factionPlayer.getScoreboard().removeLine(29);
                }
            }

            playerClass.getConsumables().stream().filter(c -> c.getCooldowns().containsKey(factionPlayer.getUniqueId())).forEach(cd -> {
                final ERemappedEffect remapped = ERemappedEffect.getRemappedEffect(cd.getEffectType());
                final EEffectScoreboardMapping mapping = EEffectScoreboardMapping.getByRemappedEffect(remapped);
                final String effectName = StringUtils.capitalize(remapped.name().toLowerCase().replaceAll("_", " "));
                final long remainingTime = cd.getCooldowns().getOrDefault(factionPlayer.getUniqueId(), 0L) - Time.now();
                final int remainingSeconds = (int)remainingTime / 1000;

                // we do not set hasEntries here
                if (mapping != null && remainingTime > 0) {
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
