package com.demigodsrpg.norsedemigods.saveable;

import com.demigodsrpg.norsedemigods.NorseDemigods;
import com.demigodsrpg.norsedemigods.Saveable;
import com.demigodsrpg.norsedemigods.util.FJsonSection;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class LocationSaveable implements Saveable {
    int X;
    int Y;
    int Z;
    String WORLD;

    public LocationSaveable(String world, int x, int y, int z) {
        X = x;
        Y = y;
        Z = z;
        WORLD = world;
    }

    public LocationSaveable(String key) {
        this(key.split("-")[3], Integer.valueOf(key.split("-")[0]), Integer.valueOf(key.split("-")[1]),
                Integer.valueOf(key.split("-")[2]));
    }

    public LocationSaveable(FJsonSection section) {
        X = section.getInt("X");
        Y = section.getInt("Y");
        Z = section.getInt("Z");
        WORLD = section.getString("WORLD");
    }

    public String getWorld() {
        return WORLD;
    }

    public int getX() {
        return X;
    }

    public int getY() {
        return Y;
    }

    public int getZ() {
        return Z;
    }

    public Location toLocationNewWorld(World w) {
        return new Location(w, X, Y, Z);
    }

    public Location toLocation(NorseDemigods instance) {
        return new Location(instance.getServer().getWorld(WORLD), X, Y, Z);
    }

    public boolean equalsApprox(LocationSaveable other) {
        return ((X == other.getX()) && (Y == other.getY()) && (Z == other.getZ()) && WORLD.equals(other.getWorld()));
    }

    @Override
    public String getKey() {
        return X + "-" + Y + "-" + Z + "-" + WORLD;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>(4);
        map.put("X", X);
        map.put("Y", Y);
        map.put("Z", Z);
        map.put("WORLD", WORLD);
        return map;
    }
}
