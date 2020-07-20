package com.demigodsrpg.norsedemigods.deity.jotunn;

/*
 * This style/format of code is now deprecated.
 */

import com.demigodsrpg.norsedemigods.DFixes;
import com.demigodsrpg.norsedemigods.DMisc;
import com.demigodsrpg.norsedemigods.Deity;
import com.demigodsrpg.norsedemigods.Setting;
import com.demigodsrpg.norsedemigods.deity.AD;
import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class Jormungand implements Deity {

    /* General */
    private static final long serialVersionUID = 2319323778421842381L;
    private final int REELCOST = 120;
    private final int drownCOST = 240;
    // private final int ULTIMATECOST = 5000;
    // private final int ULTIMATECOOLDOWNMAX = 800;
    // private final int ULTIMATECOOLDOWNMIN = 220;


    @Override
    public String getName() {
        return "Jormungand";
    }

    @Override
    public String getDefaultAlliance() {
        return "Jotunn";
    }

    @Override
    public void printInfo(Player p) {
        if (DMisc.hasDeity(p, "Jormungand") && DMisc.isFullParticipant(p)) {
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            int devotion = DMisc.getDevotion(p, getName());
            /*
             * Calculate special values first
             */
            // heal amount
            int healamt = (int) Math.ceil(0.1 * Math.pow(devotion, 0.297));
            // heal interval
            int healinterval = 10 - (int) (Math.round(Math.pow(devotion, 0.125))); // seconds
            if (healinterval < 1) healinterval = 1;
            // drown
            int radius = (int) (Math.ceil(1.6955424 * Math.pow(devotion, 0.129349)));
            int duration = (int) Math.ceil(2.80488 * Math.pow(devotion, 0.2689)); // seconds
            // reel
            int damage = (int) Math.ceil(0.37286 * Math.pow(devotion, 0.371238));
            // ult
            // int numtargets = (int)Math.round(5*Math.pow(devotion, 0.15));
            // int ultduration = (int)Math.round(30*Math.pow(devotion, 0.09));
            // int t = (int)(ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN)*
            // ((double)DMiscUtil.getAscensions(p)/100)));
            /*
             * The printed text
             */
            p.sendMessage("--" + ChatColor.GOLD + "Jormungand" + ChatColor.GRAY + " [" + devotion + "]");
            p.sendMessage(":Heal " + healamt + " every " + healinterval + " seconds while in contact with water.");
            p.sendMessage("Immune to drowning, sneak while in water to swim very fast!");
            p.sendMessage(":Deal " + damage + " damage and soak an enemy from a distance. " + ChatColor.GREEN + "/reel");
            p.sendMessage(ChatColor.YELLOW + "Costs " + REELCOST + " Favor. Must have fishing rod in hand.");
            if (save.getAbilityData("reel", AD.ACTIVE, false))
                p.sendMessage(ChatColor.AQUA + "    Reel is active.");
            p.sendMessage(":Create a temporary flood of water. " + ChatColor.GREEN + "/drown");
            p.sendMessage(ChatColor.YELLOW + "Costs " + drownCOST + " Favor.");
            p.sendMessage("Water has radius of " + radius + " for " + duration + " seconds.");
            if (save.getBind("drown").isPresent())
                p.sendMessage(ChatColor.AQUA + "    drown bound to " + save.getBind("drown").get().name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to bind this skill to an item.");
            return;
        }
        p.sendMessage("--" + ChatColor.GOLD + "Jormungand");
        p.sendMessage("Passive: Immune to drowning, with increased healing while in water.");
        p.sendMessage("Passive: Fast swim, sneak while in water to swim very fast!");
        p.sendMessage("Active: Deal damage and soak an enemy with a fishing rod. " + ChatColor.GREEN + "/reel");
        p.sendMessage(ChatColor.YELLOW + "Costs " + REELCOST + " Favor.");
        p.sendMessage("Active: Create a temporary flood of water.");
        p.sendMessage(ChatColor.GREEN + "/drown " + ChatColor.YELLOW + "Costs " + drownCOST + " Favor. Can bind.");
        p.sendMessage(ChatColor.YELLOW + "Select item: water bucket");
    }

    @Override
    public void onEvent(Event ee) {
        if (ee instanceof PlayerMoveEvent) {
            PlayerMoveEvent move = (PlayerMoveEvent) ee;
            Player p = move.getPlayer();
            if (!DMisc.isFullParticipant(p)) return;
            if (!DMisc.hasDeity(p, "Jormungand")) return;
            // PHELPS SWIMMING
            if (p.getLocation().getBlock().getType().equals(Material.WATER)) {
                Vector dir = p.getLocation().getDirection().normalize().multiply(1.3D);
                Vector vec = new Vector(dir.getX(), dir.getY(), dir.getZ());
                if (p.isSneaking()) p.setVelocity(vec);
            }
        }
        if (ee instanceof EntityDamageEvent) {
            if (((EntityDamageEvent) ee).getCause().equals(DamageCause.DROWNING) && ((EntityDamageEvent) ee).getEntity() instanceof Player) {
                Player p = (Player) ((EntityDamageEvent) ee).getEntity();
                if (!DMisc.isFullParticipant(p)) return;
                if (!DMisc.hasDeity(p, "Jormungand")) return;
                DFixes.checkAndCancel((EntityDamageEvent) ee);
            }
        } else if (ee instanceof PlayerInteractEvent) {
            PlayerInteractEvent e = (PlayerInteractEvent) ee;
            Player p = e.getPlayer();
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            if (!DMisc.isFullParticipant(p)) return;
            if (!DMisc.hasDeity(p, "Jormungand")) return;
            if (save.getAbilityData("reel", AD.ACTIVE, false)) {
                if (p.getItemInHand().getType() == Material.FISHING_ROD) {
                    double time = save.getAbilityData("reel", AD.TIME, (double) System.currentTimeMillis());
                    if (System.currentTimeMillis() < time) return;
                    if (DMisc.getFavor(p) > REELCOST) {
                        if (reel(p, save)) {
                            DMisc.setFavor(p, DMisc.getFavor(p) - REELCOST);
                            int REELDELAY = 1100;
                            save.setAbilityData("reel", AD.TIME, System.currentTimeMillis() + REELDELAY);
                        }
                    } else {
                        p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor.");
                        save.setAbilityData("reel", AD.ACTIVE, false);
                    }
                }
            }
            Optional<Material> drownBind = save.getBind("drown");
            if ((drownBind.isPresent() && p.getItemInHand().getType() == drownBind.get()) ||
                    save.getAbilityData("drown", AD.ACTIVE, false)) {
                double time = save.getAbilityData("drown", AD.TIME, (double) System.currentTimeMillis());
                if (System.currentTimeMillis() < time) {
                    p.sendMessage(ChatColor.YELLOW + "You may not use this skill yet.");
                    return;
                }
                if (DMisc.getFavor(p) > drownCOST) {
                    if (drown(p)) {
                        DMisc.setFavor(p, DMisc.getFavor(p) - drownCOST);
                        int DROWNDELAY = 15000;
                        save.setAbilityData("drown", "time", System.currentTimeMillis() + DROWNDELAY);
                    }
                } else {
                    p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor.");
                    save.setAbilityData("drown", "active", false);
                }
            }
        }
    }

    @Override
    public void onCommand(Player P, String str, String[] args, boolean bind) {
        if (!DMisc.isFullParticipant(P)) return;
        if (!DMisc.hasDeity(P, "Jormungand")) return;
        PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(P);
        if (str.equalsIgnoreCase("reel")) {
            if (save.getAbilityData("reel", AD.ACTIVE, false)) {
                save.setAbilityData("reel", AD.ACTIVE, false);
                P.sendMessage(ChatColor.YELLOW + "Reel is no longer active.");
            } else {
                save.setAbilityData("reel", AD.ACTIVE, true);
                P.sendMessage(ChatColor.YELLOW + "Reel is now active.");
                P.sendMessage(ChatColor.YELLOW + "It can only be used with a fishing rods.");
            }
        } else if (str.equalsIgnoreCase("drown")) {
            if (bind) {
                Optional<Material> drownBind = save.getBind("drown");
                if (!drownBind.isPresent()) {
                    if (DMisc.isBound(P, P.getItemInHand().getType()))
                        P.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                    if (P.getItemInHand().getType() == Material.AIR)
                        P.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                    else {
                        save.setBind("drown", P.getItemInHand().getType());
                        P.sendMessage(ChatColor.YELLOW + "Drown is now bound to " + P.getItemInHand().getType().name() + ".");
                    }
                } else {
                    save.removeBind("drown");
                    P.sendMessage(ChatColor.YELLOW + "Drown is no longer bound to " + drownBind.get().name() + ".");
                }
                return;
            }
            if (save.getAbilityData("drown", AD.ACTIVE, false)) {
                save.setAbilityData("drown", AD.ACTIVE, false);
                P.sendMessage(ChatColor.YELLOW + "Drown is no longer active.");
            } else {
                save.setAbilityData("drown", AD.ACTIVE, true);
                P.sendMessage(ChatColor.YELLOW + "Drown is now active.");
            }
        }

    }

    @Override
    public void onSyncTick(long timeSent) {
        for (UUID id : getPlayerIds()) {
            Optional<PlayerDataSaveable> opSave = getBackend().getPlayerDataRegistry().fromKey(id.toString());
            if (opSave.isPresent()) {
                Player p = Bukkit.getPlayer(id);
                if (p != null) {
                    PlayerDataSaveable save = opSave.get();
                    int healinterval = 10 - (int) (Math.round(Math.pow(DMisc.getDevotion(id, getName()), 0.125))); // seconds
                    if (healinterval < 1) healinterval = 1;
                    if (save.getAbilityData("jormungand_heal", AD.TIME).isPresent() && timeSent > (double) save.getAbilityData("jormungand_heal", AD.TIME).get() + (healinterval * 1000)) {
                        save.setAbilityData("jormungand_heal", AD.TIME, timeSent);
                        if ((p.getLocation().getBlock().getType() == Material.WATER) || (p.getEyeLocation().getBlock().getType() == Material.WATER)) {
                            double healamt = Math.ceil(0.1 * Math.pow(DMisc.getDevotion(id, getName()), 0.297));
                            if (p.getHealth() + healamt > p.getMaxHealth())
                                healamt = p.getMaxHealth() - p.getHealth();
                            DMisc.setHP(p, p.getHealth() + healamt);
                        }
                    }
                }
            }
        }
    }

    private boolean reel(Player p, PlayerDataSaveable save) {
        if (!DMisc.canTarget(p, p.getLocation())) {
            return false;
        }
        LivingEntity le = DMisc.getTargetLivingEntity(p, 3);
        if ((le == null) || le.isDead()) return false;
        if (!DMisc.canTarget(le, le.getLocation())) return false;
        if (le.getLocation().getBlock().getType() == Material.AIR) {
            le.getLocation().getBlock().setType(Material.WATER);
            //le.getLocation().getBlock().setData((byte) 0x8);
        }
        int damage = (int) Math.ceil(0.37286 * Math.pow(DMisc.getDevotion(p, getName()), 0.371238));
        if (le instanceof Player) {
            if (DMisc.isFullParticipant((Player) le))
                if (DMisc.getAllegiance((Player) le).equalsIgnoreCase(DMisc.getAllegiance(p))) return false;
        }
        DMisc.damageDemigods(p, le, damage, DamageCause.ENTITY_ATTACK);
        save.setAbilityData("reel", AD.TIME, System.currentTimeMillis());
        return true;
    }

    private boolean drown(Player p) {
        if (!DMisc.canTarget(p, p.getLocation())) {
            p.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
            return false;
        }
        // special values
        int devotion = DMisc.getDevotion(p, getName());
        int radius = (int) (Math.ceil(1.6955424 * Math.pow(devotion, 0.129349)));
        int duration = (int) Math.ceil(2.80488 * Math.pow(devotion, 0.2689)); // seconds
        //
        Location target = DMisc.getTargetLocation(p);
        if (!DMisc.canLocationPVP(p, target) || !DMisc.canWorldGuardBuild(p, target)) {
            p.sendMessage(ChatColor.YELLOW + "That is a no-PVP zone.");
            return false;
        }

        if (target.getBlockY() >= Setting.DROWN_HEIGHT_LIMIT) {
            p.sendMessage(ChatColor.YELLOW + "You cannot use drown from this high up.");
            return false;
        }

        drown(p, target, radius, duration * 20);
        return true;
    }

    private void drown(Player caster, Location target, int radius, int duration) {
        final ArrayList<Block> toreset = new ArrayList<Block>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = target.getWorld().getBlockAt(target.getBlockX() + x, target.getBlockY() + y, target.getBlockZ() + z);
                    if (block.getLocation().distance(target) <= radius) {
                        if (DMisc.canLocationPVP(caster, block.getLocation())) if (block.getType() == Material.AIR) {
                            block.setType(Material.WATER);
                            //block.setData((byte) (0x8));
                            toreset.add(block);
                        }
                    }
                }
            }
        }
        DMisc.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMisc.getPlugin(), new Runnable() {
            @Override
            public void run() {
                for (Block b : toreset)
                    if ((b.getType() == Material.WATER))
                        b.setType(Material.AIR);
            }
        }, duration);
    }
    /*
     * private int waterfall(Player p) {
     * int numtargets = (int)Math.round(15*Math.pow(DMiscUtil.getDevotion(p, getName()), 0.15));
     * final int ultduration = (int)Math.round(30*Math.pow(DMiscUtil.getDevotion(p, getName()), 0.09)*20);
     * ArrayList<LivingEntity> entitylist = new ArrayList<LivingEntity>();
     * Vector ploc = p.getLocation().toVector();
     * for (LivingEntity anEntity : p.getWorld().getLivingEntities()){
     * if (anEntity instanceof Player)
     * if (DMiscUtil.isFullParticipant((Player)anEntity))
     * if (DMiscUtil.getAllegiance((Player)anEntity).equalsIgnoreCase(DMiscUtil.getAllegiance(p)))
     * continue;
     * if (!DMiscUtil.canPVP(anEntity.getLocation()))
     * continue;
     * if (anEntity.getLocation().toVector().isInSphere(ploc, 50.0) && (entitylist.size() < numtargets))
     * entitylist.add(anEntity);
     * }
     * for (LivingEntity le : entitylist) {
     * final LivingEntity fl = le;
     * for (int i=0;i<ultduration;i+=9) {
     * final int ii = i;
     * DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
     *
     * @Override
     * public void run() {
     * if ((fl != null) && !fl.isDead()) {
     * drown(fl.getLocation(), 3, ultduration - ii);
     * }
     * }
     * }, i);
     * }
     * }
     * return entitylist.size();
     * }
     */

    @Override
    public boolean canTribute() {
        return true;
    }
}
