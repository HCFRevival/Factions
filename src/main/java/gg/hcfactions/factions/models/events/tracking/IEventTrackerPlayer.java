package gg.hcfactions.factions.models.events.tracking;

import gg.hcfactions.libs.base.connect.impl.mongo.MongoDocument;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface IEventTrackerPlayer extends MongoDocument<IEventTrackerPlayer> {
    UUID getUniqueId();
    String getUsername();
    Map<String, Number> getValues();

    void setUsername(String username);

    default void add(String key, Number value) {
        if (getValues().containsKey(key)) {
            final Number existing = getValues().get(key);

            if (existing instanceof Integer) {
                getValues().put(key, (value.intValue() + existing.intValue()));
            } else if (existing instanceof Double) {
                getValues().put(key, (value.doubleValue() + existing.doubleValue()));
            } else if (existing instanceof Long) {
                getValues().put(key, (value.longValue() + existing.longValue()));
            }

            return;
        }

        getValues().put(key, value);
    }

    default Optional<Player> getPlayer() {
        final Player player = Bukkit.getPlayer(getUniqueId());

        if (player == null || !player.isOnline()) {
            return Optional.empty();
        }

        return Optional.of(player);
    }
}
