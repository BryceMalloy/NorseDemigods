package com.demigodsrpg.norsedemigods;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

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
}
