package com.demigodsrpg.norsedemigods.util;

import com.demigodsrpg.norsedemigods.DCommandExecutor;
import com.demigodsrpg.norsedemigods.DFixes;
import com.demigodsrpg.norsedemigods.NorseDemigods;
import com.demigodsrpg.norsedemigods.deity.Deity;
import com.demigodsrpg.norsedemigods.listener.DDamage;
import com.demigodsrpg.norsedemigods.listener.DShrines;
import com.demigodsrpg.norsedemigods.saveable.LocationSaveable;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Logger;

public class DMiscUtil {
    private static final NorseDemigods plugin = (NorseDemigods) Bukkit.getServer().getPluginManager().getPlugin("NorseDemigods"); // obviously needed
    private static final int dist = DSettings.getSettingInt("max_target_range"); // maximum range on targeting
    private static final int MAXIMUMHP = DSettings.getSettingInt("max_hp"); // max hp a player can have
    public static final int ASCENSIONCAP = DSettings.getSettingInt("ascension_cap"); // max levels
    private static final int FAVORCAP = DSettings.getSettingInt("globalfavorcap"); // max favor
    private static final boolean BROADCASTNEWDEITY = DSettings.getSettingBoolean("broadcast_new_deities"); // tell server when a player gets a deity
    private static final boolean ALLOWPVPEVERYWHERE = DSettings.getSettingBoolean("allow_skills_everywhere");
    private static final boolean USENEWPVP = DSettings.getSettingBoolean("use_new_pvp_zones");

    public static void consoleMSG(String level, String msg) {
        // Define variables
        Logger log = Logger.getLogger("Minecraft");

        if (level.equalsIgnoreCase("info")) log.info("[Demigods] " + msg);
        if (level.equalsIgnoreCase("warning")) log.warning("[Demigods] " + msg);
        if (level.equalsIgnoreCase("severe")) log.severe("[Demigods] " + msg);
    }

    public static Player getOnlinePlayer(String name) {
        return plugin.getServer().getPlayer(name);
    }

    public static Player getOnlinePlayer(UUID id) {
        return plugin.getServer().getPlayer(id);
    }

    public static UUID getDemigodsPlayerId(String name) {
        UUID found = null;
        String lowerName = name.toLowerCase();
        int delta = Integer.MAX_VALUE;
        for (UUID player : DSave.getCompleteData().keySet()) {
            String playername = getLastKnownName(player);
            if (playername.toLowerCase().startsWith(lowerName)) {
                int curDelta = playername.length() - lowerName.length();
                if (curDelta < delta) {
                    found = player;
                    delta = curDelta;
                }
                if (curDelta == 0) break;
            }
        }
        return found;
    }

    @Deprecated
    public static String getDemigodsPlayer(String name) {
        String found = null;
        String lowerName = name.toLowerCase();
        int delta = Integer.MAX_VALUE;
        for (UUID player : DSave.getCompleteData().keySet()) {
            String playername = getLastKnownName(player);
            if (playername.toLowerCase().startsWith(lowerName)) {
                int curDelta = playername.length() - lowerName.length();
                if (curDelta < delta) {
                    found = playername;
                    delta = curDelta;
                }
                if (curDelta == 0) break;
            }
        }
        return found;
    }

    public static String getLastKnownName(UUID p) {
        return DSave.getData(p, "LASTKNOWNNAME").toString();
    }

    /**
     * Gets the Location a Player is looking at.
     */
    public static Location getTargetLocation(Player p) {
        return p.getTargetBlock((Set) null, dist).getLocation();
    }

    /**
     * Gets the LivingEntity a Player is looking at.
     */
    public static LivingEntity getTargetLivingEntity(Player p, int offset) {
        LivingEntity e = null;
        for (Block b : p.getLineOfSight((Set) null, dist)) {
            for (Entity t : b.getChunk().getEntities()) {
                if (t.getWorld() != b.getWorld()) continue;
                if (t instanceof LivingEntity)
                    if ((t.getLocation().distance(b.getLocation()) <= offset) && !t.equals(p)) e = (LivingEntity) t;
            }
        }
        return e;
    }

    /**
     * Converts a Location to WriteLocation.
     */
    public static LocationSaveable toWriteLocation(Location l) {
        return new LocationSaveable(l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ());
    }

    /**
     * Converts a WriteLocation to Location.
     */
    public static Location toLocation(LocationSaveable l) {
        try {
            return new Location(plugin.getServer().getWorld(l.getWorld()), l.getX(), l.getY(), l.getZ());
        } catch (Exception er) {
            return null;
        }
    }

    /**
     * Checks if a player has the given permission or is OP.
     */
    public static boolean hasPermissionOrOP(Player p) {// convenience method for permissions
        return p.isOp() || p.hasPermission("demigods.admin");
    }

    /**
     * Checks is a player has the given permission.
     */
    public static boolean hasPermission(Player p, String pe) {// convenience method for permissions
        return p.hasPermission(pe);
    }

    public static void setJotunn(UUID p) {
        DSave.saveData(p, "ALLEGIANCE", "jotunn");
    }

    public static void setAEsir(UUID p) {
        DSave.saveData(p, "ALLEGIANCE", "aesir");
    }

    public static void setAllegiance(UUID p, String allegiance) {
        DSave.saveData(p, "ALLEGIANCE", allegiance);
    }

    public static boolean areAllied(Player p1, Player p2) {
        return areAllied(p1.getUniqueId(), p2.getUniqueId());
    }

    private static boolean areAllied(UUID p1, UUID p2) {
        return isFullParticipant(p1) && isFullParticipant(p2) && getAllegiance(p1).equalsIgnoreCase(getAllegiance(p2));
    }

    /**
     * Gets the String representation of a player's allegiance.
     */
    public static String getAllegiance(Player p) {
        return getAllegiance(p.getUniqueId());
    }

    /**
     * Gets the String representation of a player's allegiance.
     */
    public static String getAllegiance(UUID p) {
        if (DSave.hasData(p, "ALLEGIANCE")) return ((String) DSave.getData(p, "ALLEGIANCE"));
        return "Human";
    }

