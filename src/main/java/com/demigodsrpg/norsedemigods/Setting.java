package com.demigodsrpg.norsedemigods;

import org.bukkit.configuration.Configuration;

public class Setting {
    public static final int MAX_TARGET_RANGE = getConfig().getInt("max_target_range", 100); // maximum range on targeting
    public static final int MAXIMUM_HP = getConfig().getInt("max_hp", 350); // max hp a player can have
    public static final int ASCENSION_CAP = getConfig().getInt("ascension_cap", 120); // max levels
    public static final int FAVOR_CAP = getConfig().getInt("globalfavorcap", 20_000); // max favor
    public static final boolean BROADCAST_NEW_DEITY = getConfig().getBoolean("broadcast_new_deities", true); // tell server when a player gets a deity
    public static final boolean ALLOW_PVP_EVERYWHERE = getConfig().getBoolean("allow_skills_everywhere", false);
    public static final int PVP_DELAY = getConfig().getInt("pvp_area_delay_time", 10);
    public static final int NOOB_LEVEL = getConfig().getInt("noob_level", 5);
    public static final boolean FRIENDLY_FIRE = getConfig().getBoolean("friendly_fire", false);
    public static final boolean FRIENDLY_FIRE_WARNING = getConfig().getBoolean("friendly_fire_message", false);
    public static final int START_DELAY = (int) getConfig().getDouble("start_delay_seconds", 0.1) * 20;
    public static final int FAVOR_FREQ = (int) getConfig().getDouble("favor_regen_seconds", 0.5) * 20;
    public static final int HP_FREQ = (int) getConfig().getDouble("hp_regen_seconds", 10.0) * 20;
    public static final int STAT_FREQ = (int) getConfig().getDouble("stat_display_frequency_in_seconds", 0.0) * 20;
    public static final boolean BALANCE_TEAMS = getConfig().getBoolean("balance_teams", true);
    public static final boolean MOTD = getConfig().getBoolean("motd", true);
    public static final double EXP_MULTIPLIER = getConfig().getDouble("globalexpmultiplier", 1.0); // can be modified
    public static final double FAVOR_MULTIPLIER = getConfig().getDouble("globalfavormultiplier", 1.0);
    public static final double PVP_MULTIPLIER = getConfig().getDouble("pvp_exp_bonus", 1.5); // bonus for dealing damage
    public static final int LOSS_LIMIT = getConfig().getInt("max_devotion_lost_on_death", 12_000); // max devotion lost on death per deity
    public static final int DROWN_HEIGHT_LIMIT = getConfig().getInt("drown_world_height_limit", 90);
    public static final boolean SERVER_ID_TAG = getConfig().getBoolean("server_id_tag", false);

    public static Configuration getConfig() {
        return NorseDemigods.INST.getConfig();
    }
}
