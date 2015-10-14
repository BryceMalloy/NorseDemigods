package com.demigodsrpg.norsedemigods;

import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public interface Deity extends Listener {

    String getName();

    String getDefaultAlliance();

    void printInfo(Player player);

    void onCommand(Player player, String label, String[] args, boolean bind);

    void onSyncTick(long timeSent);

    boolean canTribute();

    void onEvent(Event ee);

    default NorseDemigods getBackend() {
        return NorseDemigods.INST;
    }

    default List<UUID> getPlayerIds() {
        return getBackend().getPlayerDataRegistry().getFromDb().values().stream().filter(p -> p.getDeityList().
                contains(getName())).map(PlayerDataSaveable::getPlayerId).collect(Collectors.toList());
    }
}
