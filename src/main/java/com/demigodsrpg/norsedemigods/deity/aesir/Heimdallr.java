package com.demigodsrpg.norsedemigods.deity.aesir;

import com.demigodsrpg.norsedemigods.Deity;
import com.demigodsrpg.norsedemigods.util.DMiscUtil;
import com.demigodsrpg.norsedemigods.util.DSave;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Heimdallr implements Deity {
    private static final long serialVersionUID = -9039521341663053625L;
    private final UUID PLAYER;

    private static final int SKILLCOST = 100;
    private static final int SKILLDELAY = 3600; // milliseconds
    private static final int ULTIMATECOST = 4000;
    private static final int ULTIMATECOOLDOWNMAX = 500; // seconds
    private static final int ULTIMATECOOLDOWNMIN = 300;

    private boolean SKILL = false;
    private Material SKILLBIND = null;
    private long SKILLTIME;
    private long ULTIMATETIME;

    public Heimdallr(UUID player) {
        PLAYER = player;
    }

    @Override
    public String getName() {
        return "Heimdallr";
    }

    @Override
    public UUID getPlayerId() {
        return PLAYER;
    }

    @Override
    public String getDefaultAlliance() {
        return "AEsir";
    }

    @Override
    public void printInfo(Player p) {
        if (DMiscUtil.isFullParticipant(p) && DMiscUtil.hasDeity(p, getName())) {
            int devotion = DMiscUtil.getDevotion(p, getName());
            // flash range
            int range = (int) Math.ceil(3 * Math.pow(devotion, 0.2));
            // ceasefire range
            int crange = (int) Math.floor(15 * Math.pow(devotion, 0.275));
            // ceasefire duration
            int duration = (int) Math.ceil(10 * Math.pow(devotion, 0.194));
            int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) * ((double) DMiscUtil.getAscensions(p) / DMiscUtil.ASCENSIONCAP)));
            // print
            p.sendMessage("--" + ChatColor.GOLD + getName() + ChatColor.GRAY + "[" + devotion + "]");
            p.sendMessage(":Use " + ChatColor.YELLOW + "qd <name>" + ChatColor.WHITE + " for detailed information about any player");
            p.sendMessage("you are looking at.");
            p.sendMessage(":Left-click to teleport with range " + range + "." + ChatColor.GREEN + " /flash " + ChatColor.YELLOW + "Costs " + SKILLCOST + " Favor.");
            if (((Heimdallr) DMiscUtil.getDeity(p, getName())).SKILLBIND != null)
                p.sendMessage(ChatColor.AQUA + "    Bound to " + ((Heimdallr) DMiscUtil.getDeity(p, getName())).SKILLBIND.name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to bind this skill to an item.");
            p.sendMessage(":Heimdallr silences the battlefield, preventing all damage in range " + crange);
            p.sendMessage("dealt by AEsir and Jotunn alike for " + duration + " seconds." + ChatColor.GREEN + " /ceasefire");
            p.sendMessage(ChatColor.YELLOW + "Costs " + ULTIMATECOST + " Favor. Cooldown time: " + t + " seconds.");
            return;
        }
        p.sendMessage("--" + ChatColor.GOLD + getName());
        p.sendMessage("Passive: " + ChatColor.YELLOW + "qd" + ChatColor.WHITE + " gives more detail on targets.");
        p.sendMessage("Active: Left-click to teleport forward a set distance." + ChatColor.GREEN + "/flash");
        p.sendMessage(ChatColor.YELLOW + "Costs " + SKILLCOST + " Favor. Can bind.");
        p.sendMessage("Ultimate: Heimdallr silences the battlefield, preventing all");
        p.sendMessage("damage nearby for a short duration. " + ChatColor.GREEN + "/ceasefire");
        p.sendMessage(ChatColor.YELLOW + "Costs " + ULTIMATECOST + " Favor. Has cooldown.");
        p.sendMessage(ChatColor.YELLOW + "Select item: book");
    }

    @Override
    public void onEvent(Event ee) {
        if (ee instanceof PlayerInteractEvent) {
            PlayerInteractEvent e = (PlayerInteractEvent) ee;
            Player p = e.getPlayer();
            if (!DMiscUtil.isFullParticipant(p) || !DMiscUtil.hasDeity(p, getName())) return;
            if (SKILL || ((p.getItemInHand() != null) && (p.getItemInHand().getType() == SKILLBIND))) {
                if (SKILLTIME > System.currentTimeMillis()) return;
                SKILLTIME = System.currentTimeMillis() + SKILLDELAY;
                if (DMiscUtil.getFavor(p) >= SKILLCOST) {
                    float pitch = p.getLocation().getPitch();
                    float yaw = p.getLocation().getYaw();
                    int range = (int) Math.ceil(3 * Math.pow(DMiscUtil.getDevotion(p, getName()), 0.2));
                    List<Block> los = p.getLineOfSight((Set) null, 100);
                    Location go = null;
                    if (los.size() - 1 < range) go = los.get(los.size() - 1).getLocation();
                    else go = los.get(range).getLocation();
                    if (go == null) return;
                    go.setY(go.getBlockY() + 1);
                    go.setPitch(pitch);
                    go.setYaw(yaw);
                    if ((go.getBlock().isLiquid() || go.getBlock().isEmpty()) && DMiscUtil.canLocationPVP(go)) {
                        DSave.saveData(p, "temp_flash", true);
                        DMiscUtil.horseTeleport(p, go);
                        DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) - SKILLCOST);
                    }
                } else {
                    p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor.");
                    SKILL = false;
                }
            }
        }
    }

    @Override
    public void onCommand(final Player p, String str, String[] args, boolean bind) {
        if (DMiscUtil.hasDeity(p, getName())) {
            if (str.equalsIgnoreCase("flash")) {
                if (bind) {
                    if (SKILLBIND == null) {
                        if (DMiscUtil.isBound(p, p.getItemInHand().getType()))
                            p.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                        if (p.getItemInHand().getType() == Material.AIR)
                            p.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                        else {
                            DMiscUtil.registerBind(p, p.getItemInHand().getType());
                            SKILLBIND = p.getItemInHand().getType();
                            p.sendMessage(ChatColor.YELLOW + "Flash is now bound to " + p.getItemInHand().getType().name() + ".");
                        }
                    } else {
                        DMiscUtil.removeBind(p, SKILLBIND);
                        p.sendMessage(ChatColor.YELLOW + "Flash is no longer bound to " + SKILLBIND.name() + ".");
                        SKILLBIND = null;
                    }
                    return;
                }
                if (SKILL) {
                    SKILL = false;
                    p.sendMessage(ChatColor.YELLOW + "Skill is no longer active.");
                } else {
                    SKILL = true;
                    p.sendMessage(ChatColor.YELLOW + "Skill is now active.");
                }
            } else if (str.equalsIgnoreCase("ceasefire")) {
                long TIME = ULTIMATETIME;
                if (System.currentTimeMillis() < TIME) {
                    p.sendMessage(ChatColor.YELLOW + "You cannot use ceasefire again for " + ((((TIME) / 1000) - (System.currentTimeMillis() / 1000))) / 60 + " minutes");
                    p.sendMessage(ChatColor.YELLOW + "and " + ((((TIME) / 1000) - (System.currentTimeMillis() / 1000)) % 60) + " seconds.");
                    return;
                }
                if (DMiscUtil.getFavor(p) >= ULTIMATECOST) {
                    int devotion = DMiscUtil.getDevotion(p, getName());
                    int crange = (int) Math.floor(15 * Math.pow(devotion, 0.275));
                    int duration = (int) Math.ceil(10 * Math.pow(devotion, 0.194));
                    int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) * ((double) DMiscUtil.getAscensions(p) / DMiscUtil.ASCENSIONCAP)));
                    ULTIMATETIME = System.currentTimeMillis() + (t * 1000);
                    p.sendMessage("In exchange for " + ChatColor.AQUA + ULTIMATECOST + ChatColor.WHITE + " Favor, ");
                    for (Player pl : p.getWorld().getPlayers()) {
                        if (pl.getLocation().distance(p.getLocation()) <= crange) {
                            if (DMiscUtil.isFullParticipant(pl)) {
                                pl.sendMessage(ChatColor.GOLD + "Heimdallr" + ChatColor.WHITE + " has mandated a ceasefire for " + duration + " seconds.");
                                DMiscUtil.addActiveEffect(pl.getUniqueId(), "Ceasefire", duration);
                            }
                        }
                    }
                    DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) - ULTIMATECOST);
                } else p.sendMessage(ChatColor.YELLOW + "Ceasefire requires " + ULTIMATECOST + " Favor.");
            }
        }
    }

    @Override
    public void onSyncTick(long timeSent) {

    }

    @Override
    public boolean canTribute() {
        return true;
    }
}
