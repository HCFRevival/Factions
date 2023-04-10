package gg.hcfactions.factions.models.faction.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.models.econ.IBankable;
import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.factions.models.message.FMessage;
import gg.hcfactions.factions.models.timer.ITimeable;
import gg.hcfactions.factions.models.timer.ETimerType;
import gg.hcfactions.factions.models.timer.impl.FTimer;
import gg.hcfactions.libs.base.connect.impl.mongo.MongoDocument;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PlayerFaction implements IFaction, IBankable, ITimeable, MongoDocument<PlayerFaction> {
    @Getter public UUID uniqueId;
    @Getter @Setter public String name;
    @Getter @Setter public String announcement;
    @Getter @Setter public PLocatable homeLocation;
    @Getter @Setter public PLocatable rallyLocation;
    @Getter @Setter public int rating;
    @Getter @Setter public double balance;
    @Getter @Setter public double dtr;
    @Getter @Setter public int reinvites;
    @Getter @Setter public long nextTick;
    @Getter @Setter public long lastRallyUpdate;
    @Getter public Set<Member> members;
    @Getter public Set<UUID> memberHistory;
    @Getter public Set<UUID> pendingInvites;
    @Getter public Set<FTimer> timers;

    @Getter public transient Scoreboard scoreboard;

    public PlayerFaction() {
        this.uniqueId = null;
        this.name = null;
        this.announcement = null;
        this.homeLocation = null;
        this.rallyLocation = null;
        this.rating = 0;
        this.balance = 0.0;
        this.dtr = 0.0;
        this.reinvites = 0;
        this.nextTick = 0L;
        this.lastRallyUpdate = 0L;
        this.members = Sets.newConcurrentHashSet();
        this.memberHistory = Sets.newConcurrentHashSet();
        this.pendingInvites = Sets.newConcurrentHashSet();
        this.timers = Sets.newConcurrentHashSet();

        setupScoreboard();
    }

    public PlayerFaction(String name) {
        this.uniqueId = UUID.randomUUID();
        this.name = name;
        this.announcement = null;
        this.homeLocation = null;
        this.rallyLocation = null;
        this.rating = 0;
        this.balance = 0.0;
        this.dtr = 0.0;
        this.reinvites = 0;
        this.nextTick = 0L;
        this.lastRallyUpdate = 0L;
        this.members = Sets.newConcurrentHashSet();
        this.memberHistory = Sets.newConcurrentHashSet();
        this.pendingInvites = Sets.newConcurrentHashSet();
        this.timers = Sets.newConcurrentHashSet();

        setupScoreboard();
    }

    /**
     * Performs initial scoreboard configuration
     */
    public void setupScoreboard() {
        this.scoreboard = Objects.requireNonNull(Bukkit.getScoreboardManager()).getNewScoreboard();

        final Team friendly = scoreboard.registerNewTeam("members");
        friendly.setColor(ChatColor.DARK_GREEN);
        friendly.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.ALWAYS);
        friendly.setCanSeeFriendlyInvisibles(true);
    }

    /**
     * Applies faction scoreboard to provided player
     * @param player Player
     */
    public void setupScoreboard(Player player) {
        if (scoreboard == null) {
            throw new NullPointerException("attempted to set null scoreboard");
        }

        Objects.requireNonNull(scoreboard.getTeam("members")).addEntry(player.getName());
        player.setScoreboard(scoreboard);
    }

    /**
     * Revokes faction scoreboard from provided player
     * @param player Player
     */
    public void destroyScoreboard(Player player) {
        if (scoreboard != null) {
            Objects.requireNonNull(scoreboard.getTeam("members")).removeEntry(player.getName());
        }

        player.setScoreboard(Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard());
    }

    /**
     * Returns true if this faction is considered raidable
     * @return True if faction is raidable
     */
    public boolean isRaidable() {
        return dtr <= 0.0;
    }

    /**
     * Returns a member matching the provided UUID
     * @param uniqueId Bukkit UUID
     * @return Member
     */
    public Member getMember(UUID uniqueId) {
        return members.stream().filter(m -> m.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    /**
     * Returns true if the provided UUID is a member of this faction
     * @param uniqueId Bukkit UUID
     * @return True if member of this faction
     */
    public boolean isMember(UUID uniqueId) {
        return members.stream().anyMatch(m -> m.getUniqueId().equals(uniqueId));
    }

    public boolean isMember(Player player) {
        return isMember(player.getUniqueId());
    }

    /**
     * Returns an immutable list of members that are currently online
     * @return List<Member>
     */
    public ImmutableList<Member> getOnlineMembers() {
        return ImmutableList.copyOf(members.stream().filter(m -> Bukkit.getPlayer(m.getUniqueId()) != null).collect(Collectors.toList()));
    }

    /**
     * Returns an immutable list of members that have the provided rank
     * @param rank Rank to query
     * @return List<Member>
     */
    public ImmutableList<Member> getMembersByRank(Rank rank) {
        return ImmutableList.copyOf(members.stream().filter(m -> m.getRank().equals(rank)).collect(Collectors.toList()));
    }

    /**
     * Add a new member of the faction
     * @param playerUid Bukkit UUID
     * @param rank Faction rank
     */
    public void addMember(UUID playerUid, Rank rank) {
        if (isMember(playerUid)) {
            return;
        }

        final Member factionMember = new Member(playerUid, rank, ChatChannel.PUBLIC);
        members.add(factionMember);
        memberHistory.add(playerUid);
    }

    /**
     * Add a new member to the faction
     * @param playerUid Bukkit UUID
     */
    public void addMember(UUID playerUid) {
        addMember(playerUid, Rank.MEMBER);
    }

    /**
     * Remove a member from the faction
     * @param playerUid Bukkit UUID
     */
    public void removeMember(UUID playerUid) {
        if (!isMember(playerUid)) {
            return;
        }

        final Member factionMember = getMember(playerUid);
        members.remove(factionMember);
    }

    /**
     * Send a message to online members in the faction
     * @param message Message to display
     */
    public void sendMessage(String message) {
        getOnlineMembers().forEach(m -> {
            final Player player = m.getBukkit();
            if (player != null) {
                player.sendMessage(message);
            }
        });
    }

    /**
     * Returns the DTR cap for this faction
     * @return DTR cap (double)
     */
    public double getMaxDtr() {
        return 10.0; // TODO: Implement max dtr
    }

    /**
     * @return True if the faction home is set
     */
    public boolean hasHome() {
        return homeLocation != null;
    }

    /**
     * Returns true if this factions power is frozen
     * @return True if faction power frozen
     */
    public boolean isFrozen() {
        final FTimer frozenTimer = getTimer(ETimerType.FREEZE);
        return frozenTimer != null && frozenTimer.isExpired();
    }

    @Override
    public void finishTimer(ETimerType type) {
        if (type.equals(ETimerType.FREEZE)) {
            sendMessage(FMessage.T_FREEZE_EXPIRE);
        }

        removeTimer(type);
    }

    @Override
    public PlayerFaction fromDocument(Document document) {
        this.uniqueId = UUID.fromString(document.getString("uuid"));
        this.name = document.getString("name");

        if (document.containsKey("announcement")) {
            this.announcement = document.getString("announcement");
        }

        if (document.containsKey("home")) {
            this.homeLocation = new PLocatable().fromDocument(document.get("home", Document.class));
        }

        if (document.containsKey("rally")) {
            this.rallyLocation = new PLocatable().fromDocument(document.get("rally", Document.class));
        }

        if (document.containsKey("members")) {
            final List<Document> memberDocs = document.getList("members", Document.class);
            memberDocs.forEach(m -> members.add(new Member().fromDocument(m)));
        }

        if (document.containsKey("timers")) {
            final List<Document> timerDocs = document.getList("timers", Document.class);
            timerDocs.forEach(td -> timers.add(new FTimer().fromDocument(td)));
        }

        this.rating = document.getInteger("rating");
        this.balance = document.getDouble("balance");
        this.dtr = document.getDouble("dtr");
        this.reinvites = document.getInteger("reinvites");
        this.lastRallyUpdate = document.getLong("last_rally_update");
        this.memberHistory.addAll((List<UUID>)document.get("member_history", List.class));
        this.pendingInvites.addAll((List<UUID>)document.get("pending_invites", List.class));

        return this;
    }

    @Override
    public Document toDocument() {
        final Document doc = new Document();
        final List<Document> timerDocs = Lists.newArrayList();
        final List<Document> memberDocs = Lists.newArrayList();

        members.forEach(m -> memberDocs.add(m.toDocument()));
        timers.forEach(t -> timerDocs.add(t.toDocument()));

        doc.append("uuid", uniqueId.toString());
        doc.append("name", name);

        if (announcement != null) {
            doc.append("announcement", announcement);
        }

        if (homeLocation != null) {
            doc.append("home", homeLocation.toDocument());
        }

        if (rallyLocation != null) {
            doc.append("rally", rallyLocation.toDocument());
        }

        doc.append("rating", rating);
        doc.append("balance", balance);
        doc.append("dtr", dtr);
        doc.append("reinvites", reinvites);
        doc.append("last_rally_update", lastRallyUpdate);
        doc.append("members", memberDocs);
        doc.append("member_history", memberHistory);
        doc.append("pending_invites", pendingInvites);
        doc.append("timers", timerDocs);

        return doc;
    }

    public class Member implements MongoDocument<Member> {
        @Getter public UUID uniqueId;
        @Getter @Setter public Rank rank;
        @Getter @Setter public ChatChannel channel;

        public Member() {
            this.uniqueId = null;
            this.rank = null;
            this.channel = null;
        }

        public Member(UUID uniqueId, Rank rank, ChatChannel channel) {
            this.uniqueId = uniqueId;
            this.rank = rank;
            this.channel = channel;
        }

        /**
         * Returns bukkit player instance
         * @return Bukkit Player
         */
        public Player getBukkit() {
            return Bukkit.getPlayer(uniqueId);
        }

        @Override
        public Member fromDocument(Document document) {
            this.uniqueId = UUID.fromString(document.getString("uuid"));
            this.rank = Rank.getRankByName(document.getString("rank"));
            this.channel = ChatChannel.getChannelByName(document.getString("chat_channel"));

            return this;
        }

        @Override
        public Document toDocument() {
            return new Document()
                    .append("uuid", uniqueId.toString())
                    .append("rank", rank.name())
                    .append("chat_channel", channel.name());
        }
    }

    @AllArgsConstructor
    public enum Rank {
        MEMBER(0, "Member"),
        OFFICER(1, "Officer"),
        LEADER(2, "Leader");

        @Getter public final int weight;
        @Getter public final String displayName;

        /**
         * Returns true if this rank is higher than the provided rank
         * @param other Rank
         * @return True if higher
         */
        public boolean isHigher(Rank other) {
            return this.getWeight() > other.getWeight();
        }

        /**
         * Returns true if this rank is higher or equal to the provided rank
         * @param other Rank
         * @return True if higher or equal
         */
        public boolean isHigherOrEqual(Rank other) {
            return this.getWeight() >= other.getWeight();
        }

        /**
         * Returns the next rank in order of this rank
         * @return Next Rank
         */
        public Rank getNext() {
            switch (this) {
                case MEMBER: return OFFICER;
                case OFFICER: return LEADER;
                default: return null;
            }
        }

        /**
         * Returns a rank matching the provided name
         * @param name Name to query
         * @return Rank
         */
        public static Rank getRankByName(String name) {
            for (Rank v : values()) {
                if (v.name().equalsIgnoreCase(name)) {
                    return v;
                }
            }

            return null;
        }
    }

    public enum ChatChannel {
        PUBLIC,
        FACTION;

        /**
         * Returns a chat channel matching the provided name
         * @param name Chat channel name
         * @return ChatChannel
         */
        public static ChatChannel getChannelByName(String name) {
            for (ChatChannel v : values()) {
                if (v.name().equalsIgnoreCase(name)) {
                    return v;
                }
            }

            return null;
        }
    }
}
