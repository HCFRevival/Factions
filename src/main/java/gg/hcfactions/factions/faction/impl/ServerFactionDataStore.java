package gg.hcfactions.factions.faction.impl;

import gg.hcfactions.factions.models.faction.impl.ServerFaction;
import gg.hcfactions.libs.base.connect.impl.mongo.IMongoDataStore;

public class ServerFactionDataStore implements IMongoDataStore<ServerFaction> {
    @Override
    public ServerFaction findById(String s) {
        return null;
    }

    @Override
    public <V> ServerFaction findByKeyValue(String s, V v) {
        return null;
    }

    @Override
    public void insertOne(ServerFaction serverFaction) {

    }

    @Override
    public void updateOne(ServerFaction serverFaction, ServerFaction t1) {

    }

    @Override
    public void deleteOne(ServerFaction serverFaction) {

    }
}