    /**
     * Checks if a player is in a certain allegiance.
     */
    private static boolean is(Player p) {
        return DSave.hasData(p, "ALLEGIANCE") && DSave.getData(p, "ALLEGIANCE").equals("omni");
    }

    /**
     * Gives a player a deity, even if they have none.
     */
    public static void giveDeity(Player p, Deity d) {
        giveDeity(p.getUniqueId(), d);
    }

    public static void giveDeity(UUID p, Deity d) {
        if (!hasPermission(getOnlinePlayer(p), d.getDefaultAlliance().toLowerCase() + "." + d.getName().toLowerCase()) && (!hasPermission(getOnlinePlayer(p), d.getDefaultAlliance().toLowerCase() + ".all"))) {
            consoleMSG("info", p + " does not have permission to get this deity.");
            return;
        }

        if (BROADCASTNEWDEITY) {
            String message;
            switch (d.getName().toLowerCase()) {
                case "frost giant":
                    message = ChatColor.YELLOW + getLastKnownName(p) + " has joined the lineage of the frost giants.";
                    break;
                case "fire giant":
                    message = ChatColor.YELLOW + getLastKnownName(p) + " has joined the lineage of fire giants.";
                    break;
                case "dwarf":
                    message = ChatColor.YELLOW + getLastKnownName(p) + " has joined the lineage of the dwarves.";
                    break;
                case "dis":
                    message = ChatColor.YELLOW + getLastKnownName(p) + " has joined a lineage of dísir.";
                    break;
                default:
                    message = ChatColor.YELLOW + getLastKnownName(p) + " has joined the lineage of " + d.getName() + ".";
            }
            plugin.getServer().broadcastMessage(message);
        }
        if (DSave.hasData(p, "DEITIES")) getDeities(p).add(d);
        else {
            ArrayList<Deity> ad = new ArrayList<Deity>();
            ad.add(d);
            DSave.saveData(p, "DEITIES", ad);
        }
        setDevotion(p, d, 1);
    }

    public static void giveDeitySilent(UUID p, Deity d) {
        if (!d.getName().equals("?????") && !hasPermission(getOnlinePlayer(p), d.getDefaultAlliance().toLowerCase() + "." + d.getName().toLowerCase()) && (!hasPermission(getOnlinePlayer(p), d.getDefaultAlliance().toLowerCase() + ".all"))) {
            consoleMSG("info", p + " does not have permission to get this deity.");
            return;
        }
        if (DSave.hasData(p, "DEITIES")) getDeities(p).add(d);
        else {
            ArrayList<Deity> ad = new ArrayList<Deity>();
            ad.add(d);
            DSave.saveData(p, "DEITIES", ad);
        }
        setDevotion(p, d, 1);
    }

    public static void removeDeity(UUID p, String name) {
        ArrayList<Deity> temp = getDeities(p);
        if (DSave.hasData(p, "DEITIES")) {
            for (Deity de : getDeities(p)) {
                if (de.getName().equalsIgnoreCase(name)) temp.remove(getDeities(p).indexOf(de));
            }
        }
        DSave.removeData(p, name + "_dvt");
        DSave.saveData(p, "DEITIES", temp);
    }

    /**
     * Checks if a player has a named deity.
     *
     * @param p
     * @param name
     * @return
     */
    public static boolean hasDeity(Player p, String name) {
        return hasDeity(p.getUniqueId(), name);
    }

    public static boolean hasDeity(UUID p, String name) {
        if (DSave.hasData(p, "DEITIES")) {
            for (Deity de : getDeities(p)) {
                if (de.getName().equalsIgnoreCase(name)) return true;
            }
        }
        return false;
    }

    /**
     * Returns the deity named if the player has it, otherwise null.
     *
     * @param p
     * @param deityname
     * @return
     */
    public static Deity getDeity(Player p, String deityname) {
        return getDeity(p.getUniqueId(), deityname);
    }

    public static Deity getDeity(UUID p, String deityname) {
        if (DSave.hasData(p, "DEITIES")) {
            for (Deity de : getDeities(p)) {
                if (de.getName().equalsIgnoreCase(deityname)) return de;
            }
        }
        return null;
    }

    /**
     * Gives the list of all the player's deities.
     */
    public static ArrayList<Deity> getDeities(Player p) {
        return getDeities(p.getUniqueId());
    }

    public static Collection<Deity> getTributeableDeities(Player p) {
        return getTributeableDeities(p.getUniqueId());
    }

    /**
     * Gives the list of all the player's deities.
     */
    public static ArrayList<Deity> getDeities(UUID p) throws NullPointerException {
        if (DSave.hasData(p, "DEITIES")) {
            try {
                return (ArrayList<Deity>) DSave.getData(p, "DEITIES");
            } catch (Throwable e) {
                throw new NullPointerException("Deity saves are missing for player " + p + ".");
            }
        }
        return null;
    }

    /**
     * Gives the list of all the player's deities.
     */
    public static Collection<Deity> getTributeableDeities(UUID p) throws NullPointerException {
        Collection<Deity> deities = getDeities(p);
        if (deities != null) {
            deities = Collections2.filter(deities, new Predicate<Deity>() {
                @Override
                public boolean apply(Deity deity) {
                    return deity.canTribute();
                }
            });
        }
        return deities;
    }

    /**
     * Gives the list of all the player's deities by name
     */
    public static ArrayList<String> getDeityNames(Player p) {
        return getDeityNames(p.getUniqueId());
    }

    private static ArrayList<String> getDeityNames(UUID p) {
        ArrayList<String> list = new ArrayList<String>();
        for (Deity d : getDeities(p))
            list.add(d.getName());
        return list;
    }

    public static ArrayList<String> getTributeableDeityNames(Player p) {
        return getTributeableDeityNames(p.getUniqueId());
    }

