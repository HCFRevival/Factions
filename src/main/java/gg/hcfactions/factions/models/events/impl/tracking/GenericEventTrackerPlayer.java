package gg.hcfactions.factions.models.events.impl.tracking;

import com.google.common.collect.Maps;
import gg.hcfactions.factions.models.events.tracking.IEventTrackerPlayer;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class GenericEventTrackerPlayer implements IEventTrackerPlayer {
    @Getter public final UUID uniqueId;
    @Getter @Setter public String username;
    @Getter public final Map<String, Number> values;

    public GenericEventTrackerPlayer(Player player) {
        this.uniqueId = player.getUniqueId();
        this.username = player.getName();
        this.values = Maps.newConcurrentMap();
    }

    public GenericEventTrackerPlayer(UUID uniqueId, String username) {
        this.uniqueId = uniqueId;
        this.username = username;
        this.values = Maps.newConcurrentMap();
    }

    /**
     * This function should never be called
     * @param document
     * @return Do not use
     */
    @Override
    public IEventTrackerPlayer fromDocument(Document document) {
        return this;
    }

    @Override
    public Document toDocument() {
        final Document doc = new Document();

        doc.append("id", uniqueId.toString());
        doc.append("name", username);
        doc.append("values", values);

        return doc;
    }
}
