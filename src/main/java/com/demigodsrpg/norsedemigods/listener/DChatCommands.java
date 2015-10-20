package com.demigodsrpg.norsedemigods.listener;

import com.demigodsrpg.norsedemigods.DMisc;
import com.demigodsrpg.norsedemigods.Deity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
            String str = "";
            if (e.getMessage().split(" ").length < 2) {
                ChatColor color = ChatColor.GREEN;
                if ((p.getHealth() / p.getMaxHealth()) < 0.25) color = ChatColor.RED;
                else if ((p.getHealth() / p.getMaxHealth()) < 0.5) color = ChatColor.YELLOW;
                str = "-- Your HP " + color + "" + p.getHealth() + "/" + p.getMaxHealth() + ChatColor.YELLOW + " Favor " + DMisc.getFavor(p) + "/" + DMisc.getFavorCap(p);
                if (DMisc.getActiveEffects(p.getUniqueId()).size() > 0) {
                    Map<String, Double> effects = DMisc.getActiveEffects(p.getUniqueId());
                    str += ChatColor.WHITE + " Active effects:";
                    for (Map.Entry<String, Double> stt : effects.entrySet())
                        str += " " + stt.getKey() + "[" + ((stt.getValue() - System.currentTimeMillis()) / 1000) + "s]";
                }
            }
            try {
                String other = e.getMessage().split(" ")[1];
                if (other != null) other = DMisc.getDemigodsPlayer(other).getLastKnownName();
                if ((other != null) && DMisc.isFullParticipant(other)) {
                    Player otherPlayer = Bukkit.getPlayer(other);
                    if (otherPlayer != null) {
                        p.sendMessage(other + " -- " + DMisc.getAllegiance(otherPlayer));
                        if (DMisc.hasDeity(p, "Heimdallr") || DMisc.hasDeity(p, "Dis")) {
                            String st = ChatColor.GRAY + "Deities:";
                            for (Deity d : DMisc.getDeities(otherPlayer))
                                st += " " + d.getName();
                            p.sendMessage(st);
                            p.sendMessage(ChatColor.GRAY + "HP " + otherPlayer.getHealth() + "/" +
                                    otherPlayer.getMaxHealth() + " Favor " + DMisc.getFavor(otherPlayer) + "/" +
                                    DMisc.getFavorCap(otherPlayer));
                            if (DMisc.getActiveEffects(otherPlayer.getUniqueId()).size() > 0) {
                                Map<String, Double> fx = DMisc.getActiveEffects(otherPlayer.getUniqueId());
                                str += ChatColor.GRAY + " Active effects:";
                                for (Map.Entry<String, Double> stt : fx.entrySet())
                                    str += " " + stt.getKey() + "[" + ((stt.getValue() - System.currentTimeMillis()) / 1000) + "s]";
                            }
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

    private void dg(Player p, AsyncPlayerChatEvent e) {
        HashMap<String, ArrayList<String>> alliances = new HashMap<String, ArrayList<String>>();
        DMisc.getPlugin().getServer().getOnlinePlayers().stream().filter(DMisc::isFullParticipant).forEach(pl -> {
            if (!alliances.containsKey(DMisc.getAllegiance(pl).toUpperCase())) {
                alliances.put(DMisc.getAllegiance(pl).toUpperCase(), new ArrayList<>());
            }
            alliances.get(DMisc.getAllegiance(pl).toUpperCase()).add(pl.getName());
        });
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
