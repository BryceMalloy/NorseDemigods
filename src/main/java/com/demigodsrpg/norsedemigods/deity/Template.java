package com.demigodsrpg.norsedemigods.deity;

import com.demigodsrpg.norsedemigods.util.DMiscUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

public class Template implements Deity {
    private static final long serialVersionUID = -2472769863144336856L;

    private static final int SKILLCOST = 120;
    private static final int SKILLDELAY = 1250; // milliseconds
    private static final int ULTIMATECOST = 10000;
    private static final int ULTIMATECOOLDOWNMAX = 180; // seconds
    private static final int ULTIMATECOOLDOWNMIN = 60;

    private static final String skillname = "";
    private static final String ult = "";

    private boolean SKILL = false;
    private Material SKILLBIND = null;
    private long SKILLTIME;
    private long ULTIMATETIME;

    @Override
    public String getName() {
        return "Name";
    }

    @Override
    public String getDefaultAlliance() {
        return "";
    }

    @Override
    public void printInfo(Player p) {
        if (DMiscUtil.isFullParticipant(p) && DMiscUtil.hasDeity(p, getName())) {
            int devotion = DMiscUtil.getDevotion(p, getName());
            p.sendMessage("--" + ChatColor.GOLD + getName() + ChatColor.GRAY + "[" + devotion + "]");
            return;
        }
        p.sendMessage("--" + getName());
        p.sendMessage("Passive: ");
        p.sendMessage("Active: ");
        p.sendMessage("Ultimate: ");
        p.sendMessage(ChatColor.YELLOW + "Select item: ");
    }

    @EventHandler
    public void onInteractEvent(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!DMiscUtil.isFullParticipant(p) || !DMiscUtil.hasDeity(p, getName())) return;
        if (SKILL || ((p.getItemInHand() != null) && (p.getItemInHand().getType() == SKILLBIND))) {
            if (SKILLTIME > System.currentTimeMillis()) return;
            SKILLTIME = System.currentTimeMillis() + SKILLDELAY;
            if (DMiscUtil.getFavor(p) >= SKILLCOST) {
                /*
                 * Skill
                 */
                DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) - SKILLCOST);
            } else {
                p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor.");
                SKILL = false;
            }
        }
    }

    @Override
    public void onCommand(Player P, String str, String[] args, boolean bind) {
        final Player p = P;
        if (DMiscUtil.hasDeity(p, getName())) {
            if (str.equalsIgnoreCase(skillname)) {
                if (bind) {
                    if (SKILLBIND == null) {
                        if (DMiscUtil.isBound(p, p.getItemInHand().getType()))
                            p.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                        if (p.getItemInHand().getType() == Material.AIR)
                            p.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                        else {
                            DMiscUtil.registerBind(p, p.getItemInHand().getType());
                            SKILLBIND = p.getItemInHand().getType();
                            p.sendMessage(ChatColor.YELLOW + "" + skillname + " is now bound to " + p.getItemInHand().getType().name() + ".");
                        }
                    } else {
                        DMiscUtil.removeBind(p, SKILLBIND);
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
                if (DMiscUtil.getFavor(p) >= ULTIMATECOST) {
                    int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) * ((double) DMiscUtil.getAscensions(p) / 100)));
                    ULTIMATETIME = System.currentTimeMillis() + (t * 1000);
                    /*
					 * Ultimate code
					 */
                    DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) - ULTIMATECOST);
                } else p.sendMessage(ChatColor.YELLOW + "" + ult + " requires " + ULTIMATECOST + " Favor.");
            }
        }
    }

    @Override
    public void onSyncTick(long timeSent) {
    }

    @Override
    public boolean canTribute() {
        return false;
    }
}
