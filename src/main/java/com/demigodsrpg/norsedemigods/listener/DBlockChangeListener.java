package com.demigodsrpg.norsedemigods.listener;

import com.demigodsrpg.norsedemigods.NorseDemigods;
import com.google.common.collect.Iterables;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.metadata.MetadataValue;

import java.util.UUID;

public class DBlockChangeListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPhysics(EntityChangeBlockEvent event) {
        if (event.getEntityType().equals(EntityType.FALLING_BLOCK) && !event.getEntity().getMetadata("splode").isEmpty()) {
            MetadataValue value = Iterables.find(event.getEntity().getMetadata("splode"), value1 -> value1.getOwningPlugin().equals(NorseDemigods.getPlugin(NorseDemigods.class)), null);
            if (value != null) {
                event.setCancelled(true);
                UUID owner = UUID.fromString(value.asString());
                event.getEntity().getWorld().createExplosion(event.getEntity().getLocation(), 6);
                event.getEntity().remove();
            }
        }
    }
}
