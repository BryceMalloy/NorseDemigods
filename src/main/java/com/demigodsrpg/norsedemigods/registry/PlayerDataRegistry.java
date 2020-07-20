package com.demigodsrpg.norsedemigods.registry;

import com.demigodsrpg.norsedemigods.NorseDemigods;
import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
import com.demigodsrpg.norsedemigods.util.FJsonSection;
import org.bukkit.entity.Player;

import java.util.Optional;

public class PlayerDataRegistry extends AbstractRegistry<PlayerDataSaveable> {

    public PlayerDataRegistry(NorseDemigods backend) {
        super(backend, "player", true);
    }

    @Override
    protected PlayerDataSaveable fromFJsonSection(String key, FJsonSection section) {
        return new PlayerDataSaveable(key, section);
    }

    public PlayerDataSaveable fromPlayer(Player player) {
        Optional<PlayerDataSaveable> opData = fromKey(player.getUniqueId().toString());
        return opData.orElseGet(() -> put(player.getUniqueId().toString(), new PlayerDataSaveable(player)));
    }

    public void purgeTempData() {
        getFromDb().values().forEach(PlayerDataSaveable::purgeTempData);
    }
}
