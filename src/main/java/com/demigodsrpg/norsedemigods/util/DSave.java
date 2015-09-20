package com.demigodsrpg.norsedemigods.util;

import com.demigodsrpg.norsedemigods.NorseDemigods;
import com.demigodsrpg.norsedemigods.deity.Deity;
import com.google.common.collect.Maps;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

/*
 * HASHMAP OF PLAYER'S NAMES
 * CONTAINS EACH PLAYER'S SAVED INFORMATION (IDENTIFIED BY STRINGS)
 * BE VERY CAREFUL NOT TO SAVE THINGS THAT CAN'T BE WRITTEN
 */
public class DSave {
    private final static Logger log = Logger.getLogger("Minecraft");
    private final static String PATH = "plugins/NorseDemigods/";
    private static HashMap<UUID, HashMap<String, Object>> SAVEDDATA = Maps.newHashMap();

    public DSave() {
        int participants = 0;
        File f1 = new File(PATH + "Players/");
        if (!f1.exists()) {
            log.info("[Demigods] Creating a new player save.");
            f1.mkdirs();
        }
        File[] list = f1.listFiles();
        if (list.length != 0) for (File element : list) {
            String load = element.getName();
            if (load.endsWith(".dg2")) {
                load = load.substring(0, load.length() - 4);
                try {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(element));
                    Object result = ois.readObject();
                    @SuppressWarnings("unchecked")
                    HashMap<String, Object> cast = (HashMap<String, Object>) result;
                    SAVEDDATA.put(UUID.fromString(load), cast);
                    if (DMiscUtil.isFullParticipant(load)) participants++;
                    ois.close();
                } catch (Exception error) {
                    log.severe("[Demigods] Could not load player " + load);
                    error.printStackTrace();
                    log.severe("[Demigods] End stack trace for " + load);
                }
            }
        }
        log.info("[Demigods] Loaded " + participants + " Demigods from " + f1.listFiles().length + " player files.");
    }

    public static String getPlayerSavePath() {
        return PATH + "Players/";
    }

    /*
     * Check if the player has saved information.
     */
    public static boolean hasPlayer(Player p) {
        return p != null && hasPlayer(p.getUniqueId());
    }

    public static boolean hasPlayer(UUID p) {
        return SAVEDDATA.containsKey(p);
    }

    /*
     * Add a player's information to be saved, under the player's name.
     */
    public static void addPlayer(Player p) {
        addPlayer(p.getUniqueId()); // always use getName();
    }

    private static boolean addPlayer(UUID p) {
        if (hasPlayer(p)) return false;
        SAVEDDATA.put(p, new HashMap<String, Object>());
        return true;
    }

    /*
     * Check if the player has data saved under a certain id.
     */
    public static boolean hasData(Player p, String id) {
        return hasData(p.getUniqueId(), id);
    }

    public static boolean hasData(UUID p, String id) {
        if (hasPlayer(p)) {
            if (SAVEDDATA.get(p).containsKey(id)) return true;
        }
        return false;
    }

    /*
     * Save data under a certain id.
     */
    public static void saveData(Player p, String id, Object save) {
        saveData(p.getUniqueId(), id, save);
    }

    public static boolean saveData(UUID p, String id, Object save) {
        if (!hasPlayer(p)) return false;
        if (SAVEDDATA.get(p).containsKey(id)) SAVEDDATA.get(p).remove(id); // remove if already there, to overwrite
        SAVEDDATA.get(p).put(id, save);
        return true;
    }

    /*
     * Get all of a player's data.
     */
    public static HashMap<String, Object> getAllData(Player p) {
        return getAllData(p.getUniqueId());
    }

    public static HashMap<String, Object> getAllData(UUID p) {
        if (hasPlayer(p)) return SAVEDDATA.get(p);
        return null;
    }

    /*
     * Get a specific piece of saved data, by id.
     */
    public static Object getData(Player p, String id) {
        return getData(p.getUniqueId(), id);
    }

    public static Object getData(UUID p, String id) {
        if (hasData(p, id)) return SAVEDDATA.get(p).get(id);
        return null;
    }

    /*
     * Remove a specific piece of data by id
     */
    public static Object removeData(Player p, String id) {
        return removeData(p.getUniqueId(), id);
    }

    public static Object removeData(UUID p, String id) {
        Object o = null;
        if (hasData(p, id)) {
            o = SAVEDDATA.get(p).remove(id);
        }
        return o;
    }

    public static void removePlayer(Player p) {
        removePlayer(p.getUniqueId());
    }

    private static void removePlayer(UUID p) {
        if (SAVEDDATA.containsKey(p)) {
            SAVEDDATA.remove(p);
        }
        try {
            File f = new File(PATH + "Players/" + p + ".dem");
            f.delete();
        } catch (Exception er) {
            Logger.getLogger("Minecraft").warning("[Demigods] Error while removing " + p + " from save.");
        }
    }

    public static HashMap<UUID, HashMap<String, Object>> getCompleteData() {
        return SAVEDDATA;
    }

    public static void removeItem(String path) {
        (new File(path)).delete();
    }

    public static void saveItem(String path, Object item) {
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(path));
            oos.writeObject(item);
            oos.flush();
            oos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void overwrite(HashMap<UUID, HashMap<String, Object>> save) {
        SAVEDDATA = save;
    }

    /*
     * Saves itself, but must be loaded elsewhere (main plugin).
     */
    public static void save() throws IOException {
        (new File(PATH + "Players/")).mkdirs();
        for (UUID id : SAVEDDATA.keySet()) {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PATH + "/Players/" + id + ".dg2"));
            oos.writeObject(SAVEDDATA.get(id));
            oos.flush();
            oos.close();
        }
    }

    public static Set<Deity> getGlobalList() {
        return NorseDemigods.deities;
    }

    public static boolean getConfirmed(Player player) {
        UUID id = player.getUniqueId();
        return !(!SAVEDDATA.containsKey(id) || !SAVEDDATA.get(id).containsKey("update")) && System.currentTimeMillis() <= (Long) SAVEDDATA.get(id).get("update");
    }

    public static void confirm(Player player, boolean confirm) {
        UUID id = player.getUniqueId();
        if (!confirm) {
            if (SAVEDDATA.containsKey(id)) SAVEDDATA.get(id).remove("update");
        } else if (!SAVEDDATA.containsKey(id)) {
            HashMap<String, Object> save = new HashMap<String, Object>();
            save.put("update", System.currentTimeMillis() + (DSettings.getSettingInt("confirm_time") * 1000));
            SAVEDDATA.put(id, save);
        } else
            SAVEDDATA.get(id).put("update", System.currentTimeMillis() + (DSettings.getSettingInt("confirm_time") * 1000));
    }
}
