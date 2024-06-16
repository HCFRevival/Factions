package gg.hcfactions.factions.models.anticlean;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.EScoreboardEntryType;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.libs.base.util.Time;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
public final class AnticleanSession {
    public final Factions plugin;
    public Set<PlayerFaction> factions;
    @Setter public ESessionStatus status;
    @Setter public long startTime;
    @Setter public long expireTime;

    public AnticleanSession(Factions plugin) {
        this.plugin = plugin;
        this.factions = Sets.newHashSet();
        this.status = ESessionStatus.PENDING;
        this.expireTime = -1L;
        this.startTime = (Time.now() + 30 * 1000L);
    }

    public boolean isMember(Player player) {
        return isMember(player.getUniqueId());
    }

    public boolean isMember(UUID id) {
        return factions.stream().anyMatch(f -> f.isMember(id));
    }

    public boolean isMember(PlayerFaction faction) {
        return factions.contains(faction);
    }

    public boolean isExpired() {
        return (status == ESessionStatus.ACTIVE && expireTime <= Time.now());
    }

    public long getRemainingTime() {
        if (status == ESessionStatus.PENDING) {
            return startTime - Time.now();
        }

        return expireTime - Time.now();
    }

    public List<UUID> getMembers() {
        List<UUID> res = Lists.newArrayList();
        factions.forEach(f -> f.getOnlineMembers().forEach(om -> res.add(om.getUniqueId())));
        return res;
    }

    public List<String> getFactionNames() {
        List<String> res = Lists.newArrayList();
        factions.forEach(f -> res.add(f.getName()));
        return res;
    }

    public List<Player> getPlayers() {
        List<Player> players = Lists.newArrayList();

        factions.forEach(f -> f.getOnlineMembers().forEach(om -> {
            Player onlinePlayer = om.getBukkit();

            if (onlinePlayer != null) {
                players.add(onlinePlayer);
            }
        }));

        return players;
    }

    public void sendMessage(Component component) {
        getMembers().forEach(id -> {
            Player player = Bukkit.getPlayer(id);

            if (player != null) {
                player.sendMessage(component);
            }
        });
    }

    public void update(Player player) {
        List<UUID> members = getMembers();
        boolean isMember = members.contains(player.getUniqueId());

        if (isMember) {
            Bukkit.getOnlinePlayers().forEach(onlinePlayer -> {
                if (!members.contains(onlinePlayer.getUniqueId()) && !onlinePlayer.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
                    FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(onlinePlayer);
                    PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(onlinePlayer);

                    if (factionPlayer != null && (faction == null || faction.getOnlineMembers().size() <= plugin.getConfiguration().getObfuscationMinFacSize())) {
                        factionPlayer.addToScoreboard(player, EScoreboardEntryType.OBFUSCATED);
                    }
                }
            });

            return;
        }

        if (player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
            return;
        }

        FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);
        PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player);

        if (faction != null && faction.getOnlineMembers().size() > plugin.getConfiguration().getObfuscationMinFacSize()) {
            return;
        }

        members.forEach(obfuscatedUUID -> {
            Player obfuscatedPlayer = Bukkit.getPlayer(obfuscatedUUID);

            if (obfuscatedPlayer != null) {
                factionPlayer.addToScoreboard(obfuscatedPlayer, EScoreboardEntryType.OBFUSCATED);
            }
        });
    }

    public void init() {
        List<UUID> members = getMembers();

        Bukkit.getOnlinePlayers().forEach(player -> {
            if (!members.contains(player.getUniqueId()) && !player.hasPermission(FPermissions.P_FACTIONS_ADMIN)) {
                FactionPlayer factionPlayer = (FactionPlayer) plugin.getPlayerManager().getPlayer(player);
                PlayerFaction faction = plugin.getFactionManager().getPlayerFactionByPlayer(player);

                if (factionPlayer != null && (faction == null || faction.getOnlineMembers().size() <= plugin.getConfiguration().getObfuscationMinFacSize())) {
                    members.forEach(obfuscatedUUID -> {
                        Player obfuscatedPlayer = Bukkit.getPlayer(obfuscatedUUID);

                        if (obfuscatedPlayer != null) {
                            factionPlayer.addToScoreboard(obfuscatedPlayer, EScoreboardEntryType.OBFUSCATED);
                        }
                    });
                }
            }
        });

        getPlayers().forEach(p -> FMessage.printObfuscationStarted(p, getFactionNames(), plugin.getConfiguration().getObfuscationDuration()));
        expireTime = Time.now() + (plugin.getConfiguration().getObfuscationDuration()*1000L);
        status = ESessionStatus.ACTIVE;
    }

    public void close() {
        List<UUID> members = getMembers();

        plugin.getPlayerManager().getPlayerRepository().forEach(factionPlayer -> members.forEach(obfuscatedUUID -> {
            Player obfuscatedPlayer = Bukkit.getPlayer(obfuscatedUUID);

            if (obfuscatedPlayer != null) {
                factionPlayer.removeFromScoreboard(obfuscatedPlayer, EScoreboardEntryType.OBFUSCATED);
            }
        }));

        getPlayers().forEach(FMessage::printObfuscationEnded);
    }

    public enum ESessionStatus {
        PENDING, ACTIVE
    }
}
