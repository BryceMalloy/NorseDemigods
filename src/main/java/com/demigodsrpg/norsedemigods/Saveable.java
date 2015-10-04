package com.demigodsrpg.norsedemigods;

import java.util.Map;

public interface Saveable {

    String getKey();

    Map<String, Object> serialize();

    default NorseDemigods getBackend() {
        return NorseDemigods.INST;
    }
}
