package com.demigodsrpg.norsedemigods.deity.aesir;

import com.demigodsrpg.norsedemigods.DMisc;
import com.demigodsrpg.norsedemigods.Deity;
import com.demigodsrpg.norsedemigods.deity.AD;
import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Set;

public class Odin implements Deity {
    // global vars
    private static final int STABCOST = 100;
    private static final int SLOWCOST = 180;
    private static final int CRONUSULTIMATECOST = 5000;
    private static final int CRONUSULTIMATECOOLDOWNMAX = 500;
    private static final int CRONUSULTIMATECOOLDOWNMIN = 120;

    @Override
    public String getName() {
        return "Odin";
    }

    @Override
    public String getDefaultAlliance() {
        return "AEsir";
    }

    @Override
    public void printInfo(Player p) {
        if (DMisc.hasDeity(p, "Odin") && DMisc.isFullParticipant(p)) {
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            int devotion = DMisc.getDevotion(p, getName());
            /*
             * Calculate special values first
             */
            // cleave
            int damage = (int) Math.ceil(Math.pow(devotion, 0.35));
            int hungerdamage = (int) Math.ceil(Math.pow(devotion, 0.1776));
            // slow
            int duration = (int) Math.ceil(3.635 * Math.pow(devotion, 0.2576)); // seconds
            int strength = (int) Math.ceil(2.757 * Math.pow(devotion, 0.097));
            // ultimate
            int slowamount = (int) Math.round(.77179 * Math.pow(DMisc.getAscensions(p), 0.17654391));
            int stopduration = (int) Math.round(9.9155621 * Math.pow(DMisc.getAscensions(p), 0.459019));
            int t = (int) (CRONUSULTIMATECOOLDOWNMAX - ((CRONUSULTIMATECOOLDOWNMAX - CRONUSULTIMATECOOLDOWNMIN) * ((double) DMisc.getAscensions(p) / 100)));
            /*
             * The printed text
             */
            p.sendMessage("--" + ChatColor.GOLD + "Odin" + ChatColor.GRAY + "[" + devotion + "]");
            p.sendMessage(":Slow your enemy when attacking with a spear (shovel).");
            p.sendMessage(":Attack with a spear (shovel) to deal " + damage + " damage and " + hungerdamage + " hunger. " + ChatColor.GREEN + "/stab");
            p.sendMessage(ChatColor.YELLOW + "Costs " + STABCOST + " Favor.");
            p.sendMessage(":Slow time to reduce movement speed of an enemy player. " + ChatColor.GREEN + "/slow");
            p.sendMessage(ChatColor.YELLOW + "Costs " + SLOWCOST + " Favor.");
            p.sendMessage("Slow power: " + strength + " for " + duration + " seconds.");
            if (save.getBind("slow").isPresent())
                p.sendMessage(ChatColor.AQUA + "    Bound to " + save.getBind("slow").get().name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to bind this skill to an item.");
            p.sendMessage(":Odin slows enemies' perception of time, slowing their");
            p.sendMessage("movement by " + slowamount + " for " + stopduration + " seconds. " + ChatColor.GREEN + "/timestop");
            p.sendMessage(ChatColor.YELLOW + "Costs " + CRONUSULTIMATECOST + " Favor. Cooldown time: " + t + " seconds.");
            return;
        }
        p.sendMessage("--" + ChatColor.GOLD + "Odin");
        p.sendMessage("Passive: Slow your enemy when attacking with a spear (shovel).");
        p.sendMessage("Active: Cause extra damage and hunger with a spear (shovel). " + ChatColor.GREEN + "/stab");
        p.sendMessage(ChatColor.YELLOW + "Costs " + STABCOST + " Favor. Can bind.");
        p.sendMessage("Active: Slow time to reduce movement speed of an enemy player.");
        p.sendMessage(ChatColor.GREEN + "/slow " + ChatColor.YELLOW + "Costs " + SLOWCOST + " Favor. Can bind.");
        p.sendMessage("Ultimate: Odin slows enemies' perception of time,");
        p.sendMessage("slowing their movement drastically. " + ChatColor.GREEN + "/timestop");
        p.sendMessage(ChatColor.YELLOW + "Costs " + CRONUSULTIMATECOST + " Favor. Has cooldown.");
        p.sendMessage(ChatColor.YELLOW + "Select item: soul sand");
    }

    @Override
    public void onEvent(Event ee) {
        if (ee instanceof EntityDamageEvent) {
            if (ee instanceof EntityDamageByEntityEvent && !((EntityDamageByEntityEvent) ee).isCancelled()) {
                EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) ee;
                if (e.getDamager() instanceof Player) {
                    Player p = (Player) e.getDamager();
                    if (DMisc.isFullParticipant(p)) {
                        PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
                        if (!DMisc.hasDeity(p, "Odin")) return;
                        if (!p.getItemInHand().getType().name().contains("_SPADE")) return;
                        if (!DMisc.canTarget(p, p.getLocation())) return;

                        Material STABITEM = p.getItemInHand().getType();

                        /*
                         * Passive ability (stop movement)
                         */
                        if (e.getEntity() instanceof Player) {
                            Player attacked = (Player) e.getEntity();
                            if (!DMisc.canTarget(attacked, attacked.getLocation())) return;
                            if (!DMisc.isFullParticipant(attacked) || (DMisc.isFullParticipant(attacked) && !(DMisc.getAllegiance(p).equalsIgnoreCase(DMisc.getAllegiance(attacked))))) {
                                attacked.setVelocity(new Vector(0, 0, 0));
                            }
                        }
                        /*
                         * Cleave
                         */
                        if (save.getAbilityData("stab", AD.ACTIVE, false)) {
                            if (DMisc.getFavor(p) >= STABCOST) {
                                if (!(e.getEntity() instanceof LivingEntity)) return;
                                if (System.currentTimeMillis() < save.getAbilityData("stab", AD.TIME, 0) + 100) return;
                                if (!DMisc.canTarget(e.getEntity(), e.getEntity().getLocation())) return;
                                DMisc.setFavor(p, DMisc.getFavor(p) - STABCOST);
                                for (int i = 1; i <= 31; i += 4)
                                    e.getEntity().getWorld().playEffect(e.getEntity().getLocation(), Effect.SMOKE, i);
                                DMisc.damageDemigods(p, (LivingEntity) e.getEntity(), (int) Math.ceil(Math.pow(DMisc.getDevotion(p, getName()), 0.35)), EntityDamageEvent.DamageCause.ENTITY_ATTACK);
                                save.setAbilityData("stab", AD.TIME, System.currentTimeMillis());
                                if (e.getEntity() instanceof Player) {
                                    Player otherP = (Player) e.getEntity();
                                    otherP.setFoodLevel(otherP.getFoodLevel() - (int) (e.getDamage() / 2));
                                    if (otherP.getFoodLevel() < 0) otherP.setFoodLevel(0);
                                }
                            } else {
                                p.sendMessage(ChatColor.YELLOW + "You don't have enough Favor to do that.");
                                save.setAbilityData("stab", AD.ACTIVE, false);
                            }
                        }
                    }
                }
            }
        } else if (ee instanceof PlayerInteractEvent) {
            PlayerInteractEvent e = (PlayerInteractEvent) ee;
            Player p = e.getPlayer();
            if (!DMisc.hasDeity(p, "Odin")) return;
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            if (save.getAbilityData("stab", AD.ACTIVE, false) || save.getBind("stab").isPresent() && (p.getItemInHand().getType() == save.getBind("stab").get())) {
                if (DMisc.getFavor(p) >= SLOWCOST) {
                    if (slow(p)) DMisc.setFavor(p, DMisc.getFavor(p) - SLOWCOST);
                } else {
                    save.setAbilityData("stab", AD.ACTIVE, false);
                    p.sendMessage(ChatColor.YELLOW + "You don't have enough Favor to do that.");
                }
            }
        }
    }

