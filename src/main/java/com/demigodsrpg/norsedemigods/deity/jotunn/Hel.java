package com.demigodsrpg.norsedemigods.deity.jotunn;

import com.demigodsrpg.norsedemigods.Deity;
import com.demigodsrpg.norsedemigods.util.DMiscUtil;
import com.demigodsrpg.norsedemigods.util.DSettings;
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
import java.util.UUID;

public class Hel implements Deity {

    /* General */
    private static final long serialVersionUID = 3647481847975286534L;
    private static final int CHAINCOST = 250;
    private static final int CHAINDELAY = 1500;
    private static final int ENTOMBCOST = 470;
    private static final int ENTOMBDELAY = 2000;
    private static final int ULTIMATECOST = 4000;
    private static final int ULTIMATECOOLDOWNMAX = 600;
    private static final int ULTIMATECOOLDOWNMIN = 320;

    /* Specific to player */
    private final UUID PLAYER;
    private boolean CHAIN = false;
    private boolean ENTOMB = false;
    private long CHAINTIME, ENTOMBTIME, ULTIMATETIME;
    private Material CHAINBIND = null;
    private Material ENTOMBBIND = null;

    public Hel(UUID name) {
        PLAYER = name;
        CHAINTIME = System.currentTimeMillis();
        ENTOMBTIME = System.currentTimeMillis();
        ULTIMATETIME = System.currentTimeMillis();
    }

