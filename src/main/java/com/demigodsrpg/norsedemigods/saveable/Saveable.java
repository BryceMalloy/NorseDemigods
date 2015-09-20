package com.demigodsrpg.norsedemigods.saveable;

import java.util.Map;

public interface Saveable {

    String getKey();

    Map<String, Object> serialize();
}
