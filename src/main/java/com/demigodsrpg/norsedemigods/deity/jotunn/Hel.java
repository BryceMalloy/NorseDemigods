package com.demigodsrpg.norsedemigods.deity.jotunn;

import com.demigodsrpg.norsedemigods.DMisc;
import com.demigodsrpg.norsedemigods.Deity;
import com.demigodsrpg.norsedemigods.Setting;
import com.demigodsrpg.norsedemigods.deity.AD;
import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class Hel implements Deity {

    /* General */
    private static final int CHAINCOST = 250;
    private static final int CHAINDELAY = 1500;
    private static final int ENTOMBCOST = 470;
    private static final int ENTOMBDELAY = 2000;
    private static final int ULTIMATECOST = 4000;
    private static final int ULTIMATECOOLDOWNMAX = 600;
    private static final int ULTIMATECOOLDOWNMIN = 320;

    @Override
    public String getName() {
        return "Hel";
    }

    @Override
    public String getDefaultAlliance() {
        return "Jotunn";
    }

    @Override
    public void printInfo(Player p) {
        if (DMisc.hasDeity(p, "Hel") && DMisc.isFullParticipant(p)) {
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            int devotion = DMisc.getDevotion(p, "Hel");
            /*
             * Calculate special values first
             */
            // chain
            int damage = (int) (Math.round(5 * Math.pow(devotion, 0.20688)));
            int blindpower = (int) Math.round(1.26985 * Math.pow(devotion, 0.13047));
            int blindduration = (int) Math.round(0.75 * Math.pow(devotion, 0.323999));
            // entomb
            int duration = (int) Math.round(2.18678 * Math.pow(devotion, 0.24723));
            // ult
            int ultrange = (int) Math.round(18.83043 * Math.pow(devotion, 0.088637));
            int ultduration = (int) Math.round(30 * Math.pow(DMisc.getDevotion(p, getName()), 0.09));
            int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) * ((double) DMisc.getAscensions(p) / 100)));
            /*
             * The printed text
             */
            p.sendMessage("--" + ChatColor.GOLD + "Hel" + ChatColor.GRAY + " [" + devotion + "]");
            p.sendMessage(":Immune to skeleton and zombie attacks.");
            p.sendMessage(":Entomb an entity in obsidian. " + ChatColor.GREEN + "/entomb");
            p.sendMessage(ChatColor.YELLOW + "Costs " + ENTOMBCOST + " Favor.");
            p.sendMessage("Duration: " + duration + " seconds.");
            if (save.getBind("entomb").isPresent())
                p.sendMessage(ChatColor.AQUA + "    Bound to " + save.getBind("entomb").get().name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to bind this skill to an item.");
            p.sendMessage(":Fire a chain of smoke, causing damage and darkness. " + ChatColor.GREEN + "/chain");
            p.sendMessage(ChatColor.YELLOW + "Costs " + CHAINCOST + " Favor.");
            p.sendMessage(damage + " damage, causes level " + blindpower + " darkness for " + blindduration + " seconds.");
            if (save.getBind("chain").isPresent())
                p.sendMessage(ChatColor.AQUA + "    Chain bound to " + save.getBind("chain").get().name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to bind this skill to an item.");
            p.sendMessage(":Turn day to night and curse your enemies.");
            p.sendMessage("Range: " + ultrange + ". Duration: " + ultduration + "" + ChatColor.GREEN + " /curse");
            p.sendMessage(ChatColor.YELLOW + "Costs " + ULTIMATECOST + " Favor. Cooldown time: " + t + " seconds.");
            return;
        }
        p.sendMessage("--" + ChatColor.GOLD + "Hel");
        p.sendMessage("Passive: Immune to skeleton and zombie attacks.");
        p.sendMessage("Active: Entomb an entity in obsidian. " + ChatColor.GREEN + "/entomb");
        p.sendMessage(ChatColor.YELLOW + "Costs " + ENTOMBCOST + " Favor. Can bind.");
        p.sendMessage("Active: Fire a chain of smoke that damages and blinds.");
        p.sendMessage(ChatColor.GREEN + "/chain " + ChatColor.YELLOW + "Costs " + CHAINCOST + " Favor. Can bind.");
        p.sendMessage("Ultimate: Turns day to night as Hel curses your enemies.");
        p.sendMessage(ChatColor.GREEN + "/curse " + ChatColor.YELLOW + "Costs " + ULTIMATECOST + " Favor. Has cooldown.");
        p.sendMessage(ChatColor.YELLOW + "Select item: bone");
    }

    @Override
    public void onEvent(Event ee) {
        if (ee instanceof PlayerInteractEvent) {
            PlayerInteractEvent e = (PlayerInteractEvent) ee;
            Player p = e.getPlayer();
            if (!DMisc.isFullParticipant(p)) return;
            if (!DMisc.hasDeity(p, "Hel")) return;
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            if (save.getAbilityData("chain", AD.ACTIVE, false) || save.getBind("chain").isPresent() &&
                    p.getItemInHand().getType() == save.getBind("chain").get()) {
                if (save.getAbilityData("chain", AD.TIME, (double) System.currentTimeMillis()) > System.currentTimeMillis())
                    return;
                if (DMisc.getFavor(p) >= CHAINCOST) {
                    int devotion = DMisc.getDevotion(p, "Hel");
                    int damage = (int) (Math.round(5 * Math.pow(devotion, 0.20688)));
                    int blindpower = (int) Math.round(1.26985 * Math.pow(devotion, 0.13047));
                    int blindduration = (int) Math.round(0.75 * Math.pow(devotion, 0.323999));
                    if (chain(p, damage, blindpower, blindduration)) {
                        save.setAbilityData("chain", AD.TIME, System.currentTimeMillis() + CHAINDELAY);
                        DMisc.setFavor(p, DMisc.getFavor(p) - CHAINCOST);
                    } else p.sendMessage(ChatColor.YELLOW + "No target found.");
                } else {
                    save.setAbilityData("chain", AD.ACTIVE, false);
                    p.sendMessage(ChatColor.YELLOW + "You don't have enough Favor to do that.");
                }
            }
            if (save.getAbilityData("entomb", AD.ACTIVE, false) || save.getBind("entomb").isPresent() &&
                    p.getItemInHand().getType() == save.getBind("chain").get()) {
                if (save.getAbilityData("chain", AD.TIME, (double) System.currentTimeMillis()) > System.currentTimeMillis())
                    return;
                if ((DMisc.getFavor(p) >= ENTOMBCOST)) {
                    if (entomb(p)) {
                        save.setAbilityData("entomb", AD.TIME, System.currentTimeMillis() + ENTOMBDELAY);
                        DMisc.setFavor(p, DMisc.getFavor(p) - ENTOMBCOST);
                    } else p.sendMessage(ChatColor.YELLOW + "No target found or area is protected.");
                } else {
                    save.setAbilityData("entomb", AD.ACTIVE, false);
                    p.sendMessage(ChatColor.YELLOW + "You don't have enough Favor to do that.");
                }
            }
        } else if (ee instanceof EntityTargetEvent) {
            EntityTargetEvent e = (EntityTargetEvent) ee;
            if (e.getEntity() instanceof LivingEntity) {
                if ((e.getEntity() instanceof Zombie) || (e.getEntity() instanceof Skeleton)) {
                    if (!DMisc.hasDeity((Player) e.getTarget(), "Hel")) return;
                    e.setCancelled(true);
                }
            }
        }
    }

    @Override
    public void onCommand(Player P, String str, String[] args, boolean bind) {
        if (!DMisc.isFullParticipant(P)) return;
        if (!DMisc.hasDeity(P, "Hel")) return;
        PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(P);
        if (str.equalsIgnoreCase("chain")) {
            if (bind) {
                if (!save.getBind("chain").isPresent()) {
                    if (DMisc.isBound(P, P.getItemInHand().getType()))
                        P.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                    if (P.getItemInHand().getType() == Material.AIR)
                        P.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                    else {
                        save.setBind("chain", P.getItemInHand().getType());
                        P.sendMessage(ChatColor.YELLOW + "Dark chain is now bound to " + P.getItemInHand().getType().name() + ".");
                    }
                } else {
                    P.sendMessage(ChatColor.YELLOW + "Dark chain is no longer bound to " + save.getBind("chain").get().name() + ".");
                    save.removeBind("chain");
                }
                return;
            }
            if (save.getAbilityData("chain", AD.ACTIVE, false)) {
                save.setAbilityData("chain", AD.ACTIVE, false);
                P.sendMessage(ChatColor.YELLOW + "Dark chain is no longer active.");
            } else {
                save.setAbilityData("chain", AD.ACTIVE, true);
                P.sendMessage(ChatColor.YELLOW + "Dark chain is now active.");
            }
        } else if (str.equalsIgnoreCase("entomb")) {
            if (bind) {
                if (!save.getBind("entomb").isPresent()) {
                    if (DMisc.isBound(P, P.getItemInHand().getType()))
                        P.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                    if (P.getItemInHand().getType() == Material.AIR)
                        P.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                    else {
                        save.setBind("entomb", P.getItemInHand().getType());
                        P.sendMessage(ChatColor.YELLOW + "Entomb is now bound to " + P.getItemInHand().getType().name() + ".");
                    }
                } else {
                    P.sendMessage(ChatColor.YELLOW + "Entomb is no longer bound to " + save.getBind("entomb").get().name() + ".");
                    save.removeBind("entomb");
                }
                return;
            }
            if (save.getAbilityData("entomb", AD.ACTIVE, false)) {
                save.setAbilityData("entomb", AD.ACTIVE, false);
                P.sendMessage(ChatColor.YELLOW + "Entomb is no longer active.");
            } else {
                save.setAbilityData("entomb", AD.ACTIVE, true);
                P.sendMessage(ChatColor.YELLOW + "Entomb is now active.");
            }
        } else if (str.equalsIgnoreCase("curse")) {
            double TIME = save.getAbilityData("curse", AD.TIME, (double) System.currentTimeMillis());
            if (System.currentTimeMillis() < TIME) {
                P.sendMessage(ChatColor.YELLOW + "You cannot use curse again for " + ((((TIME) / 1000) - (System.currentTimeMillis() / 1000))) / 60 + " minutes");
                P.sendMessage(ChatColor.YELLOW + "and " + ((((TIME) / 1000) - (System.currentTimeMillis() / 1000)) % 60) + " seconds.");
                return;
            }
            if (DMisc.getFavor(P) >= ULTIMATECOST) {
                if (!DMisc.canTarget(P, P.getLocation())) {
                    P.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
                    return;
                }
                int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) * ((double) DMisc.getAscensions(P) / 100)));
                int amt = tartarus(P);
                if (amt > 0) {
                    P.sendMessage(ChatColor.DARK_RED + "Hel" + ChatColor.GRAY + " curses " + amt + " enemies.");
                    DMisc.setFavor(P, DMisc.getFavor(P) - ULTIMATECOST);
                    P.getWorld().setTime(18000);
                    save.setAbilityData("curse", AD.TIME, System.currentTimeMillis() + t * 1000);
                } else
                    P.sendMessage(ChatColor.YELLOW + "There were no valid targets or the ultimate could not be used.");
            } else P.sendMessage(ChatColor.YELLOW + "Curse requires " + ULTIMATECOST + " Favor.");
        }
    }

    private boolean chain(Player p, int damage, int blindpower, int blindduration) {
        if (!DMisc.canTarget(p, p.getLocation())) return false;
        LivingEntity target = DMisc.getTargetLivingEntity(p, 3);
        if (target == null) return false;
        if (!DMisc.canTarget(target, target.getLocation())) {
            return false;
        }
        if (Setting.FRIENDLY_FIRE && target instanceof Player && DMisc.areAllied(p, (Player) target)) {
            if (Setting.FRIENDLY_FIRE_WARNING)
                p.sendMessage(ChatColor.YELLOW + "No friendly fire.");
            return false;
        }
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindduration, blindpower));
        DMisc.damageDemigods(p, target, damage, DamageCause.ENTITY_ATTACK);
        for (BlockFace bf : BlockFace.values()) {
            p.getWorld().playEffect(target.getLocation().getBlock().getRelative(bf).getLocation(), Effect.SMOKE, 1);
        }
        return true;
    }

    private boolean entomb(Player p) {
        LivingEntity le = DMisc.getTargetLivingEntity(p, 2);
        if (le == null) return false;
        if (!DMisc.canTarget(p, p.getLocation()) || !DMisc.canTarget(le, le.getLocation())) return false;
        int duration = (int) Math.round(2.18678 * Math.pow(DMisc.getDevotion(p, "Hel"), 0.24723)); // seconds
        final ArrayList<Block> tochange = new ArrayList<>();
        for (int x = -3; x <= 3; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    Block block = p.getWorld().getBlockAt(le.getLocation().getBlockX() + x, le.getLocation().getBlockY() + y, le.getLocation().getBlockZ() + z);
                    if ((block.getLocation().distance(le.getLocation()) > 2) && (block.getLocation().distance(le.getLocation()) < 3.5))
                        if ((block.getType() == Material.AIR) || (block.getType() == Material.WATER) || (block.getType() == Material.LAVA)) {
                            block.setType(Material.OBSIDIAN);
                            tochange.add(block);
                        }
                }
            }
        }
        DMisc.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMisc.getPlugin(), () -> {
            for (Block b : tochange)
                if (b.getType() == Material.OBSIDIAN) b.setType(Material.AIR);
        }, duration * 20);
        return true;
    }

    private int tartarus(Player p) {
        int range = (int) Math.round(18.83043 * Math.pow(DMisc.getDevotion(p, "Hel"), 0.088637));
        ArrayList<LivingEntity> entitylist = new ArrayList<>();
        Vector ploc = p.getLocation().toVector();
        for (LivingEntity anEntity : p.getWorld().getLivingEntities()) {
            if (anEntity instanceof Player) if (DMisc.isFullParticipant((Player) anEntity))
                if (DMisc.areAllied((Player) anEntity, p)) continue;
            if (anEntity.getLocation().toVector().isInSphere(ploc, range)) entitylist.add(anEntity);
        }
        int duration = (int) Math.round(30 * Math.pow(DMisc.getDevotion(p, getName()), 0.09)) * 20;
        for (LivingEntity le : entitylist)
            target(le, duration);
        return entitylist.size();
    }

    private void target(LivingEntity le, int duration) {
        le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, duration, 5));
        le.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, duration, 5));
        le.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, duration, 5));
        le.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, duration, 5));
    }

    @Override
    public void onSyncTick(long timeSent) {
    }

    @Override
    public boolean canTribute() {
        return true;
    }
}
