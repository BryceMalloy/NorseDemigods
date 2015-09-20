package com.demigodsrpg.norsedemigods.deity.jotunn;

import com.demigodsrpg.norsedemigods.deity.Deity;
import com.demigodsrpg.norsedemigods.saveable.LocationSaveable;
import com.demigodsrpg.norsedemigods.util.DMiscUtil;
import com.demigodsrpg.norsedemigods.util.DSave;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class Jord implements Deity {
    /* Generalized things */
    private static final long serialVersionUID = 4917938727569988533L;
    private final int POISONCOST = 50;
    private final int PLANTCOST = 100;
    private final int RHEAULTIMATECOST = 5500;
    private final int RHEAULTIMATECOOLDOWNMAX = 500;
    private final int RHEAULTIMATECOOLDOWNMIN = 120;

    /* Specific to owner */
    private final UUID PLAYER;
    private final ArrayList<LocationSaveable> TREES;
    private boolean PLANT = false;
    private boolean POISON = false;
    private long PLANTTIME, POISONTIME, RHEAULTIMATETIME;
    private Material PLANTBIND = null;
    private Material DETONATEBIND = null;
    private Material POISONBIND = null;

    public Jord(UUID name) {
        PLAYER = name;
        TREES = new ArrayList<LocationSaveable>();
        PLANTTIME = System.currentTimeMillis();
        POISONTIME = System.currentTimeMillis();
        RHEAULTIMATETIME = System.currentTimeMillis();
    }

    @Override
    public String getName() {
        return "Jord";
    }

    @Override
    public UUID getPlayerId() {
        return PLAYER;
    }

    @Override
    public String getDefaultAlliance() {
        return "Jotunn";
    }

    @Override
    public void printInfo(Player p) {
        if (DMiscUtil.hasDeity(p, "Jord") && DMiscUtil.isFullParticipant(p)) {
            int devotion = DMiscUtil.getDevotion(p, getName());
            /*
             * Calculate special values first
			 */
            // poison
            int duration = (int) Math.ceil(2.4063 * Math.pow(devotion, 0.11)); // seconds
            if (duration < 1) duration = 1;
            int strength = (int) Math.ceil(1 * Math.pow(devotion, 0.09));
            // explosion
            float explosionsize = (float) (Math.ceil(3 * Math.pow(devotion, 0.09)));
            // ultimate
            int range = (int) (10.84198 * Math.pow(1.01926, DMiscUtil.getAscensions(p)));
            int ultimateduration = (int) (4.95778 * Math.pow(DMiscUtil.getAscensions(p), 0.459019));
            int t = (int) (RHEAULTIMATECOOLDOWNMAX - ((RHEAULTIMATECOOLDOWNMAX - RHEAULTIMATECOOLDOWNMIN) * ((double) DMiscUtil.getAscensions(p) / 100)));
            /*
			 * The printed text
			 */
            p.sendMessage("--" + ChatColor.GOLD + "Jord" + ChatColor.GRAY + "[" + devotion + "]");
            p.sendMessage(":Right click for a bonemeal effect.");
            p.sendMessage(":Poison a target player. " + ChatColor.GREEN + "/poison");
            p.sendMessage(ChatColor.YELLOW + "Costs " + POISONCOST + " Favor.");
            p.sendMessage("Poison power: " + strength + " for " + duration + " seconds.");
            if (((Jord) (DMiscUtil.getDeity(p, "Jord"))).POISONBIND != null)
                p.sendMessage(ChatColor.AQUA + "    Bound to " + (((Jord) (DMiscUtil.getDeity(p, "Jord"))).POISONBIND).name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to bind this skill to an item.");
            p.sendMessage(":Plant and detonate exploding trees. " + ChatColor.GREEN + "/plant, /detonate");
            p.sendMessage(ChatColor.YELLOW + "Costs " + PLANTCOST + " Favor.");
            p.sendMessage("Explosion radius: " + explosionsize + ". Maximum trees: " + (DMiscUtil.getAscensions(p) + 1));
            if (((Jord) (DMiscUtil.getDeity(p, "Jord"))).PLANTBIND != null)
                p.sendMessage(ChatColor.AQUA + "    Plant bound to " + (((Jord) (DMiscUtil.getDeity(p, "Jord"))).PLANTBIND).name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to bind plant to an item.");
            if (((Jord) (DMiscUtil.getDeity(p, "Jord"))).DETONATEBIND != null)
                p.sendMessage(ChatColor.AQUA + "    Detonate bound to " + (((Jord) (DMiscUtil.getDeity(p, "Jord"))).DETONATEBIND).name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to bind detonate to an item.");
            p.sendMessage(":Jord entangles nearby enemies, damaging them if they move.");
            p.sendMessage("Range: " + range + " for " + ultimateduration + " seconds. " + ChatColor.GREEN + "/entangle");
            p.sendMessage(ChatColor.YELLOW + "Costs " + RHEAULTIMATECOST + " Favor. Cooldown time: " + t + " seconds.");
            return;
        }
        p.sendMessage("--" + ChatColor.GOLD + "Jord");
        p.sendMessage("Passive: Right click for a bonemeal effect.");
        p.sendMessage("Active: Poison a target player. " + ChatColor.GREEN + "/poison");
        p.sendMessage(ChatColor.YELLOW + "Costs " + POISONCOST + " Favor. Can bind.");
        p.sendMessage("Active: Plant and detonate explosive trees.");
        p.sendMessage(ChatColor.GREEN + "/plant " + ChatColor.YELLOW + "Costs " + PLANTCOST + " Favor. Can bind.");
        p.sendMessage(ChatColor.GREEN + "/detonate " + ChatColor.YELLOW + "Can bind.");
        p.sendMessage("Ultimate: Jord entangles nearby enemies, damaging them if they move.");
        p.sendMessage(ChatColor.GREEN + "/entangle " + ChatColor.YELLOW + "Costs " + RHEAULTIMATECOST + " Favor. Has cooldown.");
        p.sendMessage(ChatColor.YELLOW + "Select item: vines");
    }

    @Override
    public void onEvent(Event ee) {
        if (ee instanceof PlayerInteractEvent) {
            PlayerInteractEvent e = (PlayerInteractEvent) ee;
            Player p = e.getPlayer();
            if (!DMiscUtil.isFullParticipant(p)) return;
            if (!DMiscUtil.hasDeity(p, "Jord")) return;
            if (!DMiscUtil.canTarget(p, p.getLocation())) return;
            if (POISON || ((POISONBIND != null) && (p.getItemInHand().getType() == POISONBIND))) {
                if (POISONTIME > System.currentTimeMillis()) return;
                if (DMiscUtil.getFavor(p) >= POISONCOST) {
                    if (poison(p)) {
                        DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) - POISONCOST);
                        int POISONDELAY = 1500;
                        POISONTIME = System.currentTimeMillis() + POISONDELAY;
                    }
                } else {
                    POISON = false;
                    p.sendMessage(ChatColor.YELLOW + "You don't have enough Favor to do that.");
                }
            }
            if (PLANT || ((PLANTBIND != null) && (p.getItemInHand().getType() == PLANTBIND))) {
                if (PLANTTIME > System.currentTimeMillis()) return;
                if (DMiscUtil.getFavor(p) >= PLANTCOST) {
                    if (plant(p)) {
                        DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) - PLANTCOST);
                        int PLANTDELAY = 2000;
                        PLANTTIME = System.currentTimeMillis() + PLANTDELAY;
                    }
                } else {
                    PLANT = false;
                    p.sendMessage(ChatColor.YELLOW + "You don't have enough Favor to do that.");
                }
            }
            if ((DETONATEBIND != null) && (p.getItemInHand().getType() == DETONATEBIND)) {
                detonate(p);
            }
            if (e.getClickedBlock() == null) return;
            Block b = e.getClickedBlock();
            if (b.getType() == Material.SAPLING) {
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    switch (b.getData()) {
                        case 0:
                            b.setData((byte) 1);
                            break;
                        case 1:
                            b.setData((byte) 2);
                            break;
                        case 2:
                            b.setData((byte) 3);
                            break;
                        case 3:
                            b.setData((byte) 0);
                            break;
                        default:
                            b.setData((byte) 0);
                    }
                } else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    byte Y = b.getData();
                    b.setType(Material.AIR);
                    switch (Y) {
                        case 0:
                            p.getWorld().generateTree(b.getLocation(), TreeType.TREE);
                            break;
                        case 1:
                            p.getWorld().generateTree(b.getLocation(), TreeType.REDWOOD);
                            break;
                        case 2:
                            p.getWorld().generateTree(b.getLocation(), TreeType.BIRCH);
                            break;
                        case 3:
                            p.getWorld().generateTree(b.getLocation(), TreeType.JUNGLE);
                            break;
                        default:
                            p.getWorld().generateTree(b.getLocation(), TreeType.TREE);
                    }
                    e.setCancelled(true);
                }
            } else if ((b.getType() == Material.CROPS) || (b.getType() == Material.PUMPKIN_STEM) || (b.getType() == Material.MELON_STEM)) {
                if (e.getAction() == Action.RIGHT_CLICK_BLOCK) b.setData((byte) 0x7);
            } else if (b.getType() == Material.GRASS) {
                if ((e.getAction() == Action.RIGHT_CLICK_BLOCK) && (e.getPlayer().getItemInHand().getType() == Material.AIR))
                    grow(b.getRelative(BlockFace.UP), 3);
            } else if (b.getType() == Material.DIRT) {
                if ((e.getAction() == Action.RIGHT_CLICK_BLOCK) && (e.getPlayer().getItemInHand().getType() == Material.AIR))
                    b.setType(Material.GRASS);
            }
        }
    }

    @Override
    public void onCommand(Player P, String str, String[] args, boolean bind) {
        final Player p = P;
        if (!DMiscUtil.isFullParticipant(p)) return;
        if (!DMiscUtil.hasDeity(p, "Jord")) return;
        if (str.equalsIgnoreCase("poison")) {
            if (bind) {
                if (POISONBIND == null) {
                    if (DMiscUtil.isBound(p, p.getItemInHand().getType()))
                        p.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                    if (p.getItemInHand().getType() == Material.AIR)
                        p.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                    else {
                        DMiscUtil.registerBind(p, p.getItemInHand().getType());
                        POISONBIND = p.getItemInHand().getType();
                        p.sendMessage(ChatColor.YELLOW + "Poison is now bound to " + p.getItemInHand().getType().name() + ".");
                    }
                } else {
                    DMiscUtil.removeBind(p, POISONBIND);
                    p.sendMessage(ChatColor.YELLOW + "Poison is no longer bound to " + POISONBIND.name() + ".");
                    POISONBIND = null;
                }
                return;
            }
            if (POISON) {
                POISON = false;
                p.sendMessage(ChatColor.YELLOW + "Poison is no longer active.");
            } else {
                POISON = true;
                p.sendMessage(ChatColor.YELLOW + "Poison is now active.");
            }
        } else if (str.equalsIgnoreCase("plant")) {
            if (bind) {
                if (PLANTBIND == null) {
                    if (DMiscUtil.isBound(p, p.getItemInHand().getType()))
                        p.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                    if (p.getItemInHand().getType() == Material.AIR)
                        p.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                    else {
                        DMiscUtil.registerBind(p, p.getItemInHand().getType());
                        PLANTBIND = p.getItemInHand().getType();
                        p.sendMessage(ChatColor.YELLOW + "Plant is now bound to " + p.getItemInHand().getType().name() + ".");
                    }
                } else {
                    DMiscUtil.removeBind(p, PLANTBIND);
                    p.sendMessage(ChatColor.YELLOW + "Plant is no longer bound to " + PLANTBIND.name() + ".");
                    PLANTBIND = null;
                }
                return;
            }
            if (PLANT) {
                PLANT = false;
                p.sendMessage(ChatColor.YELLOW + "Plant is no longer active.");
            } else {
                PLANT = true;
                p.sendMessage(ChatColor.YELLOW + "Plant is now active.");
            }

        } else if (str.equalsIgnoreCase("detonate")) {
            if (bind) {
                if (DETONATEBIND == null) {
                    if (DMiscUtil.isBound(p, p.getItemInHand().getType()))
                        p.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                    if (p.getItemInHand().getType() == Material.AIR)
                        p.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                    else {
                        DMiscUtil.registerBind(p, p.getItemInHand().getType());
                        DETONATEBIND = p.getItemInHand().getType();
                        p.sendMessage(ChatColor.YELLOW + "Detonate is now bound to " + p.getItemInHand().getType().name() + ".");
                    }
                } else {
                    DMiscUtil.removeBind(p, DETONATEBIND);
                    p.sendMessage(ChatColor.YELLOW + "Detonate is no longer bound to " + DETONATEBIND.name() + ".");
                    DETONATEBIND = null;
                }
                return;
            }
            detonate(p);
        } else if (str.equalsIgnoreCase("entangle")) {
            if (System.currentTimeMillis() < RHEAULTIMATETIME) {
                p.sendMessage(ChatColor.YELLOW + "You cannot use entangle again for " + ((((RHEAULTIMATETIME) / 1000) - (System.currentTimeMillis() / 1000))) / 60 + " minutes");
                p.sendMessage(ChatColor.YELLOW + "and " + ((((RHEAULTIMATETIME) / 1000) - (System.currentTimeMillis() / 1000)) % 60) + " seconds.");
                return;
            }
            if (DMiscUtil.getFavor(p) >= RHEAULTIMATECOST) {
                if (!DMiscUtil.canTarget(p, p.getLocation())) {
                    p.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
                    return;
                }
                int t = (int) (RHEAULTIMATECOOLDOWNMAX - ((RHEAULTIMATECOOLDOWNMAX - RHEAULTIMATECOOLDOWNMIN) * ((double) DMiscUtil.getAscensions(p) / 100)));
                int hit = entangle(p);
                if (hit > 0) {
                    p.sendMessage(ChatColor.YELLOW + "Jord has entangled " + hit + " enemies.");
                    DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) - RHEAULTIMATECOST);
                    RHEAULTIMATETIME = System.currentTimeMillis() + (t * 1000);
                } else p.sendMessage(ChatColor.YELLOW + "No targets found.");
            } else p.sendMessage(ChatColor.YELLOW + "Entangle requires " + RHEAULTIMATECOST + " Favor.");
        }
    }

    @Override
    public void onSyncTick(long timeSent) {

    }

    private boolean poison(Player p) {
        if (!DMiscUtil.canTarget(p, p.getLocation())) {
            p.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
            return false;
        }
        int devotion = DMiscUtil.getDevotion(p, getName());
        int duration = (int) Math.ceil(2.4063 * Math.pow(devotion, 0.11)); // seconds
        if (duration < 1) duration = 1;
        int strength = (int) Math.ceil(1 * Math.pow(devotion, 0.09));
        Player target = null;
        Block b = p.getTargetBlock((Set) null, 200);
        for (Player pl : b.getWorld().getPlayers()) {
            if (pl.getLocation().distance(b.getLocation()) < 4) {
                if (!DMiscUtil.areAllied(p, pl) && DMiscUtil.canTarget(pl, pl.getLocation())) {
                    target = pl;
                    break;
                }
            }
        }
        if (target != null && DMiscUtil.canTarget(target, target.getLocation())) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration * 20, strength));
            DMiscUtil.addActiveEffect(target.getUniqueId(), "Poison", duration);
            p.sendMessage(ChatColor.YELLOW + target.getName() + " has been poisoned for " + duration + " seconds.");
            target.sendMessage(ChatColor.RED + "You have been poisoned for " + duration + " seconds.");
            return true;
        } else {
            p.sendMessage(ChatColor.YELLOW + "No target found.");
            return false;
        }
    }

    private boolean plant(Player player) {
        if (!DMiscUtil.canTarget(player, player.getLocation())) {
            player.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
            return false;
        }
        Block b = player.getTargetBlock((Set) null, 200);
        if (b != null) {
            if (!DMiscUtil.canLocationPVP(b.getLocation())) {
                player.sendMessage(ChatColor.YELLOW + "That is a protected area.");
                return false;
            }
            if (TREES.size() == (DMiscUtil.getAscensions(player) + 1)) {
                player.sendMessage(ChatColor.YELLOW + "You have reached your maximum of " + (TREES.size()) + " trees.");
                return false;
            }
            if (player.getWorld().generateTree(b.getRelative(BlockFace.UP).getLocation(), TreeType.TREE)) {
                player.sendMessage(ChatColor.YELLOW + "Use /detonate to create an explosion at this tree.");
                TREES.add(DMiscUtil.toWriteLocation(b.getRelative(BlockFace.UP).getLocation()));
                return true;
            } else player.sendMessage(ChatColor.YELLOW + "A tree cannot be placed there.");
        } else player.sendMessage(ChatColor.YELLOW + "That is a protected zone or an invalid location.");
        return false;
    }

    private void detonate(Player player) {
        float explosionsize = (float) (Math.ceil(3 * Math.pow(DMiscUtil.getDevotion(player.getUniqueId(), getName()), 0.09)));
        if (TREES.size() > 0) {
            for (LocationSaveable w : TREES) {
                Location l = DMiscUtil.toLocation(w);
                if (l.getBlock().getType() == Material.LOG) {
                    removelogs(l);
                    l.getWorld().createExplosion(l, explosionsize);
                }
            }
            player.sendMessage(ChatColor.YELLOW + "Successfully detonated " + TREES.size() + " tree(s).");
            TREES.clear();
        } else player.sendMessage(ChatColor.YELLOW + "You have not placed an exploding tree.");
    }

    private void removelogs(Location l) {
        if (l.getBlock().getType() == Material.LOG) {
            l.getBlock().setType(Material.AIR);
            removelogs(l.getBlock().getRelative(BlockFace.UP).getLocation());
        }
    }

    private void grow(Block b, int run) {
        if (!DMiscUtil.canLocationPVP(b.getLocation())) return;
        if (((b.getType() == Material.AIR) && (b.getRelative(BlockFace.DOWN).getType() == Material.GRASS)) && (run > 0)) {
            switch ((int) (Math.random() * 50)) {
                case 0:
                case 1:
                case 2:
                    b.setType(Material.RED_ROSE);
                    break;
                case 3:
                case 6:
                case 10:
                    b.setType(Material.YELLOW_FLOWER);
                    break;
                case 8:
                    b.setType(Material.PUMPKIN);
                    break;
                case 12:
                case 13:
                case 14:
                    b.setType(Material.LONG_GRASS);
                    break;
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                case 20:
                case 21:
                case 22:
                case 23:
                case 24:
                case 25:
                    b.setType(Material.LONG_GRASS);
                    b.setData((byte) (0x1));
                    break;
            }
            grow(b.getRelative(BlockFace.NORTH), run - 1);
            grow(b.getRelative(BlockFace.EAST), run - 1);
            grow(b.getRelative(BlockFace.WEST), run - 1);
            grow(b.getRelative(BlockFace.SOUTH), run - 1);
            grow(b.getRelative(BlockFace.NORTH_EAST), run - 1);
            grow(b.getRelative(BlockFace.NORTH_WEST), run - 1);
            grow(b.getRelative(BlockFace.SOUTH_EAST), run - 1);
            grow(b.getRelative(BlockFace.SOUTH_WEST), run - 1);
        }
    }

    private int entangle(Player p) {
        int range = (int) (10.84198 * Math.pow(1.01926, DMiscUtil.getAscensions(p)));
        int duration = (int) (4.95778 * Math.pow(DMiscUtil.getAscensions(p), 0.459019));
        int count = 0;
        if (!DMiscUtil.canTarget(p, p.getLocation())) return count;

        for (LivingEntity le : p.getWorld().getLivingEntities()) {
            if (le.getLocation().distance(p.getLocation()) < range) {
                if (le instanceof Player) {
                    Player pl = (Player) le;
                    if (DMiscUtil.isFullParticipant(pl)) {
                        if (!DMiscUtil.areAllied(p, pl) && DMiscUtil.canTarget(le, le.getLocation())) {
                            trap(le, duration, p);
                            count++;
                        } else continue;
                    }
                }
                if (DMiscUtil.canTarget(le, le.getLocation())) {
                    count++;
                    trap(le, duration, p);
                }
            }
        }
        return count;
    }

    private void trap(final LivingEntity le, int durationseconds, final Player p) {
        if (le instanceof Player) {
            ((Player) le).sendMessage(ChatColor.YELLOW + "You have been entangled by Jord.");
        }
        le.setVelocity(new Vector(0, 0, 0));
        final Location originalloc = le.getLocation();
        Block corner1 = le.getLocation().getBlock().getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.SOUTH_EAST);
        Block corner2 = le.getLocation().getBlock().getRelative(BlockFace.SOUTH_WEST).getRelative(BlockFace.SOUTH_WEST);
        Block corner3 = le.getLocation().getBlock().getRelative(BlockFace.NORTH_EAST).getRelative(BlockFace.NORTH_EAST);
        Block corner4 = le.getLocation().getBlock().getRelative(BlockFace.NORTH_WEST).getRelative(BlockFace.NORTH_WEST);
        final ArrayList<Location> toreset = new ArrayList<Location>();
        for (int i = 0; i < 3; i++) {
            if ((corner1.getType() == Material.AIR) || (corner1.getType() == Material.WATER)) {
                toreset.add(corner1.getLocation());
                corner1.setType(Material.LOG);
            }
            corner1 = corner1.getRelative(BlockFace.UP);
        }
        for (int i = 0; i < 3; i++) {
            if ((corner2.getType() == Material.AIR) || (corner2.getType() == Material.WATER)) {
                toreset.add(corner2.getLocation());
                corner2.setType(Material.LOG);
            }
            corner2 = corner2.getRelative(BlockFace.UP);
        }
        for (int i = 0; i < 3; i++) {
            if ((corner3.getType() == Material.AIR) || (corner3.getType() == Material.WATER)) {
                toreset.add(corner3.getLocation());
                corner3.setType(Material.LOG);
            }
            corner3 = corner3.getRelative(BlockFace.UP);
        }
        for (int i = 0; i < 3; i++) {
            if ((corner4.getType() == Material.AIR) || (corner4.getType() == Material.WATER)) {
                toreset.add(corner4.getLocation());
                corner4.setType(Material.LOG);
            }
            corner4 = corner4.getRelative(BlockFace.UP);
        }
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                Block at = le.getWorld().getBlockAt(le.getEyeLocation().getBlockX() + x, le.getEyeLocation().getBlockY() + 2, le.getEyeLocation().getBlockZ() + z);
                if ((at.getType() == Material.AIR) || (at.getType() == Material.WATER)) {
                    toreset.add(at.getLocation());
                    at.setType(Material.LEAVES);
                }
            }
        }
        for (int x = -2; x <= 2; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    if ((x == 2) || (z == 2)) {
                        Block at = le.getWorld().getBlockAt(le.getEyeLocation().getBlockX() + x, le.getEyeLocation().getBlockY() + y, le.getEyeLocation().getBlockZ() + z);
                        if ((at.getType() == Material.AIR) || (at.getType() == Material.WATER)) {
                            toreset.add(at.getLocation());
                            at.setType(Material.VINE);
                        }
                    }
                }
            }
        }
        for (int i = 0; i < durationseconds * 20; i += 10) {
            DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    if (le.isDead() && le instanceof Player) {
                        DSave.saveData((Player) le, "temp_trap_died", true);
                        return;
                    } else if (le.getLocation().distance(originalloc) > 0.5) {
                        if (le instanceof Player) {
                            if (DSave.hasData((Player) le, "temp_trap_died")) return;
                            ((Player) le).sendMessage(ChatColor.YELLOW + "You take damage from moving while entangled!");
                        }
                        DMiscUtil.damageDemigods(p, le, 5, DamageCause.ENTITY_ATTACK);
                    }
                    if (le instanceof Player) DMiscUtil.horseTeleport((Player) le, originalloc);
                    else le.teleport(originalloc);
                }
            }, i);
        }
        DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
            @Override
            public void run() {
                for (Location l : toreset)
                    l.getBlock().setType(Material.AIR);

                if (le instanceof Player) {
                    if (DSave.hasData((Player) le, "temp_trap_died")) DSave.removeData((Player) le, "temp_trap_died");
                }
            }
        }, durationseconds * 20);
    }

    @Override
    public boolean canTribute() {
        return true;
    }
}
