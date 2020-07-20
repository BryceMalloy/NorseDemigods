package com.demigodsrpg.norsedemigods;

import com.demigodsrpg.norsedemigods.deity.Deities;
import com.demigodsrpg.norsedemigods.listener.*;
import com.demigodsrpg.norsedemigods.registry.PlayerDataRegistry;
import com.demigodsrpg.norsedemigods.registry.ShrineRegistry;
import com.demigodsrpg.norsedemigods.saveable.LocationSaveable;
import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
import com.demigodsrpg.norsedemigods.saveable.ShrineSaveable;
import com.demigodsrpg.norsedemigods.util.WorldGuardUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitWorker;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class NorseDemigods extends JavaPlugin implements Listener {

    static NorseDemigods INST;

    PlayerDataRegistry PLAYER_DATA;
    ShrineRegistry SHRINE_DATA;

    @Override
    public void onEnable() {
        long firstTime = System.currentTimeMillis();

        INST = this;

        getLogger().info("Initializing.");

        saveDefaultConfig();

        new DMisc(); // #1 (needed for everything else to work)

        PLAYER_DATA = new PlayerDataRegistry(this);
        SHRINE_DATA = new ShrineRegistry(this);

        loadFixes(); // #3.5
        loadListeners(); // #4
        loadCommands(); // #5 (needed)
        initializeThreads(); // #6 (regen and etc)
        loadDependencies(); // #7 compatibility with protection plugins
        cleanUp(); // #8
        invalidShrines(); // #9
        unstickFireball(); // #12

        getLogger().info("Preparation completed in " + ((double) (System.currentTimeMillis() - firstTime) / 1000) + " seconds.");
    }

    @Override
    public void onDisable() {
        // Cancel all tasks
        int c = 0;
        for (BukkitWorker bw : getServer().getScheduler().getActiveWorkers())
            if (bw.getOwner().equals(this)) c++;
        for (BukkitTask bt : getServer().getScheduler().getPendingTasks())
            if (bt.getOwner().equals(this)) c++;
        //this.getServer().getScheduler().cancelAllTasks();

        // Clear temp data
        PLAYER_DATA.purgeTempData();

        getLogger().info(c + " tasks cancelled.");
    }

    public PlayerDataRegistry getPlayerDataRegistry() {
        return PLAYER_DATA;
    }

    public ShrineRegistry getShrineRegistry() {
        return SHRINE_DATA;
    }

    public Location getLocationFromKey(String key) {
        return new LocationSaveable(key).toLocation(this);
    }

    public String getLocationKey(Location location) {
        return location.getBlockX() + "-" + location.getBlockY() + "-" + location.getBlockZ() + "-" +
                location.getWorld().getName();
    }

    void loadDependencies() {
        if (getServer().getPluginManager().isPluginEnabled("WorldGuard")) {
            new WorldGuardUtil(this);

            getLogger().info("Attempting to hook into WorldGuard.");
            if (WorldGuardUtil.worldGuardEnabled()) {
                WorldGuardUtil.setWhenToOverridePVP(this, event -> {
                    if (event instanceof EntityDamageByEntityEvent) {
                        if (((EntityDamageByEntityEvent) event).getEntity() instanceof Player) {
                            return getPlayerDataRegistry().fromPlayer((Player) ((EntityDamageByEntityEvent) event).
                                    getEntity()).getTempStatus("temp_was_PVP");
                        }
                    }
                    return false;
                });
                getLogger().info("WorldGuard hook successful. Skills are disabled in no-PvP zones.");
            }
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
        getServer().getPluginManager().registerEvents(new DLevels(), this);
        getServer().getPluginManager().registerEvents(new DChatCommands(), this);
        getServer().getPluginManager().registerEvents(new DDamage(), this);
        getServer().getPluginManager().registerEvents(new DPvP(this), this);
        getServer().getPluginManager().registerEvents(new DShrines(this), this);
        getServer().getPluginManager().registerEvents(new DDeities(this), this);
        getServer().getPluginManager().registerEvents(new DBlockChangeListener(), this);
        for (Deity deity : Deities.values()) {
            getServer().getPluginManager().registerEvents(deity, this);
        }
    }

    private void initializeThreads() {
        // Setup threads for saving, health, and favor
        int startdelay = Setting.START_DELAY;
        int favorfrequency = Setting.FAVOR_FREQ;
        int hpfrequency = Setting.HP_FREQ;
        //if (hpfrequency < 0) hpfrequency = 600;
        if (favorfrequency < 0) favorfrequency = 600;
        if (startdelay <= 0) startdelay = 1;

        // Favor
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (World world : Bukkit.getWorlds()) {
                for (Player p : world.getPlayers())
                    if (DMisc.isFullParticipant(p)) {
                        int regenerate = DMisc.getAscensions(p); // TODO: PERK UPGRADES THIS
                        if (regenerate < 1) regenerate = 1;
                        DMisc.setFavorQuiet(p.getUniqueId(), DMisc.getFavor(p) + regenerate);
                    }
            }
        }, startdelay, favorfrequency);

        // Health regeneration
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
            for (World world : Bukkit.getWorlds()) {
                for (Player p : world.getPlayers())
                    if (DMisc.isFullParticipant(p)) {
                        if (p.getHealth() < 1.0) continue;
                        int heal = 1; // TODO: PERK UPGRADES THIS
                        if (p.getHealth() < p.getMaxHealth())
                            DMisc.setHPQuiet(p, p.getHealth() + heal);
                    }
            }
        }, startdelay, hpfrequency);

        // Information display
        if (Setting.STAT_FREQ > 0) {
            getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> {
                for (World world : Bukkit.getWorlds()) {
                    for (Player p : world.getPlayers())
                        if (DMisc.isFullParticipant(p)) if (p.getHealth() > 0) {
                            ChatColor color = ChatColor.GREEN;
                            if ((p.getHealth() / p.getMaxHealth()) < 0.25) color = ChatColor.RED;
                            else if ((p.getHealth() / p.getMaxHealth()) < 0.5) color = ChatColor.YELLOW;
                            String str = "-- HP " + color + "" + p.getHealth() + "/" + p.getMaxHealth() + ChatColor.YELLOW + " Favor " + DMisc.getFavor(p) + "/" + DMisc.getFavorCap(p);
                            p.sendMessage(str);
                        }
                }
            }, startdelay, Setting.STAT_FREQ);
        }
    }

    private void cleanUp() {
        // Clean things that may cause glitches
        for (PlayerDataSaveable saveable : PLAYER_DATA.getFromDb().values()) {
            saveable.getDeityList().stream().filter(d -> saveable.getActiveEffects().containsKey(d.toUpperCase() + "_TRIBUTE_")).forEach(d -> {
                saveable.removeEffect(d.toUpperCase() + "_TRIBUTE_");
            });
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
        Iterator<ShrineSaveable> i = DMisc.getAllShrines().iterator();
        List<String> worldnames = Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
        int count = 0;
        while (i.hasNext()) {
            ShrineSaveable n = i.next();
            if (!worldnames.contains(n.getWorld()) || (n.getY() < 0) || (n.getY() > 256)) {
                count++;
                DMisc.removeShrine(n);
            }
        }
        if (count > 0) getLogger().info("Removed " + count + " invalid shrines.");
    }

    private void unstickFireball() {
        // Unstick Prometheus fireballs
        for (World world : Bukkit.getWorlds()) {
            Iterator<Entity> it = world.getEntities().iterator();
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
