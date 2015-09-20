package com.demigodsrpg.norsedemigods.deity.jotunn;

import com.demigodsrpg.norsedemigods.NorseDemigods;
import com.demigodsrpg.norsedemigods.deity.Deity;
import com.demigodsrpg.norsedemigods.util.DMiscUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.UUID;

//TODO better replacement for BLAZE
public class FireGiant implements Deity {
    private static final long serialVersionUID = -6437607905225500420L;
    private final UUID PLAYER;
    private final int FIREBALLCOST = 100;
    private final int PROMETHEUSULTIMATECOST = 5500;
    private final int PROMETHEUSULTIMATECOOLDOWNMAX = 600; // seconds
    private final int PROMETHEUSULTIMATECOOLDOWNMIN = 60;
    private final int BLAZECOST = 400;
    private final double BLAZEDELAY = 15;

    private Material FIREBALLITEM = null;
    private Material BLAZEITEM = null;
    private boolean FIREBALL = false;
    private boolean BLAZE = false;
    private long FIRESTORMTIME;
    private long BLAZETIME;
    private long FIREBALLTIME;

    public FireGiant(UUID name) {
        PLAYER = name;
        FIRESTORMTIME = System.currentTimeMillis();
        BLAZETIME = System.currentTimeMillis();
        FIREBALLTIME = System.currentTimeMillis();
    }

    @Override
    public String getName() {
        return "Fire Giant";
    }

