package com.demigodsrpg.norsedemigods.saveable;

import com.demigodsrpg.norsedemigods.util.FJsonSection;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShrineSaveable extends LocationSaveable {

    // -- DATA -- //

    String deity;
    String name;
    String ownerId;
    List<String> guestIds;

    // -- CONSTRUCTORS -- //

    public ShrineSaveable(String deity, String name, String mojangId, Location location) {
        this(deity, name, mojangId, location.getWorld().getName(), location.getBlockX(),
                location.getBlockY(), location.getBlockZ());
    }

    public ShrineSaveable(String deity, String name, String ownerId, String world, int x, int y, int z) {
        super(world, x, y, z);
        this.deity = deity;
        this.name = name;
        this.ownerId = ownerId;
        guestIds = new ArrayList<>();
    }

    public ShrineSaveable(FJsonSection section) {
        super(section);
        deity = section.getString("deity");
        name = section.getString("name");
        ownerId = section.getString("ownerId");
        guestIds = section.getStringList("guestIds");
    }

    // -- GETTERS -- //

    public String getDeity() {
        return deity;
    }

    public String getName() {
        return name;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public List<String> getGuestIds() {
        return guestIds;
    }

    public boolean equalsApprox(ShrineSaveable other) {
        return ((X == other.getX()) && (Y == other.getY()) && (Z == other.getZ()) && WORLD.equals(other.getWorld()));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = super.serialize();
        map.put("deity", deity);
        map.put("name", name);
        map.put("ownerId", ownerId);
        map.put("guestIds", guestIds);
        return map;
    }

    // -- MUTATORS -- //


    public void setName(String name) {
        this.name = name;
        getBackend().getShrineRegistry().put(getKey(), this);
    }

    public void addGuest(String guestId) {
        if (!guestIds.contains(guestId)) {
            guestIds.add(guestId);
        }
        getBackend().getShrineRegistry().put(getKey(), this);
    }

    public void removeGuest(String guestId) {
        guestIds.remove(guestId);
        getBackend().getShrineRegistry().put(getKey(), this);
    }
}
