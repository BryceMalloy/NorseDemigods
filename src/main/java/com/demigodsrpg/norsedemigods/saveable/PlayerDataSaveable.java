package com.demigodsrpg.norsedemigods.saveable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class PlayerDataSaveable implements Saveable {

    ConcurrentMap<String, ConcurrentMap<String, Object>> DATA = new ConcurrentHashMap<>();

    public UUID getPlayerId() {
        // TODO
    }

    @Override
    public String getKey() {
        return null;
    }

    @Override
    public Map<String, Object> serialize() {
        return null;
    }
}
