package gg.hcfactions.factions.models.player.impl;

import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.player.IFactionPlayer;
import gg.hcfactions.libs.base.connect.impl.mongo.MongoDocument;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.UUID;

public final class FactionPlayer implements IFactionPlayer, MongoDocument<FactionPlayer> {
    @Getter @Setter public transient String username;
    @Getter @Setter public transient boolean safeDisconnecting;
    @Getter @Setter public transient Claim currentClaim;

    @Getter public UUID uniqueId;
    @Getter @Setter public double balance;
    @Getter @Setter public boolean resetOnJoin;

    public FactionPlayer() {
        this.username = null;
        this.safeDisconnecting = false;
        this.currentClaim = null;
        this.uniqueId = null;
        this.balance = 0.0;
        this.resetOnJoin = false;
    }

    public FactionPlayer(UUID uniqueId, String username) {
        this.uniqueId = uniqueId;
        this.username = username;
        this.safeDisconnecting = false;
        this.currentClaim = null;
        this.balance = 0.0;
        this.resetOnJoin = false;
    }

    @Override
    public FactionPlayer fromDocument(Document document) {
        this.uniqueId = UUID.fromString(document.getString("uuid"));
        this.balance = document.getDouble("balance");
        this.resetOnJoin = document.getBoolean("reset_on_join");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("uuid", uniqueId.toString())
                .append("balance", balance)
                .append("reset_on_join", resetOnJoin);
    }
}
