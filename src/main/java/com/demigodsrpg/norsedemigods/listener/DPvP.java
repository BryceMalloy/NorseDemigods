package com.demigodsrpg.norsedemigods.listener;

import com.demigodsrpg.norsedemigods.*;
import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
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

@SuppressWarnings("ALL")
public class DPvP implements Listener {
    private static final int pvpkillreward = 1500; // Devotion

    NorseDemigods ndg;

    public DPvP(NorseDemigods plugin) {
        ndg = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onArrowLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) e.getEntity();
            if (arrow.getShooter() instanceof Player) {
                Player shooter = (Player) arrow.getShooter();
                if (!DMisc.canTarget(shooter, shooter.getLocation())) {
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
                if (!DMisc.canWorldGuardBuild(player, player.getLocation())) e.getEntity().remove();
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
        if (!(DMisc.isFullParticipant(attacker) && DMisc.isFullParticipant(target))) {
            if (!DMisc.canTarget(target, target.getLocation())) {
                attacker.sendMessage(ChatColor.YELLOW + "This is a no-PvP zone.");
                DFixes.checkAndCancel(e);
                return;
            }
        }
        if (DMisc.getAllegiance(attacker).equalsIgnoreCase(DMisc.getAllegiance(target)))
            return; // Handled in DDamage...
        if (!DMisc.canTarget(target, target.getLocation())) {
            attacker.sendMessage(ChatColor.YELLOW + "This is a no-PvP zone.");
            DFixes.checkAndCancel(e);
            return;
        }
        if (!DMisc.canTarget(attacker, attacker.getLocation())) {
            attacker.sendMessage(ChatColor.YELLOW + "This is a no-PvP zone.");
            DFixes.checkAndCancel(e);
            return;
        }
        try {
            List<Deity> deities = Lists.newArrayList(DMisc.getTributeableDeities(attacker));
            if (!deities.isEmpty()) {
                Deity d = deities.get((int) Math.floor(Math.random() * deities.size()));
                DMisc.setDevotion(attacker, d, DMisc.getDevotion(attacker, d) + (int) (e.getDamage() * Setting.PVP_MULTIPLIER));
                DLevels.levelProcedure(attacker);
            }
        } catch (Exception ignored) {
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void playerDeath(final EntityDeathEvent e1) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(DMisc.getPlugin(), () -> {
            if (!(e1.getEntity() instanceof Player)) return;
            Player attacked = (Player) e1.getEntity();
            if ((attacked.getLastDamageCause() != null) && (attacked.getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
                EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) attacked.getLastDamageCause();
                if (!(e.getDamager() instanceof Player)) return;
                Player attacker = (Player) e.getDamager();
                if (!(DMisc.isFullParticipant(attacker))) return;
                if (DMisc.isFullParticipant(attacked)) {
                    if (DMisc.getAllegiance(attacker).equalsIgnoreCase(DMisc.getAllegiance(attacked))) { // betrayal
                        DMisc.getPlugin().getServer().broadcastMessage(ChatColor.YELLOW + attacked.getName() + ChatColor.GRAY + " was betrayed by " + ChatColor.YELLOW + attacker.getName() + ChatColor.GRAY + " of the " + DMisc.getAllegiance(attacker) + " alliance.");
                        if (DMisc.getKills(attacker) > 0) {
                            DMisc.setKills(attacker, DMisc.getKills(attacker) - 1);
                            attacker.sendMessage(ChatColor.RED + "Your number of kills has decreased to " + DMisc.getKills(attacker) + ".");
                        }
                    } else { // PVP kill
                        DMisc.setKills(attacker, DMisc.getKills(attacker) + 1);
                        DMisc.setDeaths(attacked, DMisc.getDeaths(attacked) + 1);
                        DMisc.getPlugin().getServer().broadcastMessage(ChatColor.YELLOW + attacked.getName() + ChatColor.GRAY + " of the " + DMisc.getAllegiance(attacked) + " alliance was slain by " + ChatColor.YELLOW + attacker.getName() + ChatColor.GRAY + " of the " + DMisc.getAllegiance(attacker) + " alliance.");

                        double adjusted = DMisc.getKills(attacked) * 1.0 / DMisc.getDeaths(attacked);
                        if (adjusted > 5) adjusted = 5;
                        if (adjusted < 0.2) adjusted = 0.2;
                        for (Deity d : DMisc.getDeities(attacker)) {
                            DMisc.setDevotion(attacker, d, DMisc.getDevotion(attacker, d) + (int) (pvpkillreward * Setting.PVP_MULTIPLIER * adjusted));
                        }
                    }
                } else { // regular player
                    DMisc.getPlugin().getServer().broadcastMessage(ChatColor.YELLOW + attacked.getName() + ChatColor.GRAY + " was slain by " + ChatColor.YELLOW + attacker.getName() + ChatColor.GRAY + " of the " + DMisc.getAllegiance(attacker) + " alliance.");
                }
            }
        }, 30);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        PlayerDataSaveable save = ndg.getPlayerDataRegistry().fromPlayer(event.getPlayer());
        onPlayerLineJump(event.getPlayer(), save, event.getTo(), event.getFrom(), Setting.PVP_DELAY);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Define variables
        final Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();
        int delayTime = Setting.PVP_DELAY;
        PlayerDataSaveable save = ndg.getPlayerDataRegistry().fromPlayer(player);
        if (save.getTempStatus("temp_flash") || event.getCause() == TeleportCause.ENDER_PEARL) {
            onPlayerLineJump(player, save, to, from, delayTime);
        } else if (!DMisc.canLocationPVP(player, to) && DMisc.canLocationPVP(player, from)) {
            save.removeTempData("temp_was_PVP");
            player.sendMessage(ChatColor.YELLOW + "You are now safe from all PVP!");
        } else if (!DMisc.canLocationPVP(player, from) && DMisc.canLocationPVP(player, to))
            player.sendMessage(ChatColor.YELLOW + "You can now PVP!");
    }

    @SuppressWarnings("deprecation")
    void onPlayerLineJump(final Player player, PlayerDataSaveable save, Location to, Location from, int delayTime) {
        // NullPointer Check
        if (to == null || from == null) return;

        if (save.getTempStatus("temp_was_PVP") || !DMisc.isFullParticipant(player)) return;

        // No Spawn Line-Jumping
        if (!DMisc.canLocationPVP(player, to) && DMisc.canLocationPVP(player, from) && delayTime > 0 && !player.hasPermission("demigods.bypasspvpdelay") && !DFixes.isNoob(player)) {
            save.setTempData("temp_was_PVP", true);
            save.removeTempData("temp_flash");

            DMisc.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMisc.getPlugin(), () -> {
                PlayerDataSaveable newSave = ndg.getPlayerDataRegistry().fromPlayer(player);
                newSave.removeTempData("temp_was_PVP");
                if (!DMisc.canLocationPVP(player, player.getLocation()))
                    player.sendMessage(ChatColor.YELLOW + "You are now safe from all PVP!");
            }, (delayTime * 20));
        } else if (!save.getTempStatus("temp_was_PVP") && !DMisc.canLocationPVP(player, to) && DMisc.canLocationPVP(player, from))
            player.sendMessage(ChatColor.YELLOW + "You are now safe from all PVP!");

        // Let players know where they can PVP
        if (!save.getTempStatus("temp_was_PVP") && DMisc.canLocationPVP(player, to) && !DMisc.canLocationPVP(player, from)) {
            if (!DMisc.canLocationPVP(player, from) && DMisc.canLocationPVP(player, to))
                player.sendMessage(ChatColor.YELLOW + "You can now PVP!");
        }
    }
}
