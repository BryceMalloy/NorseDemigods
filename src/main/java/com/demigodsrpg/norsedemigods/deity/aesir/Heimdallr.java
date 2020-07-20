package com.demigodsrpg.norsedemigods.deity.aesir;

import com.demigodsrpg.norsedemigods.DMisc;
import com.demigodsrpg.norsedemigods.Deity;
import com.demigodsrpg.norsedemigods.Setting;
import com.demigodsrpg.norsedemigods.deity.AD;
import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;
import java.util.Set;

public class Heimdallr implements Deity {
    private static final int SKILLCOST = 100;
    private static final int SKILLDELAY = 3600; // milliseconds
    private static final int ULTIMATECOST = 4000;
    private static final int ULTIMATECOOLDOWNMAX = 500; // seconds
    private static final int ULTIMATECOOLDOWNMIN = 300;

    @Override
    public String getName() {
        return "Heimdallr";
    }

    @Override
    public String getDefaultAlliance() {
        return "AEsir";
    }

    @Override
    public void printInfo(Player p) {
        if (DMisc.isFullParticipant(p) && DMisc.hasDeity(p, getName())) {
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            int devotion = DMisc.getDevotion(p, getName());
            // flash range
            int range = (int) Math.ceil(3 * Math.pow(devotion, 0.2));
            // ceasefire range
            int crange = (int) Math.floor(15 * Math.pow(devotion, 0.275));
            // ceasefire duration
            int duration = (int) Math.ceil(10 * Math.pow(devotion, 0.194));
            int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) * ((double) DMisc.getAscensions(p) / Setting.ASCENSION_CAP)));
            // print
            p.sendMessage("--" + ChatColor.GOLD + getName() + ChatColor.GRAY + "[" + devotion + "]");
            p.sendMessage(":Use " + ChatColor.YELLOW + "qd <name>" + ChatColor.WHITE + " for detailed information about any player");
            p.sendMessage("you are looking at.");
            p.sendMessage(":Left-click to teleport with range " + range + "." + ChatColor.GREEN + " /flash " + ChatColor.YELLOW + "Costs " + SKILLCOST + " Favor.");
            if (save.getBind("flash").isPresent())
                p.sendMessage(ChatColor.AQUA + "    Bound to " + save.getBind("flash").get().name());
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
            if (!DMisc.isFullParticipant(p) || !DMisc.hasDeity(p, getName())) return;
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            if (save.getAbilityData("flash", AD.ACTIVE, false) || ((p.getItemInHand() != null) &&
                    save.getBind("flash").isPresent() && (p.getItemInHand().getType() == save.getBind("flash").get()))) {
                if (save.getAbilityData("flash", AD.TIME, (double) System.currentTimeMillis()) > System.currentTimeMillis())
                    return;
                save.setAbilityData("flash", AD.TIME, System.currentTimeMillis() + SKILLDELAY);
                if (DMisc.getFavor(p) >= SKILLCOST) {
                    float pitch = p.getLocation().getPitch();
                    float yaw = p.getLocation().getYaw();
                    int range = (int) Math.ceil(3 * Math.pow(DMisc.getDevotion(p, getName()), 0.2));
                    List<Block> los = p.getLineOfSight((Set) null, 100);
                    Location go;
                    if (los.size() - 1 < range) go = los.get(los.size() - 1).getLocation();
                    else go = los.get(range).getLocation();
                    if (go == null) return;
                    go.setY(go.getBlockY() + 1);
                    go.setPitch(pitch);
                    go.setYaw(yaw);
                    if ((go.getBlock().isLiquid() || go.getBlock().isEmpty()) && DMisc.canLocationPVP(p, go)) {
                        save.setTempData("temp_flash", true);
                        DMisc.horseTeleport(p, go);
                        DMisc.setFavor(p, DMisc.getFavor(p) - SKILLCOST);
                    }
                } else {
                    p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor.");
                    save.setAbilityData("flash", AD.ACTIVE, false);
                }
            }
        }
    }

    @Override
    public void onCommand(final Player p, String str, String[] args, boolean bind) {
        if (DMisc.hasDeity(p, getName())) {
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            if (str.equalsIgnoreCase("flash")) {
                if (bind) {
                    if (!save.getBind("flash").isPresent()) {
                        if (DMisc.isBound(p, p.getItemInHand().getType()))
                            p.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                        if (p.getItemInHand().getType() == Material.AIR)
                            p.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                        else {
                            save.setBind("flash", p.getItemInHand().getType());
                            p.sendMessage(ChatColor.YELLOW + "Flash is now bound to " + p.getItemInHand().getType().name() + ".");
                        }
                    } else {
                        p.sendMessage(ChatColor.YELLOW + "Flash is no longer bound to " + save.getBind("flash").get().name() + ".");
                        save.removeBind("flash");
                    }
                    return;
                }
                if (save.getAbilityData("flash", AD.ACTIVE, false)) {
                    save.setAbilityData("flash", AD.ACTIVE, false);
                    p.sendMessage(ChatColor.YELLOW + "Flash is no longer active.");
                } else {
                    save.setAbilityData("flash", AD.ACTIVE, true);
                    p.sendMessage(ChatColor.YELLOW + "Flash is now active.");
                }
            } else if (str.equalsIgnoreCase("ceasefire")) {
                double TIME = save.getAbilityData("ceasefire", AD.TIME, (double) System.currentTimeMillis());
                if (System.currentTimeMillis() < TIME) {
                    p.sendMessage(ChatColor.YELLOW + "You cannot use ceasefire again for " + ((((TIME) / 1000) - (System.currentTimeMillis() / 1000))) / 60 + " minutes");
                    p.sendMessage(ChatColor.YELLOW + "and " + ((((TIME) / 1000) - (System.currentTimeMillis() / 1000)) % 60) + " seconds.");
                    return;
                }
                if (DMisc.getFavor(p) >= ULTIMATECOST) {
                    int devotion = DMisc.getDevotion(p, getName());
                    int crange = (int) Math.floor(15 * Math.pow(devotion, 0.275));
                    int duration = (int) Math.ceil(10 * Math.pow(devotion, 0.194));
                    int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) * ((double) DMisc.getAscensions(p) / Setting.ASCENSION_CAP)));
                    save.setAbilityData("ceasefire", AD.TIME, System.currentTimeMillis() + (t * 1000));
                    p.sendMessage("In exchange for " + ChatColor.AQUA + ULTIMATECOST + ChatColor.WHITE + " Favor, ");
                    for (Player pl : p.getWorld().getPlayers()) {
                        if (pl.getLocation().distance(p.getLocation()) <= crange) {
                            if (DMisc.isFullParticipant(pl)) {
                                pl.sendMessage(ChatColor.GOLD + "Heimdallr" + ChatColor.WHITE + " has mandated a ceasefire for " + duration + " seconds.");
                                DMisc.addActiveEffect(pl.getUniqueId(), "Ceasefire", duration);
                            }
                        }
                    }
                    DMisc.setFavor(p, DMisc.getFavor(p) - ULTIMATECOST);
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
