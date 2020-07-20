package com.demigodsrpg.norsedemigods.deity.jotunn;

import com.demigodsrpg.norsedemigods.DMisc;
import com.demigodsrpg.norsedemigods.Deity;
import com.demigodsrpg.norsedemigods.NorseDemigods;
import com.demigodsrpg.norsedemigods.deity.AD;
import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
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

//TODO better replacement for BLAZE
public class FireGiant implements Deity {
    private final int FIREBALLCOST = 100;
    private final int PROMETHEUSULTIMATECOST = 5500;
    private final int PROMETHEUSULTIMATECOOLDOWNMAX = 600; // seconds
    private final int PROMETHEUSULTIMATECOOLDOWNMIN = 60;
    private final int BLAZECOST = 400;
    private final double BLAZEDELAY = 15;

    @Override
    public String getName() {
        return "Fire Giant";
    }

    @Override
    public String getDefaultAlliance() {
        return "Jotunn";
    }

    @Override
    public void printInfo(Player p) {
        if (DMisc.hasDeity(p, "Fire Giant") && DMisc.isFullParticipant(p)) {
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            int devotion = 10000;
            /*
             * Calculate special values first
             */
            int t = (int) (PROMETHEUSULTIMATECOOLDOWNMAX - ((PROMETHEUSULTIMATECOOLDOWNMAX - PROMETHEUSULTIMATECOOLDOWNMIN) * ((double) DMisc.getAscensions(p) / 100)));
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
            if (save.getBind("fireball").isPresent())
                p.sendMessage(ChatColor.AQUA + "    Bound to " + save.getBind("fireball").get().name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to bind this skill to an item.");
            p.sendMessage(":Ignite the ground at the target location with diameter " + diameter + ". " + ChatColor.GREEN + "/blaze");
            p.sendMessage(ChatColor.YELLOW + "Costs " + BLAZECOST + " Favor. Cooldown time: " + BLAZEDELAY + " seconds.");
            if (save.getBind("blaze").isPresent())
                p.sendMessage(ChatColor.AQUA + "    Bound to " + save.getBind("blaze").get().name());
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
            if (!DMisc.isFullParticipant(p)) return;
            if (!DMisc.hasDeity(p, "Fire Giant")) return;
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            if (save.getAbilityData("fireball", AD.ACTIVE, false) || ((p.getItemInHand() != null) && save.getBind("fireball").isPresent() && (p.getItemInHand().getType() == save.getBind("fireball").get()))) {
                if (System.currentTimeMillis() < save.getAbilityData("fireball", AD.TIME, (double) System.currentTimeMillis()))
                    return;
                if (DMisc.getFavor(p) >= FIREBALLCOST) {
                    if (!DMisc.canTarget(p, p.getLocation())) {
                        p.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
                        return;
                    }
                    DMisc.setFavor(p, DMisc.getFavor(p) - FIREBALLCOST);
                    shootFireball(p.getEyeLocation(), DMisc.getTargetLocation(p), p);
                    double FIREBALLDELAY = 0.5;
                    save.setAbilityData("fireball", AD.TIME, System.currentTimeMillis() + (long) (FIREBALLDELAY * 1000));
                } else {
                    save.setAbilityData("fireball", AD.ACTIVE, false);
                    p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor to do that.");
                }
            }
            if (save.getAbilityData("blaze", AD.ACTIVE, false) || ((p.getItemInHand() != null) && save.getBind("blaze").isPresent() && (p.getItemInHand().getType() == save.getBind("blaze").get()))) {
                if (System.currentTimeMillis() < save.getAbilityData("blaze", AD.TIME, (double) System.currentTimeMillis())) {
                    p.sendMessage(ChatColor.YELLOW + "Blaze is on cooldown.");
                    return;
                }
                if (DMisc.getFavor(p) >= BLAZECOST) {
                    if (!DMisc.canTarget(p, p.getLocation())) {
                        p.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
                        return;
                    }
                    int diameter = (int) Math.ceil(1.43 * Math.pow(10000, 0.1527));
                    if (diameter > 12) diameter = 12;
                    if (DMisc.canLocationPVP(p, DMisc.getTargetLocation(p))) {
                        blaze(p, DMisc.getTargetLocation(p), diameter);
                        DMisc.setFavor(p, DMisc.getFavor(p) - BLAZECOST);
                        save.setAbilityData("blaze", AD.TIME, System.currentTimeMillis() + (long) (BLAZEDELAY * 1000));
                    } else p.sendMessage(ChatColor.YELLOW + "That is a protected area.");
                } else {
                    save.setAbilityData("blaze", AD.ACTIVE, false);
                    p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor to do that.");
                }
            }
        }
    }

    @Override
    public void onCommand(Player P, String str, String[] args, boolean bind) {
        if (!DMisc.isFullParticipant(P)) return;
        if (!DMisc.hasDeity(P, "Fire Giant")) return;
        PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(P);
        if (str.equalsIgnoreCase("fireball")) {
            if (bind) {
                if (!save.getBind("fireball").isPresent()) {
                    if (DMisc.isBound(P, P.getItemInHand().getType()))
                        P.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                    if (P.getItemInHand() == null) P.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                    else {
                        save.setBind("fireball", P.getItemInHand().getType());
                        P.sendMessage(ChatColor.YELLOW + "Fireball is now bound to " + P.getItemInHand().getType().name() + ".");
                    }
                } else {
                    P.sendMessage(ChatColor.YELLOW + "Fireball is no longer bound to " + save.getBind("fireball").get().name() + ".");
                    save.removeBind("fireball");
                }
                return;
            }
            if (save.getAbilityData("fireball", AD.ACTIVE, false)) {
                save.setAbilityData("fireball", AD.ACTIVE, false);
                P.sendMessage(ChatColor.YELLOW + "Fireball is no longer active.");
            } else {
                save.setAbilityData("fireball", AD.ACTIVE, true);
                P.sendMessage(ChatColor.YELLOW + "Fireball is now active.");
            }
        } else if (str.equalsIgnoreCase("blaze")) {
            if (bind) {
                if (!save.getBind("blaze").isPresent()) {
                    if (DMisc.isBound(P, P.getItemInHand().getType()))
                        P.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                    if (P.getItemInHand() == null) P.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                    else {
                        save.setBind("blaze", P.getItemInHand().getType());
                        P.sendMessage(ChatColor.YELLOW + "Blaze is now bound to " + P.getItemInHand().getType().name() + ".");
                    }
                } else {
                    P.sendMessage(ChatColor.YELLOW + "Blaze is no longer bound to " + save.getBind("blaze").get().name() + ".");
                    save.removeBind("blaze");
                }
                return;
            }
            if (save.getAbilityData("blaze", AD.ACTIVE, false)) {
                save.setAbilityData("blaze", AD.ACTIVE, false);
                P.sendMessage(ChatColor.YELLOW + "Blaze is no longer active.");
            } else {
                save.setAbilityData("blaze", AD.ACTIVE, true);
                P.sendMessage(ChatColor.YELLOW + "Blaze is now active.");
            }
        } else if (str.equalsIgnoreCase("firestorm")) {
            double FIRESTORMTIME = save.getAbilityData("firestorm", AD.TIME, (double) System.currentTimeMillis());
            if (System.currentTimeMillis() < FIRESTORMTIME) {
                P.sendMessage(ChatColor.YELLOW + "You cannot use the firestorm again for " + ((((FIRESTORMTIME) / 1000) - (System.currentTimeMillis() / 1000))) / 60 + " minutes");
                P.sendMessage(ChatColor.YELLOW + "and " + ((((FIRESTORMTIME) / 1000) - (System.currentTimeMillis() / 1000)) % 60) + " seconds.");
                return;
            }
            if (DMisc.getFavor(P) >= PROMETHEUSULTIMATECOST) {
                if (!DMisc.canTarget(P, P.getLocation())) {
                    P.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
                    return;
                }
                int t = (int) (PROMETHEUSULTIMATECOOLDOWNMAX - ((PROMETHEUSULTIMATECOOLDOWNMAX - PROMETHEUSULTIMATECOOLDOWNMIN) * ((double) DMisc.getAscensions(P) / 100)));
                save.setAbilityData("firestorm", AD.TIME, System.currentTimeMillis() + (t * 1000));
                P.sendMessage(ChatColor.GOLD + "Your divine fire " + ChatColor.WHITE + " has unleashed his wrath on " + firestorm(P) + " non-allied entities,");
                P.sendMessage("in exchange for " + ChatColor.AQUA + PROMETHEUSULTIMATECOST + ChatColor.WHITE + " Favor.");
                DMisc.setFavor(P, DMisc.getFavor(P) - PROMETHEUSULTIMATECOST);
            } else P.sendMessage("Firestorm requires " + PROMETHEUSULTIMATECOST + " Favor.");
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

    private void blaze(Player caster, Location target, int diameter) {
        for (int x = -diameter / 2; x <= diameter / 2; x++) {
            for (int y = -diameter / 2; y <= diameter / 2; y++) {
                for (int z = -diameter / 2; z <= diameter / 2; z++) {
                    Block b = target.getWorld().getBlockAt(target.getBlockX() + x, target.getBlockY() + y, target.getBlockZ() + z);
                    if ((b.getType() == Material.AIR) || (((b.getType() == Material.SNOW)) && DMisc.canLocationPVP(caster, b.getLocation())))
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
            if (anEntity instanceof Player) if (DMisc.isFullParticipant((Player) anEntity))
                if (DMisc.areAllied(p, (Player) anEntity)) continue;
            if (!DMisc.canTarget(anEntity, anEntity.getLocation())) continue;
            if (anEntity.getLocation().toVector().isInSphere(ploc, 50)) entitylist.add(anEntity);
        }
        final Player pl = p;
        final ArrayList<LivingEntity> enList = entitylist;
        for (int i = 0; i <= total; i += 20) {
            DMisc.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMisc.getPlugin(), () -> {
                for (LivingEntity e1 : enList) {
                    Location up = new Location(e1.getWorld(), e1.getLocation().getX() + Math.random() * 5, 256, e1.getLocation().getZ() + Math.random() * 5);
                    up.setPitch(90);
                    shootFireball(up, new Location(e1.getWorld(), e1.getLocation().getX() + Math.random() * 5, e1.getLocation().getY(), e1.getLocation().getZ() + Math.random() * 5), pl);
                }
            }, i);
        }
        return enList.size();
    }
}
