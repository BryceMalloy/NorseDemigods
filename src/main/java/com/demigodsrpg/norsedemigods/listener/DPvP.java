package com.demigodsrpg.norsedemigods.listener;

import com.demigodsrpg.norsedemigods.DFixes;
import com.demigodsrpg.norsedemigods.Deity;
import com.demigodsrpg.norsedemigods.util.DMiscUtil;
import com.demigodsrpg.norsedemigods.util.DSave;
import com.demigodsrpg.norsedemigods.util.DSettings;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class DPvP implements Listener {
    private static final double MULTIPLIER = DSettings.getSettingDouble("pvp_exp_bonus"); // bonus for dealing damage
    private static final int pvpkillreward = 1500; // Devotion

    @EventHandler(priority = EventPriority.HIGH)
    public void onArrowLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) e.getEntity();
            if (arrow.getShooter() instanceof Player) {
                Player shooter = (Player) arrow.getShooter();
                if (!DMiscUtil.canTarget(shooter, shooter.getLocation())) {
                    shooter.sendMessage(ChatColor.YELLOW + "This is a no-PvP zone.");

                    // Undo the arrow being removed from the inventory
                    int slot = shooter.getInventory().first(Material.ARROW);
                    ItemStack arrows = shooter.getInventory().getItem(slot);
                    arrows.setAmount(arrows.getAmount() + 1);
                    shooter.getInventory().setItem(slot, arrows);

                    e.setCancelled(true);
                }
            }
        } else if (e.getEntityType().equals(EntityType.ENDER_PEARL)) {
            if (e.getEntity().getShooter() instanceof Player) {
                Player player = (Player) e.getEntity().getShooter();
                if (!DMiscUtil.canWorldGuardBuild(player, player.getLocation())) e.getEntity().remove();
            }
        }
    }

    public static void pvpDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player target = (Player) e.getEntity();
        Player attacker;
        if (e.getDamager() instanceof Arrow && ((Arrow) e.getDamager()).getShooter() instanceof Player)
            attacker = (Player) ((Arrow) e.getDamager()).getShooter();
        else if (e.getDamager() instanceof Player) attacker = (Player) e.getDamager();
        else return;
        if (!DSettings.getEnabledWorlds().contains(attacker.getWorld())) return;
        if (!(DMiscUtil.isFullParticipant(attacker) && DMiscUtil.isFullParticipant(target))) {
            if (!DMiscUtil.canTarget(target, target.getLocation())) {
                attacker.sendMessage(ChatColor.YELLOW + "This is a no-PvP zone.");
                DFixes.checkAndCancel(e);
                return;
            }
        }
        if (DMiscUtil.getAllegiance(attacker).equalsIgnoreCase(DMiscUtil.getAllegiance(target)))
            return; // Handled in DDamage...
        if (!DMiscUtil.canTarget(target, target.getLocation())) {
            attacker.sendMessage(ChatColor.YELLOW + "This is a no-PvP zone.");
            DFixes.checkAndCancel(e);
            return;
        }
        if (!DMiscUtil.canTarget(attacker, attacker.getLocation())) {
            attacker.sendMessage(ChatColor.YELLOW + "This is a no-PvP zone.");
            DFixes.checkAndCancel(e);
            return;
        }
        try {
            List<Deity> deities = Lists.newArrayList(DMiscUtil.getTributeableDeities(attacker));
            if (!deities.isEmpty()) {
                Deity d = deities.get((int) Math.floor(Math.random() * deities.size()));
                DMiscUtil.setDevotion(attacker, d, DMiscUtil.getDevotion(attacker, d) + (int) (e.getDamage() * MULTIPLIER));
                DLevels.levelProcedure(attacker);
            }
        } catch (Exception ignored) {
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void playerDeath(final EntityDeathEvent e1) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (!(e1.getEntity() instanceof Player)) return;
                Player attacked = (Player) e1.getEntity();
                if (!DSettings.getEnabledWorlds().contains(attacked.getWorld())) return;

                if ((attacked.getLastDamageCause() != null) && (attacked.getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
                    EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) attacked.getLastDamageCause();
                    if (!(e.getDamager() instanceof Player)) return;
                    Player attacker = (Player) e.getDamager();
                    if (!(DMiscUtil.isFullParticipant(attacker))) return;
                    if (DMiscUtil.isFullParticipant(attacked)) {
                        if (DMiscUtil.getAllegiance(attacker).equalsIgnoreCase(DMiscUtil.getAllegiance(attacked))) { // betrayal
                            DMiscUtil.getPlugin().getServer().broadcastMessage(ChatColor.YELLOW + attacked.getName() + ChatColor.GRAY + " was betrayed by " + ChatColor.YELLOW + attacker.getName() + ChatColor.GRAY + " of the " + DMiscUtil.getAllegiance(attacker) + " alliance.");
                            if (DMiscUtil.getKills(attacker) > 0) {
                                DMiscUtil.setKills(attacker, DMiscUtil.getKills(attacker) - 1);
                                attacker.sendMessage(ChatColor.RED + "Your number of kills has decreased to " + DMiscUtil.getKills(attacker) + ".");
                            }
                        } else { // PVP kill
                            DMiscUtil.setKills(attacker, DMiscUtil.getKills(attacker) + 1);
                            DMiscUtil.setDeaths(attacked, DMiscUtil.getDeaths(attacked) + 1);
                            DMiscUtil.getPlugin().getServer().broadcastMessage(ChatColor.YELLOW + attacked.getName() + ChatColor.GRAY + " of the " + DMiscUtil.getAllegiance(attacked) + " alliance was slain by " + ChatColor.YELLOW + attacker.getName() + ChatColor.GRAY + " of the " + DMiscUtil.getAllegiance(attacker) + " alliance.");

                            double adjusted = DMiscUtil.getKills(attacked) * 1.0 / DMiscUtil.getDeaths(attacked);
                            if (adjusted > 5) adjusted = 5;
                            if (adjusted < 0.2) adjusted = 0.2;
                            for (Deity d : DMiscUtil.getDeities(attacker)) {
                                DMiscUtil.setDevotion(attacker, d, DMiscUtil.getDevotion(attacker, d) + (int) (pvpkillreward * MULTIPLIER * adjusted));
                            }
                        }
                    } else { // regular player
                        DMiscUtil.getPlugin().getServer().broadcastMessage(ChatColor.YELLOW + attacked.getName() + ChatColor.GRAY + " was slain by " + ChatColor.YELLOW + attacker.getName() + ChatColor.GRAY + " of the " + DMiscUtil.getAllegiance(attacker) + " alliance.");
                    }
                }
            }
        }, 30);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        onPlayerLineJump(event.getPlayer(), event.getTo(), event.getFrom(), DSettings.getSettingInt("pvp_area_delay_time"));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Define variables
        final Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();
        int delayTime = DSettings.getSettingInt("pvp_area_delay_time");

        if (DSave.hasData(player, "temp_flash") || event.getCause() == TeleportCause.ENDER_PEARL) {
            onPlayerLineJump(player, to, from, delayTime);
        } else if (!DMiscUtil.canLocationPVP(to) && DMiscUtil.canLocationPVP(from)) {
            DSave.removeData(player, "temp_was_PVP");
            player.sendMessage(ChatColor.YELLOW + "You are now safe from all PVP!");
        } else if (!DMiscUtil.canLocationPVP(from) && DMiscUtil.canLocationPVP(to))
            player.sendMessage(ChatColor.YELLOW + "You can now PVP!");
    }

    void onPlayerLineJump(final Player player, Location to, Location from, int delayTime) {
        // NullPointer Check
        if (to == null || from == null) return;

        if (DSave.hasData(player, "temp_was_PVP") || !DMiscUtil.isFullParticipant(player)) return;

        // No Spawn Line-Jumping
        if (!DMiscUtil.canLocationPVP(to) && DMiscUtil.canLocationPVP(from) && delayTime > 0 && !DMiscUtil.hasPermission(player, "demigods.bypasspvpdelay") && !DFixes.isNoob(player)) {
            DSave.saveData(player, "temp_was_PVP", true);
            if (DSave.hasData(player, "temp_flash")) DSave.removeData(player, "temp_flash");

            DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
                @Override
                public void run() {
                    DSave.removeData(player, "temp_was_PVP");
                    if (!DMiscUtil.canLocationPVP(player.getLocation()))
                        player.sendMessage(ChatColor.YELLOW + "You are now safe from all PVP!");
                }
            }, (delayTime * 20));
        } else if (!DSave.hasData(player, "temp_was_PVP") && !DMiscUtil.canLocationPVP(to) && DMiscUtil.canLocationPVP(from))
            player.sendMessage(ChatColor.YELLOW + "You are now safe from all PVP!");

        // Let players know where they can PVP
        if (!DSave.hasData(player, "temp_was_PVP") && DMiscUtil.canLocationPVP(to) && !DMiscUtil.canLocationPVP(from)) {
            if (!DMiscUtil.canLocationPVP(from) && DMiscUtil.canLocationPVP(to))
                player.sendMessage(ChatColor.YELLOW + "You can now PVP!");
        }
    }
}
