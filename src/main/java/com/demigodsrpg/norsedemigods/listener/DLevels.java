package com.demigodsrpg.norsedemigods.listener;

import com.demigodsrpg.norsedemigods.Deity;
import com.demigodsrpg.norsedemigods.util.DMiscUtil;
import com.demigodsrpg.norsedemigods.util.DSettings;
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
import java.util.UUID;

public class DLevels implements Listener {
    static final double MULTIPLIER = DSettings.getSettingDouble("globalexpmultiplier"); // can be modified
    static final int LOSSLIMIT = DSettings.getSettingInt("max_devotion_lost_on_death"); // max devotion lost on death per deity

    @SuppressWarnings({"incomplete-switch"})
    @EventHandler(priority = EventPriority.HIGHEST)
    public void gainEXP(BlockBreakEvent e) {
        if (e.getPlayer() != null) {
            Player p = e.getPlayer();
            try {
                if (!DMiscUtil.canWorldGuardBuild(p, e.getBlock().getLocation())) return;
            } catch (Exception ignored) {
            }
            if (!DSettings.getEnabledWorlds().contains(p.getWorld())) return;
            if (!DMiscUtil.isFullParticipant(p)) return;
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
            value *= MULTIPLIER;

            List<Deity> deities = Lists.newArrayList(DMiscUtil.getTributeableDeities(p));
            if (!deities.isEmpty()) {
                Deity d = deities.get((int) Math.floor(Math.random() * deities.size()));
                DMiscUtil.setDevotion(p, d, DMiscUtil.getDevotion(p, d) + value);
                levelProcedure(p);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void gainEXP(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            Player p = (Player) e.getDamager();
            try {
                if (!DMiscUtil.canWorldGuardBuild(p, e.getEntity().getLocation())) return;
            } catch (Exception ex) {
                // Do nothing
            }
            if (!DMiscUtil.isFullParticipant(p)) return;
            if (!DSettings.getEnabledWorlds().contains(p.getWorld())) return;
            if (!DMiscUtil.canTarget(e.getEntity(), e.getEntity().getLocation())) {
                return;
            }
            List<Deity> deities = Lists.newArrayList(DMiscUtil.getTributeableDeities(p));
            if (!deities.isEmpty()) {
                Deity d = deities.get((int) Math.floor(Math.random() * deities.size()));
                DMiscUtil.setDevotion(p, d, (int) (DMiscUtil.getDevotion(p, d) + e.getDamage() * MULTIPLIER));
                levelProcedure(p);
            } else if (!DMiscUtil.getDeities(p).isEmpty()) {
                Deity d = deities.get((int) Math.floor(Math.random() * DMiscUtil.getDeities(p).size()));
                DMiscUtil.setDevotion(p, d, (int) (DMiscUtil.getDevotion(p, d) + e.getDamage() * MULTIPLIER));
                levelProcedure(p);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void deathPenalty(EntityDeathEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (!DMiscUtil.isFullParticipant(p)) return;
        if (!DSettings.getEnabledWorlds().contains(p.getWorld())) return;
        double reduced = 0.1; // TODO
        long before = DMiscUtil.getDevotion(p);
        List<Deity> deities = Lists.newArrayList(DMiscUtil.getTributeableDeities(p));
        for (Deity d : deities) {
            int reduceamt = (int) Math.round(DMiscUtil.getDevotion(p, d) * reduced * MULTIPLIER);
            if (reduceamt > LOSSLIMIT) reduceamt = LOSSLIMIT;
            DMiscUtil.setDevotion(p, d, DMiscUtil.getDevotion(p, d) - reduceamt);
        }
        if (deities.size() == 1)
            p.sendMessage(ChatColor.DARK_RED + "You have failed in your service to " + deities.get(0).getName() + ".");
        else p.sendMessage(ChatColor.DARK_RED + "You have failed in your service to your deities.");
        if (before != DMiscUtil.getDevotion(p))
            p.sendMessage(ChatColor.DARK_RED + "Your Devotion has been reduced by " + (before - DMiscUtil.getDevotion(p)) + ".");
        DMiscUtil.setHP(p, 0);
    }

    public static void levelProcedure(Player p) {
        levelProcedure(p.getUniqueId());
    }

    public static void levelProcedure(UUID p) {
        if (DMiscUtil.isFullParticipant(p)) if (DMiscUtil.getAscensions(p) >= DMiscUtil.ASCENSIONCAP) return;
        while ((DMiscUtil.getDevotion(p) >= DMiscUtil.costForNextAscension(p)) && (DMiscUtil.getAscensions(p) < DMiscUtil.ASCENSIONCAP)) {
            DMiscUtil.setMaxHP(p, DMiscUtil.getMaxHP(p) + 10);
            DMiscUtil.setHP(p, DMiscUtil.getMaxHP(p));
            DMiscUtil.setAscensions(p, DMiscUtil.getAscensions(p) + 1);

            if (DMiscUtil.getOnlinePlayer(p) != null) {
                DMiscUtil.getOnlinePlayer(p).sendMessage(ChatColor.AQUA + "Congratulations! Your Ascensions increased to " + DMiscUtil.getAscensions(p) + ".");
                DMiscUtil.getOnlinePlayer(p).sendMessage(ChatColor.YELLOW + "Your maximum HP has increased to " + DMiscUtil.getMaxHP(p) + ".");
            }
        }
    }
}
