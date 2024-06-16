package gg.hcfactions.factions.anticlean;

import com.google.common.collect.Sets;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.anticlean.AnticleanSession;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Getter
public class AnticleanManager implements IManager {
    public final Factions plugin;
    public final Set<AnticleanSession> sessionRepository;
    public BukkitTask sessionTickingTask;

    public AnticleanManager(Factions plugin) {
        this.plugin = plugin;
        this.sessionRepository = Sets.newConcurrentHashSet();
    }

    public Optional<AnticleanSession> getSession(Player player) {
        return sessionRepository.stream().filter(s -> s.isMember(player)).findAny();
    }

    public Optional<AnticleanSession> getSession(PlayerFaction faction) {
        return sessionRepository.stream().filter(s -> s.isMember(faction)).findAny();
    }

    public List<AnticleanSession> getActiveSessions() {
        return sessionRepository.stream().filter(s -> !s.isExpired()).toList();
    }

    public void mergeOrCreateSession(Player attacker, Player attacked) {
        PlayerFaction attackerFaction = plugin.getFactionManager().getPlayerFactionByPlayer(attacker);
        PlayerFaction attackedFaction = plugin.getFactionManager().getPlayerFactionByPlayer(attacked);

        if (attackerFaction == null || attackedFaction == null) {
            return;
        }

        if (
                attackerFaction.getOnlineMembers().size() <= plugin.getConfiguration().getObfuscationMinFacSize()
            || attackedFaction.getOnlineMembers().size() <= plugin.getConfiguration().getObfuscationMinFacSize()
        ) {
            return;
        }

        Optional<AnticleanSession> attackerSessionQuery = getSession(attackerFaction);
        Optional<AnticleanSession> attackedSessionQuery = getSession(attackedFaction);

        if (attackerSessionQuery.isPresent()) {
            AnticleanSession attackerSession = attackerSessionQuery.get();

            if (attackerSession.getStatus().equals(AnticleanSession.ESessionStatus.ACTIVE) && attackerSession.isMember(attackedFaction)) {
                attackerSession.setExpireTime((Time.now() + (60 * 1000L)));
            }

            if (attackerSession.getStatus().equals(AnticleanSession.ESessionStatus.PENDING) && !attackerSession.isMember(attackedFaction)) {
                if (attackedSessionQuery.isPresent()) {
                    AnticleanSession attackedSession = attackedSessionQuery.get();

                    if (attackedSession.getStatus().equals(AnticleanSession.ESessionStatus.PENDING)) {
                        attackedSession.getFactions().forEach(f -> attackerSession.getFactions().add(f));
                        sessionRepository.remove(attackedSession);
                    }
                } else {
                    attackerSession.getFactions().add(attackedFaction);
                }
            }

            return;
        }

        if (attackedSessionQuery.isPresent()) {
            AnticleanSession attackedSession = attackedSessionQuery.get();

            if (attackedSession.getStatus().equals(AnticleanSession.ESessionStatus.ACTIVE) && attackedSession.isMember(attackerFaction)) {
                attackedSession.setExpireTime((Time.now() + (60 * 1000L)));
            }

            if (attackedSession.getStatus().equals(AnticleanSession.ESessionStatus.PENDING) && !attackedSession.isMember(attackerFaction)) {
                attackedSession.getFactions().add(attackerFaction);
            }

            return;
        }

        AnticleanSession newSession = new AnticleanSession(plugin);
        newSession.getFactions().add(attackerFaction);
        newSession.getFactions().add(attackedFaction);
        sessionRepository.add(newSession);
    }

    @Override
    public void onEnable() {
        sessionTickingTask = new Scheduler(plugin).async(() -> {
            sessionRepository.stream().filter(s -> s.getStartTime() <= Time.now() && s.getStatus().equals(AnticleanSession.ESessionStatus.PENDING)).forEach(readySession -> new Scheduler(plugin).sync(readySession::init).run());

            sessionRepository.stream().filter(AnticleanSession::isExpired).forEach(expiredSession -> new Scheduler(plugin).sync(() -> {
                expiredSession.close();
                sessionRepository.remove(expiredSession);
            }).run());
        }).repeat(0L, 10L).run();
    }

    @Override
    public void onDisable() {
        sessionTickingTask.cancel();
        sessionTickingTask = null;
        sessionRepository.clear();
    }
}
