package com.demigodsrpg.norsedemigods.deity.jotunn;

import com.demigodsrpg.norsedemigods.DMisc;
import com.demigodsrpg.norsedemigods.Deity;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class Dis implements Deity {
    private static final long serialVersionUID = -2472769863144336856L;
    private final UUID PLAYER;

    private static final int SKILLCOST = 310;
    private static final int SKILLDELAY = 2400; // milliseconds
    private static final int ULTIMATECOST = 6000;
    private static final int ULTIMATECOOLDOWNMAX = 1200; // seconds
    private static final int ULTIMATECOOLDOWNMIN = 500;

    private static final String skillname = "Swap";
    private static final String ult = "Congregate";

    private boolean SKILL = false;
    private Material SKILLBIND = null;
    private long SKILLTIME;
    private long ULTIMATETIME;
    private long LASTCHECK;

    public Dis(UUID player) {
        PLAYER = player;
        SKILLTIME = System.currentTimeMillis();
        ULTIMATETIME = System.currentTimeMillis();
        LASTCHECK = System.currentTimeMillis();
    }

    @Override
    public String getName() {
        return "Dis";
    }

    @Override
    public UUID getPlayerId() {
        return PLAYER;
    }

    @Override
    public String getDefaultAlliance() {
        return "Jotunn";
    }

    @Override
    public void printInfo(Player p) {
        if (DMisc.isFullParticipant(p) && DMisc.hasDeity(p, getName())) {
            int devotion = DMisc.getDevotion(p, getName());
            int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) * ((double) DMisc.getAscensions(p) / 100)));
            p.sendMessage("--" + ChatColor.GOLD + getName() + ChatColor.GRAY + "[" + devotion + "]");
            p.sendMessage(":Use " + ChatColor.YELLOW + "qd <name>" + ChatColor.WHITE + " for detailed information about any player");
            p.sendMessage(":Click a target player or mob to switch locations with them.");
            p.sendMessage(ChatColor.GREEN + "/swap" + ChatColor.YELLOW + "Costs " + SKILLCOST + " Favor.");
            if (((Dis) DMisc.getDeity(p, getName())).SKILLBIND != null)
                p.sendMessage(ChatColor.AQUA + "    Bound to " + ((Dis) DMisc.getDeity(p, getName())).SKILLBIND.name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to bind this skill to an item.");
            p.sendMessage(":Call all AEsir and Jotunn together for an assembly at your location.");
            p.sendMessage("Players will be temporarily immune to damage after teleporting.");
            p.sendMessage("Only consenting players will be teleported. " + ChatColor.GREEN + "/congregate");
            p.sendMessage(ChatColor.YELLOW + "Costs " + ULTIMATECOST + " Favor. Cooldown time: " + t + " seconds.");
            return;
        }
        p.sendMessage("--" + getName());
        p.sendMessage("Passive: " + ChatColor.YELLOW + "qd" + ChatColor.WHITE + " gives more detail on targets.");
        p.sendMessage("Active: Change positions with a target animal or player. " + ChatColor.GREEN + "/swap");
        p.sendMessage(ChatColor.YELLOW + "Costs " + SKILLCOST + " Favor. Can bind.");
        p.sendMessage("Ultimate: Your dís calls together all AEsir and Jotunn to you.");
        p.sendMessage("Requires other players' consent." + ChatColor.GREEN + "/congregate " + ChatColor.YELLOW + "Costs " + ULTIMATECOST + " Favor. Has cooldown.");
        p.sendMessage(ChatColor.YELLOW + "Select item: compass");
    }

    @Override
    public void onEvent(Event ee) {
        if (ee instanceof PlayerInteractEvent) {
            PlayerInteractEvent e = (PlayerInteractEvent) ee;
            Player p = e.getPlayer();
            if (!DMisc.isFullParticipant(p) || !DMisc.hasDeity(p, getName())) return;
            if (SKILL || ((p.getItemInHand() != null) && (p.getItemInHand().getType() == SKILLBIND))) {
                if (SKILLTIME > System.currentTimeMillis()) return;
                SKILLTIME = System.currentTimeMillis() + SKILLDELAY;
                if (DMisc.getFavor(p) >= SKILLCOST) {
                    if (swap()) {
                        DMisc.setFavor(p, DMisc.getFavor(p) - SKILLCOST);
                    } else p.sendMessage(ChatColor.YELLOW + "No target found, or you are in a no-PVP zone.");
                } else {
                    p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor.");
                    SKILL = false;
                }
            }
        }
    }

    @Override
    public void onCommand(Player P, String str, String[] args, boolean bind) {
        final Player p = P;
        if (DMisc.hasDeity(p, getName())) {
            if (str.equalsIgnoreCase(skillname)) {
                if (bind) {
                    if (SKILLBIND == null) {
                        if (DMisc.isBound(p, p.getItemInHand().getType()))
                            p.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                        if (p.getItemInHand().getType() == Material.AIR)
                            p.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                        else {
                            DMisc.registerBind(p, p.getItemInHand().getType());
                            SKILLBIND = p.getItemInHand().getType();
                            p.sendMessage(ChatColor.YELLOW + "" + skillname + " is now bound to " + p.getItemInHand().getType().name() + ".");
                        }
                    } else {
                        DMisc.removeBind(p, SKILLBIND);
                        p.sendMessage(ChatColor.YELLOW + "" + skillname + " is no longer bound to " + SKILLBIND.name() + ".");
                        SKILLBIND = null;
                    }
                    return;
                }
                if (SKILL) {
                    SKILL = false;
                    p.sendMessage(ChatColor.YELLOW + "" + skillname + " is no longer active.");
                } else {
                    SKILL = true;
                    p.sendMessage(ChatColor.YELLOW + "" + skillname + " is now active.");
                }
            } else if (str.equalsIgnoreCase(ult)) {
                long TIME = ULTIMATETIME;
                if (System.currentTimeMillis() < TIME) {
                    p.sendMessage(ChatColor.YELLOW + "You cannot use " + ult + " again for " + ((((TIME) / 1000) - (System.currentTimeMillis() / 1000))) / 60 + " minutes");
                    p.sendMessage(ChatColor.YELLOW + "and " + ((((TIME) / 1000) - (System.currentTimeMillis() / 1000)) % 60) + " seconds.");
                    return;
                }
                if (DMisc.getFavor(p) >= ULTIMATECOST) {
                    for (Player pl : p.getWorld().getPlayers()) {
                        if (DMisc.isFullParticipant(pl) && DMisc.getActiveEffectsList(pl.getUniqueId()).contains("Congregate")) {
                            p.sendMessage(ChatColor.YELLOW + "Congregate is already in effect.");
                            return;
                        }
                    }
                    int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) * ((double) DMisc.getAscensions(p) / 100)));
                    ULTIMATETIME = System.currentTimeMillis() + (t * 1000);
                    int n = congregate();
                    if (n > 0) {
                        p.sendMessage(ChatColor.GOLD + "A dís has called upon " + n + " players to assemble at your location.");
                        DMisc.setFavor(p, DMisc.getFavor(p) - ULTIMATECOST);
                    } else p.sendMessage(ChatColor.YELLOW + "There are no players to assemble.");
                } else p.sendMessage(ChatColor.YELLOW + "" + ult + " requires " + ULTIMATECOST + " Favor.");
            }
        }
    }

    @Override
    public void onSyncTick(long timeSent) {
        if (timeSent > LASTCHECK + 1000) {
            LASTCHECK = timeSent;
        }
    }

    private boolean swap() {
        Player p = DMisc.getOnlinePlayer(getPlayerId());
        if (!DMisc.canTarget(p, p.getLocation())) return false;
        LivingEntity target = DMisc.getTargetLivingEntity(p, 4);
        if (target == null) return false;
        if (!DMisc.canTarget(target, target.getLocation())) return false;
        Location between = p.getLocation();
        DMisc.horseTeleport(p, target.getLocation());
        if (target instanceof Player) DMisc.horseTeleport((Player) target, between);
        else target.teleport(between);
        return true;
    }

    private int congregate() {
        Player p = DMisc.getOnlinePlayer(getPlayerId());
        DMisc.addActiveEffect(p.getUniqueId(), "Congregate Call", 60);
        int count = 0;
        for (Player pl : p.getWorld().getPlayers()) {
            if (DMisc.isFullParticipant(pl)) {
                count++;
                if (!p.equals(pl) && !DMisc.getActiveEffectsList(pl.getUniqueId()).contains("Congregate")) {
                    pl.sendMessage(ChatColor.GOLD + "A dís has called for an assembly of deities at " + p.getName() + "'s location.");
                    pl.sendMessage(ChatColor.GOLD + "Type " + ChatColor.WHITE + "/assemble" + ChatColor.GOLD + " to be teleported.");
                    pl.sendMessage(ChatColor.GOLD + "You will be immune to damage upon arrival for a short time.");
                    pl.sendMessage(ChatColor.GRAY + "You have one minute to answer the invitation.");
                    pl.sendMessage(ChatColor.GRAY + "To see how much time is left to respond, use " + ChatColor.WHITE + "qd" + ChatColor.GRAY + ".");
                    DMisc.addActiveEffect(pl.getUniqueId(), "Congregate", 60);
                }
            }
        }
        return count;
    }

    @Override
    public boolean canTribute() {
        return false;
    }
}
