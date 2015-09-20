package com.demigodsrpg.norsedemigods.deity.aesir;

import com.demigodsrpg.norsedemigods.deity.Deity;
import com.demigodsrpg.norsedemigods.util.DMiscUtil;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.UUID;

public class Baldr implements Deity {
    private static final long serialVersionUID = -2472769863144336856L;
    private final UUID PLAYER;

    private static final int SKILLCOST = 200;
    private static final int SKILLDELAY = 1250; // milliseconds
    private static final int ULTIMATECOST = 6500;
    private static final int ULTIMATECOOLDOWNMAX = 600; // seconds
    private static final int ULTIMATECOOLDOWNMIN = 300;

    private static final String skillname = "Starfall";
    private static final String passivename = "Sprint";
    private static final String ult = "Smite";

    private boolean SKILL = false;
    private Material SKILLBIND = null;
    private long SKILLTIME;
    private boolean PASSIVE = false;
    private long ULTIMATETIME;
    private long LASTCHECK;

    public Baldr(UUID player) {
        PLAYER = player;
        SKILLTIME = System.currentTimeMillis();
        ULTIMATETIME = System.currentTimeMillis();
        LASTCHECK = System.currentTimeMillis();
    }

    @Override
    public String getName() {
        return "Baldr";
    }

    @Override
    public UUID getPlayerId() {
        return PLAYER;
    }

    @Override
    public String getDefaultAlliance() {
        return "AEsir";
    }

