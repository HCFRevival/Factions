package gg.hcfactions.factions.models.player.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.listeners.events.player.TankShieldReadyEvent;
import gg.hcfactions.factions.models.claim.EClaimPillarType;
import gg.hcfactions.factions.models.claim.EShieldType;
import gg.hcfactions.factions.models.claim.IPillar;
import gg.hcfactions.factions.models.claim.IShield;
import gg.hcfactions.factions.models.claim.impl.*;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.classes.impl.Tank;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.EScoreboardEntryType;
import gg.hcfactions.factions.models.player.IFactionPlayer;
import gg.hcfactions.factions.models.scoreboard.FScoreboard;
import gg.hcfactions.factions.player.PlayerManager;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import gg.hcfactions.factions.utils.FactionUtil;
import gg.hcfactions.libs.base.connect.impl.mongo.MongoDocument;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class FactionPlayer implements IFactionPlayer, MongoDocument<FactionPlayer> {
    @Getter public final transient PlayerManager playerManager;
    @Getter @Setter public transient String username;
    @Getter @Setter public transient boolean safeDisconnect;
    @Getter @Setter public transient Claim currentClaim;
    @Getter public FScoreboard scoreboard;
    @Getter public final Set<IPillar> pillars;
    @Getter public final Set<IShield> shields;

    @Getter public UUID uniqueId;
    @Getter @Setter public double balance;
    @Getter @Setter public boolean resetOnJoin;
    @Getter public Set<FTimer> timers;

    public FactionPlayer(PlayerManager playerManager) {
        this.playerManager = playerManager;
        this.username = null;
        this.safeDisconnect = false;
        this.currentClaim = null;
        this.uniqueId = null;
        this.balance = playerManager.getPlugin().getConfiguration().getStartingBalance();
        this.resetOnJoin = false;
        this.scoreboard = null;
        this.timers = Sets.newConcurrentHashSet();
        this.pillars = Sets.newHashSet();
        this.shields = Sets.newConcurrentHashSet();
    }

    public FactionPlayer(PlayerManager playerManager, UUID uniqueId, String username) {
        this.playerManager = playerManager;
        this.uniqueId = uniqueId;
        this.username = username;
        this.safeDisconnect = false;
        this.currentClaim = null;
        this.balance = playerManager.getPlugin().getConfiguration().getStartingBalance();
        this.resetOnJoin = false;
        this.scoreboard = null;
        this.timers = Sets.newConcurrentHashSet();
        this.pillars = Sets.newHashSet();
        this.shields = Sets.newConcurrentHashSet();
    }

    @Override
    public void setupScoreboard() {
        this.scoreboard = new FScoreboard(playerManager.getPlugin(), getBukkit(), playerManager.getPlugin().getConfiguration().getScoreboardTitle());
        getBukkit().setScoreboard(scoreboard.getInternal());
    }

    @Override
    public void destroyScoreboard() {
        if (scoreboard == null) {
            return;
        }

        scoreboard.getInternal().clearSlot(DisplaySlot.SIDEBAR);
        scoreboard = null;
    }

    @Override
    public void addToScoreboard(Player player) {
        if (scoreboard == null) {
            playerManager.getPlugin().getAresLogger().error("attempted to add to scoreboard but scoreboard was null");
            return;
        }

        final Team friendly = scoreboard.getInternal().getTeam(EScoreboardEntryType.FRIENDLY.getScoreboardTeamName());
        if (friendly == null) {
            playerManager.getPlugin().getAresLogger().error("attempted to add to scoreboard but team was null");
            return;
        }

        if (friendly.hasEntry(player.getName())) {
            return;
        }

        friendly.addEntry(player.getName());
    }

    @Override
    public void addToScoreboard(Player player, EScoreboardEntryType entryType) {
        if (scoreboard == null) {
            playerManager.getPlugin().getAresLogger().error("attempted to add to scoreboard but scoreboard was null");
            return;
        }

        final Team team = scoreboard.getInternal().getTeam(entryType.getScoreboardTeamName());
        if (team == null) {
            playerManager.getPlugin().getAresLogger().error("attempted to add to scoreboard but team was null");
            return;
        }

        if (team.hasEntry(player.getName())) {
            return;
        }

        team.addEntry(player.getName());
    }

    @Override
    public void removeFromScoreboard(Player player) {
        if (scoreboard == null) {
            playerManager.getPlugin().getAresLogger().error("attempted to remove one from scoreboard but scoreboard was null");
            return;
        }

        final Team friendly = scoreboard.getInternal().getTeam(EScoreboardEntryType.FRIENDLY.getScoreboardTeamName());
        if (friendly == null) {
            playerManager.getPlugin().getAresLogger().error("attempted to remove one from scoreboard but team was null");
            return;
        }

        friendly.removeEntry(player.getName());
    }

    @Override
    public void removeFromScoreboard(Player player, EScoreboardEntryType entryType) {
        if (scoreboard == null) {
            playerManager.getPlugin().getAresLogger().error("attempted to remove one from scoreboard but scoreboard was null");
            return;
        }

        final Team team = scoreboard.getInternal().getTeam(entryType.getScoreboardTeamName());
        if (team == null) {
            playerManager.getPlugin().getAresLogger().error("attempted to remove one from scoreboard but team was null");
            return;
        }

        team.removeEntry(player.getName());
    }

    @Override
    public void removeAllFromScoreboard() {
        if (scoreboard == null) {
            playerManager.getPlugin().getAresLogger().error("attempted to remove all from scoreboard but scoreboard was null");
            return;
        }

        final Team friendly = scoreboard.getInternal().getTeam("friendly");
        if (friendly == null) {
            playerManager.getPlugin().getAresLogger().error("attempted to remove all from scoreboard but team was null");
            return;
        }

        friendly.getEntries().forEach(friendly::removeEntry);
    }

    public Player getBukkit() {
        return Bukkit.getPlayer(uniqueId);
    }

    public void sendMessage(String message) {
        final Player player = getBukkit();
        if (player == null) {
            return;
        }

        player.sendMessage(message);
    }

    /**
     * Returns a Set collection of Shield Blocks matching the given type
     * @param type Shield Type
     * @return Set containing shield blocks matching type
     */
    public Set<IShield> getShieldBlocks(EShieldType type) {
        if (type.equals(EShieldType.COMBAT)) {
            return shields.stream().filter(s -> s instanceof CombatShield).collect(Collectors.toSet());
        }

        if (type.equals(EShieldType.PROTECTION)) {
            return shields.stream().filter(s -> s instanceof ProtectionShield).collect(Collectors.toSet());
        }

        return null;
    }

    public IShield getShieldBlockAt(BLocatable location) {
        if (shields.isEmpty()) {
            return null;
        }

        return shields
                .stream()
                .filter(shield ->
                        shield.getLocation().getX() == location.getX() &&
                                shield.getLocation().getY() == location.getY() &&
                                shield.getLocation().getZ() == location.getZ() &&
                                shield.getLocation().getWorldName().equals(location.getWorldName()))
                .findFirst()
                .orElse(null);
    }

    public ClaimPillar getExistingClaimPillar(EClaimPillarType pillarType) {
        return (ClaimPillar) pillars
                .stream()
                .filter(p -> p instanceof ClaimPillar)
                .filter(cp -> ((ClaimPillar)cp).getType().equals(pillarType))
                .findFirst()
                .orElse(null);
    }

    public void hideAllPillars() {
        pillars.forEach(IPillar::hide);
        pillars.clear();
    }

    public void hideAllShields() {
        shields.forEach(IShield::hide);
        shields.clear();
    }

    public boolean hasClaimPillars() {
        return pillars.stream().anyMatch(p -> p instanceof ClaimPillar);
    }

    public boolean hasMapPillars() {
        return pillars.stream().anyMatch(p -> p instanceof MapPillar);
    }

    public boolean hasCombatShields() {
        return shields.stream().anyMatch(shield -> shield instanceof CombatShield);
    }

    public boolean hasProtectionShields() {
        return shields.stream().anyMatch(shield -> shield instanceof ProtectionShield);
    }

    public void hideClaimPillars() {
        if (pillars.isEmpty() || !hasClaimPillars()) {
            return;
        }

        final List<IPillar> toRemove = Lists.newArrayList();

        pillars.stream().filter(p -> p instanceof ClaimPillar).forEach(cp -> {
            cp.hide();
            toRemove.add(cp);
        });

        toRemove.forEach(pillars::remove);
    }

    public void hideAllCombatShields() {
        final List<IShield> toRemove = Lists.newArrayList();

        shields
                .stream()
                .filter(shield -> shield instanceof CombatShield)
                .forEach(combatShield -> {
                    combatShield.hide();
                    toRemove.add(combatShield);
                });

        toRemove.forEach(shields::remove);
    }

    public void hideAllProtectionShields() {
        final List<IShield> toRemove = Lists.newArrayList();

        shields
                .stream()
                .filter(shield -> shield instanceof ProtectionShield)
                .forEach(protShield -> {
                    protShield.hide();
                    toRemove.add(protShield);
                });

        toRemove.forEach(shields::remove);
    }

    public void hideMapPillars() {
        if (pillars.isEmpty() || !hasMapPillars()) {
            return;
        }

        final List<IPillar> toRemove = Lists.newArrayList();

        pillars.stream().filter(p -> p instanceof MapPillar).forEach(cp -> {
            cp.hide();
            toRemove.add(cp);
        });

        toRemove.forEach(pillars::remove);
    }

    @Override
    public void finishTimer(ETimerType type) {
        if (type.equals(ETimerType.ENDERPEARL)) {
            sendMessage(FMessage.T_EPEARL_UNLOCKED);
        }

        if (type.equals(ETimerType.CRAPPLE)) {
            sendMessage(FMessage.T_CRAPPLE_UNLOCKED);
        }

        if (type.equals(ETimerType.GAPPLE)) {
            sendMessage(FMessage.T_GAPPLE_UNLOCKED);
        }

        if (type.equals(ETimerType.CHORUS_FRUIT)) {
            sendMessage(FMessage.T_CHORUS_UNLOCKED);
        }

        if (type.equals(ETimerType.TRIDENT)) {
            sendMessage(FMessage.T_TRIDENT_UNLOCKED);
        }

        if (type.equals(ETimerType.GRAPPLE)) {
            sendMessage(FMessage.T_GRAPPLE_UNLOCKED);
        }

        if (type.equals(ETimerType.STUCK)) {
            FactionUtil.teleportToSafety(playerManager.getPlugin(), getBukkit());
            sendMessage(FMessage.T_STUCK_EXPIRE);
        }

        if (type.equals(ETimerType.LOGOUT)) {
            setSafeDisconnect(true);
            getBukkit().kickPlayer(FMessage.T_LOGOUT_EXPIRE);
        }

        if (type.equals(ETimerType.COMBAT)) {
            hideAllCombatShields();
            sendMessage(FMessage.T_CTAG_EXPIRE);
        }

        if (type.equals(ETimerType.ARCHER_MARK)) {
            sendMessage(FMessage.T_ARCHER_MARK_COMPLETE);
        }

        if (type.equals(ETimerType.HOME)) {
            final PlayerFaction faction = playerManager.getPlugin().getFactionManager().getPlayerFactionByPlayer(uniqueId);

            if (faction == null) {
                sendMessage(FMessage.ERROR + FError.P_NOT_IN_FAC.getErrorDescription());
            } else if (faction.getHomeLocation() == null) {
                sendMessage(FMessage.ERROR + FError.F_HOME_UNSET.getErrorDescription());
            } else {
                getBukkit().teleport(faction.getHomeLocation().getBukkitLocation());
                sendMessage(FMessage.T_HOME_EXPIRE);
            }
        }

        if (type.equals(ETimerType.CLASS)) {
            final IClass playerClass = playerManager.getPlugin().getClassManager().getClassByArmor(getBukkit());

            if (playerClass != null) {
                playerClass.activate(getBukkit());
            }
        }

        if (type.equals(ETimerType.GUARD)) {
            final IClass playerClass = playerManager.getPlugin().getClassManager().getClassByArmor(getBukkit());

            if (!(playerClass instanceof final Tank tankClass)) {
                sendMessage(FMessage.ERROR + FError.C_CLASS_MISMATCH.getErrorDescription());
            } else {
                final TankShieldReadyEvent readyEvent = new TankShieldReadyEvent(getBukkit(), tankClass);
                Bukkit.getPluginManager().callEvent(readyEvent);

                if (!readyEvent.isCancelled()) {
                    tankClass.activateShield(getBukkit());
                }
            }
        }

        if (type.equals(ETimerType.PROTECTION)) {
            hideAllProtectionShields();
            sendMessage(FMessage.T_PROTECTION_EXPIRE);
        }

        removeTimer(type);
    }

    @Override
    public FactionPlayer fromDocument(Document document) {
        this.uniqueId = UUID.fromString(document.getString("uuid"));
        this.balance = document.getDouble("balance");
        this.resetOnJoin = document.getBoolean("reset_on_join");

        if (document.containsKey("timers")) {
            final List<Document> timerDocs = document.getList("timers", Document.class);

            for (Document td : timerDocs) {
                final FTimer timer = new FTimer().fromDocument(td);

                if (timer.getType().equals(ETimerType.PROTECTION) && playerManager.getPlugin().getServerStateManager().isEOTW()) {
                    continue;
                }

                timers.add(timer);
            }
        }

        return this;
    }

    @Override
    public Document toDocument() {
        final List<Document> timerDocs = Lists.newArrayList();
        timers.stream().filter(t -> t.getType().isPersistent()).forEach(pt -> timerDocs.add(pt.toDocument()));

        return new Document()
                .append("uuid", uniqueId.toString())
                .append("balance", balance)
                .append("reset_on_join", resetOnJoin)
                .append("timers", timerDocs);
    }
}
