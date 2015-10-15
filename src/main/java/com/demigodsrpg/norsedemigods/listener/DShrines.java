package com.demigodsrpg.norsedemigods.listener;

import com.demigodsrpg.norsedemigods.DMisc;
import com.demigodsrpg.norsedemigods.Deity;
import com.demigodsrpg.norsedemigods.NorseDemigods;
import com.demigodsrpg.norsedemigods.Setting;
import com.demigodsrpg.norsedemigods.deity.Deities;
import com.demigodsrpg.norsedemigods.saveable.LocationSaveable;
import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
import com.demigodsrpg.norsedemigods.saveable.ShrineSaveable;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class DShrines implements Listener {
    // Define constant
    public static final int RADIUS = 8;

    NorseDemigods ndg;

    public DShrines(NorseDemigods pl) {
        ndg = pl;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void createShrine(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!DMisc.isFullParticipant(e.getPlayer())) return;
        if ((e.getClickedBlock().getType() != Material.SIGN) && (e.getClickedBlock().getType() != Material.SIGN_POST))
            return;
        Sign s = (Sign) e.getClickedBlock().getState();
        if (!s.getLines()[0].trim().equalsIgnoreCase("shrine")) return;
        if (!s.getLines()[1].trim().equalsIgnoreCase("dedicate")) return;
        String deityname = null;
        Player p = e.getPlayer();
        for (String name : DMisc.getTributeableDeityNames(p)) {
            if (s.getLines()[2].trim().equalsIgnoreCase(name)) {
                deityname = name;
                break;
            }
        }

        if (deityname == null) {
            p.sendMessage(ChatColor.YELLOW + "You cannot make a shrine to that deity (if it even exists).");
            return;
        }

        if (DMisc.getShrine(p.getUniqueId(), deityname) != null) {
            p.sendMessage(ChatColor.YELLOW + "You already have a shrine dedicated to " + deityname + ".");
            return;
        }
        String shrinename = "";
        if (s.getLines()[3].trim().length() > 0) {
            if (s.getLines()[3].trim().contains(" ")) {
                p.sendMessage(ChatColor.YELLOW + "The shrine's name cannot contain a space.");
                return;
            }
            for (Deity d : Deities.values()) {
                if (s.getLines()[3].trim().equalsIgnoreCase(d.getName())) {
                    p.sendMessage(ChatColor.YELLOW + "The shrine's name cannot be the same as a deity.");
                    return;
                }
            }
            for (ShrineSaveable w : DMisc.getAllShrines()) {
                if (w.getName().equals(s.getLines()[3].trim())) {
                    p.sendMessage(ChatColor.YELLOW + "A shrine with that name already exists.");
                    return;
                }
            }
            shrinename = s.getLines()[3].trim();
        }
        for (LocationSaveable center : DMisc.getAllShrines()) {
            if (DMisc.toLocation(center).getWorld().equals(e.getClickedBlock().getWorld()))
                if (e.getClickedBlock().getLocation().distance(DMisc.toLocation(center)) < (RADIUS + 1)) {
                    p.sendMessage(ChatColor.YELLOW + "Too close to an existing shrine.");
                    return;
                }
        }
        // conditions cleared
        DMisc.addShrine(p.getUniqueId(), deityname, shrinename, e.getClickedBlock().getLocation());
        e.getClickedBlock().setType(Material.GOLD_BLOCK);
        e.getClickedBlock().getWorld().strikeLightningEffect(e.getClickedBlock().getLocation());
        p.sendMessage(ChatColor.AQUA + "You have dedicated this shrine to " + deityname + ".");
        p.sendMessage(ChatColor.YELLOW + "Warp here at any time with /shrinewarp " + deityname.toLowerCase() + ".");
        if ((shrinename.length() > 0)) {
            p.sendMessage(ChatColor.YELLOW + "You may also warp here using /shrinewarp " + shrinename.toLowerCase() + ".");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public static void destroyShrine(BlockBreakEvent e) {
        for (LocationSaveable center : DMisc.getAllShrines()) {
            if ((DMisc.toWriteLocation(e.getBlock().getLocation())).equalsApprox(center)) {
                e.getPlayer().sendMessage(ChatColor.YELLOW + "Shrines cannot be broken by hand.");
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void stopShrineDamage(BlockDamageEvent e) {
        for (LocationSaveable center : DMisc.getAllShrines()) {
            if ((DMisc.toWriteLocation(e.getBlock().getLocation())).equalsApprox(center)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void stopShrineIgnite(BlockIgniteEvent e) {
        for (LocationSaveable center : DMisc.getAllShrines()) {
            if ((DMisc.toWriteLocation(e.getBlock().getLocation())).equalsApprox(center)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void stopShrineBurn(BlockBurnEvent e) {
        for (LocationSaveable center : DMisc.getAllShrines()) {
            if ((DMisc.toWriteLocation(e.getBlock().getLocation())).equalsApprox(center)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void stopShrinePistonExtend(BlockPistonExtendEvent e) {
        List<Block> blocks = e.getBlocks();
        CHECKBLOCKS:
        for (Block b : blocks) {
            for (LocationSaveable center : DMisc.getAllShrines()) {
                if ((DMisc.toWriteLocation(b.getLocation())).equalsApprox(center)) {
                    e.setCancelled(true);
                    break CHECKBLOCKS;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void stopShrinePistonRetract(BlockPistonRetractEvent e) {
        // Define variables
        final Block b = e.getBlock().getRelative(e.getDirection(), 2);

        for (LocationSaveable shrine : DMisc.getAllShrines()) {
            if ((DMisc.toWriteLocation(b.getLocation())).equalsApprox((shrine)) && e.isSticky()) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void shrineExplode(final EntityExplodeEvent e) {
        try {
            // Remove shrine blocks from explosions
            Iterator<Block> i = e.blockList().iterator();
            while (i.hasNext()) {
                Block b = i.next();
                if (!DMisc.canLocationPVP(b.getLocation())) i.remove();
                for (LocationSaveable center : DMisc.getAllShrines()) {
                    if ((DMisc.toWriteLocation(b.getLocation())).equalsApprox(center)) i.remove();
                }
            }
        } catch (Exception ignored) {
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerTribute(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getClickedBlock().getType() != Material.GOLD_BLOCK) return;
        if (!DMisc.isFullParticipant(e.getPlayer())) return;
        // check if block is shrine
        String deityname = DMisc.getDeityAtShrine(e.getClickedBlock().getLocation());
        if (deityname == null) return;
        // check if player has deity
        Player p = e.getPlayer();
        for (Deity d : DMisc.getDeities(p))
            if (d.getName().equalsIgnoreCase(deityname)) {
                // open the tribute inventory
                Inventory ii = DMisc.getPlugin().getServer().createInventory(p, 27, "Tributes");
                p.openInventory(ii);
                PlayerDataSaveable save = ndg.getPlayerDataRegistry().fromPlayer(p);
                save.setTempStatus(deityname.toUpperCase() + "_TRIBUTE_", true); // DSave.saveData(p, deityname.toUpperCase() + "_TRIBUTE_", DMisc.getOwnerOfShrine(DMisc.toWriteLocation(e.getClickedBlock().getLocation())));
                save.setTempStatus("_BORROWED_SHRINE_" + DMisc.getOwnerOfShrine(e.getClickedBlock().getLocation()).toString(), true);
                e.setCancelled(true);
                return;
            }
        p.sendMessage(ChatColor.YELLOW + "You must be allied with " + deityname + " in order to tribute here.");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void shrineAlerts(PlayerMoveEvent e) {
        if (!e.getFrom().getWorld().equals(e.getTo().getWorld())) return;
        if (e.getFrom().distance(e.getTo()) < 0.1) return;
        for (String p : DMisc.getFullParticipants()) {
            UUID player = UUID.fromString(p);
            if (DMisc.getShrines(player) != null)
                for (ShrineSaveable center : DMisc.getShrines(player)) {
                    // Check for world errors
                    if (!DMisc.toLocation(center).getWorld().equals(e.getPlayer().getWorld())) return;
                    if (e.getFrom().getWorld() != DMisc.toLocation(center).getWorld()) return;
                /*
                 * Outside coming in
				 */
                    if (e.getFrom().distance(DMisc.toLocation(center)) > RADIUS) {
                        if (DMisc.toLocation(center).distance(e.getTo()) <= RADIUS) {
                            e.getPlayer().sendMessage(ChatColor.GRAY + "You have entered " + DMisc.getLastKnownName(player) + "'s shrine to " + ChatColor.YELLOW + center.getDeity() + ChatColor.GRAY + ".");
                            return;
                        }
                    }
                /*
                 * Leaving
				 */
                    else if (e.getFrom().distance(DMisc.toLocation(center)) <= RADIUS) {
                        if (DMisc.toLocation(center).distance(e.getTo()) > RADIUS) {
                            e.getPlayer().sendMessage(ChatColor.GRAY + "You have left a shrine.");
                            return;
                        }
                    }
                }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void tributeSuccess(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;
        Player p = (Player) e.getPlayer();
        if (!DMisc.isFullParticipant(p)) return;
        // continue if tribute chest
        if (!e.getInventory().getName().equals("Tributes")) return;
        // get which deity tribute goes to
        String togive = null;
        PlayerDataSaveable save = ndg.getPlayerDataRegistry().fromPlayer((Player) e.getPlayer());
        for (String d : save.getDeityList()) {
            if (save.getTempStatus(d.toUpperCase() + "_TRIBUTE_")) {
                togive = d;
                break;
            }
        }
        if (togive == null) return;
        save.removeTempStatus(togive.toUpperCase() + "_TRIBUTE_");
        String creatorId = save.getTempData("_BORROWED_SHRINE_", true);
        // calculate value of chest
        int value = 0;
        int items = 0;
        for (ItemStack ii : e.getInventory().getContents()) {
            if (ii != null) {
                value += DMisc.getValue(ii);
                items++;
            }
        }
        value *= Setting.FAVOR_MULTIPLIER;
        // give devotion
        int dbefore = DMisc.getDevotion(p, togive);
        DMisc.setDevotion(p, togive, DMisc.getDevotion(p, togive) + value);
        if (creatorId != null) {
            UUID creator = UUID.fromString(creatorId);
            DMisc.setDevotion(creator, togive, DMisc.getDevotion(creator, togive) + value / 7);
        }
        // give favor
        int fbefore = DMisc.getFavorCap(p);
        DMisc.setFavorCap(p, DMisc.getFavorCap(p) + value / 5);
        // devotion lock TODO
        if (dbefore < DMisc.getDevotion(p, togive))
            p.sendMessage(ChatColor.YELLOW + "Your Devotion for " + togive + " has increased to " + DMisc.getDevotion(p, togive) + ".");
        if (fbefore < DMisc.getFavorCap(p))
            p.sendMessage(ChatColor.YELLOW + "Your Favor Cap has increased to " + DMisc.getFavorCap(p) + ".");
        if ((fbefore == DMisc.getFavorCap(p)) && (dbefore == DMisc.getDevotion(p, togive)) && (items > 0))
            p.sendMessage(ChatColor.YELLOW + "Your tributes were insufficient for " + togive + "'s blessings.");
        DLevels.levelProcedure(p);
        // clear inventory
        e.getInventory().clear();
    }
}
