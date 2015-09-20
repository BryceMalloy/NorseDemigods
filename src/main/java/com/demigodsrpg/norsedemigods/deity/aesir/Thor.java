package com.demigodsrpg.norsedemigods.deity.aesir;

import com.demigodsrpg.norsedemigods.deity.Deity;
import com.demigodsrpg.norsedemigods.util.DMiscUtil;
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
import java.util.Set;
import java.util.UUID;

/*
 * Affected by level:
 * Fall damage reduction
 * Shove distance/range
 * Ultimate cooldown
 */

public class Thor implements Deity {
    /**
     *
     */
    private static final long serialVersionUID = 2242753324910371936L;

    private final UUID PLAYER;
    private static final int SHOVECOST = 170;
    private static final int SHOVEDELAY = 1500; // milliseconds
    private static final int LIGHTNINGCOST = 140;
    private static final int LIGHTNINGDELAY = 1000; // milliseconds
    private static final int ZEUSULTIMATECOST = 3700;
    private static final int ZEUSULTIMATECOOLDOWNMAX = 600; // seconds
    private static final int ZEUSULTIMATECOOLDOWNMIN = 60;

    private long ZEUSSHOVETIME;
    private long ZEUSLIGHTNINGTIME;
    private boolean SHOVE = false;
    private boolean LIGHTNING = false;
    private Material SHOVEBIND = null;
    private Material LIGHTNINGBIND = null;

    public Thor(UUID player) {
        PLAYER = player;
        ZEUSSHOVETIME = System.currentTimeMillis();
        ZEUSLIGHTNINGTIME = System.currentTimeMillis();
    }

    @Override
    public String getDefaultAlliance() {
        return "AEsir";
    }

