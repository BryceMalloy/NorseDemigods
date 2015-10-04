package com.demigodsrpg.norsedemigods.listener;

import com.demigodsrpg.norsedemigods.DMisc;
import com.demigodsrpg.norsedemigods.Deity;
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

        if (!DMisc.isFullParticipant(p)) return;
        if (e.getMessage().contains("qd")) qd(p, e);
        else if (e.getMessage().equals("dg")) dg(p, e);
    }

    private void qd(Player p, AsyncPlayerChatEvent e) {
        if ((e.getMessage().charAt(0) == 'q') && (e.getMessage().charAt(1) == 'd')) {
            String str;
            if (p.getHealth() > 0) {
                ChatColor color = ChatColor.GREEN;
                if ((DMisc.getHP(p) / DMisc.getMaxHP(p)) < 0.25) color = ChatColor.RED;
                else if ((DMisc.getHP(p) / DMisc.getMaxHP(p)) < 0.5) color = ChatColor.YELLOW;
                str = "-- Your HP " + color + "" + DMisc.getHP(p) + "/" + DMisc.getMaxHP(p) + ChatColor.YELLOW + " Favor " + DMisc.getFavor(p) + "/" + DMisc.getFavorCap(p);
                if (DMisc.getActiveEffects(p.getUniqueId()).size() > 0) {
                    HashMap<String, Long> effects = DMisc.getActiveEffects(p.getUniqueId());
                    str += ChatColor.WHITE + " Active effects:";
                    for (Map.Entry<String, Long> stt : effects.entrySet())
                        str += " " + stt.getKey() + "[" + ((stt.getValue() - System.currentTimeMillis()) / 1000) + "s]";
                }
                try {
                    String other = e.getMessage().split(" ")[1];
                    if (other != null) other = DMisc.getDemigodsPlayer(other);
                    if ((other != null) && DMisc.isFullParticipant(other)) {
                        UUID otherId = DMisc.getDemigodsPlayerId(other);
                        p.sendMessage(other + " -- " + DMisc.getAllegiance(otherId));
                        if (DMisc.hasDeity(p, "Athena") || DMisc.hasDeity(p, "Themis")) {
                            String st = ChatColor.GRAY + "Deities:";
                            for (Deity d : DMisc.getDeities(otherId))
                                st += " " + d.getName();
                            p.sendMessage(st);
                            p.sendMessage(ChatColor.GRAY + "HP " + DMisc.getHP(otherId) + "/" + DMisc.getMaxHP(otherId) + " Favor " + DMisc.getFavor(otherId) + "/" + DMisc.getFavorCap(otherId));
                            if (DMisc.getActiveEffects(otherId).size() > 0) {
                                HashMap<String, Long> fx = DMisc.getActiveEffects(otherId);
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
        for (Player pl : DMisc.getPlugin().getServer().getOnlinePlayers()) {
            if (DSettings.getEnabledWorlds().contains(pl.getWorld())) {
                if (DMisc.isFullParticipant(pl)) {
                    if (!alliances.containsKey(DMisc.getAllegiance(pl).toUpperCase())) {
                        alliances.put(DMisc.getAllegiance(pl).toUpperCase(), new ArrayList<String>());
                    }
                    alliances.get(DMisc.getAllegiance(pl).toUpperCase()).add(pl.getName());
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
