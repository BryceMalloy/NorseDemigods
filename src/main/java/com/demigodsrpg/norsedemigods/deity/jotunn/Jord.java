package com.demigodsrpg.norsedemigods.deity.jotunn;

import com.demigodsrpg.norsedemigods.DMisc;
import com.demigodsrpg.norsedemigods.Deity;
import com.demigodsrpg.norsedemigods.deity.AD;
import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Sapling;
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
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Jord implements Deity {
    /* Generalized things */
    private final int POISONCOST = 50;
    private final int PLANTCOST = 100;
    private final int RHEAULTIMATECOST = 5500;
    private final int RHEAULTIMATECOOLDOWNMAX = 500;
    private final int RHEAULTIMATECOOLDOWNMIN = 120;

    @Override
    public String getName() {
        return "Jord";
    }

    @Override
    public String getDefaultAlliance() {
        return "Jotunn";
    }

    @Override
    public void printInfo(Player p) {
        if (DMisc.hasDeity(p, "Jord") && DMisc.isFullParticipant(p)) {
            int devotion = DMisc.getDevotion(p, getName());
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
            int range = (int) (10.84198 * Math.pow(1.01926, DMisc.getAscensions(p)));
            int ultimateduration = (int) (4.95778 * Math.pow(DMisc.getAscensions(p), 0.459019));
            int t = (int) (RHEAULTIMATECOOLDOWNMAX - ((RHEAULTIMATECOOLDOWNMAX - RHEAULTIMATECOOLDOWNMIN) * ((double) DMisc.getAscensions(p) / 100)));
            /*
             * The printed text
             */
            p.sendMessage("--" + ChatColor.GOLD + "Jord" + ChatColor.GRAY + "[" + devotion + "]");
            p.sendMessage(":Right click for a bonemeal effect.");
            p.sendMessage(":Poison a target player. " + ChatColor.GREEN + "/poison");
            p.sendMessage(ChatColor.YELLOW + "Costs " + POISONCOST + " Favor.");
            p.sendMessage("Poison power: " + strength + " for " + duration + " seconds.");
            PlayerDataSaveable saveable = getBackend().getPlayerDataRegistry().fromPlayer(p);
            Optional<Material> poisonBind = saveable.getBind("poison");
            if (poisonBind.isPresent())
                p.sendMessage(ChatColor.AQUA + "    Bound to " + poisonBind.get().name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to bind this skill to an item.");
            p.sendMessage(":Plant and detonate exploding trees. " + ChatColor.GREEN + "/plant, /detonate");
            p.sendMessage(ChatColor.YELLOW + "Costs " + PLANTCOST + " Favor.");
            p.sendMessage("Explosion radius: " + explosionsize + ". Maximum trees: " + (DMisc.getAscensions(p) + 1));
            Optional<Material> plantBind = saveable.getBind("plant");
            if (plantBind.isPresent())
                p.sendMessage(ChatColor.AQUA + "    Bound to " + plantBind.get().name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to bind plant to an item.");
            Optional<Material> detonateBind = saveable.getBind("detonate");
            if (detonateBind.isPresent())
                p.sendMessage(ChatColor.AQUA + "    Bound to " + detonateBind.get().name());
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
            if (!DMisc.isFullParticipant(p)) return;
            if (!DMisc.hasDeity(p, "Jord")) return;
            if (!DMisc.canTarget(p, p.getLocation())) return;
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            if (save.getAbilityData("poison", AD.ACTIVE, false) || save.getBind("poison").isPresent() &&
                    p.getItemInHand().getType() == save.getBind("poison").get()) {
                if (save.getAbilityData("poison", AD.TIME, (double) System.currentTimeMillis()) > System.currentTimeMillis())
                    return;
                if (DMisc.getFavor(p) >= POISONCOST) {
                    if (poison(p)) {
                        DMisc.setFavor(p, DMisc.getFavor(p) - POISONCOST);
                        int POISONDELAY = 1500;
                        save.setAbilityData("poison", AD.TIME, System.currentTimeMillis() + POISONDELAY);
                    }
                } else {
                    save.setAbilityData("poison", AD.ACTIVE, false);
                    p.sendMessage(ChatColor.YELLOW + "You don't have enough Favor to do that.");
                }
            }
            if (save.getAbilityData("plant", AD.ACTIVE, false) || save.getBind("plant").isPresent() &&
                    p.getItemInHand().getType() == save.getBind("plant").get()) {
                if (save.getAbilityData("plant", AD.TIME, (double) System.currentTimeMillis()) > System.currentTimeMillis())
                    return;
                if (DMisc.getFavor(p) >= PLANTCOST) {
                    if (plant(p, save)) {
                        DMisc.setFavor(p, DMisc.getFavor(p) - PLANTCOST);
                        int PLANTDELAY = 2000;
                        save.setAbilityData("plant", AD.TIME, System.currentTimeMillis() + PLANTDELAY);
                    }
                } else {
                    save.setAbilityData("plant", AD.ACTIVE, false);
                    p.sendMessage(ChatColor.YELLOW + "You don't have enough Favor to do that.");
                }
            }
            if (save.getBind("detonate").isPresent() && p.getItemInHand().getType() == save.getBind("detonate").get()) {
                if (e.getClickedBlock() == null) return;
                Block b = e.getClickedBlock();
                if (b.getType().name().contains("SAPLING")) {
                    if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        Sapling sap = (Sapling) b;
                        switch (sap.getStage()) {
                            case 0:
                                sap.setStage(1);
                                break;
                            case 1:
                                sap.setStage(2);
                                break;
                            case 2:
                                sap.setStage(3);
                                break;
                            case 3:
                            default:
                                sap.setStage(0);
                        }
                    } else if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                        byte Y = b.getData();
                        b.setType(Material.AIR);
                        switch (Y) {
                            case 1:
                                p.getWorld().generateTree(b.getLocation(), TreeType.REDWOOD);
                                break;
                            case 2:
                                p.getWorld().generateTree(b.getLocation(), TreeType.BIRCH);
                                break;
                            case 3:
                                p.getWorld().generateTree(b.getLocation(), TreeType.JUNGLE);
                                break;
                            case 0:
                            default:
                                p.getWorld().generateTree(b.getLocation(), TreeType.TREE);
                        }
                        e.setCancelled(true);
                    }
                } else if ((b.getType() == Material.WHEAT) || (b.getType() == Material.PUMPKIN_STEM) || (b.getType() == Material.MELON_STEM)) {
                    //if (e.getAction() == Action.RIGHT_CLICK_BLOCK) b.setData((byte) 0x7);
                } else if (b.getType() == Material.GRASS) {
                    if ((e.getAction() == Action.RIGHT_CLICK_BLOCK) && (e.getPlayer().getItemInHand().getType() == Material.AIR))
                        grow(p, b.getRelative(BlockFace.UP), 3);
                } else if (b.getType() == Material.DIRT) {
                    if ((e.getAction() == Action.RIGHT_CLICK_BLOCK) && (e.getPlayer().getItemInHand().getType() == Material.AIR))
                        b.setType(Material.GRASS);
                }
            }
        }
    }

    @Override
    public void onCommand(Player P, String str, String[] args, boolean bind) {
        if (!DMisc.isFullParticipant(P)) return;
        if (!DMisc.hasDeity(P, "Jord")) return;
        PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(P);
        if (str.equalsIgnoreCase("poison")) {
            if (bind) {
                if (!save.getBind("poison").isPresent()) {
                    if (DMisc.isBound(P, P.getItemInHand().getType()))
                        P.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                    if (P.getItemInHand().getType() == Material.AIR)
                        P.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                    else {
                        save.setBind("ability", P.getItemInHand().getType());
                        P.sendMessage(ChatColor.YELLOW + "Poison is now bound to " + P.getItemInHand().getType().name() + ".");
                    }
                } else {
                    P.sendMessage(ChatColor.YELLOW + "Poison is no longer bound to " + save.getBind("poison").get().name() + ".");
                    save.removeBind("poison");
                }
                return;
            }
            if (save.getAbilityData("poison", AD.ACTIVE, false)) {
                save.setAbilityData("poison", AD.ACTIVE, false);
                P.sendMessage(ChatColor.YELLOW + "Poison is no longer active.");
            } else {
                save.setAbilityData("poison", AD.ACTIVE, true);
                P.sendMessage(ChatColor.YELLOW + "Poison is now active.");
            }
        } else if (str.equalsIgnoreCase("plant")) {
            if (bind) {
                if (!save.getBind("plant").isPresent()) {
                    if (DMisc.isBound(P, P.getItemInHand().getType()))
                        P.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                    if (P.getItemInHand().getType() == Material.AIR)
                        P.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                    else {
                        save.setBind("plant", P.getItemInHand().getType());
                        P.sendMessage(ChatColor.YELLOW + "Plant is now bound to " + P.getItemInHand().getType().name() + ".");
                    }
                } else {
                    P.sendMessage(ChatColor.YELLOW + "Plant is no longer bound to " + save.getBind("plant").get().name() + ".");
                    save.removeBind("plant");
                }
                return;
            }
            if (save.getAbilityData("plant", AD.ACTIVE, false)) {
                save.setAbilityData("plant", AD.ACTIVE, false);
                P.sendMessage(ChatColor.YELLOW + "Plant is no longer active.");
            } else {
                save.setAbilityData("plant", AD.ACTIVE, true);
                P.sendMessage(ChatColor.YELLOW + "Plant is now active.");
            }

        } else if (str.equalsIgnoreCase("detonate")) {
            if (bind) {
                if (!save.getBind("detonate").isPresent()) {
                    if (DMisc.isBound(P, P.getItemInHand().getType()))
                        P.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                    if (P.getItemInHand().getType() == Material.AIR)
                        P.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                    else {
                        save.setBind("detonate", P.getItemInHand().getType());
                        P.sendMessage(ChatColor.YELLOW + "Detonate is now bound to " + P.getItemInHand().getType().name() + ".");
                    }
                } else {
                    P.sendMessage(ChatColor.YELLOW + "Detonate is no longer bound to " + save.getBind("detonate").get().name() + ".");
                    save.removeBind("detonate");
                }
                return;
            }
            detonate(P, save);
        } else if (str.equalsIgnoreCase("entangle")) {
            double time = save.getAbilityData("entangle", AD.TIME, (double) System.currentTimeMillis());
            if (System.currentTimeMillis() < time) {
                P.sendMessage(ChatColor.YELLOW + "You cannot use entangle again for " + ((((time) / 1000) - (System.currentTimeMillis() / 1000))) / 60 + " minutes");
                P.sendMessage(ChatColor.YELLOW + "and " + ((((time) / 1000) - (System.currentTimeMillis() / 1000)) % 60) + " seconds.");
                return;
            }
            if (DMisc.getFavor(P) >= RHEAULTIMATECOST) {
                if (!DMisc.canTarget(P, P.getLocation())) {
                    P.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
                    return;
                }
                int t = (int) (RHEAULTIMATECOOLDOWNMAX - ((RHEAULTIMATECOOLDOWNMAX - RHEAULTIMATECOOLDOWNMIN) * ((double) DMisc.getAscensions(P) / 100)));
                int hit = entangle(P);
                if (hit > 0) {
                    P.sendMessage(ChatColor.YELLOW + "Jord has entangled " + hit + " enemies.");
                    DMisc.setFavor(P, DMisc.getFavor(P) - RHEAULTIMATECOST);
                    save.setAbilityData("entangle", AD.TIME, System.currentTimeMillis() + (t * 1000));
                } else P.sendMessage(ChatColor.YELLOW + "No targets found.");
            } else P.sendMessage(ChatColor.YELLOW + "Entangle requires " + RHEAULTIMATECOST + " Favor.");
        }
    }

    @Override
    public void onSyncTick(long timeSent) {

    }

    private boolean poison(Player p) {
        if (!DMisc.canTarget(p, p.getLocation())) {
            p.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
            return false;
        }
        int devotion = DMisc.getDevotion(p, getName());
        int duration = (int) Math.ceil(2.4063 * Math.pow(devotion, 0.11)); // seconds
        if (duration < 1) duration = 1;
        int strength = (int) Math.ceil(1 * Math.pow(devotion, 0.09));
        Player target = null;
        Block b = p.getTargetBlock((Set) null, 200);
        for (Player pl : b.getWorld().getPlayers()) {
            if (pl.getLocation().distance(b.getLocation()) < 4) {
                if (!DMisc.areAllied(p, pl) && DMisc.canTarget(pl, pl.getLocation())) {
                    target = pl;
                    break;
                }
            }
        }
        if (target != null && DMisc.canTarget(target, target.getLocation())) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, duration * 20, strength));
            DMisc.addActiveEffect(target.getUniqueId(), "Poison", duration);
            p.sendMessage(ChatColor.YELLOW + target.getName() + " has been poisoned for " + duration + " seconds.");
            target.sendMessage(ChatColor.RED + "You have been poisoned for " + duration + " seconds.");
            return true;
        } else {
            p.sendMessage(ChatColor.YELLOW + "No target found.");
            return false;
        }
    }

    private boolean plant(Player player, PlayerDataSaveable save) {
        if (!DMisc.canTarget(player, player.getLocation())) {
            player.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
            return false;
        }
        Block b = player.getTargetBlock((Set) null, 200);
        List<String> TREES = save.getAbilityData("plant", "trees", new ArrayList<>());
        if (b != null) {
            if (!DMisc.canLocationPVP(player, b.getLocation())) {
                player.sendMessage(ChatColor.YELLOW + "That is a protected area.");
                return false;
            }
            if (TREES.size() == (DMisc.getAscensions(player) + 1)) {
                player.sendMessage(ChatColor.YELLOW + "You have reached your maximum of " + (TREES.size()) + " trees.");
                return false;
            }
            if (player.getWorld().generateTree(b.getRelative(BlockFace.UP).getLocation(), TreeType.TREE)) {
                player.sendMessage(ChatColor.YELLOW + "Use /detonate to create an explosion at this tree.");
                TREES.add(getBackend().getLocationKey((b.getRelative(BlockFace.UP).getLocation())));
                save.setAbilityData("plant", "trees", TREES);
                return true;
            } else player.sendMessage(ChatColor.YELLOW + "A tree cannot be placed there.");
        } else player.sendMessage(ChatColor.YELLOW + "That is a protected zone or an invalid location.");
        return false;
    }

    private void detonate(Player player, PlayerDataSaveable save) {
        float explosionsize = (float) (Math.ceil(3 * Math.pow(DMisc.getDevotion(player.getUniqueId(), getName()), 0.09)));
        List<String> TREES = save.getAbilityData("plant", "trees", new ArrayList<>());
        if (TREES.size() > 0) {
            for (String w : TREES) {
                Location l = getBackend().getLocationFromKey(w);
                if (l.getBlock().getType().name().contains("LOG")) {
                    removelogs(l);
                    l.getWorld().createExplosion(l, explosionsize);
                }
            }
            player.sendMessage(ChatColor.YELLOW + "Successfully detonated " + TREES.size() + " tree(s).");
            TREES.clear();
        } else player.sendMessage(ChatColor.YELLOW + "You have not placed an exploding tree.");
    }

    private void removelogs(Location l) {
        if (l.getBlock().getType().name().contains("LOG")) {
            l.getBlock().setType(Material.AIR);
            removelogs(l.getBlock().getRelative(BlockFace.UP).getLocation());
        }
    }

    private void grow(Player c, Block b, int run) {
        if (!DMisc.canLocationPVP(c, b.getLocation())) return;
        if (((b.getType() == Material.AIR) && (b.getRelative(BlockFace.DOWN).getType() == Material.GRASS)) && (run > 0)) {
            switch ((int) (Math.random() * 50)) {
                case 0:
                case 1:
                case 2:
                    b.setType(Material.ROSE_BUSH);
                    break;
                case 3:
                case 6:
                case 10:
                    b.setType(Material.CORNFLOWER);
                    break;
                case 8:
                    b.setType(Material.PUMPKIN);
                    break;
                case 12:
                case 13:
                case 14:
                    b.setType(Material.TALL_GRASS);
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
                    b.setType(Material.TALL_GRASS);
                    //b.setData((byte) (0x1));
                    break;
            }
            grow(c, b.getRelative(BlockFace.NORTH), run - 1);
            grow(c, b.getRelative(BlockFace.EAST), run - 1);
            grow(c, b.getRelative(BlockFace.WEST), run - 1);
            grow(c, b.getRelative(BlockFace.SOUTH), run - 1);
            grow(c, b.getRelative(BlockFace.NORTH_EAST), run - 1);
            grow(c, b.getRelative(BlockFace.NORTH_WEST), run - 1);
            grow(c, b.getRelative(BlockFace.SOUTH_EAST), run - 1);
            grow(c, b.getRelative(BlockFace.SOUTH_WEST), run - 1);
        }
    }

    private int entangle(Player p) {
        int range = (int) (10.84198 * Math.pow(1.01926, DMisc.getAscensions(p)));
        int duration = (int) (4.95778 * Math.pow(DMisc.getAscensions(p), 0.459019));
        int count = 0;
        if (!DMisc.canTarget(p, p.getLocation())) return count;

        for (LivingEntity le : p.getWorld().getLivingEntities()) {
            if (le.getLocation().distance(p.getLocation()) < range) {
                if (le instanceof Player) {
                    Player pl = (Player) le;
                    if (DMisc.isFullParticipant(pl)) {
                        if (!DMisc.areAllied(p, pl) && DMisc.canTarget(le, le.getLocation())) {
                            trap(le, duration, p);
                            count++;
                        } else continue;
                    }
                }
                if (DMisc.canTarget(le, le.getLocation())) {
                    count++;
                    trap(le, duration, p);
                }
            }
        }
        return count;
    }

    private void trap(final LivingEntity le, int durationseconds, final Player p) {
        if (le instanceof Player) {
            le.sendMessage(ChatColor.YELLOW + "You have been entangled by Jord.");
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
                corner1.setType(Material.OAK_LOG);
            }
            corner1 = corner1.getRelative(BlockFace.UP);
        }
        for (int i = 0; i < 3; i++) {
            if ((corner2.getType() == Material.AIR) || (corner2.getType() == Material.WATER)) {
                toreset.add(corner2.getLocation());
                corner2.setType(Material.OAK_LOG);
            }
            corner2 = corner2.getRelative(BlockFace.UP);
        }
        for (int i = 0; i < 3; i++) {
            if ((corner3.getType() == Material.AIR) || (corner3.getType() == Material.WATER)) {
                toreset.add(corner3.getLocation());
                corner3.setType(Material.OAK_LOG);
            }
            corner3 = corner3.getRelative(BlockFace.UP);
        }
        for (int i = 0; i < 3; i++) {
            if ((corner4.getType() == Material.AIR) || (corner4.getType() == Material.WATER)) {
                toreset.add(corner4.getLocation());
                corner4.setType(Material.OAK_LOG);
            }
            corner4 = corner4.getRelative(BlockFace.UP);
        }
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                Block at = le.getWorld().getBlockAt(le.getEyeLocation().getBlockX() + x, le.getEyeLocation().getBlockY() + 2, le.getEyeLocation().getBlockZ() + z);
                if ((at.getType() == Material.AIR) || (at.getType() == Material.WATER)) {
                    toreset.add(at.getLocation());
                    at.setType(Material.OAK_LEAVES);
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
            DMisc.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMisc.getPlugin(), () -> {
                if (le.isDead() && le instanceof Player) {
                    PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer((Player) le);
                    save.setTempData("temp_trap_died", true);
                    return;
                } else if (le.getLocation().distance(originalloc) > 0.5) {
                    if (le instanceof Player) {
                        PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer((Player) le);
                        if (save.getTempStatus("temp_trap_died")) return;
                        le.sendMessage(ChatColor.YELLOW + "You take damage from moving while entangled!");
                    }
                    DMisc.damageDemigods(p, le, 5, DamageCause.ENTITY_ATTACK);
                }
                if (le instanceof Player) DMisc.horseTeleport((Player) le, originalloc);
                else le.teleport(originalloc);
            }, i);
        }
        DMisc.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMisc.getPlugin(), () -> {
            for (Location l : toreset)
                l.getBlock().setType(Material.AIR);

            if (le instanceof Player) {
                PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer((Player) le);
                save.removeTempData("temp_trap_died");
            }
        }, durationseconds * 20);
    }

    @Override
    public boolean canTribute() {
        return true;
    }
}
