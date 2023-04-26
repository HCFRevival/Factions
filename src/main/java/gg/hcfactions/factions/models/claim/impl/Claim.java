package gg.hcfactions.factions.models.claim.impl;

import com.google.common.collect.Lists;
import gg.hcfactions.factions.claims.ClaimManager;
import gg.hcfactions.factions.models.claim.IClaim;
import gg.hcfactions.libs.base.connect.impl.mongo.MongoDocument;
import gg.hcfactions.libs.bukkit.location.ILocatable;
import gg.hcfactions.libs.bukkit.location.impl.BLocatable;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

import java.util.List;
import java.util.UUID;

public final class Claim implements IClaim, MongoDocument<Claim> {
    @Getter public ClaimManager manager;
    @Getter public UUID uniqueId;
    @Getter public UUID owner;
    @Getter public BLocatable cornerA;
    @Getter public BLocatable cornerB;
    @Getter @Setter public double cost;

    public Claim(ClaimManager manager) {
        this.manager = manager;
        this.uniqueId = null;
        this.owner = null;
        this.cornerA = null;
        this.cornerB = null;
        this.cost = 0.0D;
    }

    public Claim(ClaimManager manager, UUID owner, BLocatable cornerA, BLocatable cornerB, double cost) {
        this.manager = manager;
        this.uniqueId = UUID.randomUUID();
        this.owner = owner;
        this.cornerA = cornerA;
        this.cornerB = cornerB;
        this.cost = cost;
    }

    /**
     * Collects all corners and returns them in an array
     * @return Array of claim corner locations
     */
    public List<BLocatable> getCorners() {
        final List<BLocatable> res = Lists.newArrayList();

        for (int i = 1; i <= 4; i++) {
            res.add(getCorner(i));
        }

        return res;
    }

    /**
     * Returns true if the provided location is within the buffer radius for this claim
     * @param location Location to compare against
     * @param buffer Buffer radius to check
     * @return True if location is within the provided buffer radius of this claim
     */
    public boolean isInsideBuffer(ILocatable location, double buffer) {
        if (!location.getWorldName().equals(cornerA.getWorldName())) {
            return false;
        }

        // Add X
        if (isInside(new BLocatable(location.getWorldName(), (location.getX() + buffer), location.getY(), location.getZ()), false)) {
            return true;
        }

        // Add Z
        if (isInside(new BLocatable(location.getWorldName(), location.getX(), location.getY(), (location.getZ() + buffer)), false)) {
            return true;
        }

        // Subtract X
        if (isInside(new BLocatable(location.getWorldName(), (location.getX() - buffer), location.getY(), location.getZ()), false)) {
            return true;
        }

        // Subtract Z
        if (isInside(new BLocatable(location.getWorldName(), location.getX(), location.getY(), (location.getZ() - buffer)), false)) {
            return true;
        }

        // Add X, Z
        if (isInside(new BLocatable(location.getWorldName(), (location.getX() + buffer), location.getY(), (location.getZ() + buffer)), false)) {
            return true;
        }

        // Subtract X, Z
        if (isInside(new BLocatable(location.getWorldName(), (location.getX() - buffer), location.getY(), (location.getZ() - buffer)), false)) {
            return true;
        }

        // Add X, Subtract Z
        if (isInside(new BLocatable(location.getWorldName(), (location.getX() + buffer), location.getY(), (location.getZ() - buffer)), false)) {
            return true;
        }

        // Subtract X, Add Z
        if (isInside(new BLocatable(location.getWorldName(), (location.getX() - buffer), location.getY(), (location.getZ() + buffer)), false)) {
            return true;
        }

        return false;
    }

    /**
     * Returns true if the provided location is touching this claim
     * @param location Location to compare
     * @return True if location is touching this claim
     */
    public boolean isTouching(ILocatable location) {
        if (!location.getWorldName().equals(cornerA.getWorldName())) {
            return false;
        }

        if (isInside(new BLocatable(location.getWorldName(), location.getX() + 1.0, location.getY(), location.getZ()), true)) {
            return true;
        }

        if (isInside(new BLocatable(location.getWorldName(), location.getX(), location.getY(), location.getZ() + 1.0), true)) {
            return true;
        }

        if (isInside(new BLocatable(location.getWorldName(), location.getX() - 1.0, location.getY(), location.getZ()), true)) {
            return true;
        }

        if (isInside(new BLocatable(location.getWorldName(), location.getX(), location.getY(), location.getZ() - 1.0), true)) {
            return true;
        }

        return false;
    }

    @Override
    public Claim fromDocument(Document document) {
        this.uniqueId = UUID.fromString(document.getString("uuid"));
        this.owner = UUID.fromString(document.getString("owner"));
        this.cornerA = new BLocatable().fromDocument(document.get("corner_a", Document.class));
        this.cornerB = new BLocatable().fromDocument(document.get("corner_b", Document.class));
        this.cost = document.getDouble("cost");

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("uuid", uniqueId.toString())
                .append("owner", owner.toString())
                .append("corner_a", cornerA.toDocument())
                .append("corner_b", cornerB.toDocument())
                .append("cost", cost);
    }
}
