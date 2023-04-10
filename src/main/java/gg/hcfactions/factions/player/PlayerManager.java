package gg.hcfactions.factions.player;

import com.google.common.collect.Sets;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.player.IFactionPlayer;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.libs.base.connect.impl.mongo.Mongo;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public final class PlayerManager implements IManager {
    public static final String PLAYER_DB_NAME = "dev";
    public static final String PLAYER_DB_COLL_NAME = "players";

    @Getter public Factions plugin;
    @Getter public Set<IFactionPlayer> playerRepository;

    public PlayerManager(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        this.playerRepository = Sets.newConcurrentHashSet();
    }

    @Override
    public void onDisable() {
        savePlayers();

        this.plugin = null;
        this.playerRepository = null;
    }

    public IFactionPlayer loadPlayer(Bson filter, boolean cache) {
        final Mongo mdb = (Mongo) plugin.getConnectable(Mongo.class);
        if (mdb == null) {
            plugin.getAresLogger().error("attempted to load player with null mongo instance");
            return null;
        }

        final MongoDatabase db = mdb.getDatabase(PLAYER_DB_NAME);
        if (db == null) {
            plugin.getAresLogger().error("attempted to load player with null db instance");
            return null;
        }

        final MongoCollection<Document> coll = db.getCollection(PLAYER_DB_COLL_NAME);
        final Document doc = coll.find(filter).first();
        if (doc == null) {
            return null;
        }

        final FactionPlayer factionPlayer = new FactionPlayer(plugin.getPlayerManager()).fromDocument(doc);
        if (cache) {
            playerRepository.add(factionPlayer);
        }

        return factionPlayer;
    }

    public void savePlayer(IFactionPlayer factionPlayer) {
        final FactionPlayer fp = (FactionPlayer) factionPlayer;
        final Mongo mdb = (Mongo) plugin.getConnectable(Mongo.class);
        if (mdb == null) {
            plugin.getAresLogger().error("attempted to save player with null mongo instance");
            return;
        }

        final MongoDatabase db = mdb.getDatabase(PLAYER_DB_NAME);
        if (db == null) {
            plugin.getAresLogger().error("attempted to save player with null db instance");
            return;
        }

        final MongoCollection<Document> coll = db.getCollection(PLAYER_DB_COLL_NAME);
        final Document existing = coll.find(Filters.eq("uuid", fp.getUniqueId().toString())).first();

        if (existing != null) {
            coll.replaceOne(existing, fp.toDocument());
        } else {
            coll.insertOne(fp.toDocument());
        }
    }

    public void savePlayers() {
        playerRepository.forEach(this::savePlayer);
        plugin.getAresLogger().info("wrote " + playerRepository.size() + " players to db");
    }

    public IFactionPlayer getPlayer(UUID uniqueId) {
        return playerRepository.stream().filter(p -> p.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public IFactionPlayer getPlayer(String username) {
        return playerRepository.stream().filter(p -> p.getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);
    }

    public IFactionPlayer getPlayer(Player player) {
        return getPlayer(player.getUniqueId());
    }
}
