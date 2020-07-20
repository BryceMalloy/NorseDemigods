package com.demigodsrpg.norsedemigods.deity.aesir;

import com.demigodsrpg.norsedemigods.DMisc;
import com.demigodsrpg.norsedemigods.Deity;
import com.demigodsrpg.norsedemigods.deity.AD;
import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;

/*
 * Affected by level:
 * Amount of EXP gained for overkill
 * Damage, range, slow amount, slow duration of strike
 * Duration of bloodthirst
 * Range, damage, and cooldown of ultimate
 */

public class Vidar implements Deity {
    /*
     * Needs to be loaded out of config
     */
    private static final int STRIKECOST = 120;
    private static final int STRIKEDELAY = 1250; // milliseconds
    private static final int ARESULTIMATECOST = 5000;
    private static final int ARESULTIMATECOOLDOWNMAX = 180; // seconds
    private static final int ARESULTIMATECOOLDOWNMIN = 60;

    @Override
    public String getDefaultAlliance() {
        return "AEsir";
    }

    @Override
    public void printInfo(Player p) {
        if (DMisc.hasDeity(p, "Vidar") && DMisc.isFullParticipant(p)) {
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            int devotion = DMisc.getDevotion(p, getName());
            /*
             * Calculate special values first
             */
            int dmg = (int) Math.round(0.9 * Math.pow(devotion, 0.34));
            final int slowpower = (int) (Math.ceil(1.681539 * Math.pow(devotion, 0.11457)));
            int duration = (int) (Math.ceil(2.9573 * Math.pow(devotion, 0.138428)));
            // ultimate
            int targets = (int) Math.ceil(3.08 * (Math.pow(1.05, DMisc.getAscensions(p))));
            int range = (int) Math.ceil(7.17 * Math.pow(1.035, DMisc.getAscensions(p)));
            int damage = (int) Math.ceil(10 * Math.pow(DMisc.getAscensions(p), 0.868));
            int confuseduration = (int) (1.0354 * Math.pow(DMisc.getAscensions(p), 0.4177)) * 20;
            int t = (int) (ARESULTIMATECOOLDOWNMAX - ((ARESULTIMATECOOLDOWNMAX - ARESULTIMATECOOLDOWNMIN) * ((double) DMisc.getAscensions(p) / 100)));
            /*
             * The printed text
             */
            p.sendMessage("--" + ChatColor.GOLD + "Vidar" + ChatColor.GRAY + " [" + devotion + "]");
            p.sendMessage(":Up to " + DMisc.getAscensions(p) + " additional Favor per hit on overkill.");
            p.sendMessage(":Strike an enemy from afar with your sword, slowing them down.");
            p.sendMessage("Slow: " + slowpower + " for " + duration + " seconds. Damage: " + dmg + ChatColor.GREEN + " /strike " + ChatColor.YELLOW + "Costs " + STRIKECOST + " Favor.");
            if (save.getBind("strike").isPresent())
                p.sendMessage(ChatColor.AQUA + "    Bound to " + save.getBind("strike").get().name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to bind this skill to an item.");
            p.sendMessage(":Vidar flings up to " + targets + " targets within range " + range + " to you, dealing");
            p.sendMessage(damage + " damage to each and confusing them for " + confuseduration + " seconds." + ChatColor.GREEN + " /crash");
            p.sendMessage(ChatColor.YELLOW + "Costs " + ARESULTIMATECOST + " Favor. Cooldown time: " + t + " seconds.");
            return;
        }
        p.sendMessage("--" + ChatColor.GOLD + "Vidar");
        p.sendMessage("Passive: Gain favor for overkill attacks.");
        p.sendMessage("Active: Strike at an enemy from afar with your sword, with");
        p.sendMessage("a slowing effect. " + ChatColor.GREEN + "/strike");
        p.sendMessage(ChatColor.YELLOW + "Costs " + STRIKECOST + " Favor. Can bind.");
        p.sendMessage("Ultimate: Vidar flings nearby enemies towards you. Damages and");
        p.sendMessage("confuses targets. " + ChatColor.GREEN + "/crash");
        p.sendMessage(ChatColor.YELLOW + "Costs " + ARESULTIMATECOST + " Favor. Has cooldown.");
        p.sendMessage(ChatColor.YELLOW + "Select item: gold sword");
    }

    @Override
    public String getName() {
        return "Vidar";
    }

    @Override
    public void onEvent(Event ee) {
        if (ee instanceof PlayerInteractEvent) {
            PlayerInteractEvent e = (PlayerInteractEvent) ee;
            Player p = e.getPlayer();
            if (!DMisc.hasDeity(p, "Vidar") || !DMisc.isFullParticipant(p)) return;
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            if ((p.getItemInHand() != null) && p.getItemInHand().getType().name().contains("SWORD")) {
                if (save.getAbilityData("strike", AD.ACTIVE, false) || ((p.getItemInHand() != null) && save.getBind("strike").isPresent() && (p.getItemInHand().getType() == save.getBind("strike").get()))) {
                    if (save.getAbilityData("strike", AD.TIME, (double) System.currentTimeMillis()) > System.currentTimeMillis())
                        return;
                    save.setAbilityData("strike", AD.TIME, System.currentTimeMillis() + STRIKEDELAY);
                    if (DMisc.getFavor(p) >= STRIKECOST) {
                        strike(p);
                        DMisc.setFavor(p, DMisc.getFavor(p) - STRIKECOST);
                    } else {
                        p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor.");
                        save.setAbilityData("strike", AD.ACTIVE, false);
                    }
                }
            }
        } else if (ee instanceof EntityDamageByEntityEvent && !((EntityDamageByEntityEvent) ee).isCancelled()) {
            try {
                EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) ee;
                if (e.getDamager() instanceof Player) {
                    Player p = (Player) e.getDamager();
                    if (!DMisc.hasDeity(p, "Vidar") || !DMisc.isFullParticipant(p)) return;
                    try {
                        LivingEntity le = (LivingEntity) e.getEntity();
                        if (le.getHealth() - e.getDamage() <= 0.0) {
                            //
                            if ((int) (Math.random() * 3) == 1) {
                                int reward = 1 + (int) (Math.random() * DMisc.getAscensions(p));
                                p.sendMessage(ChatColor.RED + "Finishing bonus: +" + reward);
                                DMisc.setFavor(p, DMisc.getFavor(p) + reward);
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }

    /*
     * ---------------
     * Commands
     * ---------------
     */
    @Override
    public void onCommand(final Player p, String str, String[] args, boolean bind) {
        if (DMisc.hasDeity(p, "Vidar")) {
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            if (str.equalsIgnoreCase("strike")) {
                if (bind) {
                    if (!save.getBind("strike").isPresent()) {
                        if (DMisc.isBound(p, p.getItemInHand().getType()))
                            p.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                        if (p.getItemInHand().getType() == Material.AIR)
                            p.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                        if (!p.getItemInHand().getType().name().contains("SWORD"))
                            p.sendMessage(ChatColor.YELLOW + "You must bind this skill to a sword.");
                        else {
                            save.setBind("strike", p.getItemInHand().getType());
                            p.sendMessage(ChatColor.YELLOW + "Strike is now bound to " + p.getItemInHand().getType().name() + ".");
                        }
                    } else {
                        p.sendMessage(ChatColor.YELLOW + "Strike is no longer bound to " + save.getBind("strike").get().name() + ".");
                        save.removeBind("strike");
                    }
                    return;
                }
                if (save.getAbilityData("strike", AD.ACTIVE, false)) {
                    save.setAbilityData("strike", AD.ACTIVE, false);
                    p.sendMessage(ChatColor.YELLOW + "Strike is no longer active.");
                } else {
                    save.setAbilityData("strike", AD.ACTIVE, true);
                    p.sendMessage(ChatColor.YELLOW + "Strike is now active.");
                }
            } else if (str.equalsIgnoreCase("crash")) {
                double TIME = save.getAbilityData("crash", AD.TIME, (double) System.currentTimeMillis());
                if (System.currentTimeMillis() < TIME) {
                    p.sendMessage(ChatColor.YELLOW + "You cannot use the power crash again for " + ((((TIME) / 1000) - (System.currentTimeMillis() / 1000))) / 60 + " minutes");
                    p.sendMessage(ChatColor.YELLOW + "and " + ((((TIME) / 1000) - (System.currentTimeMillis() / 1000)) % 60) + " seconds.");
                    return;
                }
                if (DMisc.getFavor(p) >= ARESULTIMATECOST) {
                    if (!DMisc.canTarget(p, p.getLocation())) {
                        p.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
                        return;
                    }
                    int hits = crash(p);
                    if (hits < 1) {
                        p.sendMessage(ChatColor.YELLOW + "No targets were found, or the skill could not be used.");
                        return;
                    }
                    int t = (int) (ARESULTIMATECOOLDOWNMAX - ((ARESULTIMATECOOLDOWNMAX - ARESULTIMATECOOLDOWNMIN) * ((double) DMisc.getAscensions(p) / 100)));
                    save.setAbilityData("crash", AD.TIME, System.currentTimeMillis() + (t * 1000));
                    p.sendMessage("In exchange for " + ChatColor.AQUA + ARESULTIMATECOST + ChatColor.WHITE + " Favor, ");
                    p.sendMessage(ChatColor.GOLD + "Vidar" + ChatColor.WHITE + " has unleashed his powers on " + hits + " non-allied entities.");
                    DMisc.setFavor(p, DMisc.getFavor(p) - ARESULTIMATECOST);
                } else p.sendMessage(ChatColor.YELLOW + "Power crash requires " + ARESULTIMATECOST + " Favor.");
            }
        }
    }

    /*
     * ---------------
     * Helper methods
     * ---------------
     */
    private void strike(Player p) {
        /*
         * /
         */
        LivingEntity target = DMisc.getTargetLivingEntity(p, 2);
        if (target == null) {
            p.sendMessage(ChatColor.YELLOW + "No target found.");
            return;
        }
        if (!DMisc.canTarget(target, target.getLocation()) || !DMisc.canTarget(p, p.getLocation())) {
            p.sendMessage(ChatColor.YELLOW + "Can't attack in a no-PVP zone.");
            return;
        }
        /*
         * Calculate special values
         */
        int devotion = DMisc.getDevotion(p, getName());
        int damage = (int) Math.round(0.9 * Math.pow(devotion, 0.34));
        final int slowpower = (int) (Math.ceil(1.681539 * Math.pow(devotion, 0.11457)));
        int duration = (int) (Math.ceil(2.9573 * Math.pow(devotion, 0.138428)));
        duration *= 20; // ticks
        /*
         * Deal damage and slow if player
         */
        DMisc.damageDemigods(p, target, damage, DamageCause.ENTITY_ATTACK);
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, duration, slowpower));
    }

    private int crash(Player p) {
        /*
         * Calculate specials.
         * Range: distance in a circle
         * Damage: done instantly
         * Confusion: how long players remain dizzied
         */
        int range = (int) (7.17 * Math.pow(1.035, DMisc.getAscensions(p)));
        double damage = (1.929 * Math.pow(DMisc.getAscensions(p), 0.48028));
        int confuseduration = (int) (1.0354 * Math.pow(DMisc.getAscensions(p), 0.4177)) * 20;
        /*
         * The ultimate
         */
        ArrayList<LivingEntity> targets = new ArrayList<LivingEntity>();
        ArrayList<Player> confuse = new ArrayList<Player>();
        for (LivingEntity le : p.getWorld().getLivingEntities()) {
            if (le.getLocation().distance(p.getLocation()) <= range) {
                if (le instanceof Player) {
                    Player pt = (Player) le;
                    if (DMisc.getAllegiance(pt).equals(DMisc.getAllegiance(p)) || pt.equals(p)) continue;
                    if (!DMisc.canTarget(le, le.getLocation())) continue;
                    targets.add(le);
                    confuse.add(pt);
                } else targets.add(le);
            }
        }
        if (targets.size() > 0) {
            for (LivingEntity le : targets) {
                Vector v = le.getLocation().toVector();
                Vector victor = p.getLocation().toVector().subtract(v);
                le.setVelocity(victor);
                DMisc.damageDemigods(p, le, damage, DamageCause.ENTITY_ATTACK);
            }
        }
        if (confuse.size() > 0) {
            for (Player pl : confuse) {
                (new PotionEffect(PotionEffectType.CONFUSION, confuseduration * 20, 4)).apply(pl);
            }
        }
        return targets.size();
    }

    @Override
    public void onSyncTick(long timeSent) {
    }

    @Override
    public boolean canTribute() {
        return true;
    }
}
