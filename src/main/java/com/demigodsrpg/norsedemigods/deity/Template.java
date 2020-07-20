package com.demigodsrpg.norsedemigods.deity;

import com.demigodsrpg.norsedemigods.DMisc;
import com.demigodsrpg.norsedemigods.Deity;
import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Optional;

public class Template implements Deity {
    private static final int SKILLCOST = 120;
    private static final int SKILLDELAY = 1250; // milliseconds
    private static final int ULTIMATECOST = 10000;
    private static final int ULTIMATECOOLDOWNMAX = 180; // seconds
    private static final int ULTIMATECOOLDOWNMIN = 60;

    private static final String skillname = "test";
    private static final String ult = "test ult";

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
        if (DMisc.isFullParticipant(p) && DMisc.hasDeity(p, getName())) {
            int devotion = DMisc.getDevotion(p, getName());
            p.sendMessage("--" + ChatColor.GOLD + getName() + ChatColor.GRAY + "[" + devotion + "]");
            return;
        }
        p.sendMessage("--" + getName());
        p.sendMessage("Passive: ");
        p.sendMessage("Active: ");
        p.sendMessage("Ultimate: ");
        p.sendMessage(ChatColor.YELLOW + "Select item: ");
    }

    @Override
    public void onEvent(Event ee) {
        if (ee instanceof PlayerInteractEvent) {
            Player p = ((PlayerInteractEvent) ee).getPlayer();
            PlayerDataSaveable saveable = getBackend().getPlayerDataRegistry().fromPlayer(p);
            if (!DMisc.isFullParticipant(p) || !DMisc.hasDeity(p, getName())) return;
            boolean active = saveable.getAbilityData(skillname, AD.ACTIVE, false);
            Optional<Material> opBind = saveable.getBind(skillname);
            if (active || ((p.getItemInHand() != null) &&
                    (opBind.isPresent() && p.getItemInHand().getType() == opBind.get()))) {
                double time = saveable.getAbilityData(skillname, AD.TIME, (double) System.currentTimeMillis());
                if (System.currentTimeMillis() < time) return;
                saveable.setAbilityData(skillname, AD.TIME, System.currentTimeMillis() + SKILLDELAY);
                if (DMisc.getFavor(p) >= SKILLCOST) {
                    /*
                     * Skill
                     */
                    DMisc.setFavor(p, DMisc.getFavor(p) - SKILLCOST);
                } else {
                    p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor.");
                    saveable.setAbilityData(skillname, "enabled", false);
                }
            }
        }
    }

    @Override
    public void onCommand(Player P, String str, String[] args, boolean bind) {
        PlayerDataSaveable saveable = getBackend().getPlayerDataRegistry().fromPlayer(P);
        if (DMisc.hasDeity(P, getName())) {
            if (str.equalsIgnoreCase(skillname)) {
                if (bind) {
                    Optional opBind = saveable.getAbilityData(skillname, "bind");
                    if (!opBind.isPresent()) {
                        if (DMisc.isBound(P, P.getItemInHand().getType()))
                            P.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                        if (P.getItemInHand().getType() == Material.AIR)
                            P.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                        else {
                            saveable.setBind(skillname, P.getItemInHand().getType());
                            P.sendMessage(ChatColor.YELLOW + "" + skillname + " is now bound to " +
                                    P.getItemInHand().getType().name() + ".");
                        }
                    } else {
                        saveable.removeBind(skillname);
                        P.sendMessage(ChatColor.YELLOW + "" + skillname + " is no longer bound.");
                    }
                    return;
                }
                Optional opEnabled = saveable.getAbilityData(skillname, "enabled");
                if (opEnabled.isPresent() && (boolean) opEnabled.get()) {
                    saveable.setAbilityData(skillname, AD.ACTIVE, false);
                    P.sendMessage(ChatColor.YELLOW + "" + skillname + " is no longer active.");
                } else {
                    saveable.setAbilityData(skillname, AD.ACTIVE, true);
                    P.sendMessage(ChatColor.YELLOW + "" + skillname + " is now active.");
                }
            } else if (str.equalsIgnoreCase(ult)) {
                Optional opTime = saveable.getAbilityData(ult, AD.TIME);
                if (opTime.isPresent() && System.currentTimeMillis() < (double) opTime.get()) {
                    double TIME = (double) opTime.get();
                    P.sendMessage(ChatColor.YELLOW + "You cannot use " + ult + " again for " + ((((TIME) / 1000) -
                            (System.currentTimeMillis() / 1000))) / 60 + " minutes");
                    P.sendMessage(ChatColor.YELLOW + "and " + ((((TIME) / 1000) -
                            (System.currentTimeMillis() / 1000)) % 60) + " seconds.");
                    return;
                }
                if (DMisc.getFavor(P) >= ULTIMATECOST) {
                    int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) *
                            ((double) DMisc.getAscensions(P) / 100)));
                    saveable.setAbilityData(ult, AD.TIME, System.currentTimeMillis() + (t * 1000));
                    /*
                     * Ultimate code
                     */
                    DMisc.setFavor(P, DMisc.getFavor(P) - ULTIMATECOST);
                } else P.sendMessage(ChatColor.YELLOW + "" + ult + " requires " + ULTIMATECOST + " Favor.");
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
