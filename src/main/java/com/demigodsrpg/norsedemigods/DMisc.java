package com.demigodsrpg.norsedemigods;

import com.demigodsrpg.norsedemigods.deity.Deities;
import com.demigodsrpg.norsedemigods.listener.DDamage;
import com.demigodsrpg.norsedemigods.listener.DShrines;
import com.demigodsrpg.norsedemigods.saveable.LocationSaveable;
import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
import com.demigodsrpg.norsedemigods.saveable.ShrineSaveable;
import com.demigodsrpg.norsedemigods.util.WorldGuardUtil;
import com.google.common.base.Predicate;
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
import java.util.stream.Collectors;

public class DMisc {

    @Deprecated
    public static UUID getDemigodsPlayerId(String name) {
        PlayerDataSaveable found = null;
        String lowerName = name.toLowerCase();
        int delta = Integer.MAX_VALUE;
        for (PlayerDataSaveable data : getPlugin().getPlayerDataRegistry().getFromDb().values()) {
            String playername = data.getLastKnownName();
            if (playername.toLowerCase().startsWith(lowerName)) {
                int curDelta = playername.length() - lowerName.length();
                if (curDelta < delta) {
                    found = data;
                    delta = curDelta;
                }
                if (curDelta == 0) break;
            }
        }
        return found != null ? found.getPlayerId() : null;
    }

    @Deprecated
    public static PlayerDataSaveable getDemigodsPlayer(String name) {
        PlayerDataSaveable found = null;
        String lowerName = name.toLowerCase();
        int delta = Integer.MAX_VALUE;
        for (PlayerDataSaveable data : getPlugin().getPlayerDataRegistry().getFromDb().values()) {
            String playername = data.getLastKnownName();
            if (playername.toLowerCase().startsWith(lowerName)) {
                int curDelta = playername.length() - lowerName.length();
                if (curDelta < delta) {
                    found = data;
                    delta = curDelta;
                }
                if (curDelta == 0) break;
            }
        }
        return found;
    }

