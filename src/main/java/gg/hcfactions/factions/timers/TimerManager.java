package gg.hcfactions.factions.timers;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.libs.base.timer.impl.GenericTimer;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public final class TimerManager implements IManager {
    @Getter public final Factions plugin;
    @Getter @Setter public BukkitTask updateTask;
    @Getter @Setter public BukkitTask uiTask;

    public TimerManager(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        uiTask = new Scheduler(plugin).sync(() -> {
            plugin.getPlayerManager().getPlayerRepository().stream().filter(p -> !p.getTimers().isEmpty()).forEach(p -> {
                final List<String> toRender = Lists.newArrayList();

                p.getTimers().stream().filter(t -> t.getType().isRender()).forEach(rt -> {
                    final String time = (rt.getType().isDecimal() && rt.getRemainingSeconds() < 10)
                            ? Time.convertToDecimal(rt.getRemaining()) + "s"
                            : Time.convertToHHMMSS(rt.getRemaining());

                    toRender.add(rt.getType().getDisplayName() + ChatColor.RED + ": " + time);
                });

                // TODO: Append event timers here
                // TODO: Append automatic reboots here

                final Player player = ((FactionPlayer)p).getBukkit();
                if (!toRender.isEmpty() && player != null) {
                    final String hud = Joiner.on(ChatColor.RESET + " " + ChatColor.RESET + " ").join(toRender);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(hud));
                }
            });
        }).repeat(0L, 1L).run();

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
}