    @Override
    public String getName() {
        return "Hel";
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
        if (DMiscUtil.hasDeity(p, "Hel") && DMiscUtil.isFullParticipant(p)) {
            int devotion = DMiscUtil.getDevotion(p, "Hel");
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
            int ultduration = (int) Math.round(30 * Math.pow(DMiscUtil.getDevotion(p, getName()), 0.09));
            int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) * ((double) DMiscUtil.getAscensions(p) / 100)));
            /*
			 * The printed text
			 */
            p.sendMessage("--" + ChatColor.GOLD + "Hel" + ChatColor.GRAY + " [" + devotion + "]");
            p.sendMessage(":Immune to skeleton and zombie attacks.");
            p.sendMessage(":Entomb an entity in obsidian. " + ChatColor.GREEN + "/entomb");
            p.sendMessage(ChatColor.YELLOW + "Costs " + ENTOMBCOST + " Favor.");
            p.sendMessage("Duration: " + duration + " seconds.");
            if (((Hel) (DMiscUtil.getDeity(p, "Hel"))).ENTOMBBIND != null)
                p.sendMessage(ChatColor.AQUA + "    Bound to " + (((Hel) (DMiscUtil.getDeity(p, "Hel"))).ENTOMBBIND).name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to bind this skill to an item.");
            p.sendMessage(":Fire a chain of smoke, causing damage and darkness. " + ChatColor.GREEN + "/chain");
            p.sendMessage(ChatColor.YELLOW + "Costs " + CHAINCOST + " Favor.");
            p.sendMessage(damage + " damage, causes level " + blindpower + " darkness for " + blindduration + " seconds.");
            if (((Hel) (DMiscUtil.getDeity(p, "Hel"))).CHAINBIND != null)
                p.sendMessage(ChatColor.AQUA + "    Chain bound to " + (((Hel) (DMiscUtil.getDeity(p, "Hel"))).CHAINBIND).name());
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
            if (!DMiscUtil.isFullParticipant(p)) return;
            if (!DMiscUtil.hasDeity(p, "Hel")) return;
            if (CHAIN || ((CHAINBIND != null) && (p.getItemInHand().getType() == CHAINBIND))) {
                if (System.currentTimeMillis() < CHAINTIME) return;
                if (DMiscUtil.getFavor(p) >= CHAINCOST) {
                    int devotion = DMiscUtil.getDevotion(p, "Hel");
                    int damage = (int) (Math.round(5 * Math.pow(devotion, 0.20688)));
                    int blindpower = (int) Math.round(1.26985 * Math.pow(devotion, 0.13047));
                    int blindduration = (int) Math.round(0.75 * Math.pow(devotion, 0.323999));
                    if (chain(p, damage, blindpower, blindduration)) {
                        CHAINTIME = System.currentTimeMillis() + CHAINDELAY;
                        DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) - CHAINCOST);
                    } else p.sendMessage(ChatColor.YELLOW + "No target found.");
                } else {
                    CHAIN = false;
                    p.sendMessage(ChatColor.YELLOW + "You don't have enough Favor to do that.");
                }
            }
            if (ENTOMB || ((ENTOMBBIND != null) && (p.getItemInHand().getType() == ENTOMBBIND))) {
                if (System.currentTimeMillis() < ENTOMBTIME) return;
                if ((DMiscUtil.getFavor(p) >= ENTOMBCOST)) {
                    if (entomb(p)) {
                        ENTOMBTIME = System.currentTimeMillis() + ENTOMBDELAY;
                        DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) - ENTOMBCOST);
                    } else p.sendMessage(ChatColor.YELLOW + "No target found or area is protected.");
                } else {
                    ENTOMB = false;
                    p.sendMessage(ChatColor.YELLOW + "You don't have enough Favor to do that.");
                }
            }
        } else if (ee instanceof EntityTargetEvent) {
            EntityTargetEvent e = (EntityTargetEvent) ee;
            if (e.getEntity() instanceof LivingEntity) {
                if ((e.getEntity() instanceof Zombie) || (e.getEntity() instanceof Skeleton)) {
                    if (!DMiscUtil.hasDeity((Player) e.getTarget(), "Hel")) return;
                    e.setCancelled(true);
                }
            }
        }
    }

    @Override
    public void onCommand(Player P, String str, String[] args, boolean bind) {
        final Player p = P;
        if (!DMiscUtil.isFullParticipant(p)) return;
        if (!DMiscUtil.hasDeity(p, "Hel")) return;
        if (str.equalsIgnoreCase("chain")) {
            if (bind) {
                if (CHAINBIND == null) {
                    if (DMiscUtil.isBound(p, p.getItemInHand().getType()))
                        p.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                    if (p.getItemInHand().getType() == Material.AIR)
                        p.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                    else {
                        DMiscUtil.registerBind(p, p.getItemInHand().getType());
                        CHAINBIND = p.getItemInHand().getType();
                        p.sendMessage(ChatColor.YELLOW + "Dark chain is now bound to " + p.getItemInHand().getType().name() + ".");
                    }
                } else {
                    DMiscUtil.removeBind(p, CHAINBIND);
                    p.sendMessage(ChatColor.YELLOW + "Dark chain is no longer bound to " + CHAINBIND.name() + ".");
                    CHAINBIND = null;
                }
                return;
            }
            if (CHAIN) {
                CHAIN = false;
                p.sendMessage(ChatColor.YELLOW + "Dark chain is no longer active.");
            } else {
                CHAIN = true;
                p.sendMessage(ChatColor.YELLOW + "Dark chain is now active.");
            }
        } else if (str.equalsIgnoreCase("entomb")) {
            if (bind) {
                if (ENTOMBBIND == null) {
                    if (DMiscUtil.isBound(p, p.getItemInHand().getType()))
                        p.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                    if (p.getItemInHand().getType() == Material.AIR)
                        p.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                    else {
                        DMiscUtil.registerBind(p, p.getItemInHand().getType());
                        ENTOMBBIND = p.getItemInHand().getType();
                        p.sendMessage(ChatColor.YELLOW + "Entomb is now bound to " + p.getItemInHand().getType().name() + ".");
                    }
                } else {
                    DMiscUtil.removeBind(p, ENTOMBBIND);
                    p.sendMessage(ChatColor.YELLOW + "Entomb is no longer bound to " + ENTOMBBIND.name() + ".");
                    ENTOMBBIND = null;
                }
                return;
            }
            if (ENTOMB) {
                ENTOMB = false;
                p.sendMessage(ChatColor.YELLOW + "Entomb is no longer active.");
            } else {
                ENTOMB = true;
                p.sendMessage(ChatColor.YELLOW + "Entomb is now active.");
            }
        } else if (str.equalsIgnoreCase("curse")) {
            long TIME = ULTIMATETIME;
            if (System.currentTimeMillis() < TIME) {
                p.sendMessage(ChatColor.YELLOW + "You cannot use curse again for " + ((((TIME) / 1000) - (System.currentTimeMillis() / 1000))) / 60 + " minutes");
                p.sendMessage(ChatColor.YELLOW + "and " + ((((TIME) / 1000) - (System.currentTimeMillis() / 1000)) % 60) + " seconds.");
                return;
            }
            if (DMiscUtil.getFavor(p) >= ULTIMATECOST) {
                if (!DMiscUtil.canTarget(p, p.getLocation())) {
                    p.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
                    return;
                }
                int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) * ((double) DMiscUtil.getAscensions(p) / 100)));
                int amt = tartarus(p);
                if (amt > 0) {
                    p.sendMessage(ChatColor.DARK_RED + "Hel" + ChatColor.GRAY + " curses " + amt + " enemies.");
                    DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) - ULTIMATECOST);
                    p.getWorld().setTime(18000);
                    ULTIMATETIME = System.currentTimeMillis() + t * 1000;
                } else
                    p.sendMessage(ChatColor.YELLOW + "There were no valid targets or the ultimate could not be used.");
            } else p.sendMessage(ChatColor.YELLOW + "Curse requires " + ULTIMATECOST + " Favor.");
        }
    }

    private boolean chain(Player p, int damage, int blindpower, int blindduration) {
        if (!DMiscUtil.canTarget(p, p.getLocation())) return false;
        LivingEntity target = DMiscUtil.getTargetLivingEntity(p, 3);
        if (target == null) return false;
        if (!DMiscUtil.canTarget(target, target.getLocation())) {
            return false;
        }
        if (DSettings.getSettingBoolean("friendly_fire") && target instanceof Player && DMiscUtil.areAllied(p, (Player) target)) {
            if (DSettings.getSettingBoolean("friendly_fire_message"))
                p.sendMessage(ChatColor.YELLOW + "No friendly fire.");
            return false;
        }
        target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, blindduration, blindpower));
        DMiscUtil.damageDemigods(p, target, damage, DamageCause.ENTITY_ATTACK);
        for (BlockFace bf : BlockFace.values()) {
            p.getWorld().playEffect(target.getLocation().getBlock().getRelative(bf).getLocation(), Effect.SMOKE, 1);
        }
        return true;
    }

    private boolean entomb(Player p) {
        LivingEntity le = DMiscUtil.getTargetLivingEntity(p, 2);
        if (le == null) return false;
        if (!DMiscUtil.canTarget(p, p.getLocation()) || !DMiscUtil.canTarget(le, le.getLocation())) return false;
        int duration = (int) Math.round(2.18678 * Math.pow(DMiscUtil.getDevotion(p, "Hel"), 0.24723)); // seconds
        final ArrayList<Block> tochange = new ArrayList<Block>();
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
        DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
            @Override
            public void run() {
                for (Block b : tochange)
                    if (b.getType() == Material.OBSIDIAN) b.setType(Material.AIR);
            }
        }, duration * 20);
        return true;
    }

    private int tartarus(Player p) {
        int range = (int) Math.round(18.83043 * Math.pow(DMiscUtil.getDevotion(p, "Hel"), 0.088637));
        ArrayList<LivingEntity> entitylist = new ArrayList<LivingEntity>();
        Vector ploc = p.getLocation().toVector();
        for (LivingEntity anEntity : p.getWorld().getLivingEntities()) {
            if (anEntity instanceof Player) if (DMiscUtil.isFullParticipant((Player) anEntity))
                if (DMiscUtil.areAllied((Player) anEntity, p)) continue;
            if (anEntity.getLocation().toVector().isInSphere(ploc, range)) entitylist.add(anEntity);
        }
        int duration = (int) Math.round(30 * Math.pow(DMiscUtil.getDevotion(p, getName()), 0.09)) * 20;
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