    private static ArrayList<String> getTributeableDeityNames(UUID p) {
        ArrayList<String> list = new ArrayList<String>();
        for (Deity d : getDeities(p))
            if (d.canTribute()) list.add(d.getName());
        return list;
    }

    /**
     * Set a player's favor.
     *
     * @param p
     * @param amt
     */
    public static void setFavor(Player p, int amt) {
        setFavor(p.getUniqueId(), amt);
    }

    /**
     * Set a player's favor.
     *
     * @param p
     * @param amt
     */
    public static void setFavor(UUID p, int amt) {
        if (amt > getFavorCap(p)) amt = getFavorCap(p);
        int c = amt - getFavor(p);
        DSave.saveData(p, "FAVOR", amt);
    }

    public static void setFavorQuiet(UUID p, int amt) {
        if (amt > getFavorCap(p)) amt = getFavorCap(p);
        DSave.saveData(p, "FAVOR", amt);
    }

    /**
     * Get a player's favor.
     */
    public static int getFavor(Player p) {
        return getFavor(p.getUniqueId());
    }

    /**
     * Get a player's favor.
     */
    public static int getFavor(UUID p) {
        if (DSave.hasData(p, "FAVOR")) return (Integer) DSave.getData(p, "FAVOR");
        return -1;
    }

    /**
     * Set a player's HP.
     *
     * @param p
     * @param amt
     */
    public static void setHP(Player p, double amt) {
        setHP(p.getUniqueId(), amt);
    }

    /**
     * Set a player's HP.
     *
     * @param p
     * @param amt
     */
    public static void setHP(UUID p, double amt) {
        if (amt > getMaxHP(p)) amt = getMaxHP(p);
        if (amt < 0) amt = 0;
        double c = amt - getHP(p);
        DSave.saveData(p, "dHP", amt);
        if ((c != 0) && (DMiscUtil.getOnlinePlayer(p) != null)) {
            ChatColor color = ChatColor.GREEN;
            if ((DMiscUtil.getHP(p) / DMiscUtil.getMaxHP(p)) < 0.25) color = ChatColor.RED;
            else if ((DMiscUtil.getHP(p) / DMiscUtil.getMaxHP(p)) < 0.5) color = ChatColor.YELLOW;
            String disp = "";
            if (c > 0) disp = "+" + c;
            else disp += c;
            String str = color + "HP: " + DMiscUtil.getHP(p) + "/" + DMiscUtil.getMaxHP(p) + " (" + disp + ")";
        }
    }

    public static void setHPQuiet(UUID p, double amt) {
        if (amt > getMaxHP(p)) amt = getMaxHP(p);
        DSave.saveData(p, "dHP", amt);
    }

    /**
     * Set a player's max HP.
     */
    public static void setMaxHP(UUID p, double amt) {
        if (amt > MAXIMUMHP) amt = MAXIMUMHP;
        DSave.saveData(p, "dmaxHP", amt);
    }

    /**
     * Get a player's scaled-up HP.
     */
    public static double getHP(Player p) {
        return getHP(p.getUniqueId());
    }

    /**
     * Get a player's scaled-up HP.
     */
    public static double getHP(UUID p) {
        if (DSave.hasData(p, "dHP")) return Double.valueOf(DSave.getData(p, "dHP").toString());
        return -1.0;
    }

    /**
     * Get a player's maximum scaled-up HP.
     */
    public static double getMaxHP(Player p) {
        return getMaxHP(p.getUniqueId());
    }

    /**
     * Get a player's maximum scaled-up HP.
     */
    public static double getMaxHP(UUID p) {
        if (DSave.hasData(p, "dmaxHP")) return Double.valueOf(DSave.getData(p, "dmaxHP").toString());
        return -1.0;
    }

    /**
     * Set a player's devotion for a specific deity.
     */
    public static boolean setDevotion(UUID p, String deityname, int amt) {
        try {
            int c = amt - getDevotion(p, deityname);
            DSave.saveData(p, deityname + "_dvt", amt);
            if ((c != 0) && (getOnlinePlayer(p) != null)) {
                String disp = "";
                if (c > 0) disp = "+" + c;
                else disp += c;
                // TODO Does this do anything?
            }
            return true;
        } catch (NullPointerException ne) {
            return false;
        }
    }

    public static void setDevotion(Player p, Deity d, int amt) {
        setDevotion(p.getUniqueId(), d.getName(), amt);
    }

    public static void setDevotion(UUID p, Deity d, int amt) {
        setDevotion(p, d.getName(), amt);
    }

    public static void setDevotion(Player p, String deityname, int amt) {
        setDevotion(p.getUniqueId(), deityname, amt);
    }

    /**
     * Get a player's devotion for a specific deity.
     */
    public static int getDevotion(Player p, String deityname) {
        return getDevotion(p.getUniqueId(), deityname);
    }

    public static int getDevotion(Player p, Deity d) {
        return getDevotion(p.getUniqueId(), d.getName());
    }

    public static int getDevotion(UUID p, Deity d) {
        return getDevotion(p, d.getName());
    }

    public static int getDevotion(UUID p, String deityname) {
        if (DSave.hasData(p, deityname + "_dvt")) return (Integer) DSave.getData(p, deityname + "_dvt");
        return -1;
    }

    /**
     * Get a player's total Devotion
     */
    public static long getDevotion(Player p) {
        return getDevotion(p.getUniqueId());
    }

    public static long getDevotion(UUID p) {
        if (!isFullParticipant(p)) return -1;
        int total = 0;
        for (Deity d : getDeities(p)) {
            total += getDevotion(p, d.getName());
        }
        return total;
    }

    /**
     * Get the unclaimed devotion a player has been given.
     */
    public static int getUnclaimedDevotion(Player p) {
        return getUnclaimedDevotion(p.getUniqueId());
    }

