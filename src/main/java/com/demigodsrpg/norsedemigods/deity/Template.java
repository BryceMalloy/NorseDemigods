package com.demigodsrpg.norsedemigods.deity;

import com.demigodsrpg.norsedemigods.DMisc;
import com.demigodsrpg.norsedemigods.Deity;
import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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

    @EventHandler
    public void onInteractEvent(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        PlayerDataSaveable saveable = getBackend().getPlayerDataRegistry().fromPlayer(p);
        if (!DMisc.isFullParticipant(p) || !DMisc.hasDeity(p, getName())) return;
        Optional opEnabled = saveable.getAbilityData(skillname, "enabled");
        Optional<Material> opBind = saveable.getBind(skillname);
        if (opEnabled.isPresent() && (boolean) opEnabled.get() || ((p.getItemInHand() != null) &&
                (opBind.isPresent() && p.getItemInHand().getType() == opBind.get()))) {
            Optional opTime = saveable.getAbilityData(skillname, "time");
            if (opTime.isPresent() && (double) opTime.get() > System.currentTimeMillis()) return;
            saveable.setAbilityData(getBackend(), skillname, "time", System.currentTimeMillis() + SKILLDELAY);
            if (DMisc.getFavor(p) >= SKILLCOST) {
                /*
                 * Skill
                 */
                DMisc.setFavor(p, DMisc.getFavor(p) - SKILLCOST);
            } else {
                p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor.");
                saveable.setAbilityData(getBackend(), skillname, "enabled", false);
            }
        }
    }

    @Override
    public void onCommand(Player P, String str, String[] args, boolean bind) {
        final Player p = P;
        PlayerDataSaveable saveable = getBackend().getPlayerDataRegistry().fromPlayer(p);
        if (DMisc.hasDeity(p, getName())) {
            if (str.equalsIgnoreCase(skillname)) {
                if (bind) {
                    Optional opBind = saveable.getAbilityData(skillname, "bind");
                    if (opBind.isPresent()) {
                        if (DMisc.isBound(p, p.getItemInHand().getType()))
                            p.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                        if (p.getItemInHand().getType() == Material.AIR)
                            p.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                        else {
                            saveable.setBind(getBackend(), skillname, p.getItemInHand().getType());
                            p.sendMessage(ChatColor.YELLOW + "" + skillname + " is now bound to " +
                                    p.getItemInHand().getType().name() + ".");
                        }
                    } else {
                        saveable.removeBind(getBackend(), skillname);
                        p.sendMessage(ChatColor.YELLOW + "" + skillname + " is no longer bound.");
                    }
                    return;
                }
                Optional opEnabled = saveable.getAbilityData(skillname, "enabled");
                if (opEnabled.isPresent() && (boolean) opEnabled.get()) {
                    saveable.setAbilityData(getBackend(), skillname, "enabled", false);
                    p.sendMessage(ChatColor.YELLOW + "" + skillname + " is no longer active.");
                } else {
                    saveable.setAbilityData(getBackend(), "test", "enabled", true);
                    p.sendMessage(ChatColor.YELLOW + "" + skillname + " is now active.");
                }
            } else if (str.equalsIgnoreCase(ult)) {
                Optional opTime = saveable.getAbilityData(ult, "time");
                if (opTime.isPresent() && System.currentTimeMillis() < (double) opTime.get()) {
                    double TIME = (double) opTime.get();
                    p.sendMessage(ChatColor.YELLOW + "You cannot use " + ult + " again for " + ((((TIME) / 1000) -
                            (System.currentTimeMillis() / 1000))) / 60 + " minutes");
                    p.sendMessage(ChatColor.YELLOW + "and " + ((((TIME) / 1000) -
                            (System.currentTimeMillis() / 1000)) % 60) + " seconds.");
                    return;
                }
                if (DMisc.getFavor(p) >= ULTIMATECOST) {
                    int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) *
                            ((double) DMisc.getAscensions(p) / 100)));
                    saveable.setAbilityData(getBackend(), ult, "time", System.currentTimeMillis() + (t * 1000));
                    /*
					 * Ultimate code
					 */
                    DMisc.setFavor(p, DMisc.getFavor(p) - ULTIMATECOST);
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
