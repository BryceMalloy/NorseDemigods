package com.demigodsrpg.norsedemigods.listener;

import com.demigodsrpg.norsedemigods.DMisc;
import com.demigodsrpg.norsedemigods.Deity;
import com.demigodsrpg.norsedemigods.Setting;
import com.google.common.collect.Lists;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.List;

public class DLevels implements Listener {
    @SuppressWarnings({"incomplete-switch"})
    @EventHandler(priority = EventPriority.HIGHEST)
    public void gainEXP(BlockBreakEvent e) {
        if (e.getPlayer() != null) {
            Player p = e.getPlayer();
            try {
                if (!DMisc.canWorldGuardBuild(p, e.getBlock().getLocation())) return;
            } catch (Exception ignored) {
            }
            if (!DMisc.isFullParticipant(p)) return;
            int value = 0;
            switch (e.getBlock().getType()) {
                case DIAMOND_ORE:
                    if (e.getExpToDrop() != 0) value = 100;
                    break;
                case COAL_ORE:
                    if (e.getExpToDrop() != 0) value = 3;
                    break;
                case LAPIS_ORE:
                    if (e.getExpToDrop() != 0) value = 30;
                    break;
                case OBSIDIAN:
                    value = 15;
                    break;
                case REDSTONE_ORE:
                    if (e.getExpToDrop() != 0) value = 5;
                    break;
            }
            value *= Setting.EXP_MULTIPLIER;

            List<Deity> deities = Lists.newArrayList(DMisc.getTributeableDeities(p));
            if (!deities.isEmpty()) {
                Deity d = deities.get((int) Math.floor(Math.random() * deities.size()));
                DMisc.setDevotion(p, d, DMisc.getDevotion(p, d) + value);
                levelProcedure(p);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void gainEXP(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            try {
                if (!DMisc.canWorldGuardBuild(p, e.getEntity().getLocation())) return;
            } catch (Exception ex) {
                // Do nothing
            }
            if (!DMisc.isFullParticipant(p)) return;
            if (!DMisc.canTarget(e.getEntity(), e.getEntity().getLocation())) {
                return;
            }
            List<Deity> deities = Lists.newArrayList(DMisc.getTributeableDeities(p));
            if (!deities.isEmpty()) {
                Deity d = deities.get((int) Math.floor(Math.random() * deities.size()));
                DMisc.setDevotion(p, d, (int) (DMisc.getDevotion(p, d) + e.getDamage() * Setting.EXP_MULTIPLIER));
                levelProcedure(p);
            } else if (!DMisc.getDeities(p).isEmpty()) {
                Deity d = deities.get((int) Math.floor(Math.random() * DMisc.getDeities(p).size()));
                DMisc.setDevotion(p, d, (int) (DMisc.getDevotion(p, d) + e.getDamage() * Setting.EXP_MULTIPLIER));
                levelProcedure(p);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void deathPenalty(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (!DMisc.isFullParticipant(p)) return;
        double reduced = 0.1; // TODO
        long before = DMisc.getDevotion(p);
        List<Deity> deities = Lists.newArrayList(DMisc.getTributeableDeities(p));
        for (Deity d : deities) {
            int reduceamt = (int) Math.round(DMisc.getDevotion(p, d) * reduced * Setting.EXP_MULTIPLIER);
            if (reduceamt > Setting.LOSS_LIMIT) reduceamt = Setting.LOSS_LIMIT;
            DMisc.setDevotion(p, d, DMisc.getDevotion(p, d) - reduceamt);
        }
        if (deities.size() == 1)
            p.sendMessage(ChatColor.DARK_RED + "You have failed in your service to " + deities.get(0).getName() + ".");
        else p.sendMessage(ChatColor.DARK_RED + "You have failed in your service to your deities.");
        if (before != DMisc.getDevotion(p))
            p.sendMessage(ChatColor.DARK_RED + "Your Devotion has been reduced by " + (before - DMisc.getDevotion(p)) + ".");
        DMisc.setHP(p, 0);
    }

    public static void levelProcedure(Player p) {
        if (DMisc.isFullParticipant(p.getUniqueId())) if (DMisc.getAscensions(p) >= Setting.ASCENSION_CAP) return;
        while ((DMisc.getDevotion(p) >= DMisc.costForNextAscension(p.getUniqueId())) && (DMisc.getAscensions(p) < Setting.ASCENSION_CAP)) {
            DMisc.setMaxHP(p, p.getMaxHealth() + 10);
            DMisc.setHP(p, p.getMaxHealth());
            DMisc.setAscensions(p.getUniqueId(), DMisc.getAscensions(p) + 1);
            p.sendMessage(ChatColor.AQUA + "Congratulations! Your Ascensions increased to " + DMisc.getAscensions(p) + ".");
            p.sendMessage(ChatColor.YELLOW + "Your maximum HP has increased to " + p.getMaxHealth() + ".");
        }
    }
}