    public static int getUnclaimedDevotion(UUID p) {
        if (!DSave.hasData(p, "U_DVT")) {
            DSave.saveData(p, "U_DVT", 0);
        }
        try {
            return (Integer) DSave.getData(p, "U_DVT");
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Set the unclaimed devotion a player has been given.
     *
     * @param p
     * @param amount
     */
    public static void setUnclaimedDevotion(Player p, int amount) {
        setUnclaimedDevotion(p.getUniqueId(), amount);
    }

    public static void setUnclaimedDevotion(UUID p, int amount) {
        DSave.saveData(p, "U_DVT", amount);
    }

    /**
     * Set a player's number of ascensions.
     *
     * @param p
     * @param amt
     */
    public static void setAscensions(UUID p, int amt) {
        if (amt > ASCENSIONCAP) amt = ASCENSIONCAP;
        DSave.saveData(p, "ASCENSIONS", amt);
    }

    public static int costForNextAscension(UUID p) {
        return costForNextAscension(getAscensions(p));
    }

    public static int costForNextAscension(int ascensions) {
        return (int) Math.ceil(500 * Math.pow(ascensions + 1, 2.02));
    }

    /**
     * Get a player's ascensions.
     */
    public static int getAscensions(UUID p) {
        if (DSave.hasData(p, "ASCENSIONS")) return (Integer) DSave.getData(p, "ASCENSIONS");
        return -1;
    }

    public static int getAscensions(Player p) {
        return getAscensions(p.getUniqueId());
    }

    /**
     * Set the number of kills a player has.
     *
     * @param p
     * @param amt
     */
    public static void setKills(Player p, int amt) {
        DSave.saveData(p, "KILLS", amt);
    }

    public static void setKills(UUID p, int amt) {
        DSave.saveData(p, "KILLS", amt);
    }

    /**
     * Set the number of deaths a player has.
     *
     * @param p
     * @param amt
     */
    public static void setDeaths(Player p, int amt) {
        DSave.saveData(p, "DEATHS", amt);
    }

    public static void setDeaths(UUID p, int amt) {
        DSave.saveData(p, "DEATHS", amt);
    }

    /**
     * Get the number of kills a player has.
     */
    public static int getKills(Player p) {
        if (DSave.hasData(p, "KILLS")) return (Integer) DSave.getData(p, "KILLS");
        return -1;
    }

    /**
     * Get the number of kills a player has.
     */
    public static int getKills(UUID p) {
        if (DSave.hasData(p, "KILLS")) return (Integer) DSave.getData(p, "KILLS");
        return -1;
    }

    /**
     * Get the number of deaths a player has.
     */
    public static int getDeaths(Player p) {
        if (DSave.hasData(p, "DEATHS")) return (Integer) DSave.getData(p, "DEATHS");
        return -1;
    }

    /**
     * Get the number of deaths a player has.
     */
    public static int getDeaths(UUID p) {
        if (DSave.hasData(p, "DEATHS")) return (Integer) DSave.getData(p, "DEATHS");
        return -1;
    }

    /**
     * Gets the cost in Ascensions for the next Deity.
     */
    public static int costForNextDeity(UUID p) {
        switch (getDeities(p).size()) {
            case 1:
                return 2;
            case 2:
                return 5;
            case 3:
                return 9;
            case 4:
                return 14;
            case 5:
                return 19;
            case 6:
                return 25;
            case 7:
                return 30;
            case 8:
                return 35;
            case 9:
                return 40;
            case 10:
                return 50;
            case 11:
                return 60;
            case 12:
                return 70;
            case 13:
                return 80;
        }
        return -1;
    }

    public static int costForNextDeity(Player p) {
        return costForNextDeity(p.getUniqueId());
    }

    /**
     * Set a player's maximum favor amount.
     *
     * @param p
     * @param amt
     */
    public static void setFavorCap(UUID p, int amt) {
        if (amt > FAVORCAP) amt = FAVORCAP;
        DSave.saveData(p, "FAVORCAP", amt);
    }

    public static void setFavorCap(Player p, int amt) {
        setFavorCap(p.getUniqueId(), amt);
    }

    /**
     * Get a player's maximum favor.
     */
    public static int getFavorCap(UUID p) {
        if (DSave.hasData(p, "FAVORCAP")) return (Integer) DSave.getData(p, "FAVORCAP");
        return -1;
    }

    public static int getFavorCap(Player p) {
        return getFavorCap(p.getUniqueId());
    }

    /**
     * Gets the name rank of a player.
     *
     * @return
     */
    public static String getRank(Player p) {
        switch (getDeities(p).size()) {
            case 1:
                return "Apprentice";
            case 2:
                return "Acolyte";
            case 3:
                return "Zealot";
            case 4:
                return "Legionnaire";
            case 5:
                return "Champion";
            case 6:
                return "Hero";
            case 7:
                return "Demigod";
            case 8:
                return "Exalted";
            case 9:
                return "Ascended";
            case 10:
                return "Exemplar";
            default:
                return getAllegiance(p).equalsIgnoreCase("AEsir") ? "Valhallan" : "Jatunnspawn";
        }
    }

    /**
     * Gets the arbitrary numeric ranking
     * of a player.
     */
    public static long getRanking(UUID p) {
        return (getDevotion(p) / 100) + (getDeities(p).size() * 100) + (getKills(p) * 200) - (getDeaths(p) * 200);
    }

    /**
     * Checks if a certain material is bound to a skill by the player.
     *
     * @param p
     * @param material
     * @return
     */
    @SuppressWarnings("unchecked")
    public static boolean isBound(Player p, Material material) {
        return DSave.hasData(p, "BINDINGS") && ((ArrayList<Material>) DSave.getData(p, "BINDINGS")).contains(material);
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<Material> getBindings(Player p) {
        if (DSave.hasData(p, "BINDINGS")) {
            return (ArrayList<Material>) DSave.getData(p, "BINDINGS");
        }
        return null;
    }

    /**
     * Registers a material to the player's list of bound items.
     *
     * @param p
     * @param m
     */
    @SuppressWarnings("unchecked")
    public static void registerBind(Player p, Material m) {
        ArrayList<Material> used = new ArrayList<Material>();
        if (DSave.hasData(p, "BINDINGS")) {
            used = (ArrayList<Material>) DSave.getData(p, "BINDINGS");
            if (!used.contains(m)) used.add(m);
        } else {
            used.add(m);
        }
        DSave.saveData(p, "BINDINGS", used);
    }

    /**
     * Removes a material from the player's list of bound items.
     */
    @SuppressWarnings("unchecked")
    public static void removeBind(Player p, Material m) {
        ArrayList<Material> used;
        if (DSave.hasData(p, "BINDINGS")) {
            used = (ArrayList<Material>) DSave.getData(p, "BINDINGS");
            if (used.contains(m)) used.remove(m);
            DSave.saveData(p, "BINDINGS", used);
        }
    }

    /**
     * Grab the plugin.
     */
    public static NorseDemigods getPlugin() {
        return plugin;
    }

    /**
     * Checks if a player has all required attributes (should not give nulls).
     */
    public static boolean isFullParticipant(Player p) {
        return isFullParticipant(p.getUniqueId());
    }

    /**
     * Check if a player has all required attributes.
     */
    public static boolean isFullParticipant(UUID p) {
        if (getAllegiance(p) == null) return false;
        if (getDeaths(p) == -1) return false;
        if (getKills(p) == -1) return false;
        if ((getDeities(p) == null) || (getDeities(p).size() == 0)) return false;
        return getAscensions(p) != -1 && getMaxHP(p) != -1 && getFavor(p) != -1 && getFavorCap(p) != -1;
    }

    @Deprecated
    public static boolean isFullParticipant(String p) {
        try {
            return isFullParticipant(getDemigodsPlayerId(p));
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Checks if one team has an advantage over the other by greater than the given %.
     *
     * @param alliance
     * @return
     */
    public static boolean hasAdvantage(String alliance) {
        HashMap<String, Integer> alliances = new HashMap<String, Integer>();
        for (UUID player : DSave.getCompleteData().keySet()) {
            if (DSave.hasData(player, "ALLEGIANCE")) {
                if (DSave.hasData(player, "LASTLOGINTIME"))
                    if ((Long) DSave.getData(player, "LASTLOGINTIME") < System.currentTimeMillis() - 604800000)
                        continue;
                if (alliances.containsKey(((String) DSave.getData(player, "ALLEGIANCE")).toUpperCase())) {
                    int put = alliances.remove(((String) DSave.getData(player, "ALLEGIANCE")).toUpperCase()) + 1;
                    alliances.put(((String) DSave.getData(player, "ALLEGIANCE")).toUpperCase(), put);
                } else alliances.put(((String) DSave.getData(player, "ALLEGIANCE")).toUpperCase(), 1);
            }
        }
        @SuppressWarnings("unchecked")
        HashMap<String, Integer> talliances = (HashMap<String, Integer>) alliances.clone();
        ArrayList<String> alliancerank = new ArrayList<String>();
        Logger.getLogger("Minecraft").info("Total alliances: " + alliances.size());
        Logger.getLogger("Minecraft").info(alliances + "");
        for (int i = 0; i < alliances.size() + 1; i++) {
            String newleader = "";
            int leadamt = -1;
            for (Map.Entry<String, Integer> all : alliances.entrySet()) {
                if (all.getValue() > leadamt) {
                    leadamt = all.getValue();
                    newleader = all.getKey();
                }
            }
            alliancerank.add(newleader);
            alliances.remove(newleader);
        }
        if (alliancerank.size() == 1) return false;
        return alliancerank.get(0).equalsIgnoreCase(alliance) && DCommandExecutor.ADVANTAGEPERCENT <= ((double) talliances.get(alliancerank.get(0)) / talliances.get(alliancerank.get(1)));
    }

    /**
     * Calculates the value of the item.
     *
     * @param ii
     * @return
     */
    public static int getValue(ItemStack ii) {
        int val = 0;
        if (ii == null) return 0;
        switch (ii.getType()) {
            case STONE:
                val += ii.getAmount() * 0.5;
                break;
            case COBBLESTONE:
                val += ii.getAmount() * 0.3;
                break;
            case DIRT:
                val += ii.getAmount() * 0.1;
                break;
            case LOG:
                val += ii.getAmount();
                break;
            case WOOD:
                val += ii.getAmount() * 0.23;
                break;
            case STICK:
                val += ii.getAmount() * 0.11;
                break;
            case GLASS:
                val += ii.getAmount() * 1.5;
                break;
            case LAPIS_BLOCK:
                val += ii.getAmount() * 85;
                break;
            case SANDSTONE:
                val += ii.getAmount() * 0.9;
                break;
            case GOLD_BLOCK:
                val += ii.getAmount() * 170;
                break;
            case IRON_BLOCK:
                val += ii.getAmount() * 120;
                break;
            case BRICK:
                val += ii.getAmount() * 10;
                break;
            case TNT:
                val += ii.getAmount() * 10;
                break;
            case MOSSY_COBBLESTONE:
                val += ii.getAmount() * 10;
                break;
            case OBSIDIAN:
                val += ii.getAmount() * 10;
                break;
            case DIAMOND_BLOCK:
                val += ii.getAmount() * 300;
                break;
            case CACTUS:
                val += ii.getAmount() * 1.7;
                break;
            case YELLOW_FLOWER:
                val += ii.getAmount() * 0.1;
                break;
            case SEEDS:
                val += ii.getAmount() * 0.3;
                break;
            case PUMPKIN:
                val += ii.getAmount() * 0.7;
                break;
            case CAKE:
                val += ii.getAmount() * 22;
                break;
            case APPLE:
                val += ii.getAmount() * 5;
                break;
            case COAL:
                val += ii.getAmount() * 2.5;
                break;
            case DIAMOND:
                val += ii.getAmount() * 30;
                break;
            case IRON_ORE:
                val += ii.getAmount() * 7;
                break;
            case GOLD_ORE:
                val += ii.getAmount() * 13;
                break;
            case IRON_INGOT:
                val += ii.getAmount() * 12;
                break;
            case GOLD_INGOT:
                val += ii.getAmount() * 18;
                break;
            case STRING:
                val += ii.getAmount() * 2.4;
                break;
            case WHEAT:
                val += ii.getAmount() * 0.6;
                break;
            case BREAD:
                val += ii.getAmount() * 2;
                break;
            case RAW_FISH:
                val += ii.getAmount() * 2.4;
                break;
            case PORK:
                val += ii.getAmount() * 2.4;
                break;
            case COOKED_FISH:
                val += ii.getAmount() * 3.4;
                break;
            case GRILLED_PORK:
                val += ii.getAmount() * 3.4;
                break;
            case GOLDEN_APPLE:
                val += ii.getAmount() * 190;
                break;
            case GOLD_RECORD:
                val += ii.getAmount() * 60;
                break;
            case GREEN_RECORD:
                val += ii.getAmount() * 60;
                break;
            case GLOWSTONE:
                val += ii.getAmount() * 1.7;
                break;
            case REDSTONE:
                val += ii.getAmount() * 3.3;
                break;
            case EGG:
                val += ii.getAmount() * 0.3;
                break;
            case SUGAR:
                val += ii.getAmount() * 1.2;
                break;
            case BONE:
                val += ii.getAmount() * 3;
                break;
            case ENDER_PEARL:
                val += ii.getAmount() * 1.7;
                break;
            case SULPHUR:
                val += ii.getAmount() * 1.2;
                break;
            case COCOA:
                val += ii.getAmount() * 0.6;
                break;
            case ROTTEN_FLESH:
                val += ii.getAmount() * 3;
                break;
            case RAW_CHICKEN:
                val += ii.getAmount() * 2;
                break;
            case COOKED_CHICKEN:
                val += ii.getAmount() * 2.6;
                break;
            case RAW_BEEF:
                val += ii.getAmount() * 2;
                break;
            case COOKED_BEEF:
                val += ii.getAmount() * 2.7;
                break;
            case MELON:
                val += ii.getAmount() * 0.8;
                break;
            case COOKIE:
                val += ii.getAmount() * 0.45;
                break;
            case VINE:
                val += ii.getAmount() * 1.2;
                break;
            case EMERALD:
                val += ii.getAmount() * 7;
                break;
            case EMERALD_BLOCK:
                val += ii.getAmount() * 69;
                break;
            case DRAGON_EGG:
                val += ii.getAmount() * 10000;
                break;
            default:
                val += ii.getAmount() * 0.1;
                break;
        }
        return val;
    }

    /**
     * Used for adding a player to "full participant" status
     */
    public static void initializePlayer(UUID player, String allegiance, Deity deity) {
        setAllegiance(player, allegiance);
        setFavorCap(player, 300); // set favor cap before favor (MUST!!!)
        setFavor(player, 300);
        setMaxHP(player, 25.0);
        setHP(player, 25.0);
        setAscensions(player, 0);
        setDeaths(player, 0);
        setKills(player, 0);
        giveDeity(player, deity);
        setActiveEffects(player, new HashMap<String, Long>());
        setShrines(player, new HashMap<String, LocationSaveable>());
    }

    /**
     * If the given location is a shrine, returns the deity the shrine is for
     */
    public static String getDeityAtShrine(LocationSaveable shrine) {
        for (UUID player : getFullParticipants()) {
            for (String shrinename : getShrines(player).keySet()) {
                if (shrine.equalsApprox(getShrines(player).get(shrinename))) {
                    if (shrinename.charAt(0) != '#') return shrinename;
                }
            }
        }
        return null;
    }

    public static LocationSaveable getNearbyShrine(Location l) {
        LocationSaveable shrine = null;
        for (LocationSaveable w : getAllShrines()) {
            if (!w.getWorld().equals(l.getWorld().getName())) continue;
            Location l1 = DMiscUtil.toLocation(w);
            if (l1.distance(l) < DShrines.RADIUS) {
                shrine = w;
                break;
            }
        }
        return shrine;
    }

    /**
     * If the given location is a shrine, returns the creator
     *
     * @param shrine
     * @return
     */
    public static UUID getOwnerOfShrine(LocationSaveable shrine) {
        for (UUID player : getFullParticipants()) {
            for (String shrinename : getShrines(player).keySet()) {
                if (shrine.equalsApprox(getShrines(player).get(shrinename))) {
                    return player;
                }
            }
        }
        return null;
    }

    /**
     * If given location is shrine and given player is valid (same alliance),
     * register the player as a guest
     */
    @SuppressWarnings("unchecked")
    public static void addGuest(LocationSaveable shrine, UUID guest) {
        if (!isFullParticipant(guest)) return;
        if (!getAllegiance(guest).equalsIgnoreCase(getAllegiance(getOwnerOfShrine(shrine)))) return;
        if (!DSave.hasData(guest, "S_GUESTAT")) DSave.saveData(guest, "S_GUESTAT", new ArrayList<LocationSaveable>());
        ArrayList<LocationSaveable> list = ((ArrayList<LocationSaveable>) DSave.getData(guest, "S_GUESTAT"));
        list.add(shrine);
        DSave.saveData(guest, "S_GUESTAT", list);
    }

    /**
     * If given location is a shrine and given player is a guest, remove the
     * player from guest list
     *
     * @param shrine
     * @param name
     * @return if the removal was successful
     */
    @SuppressWarnings("unchecked")
    public static boolean removeGuest(LocationSaveable shrine, UUID name) {
        if (!isFullParticipant(name)) return false;
        if (!DSave.hasData(name, "S_GUESTAT")) DSave.saveData(name, "S_GUESTAT", new ArrayList<LocationSaveable>());
        ArrayList<LocationSaveable> list = (ArrayList<LocationSaveable>) DSave.getData(name, "S_GUESTAT");
        Iterator<LocationSaveable> it = list.iterator();
        boolean success = false;
        while (it.hasNext()) {
            if ((it.next()).equalsApprox(shrine)) {
                it.remove();
                success = true;
            }
        }
        return success;
    }

    /**
     * Check if a given player is allowed to warp to a given shrine
     *
     * @param shrine
     * @param player
     * @return
     */
    public static boolean isGuest(LocationSaveable shrine, UUID player) {
        if (!isFullParticipant(player)) return false;
        for (LocationSaveable w : getAccessibleShrines(player)) {
            if (w.equalsApprox(shrine)) return true;
        }
        return false;
    }

    /**
     * Get all shrines that a player is able to access
     *
     * @param player
     * @return
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<LocationSaveable> getAccessibleShrines(UUID player) {
        if (!isFullParticipant(player)) return null;
        if (!DSave.hasData(player, "S_GUESTAT")) DSave.saveData(player, "S_GUESTAT", new ArrayList<LocationSaveable>());
        return (ArrayList<LocationSaveable>) DSave.getData(player, "S_GUESTAT");

    }

    /**
     * Get all the players who are able to access a given shrine
     *
     * @param shrine
     * @return
     */
    public static ArrayList<UUID> getShrineGuestlist(LocationSaveable shrine) {
        ArrayList<UUID> list = new ArrayList<UUID>();
        for (UUID p : getFullParticipants()) {
            for (LocationSaveable w : DMiscUtil.getAccessibleShrines(p)) {
                if (w.equalsApprox(shrine)) list.add(p);
            }
        }
        return list;
    }

    /**
     * Remove a shrine that a player created
     *
     * @param shrine
     * @return the shrine that was removed
     */
    public static void removeShrine(LocationSaveable shrine) {
        try {
            toLocation(shrine).getBlock().setType(Material.AIR);
        } catch (NullPointerException ignored) {
        }
        for (UUID p : getFullParticipants()) {
            // remove from main lists
            HashMap<String, LocationSaveable> replace = new HashMap<String, LocationSaveable>();
            for (String key : getShrines(p).keySet()) {
                if (!getShrines(p).get(key).equalsApprox(shrine)) replace.put(key, getShrines(p).get(key));
            }
            DMiscUtil.setShrines(p, replace);
            // remove from guest lists
            while (getAccessibleShrines(p).contains(shrine))
                getAccessibleShrines(p).remove(shrine);
        }
    }

    /**
     * Gets the shrine associated with the unique key (full name)
     *
     * @param shrinename
     * @return
     */
    public static LocationSaveable getShrineByKey(String shrinename) {
        for (UUID p : getFullParticipants()) {
            if (getShrines(p).containsKey(shrinename)) return getShrines(p).get(shrinename);
        }
        return null;
    }

    /**
     * Renames the given shrine to the given value
     *
     * @param shrine
     * @param newname
     * @return if it worked (cannot begin with "#", name cannot already be used)
     */
    public static boolean renameShrine(LocationSaveable shrine, String newname) {
        if (newname.charAt(0) == '#') return false;
        for (UUID p : getFullParticipants()) {
            if (getShrines(p).containsKey(newname)) return false;
        }
        UUID owner = DMiscUtil.getOwnerOfShrine(shrine);
        String tomodify = null;
        for (String shrinename : getShrines(owner).keySet()) {
            if (getShrines(owner).get(shrinename).equalsApprox(shrine)) {
                if (shrinename.charAt(0) == '#') tomodify = shrinename;
            }
        }
        if (tomodify == null) return false;
        // rename
        getShrines(owner).remove(tomodify);
        getShrines(owner).put("#" + newname, shrine);
        return true;
    }

    /**
     * Returns name of shrine if set, or the player+deity by default
     *
     * @param shrine
     * @return
     */
    public static String getShrineName(LocationSaveable shrine) {
        UUID owner = getOwnerOfShrine(shrine);
        if ((shrine == null) || (owner == null)) return null;
        for (String shrinename : getShrines(owner).keySet()) {
            if (getShrines(owner).get(shrinename).equalsApprox(shrine)) {
                if (shrinename.charAt(0) == '#') return shrinename.substring(1);
            }
        }
        return "[" + owner + " " + getDeityAtShrine(shrine) + "]";
    }

    private static void setShrines(UUID p, HashMap<String, LocationSaveable> data) {
        DSave.saveData(p, "P_SHRINES", data);
    }

    @SuppressWarnings("unchecked")
    public static void addShrine(UUID p, String deityname, LocationSaveable loc) {
        if (DSave.hasData(p, "P_SHRINES")) {
            ((HashMap<String, LocationSaveable>) DSave.getData(p, "P_SHRINES")).put(deityname, loc);
            return;
        }
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, LocationSaveable> getShrines(UUID p) {
        if (DSave.hasData(p, "P_SHRINES")) return (HashMap<String, LocationSaveable>) DSave.getData(p, "P_SHRINES");
        return null;
    }

    @SuppressWarnings("unchecked")
    public static LocationSaveable getShrine(UUID p, String deityname) {
        if (DSave.hasData(p, "P_SHRINES")) {
            HashMap<String, LocationSaveable> original = (HashMap<String, LocationSaveable>) DSave.getData(p, "P_SHRINES");
            for (Map.Entry<String, LocationSaveable> s : original.entrySet()) {
                if (s.getKey().equalsIgnoreCase(deityname)) return s.getValue();
            }
        }
        return null;
    }

    /**
     * Set a player's active effects
     *
     * @param p
     * @param data
     */
    public static void setActiveEffects(UUID p, HashMap<String, Long> data) {
        DSave.saveData(p, "A_EFFECTS", data);
    }

    /**
     * Add a single active effect, using its name and its duration in seconds
     *
     * @param p
     * @param effectname
     * @param lengthInSeconds
     * @return
     */
    @SuppressWarnings("unchecked")
    public static void addActiveEffect(UUID p, String effectname, int lengthInSeconds) {
        if (DSave.hasData(p, "A_EFFECTS")) {
            ((HashMap<String, Long>) DSave.getData(p, "A_EFFECTS")).put(effectname, System.currentTimeMillis() + lengthInSeconds * 1000);
        }
    }

    public static void removeActiveEffect(UUID p, String effectname) {
        for (String effect : DMiscUtil.getActiveEffectsList(p)) {
            if (effect.equals(effectname)) DMiscUtil.getActiveEffects(p).remove(effect);
        }
    }

    /**
     * Returns the effects on a player that are still active
     */
    @SuppressWarnings("unchecked")
    public static HashMap<String, Long> getActiveEffects(UUID p) {
        if (DSave.hasData(p, "A_EFFECTS")) {
            HashMap<String, Long> original = ((HashMap<String, Long>) DSave.getData(p, "A_EFFECTS"));
            HashMap<String, Long> toreturn = new HashMap<String, Long>();
            for (Map.Entry<String, Long> s : original.entrySet()) {
                if (s.getValue() > System.currentTimeMillis()) toreturn.put(s.getKey(), s.getValue());
            }
            setActiveEffects(p, toreturn); // clean original
            return toreturn;
        }
        return null;
    }

    /**
     * Returns the effects on a player that are still active
     */
    public static ArrayList<String> getActiveEffectsList(UUID p) {
        if (DSave.hasData(p, "A_EFFECTS")) {
            HashMap<String, Long> original = getActiveEffects(p);
            ArrayList<String> toreturn = new ArrayList<String>();
            for (Map.Entry<String, Long> s : original.entrySet())
                if (s.getValue() > System.currentTimeMillis()) toreturn.add(s.getKey());
            return toreturn;
        }
        return null;
    }

    /**
     * Gets a list of all shrines
     *
     * @return
     */
    public static List<LocationSaveable> getAllShrines() {
        return new ArrayList<LocationSaveable>() {
            {
                for (UUID player : getFullParticipants())
                    for (LocationSaveable w : getShrines(player).values())
                        add(w);
            }
        };
    }

    /**
     * Get the names of all "full participants"
     *
     * @return
     */
    public static Collection<UUID> getFullParticipants() {
        return Collections2.filter(DSave.getCompleteData().keySet(), new Predicate<UUID>() {
            @Override
            public boolean apply(UUID s) {
                return isFullParticipant(s);
            }
        });
    }

    /*
     * WORLDGUARD SUPPORT START
     */
    public static boolean canWorldGuardPVP(Location l) {
        return ALLOWPVPEVERYWHERE || WorldGuardUtil.worldGuardEnabled() && WorldGuardUtil.canPVP(l);
    }

    @Deprecated
    private static boolean canWorldGuardLegacyPVP(Location l) {
        return canWorldGuardPVP(l);
    }

    @SuppressWarnings("static-access")
    public static boolean canWorldGuardBuild(Player player, Location location) {
        return !WorldGuardUtil.worldGuardEnabled() || WorldGuardUtil.canBuild(player, location);
    }

    /*
     * WORLDGUARD SUPPORT END
     */
    @Deprecated
    public static boolean canLocationPVP(Location l) {
        return (canWorldGuardPVP(l));
    }

    public static boolean canTarget(Entity player, Location location) {
        if (!(player instanceof Player)) return true;
        else if (!USENEWPVP) return canWorldGuardPVP(location);
        else if (!isFullParticipant((Player) player)) return canWorldGuardPVP(location);
        else return (DSave.hasData((Player) player, "temp_was_PVP")) || canWorldGuardPVP(location);
    }

    /**
     * Demigods damage handling
     */
    public static void damageDemigods(LivingEntity source, LivingEntity target, double amount, DamageCause cause) {
        if (target.getHealth() > 1) target.damage(1);
        if (target instanceof Player && isFullParticipant((Player) target)) {
            if (((Player) target).getGameMode() == GameMode.CREATIVE) return;
            if (!canTarget(target, target.getLocation())) return;
            double hp = getHP((Player) target);
            if (amount < 1) return;
            amount -= DDamage.armorReduction((Player) target);
            amount = DDamage.specialReduction((Player) target, amount);
            if (amount < 1) return;
            setHP(((Player) target), hp - amount);
            if (source instanceof Player) DFixes.setLastDamageBy(source, target, cause, amount);
            DDamage.syncHealth(((Player) target));
        } else {
            EntityDamageByEntityEvent event = new EntityDamageByEntityEvent(source, target, cause, amount);
            DFixes.processed.add(event); // Demigods should ignore this event from here out.
            Bukkit.getPluginManager().callEvent(event);
            if (amount >= 1 && !event.isCancelled()) {
                target.setLastDamageCause(event);
                target.damage(amount);
            }
        }
    }

    public static void damageDemigodsNonCombat(Player target, double amount, DamageCause cause) {
        if ((target).getGameMode() == GameMode.CREATIVE) return;
        double hp = getHP(target);
        if (amount < 1) return;
        amount -= DDamage.armorReduction(target);
        amount = DDamage.specialReduction(target, amount);
        if (amount < 1) return;
        setHP((target), hp - amount);
        DFixes.setLastDamage(target, cause, amount);
    }

    public static Plugin getPlugin(final String p) {
        try {
            return Iterators.find(Sets.newHashSet(plugin.getServer().getPluginManager().getPlugins()).iterator(), new Predicate<Plugin>() {
                @Override
                public boolean apply(Plugin pl) {
                    return pl.getDescription().getName().equalsIgnoreCase(p);
                }
            });
        } catch (NoSuchElementException ignored) {
        }
        return null;
    }

    public static void taggedMessage(CommandSender sender, String msg) {
        sender.sendMessage(ChatColor.DARK_AQUA + "[Demigods] " + ChatColor.RESET + msg);
    }

    public static void horseTeleport(Player player, Location location) {
        if (player.isInsideVehicle() && player.getVehicle() instanceof Horse) {
            Horse horse = (Horse) player.getVehicle();
            DSave.saveData(player, "temp_horse", true);
            horse.eject();
            horse.teleport(location);
            horse.setPassenger(player);
            DSave.removeData(player, "temp_horse");
        } else player.teleport(location);
    }
}
