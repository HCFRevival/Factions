package gg.hcfactions.factions.timers;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import gg.hcfactions.cx.CXService;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.events.EventManager;
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

import java.util.*;

public final class TimerManager implements IManager {
    @Getter public final Factions plugin;
    @Getter @Setter public BukkitTask updateTask;
    @Getter @Setter public BukkitTask uiTask;

    public TimerManager(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        uiTask = new Scheduler(plugin).async(() -> plugin.getPlayerManager().getPlayerRepository().forEach(fp -> renderSidebarV2((FactionPlayer) fp))).repeat(0L, 1L).run();

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

    /*
        63 ---------------------------
        62 CONQUEST NAME or DPS NAME or KOTH NAME
        61 CONQUEST ENTRY or DPS LEADER or KOTH Capturer
        60 CONQUEST ENTRY or DPS CURRENT
        59 CONQUEST ENTRY
        58 EVENT RESERVED
        57 EVENT RESERVED
        56 EMPTY SPACE
        55 CLASS COOLDOWNS
        54 <class effect reserved>
        53 <class effect reserved>
        52 <class effect reserved>
        51 <class effect reserved>
        50 <class effect reserved>
        49 <class effect reserved>
        48 <class effect reserved>
        47 <class effect reserved>
        46 <class effect reserved>
        45 <class effect reserved>
        44 <class effect reserved>
        43 <class effect reserved>
        42 <class effect reserved>
        41 <class effect reserved>
        40 <class effect reserved>
        39 <class effect reserved>
        38 <class effect reserved>
        37 <class effect reserved>
        36 <class effect reserved>
        35 <class effect reserved>
        34 <class effect reserved>
        33 <EMPTY SPACE>
        32
        31
        30
        29
        28
        27
        26
        25
        24
        23
        22
        21
        20
        19
        19 CommandX Reboot
        18 CommandX Vanish
        17 Protection
        16 Combat
        15 Class
        14 Enderpearl
        13 Chorus Fruit
        12 Logout
        11 Home
        10 Stuck
        9 Stamina
        8 Guard or Grapple or Trident
        7 Archer Mark
        6 Crapple
        5 Totem
        4 Gapple
        3
        2 <empty space>
        1 play.hcfrevival.net
        0 ----------------------------
     */

    private void getScoreboardLayoutEntries(Map<Integer, String> res) {
        final String SPACER = ChatColor.GRAY + "" + ChatColor.STRIKETHROUGH + StringUtils.repeat('-', 24);
        res.put(0, SPACER);
        res.put(2, ChatColor.RESET + " ");
        res.put(1, plugin.getConfiguration().getScoreboardFooter());
        res.put(63, SPACER + ChatColor.RESET);
    }

    private void getTimerScoreboardEntries(Player player, Map<Integer, String> res) {
        final FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);

        if (factionPlayer == null) {
            return;
        }

        factionPlayer.getTimers().stream().filter(t -> t.getType().isRender()).forEach(timer -> {
            final String time = (timer.getType().isDecimal() && timer.getRemainingSeconds() < 10)
                    ? Time.convertToDecimal(timer.getRemaining()) + "s"
                    : Time.convertToHHMMSS(timer.getRemaining());

            res.put(timer.getType().getScoreboardPosition(), timer.getType().getDisplayName() + ChatColor.RED + ": " + time);
        });
    }

    private void getClassScoreboardEntries(Player player, Map<Integer, String> res) {
        final IClass playerClass = plugin.getClassManager().getCurrentClass(player);

        if (playerClass == null) {
            return;
        }

        if (playerClass.getConsumables().stream().noneMatch(c -> c.getCooldowns().containsKey(player.getUniqueId()))) {
            return;
        }

        if (res.size() > 4) {
            res.put(32, ChatColor.RESET + " " + ChatColor.RESET + " ");
        }

        res.put(55, ChatColor.GOLD + "" + ChatColor.BOLD + playerClass.getName() + " Cooldowns");

        playerClass.getConsumables().stream().filter(c -> c.getCooldowns().containsKey(player.getUniqueId())).forEach(consumeable -> {
            final ERemappedEffect remapped = ERemappedEffect.getRemappedEffect(consumeable.getEffectType());
            final EEffectScoreboardMapping mapping = EEffectScoreboardMapping.getByRemappedEffect(remapped);
            final String effectName = StringUtils.capitalize(remapped.name().toLowerCase().replaceAll("_", " "));
            final long remainingTime = consumeable.getCooldowns().getOrDefault(player.getUniqueId(), 0L) - Time.now();
            final int remainingSeconds = (int)remainingTime / 1000;

            if (mapping != null && remainingTime > 0) {
                res.put(mapping.getScoreboardPosition(), ChatColor.RESET + " " + ChatColor.RESET + " " + mapping.getColor() + "" + net.md_5.bungee.api.ChatColor.BOLD
                        + effectName + ChatColor.RED + ": " + (remainingSeconds > 10 ? Time.convertToHHMMSS(remainingTime) : Time.convertToDecimal(remainingTime) + "s"));
            }
        });

        if (playerClass instanceof Tank tankClass) {
            res.put(9, ChatColor.BLUE + "" + ChatColor.BOLD + "Stamina" + ChatColor.RED + ": " + tankClass.getStamina(player));
        }
    }