    @Override
    public void onCommand(final Player p, String str, String[] args, boolean bind) {
        if (!DMisc.hasDeity(p, "Odin")) return;
        PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
        if (str.equalsIgnoreCase("stab")) {
            if (save.getAbilityData("stab", AD.ACTIVE, false)) {
                save.setAbilityData("stab", AD.ACTIVE, false);
                p.sendMessage(ChatColor.YELLOW + "Stab is no longer active.");
            } else {
                save.setAbilityData("stab", AD.ACTIVE, true);
                p.sendMessage(ChatColor.YELLOW + "Stab is now active.");
            }
        } else if (str.equalsIgnoreCase("slow")) {
            if (bind) {
                if (!save.getBind("slow").isPresent()) {
                    if (DMisc.isBound(p, p.getItemInHand().getType()))
                        p.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                    if (p.getItemInHand().getType() == Material.AIR)
                        p.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                    else {
                        save.setBind("slow", p.getItemInHand().getType());
                        p.sendMessage(ChatColor.YELLOW + "Slow is now bound to " + p.getItemInHand().getType().name() + ".");
                    }
                } else {
                    p.sendMessage(ChatColor.YELLOW + "Slow is no longer bound to " + save.getBind("slow").get().name() + ".");
                    save.removeBind("slow");
                }
                return;
            }
            if (save.getAbilityData("slow", AD.ACTIVE, false)) {
                save.setAbilityData("slow", AD.ACTIVE, false);
                p.sendMessage(ChatColor.YELLOW + "Slow is no longer active.");
            } else {
                save.setAbilityData("slow", AD.ACTIVE, true);
                p.sendMessage(ChatColor.YELLOW + "Slow is now active.");
            }
        } else if (str.equalsIgnoreCase("timestop")) {
            if (!DMisc.hasDeity(p, "Odin")) return;
            double CRONUSULTIMATETIME = save.getAbilityData("timestop", AD.TIME, (double) System.currentTimeMillis());
            if (System.currentTimeMillis() < CRONUSULTIMATETIME) {
                p.sendMessage(ChatColor.YELLOW + "You cannot stop time again for " + ((((CRONUSULTIMATETIME) / 1000) - (System.currentTimeMillis() / 1000))) / 60 + " minutes");
                p.sendMessage(ChatColor.YELLOW + "and " + ((((CRONUSULTIMATETIME) / 1000) - (System.currentTimeMillis() / 1000)) % 60) + " seconds.");
                return;
            }
            if (DMisc.getFavor(p) >= CRONUSULTIMATECOST) {
                if (!DMisc.canTarget(p, p.getLocation())) {
                    p.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
                    return;
                }
                int t = (int) (CRONUSULTIMATECOOLDOWNMAX - ((CRONUSULTIMATECOOLDOWNMAX - CRONUSULTIMATECOOLDOWNMIN) * ((double) DMisc.getAscensions(p) / 100)));
                save.setAbilityData("timestop", AD.TIME, System.currentTimeMillis() + (t * 1000));
                timeStop(p);
                DMisc.setFavor(p, DMisc.getFavor(p) - CRONUSULTIMATECOST);
            } else p.sendMessage(ChatColor.YELLOW + "Stopping time requires " + CRONUSULTIMATECOST + " Favor.");
        }
    }

