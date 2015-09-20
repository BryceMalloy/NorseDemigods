package com.demigodsrpg.norsedemigods.listener;

import com.demigodsrpg.norsedemigods.NorseDemigods;
import com.demigodsrpg.norsedemigods.deity.Deity;
import com.demigodsrpg.norsedemigods.saveable.LocationSaveable;
import com.demigodsrpg.norsedemigods.util.DMiscUtil;
import com.demigodsrpg.norsedemigods.util.DSave;
import com.demigodsrpg.norsedemigods.util.DSettings;
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
    // Define variables
    public static final double FAVORMULTIPLIER = DSettings.getSettingDouble("globalfavormultiplier");
    public static final int RADIUS = 8;

    @EventHandler(priority = EventPriority.HIGH)
    public void createShrine(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!DSettings.getEnabledWorlds().contains(e.getClickedBlock().getWorld())) return;
        if (!DMiscUtil.isFullParticipant(e.getPlayer())) return;
        if ((e.getClickedBlock().getType() != Material.SIGN) && (e.getClickedBlock().getType() != Material.SIGN_POST))
            return;
        Sign s = (Sign) e.getClickedBlock().getState();
        if (!s.getLines()[0].trim().equalsIgnoreCase("shrine")) return;
        if (!s.getLines()[1].trim().equalsIgnoreCase("dedicate")) return;
        String deityname = null;
        Player p = e.getPlayer();
        for (String name : DMiscUtil.getTributeableDeityNames(p)) {
            if (s.getLines()[2].trim().equalsIgnoreCase(name)) {
                deityname = name;
                break;
            }
        }

        if (deityname == null) {
            p.sendMessage(ChatColor.YELLOW + "You cannot make a shrine to that deity (if it even exists).");
            return;
        }

        if (DMiscUtil.getShrine(p.getUniqueId(), deityname) != null) {
            p.sendMessage(ChatColor.YELLOW + "You already have a shrine dedicated to " + deityname + ".");
            return;
        }
        String shrinename = "";
        if (s.getLines()[3].trim().length() > 0) {
            if (s.getLines()[3].trim().charAt(0) == '#') {
                p.sendMessage(ChatColor.YELLOW + "The shrine's name cannot begin with an invalid character.");
                return;
            }
            if (s.getLines()[3].trim().contains(" ")) {
                p.sendMessage(ChatColor.YELLOW + "The shrine's name cannot contain a space.");
                return;
            }
            for (Deity d : NorseDemigods.deities) {
                if (s.getLines()[3].trim().equalsIgnoreCase(d.getName())) {
                    p.sendMessage(ChatColor.YELLOW + "The shrine's name cannot be the same as a deity.");
                    return;
                }
            }
            for (LocationSaveable w : DMiscUtil.getAllShrines()) {
                if (DMiscUtil.getShrineName(w).equals(s.getLines()[3].trim())) {
                    p.sendMessage(ChatColor.YELLOW + "A shrine with that name already exists.");
                    return;
                }
            }
            shrinename = "#" + s.getLines()[3].trim();
        }
        for (LocationSaveable center : DMiscUtil.getAllShrines()) {
            if (DMiscUtil.toLocation(center).getWorld().equals(e.getClickedBlock().getWorld()))
                if (e.getClickedBlock().getLocation().distance(DMiscUtil.toLocation(center)) < (RADIUS + 1)) {
                    p.sendMessage(ChatColor.YELLOW + "Too close to an existing shrine.");
                    return;
                }
        }
        // conditions cleared
        DMiscUtil.addShrine(p.getUniqueId(), deityname, DMiscUtil.toWriteLocation(e.getClickedBlock().getLocation()));
        if (shrinename.length() > 1)
            DMiscUtil.addShrine(p.getUniqueId(), shrinename, DMiscUtil.toWriteLocation(e.getClickedBlock().getLocation())); // accessible by two names
        e.getClickedBlock().setType(Material.GOLD_BLOCK);
        e.getClickedBlock().getWorld().strikeLightningEffect(e.getClickedBlock().getLocation());
        p.sendMessage(ChatColor.AQUA + "You have dedicated this shrine to " + deityname + ".");
        p.sendMessage(ChatColor.YELLOW + "Warp here at any time with /shrinewarp " + deityname.toLowerCase() + ".");
        if ((shrinename.length() > 0) && (shrinename.charAt(0) == '#')) {
            p.sendMessage(ChatColor.YELLOW + "You may also warp here using /shrinewarp " + shrinename.substring(1).toLowerCase() + ".");
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public static void destroyShrine(BlockBreakEvent e) {
        if (!DSettings.getEnabledWorlds().contains(e.getBlock().getWorld())) return;
        for (LocationSaveable center : DMiscUtil.getAllShrines()) {
            if ((DMiscUtil.toWriteLocation(e.getBlock().getLocation())).equalsApprox(center)) {
                e.getPlayer().sendMessage(ChatColor.YELLOW + "Shrines cannot be broken by hand.");
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void stopShrineDamage(BlockDamageEvent e) {
        if (!DSettings.getEnabledWorlds().contains(e.getBlock().getWorld())) return;
        for (LocationSaveable center : DMiscUtil.getAllShrines()) {
            if ((DMiscUtil.toWriteLocation(e.getBlock().getLocation())).equalsApprox(center)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void stopShrineIgnite(BlockIgniteEvent e) {
        if (!DSettings.getEnabledWorlds().contains(e.getBlock().getWorld())) return;
        for (LocationSaveable center : DMiscUtil.getAllShrines()) {
            if ((DMiscUtil.toWriteLocation(e.getBlock().getLocation())).equalsApprox(center)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void stopShrineBurn(BlockBurnEvent e) {
        if (!DSettings.getEnabledWorlds().contains(e.getBlock().getWorld())) return;
        for (LocationSaveable center : DMiscUtil.getAllShrines()) {
            if ((DMiscUtil.toWriteLocation(e.getBlock().getLocation())).equalsApprox(center)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void stopShrinePistonExtend(BlockPistonExtendEvent e) {
        List<Block> blocks = e.getBlocks();

        CHECKBLOCKS:
        for (Block b : blocks) {
            if (!DSettings.getEnabledWorlds().contains(b.getWorld())) {
                return;
            }
            for (LocationSaveable center : DMiscUtil.getAllShrines()) {
                if ((DMiscUtil.toWriteLocation(b.getLocation())).equalsApprox(center)) {
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

        if (!DSettings.getEnabledWorlds().contains(b.getWorld())) return;
        for (LocationSaveable shrine : DMiscUtil.getAllShrines()) {
            if ((DMiscUtil.toWriteLocation(b.getLocation())).equalsApprox((shrine)) && e.isSticky()) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void shrineExplode(final EntityExplodeEvent e) {
        if (!DSettings.getEnabledWorlds().contains(e.getLocation().getWorld())) return;
        try {
            // Remove shrine blocks from explosions
            Iterator<Block> i = e.blockList().iterator();
            while (i.hasNext()) {
                Block b = i.next();
                if (!DMiscUtil.canLocationPVP(b.getLocation())) i.remove();
                for (LocationSaveable center : DMiscUtil.getAllShrines()) {
                    if ((DMiscUtil.toWriteLocation(b.getLocation())).equalsApprox(center)) i.remove();
                }
            }
        } catch (Exception ignored) {
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerTribute(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!DSettings.getEnabledWorlds().contains(e.getClickedBlock().getWorld())) return;
        if (e.getClickedBlock().getType() != Material.GOLD_BLOCK) return;
        if (!DMiscUtil.isFullParticipant(e.getPlayer())) return;
        // check if block is shrine
        String deityname = DMiscUtil.getDeityAtShrine(DMiscUtil.toWriteLocation(e.getClickedBlock().getLocation()));
        if (deityname == null) return;
        // check if player has deity
        Player p = e.getPlayer();
        for (Deity d : DMiscUtil.getDeities(p))
            if (d.getName().equalsIgnoreCase(deityname)) {
                // open the tribute inventory
                Inventory ii = DMiscUtil.getPlugin().getServer().createInventory(p, 27, "Tributes");
                p.openInventory(ii);
                DSave.saveData(p, deityname.toUpperCase() + "_TRIBUTE_", DMiscUtil.getOwnerOfShrine(DMiscUtil.toWriteLocation(e.getClickedBlock().getLocation())));
                e.setCancelled(true);
                return;
            }
        p.sendMessage(ChatColor.YELLOW + "You must be allied with " + deityname + " in order to tribute here.");
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void shrineAlerts(PlayerMoveEvent e) {
        if (!e.getFrom().getWorld().equals(e.getTo().getWorld())) return;
        if (!DSettings.getEnabledWorlds().contains(e.getFrom().getWorld())) return;
        if (e.getFrom().distance(e.getTo()) < 0.1) return;
        for (UUID player : DMiscUtil.getFullParticipants()) {
            if (DMiscUtil.getShrines(player) != null)
                for (LocationSaveable center : DMiscUtil.getShrines(player).values()) {
                    // Check for world errors
                    if (!DMiscUtil.toLocation(center).getWorld().equals(e.getPlayer().getWorld())) return;
                    if (e.getFrom().getWorld() != DMiscUtil.toLocation(center).getWorld()) return;
                /*
                 * Outside coming in
				 */
                    if (e.getFrom().distance(DMiscUtil.toLocation(center)) > RADIUS) {
                        if (DMiscUtil.toLocation(center).distance(e.getTo()) <= RADIUS) {
                            e.getPlayer().sendMessage(ChatColor.GRAY + "You have entered " + DMiscUtil.getLastKnownName(player) + "'s shrine to " + ChatColor.YELLOW + DMiscUtil.getDeityAtShrine(center) + ChatColor.GRAY + ".");
                            return;
                        }
                    }
                /*
                 * Leaving
				 */
                    else if (e.getFrom().distance(DMiscUtil.toLocation(center)) <= RADIUS) {
                        if (DMiscUtil.toLocation(center).distance(e.getTo()) > RADIUS) {
                            e.getPlayer().sendMessage(ChatColor.GRAY + "You have left a shrine.");
                            return;
                        }
                    }
                }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void tributeSuccess(InventoryCloseEvent e) {
        if (!DSettings.getEnabledWorlds().contains(e.getPlayer().getWorld())) return;
        if (!(e.getPlayer() instanceof Player)) return;
        Player p = (Player) e.getPlayer();
        if (!DMiscUtil.isFullParticipant(p)) return;
        // continue if tribute chest
        if (!e.getInventory().getName().equals("Tributes")) return;
        // get which deity tribute goes to
        String togive = null;
        for (UUID player : DMiscUtil.getFullParticipants()) {
            for (Deity d : DMiscUtil.getDeities(player)) {
                if (DSave.hasData(player, d.getName().toUpperCase() + "_TRIBUTE_")) {
                    togive = d.getName();
                    break;
                }
            }
        }
        if (togive == null) return;
        UUID creator = (UUID) DSave.removeData(p, togive.toUpperCase() + "_TRIBUTE_"); // get the creator of the shrine
        // calculate value of chest
        int value = 0;
        int items = 0;
        for (ItemStack ii : e.getInventory().getContents()) {
            if (ii != null) {
                value += DMiscUtil.getValue(ii);
                items++;
            }
        }
        if (togive.equals("?????")) value *= 1.54;
        value *= FAVORMULTIPLIER;
        // give devotion
        int dbefore = DMiscUtil.getDevotion(p, togive);
        DMiscUtil.setDevotion(p, togive, DMiscUtil.getDevotion(p, togive) + value);
        DMiscUtil.setDevotion(creator, togive, DMiscUtil.getDevotion(creator, togive) + value / 7);
        // give favor
        int fbefore = DMiscUtil.getFavorCap(p);
        DMiscUtil.setFavorCap(p, DMiscUtil.getFavorCap(p) + value / 5);
        // devotion lock TODO
        if (dbefore < DMiscUtil.getDevotion(p, togive))
            p.sendMessage(ChatColor.YELLOW + "Your Devotion for " + togive + " has increased to " + DMiscUtil.getDevotion(p, togive) + ".");
        if (fbefore < DMiscUtil.getFavorCap(p))
            p.sendMessage(ChatColor.YELLOW + "Your Favor Cap has increased to " + DMiscUtil.getFavorCap(p) + ".");
        if ((fbefore == DMiscUtil.getFavorCap(p)) && (dbefore == DMiscUtil.getDevotion(p, togive)) && (items > 0))
            p.sendMessage(ChatColor.YELLOW + "Your tributes were insufficient for " + togive + "'s blessings.");
        DLevels.levelProcedure(p);
        // clear inventory
        e.getInventory().clear();
    }
}