    private void getServiceScoreboardEntries(Player player, Map<Integer, String> res) {
        final CXService cxService = (CXService) plugin.getService(CXService.class);

        if (cxService == null) {
            return;
        }

        if (cxService.getVanishManager().isVanished(player.getUniqueId())) {
            res.put(18, ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Vanished");
        }

        if (cxService.getRebootModule().isEnabled() && cxService.getRebootModule().isRebootInProgress()) {
            res.put(19, ChatColor.DARK_RED + "" + ChatColor.BOLD + "" + "Restart" + ChatColor.RED + ": "  + Time.convertToHHMMSS(cxService.getRebootModule().getTimeUntilReboot()));
        }

    }

    private void getEventScoreboardEntries(Player player, Map<Integer, String> res) {
        final EventManager em = plugin.getEventManager();
        final boolean shouldAddSpacer = (res.size() > 4);
        int cursor = 62;

        for (KOTHEvent kothEvent : em.getActiveKothEvents()) {
            if (cursor <= 57) {
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
                final boolean isFriendly = capturingFaction.isMember(player.getUniqueId());
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

            res.put(cursor, kothEvent.getDisplayName() + ChatColor.RED + ": " + timerDisplay);
            cursor -= 1;

            if (factionDisplay != null) {
                res.put(cursor, factionDisplay);
                cursor -= 1;
            }
        }

        for (DPSEvent dpsEvent : plugin.getEventManager().getActiveDpsEvents()) {
            if (cursor <= 57) {
                break;
            }

            if (dpsEvent.getSession() == null) {
                continue;
            }

            final long remainingMillis = dpsEvent.getSession().getRemainingTime();
            final int remainingSeconds = (int) (remainingMillis / 1000L);
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
                final boolean isFriendly = mostRecentFaction.isMember(player.getUniqueId());
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
                    final boolean isFriendly = topFaction.isMember(player.getUniqueId());
                    final boolean isMostRecent = (mostRecentFaction != null && mostRecentFaction.getUniqueId().equals(topFactionId));
                    final String factionName = topFaction.getName().length() > 10 ? topFaction.getName().substring(0, 10) + "..." : topFaction.getName();
                    final String formattedValue = String.format("%,d", damage);

                    topFactionDisplay = ChatColor.GOLD + "①" + (isMostRecent ? ChatColor.WHITE + "\uD83D\uDDE1" : "") +
                            " " + (isFriendly ? ChatColor.DARK_GREEN : ChatColor.RED) + "" + ChatColor.BOLD + factionName + " " +
                            ChatColor.BLUE + "(" + ChatColor.AQUA + formattedValue + ChatColor.BLUE + ")";
                }
            }

            res.put(cursor, dpsEvent.getDisplayName() + ChatColor.RED + ": " + timerDisplay);
            cursor -= 1;

            if (topFactionDisplay != null) {
                res.put(cursor, topFactionDisplay);
                cursor -= 1;
            }

            if (mostRecentFactionDisplay != null && !Objects.requireNonNull(topFaction).getUniqueId().equals(mostRecentFaction.getUniqueId())) {
                res.put(cursor, mostRecentFactionDisplay);
                cursor -= 1;
            }
        }

        final ConquestEvent conquestEvent = plugin.getEventManager().getActiveConquestEvent().stream().findFirst().orElse(null);
        if (conquestEvent != null) {
            final String indent = ChatColor.RESET + " " + ChatColor.RESET + " ";
            res.put(cursor, conquestEvent.getDisplayName());
            cursor -= 1;

            List<ConquestZone> zones = conquestEvent.getZonesByAlphabetical();
            Collections.reverse(zones);

            for (ConquestZone zone : zones) {
                if (cursor <= 57) {
                    break;
                }

                final long remainingMillis = zone.getTimer().getRemaining();
                final long remainingSeconds = zone.getTimer().getRemainingSeconds();
                String displayed = (remainingSeconds < 10 ? Time.convertToDecimal(remainingMillis) + "s" : Time.convertToHHMMSS(remainingMillis));

                if (remainingMillis <= 0) {
                    displayed = "Capturing...";
                }

                else if (zone.isContested()) {
                    displayed = "Contested";
                }

                res.put(cursor, indent + zone.getDisplayName() + ChatColor.RED + ": " + displayed);
                cursor -= 1;
            }
        }

        if (cursor != 62 && shouldAddSpacer) {
            res.put(56, ChatColor.RESET + " " + ChatColor.RESET + " ");
        }
    }

    private void renderSidebarV2(FactionPlayer factionPlayer) {
        final Player player = Bukkit.getPlayer(factionPlayer.getUniqueId());

        if (player == null || factionPlayer.getScoreboard() == null) {
            return;
        }

        final Map<Integer, String> entries = Maps.newHashMap();
        getScoreboardLayoutEntries(entries);
        getTimerScoreboardEntries(player, entries);
        getClassScoreboardEntries(player, entries);
        getServiceScoreboardEntries(player, entries);
        getEventScoreboardEntries(player, entries);

        if (entries.size() <= 4 && factionPlayer.getScoreboard() != null) {
            if (!factionPlayer.getScoreboard().isHidden()) {
                factionPlayer.getScoreboard().hide();
            }

            return;
        }

        for (int i = 0; i < 64; i++) {
            if (!entries.containsKey(i)) {
                factionPlayer.getScoreboard().removeLine(i);
                continue;
            }

            final String entry = entries.get(i);
            factionPlayer.getScoreboard().setLine(i, entry);
        }
    }
}
