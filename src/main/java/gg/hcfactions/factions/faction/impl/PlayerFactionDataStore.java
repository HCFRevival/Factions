package gg.hcfactions.factions.faction.impl;

import gg.hcfactions.factions.models.faction.impl.PlayerFaction;
import gg.hcfactions.libs.base.connect.impl.mongo.IMongoDataStore;

public class PlayerFactionDataStore implements IMongoDataStore<PlayerFaction> {
    @Override
    public PlayerFaction findById(String s) {
        return null;
    }

    @Override
    public <V> PlayerFaction findByKeyValue(String s, V v) {
        return null;
    }

    @Override
    public void insertOne(PlayerFaction playerFaction) {

    }

    @Override
    public void updateOne(PlayerFaction playerFaction, PlayerFaction t1) {

    }

    @Override
    public void deleteOne(PlayerFaction playerFaction) {

    }
}
