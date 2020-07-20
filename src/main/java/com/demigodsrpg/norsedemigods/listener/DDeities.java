package com.demigodsrpg.norsedemigods.listener;

import com.demigodsrpg.norsedemigods.DMisc;
import com.demigodsrpg.norsedemigods.Deity;
import com.demigodsrpg.norsedemigods.NorseDemigods;
import com.demigodsrpg.norsedemigods.Setting;
import com.demigodsrpg.norsedemigods.deity.Deities;
import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.*;

import java.util.logging.Logger;

public class DDeities implements Listener {

    NorseDemigods ndg;

    /*
     * Distributes all events to deities
     */
    public DDeities(NorseDemigods pl) {
        ndg = pl;
        DMisc.getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(DMisc.getPlugin(), () -> {
            for (Deity d : Deities.values()) {
                d.onSyncTick(System.currentTimeMillis());
            }
        }, 20, 5);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        // Player
        Player p = e.getPlayer();
        if (!DMisc.getDeities(p).isEmpty()) {
            DMisc.getDeities(p).forEach(d -> d.onEvent(e));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        // Player
        Player p = e.getPlayer();
        if (!DMisc.getDeities(p).isEmpty()) {
            DMisc.getDeities(p).forEach(d -> d.onEvent(e));
        }
    }

    public static void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (!DMisc.getDeities(p).isEmpty()) {
                DMisc.getDeities(p).forEach(d -> d.onEvent(e));
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if (e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            if (!DMisc.getDeities(p).isEmpty()) {
                DMisc.getDeities(p).forEach(d -> d.onEvent(e));
            }
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent e) {
        if (e.isCancelled()) return;
        if (e.getTarget() instanceof Player) {
            Player p = (Player) e.getTarget();
            if (!DMisc.getDeities(p).isEmpty()) {
                DMisc.getDeities(p).forEach(d -> d.onEvent(e));
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (!DMisc.getDeities(p).isEmpty()) {
            DMisc.getDeities(p).forEach(d -> d.onEvent(e));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) { // sync to master file
        final Player p = e.getPlayer();
        PlayerDataSaveable save = ndg.getPlayerDataRegistry().fromPlayer(p);
        if (Setting.MOTD) {
            p.sendMessage("This server is running NorseDemigods v" + ChatColor.YELLOW + DMisc.getPlugin().getDescription().getVersion() + ChatColor.WHITE + ".");
            p.sendMessage(ChatColor.GRAY + "Type " + ChatColor.GREEN + "/dg" + ChatColor.GRAY + " for more info.");
        }
        if ((save.getActiveEffects().containsKey("CHARGE"))) {
            save.addEffect("CHARGE", System.currentTimeMillis(), true);
            p.sendMessage(ChatColor.YELLOW + "Your charging attack has been reset.");
        }
        save.setLastKnownName(p.getName());
        save.setLastLoginTime(System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent e) {
        Player p = e.getPlayer();
        if (!DMisc.getDeities(p).isEmpty()) {
            DMisc.getDeities(p).forEach(d -> d.onEvent(e));
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();
        PlayerDataSaveable save = ndg.getPlayerDataRegistry().fromPlayer(p);
        if (DMisc.isFullParticipant(p)) {
            if (save.getTempStatus("ALLIANCECHAT")) {
                e.setCancelled(true);
                Logger.getLogger("Minecraft").info("[" + DMisc.getAllegiance(p) + "] " + p.getName() + ": " + e.getMessage());
                DMisc.getPlugin().getServer().getOnlinePlayers().stream().filter(pl -> DMisc.isFullParticipant(pl) &&
                        DMisc.getAllegiance(pl).equalsIgnoreCase(DMisc.getAllegiance(p))).forEach(pl ->
                        pl.sendMessage(ChatColor.GREEN + "[" + DMisc.getAscensions(p) + "] " + p.getName() + ": " + e.getMessage()));
            }
        }
        if ((DMisc.getDeities(p) != null) && (DMisc.getDeities(p).size() > 0)) {
            for (Deity d : DMisc.getDeities(p))
                d.onEvent(e);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!DMisc.getDeities(p).isEmpty()) {
            DMisc.getDeities(p).forEach(d -> d.onEvent(e));
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if (!DMisc.getDeities(p).isEmpty()) {
            DMisc.getDeities(p).forEach(d -> d.onEvent(e));
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (!DMisc.getDeities(p).isEmpty()) {
            DMisc.getDeities(p).forEach(d -> d.onEvent(e));
        }
    }
}
