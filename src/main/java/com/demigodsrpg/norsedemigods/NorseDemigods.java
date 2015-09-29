package com.demigodsrpg.norsedemigods;

import com.demigodsrpg.norsedemigods.deity.Deities;
import com.demigodsrpg.norsedemigods.listener.*;
import com.demigodsrpg.norsedemigods.registry.PlayerDataRegistry;
import com.demigodsrpg.norsedemigods.saveable.LocationSaveable;
import com.demigodsrpg.norsedemigods.util.DMiscUtil;
import com.demigodsrpg.norsedemigods.util.DSave;
import com.demigodsrpg.norsedemigods.util.DSettings;
import com.demigodsrpg.norsedemigods.util.WorldGuardUtil;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitWorker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class NorseDemigods extends JavaPlugin implements Listener {

    static NorseDemigods INST;

    PlayerDataRegistry PLAYER_DATA;

    @Override
    public void onEnable() {
        long firstTime = System.currentTimeMillis();

        INST = this;

        getLogger().info("Initializing.");

        new DSettings(); // #1 (needed for DMiscUtil to load)
        new DMiscUtil(); // #2 (needed for everything else to work)

        PLAYER_DATA = new PlayerDataRegistry(this);

        loadFixes(); // #3.5
        loadListeners(); // #4
        loadCommands(); // #5 (needed)
        initializeThreads(); // #6 (regen and etc)
        loadDependencies(); // #7 compatibility with protection plugins
        cleanUp(); // #8
        invalidShrines(); // #9
        levelPlayers(); // #10

        getLogger().info("Attempting to hook into WorldGuard.");

        getLogger().info("Attempting to load Metrics.");

        unstickFireball(); // #12

        getLogger().info("Preparation completed in " + ((double) (System.currentTimeMillis() - firstTime) / 1000) + " seconds.");
    }

    @Override
    public void onDisable() {
        // Try to save files, if it can't, then let the Administrator know
        try {
            DSave.save();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            getLogger().severe("Save location error.");
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().severe("Save write error.");
        }

        // Cancel all tasks
        int c = 0;
        for (BukkitWorker bw : getServer().getScheduler().getActiveWorkers())
            if (bw.getOwner().equals(this)) c++;
        for (BukkitTask bt : getServer().getScheduler().getPendingTasks())
            if (bt.getOwner().equals(this)) c++;
        this.getServer().getScheduler().cancelAllTasks();

        getLogger().info("Save completed and " + c + " tasks cancelled.");
    }

    public PlayerDataRegistry getPlayerDataRegistry() {
        return PLAYER_DATA;
    }

    @EventHandler
    void saveOnExit(PlayerQuitEvent e) {
        // Save a player file when they exit, if it can't, let the Administrator know
        if (DMiscUtil.isFullParticipant(e.getPlayer())) try {
            DSave.save();
        } catch (FileNotFoundException er) {
            er.printStackTrace();
            getLogger().severe("Save location error.");
        } catch (IOException er) {
            er.printStackTrace();
            getLogger().severe("Save write error.");
        }
    }

    void loadDependencies() {
        getLogger().info("Attempting to hook into WorldGuard.");

        // Init WorldGuard stuff # 11
        WorldGuardUtil.setWhenToOverridePVP(this, event -> event instanceof EntityDamageByEntityEvent &&
                !DSettings.getEnabledWorlds().contains(((EntityDamageByEntityEvent) event).getEntity().getWorld()));

        if (WorldGuardUtil.worldGuardEnabled()) {
            getLogger().info("WorldGuard detected. Skills are disabled in no-PvP zones.");
        }
    }

    void loadCommands() {
        // Register the command manager
        DCommandExecutor ce = new DCommandExecutor(this);

		/*
         * General commands
		 */
        getCommand("dg").setExecutor(ce);
        getCommand("check").setExecutor(ce);
        getCommand("claim").setExecutor(ce);
        getCommand("alliance").setExecutor(ce);
        getCommand("value").setExecutor(ce);
        getCommand("bindings").setExecutor(ce);
        getCommand("forsake").setExecutor(ce);
        getCommand("adddevotion").setExecutor(ce);

		/*
         * Admin Commands
		 */
        getCommand("checkplayer").setExecutor(ce);
        getCommand("removeplayer").setExecutor(ce);
        getCommand("debugplayer").setExecutor(ce);
        getCommand("setallegiance").setExecutor(ce);
        getCommand("getfavor").setExecutor(ce);
        getCommand("setfavor").setExecutor(ce);
        getCommand("addfavor").setExecutor(ce);
        getCommand("getmaxfavor").setExecutor(ce);
        getCommand("setmaxfavor").setExecutor(ce);
        getCommand("addmaxfavor").setExecutor(ce);
        getCommand("givedeity").setExecutor(ce);
        getCommand("removedeity").setExecutor(ce);
        getCommand("addunclaimeddevotion").setExecutor(ce);
        getCommand("getdevotion").setExecutor(ce);
        getCommand("setdevotion").setExecutor(ce);
        getCommand("addhp").setExecutor(ce);
        getCommand("sethp").setExecutor(ce);
        getCommand("setmaxhp").setExecutor(ce);
        getCommand("getascensions").setExecutor(ce);
        getCommand("setascensions").setExecutor(ce);
        getCommand("addascensions").setExecutor(ce);
        getCommand("setkills").setExecutor(ce);
        getCommand("setdeaths").setExecutor(ce);
        getCommand("exportdata").setExecutor(ce);

		/*
         * Shrine commands
		 */
        getCommand("shrine").setExecutor(ce);
        getCommand("shrinewarp").setExecutor(ce);
        getCommand("forceshrinewarp").setExecutor(ce);
        getCommand("shrineowner").setExecutor(ce);
        getCommand("removeshrine").setExecutor(ce);
        getCommand("fixshrine").setExecutor(ce);
        getCommand("listshrines").setExecutor(ce);
        getCommand("nameshrine").setExecutor(ce);

		/*
         * Deity Commands
		 */
        // Thor
        getCommand("slam").setExecutor(ce);
        getCommand("lightning").setExecutor(ce);
        getCommand("storm").setExecutor(ce);

        // Vidar
        getCommand("strike").setExecutor(ce);
        getCommand("bloodthirst").setExecutor(ce);
        getCommand("crash").setExecutor(ce);

        // Odin
        getCommand("slow").setExecutor(ce);
        getCommand("stab").setExecutor(ce);
        getCommand("timestop").setExecutor(ce);

        // Fire Giant
        getCommand("fireball").setExecutor(ce);
        getCommand("blaze").setExecutor(ce);
        getCommand("firestorm").setExecutor(ce);

        // Jord
        getCommand("poison").setExecutor(ce);
        getCommand("plant").setExecutor(ce);
        getCommand("detonate").setExecutor(ce);
        getCommand("entangle").setExecutor(ce);

        // Hel
        getCommand("chain").setExecutor(ce);
        getCommand("entomb").setExecutor(ce);
        getCommand("curse").setExecutor(ce);

        // Jormungand
        getCommand("reel").setExecutor(ce);
        getCommand("drown").setExecutor(ce);

        // Thrymr
        getCommand("unburden").setExecutor(ce);
        getCommand("invincible").setExecutor(ce);

        // Heimdallr
        getCommand("flash").setExecutor(ce);
        getCommand("ceasefire").setExecutor(ce);

        // Frost Giant
        getCommand("ice").setExecutor(ce);
        getCommand("chill").setExecutor(ce);

        // Baldr
        getCommand("starfall").setExecutor(ce);
        getCommand("sprint").setExecutor(ce);
        getCommand("smite").setExecutor(ce);

        // Dwarf
        getCommand("reforge").setExecutor(ce);
        getCommand("shatter").setExecutor(ce);

        // Bragi
        getCommand("cure").setExecutor(ce);
        getCommand("finale").setExecutor(ce);

        // Dís
        getCommand("swap").setExecutor(ce);
        getCommand("congregate").setExecutor(ce);
        getCommand("assemble").setExecutor(ce);
    }

    void loadFixes() {
        getServer().getPluginManager().registerEvents(new DFixes(), this);
    }

    void loadListeners() {
        new WorldGuardUtil();
        getServer().getPluginManager().registerEvents(new DLevels(), this);
        getServer().getPluginManager().registerEvents(new DChatCommands(), this);
        getServer().getPluginManager().registerEvents(new DDamage(), this);
        getServer().getPluginManager().registerEvents(new DPvP(), this);
        getServer().getPluginManager().registerEvents(new DShrines(), this);
        getServer().getPluginManager().registerEvents(new DDeities(), this);
        getServer().getPluginManager().registerEvents(new DBlockChangeListener(), this);
        for (Deity deity : Deities.values()) {
            getServer().getPluginManager().registerEvents(deity, this);
        }
    }

    private void initializeThreads() {
        // Setup threads for saving, health, and favor
        int startdelay = (int) (DSettings.getSettingDouble("start_delay_seconds") * 20);
        int favorfrequency = (int) (DSettings.getSettingDouble("favor_regen_seconds") * 20);
        int hpfrequency = (int) (DSettings.getSettingDouble("hp_regen_seconds") * 20);
        int savefrequency = DSettings.getSettingInt("save_interval_seconds") * 20;
        if (hpfrequency < 0) hpfrequency = 600;
        if (favorfrequency < 0) favorfrequency = 600;
        if (startdelay <= 0) startdelay = 1;
        if (savefrequency <= 0) savefrequency = 300;

        // Favor
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < DSettings.getEnabledWorlds().size(); i++) {
                    World w = DSettings.getEnabledWorlds().get(i);
                    if (w == null) continue;
                    for (Player p : w.getPlayers())
                        if (DMiscUtil.isFullParticipant(p)) {
                            int regenrate = DMiscUtil.getAscensions(p); // TODO: PERK UPGRADES THIS
                            if (regenrate < 1) regenrate = 1;
                            DMiscUtil.setFavorQuiet(p.getUniqueId(), DMiscUtil.getFavor(p) + regenrate);
                        }
                }
            }
        }, startdelay, favorfrequency);

        // Health regeneration
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < DSettings.getEnabledWorlds().size(); i++) {
                    World w = DSettings.getEnabledWorlds().get(i);
                    if (w == null) continue;
                    for (Player p : w.getPlayers())
                        if (DMiscUtil.isFullParticipant(p)) {
                            if ((p.getHealth() < 1.0) || (DMiscUtil.getHP(p) < 1)) continue;
                            int heal = 1; // TODO: PERK UPGRADES THIS
                            if (DMiscUtil.getHP(p) < DMiscUtil.getMaxHP(p))
                                DMiscUtil.setHPQuiet(p.getUniqueId(), DMiscUtil.getHP(p) + heal);
                        }
                }
            }
        }, startdelay, hpfrequency);

        // Health sync
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                if (DSettings.getEnabledWorlds() == null) return;
                for (int i = 0; i < DSettings.getEnabledWorlds().size(); i++) {
                    World w = DSettings.getEnabledWorlds().get(i);
                    if (w == null) continue;
                    for (Player p : w.getPlayers())
                        if (DMiscUtil.isFullParticipant(p)) if (p.getHealth() > 0) DDamage.syncHealth(p);
                }
            }
        }, startdelay, 2);

        // Data save
        getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                try {
                    DSave.save();
                    getLogger().info("Saved data for " + DMiscUtil.getFullParticipants().size() + " Demigods players. " + DSave.getCompleteData().size() + " files total.");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    getLogger().severe("Save location error.");
                } catch (IOException e) {
                    e.printStackTrace();
                    getLogger().severe("Save write error.");
                }
            }
        }, startdelay, savefrequency);

        // Information display
        int frequency = (int) (DSettings.getSettingDouble("stat_display_frequency_in_seconds") * 20);
        if (frequency > 0) {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < DSettings.getEnabledWorlds().size(); i++) {
                        World w = DSettings.getEnabledWorlds().get(i);
                        if (w == null) continue;
                        for (Player p : w.getPlayers())
                            if (DMiscUtil.isFullParticipant(p)) if (p.getHealth() > 0) {
                                ChatColor color = ChatColor.GREEN;
                                if ((DMiscUtil.getHP(p) / DMiscUtil.getMaxHP(p)) < 0.25) color = ChatColor.RED;
                                else if ((DMiscUtil.getHP(p) / DMiscUtil.getMaxHP(p)) < 0.5) color = ChatColor.YELLOW;
                                String str = "-- HP " + color + "" + DMiscUtil.getHP(p) + "/" + DMiscUtil.getMaxHP(p) + ChatColor.YELLOW + " Favor " + DMiscUtil.getFavor(p) + "/" + DMiscUtil.getFavorCap(p);
                                p.sendMessage(str);
                            }
                    }
                }
            }, startdelay, frequency);
        }
    }

    private void cleanUp() {
        // Clean things that may cause glitches
        for (UUID player : DMiscUtil.getFullParticipants()) {
            for (Deity d : DMiscUtil.getDeities(player)) {
                if (DSave.hasData(player, d.getName().toUpperCase() + "_TRIBUTE_")) {
                    DSave.removeData(player, d.getName().toUpperCase() + "_TRIBUTE_");
                }
            }
        }
    }

    /**
     * private void updateSave()
     * {
     * // Updating to 1.1
     * HashMap<String, HashMap<String, Object>> copy = DSave.getCompleteData();
     * String updated = "[Demigods] Updated players:";
     * boolean yes = false;
     * for (String player : DSave.getCompleteData().keySet())
     * {
     * if (DSave.hasData(player, "dEXP"))
     * { // Coming from pre 1.1
     * yes = true;
     * copy.get(player).remove("dEXP");
     * if (DSave.hasData(player, "LEVEL"))
     * copy.get(player).remove("LEVEL");
     * for (Deity d : DMiscUtil.getDeities(player))
     * {
     * copy.get(player).put(d.getName()+"_dvt", (int)Math.ceil((500*Math.pow(DMiscUtil.getAscensions(player), 1.98))/DMiscUtil.getDeities(player).size()));
     * }
     * if (!DSave.hasData(player, "A_EFFECTS"))
     * DMiscUtil.setActiveEffects(player, new HashMap<String, Long>());
     * if (!DSave.hasData(player, "P_SHRINES"))
     * DMiscUtil.setShrines(player, new HashMap<String, WriteLocation>());
     * updated += " "+player;
     * }
     * }
     * if (yes)
     * log.info(updated);
     * DSave.overwrite(copy);
     * }
     */

    private void invalidShrines() {
        // Remove invalid shrines
        Iterator<LocationSaveable> i = DMiscUtil.getAllShrines().iterator();
        ArrayList<String> worldnames = new ArrayList<String>();
        for (int j = 0; j < DSettings.getEnabledWorlds().size(); j++) {
            World w = DSettings.getEnabledWorlds().get(j);
            if (w == null) continue;
            worldnames.add(w.getName());
        }
        int count = 0;
        while (i.hasNext()) {
            LocationSaveable n = i.next();
            if (!worldnames.contains(n.getWorld()) || (n.getY() < 0) || (n.getY() > 256)) {
                count++;
                DMiscUtil.removeShrine(n);
            }
        }
        if (count > 0) getLogger().info("Removed " + count + " invalid shrines.");
    }

    private void levelPlayers() {
        // Level players
        for (UUID player : DSave.getCompleteData().keySet())
            DLevels.levelProcedure(player);
    }

    private void unstickFireball() {
        // Unstick Prometheus fireballs
        for (int i = 0; i < DSettings.getEnabledWorlds().size(); i++) {
            World w = DSettings.getEnabledWorlds().get(i);
            if (w == null) continue;
            Iterator<Entity> it = w.getEntities().iterator();
            while (it.hasNext()) {
                Entity e = it.next();
                if (e instanceof Fireball) {
                    e.remove();
                    it.remove();
                }
            }
        }
    }
}
