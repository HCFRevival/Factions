package gg.hcfactions.factions.models.events.tracking;

import gg.hcfactions.libs.base.connect.impl.mongo.MongoDocument;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface IEventTrackerPlayer extends MongoDocument<IEventTrackerPlayer> {
    /**
     * @return Bukkit UUID
     */
    UUID getUniqueId();

    /**
     * @return Bukkit Username
     */
    String getUsername();

    /**
     * @return Internal storage for mapped out values
     */
    Map<String, Number> getValues();

    /**
     * Update this tracker instance's Bukkit username
     * @param username New Username
     */
    void setUsername(String username);

    /**
     * Add a new entry to this tracker
     * @param key Entry Key, this will be used for display on the website
     * @param value Int, Double or Long
     */
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

    /**
     * @return Optional of Bukkit Player
     */
    default Optional<Player> getPlayer() {
        final Player player = Bukkit.getPlayer(getUniqueId());

        if (player == null || !player.isOnline()) {
            return Optional.empty();
        }

        return Optional.of(player);
    }
}
