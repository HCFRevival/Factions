package gg.hcfactions.factions.models.stats.impl.stat;

import gg.hcfactions.factions.models.stats.ITrackable;
import gg.hcfactions.libs.base.connect.impl.mongo.MongoDocument;
import gg.hcfactions.libs.base.util.Time;
import lombok.Getter;
import org.bson.Document;
import org.bukkit.ChatColor;

import java.util.UUID;

public final class KillStat implements ITrackable, MongoDocument<KillStat> {
    @Getter public UUID killerUniqueId;
    @Getter public UUID slainUniqueId;
    @Getter public String killerUsername;
    @Getter public String slainUsername;
    @Getter public String deathMessage;
    @Getter public double mapNumber;
    @Getter public long date;

    public KillStat() {
        this.killerUniqueId = null;
        this.slainUniqueId = null;
        this.killerUsername = null;
        this.slainUsername = null;
        this.deathMessage = null;
        this.mapNumber = 0.0D;
        this.date = 0L;
    }

    public KillStat(UUID killerUniqueId, String killerUsername, UUID slainUniqueId, String slainUsername, String deathMessage, double currentMap) {
        this.killerUniqueId = killerUniqueId;
        this.killerUsername = killerUsername;
        this.slainUniqueId = slainUniqueId;
        this.slainUsername = slainUsername;
        this.deathMessage = ChatColor.stripColor(deathMessage);
        this.mapNumber = currentMap;
        this.date = Time.now();
    }

    @Override
    public KillStat fromDocument(Document document) {
        this.killerUniqueId = (UUID)document.get("killer_id");
        this.killerUsername = document.getString("killer_username");
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
                .append("killer_id", killerUniqueId)
                .append("killer_username", killerUsername)
                .append("slain_id", slainUniqueId)
                .append("slain_username", slainUsername)
                .append("death_message", deathMessage)
                .append("map", mapNumber)
                .append("date", date);
    }
}
