package gg.hcfactions.factions.player;

import com.google.common.collect.Sets;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.Factions;
import gg.hcfactions.factions.manager.IManager;
import gg.hcfactions.factions.models.player.IFactionPlayer;
import gg.hcfactions.factions.models.player.impl.FactionPlayer;
import gg.hcfactions.libs.base.connect.impl.mongo.Mongo;
import gg.hcfactions.libs.base.util.Time;
import gg.hcfactions.libs.bukkit.scheduler.Scheduler;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.UUID;

public final class PlayerManager implements IManager {
    @Getter public Factions plugin;
    @Getter public Set<IFactionPlayer> playerRepository;
    @Getter public BukkitTask playerAutosaveTask;

    public PlayerManager(Factions plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onEnable() {
        this.playerRepository = Sets.newConcurrentHashSet();

        // player auto-save task
        this.playerAutosaveTask = new Scheduler(plugin).sync(() -> {
            final long start = Time.now();

            Bukkit.getOnlinePlayers().stream().filter(onlinePlayer -> onlinePlayer.hasPermission(FPermissions.P_FACTIONS_ADMIN)).forEach(staff ->
                    staff.sendMessage(ChatColor.GRAY + "Preparing to auto-save Player Data..."));

            new Scheduler(plugin).async(() -> {
                savePlayers();

                new Scheduler(plugin).sync(() -> {
                    final long finish = Time.now();

                    Bukkit.getOnlinePlayers().stream().filter(onlinePlayer -> onlinePlayer.hasPermission(FPermissions.P_FACTIONS_ADMIN)).forEach(staff ->
                            staff.sendMessage(ChatColor.GRAY + "Finished auto-saving Player data (took " + (finish - start) + "ms)"));
                }).run();
            }).run();
        }).repeat(plugin.getConfiguration().getPlayerAutosaveDelay() * 20L, plugin.getConfiguration().getPlayerAutosaveDelay() * 20L).run();
    }

    @Override
    public void onDisable() {
        if (playerAutosaveTask != null) {
            playerAutosaveTask.cancel();
            playerAutosaveTask = null;
        }

        savePlayers();
    }

    public IFactionPlayer loadPlayer(Bson filter, boolean cache) {
        final Mongo mdb = (Mongo) plugin.getConnectable(Mongo.class);
        if (mdb == null) {
            plugin.getAresLogger().error("attempted to load player with null mongo instance");
            return null;
        }

        final MongoDatabase db = mdb.getDatabase(plugin.getConfiguration().getMongoDatabaseName());
        if (db == null) {
            plugin.getAresLogger().error("attempted to load player with null db instance");
            return null;
        }

        final MongoCollection<Document> coll = db.getCollection(plugin.getConfiguration().getFactionPlayerCollection());
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

        final MongoDatabase db = mdb.getDatabase(plugin.getConfiguration().getMongoDatabaseName());
        if (db == null) {
            plugin.getAresLogger().error("attempted to save player with null db instance");
            return;
        }

        final MongoCollection<Document> coll = db.getCollection(plugin.getConfiguration().getFactionPlayerCollection());
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