    @Override
    public void onSyncTick(long timeSent) {

    }

    private boolean slow(Player p) {
        int devotion = DMisc.getDevotion(p, getName());
        int duration = (int) Math.ceil(3.635 * Math.pow(devotion, 0.2576)); // seconds
        int strength = (int) Math.ceil(2.757 * Math.pow(devotion, 0.097));
        Player target = null;
        Block b = p.getTargetBlock((Set) null, 200);
        for (Player pl : b.getWorld().getPlayers()) {
            if (pl.getLocation().distance(b.getLocation()) < 4) {
                if (!DMisc.areAllied(pl, p) && DMisc.canTarget(pl, pl.getLocation())) {
                    target = pl;
                    break;
                }
            }
        }
        if ((target != null) && (target.getEntityId() != p.getEntityId())) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration * 20, strength));
            p.sendMessage(ChatColor.YELLOW + target.getName() + " has been slowed.");
            target.sendMessage(ChatColor.RED + "You have been slowed for " + duration + " seconds.");
            DMisc.addActiveEffect(target.getUniqueId(), "Slow", duration);
            return true;
        } else {
            p.sendMessage(ChatColor.YELLOW + "No target found.");
            return false;
        }
    }

    private void timeStop(Player p) {
        int slowamount = (int) Math.round(4.77179 * Math.pow(DMisc.getAscensions(p), 0.17654391));
        int duration = (int) Math.round(9.9155621 * Math.pow(DMisc.getAscensions(p), 0.459019));
        int count = 0;
        for (Player pl : p.getWorld().getPlayers()) {
            if (!(pl.getLocation().toVector().isInSphere(p.getLocation().toVector(), 70))) continue;
            if (DMisc.isFullParticipant(pl)) {
                if (DMisc.getAllegiance(pl).equalsIgnoreCase(DMisc.getAllegiance(p))) continue;
                if (!DMisc.canTarget(pl, pl.getLocation())) continue;
            }
            pl.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration * 20, slowamount));
            p.sendMessage(ChatColor.DARK_RED + "Odin has slowed time around you.");
            DMisc.addActiveEffect(pl.getUniqueId(), "Time Stop", duration);
            count++;
        }
        p.sendMessage(ChatColor.RED + "Odin has slowed time for " + count + " players nearby.");
    }

    @Override
    public boolean canTribute() {
        return true;
    }
}