    public boolean getBlaze() {
        return BLAZE;
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
        if (DMiscUtil.hasDeity(p, "Fire Giant") && DMiscUtil.isFullParticipant(p)) {
            int devotion = 10000;
            /*
             * Calculate special values first
			 */
            int t = (int) (PROMETHEUSULTIMATECOOLDOWNMAX - ((PROMETHEUSULTIMATECOOLDOWNMAX - PROMETHEUSULTIMATECOOLDOWNMIN) * ((double) DMiscUtil.getAscensions(p) / 100)));
            int diameter = (int) Math.ceil(1.43 * Math.pow(devotion, 0.1527));
            if (diameter > 12) diameter = 12;
            int firestormshots = (int) Math.round(2 * Math.pow(40000, 0.15));
            /*
             * The printed text
			 */
            p.sendMessage("--" + ChatColor.GOLD + "Fire Giant");
            p.sendMessage(":Immune to fire damage.");
            p.sendMessage(":Shoot a fireball at the cursor's location. " + ChatColor.GREEN + "/fireball");
            p.sendMessage(ChatColor.YELLOW + "Costs " + FIREBALLCOST + " Favor.");
            if (((FireGiant) (DMiscUtil.getDeity(p, "Fire Giant"))).FIREBALLITEM != null)
                p.sendMessage(ChatColor.AQUA + "    Bound to " + ((FireGiant) (DMiscUtil.getDeity(p, "Fire Giant"))).FIREBALLITEM.name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to bind this skill to an item.");
            p.sendMessage(":Ignite the ground at the target location with diameter " + diameter + ". " + ChatColor.GREEN + "/blaze");
            p.sendMessage(ChatColor.YELLOW + "Costs " + BLAZECOST + " Favor. Cooldown time: " + BLAZEDELAY + " seconds.");
            if (((FireGiant) (DMiscUtil.getDeity(p, "Fire Giant"))).BLAZEITEM != null)
                p.sendMessage(ChatColor.AQUA + "    Bound to " + ((FireGiant) (DMiscUtil.getDeity(p, "Fire Giant"))).BLAZEITEM.name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to bind this skill to an item.");
            p.sendMessage(":Your fire power rains down on your enemies.");
            p.sendMessage("Shoots " + firestormshots + " fireballs. " + ChatColor.GREEN + "/firestorm");
            p.sendMessage(ChatColor.YELLOW + "Costs " + PROMETHEUSULTIMATECOST + " Favor. Cooldown time: " + t + " seconds.");
            return;
        }
        p.sendMessage("--" + ChatColor.GOLD + "Fire Giant");
        p.sendMessage("Passive: Immune to fire damage.");
        p.sendMessage("Active: Shoot a fireball. " + ChatColor.GREEN + "/fireball");
        p.sendMessage(ChatColor.YELLOW + "Costs " + FIREBALLCOST + " Favor. Can bind.");
        p.sendMessage("Active: Ignite the ground around the target." + ChatColor.GREEN + " /blaze ");
        p.sendMessage(ChatColor.YELLOW + "Costs " + BLAZECOST + " Favor. Can bind. Has cooldown.");
        p.sendMessage("Ultimate: Your fire power rains down on your enemies.");
        p.sendMessage(ChatColor.GREEN + "/firestorm" + ChatColor.YELLOW + " Costs " + PROMETHEUSULTIMATECOST + " Favor. Has cooldown.");
        p.sendMessage(ChatColor.YELLOW + "Select item: lighter (flint and steel)");
    }

    @Override
    public void onEvent(Event ee) {
        if (ee instanceof PlayerInteractEvent) {
            PlayerInteractEvent e = (PlayerInteractEvent) ee;
            Player p = e.getPlayer();
            if (!DMiscUtil.isFullParticipant(p)) return;
            if (!DMiscUtil.hasDeity(p, "Fire Giant")) return;
            if (FIREBALL || ((p.getItemInHand() != null) && (p.getItemInHand().getType() == FIREBALLITEM))) {
                if (System.currentTimeMillis() < FIREBALLTIME) return;
                if (DMiscUtil.getFavor(p) >= FIREBALLCOST) {
                    if (!DMiscUtil.canTarget(p, p.getLocation())) {
                        p.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
                        return;
                    }
                    DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) - FIREBALLCOST);
                    shootFireball(p.getEyeLocation(), DMiscUtil.getTargetLocation(p), p);
                    double FIREBALLDELAY = 0.5;
                    FIREBALLTIME = System.currentTimeMillis() + (long) (FIREBALLDELAY * 1000);
                } else {
                    FIREBALL = false;
                    p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor to do that.");
                }
            }
            if (BLAZE || ((p.getItemInHand() != null) && (p.getItemInHand().getType() == BLAZEITEM))) {
                if (System.currentTimeMillis() < BLAZETIME) {
                    p.sendMessage(ChatColor.YELLOW + "Blaze is on cooldown.");
                    return;
                }
                if (DMiscUtil.getFavor(p) >= BLAZECOST) {
                    if (!DMiscUtil.canTarget(p, p.getLocation())) {
                        p.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
                        return;
                    }
                    int diameter = (int) Math.ceil(1.43 * Math.pow(10000, 0.1527));
                    if (diameter > 12) diameter = 12;
                    if (DMiscUtil.canLocationPVP(DMiscUtil.getTargetLocation(p))) {
                        blaze(DMiscUtil.getTargetLocation(p), diameter);
                        DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) - BLAZECOST);
                        BLAZETIME = System.currentTimeMillis() + (long) (BLAZEDELAY * 1000);
                    } else p.sendMessage(ChatColor.YELLOW + "That is a protected area.");
                } else {
                    BLAZE = false;
                    p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor to do that.");
                }
            }
        }
    }

    @Override
    public void onCommand(Player P, String str, String[] args, boolean bind) {
        final Player p = P;
        if (!DMiscUtil.isFullParticipant(p)) return;
        if (!DMiscUtil.hasDeity(p, "Fire Giant")) return;
        if (str.equalsIgnoreCase("fireball")) {
            if (bind) {
                if (FIREBALLITEM == null) {
                    if (DMiscUtil.isBound(p, p.getItemInHand().getType()))
                        p.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                    if (p.getItemInHand() == null) p.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                    else {
                        DMiscUtil.registerBind(p, p.getItemInHand().getType());
                        FIREBALLITEM = p.getItemInHand().getType();
                        p.sendMessage(ChatColor.YELLOW + "Fireball is now bound to " + p.getItemInHand().getType().name() + ".");
                    }
                } else {
                    DMiscUtil.removeBind(p, FIREBALLITEM);
                    p.sendMessage(ChatColor.YELLOW + "Fireball is no longer bound to " + FIREBALLITEM.name() + ".");
                    FIREBALLITEM = null;
                }
                return;
            }
            if (FIREBALL) {
                FIREBALL = false;
                p.sendMessage(ChatColor.YELLOW + "Fireball is no longer active.");
            } else {
                FIREBALL = true;
                p.sendMessage(ChatColor.YELLOW + "Fireball is now active.");
            }
        } else if (str.equalsIgnoreCase("blaze")) {
            if (bind) {
                if (BLAZEITEM == null) {
                    if (DMiscUtil.isBound(p, p.getItemInHand().getType()))
                        p.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                    if (p.getItemInHand() == null) p.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                    else {
                        DMiscUtil.registerBind(p, p.getItemInHand().getType());
                        BLAZEITEM = p.getItemInHand().getType();
                        p.sendMessage(ChatColor.YELLOW + "Blaze is now bound to " + p.getItemInHand().getType().name() + ".");
                    }
                } else {
                    DMiscUtil.removeBind(p, BLAZEITEM);
                    p.sendMessage(ChatColor.YELLOW + "Blaze is no longer bound to " + BLAZEITEM.name() + ".");
                    BLAZEITEM = null;
                }
                return;
            }
            if (BLAZE) {
                BLAZE = false;
                p.sendMessage(ChatColor.YELLOW + "Blaze is no longer active.");
            } else {
                BLAZE = true;
                p.sendMessage(ChatColor.YELLOW + "Blaze is now active.");
            }
        } else if (str.equalsIgnoreCase("firestorm")) {
            if (System.currentTimeMillis() < FIRESTORMTIME) {
                p.sendMessage(ChatColor.YELLOW + "You cannot use the firestorm again for " + ((((FIRESTORMTIME) / 1000) - (System.currentTimeMillis() / 1000))) / 60 + " minutes");
                p.sendMessage(ChatColor.YELLOW + "and " + ((((FIRESTORMTIME) / 1000) - (System.currentTimeMillis() / 1000)) % 60) + " seconds.");
                return;
            }
            if (DMiscUtil.getFavor(p) >= PROMETHEUSULTIMATECOST) {
                if (!DMiscUtil.canTarget(p, p.getLocation())) {
                    p.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
                    return;
                }
                int t = (int) (PROMETHEUSULTIMATECOOLDOWNMAX - ((PROMETHEUSULTIMATECOOLDOWNMAX - PROMETHEUSULTIMATECOOLDOWNMIN) * ((double) DMiscUtil.getAscensions(p) / 100)));
                FIRESTORMTIME = System.currentTimeMillis() + (t * 1000);
                p.sendMessage(ChatColor.GOLD + "Your divine fire " + ChatColor.WHITE + " has unleashed his wrath on " + firestorm(p) + " non-allied entities,");
                p.sendMessage("in exchange for " + ChatColor.AQUA + PROMETHEUSULTIMATECOST + ChatColor.WHITE + " Favor.");
                DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) - PROMETHEUSULTIMATECOST);
            } else p.sendMessage("Firestorm requires " + PROMETHEUSULTIMATECOST + " Favor.");
        }
    }

    @Override
    public void onSyncTick(long timeSent) {

    }

    @Override
    public boolean canTribute() {
        return false;
    }

    private static void shootFireball(Location from, Location to, Player player) {
        player.getWorld().spawnEntity(from, EntityType.FIREBALL);
        for (Entity entity : player.getNearbyEntities(2, 2, 2)) {
            if (!(entity instanceof Fireball)) continue;

            Fireball fireball = (Fireball) entity;
            to.setX(to.getX() + .5);
            to.setY(to.getY() + .5);
            to.setZ(to.getZ() + .5);
            Vector path = to.toVector().subtract(from.toVector());
            Vector victor = from.toVector().add(from.getDirection().multiply(2));
            fireball.teleport(new Location(player.getWorld(), victor.getX(), victor.getY(), victor.getZ()));
            fireball.setDirection(path);
            fireball.setShooter(player);
            fireball.setMetadata("how_do_I_shot_web", new FixedMetadataValue(NorseDemigods.getPlugin(NorseDemigods.class), true));
        }
    }

    private void blaze(Location target, int diameter) {
        for (int x = -diameter / 2; x <= diameter / 2; x++) {
            for (int y = -diameter / 2; y <= diameter / 2; y++) {
                for (int z = -diameter / 2; z <= diameter / 2; z++) {
                    Block b = target.getWorld().getBlockAt(target.getBlockX() + x, target.getBlockY() + y, target.getBlockZ() + z);
                    if ((b.getType() == Material.AIR) || (((b.getType() == Material.SNOW)) && DMiscUtil.canLocationPVP(b.getLocation())))
                        b.setType(Material.FIRE);
                }
            }
        }
    }

    private int firestorm(Player p) {
        int total = 20 * (int) Math.round(2 * Math.pow(40000, 0.15));
        Vector ploc = p.getLocation().toVector();
        ArrayList<LivingEntity> entitylist = new ArrayList<LivingEntity>();
        for (LivingEntity anEntity : p.getWorld().getLivingEntities()) {
            if (anEntity instanceof Player) if (DMiscUtil.isFullParticipant((Player) anEntity))
                if (DMiscUtil.areAllied(p, (Player) anEntity)) continue;
            if (!DMiscUtil.canTarget(anEntity, anEntity.getLocation())) continue;
            if (anEntity.getLocation().toVector().isInSphere(ploc, 50)) entitylist.add(anEntity);
        }
        final Player pl = p;
        final ArrayList<LivingEntity> enList = entitylist;
        for (int i = 0; i <= total; i += 20) {
            DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    for (LivingEntity e1 : enList) {
                        Location up = new Location(e1.getWorld(), e1.getLocation().getX() + Math.random() * 5, 256, e1.getLocation().getZ() + Math.random() * 5);
                        up.setPitch(90);
                        shootFireball(up, new Location(e1.getWorld(), e1.getLocation().getX() + Math.random() * 5, e1.getLocation().getY(), e1.getLocation().getZ() + Math.random() * 5), pl);
                    }
                }
            }, i);
        }
        return enList.size();
    }
}
