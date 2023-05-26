package gg.hcfactions.factions.stats;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.stats.EStatisticType;
import gg.hcfactions.factions.models.stats.IStatHolder;
import gg.hcfactions.factions.models.stats.impl.PlayerStatHolder;
import gg.hcfactions.factions.models.stats.impl.stat.DeathStat;
import gg.hcfactions.factions.models.stats.impl.stat.EventCaptureStat;
import gg.hcfactions.factions.models.stats.impl.stat.KillStat;
import gg.hcfactions.factions.stats.impl.StatsExecutor;
import gg.hcfactions.libs.base.connect.impl.mongo.Mongo;
import gg.hcfactions.libs.base.consumer.FailablePromise;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import gg.hcfactions.libs.bukkit.services.impl.account.AccountService;
import gg.hcfactions.libs.bukkit.services.impl.account.model.AresAccount;
import lombok.Getter;
import org.bson.Document;

import java.util.*;
import java.util.function.Consumer;

public final class StatsManager implements IManager {
    public static final String STATS_DB_PLAYER_COLL_NAME = "stats_players";
    public static final String STATS_DB_FACTION_COLL_NAME = "stats_factions";
    public static final String STATS_DB_KILL_COLL_NAME = "stats_kills";
    public static final String STATS_DB_DEATH_COLL_NAME = "stats_deaths";
    public static final String STATS_DB_EVENT_COLL_NAME = "stats_events";

    @Getter public final Factions plugin;
    @Getter public StatsExecutor executor;
    @Getter public StatsConfig config;
    @Getter public Set<IStatHolder> trackerRepository;

