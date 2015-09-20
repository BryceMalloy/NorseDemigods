package com.demigodsrpg.norsedemigods.util;

import com.demigodsrpg.norsedemigods.NorseDemigods;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class DSettings {
    private static final NorseDemigods plugin;

    static {
        plugin = (NorseDemigods) Bukkit.getServer().getPluginManager().getPlugin("NorseDemigods");
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveConfig();
    }

    public static int getSettingInt(String id) {
        if (plugin.getConfig().isInt(id)) return plugin.getConfig().getInt(id);
        else return -1;
    }

    public static String getSettingString(String id) {
        if (plugin.getConfig().isString(id)) return plugin.getConfig().getString(id);
        else return null;
    }

    public static boolean getSettingBoolean(String id) {
        return !plugin.getConfig().isBoolean(id) || plugin.getConfig().getBoolean(id);
    }

    public static double getSettingDouble(String id) {
        if (plugin.getConfig().isDouble(id)) return plugin.getConfig().getDouble(id);
        else return -1;
    }

    public static List<World> getEnabledWorlds() {
        ArrayList<World> enabledWorlds = new ArrayList<World>();

        if (plugin.getConfig().isList("active_worlds")) {
            if (plugin.getConfig().getStringList("active_worlds").contains("DEFAULT")) {
                enableWorlds();
            }

            for (String s : plugin.getConfig().getStringList("active_worlds")) {
                try {
                    enabledWorlds.add(plugin.getServer().getWorld(s));
                } catch (Exception ignored) {
                    DMiscUtil.getPlugin().getLogger().severe("You do not have enabled worlds correctly configured!");
                }
            }
        } else {
            for (int count = 0; count < Bukkit.getServer().getWorlds().size(); count++) {
                for (World w : plugin.getServer().getWorlds()) {
                    if (!plugin.getConfig().getStringList("inactive_worlds").contains(w.getName())) {
                        enabledWorlds.add(w);
                    }
                }
            }
        }

        return enabledWorlds;
    }

    private static void enableWorlds() {
        ArrayList<String> worlds = new ArrayList<String>();
        for (World w : plugin.getServer().getWorlds())
            worlds.add(w.getName());
        plugin.getConfig().set("active_worlds", worlds);
        plugin.saveConfig();
    }
}
