package gg.hcfactions.factions.models.faction.impl;

import gg.hcfactions.factions.models.faction.IFaction;
import gg.hcfactions.libs.base.connect.impl.mongo.MongoDocument;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bukkit.ChatColor;

import java.util.UUID;

public final class ServerFaction implements IFaction, MongoDocument<ServerFaction> {
    @Getter public UUID uniqueId;
    @Getter @Setter public String name;
    @Getter @Setter public String displayName;
    @Getter @Setter public Flag flag;

    @Override
    public ServerFaction fromDocument(Document document) {
        this.uniqueId = UUID.fromString(document.getString("uuid"));
        this.name = document.getString("name");
        this.displayName = document.getString("display_name");
        this.flag = document.get("flag", Flag.class);

        if (displayName != null) {
            displayName = ChatColor.translateAlternateColorCodes('&', displayName);
        }

        return this;
    }

    @Override
    public Document toDocument() {
        return new Document()
                .append("uuid", uniqueId.toString())
                .append("name", name)
                .append("display_name", displayName)
                .append("flag", flag);
    }

    @AllArgsConstructor
    public enum Flag {
        SAFEZONE(ChatColor.GREEN + "Safezone"),
        EVENT(ChatColor.DARK_AQUA + "Event"),
        LANDMARK(ChatColor.LIGHT_PURPLE + "Landmark");

        @Getter public final String displayName;

        /**
         * Returns the Server Faction Flag by name
         * @param name Flag name
         * @return ServerFaction.Flag
         */
        public static Flag getFlagByName(String name) {
            for (Flag v : values()) {
                if (v.name().equalsIgnoreCase(name)) {
                    return v;
                }
            }

            return null;
        }
    }
}
