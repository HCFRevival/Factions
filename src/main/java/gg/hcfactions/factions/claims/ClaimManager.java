package gg.hcfactions.factions.claims;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.claim.impl.Claim;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.libs.base.connect.impl.mongo.Mongo;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.location.ILocatable;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ClaimManager implements IManager {
    @Getter public final Factions plugin;
    @Getter public ClaimBuilderManager claimBuilderManager;
    @Getter public BukkitTask claimAutosaveTask;
    @Getter public Set<Claim> claimRepository;

    public ClaimManager(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        this.claimBuilderManager = new ClaimBuilderManager(this);
        this.claimRepository = Sets.newConcurrentHashSet();

        claimBuilderManager.onEnable();

        loadClaims();

        // claim auto-save task
        this.claimAutosaveTask = new Scheduler(plugin).sync(() -> {
            final long start = Time.now();

            Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission(FPermissions.P_FACTIONS_ADMIN)).forEach(staff ->
                    staff.sendMessage(Component.text("Preparing to auto-save Claim data...", NamedTextColor.GRAY)));

            new Scheduler(plugin).async(() -> {
                saveClaims();

                new Scheduler(plugin).sync(() -> {
                    final long finish = Time.now();

                    Bukkit.getOnlinePlayers().stream().filter(onlinePlayer -> onlinePlayer.hasPermission(FPermissions.P_FACTIONS_ADMIN)).forEach(staff ->
                            staff.sendMessage(Component.text("Finished auto-saving Claim data (took " + (finish - start) + "ms)", NamedTextColor.GRAY)));
                }).run();
            }).run();
        }).repeat(plugin.getConfiguration().getClaimAutosaveDelay() * 20L, plugin.getConfiguration().getClaimAutosaveDelay() * 20L).run();
    }

    @Override
    public void onDisable() {
        if (claimAutosaveTask != null) {
            claimAutosaveTask.cancel();
            claimAutosaveTask = null;
        }

        claimBuilderManager.onDisable();

        saveClaims();

        this.claimBuilderManager = null;
        this.claimRepository = null;
    }

    public void saveClaims() {
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

        final MongoCollection<Document> coll = db.getCollection(plugin.getConfiguration().getClaimCollection());

        claimRepository.forEach(c -> {
            final Document existing = coll.find(Filters.eq("uuid", c.getUniqueId().toString())).first();

            if (existing != null) {
                coll.replaceOne(existing, c.toDocument());
            } else {
                coll.insertOne(c.toDocument());
            }
        });

        plugin.getAresLogger().info("wrote " + claimRepository.size() + " claims to db");
    }

    public void loadClaims() {
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

        final MongoCollection<Document> coll = db.getCollection(plugin.getConfiguration().getClaimCollection());
        final FindIterable<Document> docs = coll.find();

        try (MongoCursor<Document> cursor = docs.cursor()) {
            while (cursor.hasNext()) {
                final Document doc = cursor.next();
                final Claim claim = new Claim(this).fromDocument(doc);
                claimRepository.add(claim);
            }
        }

        final long post = Time.now();
        final long diff = post - pre;
        plugin.getAresLogger().info("loaded " + claimRepository.size() + " claims (took " + diff + "ms)");
    }

    public DeleteResult deleteClaim(Claim claim) {
        final Mongo mdb = (Mongo) plugin.getConnectable(Mongo.class);
        if (mdb == null) {
            plugin.getAresLogger().error("attempted to delete claim with null mongo instance");
            return null;
        }

        final MongoDatabase db = mdb.getDatabase(plugin.getConfiguration().getMongoDatabaseName());
        if (db == null) {
            plugin.getAresLogger().error("attempted to delete claim with null db instance");
            return null;
        }

        final MongoCollection<Document> coll = db.getCollection(plugin.getConfiguration().getClaimCollection());
        return coll.deleteOne(Filters.eq("uuid", claim.getUniqueId().toString()));
    }

    /**
     * Returns the claim at the provided locatable location
     * @param location Location
     * @return Claim
     */
    public Claim getClaimAt(ILocatable location) {
        return claimRepository.stream().filter(claim -> claim.isInside(location, false)).findFirst().orElse(null);
    }

    /**
     * Returns a claim by ID
     * @param uniqueId Claim ID
     * @return Claim
     */
    public Claim getClaimById(UUID uniqueId) {
        return claimRepository.stream().filter(claim -> claim.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    /**
     * Returns an Immutable Collection of Claims matching the provided Faction UUID
     * @param ownerId UUID
     * @return Immutable Collection of Claims
     */
    public ImmutableList<Claim> getClaimsByOwner(UUID ownerId) {
        return ImmutableList.copyOf(claimRepository.stream().filter(claim -> claim.getOwner().equals(ownerId)).collect(Collectors.toList()));
    }

    /**
     * Returns an Immutable Collection of Claims matching the provided Faction
     * @param faction Faction
     * @return Immutable Collection of Claims
     */
    public ImmutableList<Claim> getClaimsByOwner(IFaction faction) {
        return ImmutableList.copyOf(claimRepository.stream().filter(claim -> claim.getOwner().equals(faction.getUniqueId())).collect(Collectors.toList()));
    }

    /**
     * Returns an Immutable Collection of Claims that are nearby the provided location
     * @param location Location
     * @param distance Distance to check for
     * @return Immutable Collection of Claims
     */
    public ImmutableList<Claim> getClaimsNearby(ILocatable location, double distance) {
        return ImmutableList.copyOf(claimRepository.stream().filter(claim -> claim.isInsideBuffer(location, distance)).collect(Collectors.toList()));
    }

    /**
     * Returns an Immutable Collection of Claims that belong to Server Factions nearby
     * the provided location
     *
     * @param location Location
     * @param distance Distance to check for
     * @return Collection of Claims
     */
    public ImmutableList<Claim> getServerClaimsNearby(ILocatable location, double distance) {
        final List<Claim> res = Lists.newArrayList();

        claimRepository.forEach(claim -> {
            final ServerFaction sf = plugin.getFactionManager().getServerFactionById(claim.getOwner());

            if (sf != null && claim.isInsideBuffer(location, distance)) {
                res.add(claim);
            }
        });

        return ImmutableList.copyOf(res);
    }

    /**
     * Returns an Immutable Collection of Claims that are nearby the provided location
     * @param location Location
     * @param buildBuffer Enabling this will search for claims the location is within the build buffer of
     * @return Immutable Collection of Claims
     */
    public ImmutableList<Claim> getClaimsNearby(ILocatable location, boolean buildBuffer) {
        final List<Claim> result = Lists.newArrayList();

        for (Claim claim : claimRepository) {
            final IFaction faction = plugin.getFactionManager().getFactionById(claim.getOwner());

            if (faction == null) {
                continue;
            }

            if (faction instanceof final ServerFaction sf) {
                if (buildBuffer) {
                    if (claim.isInsideBuffer(location, sf.getBuildBuffer(), false)) {
                        result.add(claim);
                    }
                } else if (claim.isInsideBuffer(location, sf.getClaimBuffer(), false)) {
                    result.add(claim);
                }

                continue;
            }

            if (!buildBuffer && claim.isInsideBuffer(location, plugin.getConfiguration().getDefaultPlayerFactionClaimBuffer())) {
                result.add(claim);
            }
        }

        return ImmutableList.copyOf(result);
    }
}