    @Override
    public void printInfo(Player p) {
        if (DMiscUtil.hasDeity(p, "Thor") && DMiscUtil.isFullParticipant(p)) {
            int devotion = DMiscUtil.getDevotion(p, getName());
            /*
             * Calculate special values first
			 */
            // shove
            int targets = (int) Math.ceil(1.561 * Math.pow(devotion, 0.128424));
            double multiply = 0.1753 * Math.pow(devotion, 0.322917);
            // ultimate
            int t = (int) (ZEUSULTIMATECOOLDOWNMAX - ((ZEUSULTIMATECOOLDOWNMAX - ZEUSULTIMATECOOLDOWNMIN) * ((double) DMiscUtil.getAscensions(p) / 100)));
            /*
             * The printed text
			 */
            p.sendMessage("--" + ChatColor.GOLD + "Thor" + ChatColor.GRAY + " [" + devotion + "]");
            p.sendMessage(":Immune to fall damage.");
            p.sendMessage(":Strike lightning at a target location. " + ChatColor.GREEN + "/lightning");
            p.sendMessage(ChatColor.YELLOW + "Costs " + LIGHTNINGCOST + " Favor.");
            if (((Thor) (DMiscUtil.getDeity(p, "Thor"))).LIGHTNINGBIND != null)
                p.sendMessage(ChatColor.AQUA + "    Bound to " + ((Thor) (DMiscUtil.getDeity(p, "Thor"))).LIGHTNINGBIND.name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to designate an item as Thor's hammer.");
            p.sendMessage(":Use the force of Thor's hammer to knock back enemies. " + ChatColor.GREEN + "/slam");
            p.sendMessage(ChatColor.YELLOW + "Costs " + SHOVECOST + " Favor.");
            p.sendMessage("Affects up to " + targets + " targets with power " + (int) (Math.round(multiply * 10)) + ".");
            if (((Thor) (DMiscUtil.getDeity(p, "Thor"))).SHOVEBIND != null)
                p.sendMessage(ChatColor.AQUA + "    Bound to " + ((Thor) (DMiscUtil.getDeity(p, "Thor"))).SHOVEBIND.name());
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
    public UUID getPlayerId() {
        return PLAYER;
    }

    @Override
    public void onEvent(Event ee) {
        if (ee instanceof PlayerInteractEvent) {
            PlayerInteractEvent e = (PlayerInteractEvent) ee;
            Player p = e.getPlayer();
            if (!DMiscUtil.hasDeity(p, "Thor") || !DMiscUtil.isFullParticipant(p)) return;
            if (SHOVE || ((p.getItemInHand() != null) && (p.getItemInHand().getType() == SHOVEBIND))) {
                if (ZEUSSHOVETIME > System.currentTimeMillis()) return;
                ZEUSSHOVETIME = System.currentTimeMillis() + SHOVEDELAY;
                if (DMiscUtil.getFavor(p) >= SHOVECOST) {
                    shove(p);
                    DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) - SHOVECOST);
                    return;
                } else {
                    p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor.");
                    SHOVE = false;
                }
            }
            if (LIGHTNING || ((p.getItemInHand() != null) && (p.getItemInHand().getType() == LIGHTNINGBIND))) {
                if (ZEUSLIGHTNINGTIME > System.currentTimeMillis()) return;
                ZEUSLIGHTNINGTIME = System.currentTimeMillis() + LIGHTNINGDELAY;
                if (DMiscUtil.getFavor(p) >= LIGHTNINGCOST) {
                    lightning(p, e.getClickedBlock());
                    DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) - LIGHTNINGCOST);
                } else {
                    p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor.");
                    LIGHTNING = false;
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
        final Player p = P;
        if (!DMiscUtil.hasDeity(p, "Thor")) return;
        if (str.equalsIgnoreCase("lightning")) {
            if (bind) {
                if (LIGHTNINGBIND == null) {
                    if (DMiscUtil.isBound(p, p.getItemInHand().getType()))
                        p.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                    if (p.getItemInHand().getType() == Material.AIR)
                        p.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                    else {
                        DMiscUtil.registerBind(p, p.getItemInHand().getType());
                        LIGHTNINGBIND = p.getItemInHand().getType();
                        p.sendMessage(ChatColor.YELLOW + "Lightning is now bound to " + p.getItemInHand().getType().name() + ".");
                    }
                } else {
                    DMiscUtil.removeBind(p, LIGHTNINGBIND);
                    p.sendMessage(ChatColor.YELLOW + "Lightning is no longer bound to " + LIGHTNINGBIND.name() + ".");
                    LIGHTNINGBIND = null;
                }
                return;
            }
            if (LIGHTNING) {
                LIGHTNING = false;
                p.sendMessage(ChatColor.YELLOW + "Lightning is no longer active.");
            } else {
                LIGHTNING = true;
                p.sendMessage(ChatColor.YELLOW + "Lightning is now active.");
            }
        } else if (str.equalsIgnoreCase("slam")) {
            if (bind) {
                if (SHOVEBIND == null) {
                    if (DMiscUtil.isBound(p, p.getItemInHand().getType()))
                        p.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                    if (p.getItemInHand().getType() == Material.AIR)
                        p.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                    else {
                        DMiscUtil.registerBind(p, p.getItemInHand().getType());
                        SHOVEBIND = p.getItemInHand().getType();
                        p.sendMessage(ChatColor.YELLOW + "Slam is now bound to " + p.getItemInHand().getType().name() + ".");
                    }
                } else {
                    DMiscUtil.removeBind(p, SHOVEBIND);
                    p.sendMessage(ChatColor.YELLOW + "Slam is no longer bound to " + SHOVEBIND.name() + ".");
                    SHOVEBIND = null;
                }
                return;
            }
            if (SHOVE) {
                SHOVE = false;
                p.sendMessage(ChatColor.YELLOW + "Slam is no longer active.");
            } else {
                SHOVE = true;
                p.sendMessage(ChatColor.YELLOW + "Slam is now active.");
            }
        }
    }

    /*
     * ---------------
     * Helper methods
     * ---------------
     */
    private void shove(Player p) {
        if (!DMiscUtil.canTarget(p, p.getLocation())) {
            p.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
            return;
        }
        ArrayList<LivingEntity> hit = new ArrayList<LivingEntity>();
        int devotion = DMiscUtil.getDevotion(p, getName());
        int targets = (int) Math.ceil(1.561 * Math.pow(devotion, 0.128424));
        double multiply = 0.1753 * Math.pow(devotion, 0.322917);
        for (Block b : p.getLineOfSight((Set) null, 10)) {
            for (LivingEntity le : p.getWorld().getLivingEntities()) {
                if (targets == hit.size()) break;
                if (le instanceof Player) {
                    if (DMiscUtil.areAllied(p, (Player) le)) continue;
                }
                if ((le.getLocation().distance(b.getLocation()) <= 5) && !hit.contains(le))
                    if (DMiscUtil.canTarget(le, le.getLocation())) hit.add(le);
            }
        }
        if (hit.size() > 0) {
            for (LivingEntity le : hit) {
                Vector v = p.getLocation().toVector();
                Vector victor = le.getLocation().toVector().subtract(v);
                victor.multiply(multiply);
                le.setVelocity(victor);
            }
        }
    }

    private void lightning(Player p, Block b) {
        if (!DMiscUtil.canTarget(p, p.getLocation())) {
            p.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
            return;
        }
        try {
            Location target = b.getLocation();
            p.getWorld().strikeLightningEffect(target);
            if (p.getLocation().distance(target) > 2) {
                if (!p.getWorld().equals(target.getWorld())) return;
                if (!DMiscUtil.canLocationPVP(target)) return;
                for (Entity e : b.getLocation().getChunk().getEntities()) {
                    if (e.getLocation().distance(target) > 1) continue;
                    if (e instanceof LivingEntity) {
                        LivingEntity le = (LivingEntity) e;
                        if (le instanceof Player && le == p) continue;
                        if (le.getLocation().distance(target) < 1.5)
                            DMiscUtil.damageDemigods(p, le, DMiscUtil.getAscensions(p) * 2, DamageCause.LIGHTNING);
                    }
                }
            } else p.sendMessage(ChatColor.YELLOW + "Your target is too far away, or too close to you.");
        } catch (Exception ignored) {
        } // ignore it if something went wrong
    }

    private void strikeLightning(Player p, Entity target) {
        if (!p.getWorld().equals(target.getWorld())) return;
        if (!DMiscUtil.canTarget(target, target.getLocation())) return;
        p.getWorld().strikeLightningEffect(target.getLocation());
        for (Entity e : target.getLocation().getBlock().getChunk().getEntities()) {
            if (e instanceof LivingEntity) {
                LivingEntity le = (LivingEntity) e;
                if (le instanceof Player && (le == p || !DMiscUtil.canTarget(le, le.getLocation()))) continue;
                if (le.getLocation().distance(target.getLocation()) < 1.5)
                    DMiscUtil.damageDemigods(p, le, DMiscUtil.getAscensions(p) * (2 * 3), DamageCause.LIGHTNING);
            }
        }
    }

    @Override
    public void onTick(long timeSent) {

    }

    @Override
    public boolean canTribute() {
        return true;
    }
}
