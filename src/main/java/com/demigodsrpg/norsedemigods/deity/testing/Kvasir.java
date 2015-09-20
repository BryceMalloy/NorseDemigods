package com.demigodsrpg.norsedemigods.deity.testing;

/*
 * This style/format of code is now deprecated.
 */

import com.demigodsrpg.norsedemigods.DFixes;
import com.demigodsrpg.norsedemigods.deity.Deity;
import com.demigodsrpg.norsedemigods.util.DMiscUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Kvasir implements Deity {

    /* General */
    private static final long serialVersionUID = 2319323778421842381L;

    /* Specific to player */
    private final UUID PLAYER;

    public Kvasir(UUID name) {
        PLAYER = name;
    }

    @Override
    public String getName() {
        return "Kvasir";
    }

    @Override
    public UUID getPlayerId() {
        return PLAYER;
    }

    @Override
    public String getDefaultAlliance() {
        return "Testing";
    }

    @Override
    public void printInfo(Player p) {
        if (DMiscUtil.hasDeity(p, "Kvasir") && DMiscUtil.isFullParticipant(p)) {
            int devotion = DMiscUtil.getDevotion(p, getName());
            p.sendMessage("--" + ChatColor.GOLD + "Kvasir" + ChatColor.GRAY + " [" + devotion + "]");
            p.sendMessage("Passive: Immune to drowning, with increased healing while in water.");
            p.sendMessage("Passive: Fast swim, sneak while in water to swim very fast!");
            p.sendMessage("Passive: Sneak while on land to create temporary water to swim in!");
            return;
        }
        p.sendMessage("--" + ChatColor.GOLD + "Kvasir");
        p.sendMessage("Passive: Immune to drowning, with increased healing while in water.");
        p.sendMessage("Passive: Fast swim, sneak while in water to swim very fast!");
        p.sendMessage("Passive: Sneak while on land to create temporary water to swim in!");
    }

    @Override
    public void onEvent(Event ee) {
        if (ee instanceof PlayerMoveEvent) {
            PlayerMoveEvent move = (PlayerMoveEvent) ee;
            Player p = move.getPlayer();
            if (!DMiscUtil.isFullParticipant(p)) return;
            if (!DMiscUtil.hasDeity(p, "Kvasir")) return;
            // PHELPS SWIMMING
            if (p.getLocation().getBlock().getType().equals(Material.STATIONARY_WATER) || p.getLocation().getBlock().
                    getType().equals(Material.WATER)) {
                Vector dir = p.getLocation().getDirection().normalize().multiply(1.3D);
                Vector vec = new Vector(dir.getX(), dir.getY(), dir.getZ());
                if (p.isSneaking()) p.setVelocity(vec);
            }
        }
        if (ee instanceof EntityDamageEvent) {
            if ((((EntityDamageEvent) ee).getCause().equals(DamageCause.DROWNING) || ((EntityDamageEvent) ee).
                    getCause().equals(DamageCause.SUFFOCATION)) && ((EntityDamageEvent) ee).
                    getEntity() instanceof Player) {
                Player p = (Player) ((EntityDamageEvent) ee).getEntity();
                if (!DMiscUtil.isFullParticipant(p)) return;
                if (!DMiscUtil.hasDeity(p, "Kvasir")) return;
                DFixes.checkAndCancel((EntityDamageEvent) ee);
            }
        }
    }

    @Override
    public void onCommand(Player P, String str, String[] args, boolean bind) {
        //if (!DMiscUtil.isFullParticipant(P)) return;
        //if (!DMiscUtil.hasDeity(P, "Kvasir")) return;
    }

    @Override
    public void onTick(long timeSent) {
        for (final Player player : Bukkit.getOnlinePlayers()) {
            if (DMiscUtil.hasDeity(player, getName())) {
                if (DMiscUtil.canWorldGuardBuild(player, player.getLocation())) {
                    if (player.isSneaking() && player.getItemInHand().getType().equals(Material.WOOD_SPADE)) {
                        for (int x = -5; x < 6; x++) {
                            for (int z = -5; z < 6; z++) {
                                for (int y = -2; y < 3; y++) {
                                    final Location faux = player.getLocation().add(x, y, z);
                                    if (FAUX_LOCATIONS.contains(faux)) continue;
                                    if (faux.getBlock() instanceof Chest) continue;
                                    if (faux.getBlock().getType().equals(Material.STATIONARY_WATER) || faux.getBlock().
                                            getType().equals(Material.WATER)) continue;
                                    final MaterialData oldData = faux.getBlock().getState().getData();

                                    faux.getBlock().setTypeIdAndData(Material.STATIONARY_WATER.getId(), (byte) 0, false);

                                    Bukkit.getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), () -> {
                                        player.sendBlockChange(faux, oldData.getItemType(), oldData.getData());
                                    }, 3);

                                    FAUX_LOCATIONS.add(faux);
                                    createTask(player, oldData, faux);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static Set<Location> FAUX_LOCATIONS = new HashSet<>();

    private void createTask(final Player player, final MaterialData oldData, final Location faux) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (player.getLocation().getWorld().equals(faux.getWorld()) && faux.distance(player.getLocation()) < 5) {
                    createTask(player, oldData, faux);
                } else {
                    faux.getBlock().setTypeIdAndData(oldData.getItemTypeId(), oldData.getData(), false);
                    FAUX_LOCATIONS.remove(faux);
                }
            }
        }, 30);
    }

    @Override
    public boolean canTribute() {
        return true;
    }
}
