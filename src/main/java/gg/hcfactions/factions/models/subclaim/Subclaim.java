package gg.hcfactions.factions.models.subclaim;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import gg.hcfactions.factions.FPermissions;
import gg.hcfactions.factions.claims.subclaims.SubclaimManager;
import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.libs.base.connect.impl.mongo.MongoDocument;
import gg.hcfactions.libs.bukkit.location.ILocatable;
import gg.hcfactions.libs.bukkit.location.IRegion;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import gg.hcfactions.libs.bukkit.location.impl.PLocatable;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public final class Subclaim implements IRegion, MongoDocument<Subclaim> {
    @Getter public final SubclaimManager subclaimManager;
    @Getter public UUID uniqueId;
    @Getter public UUID owner;
    @Getter public String name;
    @Getter public Set<UUID> members;
    @Getter public BLocatable cornerA;
    @Getter public BLocatable cornerB;

    public Subclaim(SubclaimManager subclaimManager) {
        this.subclaimManager = subclaimManager;
        this.uniqueId = null;
        this.owner = null;
        this.name = null;
        this.members = Sets.newConcurrentHashSet();
        this.cornerA = null;
        this.cornerB = null;
    }

    public Subclaim(
            SubclaimManager subclaimManager,
            UUID ownerId,
            String name,
            BLocatable cornerA,
            BLocatable cornerB
    ) {
        this.subclaimManager = subclaimManager;
        this.uniqueId = UUID.randomUUID();
        this.owner = ownerId;
        this.name = name;
        this.members = Sets.newConcurrentHashSet();
        this.cornerA = cornerA;
        this.cornerB = cornerB;
    }

    /**
     * Handles adding a UUID to this subclaim
     * @param uniqueId Bukkit UUID
     */
    public void addMember(UUID uniqueId) {
        if (isMember(uniqueId)) {
            return;
        }

        members.add(uniqueId);
    }

    /**
     * Handles removing a UUID from this subclaim
     * @param uniqueId Bukkit UUID
     */
    public void removeMember(UUID uniqueId) {
        members.removeIf(uuid -> uuid.equals(uniqueId));
    }

    /**
     * Returns true if the provided UUID is a member of this subclaim
     * @param uniqueId Bukkit UUID
     * @return True if provided UUID is a member of this subclaim
     */
    public boolean isMember(UUID uniqueId) {
        return members.contains(uniqueId);
    }

    /**
     * Sends a message to all players that have access to this subclaim
     * @param message Message
     */
    public void sendMessage(String message) {
        final PlayerFaction faction = subclaimManager.getPlugin().getFactionManager().getPlayerFactionById(owner);

        if (faction == null) {
            return;
        }

        final List<PlayerFaction.Member> members = faction
                .getOnlineMembers()
                .stream()
                .filter(member -> isMember(member.getUniqueId()) || !member.getRank().equals(PlayerFaction.Rank.MEMBER))
                .toList();

        for (PlayerFaction.Member member : members) {
            final Player player = Bukkit.getPlayer(member.getUniqueId());

            if (player != null) {
                player.sendMessage(message);
            }
        }
    }

    /**
     * Sends a component based message to every online player in this subclaim
     * @param component Component
     */
    public void sendMessage(Component component) {
        final PlayerFaction faction = subclaimManager.getPlugin().getFactionManager().getPlayerFactionById(owner);

        if (faction == null) {
            return;
        }

        final List<PlayerFaction.Member> members = faction
                .getOnlineMembers()
                .stream()
                .filter(member -> isMember(member.getUniqueId()) || !member.getRank().equals(PlayerFaction.Rank.MEMBER))
                .toList();

        for (PlayerFaction.Member member : members) {
            final Player player = Bukkit.getPlayer(member.getUniqueId());

            if (player != null) {
                player.sendMessage(component);
            }
        }
    }

    /**
     * Returns true if the provided player can access this subclaim
     * @param player
     * @return
     */
    public boolean canAccess(Player player) {
        final PlayerFaction faction = subclaimManager.getPlugin().getFactionManager().getPlayerFactionById(owner);
        final boolean bypass = player.hasPermission(FPermissions.P_FACTIONS_ADMIN);

        if (bypass) {
            return true;
        }

        if (faction == null) {
            return true;
        }

        final PlayerFaction.Member member = faction.getMember(player.getUniqueId());

        if (member == null) {
            return false;
        }

        if (member.getRank().equals(PlayerFaction.Rank.MEMBER) && !isMember(player.getUniqueId())) {
            return false;
        }

        return true;
    }

    /**
     * Returns a corner location for this region
     * @param cornerId Corner ID (1 - 4)
     * @return BLocatable
     */
    public BLocatable getCorner(int cornerId) {
        if (cornerId < 1 || cornerId > 5) {
            throw new ArrayIndexOutOfBoundsException("Corner out of bounds - Must be 1-4");
        }

        if (cornerId == 1) {
            return new BLocatable(cornerA.getWorldName(), cornerA.getX(), 64.0, cornerA.getZ());
        }

        if (cornerId == 2) {
            return new BLocatable(cornerA.getWorldName(), cornerB.getX(), 64.0, cornerA.getZ());
        }

        if (cornerId == 3) {
            return new BLocatable(cornerA.getWorldName(), cornerA.getX(), 64.0, cornerB.getZ());
        }

        if (cornerId == 4) {
            return new BLocatable(cornerA.getWorldName(), cornerB.getX(), 64.0, cornerB.getZ());
        }

        return null;
    }

    /**
     * Returns an array containing all four corners of this subclaim
     * @return Array of BLocatable
     */
    public BLocatable[] getCorners() {
        final BLocatable[] arr = new BLocatable[4];

        arr[0] = getCorner(1);
        arr[1] = getCorner(2);
        arr[2] = getCorner(3);
        arr[3] = getCorner(4);

        return arr;
    }

    /**
     * Returns the length x width of this subclaim
     * @return Array[0] = length, Array[1] = width
     */
    public int[] getSize() {
        final int[] result = new int[2];

        final double xMin = Math.min(cornerA.getX(), cornerA.getX());
        final double zMin = Math.min(cornerA.getZ(), cornerB.getZ());
        final double xMax = Math.max(cornerA.getX(), cornerB.getX());
        final double zMax = Math.max(cornerA.getZ(), cornerB.getZ());

        result[0] = (int)Math.round(Math.abs(xMax - xMin));
        result[1] = (int)Math.round(Math.abs(zMax - zMin));

        return result;
    }

    /**
     * @param ca Corner A
     * @param cb Corner B
     * @param world World
     * @return True if the provided cube overlaps this claim
     */
    public boolean isOverlapping(BLocatable ca, BLocatable cb, String world) {
        if (!cornerA.getWorldName().equals(world)) {
            return false;
        }

        final double[] values = new double[2];

        final double xMin = Math.min(cornerA.getX(), cornerB.getX());
        final double zMin = Math.min(cornerA.getZ(), cornerB.getZ());
        final double xMax = Math.max(cornerA.getX(), cornerB.getX());
        final double zMax = Math.max(cornerA.getZ(), cornerB.getZ());

        values[0] = ca.getX();
        values[1] = cb.getX();
        Arrays.sort(values);

        if (xMin > values[1] || xMax < values[0]) {
            return false;
        }

        values[0] = ca.getZ();
        values[1] = cb.getZ();
        Arrays.sort(values);

        if (zMin > values[1] || zMax < values[0]) {
            return false;
        }

        return true;
    }

    /**
     * Returns true if the provided locatable is inside this Subclaim region
     * @param location Location
     * @return True if inside
     */
    public boolean isInside(ILocatable location) {
        if (!location.getWorldName().equals(cornerA.getWorldName())) {
            return false;
        }

        double xMin = Math.min(cornerA.getX(), cornerB.getX());
        double yMin = Math.min(cornerA.getY(), cornerB.getY());
        double zMin = Math.min(cornerA.getZ(), cornerB.getZ());
        double xMax = Math.max(cornerA.getX(), cornerB.getX());
        double yMax = Math.max(cornerA.getY(), cornerB.getY());
        double zMax = Math.max(cornerA.getZ(), cornerB.getZ());

        if (location instanceof PLocatable) {
            xMax++;
            zMax++;
        }

        return
                location.getX() >= xMin && location.getX() <= xMax &&
                        location.getY() >= yMin && location.getY() <= yMax &&
                        location.getZ() >= zMin && location.getZ() <= zMax;
    }

    /**
     * Returns an Immutable List containing all BLocatable locations on the perimeter of this subclaim
     * @param y Y level to pull from (example: 64 = 32, **64**, -201)
     * @return Immutable List of BLocatable
     */
    public ImmutableList<BLocatable> getPerimeter(int y) {
        final List<BLocatable> locations = Lists.newArrayList();

        double xMin = Math.min(cornerA.getX(), cornerB.getX());
        double zMin = Math.min(cornerA.getZ(), cornerB.getZ());
        double xMax = Math.max(cornerA.getX(), cornerB.getX());
        double zMax = Math.max(cornerA.getZ(), cornerB.getZ());

        for (int x = (int)xMin; x <= (int)xMax; x++) {
            for (int z = (int)zMin; z <= (int)zMax; z++) {
                if (x == xMin || x == xMax || z == zMin || z == zMax) {
                    locations.add(new BLocatable(cornerA.getWorldName(), x, y, z));
                }
            }
        }

        return ImmutableList.copyOf(locations);
    }

    @Override
    public Subclaim fromDocument(Document document) {
        this.uniqueId = UUID.fromString(document.getString("uuid"));
        this.owner = UUID.fromString(document.getString("owner"));
        this.name = document.getString("name");
        this.cornerA = new BLocatable().fromDocument(document.get("corner_a", Document.class));
        this.cornerB = new BLocatable().fromDocument(document.get("corner_b", Document.class));

        this.members.addAll((List<UUID>)document.get("members", List.class));

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("uuid", uniqueId.toString())
                .append("owner", owner.toString())
                .append("name", name)
                .append("corner_a", cornerA.toDocument())
                .append("corner_b", cornerB.toDocument())
                .append("members", members);
    }
}
