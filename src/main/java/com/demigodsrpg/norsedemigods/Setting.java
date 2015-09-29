package com.demigodsrpg.norsedemigods;

import org.bukkit.configuration.Configuration;

public class Setting {
    public static final int MAX_TARGET_RANGE = getConfig().getInt("max_target_range"); // maximum range on targeting
    public static final int MAXIMUM_HP = getConfig().getInt("max_hp"); // max hp a player can have
    public static final int ASCENSION_CAP = getConfig().getInt("ascension_cap"); // max levels
    public static final int FAVOR_CAP = getConfig().getInt("globalfavorcap"); // max favor
    public static final boolean BROADCAST_NEW_DEITY = getConfig().getBoolean("broadcast_new_deities"); // tell server when a player gets a deity
    public static final boolean ALLOW_PVP_EVERYWHERE = getConfig().getBoolean("allow_skills_everywhere");
    public static final boolean USE_NEW_PVP = getConfig().getBoolean("use_new_pvp_zones");
    public static final int NOOB_LEVEL = getConfig().getInt("noob_level");

    public static Configuration getConfig() {
        return NorseDemigods.INST.getConfig();
    }
}
