package com.demigodsrpg.norsedemigods.listener;

import com.demigodsrpg.norsedemigods.deity.Deity;
import com.demigodsrpg.norsedemigods.util.DMiscUtil;
import com.demigodsrpg.norsedemigods.util.DSettings;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DChatCommands implements Listener {
    @EventHandler
    public void onChatCommand(AsyncPlayerChatEvent e) {
        // Define variables
        Player p = e.getPlayer();

        if (!DMiscUtil.isFullParticipant(p)) return;
        if (e.getMessage().contains("qd")) qd(p, e);
        else if (e.getMessage().equals("dg")) dg(p, e);
    }

    private void qd(Player p, AsyncPlayerChatEvent e) {
        if ((e.getMessage().charAt(0) == 'q') && (e.getMessage().charAt(1) == 'd')) {
            String str;
            if (p.getHealth() > 0) {
                ChatColor color = ChatColor.GREEN;
                if ((DMiscUtil.getHP(p) / DMiscUtil.getMaxHP(p)) < 0.25) color = ChatColor.RED;
                else if ((DMiscUtil.getHP(p) / DMiscUtil.getMaxHP(p)) < 0.5) color = ChatColor.YELLOW;
                str = "-- Your HP " + color + "" + DMiscUtil.getHP(p) + "/" + DMiscUtil.getMaxHP(p) + ChatColor.YELLOW + " Favor " + DMiscUtil.getFavor(p) + "/" + DMiscUtil.getFavorCap(p);
                if (DMiscUtil.getActiveEffects(p.getUniqueId()).size() > 0) {
                    HashMap<String, Long> effects = DMiscUtil.getActiveEffects(p.getUniqueId());
                    str += ChatColor.WHITE + " Active effects:";
                    for (Map.Entry<String, Long> stt : effects.entrySet())
                        str += " " + stt.getKey() + "[" + ((stt.getValue() - System.currentTimeMillis()) / 1000) + "s]";
                }
                try {
                    String other = e.getMessage().split(" ")[1];
                    if (other != null) other = DMiscUtil.getDemigodsPlayer(other);
                    if ((other != null) && DMiscUtil.isFullParticipant(other)) {
                        UUID otherId = DMiscUtil.getDemigodsPlayerId(other);
                        p.sendMessage(other + " -- " + DMiscUtil.getAllegiance(otherId));
                        if (DMiscUtil.hasDeity(p, "Athena") || DMiscUtil.hasDeity(p, "Themis")) {
                            String st = ChatColor.GRAY + "Deities:";
                            for (Deity d : DMiscUtil.getDeities(otherId))
                                st += " " + d.getName();
                            p.sendMessage(st);
                            p.sendMessage(ChatColor.GRAY + "HP " + DMiscUtil.getHP(otherId) + "/" + DMiscUtil.getMaxHP(otherId) + " Favor " + DMiscUtil.getFavor(otherId) + "/" + DMiscUtil.getFavorCap(otherId));
                            if (DMiscUtil.getActiveEffects(otherId).size() > 0) {
                                HashMap<String, Long> fx = DMiscUtil.getActiveEffects(otherId);
                                str += ChatColor.GRAY + " Active effects:";
                                for (Map.Entry<String, Long> stt : fx.entrySet())
                                    str += " " + stt.getKey() + "[" + ((stt.getValue() - System.currentTimeMillis()) / 1000) + "s]";
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
                p.sendMessage(str);
                e.getRecipients().clear();
                e.setCancelled(true);
            }
        }
    }

    private void dg(Player p, AsyncPlayerChatEvent e) {
        HashMap<String, ArrayList<String>> alliances = new HashMap<String, ArrayList<String>>();
        for (Player pl : DMiscUtil.getPlugin().getServer().getOnlinePlayers()) {
            if (DSettings.getEnabledWorlds().contains(pl.getWorld())) {
                if (DMiscUtil.isFullParticipant(pl)) {
                    if (!alliances.containsKey(DMiscUtil.getAllegiance(pl).toUpperCase())) {
                        alliances.put(DMiscUtil.getAllegiance(pl).toUpperCase(), new ArrayList<String>());
                    }
                    alliances.get(DMiscUtil.getAllegiance(pl).toUpperCase()).add(pl.getName());
                }
            }
        }
        for (Map.Entry<String, ArrayList<String>> alliance : alliances.entrySet()) {
            String names = "";
            for (String name : alliance.getValue())
                names += " " + name;
            p.sendMessage(ChatColor.YELLOW + alliance.getKey() + ": " + ChatColor.WHITE + names);
        }
        e.getRecipients().clear();
        e.setCancelled(true);
    }
}