    @Override
    public void printInfo(Player p) {
        if (DMiscUtil.isFullParticipant(p) && DMiscUtil.hasDeity(p, getName())) {
            int devotion = DMiscUtil.getDevotion(p, getName());
            /*
             * Calculate special values first
			 */
            // starfall
            int damage = (int) (Math.round(1.4 * Math.pow(devotion, 0.3)));
            int range = (int) (Math.ceil(8 * Math.pow(devotion, 0.08)));
            // ult
            int numtargets = (int) Math.round(10 * Math.pow(devotion, 0.11));
            int igniteduration = (int) Math.round(5 * Math.pow(devotion, 0.15));
            int ultrange = (int) Math.round(25 * Math.pow(devotion, 0.09));
            int ultdamage = (int) (Math.floor(10 * Math.pow(devotion, 0.105)));
            int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) * ((double) DMiscUtil.getAscensions(p) / DMiscUtil.ASCENSIONCAP)));
            /*
             * The printed text
			 */
            p.sendMessage("--" + ChatColor.GOLD + getName() + ChatColor.GRAY + "[" + devotion + "]");
            p.sendMessage(":Move with increased speed while in a well-lit area (use" + ChatColor.GREEN + " /sprint " + ChatColor.YELLOW + "to toggle).");
            p.sendMessage(":Left-click to call down an attack dealing " + damage + " in radius " + range + "." + ChatColor.GREEN + " /starfall " + ChatColor.YELLOW + "Costs " + SKILLCOST + " Favor.");
            if (((Baldr) DMiscUtil.getDeity(p, getName())).SKILLBIND != null)
                p.sendMessage(ChatColor.AQUA + "    Bound to " + ((Baldr) DMiscUtil.getDeity(p, getName())).SKILLBIND.name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to bind this skill to an item.");
            p.sendMessage("Ignite up to " + numtargets + " enemies in range " + ultrange + " for " + igniteduration + " seconds, then");
            p.sendMessage("attack them for " + ultdamage + " damage." + ChatColor.GREEN + " /smite");
            p.sendMessage(ChatColor.YELLOW + "Costs " + ULTIMATECOST + " Favor. Cooldown time: " + t + " seconds.");
            return;
        }
        p.sendMessage("--" + getName());
        p.sendMessage("Passive: Move with increased speed while in a bright area.");
        p.sendMessage("Active: Damage nearby enemies with strikes from above. " + ChatColor.GREEN + "/starfall");
        p.sendMessage(ChatColor.YELLOW + "Costs " + SKILLCOST + " Favor. Can bind.");
        p.sendMessage("Ultimate: Ignite nearby enemies with the power of the sun, then");
        p.sendMessage("attack for a killing blow. " + ChatColor.GREEN + "/smite " + ChatColor.YELLOW + "Costs " + ULTIMATECOST + " Favor. Has cooldown.");
        p.sendMessage(ChatColor.YELLOW + "Select item: flower");
    }

    @Override
    public void onEvent(Event ee) {
        if (ee instanceof PlayerInteractEvent) {
            PlayerInteractEvent e = (PlayerInteractEvent) ee;
            Player p = e.getPlayer();
            if (!DMiscUtil.isFullParticipant(p) || !DMiscUtil.hasDeity(p, getName())) return;
            if (SKILL || ((p.getItemInHand() != null) && (p.getItemInHand().getType() == SKILLBIND))) {
                if (SKILLTIME > System.currentTimeMillis()) return;
                SKILLTIME = System.currentTimeMillis() + SKILLDELAY;
                if (DMiscUtil.getFavor(p) >= SKILLCOST) {
                    if (starfall(p) > 0) DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) - SKILLCOST);
                    else p.sendMessage(ChatColor.YELLOW + "No targets found.");
                } else {
                    p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor.");
                    SKILL = false;
                }
            }
        } else if (ee instanceof PlayerMoveEvent) {
            PlayerMoveEvent move = (PlayerMoveEvent) ee;
            Player p = move.getPlayer();
            if (!DMiscUtil.isFullParticipant(p)) return;
            if (!DMiscUtil.hasDeity(p, "Baldr")) return;
            // KENYANS
            if (PASSIVE) {
                Block playerBlock = p.getLocation().getBlock();
                if (!playerBlock.getType().equals(Material.STATIONARY_WATER) && !playerBlock.getType().equals(Material.WATER) && !playerBlock.getType().equals(Material.STATIONARY_LAVA) && !playerBlock.getType().equals(Material.LAVA) && !playerBlock.getRelative(BlockFace.DOWN).getType().equals(Material.AIR)) {
                    Vector dir = p.getLocation().getDirection().normalize().multiply(1.3D);
                    Vector vec = new Vector(dir.getX(), dir.getY(), dir.getZ());
                    if (p.isSneaking() && playerBlock.getLightLevel() > 12) p.setVelocity(vec);
                }
            }
        }
    }

    @Override
    public void onCommand(Player P, String str, String[] args, boolean bind) {
        final Player p = P;
        if (DMiscUtil.hasDeity(p, getName())) {
            if (str.equalsIgnoreCase(skillname)) {
                if (bind) {
                    if (SKILLBIND == null) {
                        if (DMiscUtil.isBound(p, p.getItemInHand().getType()))
                            p.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                        if (p.getItemInHand().getType() == Material.AIR)
                            p.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                        else {
                            DMiscUtil.registerBind(p, p.getItemInHand().getType());
                            SKILLBIND = p.getItemInHand().getType();
                            p.sendMessage(ChatColor.YELLOW + "" + skillname + " is now bound to " + p.getItemInHand().getType().name() + ".");
                        }
                    } else {
                        DMiscUtil.removeBind(p, SKILLBIND);
                        p.sendMessage(ChatColor.YELLOW + "" + skillname + " is no longer bound to " + SKILLBIND.name() + ".");
                        SKILLBIND = null;
                    }
                    return;
                }
                if (SKILL) {
                    SKILL = false;
                    p.sendMessage(ChatColor.YELLOW + "" + skillname + " is no longer active.");
                } else {
                    SKILL = true;
                    p.sendMessage(ChatColor.YELLOW + "" + skillname + " is now active.");
                }
            } else if (str.equalsIgnoreCase(passivename)) {
                if (PASSIVE) {
                    PASSIVE = false;
                    p.sendMessage(ChatColor.YELLOW + "" + passivename + " is no longer active.");
                } else {
                    PASSIVE = true;
                    p.sendMessage(ChatColor.YELLOW + "" + passivename + " is now active.");
                }
            } else if (str.equalsIgnoreCase(ult)) {
                long TIME = ULTIMATETIME;
                if (System.currentTimeMillis() < TIME) {
                    p.sendMessage(ChatColor.YELLOW + "You cannot use " + ult + " again for " + ((((TIME) / 1000) - (System.currentTimeMillis() / 1000))) / 60 + " minutes");
                    p.sendMessage(ChatColor.YELLOW + "and " + ((((TIME) / 1000) - (System.currentTimeMillis() / 1000)) % 60) + " seconds.");
                    return;
                }
                if (DMiscUtil.getFavor(p) >= ULTIMATECOST) {
                    if (!DMiscUtil.canTarget(p, p.getLocation())) {
                        p.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
                        return;
                    }
                    int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) * ((double) DMiscUtil.getAscensions(p) / DMiscUtil.ASCENSIONCAP)));
                    ULTIMATETIME = System.currentTimeMillis() + (t * 1000);
                    int num = smite(p);
                    if (num > 0) {
                        p.sendMessage("In exchange for " + ChatColor.AQUA + ULTIMATECOST + ChatColor.WHITE + " Favor, " + ChatColor.GOLD + "Baldr" + ChatColor.WHITE + " has struck " + num + " targets.");
                        DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) - ULTIMATECOST);
                    } else p.sendMessage(ChatColor.YELLOW + "No targets found.");
                } else p.sendMessage(ChatColor.YELLOW + "" + ult + " requires " + ULTIMATECOST + " Favor.");
            }
        }
    }

    @Override
    public void onTick(long timeSent) {
        if (timeSent > LASTCHECK + 1000) {
            LASTCHECK = timeSent;
        }
    }

    private int starfall(final Player p) {
        if (!DMiscUtil.canTarget(p, p.getLocation())) {
            p.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
            return 0;
        }
        int damage = (int) (Math.round(1.4 * Math.pow(DMiscUtil.getDevotion(p, getName()), 0.3)));
        int range = (int) (Math.ceil(8 * Math.pow(DMiscUtil.getDevotion(p, getName()), 0.08)));
        ArrayList<LivingEntity> entitylist = new ArrayList<LivingEntity>();
        Vector ploc = p.getLocation().toVector();
        for (LivingEntity anEntity : p.getWorld().getLivingEntities()) {
            if (anEntity instanceof Player) if (DMiscUtil.isFullParticipant((Player) anEntity))
                if (DMiscUtil.getAllegiance((Player) anEntity).equalsIgnoreCase(getDefaultAlliance())) continue;
            if (!DMiscUtil.canTarget(anEntity, anEntity.getLocation())) continue;
            if (anEntity.getLocation().toVector().isInSphere(ploc, range)) entitylist.add(anEntity);
        }
        for (LivingEntity le : entitylist) {
            for (int i = 0; i < 12; i++) {
                for (int j = 0; j < 3; j++) {
                    Location loc = le.getLocation();
                    loc.setY(le.getLocation().getBlockY() + i);
                    le.getWorld().playEffect(loc, Effect.SMOKE, (int) (Math.random() * 16));
                }
            }
            DMiscUtil.damageDemigods(p, le, damage, DamageCause.CUSTOM);
        }
        return entitylist.size();
    }

    private int smite(final Player p) {
        int devotion = DMiscUtil.getDevotion(p, getName());
        int numtargets = (int) Math.round(10 * Math.pow(devotion, 0.11));
        int igniteduration = (int) Math.round(5 * Math.pow(devotion, 0.15));
        int ultrange = (int) Math.round(25 * Math.pow(devotion, 0.09));
        final int ultdamage = (int) (Math.floor(10 * Math.pow(devotion, 0.105)));
        ArrayList<LivingEntity> entitylist = new ArrayList<LivingEntity>();
        Vector ploc = p.getLocation().toVector();
        for (LivingEntity anEntity : p.getWorld().getLivingEntities()) {
            if (anEntity instanceof Player) if (DMiscUtil.isFullParticipant((Player) anEntity))
                if (DMiscUtil.getAllegiance((Player) anEntity).equalsIgnoreCase(getDefaultAlliance())) continue;
            if (!DMiscUtil.canTarget(anEntity, anEntity.getLocation())) continue;
            if (anEntity.getLocation().toVector().isInSphere(ploc, ultrange) && (entitylist.size() < numtargets))
                entitylist.add(anEntity);
        }
        final Location start = p.getLocation();
        int delay = 0;
        for (final LivingEntity le : entitylist) {
            delay += 20;
            le.setFireTicks(igniteduration * 20);
            DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    DMiscUtil.horseTeleport(p, le.getLocation());
                    DMiscUtil.damageDemigods(p, le, ultdamage, DamageCause.CUSTOM);
                }
            }, delay);
        }
        DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
            @Override
            public void run() {
                DMiscUtil.horseTeleport(p, start);
            }
        }, 20 * entitylist.size() + 20);
        return entitylist.size();
    }

    @Override
    public boolean canTribute() {
        return true;
    }
}