    public StatsManager(Factions plugin, StatsConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public void onEnable() {
        this.executor = new StatsExecutor(this);
        this.trackerRepository = Sets.newConcurrentHashSet();
    }

    @Override
    public void onDisable() {
        saveStatistics();

        this.executor = null;
        this.trackerRepository.clear();
    }

    public void saveStatistics() {
        final Mongo mdb = (Mongo) plugin.getConnectable(Mongo.class);
        if (mdb == null) {
            plugin.getAresLogger().error("attempted to save player statistics data but mongo instance was null");
            return;
        }

        final MongoDatabase db = mdb.getDatabase(plugin.getConfiguration().getMongoDatabaseName());
        if (db == null) {
            plugin.getAresLogger().error("attempted to save player statistics data but mongo db was null");
            return;
        }

        final MongoCollection<Document> pColl = db.getCollection(STATS_DB_PLAYER_COLL_NAME);
        // TODO: Faction stats here

        trackerRepository.stream().filter(h -> h instanceof PlayerStatHolder).forEach(ph -> {
            final Document existing = pColl.find(Filters.and(
                    Filters.eq("uuid", ph.getUniqueId().toString()),
                    Filters.eq("map", config.getMapNumber())
            )).first();

            if (existing != null) {
                pColl.replaceOne(existing, ((PlayerStatHolder) ph).toDocument());
            } else {
                pColl.insertOne(((PlayerStatHolder) ph).toDocument());
            }
        });

        plugin.getAresLogger().info("finished writing player statistics to db");
    }

    /**
     * Returns a player statistic container for the provided Bukkit UUID
     * @param uniqueId Bukkit UUID
     * @return PlayerStatisticHolder
     */
    public PlayerStatHolder getPlayerStatistics(UUID uniqueId) {
        return (PlayerStatHolder) trackerRepository
                .stream()
                .filter(stats -> stats instanceof PlayerStatHolder)
                .filter(ps -> ps.getUniqueId().equals(uniqueId))
                .findAny()
                .orElse(null);
    }

    /**
     * Returns a player statistic container for the provided Bukkit UUID in the form of a consumer
     * @param uniqueId Bukkit UUID
     * @param consumer Consumer
     */
    public void getPlayerStatistics(UUID uniqueId, Consumer<PlayerStatHolder> consumer) {
        final PlayerStatHolder cached = (PlayerStatHolder) trackerRepository.stream().filter(stats -> stats.getUniqueId().equals(uniqueId)).findAny().orElse(null);

        if (cached != null) {
            consumer.accept(cached);
            return;
        }

        final AccountService acs = (AccountService) plugin.getService(AccountService.class);

        if (acs == null) {
            consumer.accept(null);
            return;
        }

        acs.getAccount(uniqueId, new FailablePromise<>() {
            @Override
            public void resolve(AresAccount aresAccount) {
                final Mongo mdb = (Mongo)plugin.getConnectable(Mongo.class);
                final PlayerStatHolder emptyHolder = new PlayerStatHolder(
                        uniqueId,
                        aresAccount == null ? null : aresAccount.getUsername(),
                        config.getMapNumber()
                );

                if (aresAccount == null || mdb == null) {
                    new Scheduler(plugin).sync(() -> consumer.accept(emptyHolder)).run();
                    return;
                }

                final MongoDatabase db = mdb.getDatabase(plugin.getConfiguration().getMongoDatabaseName());
                if (db == null) {
                    new Scheduler(plugin).sync(() -> consumer.accept(emptyHolder)).run();
                    return;
                }

                final MongoCollection<Document> coll = db.getCollection(STATS_DB_PLAYER_COLL_NAME);
                final Document document = coll.find(Filters.and(Filters.eq("uuid", uniqueId.toString()), Filters.eq("map", config.getMapNumber()))).first();

                if (document == null) {
                    new Scheduler(plugin).sync(() -> consumer.accept(emptyHolder)).run();
                    return;
                }

                new Scheduler(plugin).sync(() -> {
                    final PlayerStatHolder holder = new PlayerStatHolder().fromDocument(document);
                    holder.setName(aresAccount.getUsername());
                    consumer.accept(holder);
                }).run();
            }

            @Override
            public void reject(String s) {
                consumer.accept(null);
            }
        });
    }

    /**
     * Returns player leaderboard data in the form of a consumer
     * @param consumer Consumer
     */
    public void getPlayerLeaderboardData(Consumer<List<PlayerStatHolder>> consumer) {
        final List<PlayerStatHolder> result = Lists.newArrayList();

        new Scheduler(plugin).async(() -> {
            final Mongo mdb = (Mongo) plugin.getConnectable(Mongo.class);
            if (mdb == null) {
                plugin.getAresLogger().error("attempted to aggregate player leaderboard data but mongo instance was null");
                new Scheduler(plugin).sync(() -> consumer.accept(ImmutableList.copyOf(result))).run();
                return;
            }

            final MongoDatabase db = mdb.getDatabase(plugin.getConfiguration().getMongoDatabaseName());
            if (db == null) {
                plugin.getAresLogger().error("attempted to aggregate player leaderboard data but mongo db was null");
                return;
            }

            final MongoCollection<Document> coll = db.getCollection(STATS_DB_PLAYER_COLL_NAME);
            try (MongoCursor<Document> cursor = coll.find().cursor()) {
                while (cursor.hasNext()) {
                    result.add(new PlayerStatHolder().fromDocument(cursor.next()));
                }
            }

            new Scheduler(plugin).sync(() -> consumer.accept(ImmutableList.copyOf(result))).run();
        }).run();
    }

    /**
     * Returns leaderboards for the provided statistic type
     * @param type Statistic Type
     * @param consumer Consumer
     */
    public void getPlayerLeaderboard(EStatisticType type, Consumer<List<PlayerStatHolder>> consumer) {
        getPlayerLeaderboardData(data -> {
            final List<PlayerStatHolder> stats = Lists.newArrayList(data);
            stats.sort(Comparator.comparingLong(holder -> holder.getStatistic(type)));
            consumer.accept(stats);
        });
    }

    /**
     * Save a player stat holder to the database
     * @param holder Stat holder to save
     */
    public void savePlayer(PlayerStatHolder holder) {
        new Scheduler(plugin).async(() -> {
            final Mongo mdb = (Mongo)plugin.getConnectable(Mongo.class);

            if (mdb == null) {
                plugin.getAresLogger().error("attempted to save player stats when mongo instance is null");
                return;
            }

            final MongoDatabase db = mdb.getDatabase(plugin.getConfiguration().getMongoDatabaseName());
            if (db == null) {
                plugin.getAresLogger().error("attempted to save player stats when mongo db is null");
                return;
            }

            final MongoCollection<Document> coll = db.getCollection(STATS_DB_PLAYER_COLL_NAME);
            final Document existing = coll.find(
                    Filters.and(
                            Filters.eq("uuid", holder.getUniqueId().toString()),
                            Filters.eq("map", holder.getMapNumber())
                    )
            ).first();

            if (existing == null) {
                coll.insertOne(holder.toDocument());
            } else {
                coll.replaceOne(existing, holder.toDocument());
            }
        }).run();
    }

    /**
     * Handles creating a kill entry in the database
     * @param killerUniqueId Killer UUID
     * @param killerUsername Killer Username
     * @param slainUniqueId Slain UUID
     * @param slainUsername Slain Username
     * @param deathMessage Death Message
     */
    public void createKill(UUID killerUniqueId, String killerUsername, UUID slainUniqueId, String slainUsername, String deathMessage) {
        final KillStat kill = new KillStat(killerUniqueId, killerUsername, slainUniqueId, slainUsername, deathMessage, config.getMapNumber());

        new Scheduler(plugin).async(() -> {
            final Mongo mdb = (Mongo) plugin.getConnectable(Mongo.class);
            if (mdb == null) {
                plugin.getAresLogger().error("failed to write kill to database");
                return;
            }

            final MongoDatabase db = mdb.getDatabase(plugin.getConfiguration().getMongoDatabaseName());
            final MongoCollection<Document> killColl = db.getCollection(STATS_DB_KILL_COLL_NAME);
            // TODO: Faction tracking here

            killColl.insertOne(kill.toDocument());
        }).run();
    }

    /**
     * Handles creating a death entry in the database
     * @param slainUniqueId Slain Unique ID
     * @param slainUsername Slain Username
     * @param deathMessage Death Message
     */
    public void createDeath(UUID slainUniqueId, String slainUsername, String deathMessage) {
        final DeathStat death = new DeathStat(slainUniqueId, slainUsername, deathMessage, config.getMapNumber());

        new Scheduler(plugin).async(() -> {
            final Mongo mdb = (Mongo) plugin.getConnectable(Mongo.class);
            if (mdb == null) {
                plugin.getAresLogger().error("failed to write death to database");
                return;
            }

            final MongoDatabase db = mdb.getDatabase(plugin.getConfiguration().getMongoDatabaseName());
            final MongoCollection<Document> deathColl = db.getCollection(STATS_DB_DEATH_COLL_NAME);
            // TODO: Faction tracking here

            deathColl.insertOne(death.toDocument());
        }).run();
    }

    /**
     * Handles creating an event capture entry in the database
     * @param eventName Event name
     * @param factionUniqueId Faction UUID
     * @param factionName Faction Name
     * @param capturingUsernames Usernames involved with capturing the event
     */
    public void createEventCapture(String eventName, UUID factionUniqueId, String factionName, Collection<String> capturingUsernames) {
        final EventCaptureStat eventCapture = new EventCaptureStat(factionUniqueId, eventName, factionName, capturingUsernames, config.getMapNumber());

        new Scheduler(plugin).async(() -> {
            final Mongo mdb = (Mongo) plugin.getConnectable(Mongo.class);
            if (mdb == null) {
                plugin.getAresLogger().error("failed to write event capture to database");
                return;
            }

            final MongoDatabase db = mdb.getDatabase(plugin.getConfiguration().getMongoDatabaseName());
            final MongoCollection<Document> eventColl = db.getCollection(STATS_DB_EVENT_COLL_NAME);
            // TODO: Faction tracking here

            eventColl.insertOne(eventCapture.toDocument());
        }).run();
    }
}
