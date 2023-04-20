package gg.hcfactions.factions.models.player.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.classes.IClass;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.message.FError;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.player.IFactionPlayer;
import gg.hcfactions.factions.player.PlayerManager;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import gg.hcfactions.factions.utils.FactionUtil;
import gg.hcfactions.libs.base.connect.impl.mongo.MongoDocument;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class FactionPlayer implements IFactionPlayer, MongoDocument<FactionPlayer> {
    @Getter public final transient PlayerManager playerManager;
    @Getter @Setter public transient String username;
    @Getter @Setter public transient boolean safeDisconnecting;
    @Getter @Setter public transient Claim currentClaim;

    @Getter public UUID uniqueId;
    @Getter @Setter public double balance;
    @Getter @Setter public boolean resetOnJoin;
    @Getter public Set<FTimer> timers;

    public FactionPlayer(PlayerManager playerManager) {
        this.playerManager = playerManager;
        this.username = null;
        this.safeDisconnecting = false;
        this.currentClaim = null;
        this.uniqueId = null;
        this.balance = 0.0;
        this.resetOnJoin = false;
        this.timers = Sets.newConcurrentHashSet();
    }

    public FactionPlayer(PlayerManager playerManager, UUID uniqueId, String username) {
        this.playerManager = playerManager;
        this.uniqueId = uniqueId;
        this.username = username;
        this.safeDisconnecting = false;
        this.currentClaim = null;
        this.balance = 0.0;
        this.resetOnJoin = false;
        this.timers = Sets.newConcurrentHashSet();
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

        if (type.equals(ETimerType.STUCK)) {
            FactionUtil.teleportToSafety(playerManager.getPlugin(), getBukkit());
            sendMessage(FMessage.T_STUCK_EXPIRE);
        }

        if (type.equals(ETimerType.LOGOUT)) {
            setSafeDisconnecting(true);
            getBukkit().kickPlayer(FMessage.T_LOGOUT_EXPIRE);
        }

        if (type.equals(ETimerType.COMBAT)) {
            // TODO: Remove combat shield
            sendMessage(FMessage.T_CTAG_EXPIRE);
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

        if (type.equals(ETimerType.PROTECTION)) {
            // TODO: Remove protection shield
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
            timerDocs.forEach(td -> timers.add(new FTimer().fromDocument(td)));
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
