package com.demigodsrpg.norsedemigods.saveable;

import com.demigodsrpg.norsedemigods.NorseDemigods;
import com.demigodsrpg.norsedemigods.util.FJsonSection;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerDataSaveable implements Saveable {

    // -- DATA -- //

    String MOJANG_ID;
    String LAST_KNOWN_NAME;
    String ALLIANCE;
    Map<String, Map<String, Object>> ABILITY_DATA;
    Map<String, String> BIND_DATA;

    // -- CONSTRUCTORS -- //

    public PlayerDataSaveable(Player player) {
        MOJANG_ID = player.getUniqueId().toString();
        LAST_KNOWN_NAME = player.getName();
        ALLIANCE = "MORTAL";
        ABILITY_DATA = new HashMap<>();
        BIND_DATA = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public PlayerDataSaveable(String mojangId, FJsonSection section) {
        MOJANG_ID = mojangId;
        LAST_KNOWN_NAME = section.getString("lastKnownName");
        ALLIANCE = section.getString("alliance");
        if (section.isSection("abilityData")) {
            ABILITY_DATA = (Map) section.getSectionNullable("abilityData").getValues();
        } else {
            ABILITY_DATA = new HashMap<>();
        }
        if (section.isSection("bindData")) {
            BIND_DATA = (Map) section.getSectionNullable("bindData").getValues();
        } else {
            BIND_DATA = new HashMap<>();
        }
    }

    // -- GETTERS -- //

    public UUID getPlayerId() {
        return UUID.fromString(MOJANG_ID);
    }

    @Override
    public String getKey() {
        return MOJANG_ID;
    }

    public String getLastKnownName() {
        return LAST_KNOWN_NAME;
    }

    public String getAlliance() {
        return ALLIANCE;
    }

    public Optional<Object> getAbilityData(String ability, String key) {
        return Optional.ofNullable(ABILITY_DATA.getOrDefault(ability, new HashMap<>()).
                getOrDefault(key, null));
    }

    public Optional<Material> getBind(String ability) {
        return Optional.ofNullable(Material.valueOf(BIND_DATA.getOrDefault(ability, "")));
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("lastKnownName", LAST_KNOWN_NAME);
        map.put("alliance", ALLIANCE);
        map.put("abilityData", ABILITY_DATA);
        map.put("bindData", BIND_DATA);
        return map;
    }

    // -- MUTATORS -- //

    public void setLastKnownName(String name) {
        LAST_KNOWN_NAME = name;
    }

    public void setAlliance(String alliance) {
        ALLIANCE = alliance;
    }

    public void setAbilityData(NorseDemigods backend, String ability, String key, Object value) {
        // Get the map for the ability, and set the data
        Map<String, Object> abilityMap = ABILITY_DATA.getOrDefault(ability, new HashMap<>());
        abilityMap.put(key, value);
        ABILITY_DATA.put(ability, abilityMap);

        // Put this version of the data object into the registry
        backend.getPlayerDataRegistry().put(MOJANG_ID, this);
    }

    public void removeBind(NorseDemigods backend, String ability) {
        // Remove the mention of this
        BIND_DATA.remove(ability);

        // Put this version of the data object into the registry
        backend.getPlayerDataRegistry().put(MOJANG_ID, this);
    }

    public void setBind(NorseDemigods backend, String ability, Material type) {
        // Set the bind data tot he map
        BIND_DATA.put(ability, type.name());

        // Put this version of the data object into the registry
        backend.getPlayerDataRegistry().put(MOJANG_ID, this);
    }
}
