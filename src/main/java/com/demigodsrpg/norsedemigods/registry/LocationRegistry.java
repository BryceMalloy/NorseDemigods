package com.demigodsrpg.norsedemigods.registry;

import com.demigodsrpg.norsedemigods.NorseDemigods;
import com.demigodsrpg.norsedemigods.saveable.LocationSaveable;
import com.demigodsrpg.norsedemigods.util.FJsonSection;
import org.bukkit.Location;

import java.util.Optional;

public class LocationRegistry extends AbstractRegistry<LocationSaveable> {

    public LocationRegistry(NorseDemigods backend) {
        super(backend, "loc", true);
    }

    @Override
    protected LocationSaveable fromFJsonSection(String key, FJsonSection section) {
        return new LocationSaveable(section);
    }

    public Optional<LocationSaveable> fromLocation(Location location) {
        return fromKey(getLocationKey(location));
    }

    public LocationSaveable getOrNew(Location location) {
        String key = getLocationKey(location);
        Optional<LocationSaveable> opData = fromKey(key);
        if (opData.isPresent()) {
            return opData.get();
        }
        return put(key, new LocationSaveable(location.getWorld().getName(), location.getBlockX(), location.getBlockY(),
                location.getBlockZ()));
    }

    private String getLocationKey(Location location) {
        return location.getBlockX() + "-" + location.getBlockY() + "-" + location.getBlockZ() + "-" +
                location.getWorld().getName();
    }
}