    public static String getLastKnownName(UUID p) {
        Optional<PlayerDataSaveable> opData = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opData.isPresent()) {
            return opData.get().getLastKnownName();
        }
        return "";
    }

    /**
     * Gets the Location a Player is looking at.
     */
    public static Location getTargetLocation(Player p) {
        return p.getTargetBlock((Set) null, Setting.MAX_TARGET_RANGE).getLocation();
    }

    /**
     * Gets the LivingEntity a Player is looking at.
     */
    public static LivingEntity getTargetLivingEntity(Player p, int offset) {
        LivingEntity e = null;
        List<Block> bL = p.getLineOfSight((Set) null, Setting.MAX_TARGET_RANGE);
        for (Block b : bL) {
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
        return l.toLocation(getPlugin());
    }

    /**
     * Checks if a player has the given permission or is OP.
     */
    public static boolean isAdminOrOp(Player p) {// convenience method for permissions
        return p.isOp() || p.hasPermission("demigods.admin");
    }

    public static void setJotunn(Player p) {
        getPlugin().getPlayerDataRegistry().fromPlayer(p).setAlliance("jotunn");
    }

    public static void setAEsir(Player p) {
        getPlugin().getPlayerDataRegistry().fromPlayer(p).setAlliance("aesir");
    }

    public static void setJotunn(UUID p) {
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            opSave.get().setAlliance("jotunn");
        }
    }

    public static void setAEsir(UUID p) {
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            opSave.get().setAlliance("aesir");
        }
    }

    public static void setAllegiance(Player p, String allegiance) {
        getPlugin().getPlayerDataRegistry().fromPlayer(p).setAlliance(allegiance);
    }

    public static void setAllegiance(UUID p, String allegiance) {
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            opSave.get().setAlliance(allegiance);
        }
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
        PlayerDataSaveable player = getPlugin().getPlayerDataRegistry().fromPlayer(p);
        if (player.getAlliance() != null) {
            return player.getAlliance();
        }
        return "Human";
    }

    /**
     * Gets the String representation of a player's allegiance.
     */
    public static String getAllegiance(UUID id) {
        Optional<PlayerDataSaveable> opPlayer = getPlugin().getPlayerDataRegistry().fromKey(id.toString());
        if (opPlayer.isPresent() && opPlayer.get().getAlliance() != null) {
            return opPlayer.get().getAlliance();
        }
        return "Human";
    }

    /**
     * Gives a player a deity, even if they have none.
     */
    public static void giveDeity(Player p, Deity d) {
        giveDeity(p.getUniqueId(), d);
    }

    public static void giveDeity(UUID p, Deity d) {
        Player player = Bukkit.getPlayer(p);
        if (player == null) {
            getPlugin().getLogger().info(p + " is not currently online.");
            return;
        } else if (!player.hasPermission(d.getDefaultAlliance().toLowerCase() + "." + d.getName().toLowerCase()) &&
                (!player.hasPermission(d.getDefaultAlliance().toLowerCase() + ".all"))) {
            getPlugin().getLogger().info(p + " does not have permission to get this deity.");
            return;
        }

        if (Setting.BROADCAST_NEW_DEITY) {
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
            getPlugin().getServer().broadcastMessage(message);
        }
        PlayerDataSaveable saveable = getPlugin().getPlayerDataRegistry().fromPlayer(player);
        saveable.addDeity(d.getName());
        setDevotion(p, d, 1);
    }

    /**
     * Checks if a player has a named deity.
     *
     * @param p
     * @param name
     * @return
     */
    public static boolean hasDeity(Player p, String name) {
        return getPlugin().getPlayerDataRegistry().fromPlayer(p).getDeityList().contains(name);
    }

    public static boolean hasDeity(UUID p, String name) {
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        return opSave.isPresent() && opSave.get().getDeityList().contains(name);
    }

    /**
     * Gives the list of all the player's deities.
     */
    public static List<Deity> getDeities(Player p) {
        return getPlugin().getPlayerDataRegistry().fromPlayer(p).getDeityList().stream().map(Deities::valueOf).
                collect(Collectors.toList());
    }

    public static Collection<Deity> getTributeableDeities(Player p) {
        return getTributeableDeities(p.getUniqueId());
    }

    /**
     * Gives the list of all the player's deities.
     */
    public static List<Deity> getDeities(UUID p) {
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            return opSave.get().getDeityList().stream().map(Deities::valueOf).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * Gives the list of all the player's deities.
     */
    public static List<Deity> getTributeableDeities(UUID p) {
        return getDeities(p).stream().filter(Deity::canTribute).collect(Collectors.toList());
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
        if (amt > getFavorCap(p)) amt = getFavorCap(p);
        int c = amt - getFavor(p);
        getPlugin().getPlayerDataRegistry().fromPlayer(p).setFavor(amt);
    }

    /**
     * Set a player's favor.
     *
     * @param p
     * @param amt
     */
    public static void setFavor(UUID p, int amt) {
        if (amt > getFavorCap(p)) amt = getFavorCap(p);
        // int c = amt - getFavor(p);
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            opSave.get().setFavor(amt);
        }
    }

    public static void setFavorQuiet(UUID p, int amt) {
        if (amt > getFavorCap(p)) amt = getFavorCap(p);
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            opSave.get().setFavor(amt);
        }
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
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            return opSave.get().getFavor();
        }
        return -1;
    }

    /**
     * Set a player's HP.
     *
     * @param p
     * @param amt
     */
    public static void setHP(Player p, double amt) {
        if (amt > p.getMaxHealth()) amt = p.getMaxHealth();
        if (amt < 0) amt = 0;
        // double c = amt - p.getHealth();
        if (amt != 0) {
            p.setHealth(amt);
        } else {
            p.spigot().respawn();
        }
        /*if ((c != 0)) {
            ChatColor color = ChatColor.GREEN;
            if ((p.getHealth() / p.getMaxHealth()) < 0.25) color = ChatColor.RED;
            else if ((p.getHealth() / p.getMaxHealth()) < 0.5) color = ChatColor.YELLOW;
            String disp = "";
            if (c > 0) disp = "+" + c;
            else disp += c;
            String str = color + "HP: " + p.getHealth() + "/" + p.getMaxHealth() + " (" + disp + ")";
        }*/
    }

    public static void setHPQuiet(Player p, double amt) {
        if (amt > p.getHealth()) amt = p.getMaxHealth();
        if (amt < 0) amt = 0;
        if (amt != 0) {
            p.setHealth(amt);
        } else {
            p.spigot().respawn();
        }
    }

    /**
     * Set a player's max HP.
     */
    public static void setMaxHP(Player p, double amt) {
        if (amt > Setting.MAXIMUM_HP) amt = Setting.MAXIMUM_HP;
        p.setHealthScale(amt);
        p.setMaxHealth(amt);
    }

    /**
     * Set a player's devotion for a specific deity.
     */
    public static boolean setDevotion(UUID p, String deityname, int amt) {
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            opSave.get().setDevotion(deityname, amt);
            return true;
        }
        return false;
    }

    public static void setDevotion(Player p, String deityname, int amt) {
        PlayerDataSaveable saveable = getPlugin().getPlayerDataRegistry().fromPlayer(p);
        saveable.setDevotion(deityname, amt);
    }

    public static void setDevotion(Player p, Deity d, int amt) {
        setDevotion(p, d.getName(), amt);
    }

    public static void setDevotion(UUID p, Deity d, int amt) {
        setDevotion(p, d.getName(), amt);
    }

    /**
     * Get a player's devotion for a specific deity.
     */
    public static int getDevotion(Player p, String deityname) {
        PlayerDataSaveable saveable = getPlugin().getPlayerDataRegistry().fromPlayer(p);
        return saveable.getDevotion(deityname);
    }

    public static int getDevotion(UUID p, String deityname) {
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            return opSave.get().getDevotion(deityname);
        }
        return -1;
    }

    public static int getDevotion(Player p, Deity d) {
        return getDevotion(p, d.getName());
    }

    public static int getDevotion(UUID p, Deity d) {
        return getDevotion(p, d.getName());
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
        PlayerDataSaveable saveable = getPlugin().getPlayerDataRegistry().fromPlayer(p);
        return saveable.getUnclaimedDevotion();
    }

    public static int getUnclaimedDevotion(UUID p) {
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            return opSave.get().getUnclaimedDevotion();
        }
        return -1;
    }

    /**
     * Set the unclaimed devotion a player has been given.
     *
     * @param p
     * @param amount
     */
    public static void setUnclaimedDevotion(Player p, int amount) {
        PlayerDataSaveable saveable = getPlugin().getPlayerDataRegistry().fromPlayer(p);
        saveable.setUnclamedDevotion(amount);
    }

    public static void setUnclaimedDevotion(UUID p, int amount) {
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            opSave.get().setUnclamedDevotion(amount);
        }
    }

    /**
     * Set a player's number of ascensions.
     *
     * @param p
     * @param amt
     */
    public static void setAscensions(UUID p, int amt) {
        if (amt > Setting.ASCENSION_CAP) amt = Setting.ASCENSION_CAP;
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            opSave.get().setAscensions(amt);
        }
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
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            return opSave.get().getAscensions();
        }
        return -1;
    }

    public static int getAscensions(Player p) {
        PlayerDataSaveable saveable = getPlugin().getPlayerDataRegistry().fromPlayer(p);
        return saveable.getAscensions();
    }

    /**
     * Set the number of kills a player has.
     *
     * @param p
     * @param amt
     */
    public static void setKills(Player p, int amt) {
        PlayerDataSaveable saveable = getPlugin().getPlayerDataRegistry().fromPlayer(p);
        saveable.setKills(amt);
    }

    public static void setKills(UUID p, int amt) {
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            opSave.get().setKills(amt);
        }
    }

    /**
     * Set the number of deaths a player has.
     *
     * @param p
     * @param amt
     */
    public static void setDeaths(Player p, int amt) {
        PlayerDataSaveable saveable = getPlugin().getPlayerDataRegistry().fromPlayer(p);
        saveable.setDeaths(amt);
    }

    public static void setDeaths(UUID p, int amt) {
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            opSave.get().setDeaths(amt);
        }
    }

    /**
     * Get the number of kills a player has.
     */
    public static int getKills(Player p) {
        PlayerDataSaveable saveable = getPlugin().getPlayerDataRegistry().fromPlayer(p);
        return saveable.getKills();
    }

    /**
     * Get the number of kills a player has.
     */
    public static int getKills(UUID p) {
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            return opSave.get().getKills();
        }
        return -1;
    }

    /**
     * Get the number of deaths a player has.
     */
    public static int getDeaths(Player p) {
        PlayerDataSaveable saveable = getPlugin().getPlayerDataRegistry().fromPlayer(p);
        return saveable.getDeaths();
    }

    /**
     * Get the number of deaths a player has.
     */
    public static int getDeaths(UUID p) {
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            return opSave.get().getDeaths();
        }
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
        if (amt > Setting.FAVOR_CAP) amt = Setting.FAVOR_CAP;
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            opSave.get().setMaxFavor(amt);
        }
    }

    public static void setFavorCap(Player p, int amt) {
        setFavorCap(p.getUniqueId(), amt);
    }

    /**
     * Get a player's maximum favor.
     */
    public static int getFavorCap(UUID p) {
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            return opSave.get().getMaxFavor();
        }
        return -1;
    }

    public static int getFavorCap(Player p) {
        PlayerDataSaveable saveable = getPlugin().getPlayerDataRegistry().fromPlayer(p);
        return saveable.getMaxFavor();
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
        PlayerDataSaveable saveable = getPlugin().getPlayerDataRegistry().fromPlayer(p);
        return saveable.getBound().contains(material);
    }

    @SuppressWarnings("unchecked")
    public static List<Material> getBindings(Player p) {
        PlayerDataSaveable saveable = getPlugin().getPlayerDataRegistry().fromPlayer(p);
        return saveable.getBound();
    }

    /**
     * Grab the plugin.
     */
    public static NorseDemigods getPlugin() {
        return NorseDemigods.INST;
    }

    /**
     * Checks if a player has all required attributes (should not give nulls).
     */
    public static boolean isFullParticipant(Player p) {
        return isFullParticipant(p.getUniqueId());
    }

    /**
     * Checks if a player has all required attributes (should not give nulls).
     */
    public static boolean isFullParticipant(String name) {
        return isFullParticipant(getDemigodsPlayerId(name));
    }

    /**
     * Check if a player has all required attributes.
     */
    public static boolean isFullParticipant(UUID p) {
        if (getAllegiance(p) == null) return false;
        if (getDeaths(p) == -1) return false;
        if (getKills(p) == -1) return false;
        if ((getDeities(p) == null) || (getDeities(p).isEmpty())) return false;
        return getAscensions(p) != -1 && getFavor(p) != -1 && getFavorCap(p) != -1;
    }

    /**
     * Checks if one team has an advantage over the other by greater than the given %.
     *
     * @param alliance
     * @return
     */
    public static boolean hasAdvantage(String alliance) {
        HashMap<String, Integer> alliances = new HashMap<String, Integer>();
        for (PlayerDataSaveable saveable : getPlugin().getPlayerDataRegistry().getFromDb().values()) {
            if (!saveable.getAlliance().equals("Human")) {
                if (saveable.getLastLoginTime() < System.currentTimeMillis() - 604800000)
                        continue;
                if (alliances.containsKey(saveable.getAlliance().toUpperCase())) {
                    int put = alliances.remove(saveable.getAlliance().toUpperCase().toUpperCase()) + 1;
                    alliances.put(saveable.getAlliance().toUpperCase(), put);
                } else alliances.put(saveable.getAlliance().toUpperCase(), 1);
            }
        }
        @SuppressWarnings("unchecked")
        HashMap<String, Integer> talliances = (HashMap<String, Integer>) alliances.clone();
        ArrayList<String> alliancerank = new ArrayList<String>();
        getPlugin().getLogger().info("Total alliances: " + alliances.size());
        getPlugin().getLogger().info(alliances + "");
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
        return alliancerank.get(0).equalsIgnoreCase(alliance) && DCommandExecutor.ADVANTAGEPERCENT <= ((double)
                talliances.get(alliancerank.get(0)) / talliances.get(alliancerank.get(1)));
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
    public static void initializePlayer(Player player, String allegiance, Deity deity) {
        setAllegiance(player, allegiance);
        setFavorCap(player, 300); // set favor cap before favor (MUST!!!)
        setFavor(player, 300);
        setMaxHP(player, 25.0);
        setHP(player, 25.0);
        setAscensions(player.getUniqueId(), 0);
        setDeaths(player, 0);
        setKills(player, 0);
        giveDeity(player, deity);
        setActiveEffects(player, new HashMap<>());
    }

    /**
     * If the given location is a shrine, returns the deity the shrine is for
     */
    public static String getDeityAtShrine(Location shrine) {
        Optional<ShrineSaveable> opSave = getPlugin().getShrineRegistry().fromLocation(shrine);
        if (opSave.isPresent()) {
            return opSave.get().getDeity();
        }
        return null;
    }

    public static ShrineSaveable getNearbyShrine(Location l) {
        ShrineSaveable shrine = null;
        for (ShrineSaveable w : getAllShrines()) {
            if (!w.getWorld().equals(l.getWorld().getName())) continue;
            Location l1 = DMisc.toLocation(w);
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
    public static UUID getOwnerOfShrine(Location shrine) {
        Optional<ShrineSaveable> opSave = getPlugin().getShrineRegistry().fromLocation(shrine);
        if (opSave.isPresent()) {
            return UUID.fromString(opSave.get().getOwnerId());
        }
        return null;
    }

    /*
     * If given location is shrine and given player is valid (same alliance),
     * register the player as a guest
     */
    @SuppressWarnings("unchecked")
    public static void addGuest(Location shrine, UUID guest) {
        if (!isFullParticipant(guest)) return;
        if (!getAllegiance(guest).equalsIgnoreCase(getAllegiance(getOwnerOfShrine(shrine)))) return;
        Optional<ShrineSaveable> saveable = getPlugin().getShrineRegistry().fromLocation(shrine);
        if (saveable.isPresent()) {
            saveable.get().addGuest(guest.toString());   
        }
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
    public static void removeGuest(Location shrine, UUID name) {
        if (!isFullParticipant(name)) return;
        Optional<ShrineSaveable> saveable = getPlugin().getShrineRegistry().fromLocation(shrine);
        if (saveable.isPresent()) {
            saveable.get().removeGuest(name.toString());
        }
    }

    /**
     * Check if a given player is allowed to warp to a given shrine
     *
     * @param shrine
     * @param player
     * @return
     */
    public static boolean isGuest(Location shrine, UUID player) {
        if (!isFullParticipant(player)) return false;
        Optional<ShrineSaveable> saveable = getPlugin().getShrineRegistry().fromLocation(shrine);
        if (saveable.isPresent()) {
            return saveable.get().getGuestIds().contains(player.toString());
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
    public static List<ShrineSaveable> getAccessibleShrines(UUID player) {
        if (!isFullParticipant(player)) return null;
        List<ShrineSaveable> toReturn = new ArrayList<>(getShrines(player));
        toReturn.addAll(getPlugin().getShrineRegistry().getFromDb().values().stream().filter(s -> s.getGuestIds().
                contains(player.toString())).collect(Collectors.toList()));
        return toReturn;
    }

    /**
     * Get all the players who are able to access a given shrine
     *
     * @param shrine
     * @return
     */
    public static List<UUID> getShrineGuestList(Location shrine) {
        Optional<ShrineSaveable> saveable = getPlugin().getShrineRegistry().fromLocation(shrine);
        if (saveable.isPresent()) {
            return saveable.get().getGuestIds().stream().map(UUID::fromString).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    /**
     * Remove a shrine that a player created
     *
     * @param shrine
     * @return the shrine that was removed
     */
    public static void removeShrine(ShrineSaveable shrine) {
        try {
            shrine.toLocation(getPlugin()).getBlock().setType(Material.AIR);
        } catch (NullPointerException ignored) {
        }
        getPlugin().getShrineRegistry().remove(shrine.getKey());
    }

    /**
     * Gets the shrine associated with the unique key (full name)
     *
     * @param shrinename
     * @return
     */
    public static ShrineSaveable getShrineByName(String shrinename) {
        Optional<ShrineSaveable> opSave = getPlugin().getShrineRegistry().getFromDb().values().stream().filter(s ->
                s.getName().equalsIgnoreCase(shrinename)).findAny();
        if (opSave.isPresent()) {
            return opSave.get();
        }
        return null;
    }

    /**
     * Renames the given shrine to the given value
     *
     * @param shrine
     * @param newname
     * @return if it worked
     */
    public static boolean renameShrine(Location shrine, String newname) {
        if (!getPlugin().getShrineRegistry().getFromDb().values().stream().anyMatch(s -> s.getName().
                equalsIgnoreCase(newname))) {
            Optional<ShrineSaveable> saveable = getPlugin().getShrineRegistry().fromLocation(shrine);
            if (saveable.isPresent()) {
                saveable.get().setName(newname);
                return true;
            }
        }
        return false;
    }

    /**
     * Returns name of shrine if set, or the player+deity by default
     *
     * @param shrine
     * @return
     */
    public static String getShrineName(Location shrine) {
        Optional<ShrineSaveable> opSave = getPlugin().getShrineRegistry().fromLocation(shrine);
        if (opSave.isPresent()) {
            return opSave.get().getName();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static void addShrine(UUID p, String deityname, String name, Location loc) {
        getPlugin().getShrineRegistry().newShrine(deityname, name, p.toString(), loc);
    }

    @SuppressWarnings("unchecked")
    public static List<ShrineSaveable> getShrines(UUID mojangId) {
        return getPlugin().getShrineRegistry().getFromDb().values().stream().filter(shrine -> shrine.getOwnerId().
                equals(mojangId.toString())).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public static ShrineSaveable getShrine(UUID p, String deityname) {
        Optional<ShrineSaveable> opSave = getShrines(p).stream().filter(s -> s.getDeity().equals(deityname)).findAny();
        if (opSave.isPresent()) {
            return opSave.get();
        }
        return null;
    }

    /**
     * Set a player's active effects
     *
     * @param player
     * @param data
     */
    public static void setActiveEffects(Player player, Map<String, Double> data) {
        getPlugin().getPlayerDataRegistry().fromPlayer(player).setActiveEffects(data);
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
    public static void addActiveEffect(Player p, String effectname, int lengthInSeconds) {
        PlayerDataSaveable save = getPlugin().getPlayerDataRegistry().fromPlayer(p);
        save.addEffect(effectname, System.currentTimeMillis() + lengthInSeconds * 1000);
    }

    public static void removeActiveEffect(Player p, String effectname) {
        PlayerDataSaveable save = getPlugin().getPlayerDataRegistry().fromPlayer(p);
        save.removeEffect(effectname);
    }

    public static void addActiveEffect(UUID p, String effectname, int lengthInSeconds) {
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            opSave.get().addEffect(effectname, System.currentTimeMillis() + lengthInSeconds * 1000);
        }
    }

    public static void removeActiveEffect(UUID p, String effectname) {
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            opSave.get().removeEffect(effectname);
        }
    }

    /**
     * Returns the effects on a player that are still active
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Double> getActiveEffects(UUID p) {
        Optional<PlayerDataSaveable> opSave = getPlugin().getPlayerDataRegistry().fromKey(p.toString());
        if (opSave.isPresent()) {
            Map<String, Double> toReturn = opSave.get().getActiveEffects().entrySet().stream().filter(e -> e.getValue()
                    > System.currentTimeMillis()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            opSave.get().setActiveEffects(toReturn);
            return toReturn;
        }
        return new HashMap<>();
    }

    /**
     * Returns the effects on a player that are still active
     */
    public static List<String> getActiveEffectsList(UUID p) {
        return new ArrayList<>(getActiveEffects(p).keySet());
    }

    /**
     * Gets a list of all shrines
     *
     * @return
     */
    public static List<ShrineSaveable> getAllShrines() {
        return new ArrayList<>(getPlugin().getShrineRegistry().getFromDb().values());
    }

    /**
     * Get the ids of all "full participants"
     *
     * @return
     */
    public static Set<String> getFullParticipants() {
        return getPlugin().getPlayerDataRegistry().getFromDb().keySet();
    }

    /*
     * WORLDGUARD SUPPORT START
     */
    public static boolean canWorldGuardPVP(Location l) {
        return Setting.ALLOW_PVP_EVERYWHERE || WorldGuardUtil.worldGuardEnabled() && WorldGuardUtil.canPVP(l);
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
        return !(player instanceof Player) || canWorldGuardPVP(location);
    }

    /**
     * Demigods damage handling
     */
    public static void damageDemigods(LivingEntity source, LivingEntity target, double amount, DamageCause cause) {
        if (target.getHealth() > 1) target.damage(1);
        if (target instanceof Player && isFullParticipant((Player) target)) {
            if (((Player) target).getGameMode() == GameMode.CREATIVE) return;
            if (!canTarget(target, target.getLocation())) return;
            double hp = target.getHealth();
            if (amount < 1) return;
            amount -= DDamage.armorReduction((Player) target);
            amount = DDamage.specialReduction((Player) target, amount);
            if (amount < 1) return;
            setHP(((Player) target), hp - amount);
            if (source instanceof Player) DFixes.setLastDamageBy(source, target, cause, amount);
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
        double hp = target.getHealth();
        if (amount < 1) return;
        amount -= DDamage.armorReduction(target);
        amount = DDamage.specialReduction(target, amount);
        if (amount < 1) return;
        setHP((target), hp - amount);
        DFixes.setLastDamage(target, cause, amount);
    }

    public static Plugin getPlugin(final String p) {
        try {
            return Iterators.find(Sets.newHashSet(getPlugin().getServer().getPluginManager().getPlugins()).iterator(), new Predicate<Plugin>() {
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
            getPlugin().getPlayerDataRegistry().fromPlayer(player).setTempData("temp_horse", true);
            horse.eject();
            horse.teleport(location);
            horse.setPassenger(player);
            getPlugin().getPlayerDataRegistry().fromPlayer(player).removeTempData("temp_horse");
        } else player.teleport(location);
    }
}
