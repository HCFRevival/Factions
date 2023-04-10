package gg.hcfactions.factions.models.stats.impl.stat;

import gg.hcfactions.factions.models.stats.ITrackable;
import gg.hcfactions.libs.base.connect.impl.mongo.MongoDocument;
import gg.hcfactions.libs.base.util.Time;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bson.Document;

import java.util.UUID;

public final class DeathStat implements ITrackable, MongoDocument<DeathStat> {
    @Getter public UUID slainUniqueId;
    @Getter public String slainUsername;
    @Getter public String deathMessage;
    @Getter public double mapNumber;
    @Getter public long date;

    public DeathStat() {
        this.slainUniqueId = null;
        this.slainUsername = null;
        this.deathMessage = null;
        this.mapNumber = 0.0D;
        this.date = 0L;
    }

    public DeathStat(UUID slainUniqueId, String slainUsername, String deathMessage, double currentMap) {
        this.slainUniqueId = slainUniqueId;
        this.slainUsername = slainUsername;
        this.deathMessage = ChatColor.stripColor(deathMessage);
        this.mapNumber = currentMap;
        this.date = Time.now();
    }

    @Override
    public DeathStat fromDocument(Document document) {
        this.slainUniqueId = (UUID)document.get("slain_id");
        this.slainUsername = document.getString("slain_username");
        this.deathMessage = document.getString("death_message");
        this.mapNumber = document.getDouble("map");
        this.date = document.getLong("date");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("slain_id", slainUniqueId)
                .append("slain_username", slainUsername)
                .append("death_message", deathMessage)
                .append("map", mapNumber)
                .append("date", date);
    }
}
