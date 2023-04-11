package gg.hcfactions.factions.faction;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.faction.impl.FactionExecutor;
import gg.hcfactions.factions.faction.impl.FactionValidator;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.libs.base.connect.impl.mongo.Mongo;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class FactionManager implements IManager {
    public static final String FACTION_DB_NAME = "dev";
    public static final String PLAYER_FACTION_DB_COLL_NAME = "player_factions";
    public static final String SERVER_FACTION_DB_COLL_NAME = "server_factions";

    @Getter public Factions plugin;
    @Getter public FactionValidator validator;
    @Getter public FactionExecutor executor;
    @Getter public BukkitTask factionTickingTask;
    @Getter public Set<IFaction> factionRepository;

    public FactionManager(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        this.executor = new FactionExecutor(this);
        this.validator = new FactionValidator(this);
        this.factionRepository = Sets.newConcurrentHashSet();

        // called on main thread to lock process
        loadFactions();

        // faction ticking task
        this.factionTickingTask = new Scheduler(plugin).async(() ->
                factionRepository
                        .stream()
                        .filter(f -> f instanceof PlayerFaction)
                        .filter(pf -> ((PlayerFaction)pf).canTick())
                        .forEach(playerFaction -> ((PlayerFaction) playerFaction).tick())).repeat(0L, 20L).run();
    }

    @Override
    public void onDisable() {
        this.factionTickingTask.cancel();

        // called on the main thread to lock process
        saveFactions();

        this.plugin = null;
        this.executor = null;
        this.validator = null;
        this.factionTickingTask = null;
        this.factionRepository = null;
    }

    public void saveFactions() {
        final Mongo mdb = (Mongo) plugin.getConnectable(Mongo.class);
        if (mdb == null) {
            plugin.getAresLogger().error("attempted to save factions with null mongo instance");
            return;
        }

        final MongoDatabase db = mdb.getDatabase(FACTION_DB_NAME);
        if (db == null) {
            plugin.getAresLogger().error("attempted to save factions with null db instance");
            return;
        }

        final MongoCollection<Document> playerFactionColl = db.getCollection(PLAYER_FACTION_DB_COLL_NAME);
        final MongoCollection<Document> serverFactionColl = db.getCollection(SERVER_FACTION_DB_COLL_NAME);

        factionRepository.forEach(f -> {
            if (f instanceof PlayerFaction) {
                final Document existing = playerFactionColl.find(Filters.eq("uuid", f.getUniqueId().toString())).first();

                if (existing != null) {
                    playerFactionColl.replaceOne(existing, ((PlayerFaction) f).toDocument());
                } else {
                    playerFactionColl.insertOne(((PlayerFaction) f).toDocument());
                }
            }

            if (f instanceof ServerFaction) {
                final Document existing = serverFactionColl.find(Filters.eq("uuid", f.getUniqueId().toString())).first();

                if (existing != null) {
                    serverFactionColl.replaceOne(existing, ((ServerFaction) f).toDocument());
                } else {
                    serverFactionColl.insertOne(((ServerFaction) f).toDocument());
                }
            }
        });

        plugin.getAresLogger().info("wrote " + factionRepository.size() + " factions to db");
    }

    public void loadFactions() {
        final long pre = Time.now();
        final Mongo mdb = (Mongo) plugin.getConnectable(Mongo.class);
        if (mdb == null) {
            plugin.getAresLogger().error("attempted to save factions with null mongo instance");
            return;
        }

        final MongoDatabase db = mdb.getDatabase(FACTION_DB_NAME);
        if (db == null) {
            plugin.getAresLogger().error("attempted to save factions with null db instance");
            return;
        }

        final MongoCollection<Document> playerFactionColl = db.getCollection(PLAYER_FACTION_DB_COLL_NAME);
        final MongoCollection<Document> serverFactionColl = db.getCollection(SERVER_FACTION_DB_COLL_NAME);
        final FindIterable<Document> playerFactionDocs = playerFactionColl.find();
        final FindIterable<Document> serverFactionDocs = serverFactionColl.find();

        try (MongoCursor<Document> pCursor = playerFactionDocs.cursor()) {
            final Document doc = pCursor.tryNext();

            if (doc == null) {
                pCursor.close();
                return;
            }

            final PlayerFaction faction = new PlayerFaction(this).fromDocument(doc);
            factionRepository.add(faction);
        }

        try (MongoCursor<Document> sCursor = serverFactionDocs.cursor()) {
            final Document doc = sCursor.tryNext();

            if (doc == null) {
                sCursor.close();
                return;
            }

            final ServerFaction faction = new ServerFaction().fromDocument(doc);
            factionRepository.add(faction);
        }

        final long post = Time.now();
        final long diff = post - pre;
        plugin.getAresLogger().info("loaded " + factionRepository.size() + " factions (took " + diff + "ms)");
    }

    public DeleteResult deleteFaction(IFaction faction) {
        final Mongo mdb = (Mongo) plugin.getConnectable(Mongo.class);
        if (mdb == null) {
            plugin.getAresLogger().error("attempted to delete faction with null mongo instance");
            return null;
        }

        final MongoDatabase db = mdb.getDatabase(FACTION_DB_NAME);
        if (db == null) {
            plugin.getAresLogger().error("attempted to delete faction with null db instance");
            return null;
        }

        final MongoCollection<Document> coll = db.getCollection((faction instanceof PlayerFaction) ? PLAYER_FACTION_DB_COLL_NAME : SERVER_FACTION_DB_COLL_NAME);
        return coll.deleteOne(Filters.eq("uuid", faction.getUniqueId().toString()));
    }

    public IFaction getFactionById(UUID uniqueId) {
        return factionRepository.stream().filter(f -> f.getUniqueId().equals(uniqueId))
                .findFirst()
                .orElse(null);
    }

    public IFaction getFactionByName(String name) {
        return factionRepository.stream().filter(f -> f.getName()
                .equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public PlayerFaction getPlayerFactionById(UUID uniqueId) {
        return (PlayerFaction) factionRepository.stream().filter(f -> f instanceof PlayerFaction && f.getUniqueId().equals(uniqueId))
                .findFirst()
                .orElse(null);
    }

    public PlayerFaction getPlayerFactionByName(String name) {
        return (PlayerFaction) factionRepository.stream().filter(f -> f instanceof PlayerFaction && f.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public PlayerFaction getPlayerFactionByPlayer(UUID uniqueId) {
        return (PlayerFaction) factionRepository.stream().filter(f -> f instanceof PlayerFaction && ((PlayerFaction) f).isMember(uniqueId))
                .findFirst()
                .orElse(null);
    }

    public PlayerFaction getPlayerFactionByPlayer(Player player) {
        return getPlayerFactionByPlayer(player.getUniqueId());
    }

    public ServerFaction getServerFactionByName(String name) {
        return (ServerFaction) factionRepository.stream().filter(f -> f instanceof ServerFaction && f.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public ImmutableList<PlayerFaction> getPlayerFactions() {
        final List<PlayerFaction> res = Lists.newArrayList();
        factionRepository.stream().filter(f -> f instanceof PlayerFaction).forEach(f -> res.add((PlayerFaction)f));
        return ImmutableList.copyOf(res);
    }

    public ImmutableList<ServerFaction> getServerFactions() {
        final List<ServerFaction> res = Lists.newArrayList();
        factionRepository.stream().filter(f -> f instanceof ServerFaction).forEach(f -> res.add((ServerFaction)f));
        return ImmutableList.copyOf(res);
    }
}
