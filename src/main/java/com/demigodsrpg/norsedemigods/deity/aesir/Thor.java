package com.demigodsrpg.norsedemigods.deity.aesir;

import com.demigodsrpg.norsedemigods.DMisc;
import com.demigodsrpg.norsedemigods.Deity;
import com.demigodsrpg.norsedemigods.deity.AD;
import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/*
 * Affected by level:
 * Fall damage reduction
 * Shove distance/range
 * Ultimate cooldown
 */

public class Thor implements Deity {
    private static final int SHOVECOST = 170;
    private static final int SHOVEDELAY = 1500; // milliseconds
    private static final int LIGHTNINGCOST = 140;
    private static final int LIGHTNINGDELAY = 1000; // milliseconds
    private static final int ZEUSULTIMATECOST = 3700;
    private static final int ZEUSULTIMATECOOLDOWNMAX = 600; // seconds
    private static final int ZEUSULTIMATECOOLDOWNMIN = 60;

    @Override
    public String getDefaultAlliance() {
        return "AEsir";
    }

    @Override
    public void printInfo(Player p) {
        if (DMisc.hasDeity(p, "Thor") && DMisc.isFullParticipant(p)) {
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            int devotion = DMisc.getDevotion(p, getName());
            /*
             * Calculate special values first
             */
            // shove
            int targets = (int) Math.ceil(1.561 * Math.pow(devotion, 0.128424));
            double multiply = 0.1753 * Math.pow(devotion, 0.322917);
            // ultimate
            int t = (int) (ZEUSULTIMATECOOLDOWNMAX - ((ZEUSULTIMATECOOLDOWNMAX - ZEUSULTIMATECOOLDOWNMIN) * ((double) DMisc.getAscensions(p) / 100)));
            /*
             * The printed text
             */
            p.sendMessage("--" + ChatColor.GOLD + "Thor" + ChatColor.GRAY + " [" + devotion + "]");
            p.sendMessage(":Immune to fall damage.");
            p.sendMessage(":Strike lightning at a target location. " + ChatColor.GREEN + "/lightning");
            p.sendMessage(ChatColor.YELLOW + "Costs " + LIGHTNINGCOST + " Favor.");
            if (save.getBind("lightning").isPresent())
                p.sendMessage(ChatColor.AQUA + "    Bound to " + save.getBind("lightning").get().name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to designate an item as Thor's hammer.");
            p.sendMessage(":Use the force of Thor's hammer to knock back enemies. " + ChatColor.GREEN + "/slam");
            p.sendMessage(ChatColor.YELLOW + "Costs " + SHOVECOST + " Favor.");
            p.sendMessage("Affects up to " + targets + " targets with power " + (int) (Math.round(multiply * 10)) + ".");
            if (save.getBind("slam").isPresent())
                p.sendMessage(ChatColor.AQUA + "    Bound to " + save.getBind("shove").get().name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to bind this skill to an item.");
            return;
        }
        p.sendMessage("--" + ChatColor.GOLD + "Thor");
        p.sendMessage("Passive: Immune to fall damage.");
        p.sendMessage("Active: Strike lightning at a target location. " + ChatColor.GREEN + "/lightning");
        p.sendMessage(ChatColor.YELLOW + "Costs " + LIGHTNINGCOST + " Favor. Can bind.");
        p.sendMessage("Active: Use the force of Thor's hammer to knock back enemies. " + ChatColor.GREEN + "/slam");
        p.sendMessage(ChatColor.YELLOW + "Costs " + SHOVECOST + " Favor. Can bind.");
        p.sendMessage(ChatColor.YELLOW + "Select item: iron ingot");
    }

    @Override
    public String getName() {
        return "Thor";
    }

    @Override
    public void onEvent(Event ee) {
        if (ee instanceof PlayerInteractEvent) {
            PlayerInteractEvent e = (PlayerInteractEvent) ee;
            Player p = e.getPlayer();
            if (!DMisc.hasDeity(p, "Thor") || !DMisc.isFullParticipant(p)) return;
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            if (save.getAbilityData("slam", AD.ACTIVE, false) || ((p.getItemInHand() != null) && save.getBind("slam").isPresent() && (p.getItemInHand().getType() == save.getBind("slam").get()))) {
                if (save.getAbilityData("slam", AD.TIME, (double) System.currentTimeMillis()) > System.currentTimeMillis())
                    return;
                save.setAbilityData("slam", AD.TIME, System.currentTimeMillis() + SHOVEDELAY);
                if (DMisc.getFavor(p) >= SHOVECOST) {
                    if (shove(p)) {
                        DMisc.setFavor(p, DMisc.getFavor(p) - SHOVECOST);
                    }
                    return;
                } else {
                    p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor.");
                    save.setAbilityData("slam", AD.ACTIVE, false);
                }
            }
            if (save.getAbilityData("lightning", AD.ACTIVE, false) || ((p.getItemInHand() != null) && save.getBind("lightning").isPresent() && (p.getItemInHand().getType() == save.getBind("lightning").get()))) {
                if (save.getAbilityData("lightning", AD.TIME, (double) System.currentTimeMillis()) > System.currentTimeMillis())
                    return;
                save.setAbilityData("lightning", AD.TIME, System.currentTimeMillis() + LIGHTNINGDELAY);
                if (DMisc.getFavor(p) >= LIGHTNINGCOST) {
                    if (lightning(p, e.getClickedBlock())) {
                        DMisc.setFavor(p, DMisc.getFavor(p) - LIGHTNINGCOST);
                    }
                } else {
                    p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor.");
                    save.setAbilityData("lightning", AD.ACTIVE, false);
                }
            }
        }
    }

    /*
     * ---------------
     * Commands
     * ---------------
     */
    @Override
    public void onCommand(Player P, String str, String[] args, boolean bind) {
        if (!DMisc.hasDeity(P, "Thor")) return;
        PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(P);
        if (str.equalsIgnoreCase("lightning")) {
            if (bind) {
                if (!save.getBind("lightning").isPresent()) {
                    if (DMisc.isBound(P, P.getItemInHand().getType()))
                        P.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                    if (P.getItemInHand().getType() == Material.AIR)
                        P.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                    else {
                        save.setBind("lightning", P.getItemInHand().getType());
                        P.sendMessage(ChatColor.YELLOW + "Lightning is now bound to " + P.getItemInHand().getType().name() + ".");
                    }
                } else {
                    P.sendMessage(ChatColor.YELLOW + "Lightning is no longer bound to " + save.getBind("lightning").get().name() + ".");
                    save.removeBind("lightning");
                }
                return;
            }
            if (save.getAbilityData("lightning", AD.ACTIVE, false)) {
                save.setAbilityData("lightning", AD.ACTIVE, false);
                P.sendMessage(ChatColor.YELLOW + "Lightning is no longer active.");
            } else {
                save.setAbilityData("lightning", AD.ACTIVE, true);
                P.sendMessage(ChatColor.YELLOW + "Lightning is now active.");
            }
        } else if (str.equalsIgnoreCase("slam")) {
            if (bind) {
                if (!save.getBind("slam").isPresent()) {
                    if (DMisc.isBound(P, P.getItemInHand().getType()))
                        P.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                    if (P.getItemInHand().getType() == Material.AIR)
                        P.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                    else {
                        save.setBind("slam", P.getItemInHand().getType());
                        P.sendMessage(ChatColor.YELLOW + "Slam is now bound to " + P.getItemInHand().getType().name() + ".");
                    }
                } else {
                    P.sendMessage(ChatColor.YELLOW + "Slam is no longer bound to " + save.getBind("slam").get().name() + ".");
                    save.removeBind("slam");
                }
                return;
            }
            if (save.getAbilityData("slam", AD.ACTIVE, false)) {
                save.setAbilityData("slam", AD.ACTIVE, false);
                P.sendMessage(ChatColor.YELLOW + "Slam is no longer active.");
            } else {
                save.setAbilityData("slam", AD.ACTIVE, true);
                P.sendMessage(ChatColor.YELLOW + "Slam is now active.");
            }
        }
    }

    /*
     * ---------------
     * Helper methods
     * ---------------
     */
    private boolean shove(Player p) {
        if (!DMisc.canTarget(p, p.getLocation())) {
            p.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
            return false;
        }
        ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
        int devotion = DMisc.getDevotion(p, getName());
        int targets = (int) Math.ceil(1.561 * Math.pow(devotion, 0.128424));
        double multiply = 0.1753 * Math.pow(devotion, 0.322917);
        List<Block> bL = p.getLineOfSight((Set) null, 10);
        for (Block b : bL) {
            for (LivingEntity le : p.getWorld().getLivingEntities()) {
                if (targets == hit.size()) break;
                if (le instanceof Player) {
                    if (DMisc.areAllied(p, (Player) le)) continue;
                }
                if ((le.getLocation().distance(b.getLocation()) <= 5) && !hit.contains(le))
                    if (DMisc.canTarget(le, le.getLocation())) hit.add(le);
            }
        }
        if (hit.size() > 0) {
            for (LivingEntity le : hit) {
                Vector v = p.getLocation().toVector();
                Vector victor = le.getLocation().toVector().subtract(v);
                victor.multiply(multiply);
                le.setVelocity(victor);
            }
        } else {
            return false;
        }
        return true;
    }

    private boolean lightning(Player p, Block b) {
        if (!DMisc.canTarget(p, b != null ? b.getLocation() : p.getLocation())) {
            p.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
            return false;
        }
        try {
            Location target = b.getLocation();
            p.getWorld().strikeLightningEffect(target);
            if (p.getLocation().distance(target) > 2) {
                if (!p.getWorld().equals(target.getWorld())) return false;
                if (!DMisc.canLocationPVP(p, target)) return false;
                for (Entity e : b.getLocation().getChunk().getEntities()) {
                    if (e.getLocation().distance(target) > 1) continue;
                    if (e instanceof LivingEntity) {
                        LivingEntity le = (LivingEntity) e;
                        if (le instanceof Player && le == p) continue;
                        if (le.getLocation().distance(target) < 1.5)
                            DMisc.damageDemigods(p, le, DMisc.getAscensions(p) * 2, DamageCause.LIGHTNING);
                    }
                }
            } else {
                p.sendMessage(ChatColor.YELLOW + "Your target is too far away, or too close to you.");
                return false;
            }
        } catch (Exception ignored) {
        } // ignore it if something went wrong
        return true;
    }

    private void strikeLightning(Player p, Entity target) {
        if (!p.getWorld().equals(target.getWorld())) return;
        if (!DMisc.canTarget(target, target.getLocation())) return;
        p.getWorld().strikeLightningEffect(target.getLocation());
        for (Entity e : target.getLocation().getBlock().getChunk().getEntities()) {
            if (e instanceof LivingEntity) {
                LivingEntity le = (LivingEntity) e;
                if (le instanceof Player && (le == p || !DMisc.canTarget(le, le.getLocation()))) continue;
                if (le.getLocation().distance(target.getLocation()) < 1.5)
                    DMisc.damageDemigods(p, le, DMisc.getAscensions(p) * (2 * 3), DamageCause.LIGHTNING);
            }
        }
    }

    @Override
    public void onSyncTick(long timeSent) {

    }

    @Override
    public boolean canTribute() {
        return true;
    }
}
