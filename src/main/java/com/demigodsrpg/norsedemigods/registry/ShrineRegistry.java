package com.demigodsrpg.norsedemigods.registry;

import com.demigodsrpg.norsedemigods.NorseDemigods;
import com.demigodsrpg.norsedemigods.saveable.ShrineSaveable;
import com.demigodsrpg.norsedemigods.util.FJsonSection;
import org.bukkit.Location;

import java.util.Optional;

public class ShrineRegistry extends AbstractRegistry<ShrineSaveable> {

    public ShrineRegistry(NorseDemigods backend) {
        super(backend, "shrine", true);
    }

    @Override
    protected ShrineSaveable fromFJsonSection(String key, FJsonSection section) {
        return new ShrineSaveable(section);
    }

    public Optional<ShrineSaveable> fromLocation(Location location) {
        return fromKey(getLocationKey(location));
    }

    public ShrineSaveable newShrine(String deity, String name, String mojangId, Location location) {
        String key = getLocationKey(location);
        Optional<ShrineSaveable> opData = fromKey(key);
        return opData.orElseGet(() -> put(key, new ShrineSaveable(deity, name, mojangId, location)));
    }

    private String getLocationKey(Location location) {
        return location.getBlockX() + "-" + location.getBlockY() + "-" + location.getBlockZ() + "-" +
                location.getWorld().getName();
    }
}
