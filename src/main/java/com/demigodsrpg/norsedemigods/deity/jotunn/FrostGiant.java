package com.demigodsrpg.norsedemigods.deity.jotunn;

import com.demigodsrpg.norsedemigods.DMisc;
import com.demigodsrpg.norsedemigods.Deity;
import com.demigodsrpg.norsedemigods.Setting;
import com.demigodsrpg.norsedemigods.deity.AD;
import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.Optional;
import java.util.UUID;

public class FrostGiant implements Deity {
    private static final String skillname = "Ice";
    private static final int SKILLCOST = 225;
    private static final int ULTIMATECOST = 2000;
    private static final int ULTIMATECOOLDOWNMAX = 700; // seconds
    private static final int ULTIMATECOOLDOWNMIN = 400;

    @Override
    public String getName() {
        return "Frost Giant";
    }

    @Override
    public String getDefaultAlliance() {
        return "Jotunn";
    }

    @Override
    public void printInfo(Player p) {
        if (DMisc.isFullParticipant(p) && DMisc.hasDeity(p, getName())) {
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            // heal amount
            int healamt = (int) Math.ceil(0.1 * Math.pow(10000, 0.297));
            // heal interval
            int healinterval = 10 - (int) (Math.round(Math.pow(10000, 0.125))); // seconds
            if (healinterval < 1) healinterval = 1;
            // squid radius
            float radius = 1 + (int) Math.round(Math.pow(10000, 0.1142));
            // ult
            int duration = (int) Math.round(40 * Math.pow(10000, 0.15)); // seconds
            int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) * ((double) DMisc.getAscensions(p) / Setting.ASCENSION_CAP)));
            /*
             * Print text
             */
            p.sendMessage("--" + ChatColor.GOLD + getName());
            p.sendMessage(":While in the snow, heal " + healamt + " HP every " + healinterval + " seconds.");
            p.sendMessage(":Left-click to throw an ice bomb that explodes with " + radius + " radius." + ChatColor.GREEN + " /ice " + ChatColor.YELLOW + "Costs " + SKILLCOST + " Favor.");
            if (save.getBind("ice").isPresent())
                p.sendMessage(ChatColor.AQUA + "    Bound to " + save.getBind("ice").get().name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to bind this skill to an item.");
            p.sendMessage("Your divine frost causes a snowstorm lasting " + duration + " seconds, transforming the nearby land into tundra." + ChatColor.GREEN + " /chill");
            p.sendMessage(ChatColor.YELLOW + "Costs " + ULTIMATECOST + " Favor. Cooldown time: " + t + " seconds.");
            return;
        }
        p.sendMessage("--" + ChatColor.GOLD + getName());
        p.sendMessage("Passive: Increased healing while in snow.");
        p.sendMessage("Active: Launch an ice bomb at the target location. " + ChatColor.GREEN + "/ice");
        p.sendMessage(ChatColor.YELLOW + "Costs " + SKILLCOST + " Favor. Can bind.");
        p.sendMessage("Ultimate: Cause a snowstorm in the current world, transforming nearby land into tundra." + ChatColor.GREEN + "/chill");
        p.sendMessage(ChatColor.YELLOW + "Select item: bottle of water");
    }

    @Override
    public void onEvent(Event ee) {
        if (ee instanceof PlayerInteractEvent) {
            PlayerInteractEvent e = (PlayerInteractEvent) ee;
            Player p = e.getPlayer();
            if (!DMisc.isFullParticipant(p) || !DMisc.hasDeity(p, getName())) return;
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            if (save.getAbilityData("ice", AD.ACTIVE, false) || ((p.getItemInHand() != null) &&
                    save.getBind("ice").isPresent() && (p.getItemInHand().getType() == save.getBind("ice").get()))) {
                if (save.getAbilityData("ice", AD.TIME, (double) System.currentTimeMillis()) > System.currentTimeMillis())
                    return;
                save.setAbilityData("ice", AD.TIME, System.currentTimeMillis() + 700L);
                if (DMisc.getFavor(p) >= SKILLCOST) {
                    if (iceSpawn(p)) DMisc.setFavor(p, DMisc.getFavor(p) - SKILLCOST);
                } else {
                    p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor.");
                    save.setAbilityData("ice", AD.ACTIVE, false);
                }
            }
        }
    }

    @Override
    public void onCommand(Player P, String str, String[] args, boolean bind) {
        if (DMisc.hasDeity(P, getName())) {
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(P);
            if (str.equalsIgnoreCase(skillname)) {
                if (bind) {
                    if (!save.getBind("ice").isPresent()) {
                        if (DMisc.isBound(P, P.getItemInHand().getType()))
                            P.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                        if (P.getItemInHand().getType() == Material.AIR)
                            P.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                        else {
                            save.setBind("ice", P.getItemInHand().getType());
                            P.sendMessage(ChatColor.YELLOW + "" + skillname + " is now bound to " + P.getItemInHand().getType().name() + ".");
                        }
                    } else {
                        P.sendMessage(ChatColor.YELLOW + "" + skillname + " is no longer bound to " + save.getBind("ice").get().name() + ".");
                        save.removeBind("ice");
                    }
                    return;
                }
                if (save.getAbilityData("ice", AD.ACTIVE, false)) {
                    save.setAbilityData("ice", AD.ACTIVE, false);
                    P.sendMessage(ChatColor.YELLOW + "" + skillname + " is no longer active.");
                } else {
                    save.setAbilityData("ice", AD.ACTIVE, true);
                    P.sendMessage(ChatColor.YELLOW + "" + skillname + " is now active.");
                }
            } else if (str.equalsIgnoreCase("chill")) {
                double TIME = save.getAbilityData("chill", AD.TIME, (double) System.currentTimeMillis());
                if (System.currentTimeMillis() < TIME) {
                    P.sendMessage(ChatColor.YELLOW + "You cannot use chill again for " + ((((TIME) / 1000) - (System.currentTimeMillis() / 1000))) / 60 + " minutes");
                    P.sendMessage(ChatColor.YELLOW + "and " + ((((TIME) / 1000) - (System.currentTimeMillis() / 1000)) % 60) + " seconds.");
                    return;
                }
                if (DMisc.getFavor(P) >= ULTIMATECOST) {
                    int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) * ((double) DMisc.getAscensions(P) / Setting.ASCENSION_CAP)));
                    save.setAbilityData("chill", AD.TIME, System.currentTimeMillis() + (t * 1000));

                    for (int x = P.getLocation().getBlockX() - 14; x <= P.getLocation().getBlockX() + 14; x++)
                        for (int y = P.getLocation().getBlockY() - 14; y <= P.getLocation().getBlockY() + 14; y++)
                            for (int z = P.getLocation().getBlockZ() - 14; z <= P.getLocation().getBlockZ() + 14; z++)
                                P.getWorld().setBiome(x, y, z, Biome.TAIGA);

                    P.getWorld().refreshChunk(P.getLocation().getBlockX(), P.getLocation().getBlockZ());

                    P.getWorld().setStorm(true);
                    P.getWorld().setThundering(true);
                    P.getWorld().setWeatherDuration((int) Math.round(40 * Math.pow(10000, 0.15)) * 20);
                    P.sendMessage(ChatColor.GOLD + "Your divine frost" + ChatColor.WHITE + " has started a snowstorm on your world,");
                    P.sendMessage("in exchange for " + ChatColor.AQUA + ULTIMATECOST + ChatColor.WHITE + " Favor.");
                    DMisc.setFavor(P, DMisc.getFavor(P) - ULTIMATECOST);
                } else P.sendMessage(ChatColor.YELLOW + "Chill requires " + ULTIMATECOST + " Favor.");
            }
        }
    }

    @Override
    public void onSyncTick(long timeSent) {
        int healinterval = 10 - (int) (Math.round(Math.pow(10000, 0.125))); // seconds
        if (healinterval < 1) healinterval = 1;
        for (UUID id : getPlayerIds()) {
            Optional<PlayerDataSaveable> opSave = getBackend().getPlayerDataRegistry().fromKey(id.toString());
            if (opSave.isPresent()) {
                PlayerDataSaveable save = opSave.get();
                if (timeSent > save.getAbilityData("frost_giant_heal", AD.TIME, (double) timeSent) + (healinterval * 1000)) {
                    save.setAbilityData("frost_giant_heal", AD.TIME, timeSent);
                    if ((Bukkit.getPlayer(id) != null) && Bukkit.getPlayer(id).getWorld().hasStorm()) {
                        Player p = Bukkit.getPlayer(id);
                        int x = p.getLocation().getChunk().getX(), z = p.getLocation().getChunk().getZ();
                        if (p.getWorld().getBiome(x, z).name().contains("TAIGA") || p.getWorld().getBiome(x, z).name().contains("COLD")) {
                            double healamt = Math.ceil(0.1 * Math.pow(10000, 0.297));
                            if (p.getHealth() + healamt > p.getMaxHealth())
                                healamt = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue() - p.getHealth();
                            DMisc.setHP(p, p.getHealth() + healamt);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean canTribute() {
        return false;
    }

    private boolean iceSpawn(Player p) {
        if (!DMisc.canTarget(p, p.getLocation())) {
            p.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
            return false;
        }
        Location target = DMisc.getTargetLocation(p);
        if (!DMisc.canLocationPVP(p, target)) return false;
        target.add(.5, .5, .5);
        Vector victor = p.getEyeLocation().toVector().add(p.getEyeLocation().getDirection().multiply(2));
        Vector path = target.toVector().subtract(p.getEyeLocation().toVector());
        FallingBlock ice = p.getWorld().spawnFallingBlock(victor.toLocation(target.getWorld()), Material.PACKED_ICE, (byte) 0);
        ice.setVelocity(path);
        ice.setMetadata("splode", new FixedMetadataValue(getBackend(), p.getUniqueId().toString()));
        ice.setDropItem(false);
        return true;
    }
}
