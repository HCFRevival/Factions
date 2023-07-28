package gg.hcfactions.factions.claims.subclaims;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.claims.subclaims.impl.SubclaimExecutor;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.factions.models.subclaim.Subclaim;
import gg.hcfactions.libs.base.connect.impl.mongo.Mongo;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.location.ILocatable;
import lombok.Getter;
import org.bson.Document;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class SubclaimManager implements IManager {
    @Getter public final Factions plugin;
    @Getter public SubclaimExecutor executor;
    @Getter public SubclaimBuilderManager builderManager;
    @Getter public Set<Subclaim> subclaimRepository;

    public SubclaimManager(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        executor = new SubclaimExecutor(this);
        builderManager = new SubclaimBuilderManager(this);
        subclaimRepository = Sets.newConcurrentHashSet();

        loadSubclaims();

        builderManager.onEnable();
    }

    @Override
    public void onDisable() {
        builderManager.onDisable();

        saveSubclaims();

        executor = null;
        subclaimRepository = null;
        builderManager = null;
    }

    private void loadSubclaims() {
        final long pre = Time.now();
        final Mongo mdb = (Mongo) plugin.getConnectable(Mongo.class);
        if (mdb == null) {
            plugin.getAresLogger().error("attempted to save claims with null mongo instance");
            return;
        }

        final MongoDatabase db = mdb.getDatabase(plugin.getConfiguration().getMongoDatabaseName());
        if (db == null) {
            plugin.getAresLogger().error("attempted to save claims with null db instance");
            return;
        }

        final MongoCollection<Document> coll = db.getCollection(plugin.getConfiguration().getSubclaimCollection());
        final FindIterable<Document> docs = coll.find();

        try (MongoCursor<Document> cursor = docs.cursor()) {
            while (cursor.hasNext()) {
                final Document doc = cursor.next();
                final Subclaim claim = new Subclaim(this).fromDocument(doc);
                subclaimRepository.add(claim);
            }
        }

        final long post = Time.now();
        final long diff = post - pre;
        plugin.getAresLogger().info("loaded " + subclaimRepository.size() + " subclaims (took " + diff + "ms)");
    }

    private void saveSubclaims() {
        final Mongo mdb = (Mongo) plugin.getConnectable(Mongo.class);
        if (mdb == null) {
            plugin.getAresLogger().error("attempted to save claims with null mongo instance");
            return;
        }

        final MongoDatabase db = mdb.getDatabase(plugin.getConfiguration().getMongoDatabaseName());
        if (db == null) {
            plugin.getAresLogger().error("attempted to save claims with null db instance");
            return;
        }

        final MongoCollection<Document> coll = db.getCollection(plugin.getConfiguration().getSubclaimCollection());

        subclaimRepository.forEach(c -> {
            final Document existing = coll.find(Filters.eq("uuid", c.getUniqueId().toString())).first();

            if (existing != null) {
                coll.replaceOne(existing, c.toDocument());
            } else {
                coll.insertOne(c.toDocument());
            }
        });

        plugin.getAresLogger().info("wrote " + subclaimRepository.size() + " subclaims to db");
    }

    public DeleteResult deleteSubclaim(Subclaim claim) {
        final Mongo mdb = (Mongo) plugin.getConnectable(Mongo.class);
        if (mdb == null) {
            plugin.getAresLogger().error("attempted to delete subclaim with null mongo instance");
            return null;
        }

        final MongoDatabase db = mdb.getDatabase(plugin.getConfiguration().getMongoDatabaseName());
        if (db == null) {
            plugin.getAresLogger().error("attempted to delete subclaim with null db instance");
            return null;
        }

        final MongoCollection<Document> coll = db.getCollection(plugin.getConfiguration().getSubclaimCollection());
        return coll.deleteOne(Filters.eq("uuid", claim.getUniqueId().toString()));
    }


    /**
     * Returns a Subclaim matching a UUID
     * @param uniqueId UUID
     * @return Subclaim
     */
    public Subclaim getSubclaimById(UUID uniqueId) {
        return subclaimRepository
                .stream()
                .filter(subclaim -> subclaim.getUniqueId().equals(uniqueId))
                .findAny()
                .orElse(null);
    }

    /**
     * Returns a Subclaim matching a Player Faction and a Name
     * @param faction Faction
     * @param name Name
     * @return Subclaim
     */
    public Subclaim getSubclaimByName(PlayerFaction faction, String name) {
        return subclaimRepository
                .stream()
                .filter(subclaim -> subclaim.getOwner().equals(faction.getUniqueId()) && subclaim.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns a Subclaim at the provided location
     * @param location Location
     * @return Subclaim
     */
    public Subclaim getSubclaimAt(ILocatable location) {
        return subclaimRepository
                .stream()
                .filter(subclaim -> subclaim.isInside(location))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns an Immutable List of Subclaims that belong to the provided Player Faction
     * @param faction Player Faction
     * @return Immutable List of Subclaims
     */
    public ImmutableList<Subclaim> getSubclaimsByOwner(IFaction faction) {
        return getSubclaimsByOwner(faction.getUniqueId());
    }

    /**
     * Returns an Immutable List of Subclaims that belong to the provided Player Faction UUID
     * @param ownerId Player Faction UUID
     * @return Immutable List of Subclaims
     */
    public ImmutableList<Subclaim> getSubclaimsByOwner(UUID ownerId) {
        return ImmutableList.copyOf(subclaimRepository.stream().filter(subclaim -> subclaim.getOwner().equals(ownerId)).collect(Collectors.toList()));
    }
}
