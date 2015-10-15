package com.demigodsrpg.norsedemigods;

import org.bukkit.configuration.Configuration;

public class Setting {
    public static final int MAX_TARGET_RANGE = getConfig().getInt("max_target_range"); // maximum range on targeting
    public static final int MAXIMUM_HP = getConfig().getInt("max_hp"); // max hp a player can have
    public static final int ASCENSION_CAP = getConfig().getInt("ascension_cap"); // max levels
    public static final int FAVOR_CAP = getConfig().getInt("globalfavorcap"); // max favor
    public static final boolean BROADCAST_NEW_DEITY = getConfig().getBoolean("broadcast_new_deities"); // tell server when a player gets a deity
    public static final boolean ALLOW_PVP_EVERYWHERE = getConfig().getBoolean("allow_skills_everywhere");
    public static final int PVP_DELAY = getConfig().getInt("pvp_area_delay_time");
    public static final int NOOB_LEVEL = getConfig().getInt("noob_level");
    public static final boolean FRIENDLY_FIRE = getConfig().getBoolean("friendly_fire");
    public static final boolean FRIENDLY_FIRE_WARNING = getConfig().getBoolean("friendly_fire_message");
    public static final int START_DELAY = (int) getConfig().getDouble("start_delay_seconds") * 20;
    public static final int FAVOR_FREQ = (int) getConfig().getDouble("favor_regen_seconds") * 20;
    public static final int HP_FREQ = (int) getConfig().getDouble("hp_regen_seconds") * 20;
    public static final int STAT_FREQ = (int) getConfig().getDouble("stat_display_frequency_in_seconds") * 20;
    public static final boolean BALANCE_TEAMS = getConfig().getBoolean("balance_teams");
    public static final boolean MOTD = getConfig().getBoolean("motd");
    public static final double EXP_MULTIPLIER = getConfig().getDouble("globalexpmultiplier"); // can be modified
    public static final double FAVOR_MULTIPLIER = getConfig().getDouble("globalfavormultiplier");
    public static final double PVP_MULTIPLIER = getConfig().getDouble("pvp_exp_bonus"); // bonus for dealing damage
    public static final int LOSS_LIMIT = getConfig().getInt("max_devotion_lost_on_death"); // max devotion lost on death per deity
    public static final int DROWN_HEIGHT_LIMIT = getConfig().getInt("drown_world_height_limit");

    public static Configuration getConfig() {
        return NorseDemigods.INST.getConfig();
    }
}
