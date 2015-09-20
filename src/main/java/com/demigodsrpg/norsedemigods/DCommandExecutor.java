package com.demigodsrpg.norsedemigods;

import com.demigodsrpg.norsedemigods.deity.Deity;
import com.demigodsrpg.norsedemigods.deity.aesir.*;
import com.demigodsrpg.norsedemigods.deity.jotunn.*;
import com.demigodsrpg.norsedemigods.deity.testing.Kvasir;
import com.demigodsrpg.norsedemigods.listener.DLevels;
import com.demigodsrpg.norsedemigods.listener.DShrines;
import com.demigodsrpg.norsedemigods.util.DMiscUtil;
import com.demigodsrpg.norsedemigods.util.DSave;
import com.demigodsrpg.norsedemigods.util.DSettings;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

public class DCommandExecutor implements CommandExecutor {
    private final NorseDemigods plugin;
    public static final double ADVANTAGEPERCENT = 1.3;
    private static final double TRANSFERTAX = 0.9;
    private final boolean BALANCETEAMS = DSettings.getSettingBoolean("balance_teams");

    public DCommandExecutor(NorseDemigods d) {
        plugin = d;
    }

    /*
     * definePlayer : Defines the player from (CommandSender)sender.
	 */
    private static Player definePlayer(CommandSender sender) {
        // Define player
        Player player = null;
        if (sender instanceof Player) player = (Player) sender;

        return player;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command c, String label, String[] args) {
        // Define variables
        Player p = definePlayer(sender);

        if (p == null) {
            // Console commands
            if (c.getName().equalsIgnoreCase("setfavor")) return setFavor(args);
            else if (c.getName().equalsIgnoreCase("getfavor")) return getFavor(args);
            else if (c.getName().equalsIgnoreCase("addfavor")) return addFavor(args);
            else if (c.getName().equalsIgnoreCase("setmaxfavor")) return setMaxFavor(args);
            else if (c.getName().equalsIgnoreCase("getmaxfavor")) return getMaxFavor(args);
            else if (c.getName().equalsIgnoreCase("addmaxfavor")) return addMaxFavor(args);
            else if (c.getName().equalsIgnoreCase("getascensions")) return getAscensions(args);
            else if (c.getName().equalsIgnoreCase("setascensions")) return setAscensions(args);
            else if (c.getName().equalsIgnoreCase("addascensions")) return addAscensions(args);
            else if (c.getName().equalsIgnoreCase("addhp")) return addHP(args);
            else if (c.getName().equalsIgnoreCase("getdevotion")) return getDevotion(args);
            else if (c.getName().equalsIgnoreCase("setdevotion")) return setDevotion(args);
            else if (c.getName().equalsIgnoreCase("adddevotion")) return addDevotion(args);
            else if (c.getName().equalsIgnoreCase("addunclaimeddevotion")) return addUnclaimedDevotion(args);
            else if (c.getName().equalsIgnoreCase("debugplayer")) return debugPlayer(args);
            else if (c.getName().equalsIgnoreCase("exportdata")) return debugEveryPlayer();

            return false;
        } else {
            if (!DSettings.getEnabledWorlds().contains(p.getWorld())) {
                p.sendMessage(ChatColor.YELLOW + "Demigods is not enabled in your world.");
                return true;
            }

            // Non-deity-specific Player commands
            if (c.getName().equalsIgnoreCase("dg")) return infoDG(p, args);
            else if (c.getName().equalsIgnoreCase("check")) return checkCode(p);
                // else if (c.getName().equalsIgnoreCase("transfer")) return transfer(p,args);
            else if (c.getName().equalsIgnoreCase("alliance")) return alliance(p);
            else if (c.getName().equalsIgnoreCase("checkplayer")) return checkPlayer(p, args);
            else if (c.getName().equalsIgnoreCase("shrine")) return shrine(p);
            else if (c.getName().equalsIgnoreCase("shrinewarp")) return shrineWarp(p, args);
            else if (c.getName().equalsIgnoreCase("forceshrinewarp")) return forceShrineWarp(p, args);
            else if (c.getName().equalsIgnoreCase("shrineowner")) return shrineOwner(p, args);
            else if (c.getName().equalsIgnoreCase("fixshrine")) return fixShrine(p);
            else if (c.getName().equalsIgnoreCase("listshrines")) return listShrines(p);
            else if (c.getName().equalsIgnoreCase("removeshrine")) return removeShrine(p, args);
            else if (c.getName().equalsIgnoreCase("nameshrine")) return nameShrine(p, args);
            else if (c.getName().equalsIgnoreCase("givedeity")) return giveDeity(p, args);
            else if (c.getName().equalsIgnoreCase("removedeity")) return removeDeity(p, args);
            else if (c.getName().equalsIgnoreCase("adddevotion")) return addDevotion(p, args);
            else if (c.getName().equalsIgnoreCase("forsake")) return forsake(p, args);
            else if (c.getName().equalsIgnoreCase("setfavor")) return setFavor(p, args);
            else if (c.getName().equalsIgnoreCase("setmaxfavor")) return setMaxFavor(p, args);
            else if (c.getName().equalsIgnoreCase("sethp")) return setHP(p, args);
            else if (c.getName().equalsIgnoreCase("setmaxhp")) return setMaxHP(p, args);
            else if (c.getName().equalsIgnoreCase("setdevotion")) return setDevotion(p, args);
            else if (c.getName().equalsIgnoreCase("setascensions")) return setAscensions(p, args);
            else if (c.getName().equalsIgnoreCase("setkills")) return setKills(p, args);
            else if (c.getName().equalsIgnoreCase("setdeaths")) return setDeaths(p, args);
            else if (c.getName().equalsIgnoreCase("setallegiance") || c.getName().equalsIgnoreCase("setalliance"))
                return setAlliance(p, args);
            else if (c.getName().equalsIgnoreCase("removeplayer")) return removePlayer(p, args);
            else if (c.getName().equalsIgnoreCase("claim")) return claim(p);
            else if (c.getName().equalsIgnoreCase("value")) return value(p);
            else if (c.getName().equalsIgnoreCase("bindings")) return bindings(p);
            else if (c.getName().equalsIgnoreCase("assemble")) return assemble(p);

                // else if (c.getName().equalsIgnoreCase("setlore")) return setLore(p);

            else if (c.isRegistered()) {
                if (!DSettings.getEnabledWorlds().contains(p.getWorld())) {
                    p.sendMessage(ChatColor.YELLOW + "Demigods is not enabled in this world.");
                    return true;
                }
                boolean bind = false;
                if (args.length == 1) if (args[0].contains("bind")) bind = true;
                if (DMiscUtil.getDeities(p) != null) for (Deity d : DMiscUtil.getDeities(p))
                    d.onCommand(p, c.getName(), args, bind);
            }

            return false;
        }
    }

	/*
     * Every command gets it's own method below.
	 */

    private boolean setFavor(String[] args) {
        if (args.length != 2) return false;
        try {
            UUID pl = DMiscUtil.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            DMiscUtil.setFavor(pl, amt);
            Logger.getLogger("Minecraft").info("[Demigods] Set " + pl + "'s Favor to " + amt + ".");
            return true;
        } catch (Exception error) {
            Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
            return false;
        }
    }

    private boolean getFavor(String[] args) {
        if (args.length != 1) return false;
        try {
            UUID pl = DMiscUtil.getDemigodsPlayerId(args[0]);
            Logger.getLogger("Minecraft").info(DMiscUtil.getFavor(pl) + "");
            return true;
        } catch (Exception error) {
            Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
            return false;
        }
    }

    private boolean addFavor(String[] args) {
        if (args.length != 2) return false;
        try {
            UUID pl = DMiscUtil.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            DMiscUtil.setFavor(pl, amt + DMiscUtil.getFavor(pl));
            Logger.getLogger("Minecraft").info("[Demigods] Increased " + pl + "'s Favor by " + amt + " to " + DMiscUtil.getFavor(pl) + ".");
            return true;
        } catch (Exception error) {
            Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
            return false;
        }
    }

    private boolean setMaxFavor(String[] args) {
        if (args.length != 2) return false;
        try {
            UUID pl = DMiscUtil.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            DMiscUtil.setFavorCap(pl, amt);
            Logger.getLogger("Minecraft").info("[Demigods] Set " + pl + "'s max Favor to " + amt + ".");
            return true;
        } catch (Exception error) {
            Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
            return false;
        }
    }

    private boolean getMaxFavor(String[] args) {
        if (args.length != 1) return false;
        try {
            UUID pl = DMiscUtil.getDemigodsPlayerId(args[0]);
            Logger.getLogger("Minecraft").info(DMiscUtil.getFavorCap(pl) + "");
            return true;
        } catch (Exception error) {
            Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
            return false;
        }
    }

    private boolean addMaxFavor(String[] args) {
        if (args.length != 2) return false;
        try {
            UUID pl = DMiscUtil.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            DMiscUtil.setFavorCap(pl, amt + DMiscUtil.getFavor(pl));
            Logger.getLogger("Minecraft").info("[Demigods] Increased " + pl + "'s max Favor by " + amt + " to " + DMiscUtil.getFavor(pl) + ".");
            return true;
        } catch (Exception error) {
            Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
            return false;
        }
    }

    private boolean getAscensions(String[] args) {
        if (args.length != 1) return false;
        try {
            UUID pl = DMiscUtil.getDemigodsPlayerId(args[0]);
            Logger.getLogger("Minecraft").info(DMiscUtil.getAscensions(pl) + "");
            return true;
        } catch (Exception error) {
            Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
            return false;
        }
    }

    private boolean setAscensions(String[] args) {
        if (args.length != 2) return false;
        try {
            UUID target = DMiscUtil.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            DMiscUtil.setAscensions(target, amt);
            long oldtotal = DMiscUtil.getDevotion(target);
            int newtotal = DMiscUtil.costForNextAscension(amt - 1);
            for (Deity d : DMiscUtil.getDeities(target)) {
                int devotion = DMiscUtil.getDevotion(target, d);
                DMiscUtil.setDevotion(target, d, (int) Math.ceil((newtotal * 1.0 * devotion) / oldtotal));
            }
            Logger.getLogger("Minecraft").info("[Demigods] Set " + target + "'s ascensions to " + amt + ".");
            return true;
        } catch (Exception error) {
            Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
            return false;
        }
    }

    private boolean addAscensions(String[] args) {
        if (args.length != 2) return false;
        try {
            UUID target = DMiscUtil.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            DMiscUtil.setAscensions(target, DMiscUtil.getAscensions(target) + amt);
            Logger.getLogger("Minecraft").info("[Demigods] Increased " + target + "'s ascensions by " + amt + " to " + DMiscUtil.getAscensions(target) + ".");
            return true;
        } catch (Exception error) {
            Logger.getLogger("Minecraft").severe(error.getMessage());
            Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
            return false;
        }
    }

    private boolean addHP(String[] args) {
        if (args.length != 2) return false;
        try {
            UUID pl = DMiscUtil.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            DMiscUtil.setHP(pl, amt + DMiscUtil.getHP(pl));
            Logger.getLogger("Minecraft").info("[Demigods] Increased " + pl + "'s hp by " + amt + " to " + DMiscUtil.getHP(pl) + ".");
            return true;
        } catch (Exception error) {
            Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
            return false;
        }
    }

    private boolean getDevotion(String[] args) {
        if (args.length == 1) {
            try {
                UUID pl = DMiscUtil.getDemigodsPlayerId(args[0]);
                Logger.getLogger("Minecraft").info(DMiscUtil.getDevotion(pl) + "");
                return true;
            } catch (Exception error) {
                Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
                return false;
            }
        } else if (args.length == 2) {
            try {
                UUID pl = DMiscUtil.getDemigodsPlayerId(args[0]);
                Deity deity = DMiscUtil.getDeity(pl, args[1]);
                Logger.getLogger("Minecraft").info(DMiscUtil.getDevotion(pl, deity) + "");
                return true;
            } catch (Exception error) {
                Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
                return false;
            }
        } else return false;
    }

    private boolean setDevotion(String[] args) {
        if (args.length != 3) return false;
        try {
            UUID pl = DMiscUtil.getDemigodsPlayerId(args[0]);
            Deity deity = DMiscUtil.getDeity(pl, args[1]);
            int amt = Integer.parseInt(args[2]);
            DMiscUtil.setDevotion(pl, deity, amt);
            Logger.getLogger("Minecraft").info("[Demigods] Set " + pl + "'s devotion for " + deity.getName() + " to " + amt + ".");
            return true;
        } catch (Exception error) {
            Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
            return false;
        }
    }

    private boolean addDevotion(String[] args) {
        if (args.length != 3) return false;
        try {
            UUID pl = DMiscUtil.getDemigodsPlayerId(args[0]);
            Deity deity = DMiscUtil.getDeity(pl, args[1]);
            int amt = Integer.parseInt(args[2]);
            int before = DMiscUtil.getDevotion(pl, deity);
            DMiscUtil.setDevotion(pl, deity, before + amt);
            Logger.getLogger("Minecraft").info("[Demigods] Increased " + pl + "'s devotion for " + deity.getName() + " by " + amt + " to " + DMiscUtil.getDevotion(pl, deity) + ".");
            return true;
        } catch (Exception error) {
            Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
            return false;
        }
    }

    private boolean addUnclaimedDevotion(String[] args) {
        if (args.length != 2) return false;
        try {
            UUID pl = DMiscUtil.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            int before = DMiscUtil.getUnclaimedDevotion(pl);
            DMiscUtil.setUnclaimedDevotion(pl, before + amt);
            Logger.getLogger("Minecraft").info("[Demigods] Increased " + pl + "'s unclaimed devotion by " + amt + " to " + DMiscUtil.getUnclaimedDevotion(pl) + ".");
            return true;
        } catch (Exception error) {
            Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
            return false;
        }
    }

    private boolean debugPlayer(String[] args) {
        if (args.length == 1) {
            UUID player = DMiscUtil.getDemigodsPlayerId(args[0]);
            if (player == null) {
                Logger.getLogger("Minecraft").info("[Demigods] Player not found.");
                return true;
            }
            DDebug.printData(Logger.getLogger("Minecraft"), player);
            return true;
        } else if (args.length == 2) {
            if (args[1].equalsIgnoreCase("print")) {
                UUID player = DMiscUtil.getDemigodsPlayerId(args[0]);
                if (player == null) {
                    Logger.getLogger("Minecraft").info("[Demigods] Player not found.");
                    return true;
                }
                DDebug.printData(Logger.getLogger("Minecraft"), player);
                return true;
            } else if (args[1].equalsIgnoreCase("write")) {
                UUID player = DMiscUtil.getDemigodsPlayerId(args[0]);
                if (player == null) {
                    Logger.getLogger("Minecraft").info("[Demigods] Player not found.");
                    return true;
                }
                try {
                    DDebug.writeData(player);
                    return true;
                } catch (IOException e) {
                    Logger.getLogger("Minecraft").warning("[Demigods] Error writing debug for " + player + ".");
                    e.printStackTrace();
                    Logger.getLogger("Minecraft").warning("[Demigods] End stack trace for debug.");
                }
            }
        }
        return false;
    }

    private boolean debugEveryPlayer() {
        int count = 0;
        DMiscUtil.consoleMSG("info", "Exporting Legacy data...");
        for (UUID player : DSave.getCompleteData().keySet()) {
            try {
                DDebug.writeLegacyData(player);
                count++;
            } catch (IOException e) {
                Logger.getLogger("Minecraft").warning("[Demigods] Error writing debug for " + player + ".");
                e.printStackTrace();
                Logger.getLogger("Minecraft").warning("[Demigods] End stack trace for debug.");
            }
        }
        DMiscUtil.consoleMSG("info", "Finished exporting Legacy data for " + count + " players.");
        return true;
    }

    private boolean infoDG(Player p, String[] args) {
        if ((args.length == 2) || (args.length == 3)) {
            if (args[0].equalsIgnoreCase("debug") && DMiscUtil.hasPermissionOrOP(p)) {
                UUID target = DMiscUtil.getDemigodsPlayerId(args[1]);
                if (args.length == 3) {
                    if (args[2].equalsIgnoreCase("write")) {
                        if (target == null) {
                            p.sendMessage(ChatColor.YELLOW + "Player not found.");
                            return true;
                        }
                        try {
                            p.sendMessage(ChatColor.YELLOW + "Writing debug data for " + target + "...");
                            DDebug.writeData(target);
                        } catch (IOException e) {
                            Logger.getLogger("Minecraft").warning("[Demigods] Error writing debug data for " + target + ".");
                            e.printStackTrace();
                            Logger.getLogger("Minecraft").warning("[Demigods] End stack trace.");
                            p.sendMessage(ChatColor.RED + "Error writing data. Check the log for a stack trace.");
                        }
                        p.sendMessage(ChatColor.YELLOW + "Debug data for " + target + " should have been written to file.");
                    }
                } else {
                    if (target == null) {
                        p.sendMessage(ChatColor.YELLOW + "Player not found.");
                        return true;
                    }
                    DDebug.printData(p, target);
                }
            }
        }
        if (args.length == 0) {
            p.sendMessage(ChatColor.YELLOW + "[Demigods] Information Directory");
            p.sendMessage(ChatColor.GRAY + "/dg aesir");
            p.sendMessage(ChatColor.GRAY + "/dg jotunn");
            p.sendMessage(ChatColor.GRAY + "/dg claim");
            p.sendMessage(ChatColor.GRAY + "/dg shrine");
            p.sendMessage(ChatColor.GRAY + "/dg tribute");
            p.sendMessage(ChatColor.GRAY + "/dg player");
            p.sendMessage(ChatColor.GRAY + "/dg pvp");
            p.sendMessage(ChatColor.GRAY + "/dg stats");
            p.sendMessage(ChatColor.GRAY + "/dg rankings");
            p.sendMessage("To see your own information, use " + ChatColor.YELLOW + "/check");
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("check")) checkCode(p);
            else if (args[0].equalsIgnoreCase("aesir") || args[0].equalsIgnoreCase("god")) {
                p.sendMessage(ChatColor.YELLOW + "[Demigods] AEsir Help File");
                p.sendMessage(ChatColor.GRAY + "For more information on the AEsir, use /dg <name>");
                p.sendMessage(ChatColor.GOLD + "----Tier 1");
                p.sendMessage(ChatColor.GRAY + "Odin - God of death, wisdom, and time.");
                p.sendMessage(ChatColor.GRAY + "Thor - God of battle, thunder, and strength.");
                p.sendMessage(ChatColor.GRAY + "Baldr - God of light and purity.");
                p.sendMessage(ChatColor.GOLD + "----Tier 2");
                p.sendMessage(ChatColor.GRAY + "Vidar - God of vengeance.");
                p.sendMessage(ChatColor.GRAY + "Heimdallr - God of wit.");
                p.sendMessage(ChatColor.GRAY + "Bragi - God of poetry.");
                p.sendMessage(ChatColor.GRAY + "Dwarf** - A crafty race of wise blacksmiths.");
                p.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + " Marked ** are races, not deities. No shrines or tributes.");
            } else if (args[0].equalsIgnoreCase("jotunn")) {
                p.sendMessage(ChatColor.YELLOW + "[Demigods] Jotunn Help File");
                p.sendMessage(ChatColor.GRAY + "For more information on the Jotunn, use /dg <name>");
                p.sendMessage(ChatColor.GOLD + "----Tier 1");
                p.sendMessage(ChatColor.GRAY + "Hel - Ruler of the unwanted dead.");
                p.sendMessage(ChatColor.GRAY + "Jormungand - The World Serpent.");
                p.sendMessage(ChatColor.GRAY + "Fire Giant** - A mighty race of deified fire.");
                p.sendMessage(ChatColor.GOLD + "----Tier 2");
                p.sendMessage(ChatColor.GRAY + "Thrymr - Late king of the Jotunn.");
                p.sendMessage(ChatColor.GRAY + "Jord - Mother of plants and animals.");
                p.sendMessage(ChatColor.GRAY + "Frost Giant** - A mighty race of deified frost.");
                p.sendMessage(ChatColor.GRAY + "Dís** - A fearsome race of protective spirits.");
                p.sendMessage(ChatColor.GRAY.toString() + ChatColor.ITALIC + " Marked ** are races, not deities. No shrines or tributes.");
            } else if (args[0].equalsIgnoreCase("claim")) {
                p.sendMessage(ChatColor.YELLOW + "[Demigods] Claim Help File");
                p.sendMessage(ChatColor.GRAY + "To claim your first deity, use " + ChatColor.YELLOW + "/claim" + ChatColor.GRAY + " with");
                p.sendMessage(ChatColor.GRAY + "a 'select item' in your hand. The 'select item' varies for each");
                p.sendMessage(ChatColor.GRAY + "deity and can be found at /dg <deity name>.");
            } else if (args[0].equalsIgnoreCase("shrine")) {
                p.sendMessage(ChatColor.YELLOW + "[Demigods] Shrine Help File");
                p.sendMessage(ChatColor.GRAY + "You may have one shrine per deity you are allied to.");
                p.sendMessage(ChatColor.GRAY + "Shrines serve two purposes: tributing and warps.");
                p.sendMessage(ChatColor.GRAY + "Read /dg tribute for more information about tributes.");
                p.sendMessage(ChatColor.GRAY + "Warp to a specific deity's shrine using /shrinewarp <deity>.");
                p.sendMessage(ChatColor.GRAY + "You may also give a shrine a specific name.");
                p.sendMessage(ChatColor.GRAY + "To create a shrine, place a sign with the following text:");
                p.sendMessage(ChatColor.GRAY + "        shrine        ");
                p.sendMessage(ChatColor.GRAY + "       dedicate       ");
                p.sendMessage(ChatColor.GRAY + "     <deity name>     ");
                p.sendMessage(ChatColor.GRAY + "<optional shrine name>");
                p.sendMessage(ChatColor.GRAY + "Then right click the sign to \"activate\" it.");
                p.sendMessage(ChatColor.GRAY + "The following commands are used when standing near a shrine:");
                p.sendMessage(ChatColor.YELLOW + "/shrinewarp" + ChatColor.GRAY + " - warp to a shrine with the given name");
                p.sendMessage(ChatColor.YELLOW + "/shrineowner add|remove|set" + ChatColor.GRAY + " - commands to allow/unallow");
                p.sendMessage(ChatColor.GRAY + "other players to warp to a shrine that you created");
                p.sendMessage(ChatColor.YELLOW + "/removeshrine" + ChatColor.GRAY + " - removes a shrine you created, costs Devotion");
                p.sendMessage(ChatColor.YELLOW + "/nameshrine" + ChatColor.GRAY + " - rename a shrine you created");
                p.sendMessage(ChatColor.GRAY + "For information about your shrines, use " + ChatColor.YELLOW + "/shrine");
            } else if (args[0].equalsIgnoreCase("tribute")) {
                p.sendMessage(ChatColor.YELLOW + "[Demigods] Tribute Help File");
                p.sendMessage(ChatColor.GRAY + "Tributing is the only way to raise your Favor cap, which");
                p.sendMessage(ChatColor.GRAY + "allows you to stockpile Favor for skills. Tributing may occur");
                p.sendMessage(ChatColor.GRAY + "at any shrine that belongs to a deity you are allied with.");
                p.sendMessage(ChatColor.GRAY + "To tribute, simply right click the gold block that marks the");
                p.sendMessage(ChatColor.GRAY + "shrine's center and place the items you wish to tribute in the");
                p.sendMessage(ChatColor.GRAY + "\"Tributes\" inventory.");
                p.sendMessage(ChatColor.GRAY + "A bonus of Devotion is given to the owner of a shrine when any");
                p.sendMessage(ChatColor.GRAY + "player makes a tribute there, so for best results tribute at");
                p.sendMessage(ChatColor.GRAY + "your own shrines.");
            } else if (args[0].equalsIgnoreCase("player")) {
                p.sendMessage(ChatColor.YELLOW + "[Demigods] Player Help File");
                p.sendMessage(ChatColor.GRAY + "As a player, you may choose to ally with the AEsir or");
                p.sendMessage(ChatColor.GRAY + "the Jotunn. Once you have made an allegiance, you may");
                p.sendMessage(ChatColor.GRAY + "not break it without forsaking all the deities you have.");
                p.sendMessage(ChatColor.GRAY + "The three major attributes you have are:");
                p.sendMessage(ChatColor.YELLOW + "Favor " + ChatColor.GRAY + "- A measure of power and a divine currency");
                p.sendMessage(ChatColor.GRAY + "that can be spent by using skills or upgrading perks.");
                p.sendMessage(ChatColor.GRAY + "Favor regenerates whenever you are logged on.");
                p.sendMessage(ChatColor.YELLOW + "Devotion " + ChatColor.GRAY + "- A measure of how much power a deity gives you.");
                p.sendMessage(ChatColor.GRAY + "Stronger Devotion to a deity grants you increased power when.");
                p.sendMessage(ChatColor.GRAY + "using their skills.");
                p.sendMessage(ChatColor.GRAY + "Gained by dealing damage, exploring, and harvesting blocks.");
                p.sendMessage(ChatColor.YELLOW + "Ascensions " + ChatColor.GRAY + "- ");
                p.sendMessage(ChatColor.GRAY + "Ascensions unlock deities. More in progress.");
            } else if (args[0].equalsIgnoreCase("pvp")) {
                p.sendMessage(ChatColor.YELLOW + "[Demigods] PvP Help File");
                p.sendMessage(ChatColor.GRAY + "Demigods is a player versus player centric plugin and");
                p.sendMessage(ChatColor.GRAY + "rewards players greatly for defeating members of the enemy");
                p.sendMessage(ChatColor.GRAY + "alliance. Killing an enemy player rewards you with Favor and");
                p.sendMessage(ChatColor.GRAY + "EXP. If you die in combat, your Level is instantly reduced");
                p.sendMessage(ChatColor.GRAY + "to 1, although Perks can nullify this.");
                p.sendMessage(ChatColor.GRAY + "The alliance with more overall kills receives a passive EXP");
                p.sendMessage(ChatColor.GRAY + "and Favor multiplier.");
            } else if (args[0].equalsIgnoreCase("stats")) {
                int titancount = 0;
                int godcount = 0;
                int othercount = 0;
                int titankills = 0;
                int godkills = 0;
                int otherkills = 0;
                int titandeaths = 0;
                int goddeaths = 0;
                int otherdeaths = 0;
                ArrayList<UUID> onlinegods = new ArrayList<UUID>();
                ArrayList<UUID> onlinetitans = new ArrayList<UUID>();
                ArrayList<UUID> onlineother = new ArrayList<UUID>();
                for (UUID id : DSave.getCompleteData().keySet()) {
                    try {
                        if (!DMiscUtil.isFullParticipant(id)) continue;
                        if (DSave.hasData(id, "LASTLOGINTIME"))
                            if ((Long) DSave.getData(id, "LASTLOGINTIME") < System.currentTimeMillis() - 604800000)
                                continue;
                        if (DMiscUtil.getAllegiance(id).equalsIgnoreCase("Jotunn")) {
                            titancount++;
                            titankills += DMiscUtil.getKills(id);
                            titandeaths += DMiscUtil.getDeaths(id);
                            if (DMiscUtil.getPlugin().getServer().getPlayer(id).isOnline()) {
                                onlinetitans.add(id);
                            }
                        } else if (DMiscUtil.getAllegiance(id).equalsIgnoreCase("AEsir")) {
                            godcount++;
                            godkills += DMiscUtil.getKills(id);
                            goddeaths += DMiscUtil.getDeaths(id);
                            if (DMiscUtil.getPlugin().getServer().getPlayer(id).isOnline()) {
                                onlinegods.add(id);
                            }
                        } else {
                            if (!DMiscUtil.isFullParticipant(id)) continue;
                            othercount++;
                            otherkills += DMiscUtil.getKills(id);
                            otherdeaths += DMiscUtil.getDeaths(id);
                            if (DMiscUtil.getPlugin().getServer().getPlayer(id).isOnline()) {
                                onlineother.add(id);
                            }
                        }
                    } catch (NullPointerException ignored) {
                    }
                }
                /*
                 * Print data
				 */
                p.sendMessage(ChatColor.GRAY + "----Stats----");
                String str1 = "";
                if (onlinegods.size() > 0) {
                    for (UUID g : onlinegods) {
                        str1 += DMiscUtil.getLastKnownName(g) + ", ";
                    }
                    str1 = str1.substring(0, str1.length() - 2);
                }
                String str2 = "";
                if (onlinetitans.size() > 0) {
                    for (UUID t : onlinetitans) {
                        str2 += DMiscUtil.getLastKnownName(t) + ", ";
                    }
                    str2 = str2.substring(0, str2.length() - 2);
                }
                String str3 = "";
                if (onlineother.size() > 0) {
                    for (UUID o : onlineother) {
                        str3 += DMiscUtil.getLastKnownName(o) + ", ";
                    }
                    str3 = str3.substring(0, str3.length() - 2);
                }
                p.sendMessage("There are " + ChatColor.GREEN + onlinegods.size() + "/" + ChatColor.YELLOW + godcount + ChatColor.WHITE + " AEsir online: " + ChatColor.GOLD + str1);
                p.sendMessage("There are " + ChatColor.GREEN + onlinetitans.size() + "/" + ChatColor.YELLOW + titancount + ChatColor.WHITE + " Jotunn online: " + ChatColor.GOLD + str2);
                if (othercount > 0)
                    p.sendMessage("There are " + ChatColor.GREEN + onlineother.size() + "/" + ChatColor.YELLOW + othercount + ChatColor.WHITE + " others online: " + ChatColor.GOLD + str3);
                p.sendMessage("Total AEsir kills: " + ChatColor.GREEN + godkills + ChatColor.YELLOW + " --- " + ChatColor.WHITE + " AEsir K/D Ratio: " + ChatColor.YELLOW + ((float) godkills / goddeaths));
                p.sendMessage("Total Jotunn kills: " + ChatColor.GREEN + titankills + ChatColor.YELLOW + " --- " + ChatColor.WHITE + " Jotunn K/D Ratio: " + ChatColor.YELLOW + ((float) titankills / titandeaths));
                if (othercount > 0) {
                    p.sendMessage("Total Other kills: " + ChatColor.GREEN + otherkills + ChatColor.YELLOW + " --- " + ChatColor.WHITE + " Other K/D Ratio: " + ChatColor.YELLOW + ((float) otherkills / otherdeaths));
                }
            } else if (args[0].equalsIgnoreCase("ranking") || args[0].equalsIgnoreCase("rankings")) {
                if (DMiscUtil.getFullParticipants().size() < 1) {
                    p.sendMessage(ChatColor.GRAY + "There are no players to rank.");
                    return true;
                }
                // get list of gods and titans
                ArrayList<UUID> gods = new ArrayList<UUID>();
                ArrayList<UUID> titans = new ArrayList<UUID>();
                ArrayList<Long> gr = new ArrayList<Long>();
                ArrayList<Long> tr = new ArrayList<Long>();
                for (UUID s : DMiscUtil.getFullParticipants()) {
                    if (DMiscUtil.getAllegiance(s).equalsIgnoreCase("AEsir")) {
                        if (DSave.hasData(s, "LASTLOGINTIME"))
                            if ((Long) DSave.getData(s, "LASTLOGINTIME") < System.currentTimeMillis() - 604800000)
                                continue;
                        gods.add(s);
                        gr.add(DMiscUtil.getRanking(s));
                    } else if (DMiscUtil.getAllegiance(s).equalsIgnoreCase("Jotunn")) {
                        if (DSave.hasData(s, "LASTLOGINTIME"))
                            if ((Long) DSave.getData(s, "LASTLOGINTIME") < System.currentTimeMillis() - 604800000)
                                continue;
                        titans.add(s);
                        tr.add(DMiscUtil.getRanking(s));
                    }
                }
                String[] Gods = new String[gods.size()];
                String[] Titans = new String[titans.size()];
                Long[] GR = new Long[gods.size()];
                Long[] TR = new Long[titans.size()];
                for (int i = 0; i < Gods.length; i++) {
                    Gods[i] = DMiscUtil.getLastKnownName(gods.get(i));
                    GR[i] = gr.get(i);
                }
                for (int i = 0; i < Titans.length; i++) {
                    Titans[i] = DMiscUtil.getLastKnownName(titans.get(i));
                    TR[i] = tr.get(i);
                }
                // sort gods
                for (int i = 0; i < Gods.length; i++) {
                    int highestIndex = i;
                    long highestRank = GR[i];
                    for (int j = i; j < Gods.length; j++) {
                        if (GR[j] > highestRank) {
                            highestIndex = j;
                            highestRank = GR[j];
                        }
                    }
                    if (highestRank == GR[i]) continue;
                    String t = Gods[i];
                    Gods[i] = Gods[highestIndex];
                    Gods[highestIndex] = t;
                    Long l = GR[i];
                    GR[i] = GR[highestIndex];
                    GR[highestIndex] = l;
                }
                // sort titans
                for (int i = 0; i < Titans.length; i++) {
                    int highestIndex = i;
                    long highestRank = TR[i];
                    for (int j = i; j < Titans.length; j++) {
                        if (TR[j] > highestRank) {
                            highestIndex = j;
                            highestRank = TR[j];
                        }
                    }
                    if (highestRank == TR[i]) continue;
                    String t = Titans[i];
                    Titans[i] = Titans[highestIndex];
                    Titans[highestIndex] = t;
                    Long l = TR[i];
                    TR[i] = TR[highestIndex];
                    TR[highestIndex] = l;
                }
                // print
                p.sendMessage(ChatColor.GRAY + "----Rankings----");
                p.sendMessage(ChatColor.GRAY + "Rankings are determined by Devotion, Deities, and Kills.");
                int gp = Gods.length;
                if (gp > 5) gp = 5;
                p.sendMessage(ChatColor.GOLD + "-- AEsir");
                for (int i = 0; i < gp; i++) {
                    if (DMiscUtil.getOnlinePlayer(Gods[i]) != null)
                        p.sendMessage(ChatColor.GREEN + "  " + (i + 1) + ". " + Gods[i] + " :: " + GR[i]);
                    else p.sendMessage(ChatColor.GRAY + "  " + (i + 1) + ". " + Gods[i] + " :: " + GR[i]);
                }
                int tp = Titans.length;
                if (tp > 5) tp = 5;
                p.sendMessage(ChatColor.DARK_RED + "-- Jotunn");
                for (int i = 0; i < tp; i++) {
                    if (DMiscUtil.getOnlinePlayer(Titans[i]) != null)
                        p.sendMessage(ChatColor.GREEN + "  " + (i + 1) + ". " + Titans[i] + " :: " + TR[i]);
                    else p.sendMessage(ChatColor.GRAY + "  " + (i + 1) + ". " + Titans[i] + " :: " + TR[i]);
                }
            } else {
                for (Deity deity : DSave.getGlobalList()) {
                    if (deity.getName().equalsIgnoreCase(args[0])) deity.printInfo(p);
                }
            }
        } else if (args.length == 2) {
            for (Deity deity : DSave.getGlobalList()) {
                if (deity.getName().equalsIgnoreCase(args[0] + " " + args[1])) deity.printInfo(p);
            }
            if (args[0].equalsIgnoreCase("ranking") || args[0].equalsIgnoreCase("rankings")) {
                if (args[1].equalsIgnoreCase("aesir") || args[1].equalsIgnoreCase("god")) {
                    // get list of gods
                    ArrayList<UUID> gods = new ArrayList<UUID>();
                    ArrayList<Long> gr = new ArrayList<Long>();
                    for (UUID s : DMiscUtil.getFullParticipants()) {
                        if (DMiscUtil.getAllegiance(s).equalsIgnoreCase("AEsir")) {
                            if (DSave.hasData(s, "LASTLOGINTIME"))
                                if ((Long) DSave.getData(s, "LASTLOGINTIME") < System.currentTimeMillis() - 604800000)
                                    continue;
                            gods.add(s);
                            gr.add(DMiscUtil.getRanking(s));
                        }
                    }
                    if (gods.size() < 1) {
                        p.sendMessage(ChatColor.GRAY + "There are no players to rank.");
                        return true;
                    }
                    String[] Gods = new String[gods.size()];
                    Long[] GR = new Long[gods.size()];
                    for (int i = 0; i < Gods.length; i++) {
                        Gods[i] = DMiscUtil.getLastKnownName(gods.get(i));
                        GR[i] = gr.get(i);
                    }
                    // sort gods
                    for (int i = 0; i < Gods.length; i++) {
                        int highestIndex = i;
                        long highestRank = GR[i];
                        for (int j = i; j < Gods.length; j++) {
                            if (GR[j] > highestRank) {
                                highestIndex = j;
                                highestRank = GR[j];
                            }
                        }
                        if (highestRank == GR[i]) continue;
                        String t = Gods[i];
                        Gods[i] = Gods[highestIndex];
                        Gods[highestIndex] = t;
                        Long l = GR[i];
                        GR[i] = GR[highestIndex];
                        GR[highestIndex] = l;
                    }
                    p.sendMessage(ChatColor.GRAY + "----AEsir Rankings----");
                    p.sendMessage(ChatColor.GRAY + "Rankings are determined by Devotion, Deities, and Kills.");
                    for (int i = 0; i < Gods.length; i++) {
                        if (DMiscUtil.getOnlinePlayer(Gods[i]) != null)
                            p.sendMessage(ChatColor.GREEN + "  " + (i + 1) + ". " + Gods[i] + " :: " + GR[i]);
                        else p.sendMessage(ChatColor.GRAY + "  " + (i + 1) + ". " + Gods[i] + " :: " + GR[i]);
                    }
                } else if (args[1].equalsIgnoreCase("jotunn")) {
                    // get list of titans
                    ArrayList<UUID> titans = new ArrayList<UUID>();
                    ArrayList<Long> tr = new ArrayList<Long>();
                    for (UUID s : DMiscUtil.getFullParticipants()) {
                        if (DMiscUtil.getAllegiance(s).equalsIgnoreCase("Jotunn")) {
                            if (DSave.hasData(s, "LASTLOGINTIME"))
                                if ((Long) DSave.getData(s, "LASTLOGINTIME") < System.currentTimeMillis() - 604800000)
                                    continue;
                            titans.add(s);
                            tr.add(DMiscUtil.getRanking(s));
                        }
                    }
                    if (titans.size() < 1) {
                        p.sendMessage(ChatColor.GRAY + "There are no players to rank.");
                        return true;
                    }
                    String[] Titans = new String[titans.size()];
                    Long[] TR = new Long[titans.size()];
                    for (int i = 0; i < Titans.length; i++) {
                        Titans[i] = DMiscUtil.getLastKnownName(titans.get(i));
                        TR[i] = tr.get(i);
                    }
                    // sort titans
                    for (int i = 0; i < Titans.length; i++) {
                        int highestIndex = i;
                        long highestRank = TR[i];
                        for (int j = i; j < Titans.length; j++) {
                            if (TR[j] > highestRank) {
                                highestIndex = j;
                                highestRank = TR[j];
                            }
                        }
                        if (highestRank == TR[i]) continue;
                        String t = Titans[i];
                        Titans[i] = Titans[highestIndex];
                        Titans[highestIndex] = t;
                        Long l = TR[i];
                        TR[i] = TR[highestIndex];
                        TR[highestIndex] = l;
                    }
                    // print
                    p.sendMessage(ChatColor.GRAY + "----Jotunn Rankings----");
                    p.sendMessage(ChatColor.GRAY + "Rankings are determined by Devotion, Deities, and Kills.");
                    for (int i = 0; i < Titans.length; i++) {
                        if (DMiscUtil.getOnlinePlayer(Titans[i]) != null)
                            p.sendMessage(ChatColor.GREEN + "  " + (i + 1) + ". " + Titans[i] + " :: " + TR[i]);
                        else p.sendMessage(ChatColor.GRAY + "  " + (i + 1) + ". " + Titans[i] + " :: " + TR[i]);
                    }
                }
            }
        } else if (args.length == 3) {
            if (DMiscUtil.hasPermissionOrOP(p)) try {
                int one = Integer.parseInt(args[0]);
                int two = Integer.parseInt(args[1]);
                int three = Integer.parseInt(args[2]);
                DMiscUtil.horseTeleport(p, new Location(p.getWorld(), one, two, three));
            } catch (Exception ignored) {
            }
        }
        return true;
    }

    private boolean checkCode(Player p) {
        if (!DMiscUtil.isFullParticipant(p)) {
            p.sendMessage(ChatColor.YELLOW + "--" + p.getName() + "--Human--");
            p.sendMessage("You are not affiliated with any AEsir or Jotunn.");
            return true;
        }
        if (DMiscUtil.getUnclaimedDevotion(p) > 0) {
            p.sendMessage(ChatColor.AQUA + "You have " + DMiscUtil.getUnclaimedDevotion(p) + " unclaimed Devotion.");
            p.sendMessage(ChatColor.AQUA + "Allocate it with /adddevotion <deity> <amount>.");
        }
        p.sendMessage(ChatColor.YELLOW + "--" + p.getName() + "--" + DMiscUtil.getRank(p) + "");
        // HP
        ChatColor color = ChatColor.GREEN;
        if ((DMiscUtil.getHP(p) / DMiscUtil.getMaxHP(p)) < 0.25) color = ChatColor.RED;
        else if ((DMiscUtil.getHP(p) / DMiscUtil.getMaxHP(p)) < 0.5) color = ChatColor.YELLOW;
        p.sendMessage("HP: " + color + DMiscUtil.getHP(p) + "/" + DMiscUtil.getMaxHP(p));
        // List deities
        String send = "You are empowered by:";
        for (Deity d : DMiscUtil.getDeities(p)) {
            send += " " + d.getName() + " " + ChatColor.YELLOW + (d.canTribute() ? "<" + DMiscUtil.getDevotion(p, d) + ">" : "") + ChatColor.WHITE;
        }
        p.sendMessage(send);
        // Display Favor/Ascensions and K/D
        // float percentage = (DMiscUtil.getDevotion(p)-DMiscUtil.costForNextAscension(DMiscUtil.getAscensions(p)-1))/(float)(DMiscUtil.costForNextAscension(p)-DMiscUtil.costForNextAscension(DMiscUtil.getAscensions(p)-1))*100;
        String op = ChatColor.YELLOW + "   |   " + (DMiscUtil.costForNextAscension(DMiscUtil.getAscensions(p)) - DMiscUtil.getDevotion(p)) + " until next Ascension";
        if (DMiscUtil.getAscensions(p) >= DMiscUtil.ASCENSIONCAP) op = "";
        p.sendMessage("Devotion: " + DMiscUtil.getDevotion(p) + op);
        p.sendMessage("Favor: " + DMiscUtil.getFavor(p) + ChatColor.YELLOW + "/" + DMiscUtil.getFavorCap(p));
        p.sendMessage("Ascensions: " + DMiscUtil.getAscensions(p));
        p.sendMessage("Kills: " + ChatColor.GREEN + DMiscUtil.getKills(p) + ChatColor.WHITE + " // " + "Deaths: " + ChatColor.RED + DMiscUtil.getDeaths(p));
        // Deity information
        if (DMiscUtil.getAscensions(p) < DMiscUtil.costForNextDeity(p))
            p.sendMessage("You may form a new alliance at " + ChatColor.GOLD + DMiscUtil.costForNextDeity(p) + ChatColor.WHITE + " Ascensions.");
        else {
            p.sendMessage(ChatColor.AQUA + "You are eligible for a new alliance.");
        }
        // Effects
        if (DMiscUtil.getActiveEffects(p.getUniqueId()).size() > 0) {
            String printout = ChatColor.YELLOW + "Active effects:";
            HashMap<String, Long> fx = DMiscUtil.getActiveEffects(p.getUniqueId());
            for (Map.Entry<String, Long> str : fx.entrySet()) {
                printout += " " + str.getKey() + "[" + (Math.round(str.getValue() - System.currentTimeMillis()) / 1000) + "s]";
            }
            p.sendMessage(printout);
        }
        return true;
    }

    @SuppressWarnings("unused")
    private boolean transfer(Player p, String[] args) {
        if (!DMiscUtil.isFullParticipant(p)) return true;
        if (args.length == 1) {
            try {
                int give = Integer.parseInt(args[0]);
                if (DMiscUtil.getFavor(p) < give) {
                    p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor.");
                    return true;
                }
                for (Block b : p.getLineOfSight((Set) null, 5)) {
                    for (Player pl : p.getWorld().getPlayers()) {
                        if (pl.getLocation().distance(b.getLocation()) < 0.8) {
                            if (!DMiscUtil.isFullParticipant(pl)) continue;
                            if (!DMiscUtil.getAllegiance(pl).equalsIgnoreCase(DMiscUtil.getAllegiance(p))) continue;
                            DMiscUtil.setFavor(pl, DMiscUtil.getFavor(pl) + give);
                            DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) - give);
                            p.sendMessage(ChatColor.YELLOW + "Successfully transferred " + give + " Favor to " + pl.getName() + ".");
                            pl.sendMessage(ChatColor.YELLOW + "Received " + give + " Favor from " + p.getName() + ".");
                            return true;
                        }
                    }
                }
                p.sendMessage(ChatColor.YELLOW + "No players found. You may only transfer Favor within your alliance.");
            } catch (Exception e) {
                return false;
            }
            return true;
        } else if (args.length == 2) {
            try {
                Player pl = DMiscUtil.getOnlinePlayer(args[0]);
                if (pl.getUniqueId().equals(p.getUniqueId())) {
                    p.sendMessage(ChatColor.YELLOW + "You cannot send Favor to yourself.");
                    return true;
                }
                int give = Integer.parseInt(args[1]);
                int tax = (int) (TRANSFERTAX * give);
                if (DMiscUtil.getFavor(p) < (give + tax)) {
                    p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor.");
                    p.sendMessage(ChatColor.YELLOW + "The tax for this long-distance transfer is " + tax + ".");
                    return true;
                }
                if (!DMiscUtil.isFullParticipant(pl)) return true;
                if (!DMiscUtil.getAllegiance(pl).equalsIgnoreCase(DMiscUtil.getAllegiance(p))) return true;
                DMiscUtil.setFavor(pl, DMiscUtil.getFavor(pl) + give);
                DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) - give - tax);
                p.sendMessage(ChatColor.YELLOW + "Successfully transferred " + give + " Favor to " + pl.getName() + ".");
                if (tax > 0)
                    p.sendMessage(ChatColor.YELLOW + "You lost " + tax + " Favor in tax for a long-distance transfer.");
                pl.sendMessage(ChatColor.YELLOW + "Received " + give + " Favor from " + p.getName() + ".");
                return true;
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean alliance(Player p) {
        if (!DMiscUtil.isFullParticipant(p)) return true;
        if (DSave.hasData(p, "ALLIANCECHAT")) {
            if ((Boolean) DSave.getData(p, "ALLIANCECHAT")) {
                p.sendMessage(ChatColor.YELLOW + "Alliance chat has been turned off.");
                DSave.saveData(p, "ALLIANCECHAT", false);
                return true;
            }
        }
        p.sendMessage(ChatColor.YELLOW + "Alliance chat has been turned on.");
        DSave.saveData(p, "ALLIANCECHAT", true);
        return true;
    }

    private boolean checkPlayer(Player p, String[] args) {
        if (!(DMiscUtil.hasPermission(p, "demigods.checkplayer") || DMiscUtil.hasPermission(p, "demigods.admin")))
            return true;
        if (args.length != 1) return false;
        try {
            Player ptarget = DMiscUtil.getOnlinePlayer(args[0]);
            UUID target = DMiscUtil.getDemigodsPlayerId(args[0]);
            if (DMiscUtil.isFullParticipant(target)) {
                p.sendMessage(ChatColor.YELLOW + "--" + target + "--");
                // List deities
                String send = target + "'s deities are:";
                for (Deity d : DMiscUtil.getDeities(target)) {
                    send += " " + d.getName() + " " + ChatColor.YELLOW + "<" + DMiscUtil.getDevotion(target, d) + ">" + ChatColor.WHITE;
                }
                p.sendMessage(send);
                // HP
                ChatColor color = ChatColor.GREEN;
                if ((DMiscUtil.getHP(target) / DMiscUtil.getMaxHP(target)) < 0.25) color = ChatColor.RED;
                else if ((DMiscUtil.getHP(target) / DMiscUtil.getMaxHP(target)) < 0.5) color = ChatColor.YELLOW;
                p.sendMessage("HP: " + color + DMiscUtil.getHP(target) + "/" + DMiscUtil.getMaxHP(target));
                // Display Favor/Ascensions and K/D
                p.sendMessage("Devotion: " + DMiscUtil.getDevotion(target) + ChatColor.YELLOW + "   |   " + (DMiscUtil.costForNextAscension(DMiscUtil.getAscensions(target)) - DMiscUtil.getDevotion(target)) + " until next Ascension");
                p.sendMessage("Favor: " + DMiscUtil.getFavor(target) + ChatColor.YELLOW + "/" + DMiscUtil.getFavorCap(target));
                p.sendMessage("Ascensions: " + DMiscUtil.getAscensions(target));
                p.sendMessage("Kills: " + ChatColor.GREEN + DMiscUtil.getKills(target) + ChatColor.WHITE + " // " + "Deaths: " + ChatColor.RED + DMiscUtil.getDeaths(target));
                // Deity information
                if (DMiscUtil.costForNextDeity(target) > DMiscUtil.getAscensions(target))
                    p.sendMessage(target + " may form a new alliance at " + ChatColor.GOLD + DMiscUtil.costForNextDeity(target) + ChatColor.WHITE + " Ascensions.");
                else {
                    p.sendMessage(ChatColor.AQUA + DMiscUtil.getLastKnownName(target) + " is eligible for a new alliance.");
                }
                // Effects
                if (DMiscUtil.getActiveEffectsList(target).size() > 0) {
                    String printout = ChatColor.YELLOW + "Active effects:";
                    for (String str : DMiscUtil.getActiveEffectsList(target))
                        printout += " " + str;
                    p.sendMessage(printout);
                }
            } else {
                p.sendMessage(ChatColor.YELLOW + "--" + ptarget.getName() + "--Human--");
                p.sendMessage(ptarget.getName() + " is not affiliated with any AEsir or Jotunn.");
            }
        } catch (NullPointerException name) {
            p.sendMessage(ChatColor.YELLOW + "Player not found.");
        }
        return true;
    }

    private boolean shrine(Player p) {
        if (!DMiscUtil.isFullParticipant(p)) return true;
        // player has shrines for these deities
        String str1 = "Shrines:";
        for (String name : DMiscUtil.getShrines(p.getUniqueId()).keySet()) {
            if (name.charAt(0) != '#')
                str1 += " " + DMiscUtil.getDeityAtShrine(DMiscUtil.getShrines(p.getUniqueId()).get(name));
        }
        // player's named shrines
        String str2 = "Named shrines:";
        for (String name : DMiscUtil.getShrines(p.getUniqueId()).keySet()) {
            if (name.charAt(0) == '#') str2 += " " + name.substring(1);
        }
        // player can warp to these shrines
        String str3 = "Other shrines you may warp to:";
        for (WriteLocation shrine : DMiscUtil.getAccessibleShrines(p.getUniqueId())) {
            str3 += " " + DMiscUtil.getShrineName(shrine);
        }
        p.sendMessage(ChatColor.YELLOW + str1);
        p.sendMessage(ChatColor.YELLOW + str2);
        p.sendMessage(ChatColor.LIGHT_PURPLE + str3);
        return true;
    }

    private boolean shrineWarp(Player p, String[] args) {
        if (!(DMiscUtil.hasPermission(p, "demigods.shrinewarp") || DMiscUtil.hasPermission(p, "demigods.admin")))
            return true;
        WriteLocation target = null;
        if ((args.length != 1) && (args.length != 2)) return false;
        if (args.length == 1)
            // try matching the name to deities the player has
            target = DMiscUtil.getShrine(p.getUniqueId(), args[0]);
        // try matching the name to another player's warp
        if ((target == null) && (args.length == 2)) {
            if (DMiscUtil.isFullParticipant(DMiscUtil.getDemigodsPlayerId(args[0]))) {
                target = DMiscUtil.getShrine(DMiscUtil.getDemigodsPlayerId(args[0]), args[1]);
            }
        }
        if ((target == null) && (args.length == 1)) target = DMiscUtil.getShrineByKey("#" + args[0]);
        if (target == null) {
            p.sendMessage(ChatColor.YELLOW + "Target shrine not found. Shrine names are case sensitive.");
            return true;
        }
        // check for permission
        if (!DMiscUtil.isGuest(target, p.getUniqueId()) && !DMiscUtil.getOwnerOfShrine(target).equals(p.getUniqueId())) {
            p.sendMessage(ChatColor.YELLOW + "You do not have permission for that warp.");
            return true;
        }
        // check if warp is valid
        if (!DSettings.getEnabledWorlds().contains(p.getWorld())) {
            return true;
        }
        if (!DSettings.getEnabledWorlds().contains(DMiscUtil.toLocation(target).getWorld())) {
            p.sendMessage(ChatColor.YELLOW + "Demigods is not enabled in the target world.");
            return true;
        }
        // check if warp is clear
        Block b = DMiscUtil.toLocation(target).getBlock();
        if ((b.getRelative(BlockFace.UP).getType() != Material.AIR) || (b.getRelative(BlockFace.UP).getRelative(BlockFace.UP).getType() != Material.AIR)) {
            p.sendMessage(ChatColor.YELLOW + "The target location is blocked.");
            return true;
        }
        // warp code
        target = DMiscUtil.toWriteLocation(b.getRelative(BlockFace.UP).getLocation());
        final WriteLocation current = DMiscUtil.toWriteLocation(p.getLocation());
        final double hp = DMiscUtil.getHP(p);
        final float pitch = p.getLocation().getPitch();
        final float yaw = p.getLocation().getYaw();
        final Player pt = p;
        final WriteLocation TARGET = target;
        DMiscUtil.addActiveEffect(p.getUniqueId(), "Warping", 1000);
        p.sendMessage(ChatColor.YELLOW + "Don't move, warping in progress...");
        DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (DMiscUtil.getActiveEffectsList(pt.getUniqueId()).contains("Warping")) {
                    if (!current.equalsApprox(DMiscUtil.toWriteLocation(pt.getLocation()))) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to movement.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                    if (DMiscUtil.getHP(pt) < hp) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to loss of health.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                }
            }
        }, 20);
        DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (DMiscUtil.getActiveEffectsList(pt.getUniqueId()).contains("Warping")) {
                    if (!current.equalsApprox(DMiscUtil.toWriteLocation(pt.getLocation()))) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to movement.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                    if (DMiscUtil.getHP(pt) < hp) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to loss of health.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                }
            }
        }, 40);
        DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (DMiscUtil.getActiveEffectsList(pt.getUniqueId()).contains("Warping")) {
                    if (!current.equalsApprox(DMiscUtil.toWriteLocation(pt.getLocation()))) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to movement.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                    if (DMiscUtil.getHP(pt) < hp) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to loss of health.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                }
            }
        }, 60);
        DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (DMiscUtil.getActiveEffectsList(pt.getUniqueId()).contains("Warping")) {
                    if (!current.equalsApprox(DMiscUtil.toWriteLocation(pt.getLocation()))) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to movement.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                    if (DMiscUtil.getHP(pt) < hp) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to loss of health.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                }
            }
        }, 80);
        DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (DMiscUtil.getActiveEffectsList(pt.getUniqueId()).contains("Warping")) {
                    Location newloc = DMiscUtil.toLocation(TARGET);
                    newloc.setPitch(pitch);
                    newloc.setYaw(yaw);
                    newloc.setX(newloc.getX() + 0.5);
                    newloc.setZ(newloc.getZ() + 0.5);
                    DMiscUtil.horseTeleport(pt, newloc);
                    pt.sendMessage(ChatColor.YELLOW + "Shrine warp successful.");
                    DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                }
            }
        }, 90);
        return true;
    }

    private boolean forceShrineWarp(Player p, String[] args) {
        if (!(DMiscUtil.hasPermission(p, "demigods.shrinewarp") || DMiscUtil.hasPermission(p, "demigods.admin")))
            return true;
        WriteLocation target = null;
        if ((args.length != 1) && (args.length != 2)) return false;
        if (args.length == 1)
            // try matching the name to deities the player has
            target = DMiscUtil.getShrine(p.getUniqueId(), args[0]);
        // try matching the name to another player's warp
        if ((target == null) && (args.length == 2)) {
            if (DMiscUtil.isFullParticipant(DMiscUtil.getDemigodsPlayerId(args[0]))) {
                target = DMiscUtil.getShrine(DMiscUtil.getDemigodsPlayerId(args[0]), args[1]);
            }
        }
        if ((target == null) && (args.length == 1)) target = DMiscUtil.getShrineByKey("#" + args[0]);
        if (target == null) {
            p.sendMessage(ChatColor.YELLOW + "Target shrine not found. Shrine names are case sensitive.");
            return true;
        }
        // check for permission
        if (!DMiscUtil.isGuest(target, p.getUniqueId()) && !DMiscUtil.getOwnerOfShrine(target).equals(p.getUniqueId())) {
            p.sendMessage(ChatColor.YELLOW + "You do not have permission for that warp.");
            return true;
        }
        // check if warp is valid
        if (!DSettings.getEnabledWorlds().contains(p.getWorld())) {
            return true;
        }
        if (!DSettings.getEnabledWorlds().contains(DMiscUtil.toLocation(target).getWorld())) {
            p.sendMessage(ChatColor.YELLOW + "Demigods is not enabled in the target world.");
            return true;
        }
        // check if warp is clear
        Block b = DMiscUtil.toLocation(target).getBlock();
        if ((b.getRelative(BlockFace.UP).getType() != Material.AIR) || (b.getRelative(BlockFace.UP).getRelative(BlockFace.UP).getType() != Material.AIR)) {
            p.sendMessage(ChatColor.YELLOW + "The target location is blocked, warping anyways.");
        }
        // warp code
        target = DMiscUtil.toWriteLocation(b.getRelative(BlockFace.UP).getLocation());
        final WriteLocation current = DMiscUtil.toWriteLocation(p.getLocation());
        final double hp = DMiscUtil.getHP(p);
        final float pitch = p.getLocation().getPitch();
        final float yaw = p.getLocation().getYaw();
        final Player pt = p;
        final WriteLocation TARGET = target;
        DMiscUtil.addActiveEffect(p.getUniqueId(), "Warping", 1000);
        p.sendMessage(ChatColor.YELLOW + "Don't move, warping in progress...");
        DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (DMiscUtil.getActiveEffectsList(pt.getUniqueId()).contains("Warping")) {
                    if (!current.equalsApprox(DMiscUtil.toWriteLocation(pt.getLocation()))) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to movement.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                    if (DMiscUtil.getHP(pt) < hp) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to loss of health.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                }
            }
        }, 20);
        DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (DMiscUtil.getActiveEffectsList(pt.getUniqueId()).contains("Warping")) {
                    if (!current.equalsApprox(DMiscUtil.toWriteLocation(pt.getLocation()))) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to movement.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                    if (DMiscUtil.getHP(pt) < hp) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to loss of health.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                }
            }
        }, 40);
        DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (DMiscUtil.getActiveEffectsList(pt.getUniqueId()).contains("Warping")) {
                    if (!current.equalsApprox(DMiscUtil.toWriteLocation(pt.getLocation()))) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to movement.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                    if (DMiscUtil.getHP(pt) < hp) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to loss of health.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                }
            }
        }, 60);
        DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (DMiscUtil.getActiveEffectsList(pt.getUniqueId()).contains("Warping")) {
                    if (!current.equalsApprox(DMiscUtil.toWriteLocation(pt.getLocation()))) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to movement.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                    if (DMiscUtil.getHP(pt) < hp) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to loss of health.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                }
            }
        }, 80);
        DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (DMiscUtil.getActiveEffectsList(pt.getUniqueId()).contains("Warping")) {
                    if (!current.equalsApprox(DMiscUtil.toWriteLocation(pt.getLocation()))) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to movement.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                    if (DMiscUtil.getHP(pt) < hp) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to loss of health.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                }
            }
        }, 100);
        DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (DMiscUtil.getActiveEffectsList(pt.getUniqueId()).contains("Warping")) {
                    if (!current.equalsApprox(DMiscUtil.toWriteLocation(pt.getLocation()))) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to movement.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                    if (DMiscUtil.getHP(pt) < hp) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to loss of health.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                }
            }
        }, 120);
        DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (DMiscUtil.getActiveEffectsList(pt.getUniqueId()).contains("Warping")) {
                    if (!current.equalsApprox(DMiscUtil.toWriteLocation(pt.getLocation()))) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to movement.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                    if (DMiscUtil.getHP(pt) < hp) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to loss of health.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                }
            }
        }, 140);
        DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (DMiscUtil.getActiveEffectsList(pt.getUniqueId()).contains("Warping")) {
                    if (!current.equalsApprox(DMiscUtil.toWriteLocation(pt.getLocation()))) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to movement.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                    if (DMiscUtil.getHP(pt) < hp) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to loss of health.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                }
            }
        }, 160);
        DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (DMiscUtil.getActiveEffectsList(pt.getUniqueId()).contains("Warping")) {
                    if (!current.equalsApprox(DMiscUtil.toWriteLocation(pt.getLocation()))) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to movement.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                    if (DMiscUtil.getHP(pt) < hp) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to loss of health.");
                        DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                }
            }
        }, 180);
        DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if (DMiscUtil.getActiveEffectsList(pt.getUniqueId()).contains("Warping")) {
                    Location newloc = DMiscUtil.toLocation(TARGET);
                    newloc.setPitch(pitch);
                    newloc.setYaw(yaw);
                    newloc.setX(newloc.getX() + 0.5);
                    newloc.setZ(newloc.getZ() + 0.5);
                    DMiscUtil.horseTeleport(pt, newloc);
                    pt.sendMessage(ChatColor.YELLOW + "Shrine warp successful.");
                    DMiscUtil.removeActiveEffect(pt.getUniqueId(), "Warping");
                }
            }
        }, 190);
        return true;
    }

    private boolean shrineOwner(Player p, String[] args) {
        if (!(DMiscUtil.hasPermission(p, "demigods.shrineowner") || DMiscUtil.hasPermission(p, "demigods.admin")))
            return true;
        if (args.length != 2) return false;
        WriteLocation shrine = DMiscUtil.getNearbyShrine(p.getLocation());
        if (shrine == null) {
            p.sendMessage(ChatColor.YELLOW + "No shrine nearby.");
            return true;
        }
        if (!DMiscUtil.getOwnerOfShrine(shrine).equals(p.getUniqueId()) && !DMiscUtil.hasPermission(p, "demigods.admin")) {
            p.sendMessage(ChatColor.YELLOW + "Only admins and the creator of a shrine can modify it.");
            return true;
        }
        // add <name>
        if (args[0].equalsIgnoreCase("add")) {
            UUID toadd = DMiscUtil.getDemigodsPlayerId(args[1]);
            if (toadd == null) {
                p.sendMessage(ChatColor.YELLOW + "Player not found.");
            } else if (toadd.equals(p.getUniqueId())) {
                p.sendMessage(ChatColor.YELLOW + "You are already the shrine owner.");
            } else if (DMiscUtil.getShrineGuestlist(shrine).contains(toadd)) {
                p.sendMessage(ChatColor.YELLOW + DMiscUtil.getLastKnownName(toadd) + " already has permission to warp to this shrine.");
            } else if (!DMiscUtil.getAllegiance(toadd).equals(DMiscUtil.getAllegiance(p))) {
                p.sendMessage(ChatColor.YELLOW + DMiscUtil.getLastKnownName(toadd) + " is not in your alliance.");
            } else {
                p.sendMessage(ChatColor.YELLOW + DMiscUtil.getLastKnownName(toadd) + " now has permission to warp to this shrine.");
                DMiscUtil.addGuest(shrine, toadd);
            }
        }
        // remove <name>
        else if (args[0].equalsIgnoreCase("remove")) {
            UUID remove = DMiscUtil.getDemigodsPlayerId(args[1]);
            if (remove == null) {
                p.sendMessage(ChatColor.YELLOW + "Player not found.");
            } else if (remove.equals(p.getUniqueId())) {
                p.sendMessage(ChatColor.YELLOW + "You cannot remove yourself as an owner.");
            } else if (!DMiscUtil.getShrineGuestlist(shrine).contains(remove)) {
                p.sendMessage(ChatColor.YELLOW + DMiscUtil.getLastKnownName(remove) + " is not an owner of this shrine.");
            } else {
                if (DMiscUtil.removeGuest(shrine, remove))
                    p.sendMessage(ChatColor.YELLOW + DMiscUtil.getLastKnownName(remove) + " no longer has permission to warp to this shrine.");
                else p.sendMessage(ChatColor.YELLOW + "Error while removing " + remove + "'s permission.");
            }
        }
        // set <name>
        else if (args[0].equalsIgnoreCase("set")) {
            UUID newowner = DMiscUtil.getDemigodsPlayerId(args[1]);
            if (newowner == null) {
                p.sendMessage(ChatColor.YELLOW + "Player not found.");
            } else if (newowner.equals(DMiscUtil.getOwnerOfShrine(shrine))) {
                p.sendMessage(ChatColor.YELLOW + DMiscUtil.getLastKnownName(newowner) + " is already the shrine's owner.");
            } else {
                p.sendMessage(ChatColor.YELLOW + DMiscUtil.getLastKnownName(newowner) + " is the new owner of the shrine.");
                String deity = DMiscUtil.getDeityAtShrine(shrine);
                String shrinename = DMiscUtil.getShrineName(shrine);
                DMiscUtil.removeShrine(shrine);
                DMiscUtil.addShrine(newowner, deity, shrine);
                DMiscUtil.addShrine(newowner, shrinename, shrine);
            }
        } else return false;
        return true;
    }

    private boolean fixShrine(Player p) {
        WriteLocation shrine = DMiscUtil.getNearbyShrine(p.getLocation());
        if (shrine == null) {
            p.sendMessage(ChatColor.YELLOW + "No shrine nearby.");
            return true;
        }
        // check if creator/admin
        if (!DMiscUtil.getOwnerOfShrine(shrine).equals(p.getUniqueId()) && !DMiscUtil.hasPermission(p, "demigods.admin")) {
            p.sendMessage(ChatColor.YELLOW + "Only admins and the creator of a shrine can modify it.");
            return true;
        }
        if (DMiscUtil.toLocation(shrine).getBlock().getType() != Material.GOLD_BLOCK)
            DMiscUtil.toLocation(shrine).getBlock().setType(Material.GOLD_BLOCK);
        p.sendMessage(ChatColor.YELLOW + "Shrine fixed.");
        return true;
    }

    private boolean listShrines(Player p) {
        if (!(DMiscUtil.hasPermission(p, "demigods.listshrines") || DMiscUtil.hasPermission(p, "demigods.admin")))
            return true;
        String str = "";
        for (WriteLocation w : DMiscUtil.getAllShrines()) {
            String toadd = DMiscUtil.getShrineName(w);
            if (!str.contains(toadd)) str += toadd + ", ";
        }
        if (str.length() > 3) str = str.substring(0, str.length() - 2);
        if (str.length() > 0) p.sendMessage(str);
        else p.sendMessage(ChatColor.YELLOW + "No shrines found.");
        return true;
    }

    private boolean removeShrine(Player p, String[] args) {
        if (!(DMiscUtil.hasPermission(p, "demigods.removeshrine") || DMiscUtil.hasPermission(p, "demigods.admin")))
            return true;
        if ((args.length == 1) && DMiscUtil.hasPermission(p, "demigods.admin") && args[0].equals("all")) {
            for (WriteLocation w : DMiscUtil.getAllShrines()) {
                p.sendMessage("Deleting " + DMiscUtil.getShrineName(w));
                DMiscUtil.toLocation(w).getBlock().setType(Material.AIR);
                DMiscUtil.removeShrine(w);
            }
            return true;
        }
        if (args.length != 0) return false;
        // find nearby shrine
        WriteLocation shrine = DMiscUtil.getNearbyShrine(p.getLocation());
        if (shrine == null) {
            p.sendMessage(ChatColor.YELLOW + "No shrine nearby.");
            return true;
        }
        // check if creator/admin
        if (!DMiscUtil.getOwnerOfShrine(shrine).equals(p.getUniqueId()) && !DMiscUtil.hasPermission(p, "demigods.admin")) {
            p.sendMessage(ChatColor.YELLOW + "Only admins and the creator of a shrine can modify it.");
            return true;
        }
        // remove
        String deity = DMiscUtil.getDeityAtShrine(shrine);
        DMiscUtil.toLocation(shrine).getBlock().setType(Material.AIR);
        p.sendMessage(ChatColor.YELLOW + "The shrine " + DMiscUtil.getShrineName(shrine) + " has been removed.");
        DMiscUtil.removeShrine(shrine);
        if (!DMiscUtil.hasPermission(p, "demigods.admin")) {
            // penalty
            DMiscUtil.setDevotion(p, deity, (int) (DMiscUtil.getDevotion(p, deity) * 0.75));
            p.sendMessage(ChatColor.RED + "Your Devotion for " + deity + " has been reduced to " + DMiscUtil.getDevotion(p, deity) + ".");
        }
        return true;
    }

    private boolean nameShrine(Player p, String[] args) {
        if (!(DMiscUtil.hasPermission(p, "demigods.nameshrine") || DMiscUtil.hasPermission(p, "demigods.admin")))
            return true;
        if (args.length != 1) return false;
        // find nearby shrine
        WriteLocation shrine = DMiscUtil.getNearbyShrine(p.getLocation());
        if (shrine == null) {
            p.sendMessage(ChatColor.YELLOW + "No shrine nearby.");
            return true;
        }
        // check if creator/admin
        if (!DMiscUtil.getOwnerOfShrine(shrine).equals(p.getUniqueId()) && !DMiscUtil.hasPermission(p, "demigods.admin")) {
            p.sendMessage(ChatColor.YELLOW + "Only admins and the creator of a shrine can modify it.");
            return true;
        }
        // remove
        if (DMiscUtil.renameShrine(shrine, args[0]))
            p.sendMessage(ChatColor.YELLOW + "The shrine has been renamed to " + args[0] + ".");
        else p.sendMessage(ChatColor.YELLOW + "Error. Is there already a shrine named " + args[1] + "?");
        return true;
    }

    private boolean giveDeity(Player p, String[] args) {
        if (!(DMiscUtil.hasPermission(p, "demigods.givedeity") || DMiscUtil.hasPermission(p, "demigods.admin")))
            return true;
        if (args.length < 2) return false;
        UUID target = DMiscUtil.getDemigodsPlayerId(args[0]);
        if (DMiscUtil.hasDeity(target, args[1])) {
            p.sendMessage(ChatColor.YELLOW + "" + target + " already has that deity.");
            return true;
        } else {
            String s = args[1].toLowerCase();
            String targetName = DMiscUtil.getLastKnownName(target);
            String success = ChatColor.YELLOW + "Success! " + targetName + " now has the deity " + args[1] + ".";
            switch (s) {
                case "thor":
                    DMiscUtil.giveDeity(target, new Thor(target));
                    break;
                case "vidar":
                    DMiscUtil.giveDeity(target, new Vidar(target));
                    break;
                case "odin":
                    DMiscUtil.giveDeity(target, new Odin(target));
                    break;
                case "fire":
                case "firegiant":
                    DMiscUtil.giveDeity(target, new FireGiant(target));
                    success = ChatColor.YELLOW + "Success! " + targetName + " now has divine fire.";
                    break;
                case "jord":
                    DMiscUtil.giveDeity(target, new Jord(target));
                    break;
                case "hel":
                    DMiscUtil.giveDeity(target, new Hel(target));
                    break;
                case "jormungand":
                    DMiscUtil.giveDeity(target, new Jormungand(target));
                    break;
                case "thrymr":
                    DMiscUtil.giveDeity(target, new Thrymr(target));
                    break;
                case "heimdallr":
                    DMiscUtil.giveDeity(target, new Heimdallr(target));
                    break;
                case "frost":
                case "frostgiant":
                    DMiscUtil.giveDeity(target, new FrostGiant(target));
                    success = ChatColor.YELLOW + "Success! " + targetName + " now has divine frost.";
                    break;
                case "baldr":
                    DMiscUtil.giveDeity(target, new Baldr(target));
                    break;
                case "dwarf":
                    DMiscUtil.giveDeity(target, new Dwarf(target));
                    success = ChatColor.YELLOW + "Success! " + targetName + " now has dwarven powers.";
                    break;
                case "bragi":
                    DMiscUtil.giveDeity(target, new Bragi(target));
                    break;
                case "dís":
                case "dis":
                    DMiscUtil.giveDeity(target, new Dis(target));
                    success = ChatColor.YELLOW + "Success! " + targetName + " has joined the dísir.";
                    break;
                case "kvasir":
                    DMiscUtil.giveDeity(target, new Kvasir(target));
                    success = ChatColor.YELLOW + "Success! " + targetName + " is ready for a world of pain.";
                    break;
                default:
                    return false;
            }
            p.sendMessage(success);
            p.sendMessage(ChatColor.YELLOW + "Skills may not work if you mismatch Jotunn and AEsir.");
        }
        return true;
    }

    private boolean removeDeity(Player p, String[] args) {
        if (!(DMiscUtil.hasPermission(p, "demigods.removedeity") || DMiscUtil.hasPermission(p, "demigods.admin")))
            return true;
        if (args.length != 2) return false;
        UUID target = DMiscUtil.getDemigodsPlayerId(args[0]);
        if (!DMiscUtil.hasDeity(target, args[1])) {
            p.sendMessage(ChatColor.YELLOW + "" + target + " does not have that deity.");
        } else {
            DMiscUtil.getDeities(target).remove(DMiscUtil.getDeity(target, args[1]));
            p.sendMessage(ChatColor.YELLOW + "Success! " + target + " no longer has that deity.");
        }
        return true;
    }

    private boolean addDevotion(Player p, String[] args) {
        if (args.length != 2) {
            p.sendMessage("/adddevotion <deity name> <amount>");
            return true;
        }
        String deity = args[0];
        if (!DMiscUtil.hasDeity(p, deity)) {
            p.sendMessage(ChatColor.YELLOW + "You do not have a deity with the name " + deity + ".");
            return true;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[1]);
        } catch (Exception err) {
            p.sendMessage(ChatColor.YELLOW + "" + args[1] + " is not a valid number.");
            return true;
        }
        if (amount > DMiscUtil.getUnclaimedDevotion(p)) {
            p.sendMessage(ChatColor.YELLOW + "You do not enough unclaimed Devotion.");
            return true;
        } else if (amount < 1) {
            p.sendMessage(ChatColor.YELLOW + "Why would you want to do that?");
            return true;
        }
        Deity d = DMiscUtil.getDeity(p, deity);
        DMiscUtil.setUnclaimedDevotion(p, DMiscUtil.getUnclaimedDevotion(p) - amount);
        DMiscUtil.setDevotion(p, d, DMiscUtil.getDevotion(p, d) + amount);
        p.sendMessage(ChatColor.YELLOW + "Your Devotion for " + d.getName() + " has increased to " + DMiscUtil.getDevotion(p, d) + ".");
        DLevels.levelProcedure(p);
        p.sendMessage("You have " + DMiscUtil.getUnclaimedDevotion(p) + " unclaimed Devotion remaining.");
        return true;
    }

    private boolean forsake(Player p, String[] args) {
        if (!(DMiscUtil.hasPermission(p, "demigods.forsake") || DMiscUtil.hasPermission(p, "demigods.admin")))
            return true;
        if (!DMiscUtil.isFullParticipant(p)) return true;
        if (args.length == 1 || args.length == 2) {
            if (args[0].equalsIgnoreCase("all")) {
                DMiscUtil.getPlugin().getServer().broadcastMessage(ChatColor.RED + p.getName() + " has forsaken their deities.");
                p.kickPlayer(ChatColor.RED + "You are mortal.");
                for (WriteLocation w : DMiscUtil.getShrines(p.getUniqueId()).values())
                    DMiscUtil.removeShrine(w);
                DSave.removePlayer(p);
                DSave.addPlayer(p);
                return true;
            }
            String deityName = args[0];
            if (args.length == 2) {
                deityName = args[0] + " " + args[1];
            }
            if (!DMiscUtil.hasDeity(p, deityName)) {
                p.sendMessage(ChatColor.YELLOW + "You do not have that deity.");
            } else {
                if (DMiscUtil.getDeities(p).size() >= 2) {
                    String str = "";
                    Deity toremove = DMiscUtil.getDeity(p, deityName);
                    DLevels.levelProcedure(p);
                    p.sendMessage(ChatColor.YELLOW + "You have forsaken " + toremove.getName() + "." + str);
                    DMiscUtil.getPlugin().getServer().broadcastMessage(ChatColor.RED + p.getName() + " has forsaken " + toremove.getName() + ".");
                    DMiscUtil.getDeities(p).remove(toremove);
                } else {
                    Deity toremove = DMiscUtil.getDeity(p, deityName);
                    p.sendMessage(ChatColor.YELLOW + "You have forsaken " + toremove.getName() + ".");
                    p.kickPlayer(ChatColor.YELLOW + "You have forsaken " + toremove.getName() + ChatColor.WHITE + " -- " + ChatColor.RED + "You are mortal.");
                    DMiscUtil.getPlugin().getServer().broadcastMessage(ChatColor.RED + p.getName() + " has forsaken " + toremove.getName() + ".");
                    DSave.removePlayer(p);
                    DSave.addPlayer(p);
                }
            }
            return true;
        }
        return false;
    }

    private boolean setFavor(Player p, String[] args) {
        if (!(DMiscUtil.hasPermission(p, "demigods.setfavor") || DMiscUtil.hasPermission(p, "demigods.admin")))
            return true;
        if (args.length != 2) return false;
        try {
            UUID target = DMiscUtil.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            if (amt < 0) {
                p.sendMessage(ChatColor.YELLOW + "The amount must be greater than 0.");
                return true;
            }
            if (DSave.hasPlayer(target)) {
                DMiscUtil.setFavor(target, amt);
                p.sendMessage(ChatColor.YELLOW + "Success! " + target + " now has " + amt + " Favor.");
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean setMaxFavor(Player p, String[] args) {
        if (!(DMiscUtil.hasPermission(p, "demigods.setfavor") || DMiscUtil.hasPermission(p, "demigods.admin")))
            return true;
        if (args.length != 2) return false;
        try {
            UUID target = DMiscUtil.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            if (amt < 0) {
                p.sendMessage(ChatColor.YELLOW + "The amount must be greater than 0.");
                return true;
            }
            if (DSave.hasPlayer(target)) {
                DMiscUtil.setFavorCap(target, amt);
                p.sendMessage(ChatColor.YELLOW + "Success! " + target + " now has " + amt + " max Favor.");
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean setHP(Player p, String[] args) {
        if (!(DMiscUtil.hasPermission(p, "demigods.sethp") || DMiscUtil.hasPermission(p, "demigods.admin")))
            return true;
        if (args.length != 2) return false;
        try {
            UUID target = DMiscUtil.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            if (amt < 0) {
                p.sendMessage(ChatColor.YELLOW + "The amount must be greater than 0.");
                return true;
            }
            if (DSave.hasPlayer(target)) {
                if (amt > DMiscUtil.getMaxHP(target)) DMiscUtil.setMaxHP(target, amt);
                DMiscUtil.setHP(target, amt);
                p.sendMessage(ChatColor.YELLOW + "Success! " + target + " now has " + amt + " HP.");
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean setMaxHP(Player p, String[] args) {
        if (!(DMiscUtil.hasPermission(p, "demigods.setmaxhp") || DMiscUtil.hasPermission(p, "demigods.admin")))
            return true;
        if (args.length != 2) return false;
        try {
            UUID target = DMiscUtil.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            if (amt < 0) {
                p.sendMessage(ChatColor.YELLOW + "The amount must be greater than 0.");
                return true;
            }
            if (DSave.hasPlayer(target)) {
                DMiscUtil.setMaxHP(target, amt);
                p.sendMessage(ChatColor.YELLOW + "Success! " + target + " now has " + amt + " max HP.");
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean setDevotion(Player p, String[] args) {
        if (!(DMiscUtil.hasPermission(p, "demigods.setdevotion") || DMiscUtil.hasPermission(p, "demigods.admin")))
            return true;
        if (args.length != 3) return false;
        UUID target = DMiscUtil.getDemigodsPlayerId(args[0]);
        if (!DMiscUtil.isFullParticipant(target)) {
            p.sendMessage("That player is a mortal.");
            return true;
        }
        int amt = Integer.parseInt(args[2]);
        if (amt < 0) {
            p.sendMessage(ChatColor.YELLOW + "The amount must be greater than 0.");
            return true;
        }
        if (DMiscUtil.hasDeity(target, args[1])) {
            DMiscUtil.setDevotion(target, DMiscUtil.getDeity(target, args[1]), amt);
            p.sendMessage(ChatColor.YELLOW + "Success! " + target + " now has " + amt + " Devotion for " + args[1].toUpperCase() + ".");
            return true;
        }
        return false;
    }

    private boolean setAscensions(Player p, String[] args) {
        if (!(DMiscUtil.hasPermission(p, "demigods.setascensions") || DMiscUtil.hasPermission(p, "demigods.admin")))
            return true;
        if (args.length != 2) return false;
        try {
            UUID target = DMiscUtil.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            if (amt < 0) {
                p.sendMessage(ChatColor.YELLOW + "The number must be greater than 0.");
                return true;
            }
            if (DSave.hasPlayer(target)) {
                DMiscUtil.setAscensions(target, amt);
                long oldtotal = DMiscUtil.getDevotion(target);
                int newtotal = DMiscUtil.costForNextAscension(amt - 1);
                for (Deity d : DMiscUtil.getDeities(target)) {
                    int devotion = DMiscUtil.getDevotion(target, d);
                    DMiscUtil.setDevotion(target, d, (int) Math.ceil((newtotal * 1.0 * devotion) / oldtotal));
                }
                p.sendMessage(ChatColor.YELLOW + "Success! " + target + " now has " + amt + " Ascensions.");
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean setKills(Player p, String[] args) {
        if (!(DMiscUtil.hasPermission(p, "demigods.setkills") || DMiscUtil.hasPermission(p, "demigods.admin")))
            return true;
        if (args.length != 2) return false;
        try {
            UUID target = DMiscUtil.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            if (amt < 0) {
                p.sendMessage(ChatColor.YELLOW + "The amount must be greater than 0.");
                return true;
            }
            if (DSave.hasPlayer(target)) {
                DMiscUtil.setKills(target, amt);
                p.sendMessage(ChatColor.YELLOW + "Success! " + target + " now has " + amt + " kills.");
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean setDeaths(Player p, String[] args) {
        if (!(DMiscUtil.hasPermission(p, "demigods.setdeaths") || DMiscUtil.hasPermission(p, "demigods.admin")))
            return true;
        if (args.length != 2) return false;
        try {
            UUID target = DMiscUtil.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            if (amt < 0) {
                p.sendMessage(ChatColor.YELLOW + "The amount must be greater than 0.");
                return true;
            }
            if (DSave.hasPlayer(target)) {
                DMiscUtil.setDeaths(target, amt);
                p.sendMessage(ChatColor.YELLOW + "Success! " + target + " now has " + amt + " deaths.");
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean setAlliance(Player p, String[] args) {
        if (!(DMiscUtil.hasPermission(p, "demigods.setalliance") || DMiscUtil.hasPermission(p, "demigods.admin")))
            return true;
        if (args.length != 2) return false;
        try {
            UUID target = DMiscUtil.getDemigodsPlayerId(args[0]);
            String allegiance = args[1];
            if (allegiance.equalsIgnoreCase("aesir")) DMiscUtil.setAEsir(target);
            else if (allegiance.equalsIgnoreCase("jotunn")) DMiscUtil.setJotunn(target);
            else DMiscUtil.setAllegiance(target, allegiance);
            p.sendMessage(ChatColor.YELLOW + "Success! " + target + " is now in the " + DMiscUtil.getAllegiance(target) + " allegiance.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean removePlayer(Player p, String[] args) {
        if (!(DMiscUtil.hasPermission(p, "demigods.removeplayer") || DMiscUtil.hasPermission(p, "demigods.admin")))
            return true;
        if (args.length != 1) return false;
        Player toremove = plugin.getServer().getPlayer(args[0]);
        if (DSave.hasPlayer(toremove)) {
            p.sendMessage(ChatColor.YELLOW + toremove.getName() + " was successfully removed from the save.");
            DSave.removePlayer(toremove);
            toremove.kickPlayer("Save removed. Please log in again.");

        } else p.sendMessage(ChatColor.YELLOW + "That player is not in the save.");
        return true;
    }

    @SuppressWarnings("incomplete-switch")
    private boolean claim(Player p) {
        /*
         * Check for a new player first
		 */
        if ((DMiscUtil.getDeities(p) == null) || (DMiscUtil.getDeities(p).size() == 0)) {
            Deity choice = null;
            switch (p.getItemInHand().getType()) {
                case SOUL_SAND:
                    choice = new Odin(p.getUniqueId());
                    break;
                case IRON_INGOT:
                    choice = new Thor(p.getUniqueId());
                    break;
                case RED_ROSE:
                case YELLOW_FLOWER:
                    choice = new Baldr(p.getUniqueId());
                    break;
                //
                case FLINT_AND_STEEL:
                    choice = new FireGiant(p.getUniqueId());
                    break;
                case WATER_BUCKET:
                    choice = new Jormungand(p.getUniqueId());
                    break;
                case BONE:
                    choice = new Hel(p.getUniqueId());
                    break;
            }
            if (choice != null) {
                if (!DMiscUtil.hasPermission(p, choice.getDefaultAlliance().toLowerCase() + "." + choice.getName().toLowerCase()) && (!DMiscUtil.hasPermission(p, choice.getDefaultAlliance().toLowerCase() + ".all"))) {
                    p.sendMessage(ChatColor.RED + "You do not have permission to claim this deity.");
                    return true;
                }
                p.sendMessage(ChatColor.YELLOW + "The great powers ponder your decision...");
                if (BALANCETEAMS && DMiscUtil.hasAdvantage(choice.getDefaultAlliance())) {
                    p.sendMessage(ChatColor.RED + "Your selection would unbalance the order of the universe.");
                    p.sendMessage(ChatColor.RED + "Try again later or select a different deity.");
                    return true;
                }
                if (choice.getClass() == Dwarf.class) {
                    p.sendMessage(ChatColor.YELLOW + "You have been accepted to the dwarven lineage.");
                } else if (choice.getClass() == Dis.class) {
                    p.sendMessage(ChatColor.YELLOW + "You have been accepted into the lineage of the dísir.");
                } else if (choice.getClass() == FireGiant.class) {
                    p.sendMessage(ChatColor.YELLOW + "You are gifted with divine fire, joining the lineage of the fire giants.");
                } else {
                    p.sendMessage(ChatColor.YELLOW + "You have been accepted to the lineage of " + choice.getName() + ".");
                }
                DMiscUtil.initializePlayer(p.getUniqueId(), choice.getDefaultAlliance(), choice);
                p.getWorld().strikeLightningEffect(p.getLocation());
                for (int i = 0; i < 20; i++)
                    p.getWorld().spawn(p.getLocation(), ExperienceOrb.class);
                return true;
            }
            p.sendMessage(ChatColor.YELLOW + "That is not a valid selection item for your first deity.");
            return true;
        }
        /*
		 * Otherwise
		 */
        if (!DMiscUtil.isFullParticipant(p)) return true;
        if (DMiscUtil.getAscensions(p) < DMiscUtil.costForNextDeity(p)) {
            p.sendMessage(ChatColor.YELLOW + "You must have " + DMiscUtil.costForNextDeity(p) + " Ascensions to claim another deity.");
            return true;
        }
        Deity choice = null;
        switch (p.getItemInHand().getType()) {
            case IRON_INGOT:
                choice = new Thor(p.getUniqueId());
                break;
            case GOLD_SWORD:
                choice = new Vidar(p.getUniqueId());
                break;
            case SOUL_SAND:
                choice = new Odin(p.getUniqueId());
                break;
            case BOOK:
                choice = new Heimdallr(p.getUniqueId());
                break;
            case FURNACE:
                choice = new Dwarf(p.getUniqueId());
                break;
            case JUKEBOX:
                choice = new Bragi(p.getUniqueId());
                break;
            case RED_ROSE:
            case YELLOW_FLOWER:
                choice = new Baldr(p.getUniqueId());
                break;
            //
            case WATER_BUCKET:
                choice = new Jormungand(p.getUniqueId());
                break;
            case BONE:
                choice = new Hel(p.getUniqueId());
                break;
            case FLINT_AND_STEEL:
                choice = new FireGiant(p.getUniqueId());
                break;
            case VINE:
                choice = new Jord(p.getUniqueId());
                break;
            case OBSIDIAN:
                choice = new Thrymr(p.getUniqueId());
                break;
            case GLASS_BOTTLE:
                choice = new FrostGiant(p.getUniqueId());
                break;
            case COMPASS:
                choice = new Dis(p.getUniqueId());
                break;
        }
        if (choice == null) {
            p.sendMessage(ChatColor.YELLOW + "That is not a valid selection item.");
            return true;
        }
        if (!DMiscUtil.hasPermission(p, choice.getDefaultAlliance().toLowerCase() + "." + choice.getName().toLowerCase()) && (!DMiscUtil.hasPermission(p, choice.getDefaultAlliance().toLowerCase() + ".all"))) {
            p.sendMessage(ChatColor.RED + "You do not have permission to claim this deity.");
            return true;
        }
        if (!choice.getDefaultAlliance().equalsIgnoreCase(DMiscUtil.getAllegiance(p))) {
            if (DMiscUtil.hasPermission(p, "demigods.bypassclaim")) {
                p.sendMessage(ChatColor.YELLOW + choice.getName() + " has offered you power in exchange for loyalty.");
            } else {
                p.sendMessage(ChatColor.RED + "That deity is not of your alliance.");
                return true;
            }
        }
        if (DMiscUtil.hasDeity(p, choice.getName())) {
            p.sendMessage(ChatColor.RED + "You are already allied with " + choice.getName() + ".");
            return true;
        }
        DMiscUtil.giveDeity(p, choice);
        DMiscUtil.setFavorCap(p, DMiscUtil.getFavorCap(p) + 100);
        DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) + 100);
        if (choice.getClass() == Dwarf.class) {
            p.sendMessage(ChatColor.YELLOW + "You have been accepted to the dwarven lineage.");
        } else if (choice.getClass() == Dis.class) {
            p.sendMessage(ChatColor.YELLOW + "You have been accepted into the lineage of the dísir.");
        } else if (choice.getClass() == FireGiant.class) {
            p.sendMessage(ChatColor.YELLOW + "You are gifted with divine fire, joining the lineage of the fire giants.");
        } else if (choice.getClass() == FrostGiant.class) {
            p.sendMessage(ChatColor.YELLOW + "You are gifted with divine frost, joining the lineage of the frost giants.");
        } else {
            p.sendMessage(ChatColor.YELLOW + "You have been accepted to the lineage of " + choice.getName() + ".");
        }
        return true;
    }

    private boolean value(Player p) {
        if (DMiscUtil.isFullParticipant(p)) if (p.getItemInHand() != null)
            p.sendMessage(ChatColor.YELLOW + p.getItemInHand().getType().name() + " x" + p.getItemInHand().getAmount() + " is worth " + (int) (DMiscUtil.getValue(p.getItemInHand()) * DShrines.FAVORMULTIPLIER) + " at a shrine.");
        return true;
    }

    private boolean bindings(Player p) {
        if (!DMiscUtil.isFullParticipant(p)) return true;
        if (!(DMiscUtil.hasPermission(p, "demigods.bindings") || DMiscUtil.hasPermission(p, "demigods.admin")))
            return true;
        ArrayList<Material> items = DMiscUtil.getBindings(p);
        if ((items != null) && (items.size() > 0)) {
            String disp = ChatColor.YELLOW + "Bound items:";
            for (Material m : items)
                disp += " " + m.name().toLowerCase();
            p.sendMessage(disp);
        } else p.sendMessage(ChatColor.YELLOW + "You have no bindings.");
        return true;
    }

    private boolean assemble(Player p) {
        if (!DMiscUtil.isFullParticipant(p)) return true;
        if (!DMiscUtil.getActiveEffectsList(p.getUniqueId()).contains("Congregate")) return true;
        for (Player pl : p.getWorld().getPlayers()) {
            if (DMiscUtil.isFullParticipant(pl) && DMiscUtil.getActiveEffectsList(pl.getUniqueId()).contains("Congregate Call")) {
                DMiscUtil.removeActiveEffect(p.getUniqueId(), "Congregate");
                DMiscUtil.addActiveEffect(p.getUniqueId(), "Ceasefire", 60);
                DMiscUtil.horseTeleport(p, pl.getLocation());
                return true;
            }
        }
        p.sendMessage(ChatColor.YELLOW + "Unable to reach the congregation's location.");
        return true;
    }

    static class DDebug {

        /**
         * Prints data for "p" in-game to "cm".
         *
         * @param cm
         * @param p
         */
        public static void printData(Player cm, UUID p) {
            try {
                cm.sendMessage("Name: " + DMiscUtil.getLastKnownName(p));
            } catch (NullPointerException ne) {
                cm.sendMessage(ChatColor.RED + "Name is missing/null.");
            }
            try {
                cm.sendMessage("Alliance: " + DMiscUtil.getAllegiance(p));
            } catch (NullPointerException ne) {
                cm.sendMessage(ChatColor.RED + "Alliance is missing/null.");
            }
            try {
                cm.sendMessage("Current HP: " + DMiscUtil.getHP(p));
            } catch (NullPointerException ne) {
                cm.sendMessage(ChatColor.RED + "HP is missing/null.");
            }
            try {
                cm.sendMessage("Max HP: " + DMiscUtil.getMaxHP(p));
            } catch (NullPointerException ne) {
                cm.sendMessage(ChatColor.RED + "Max HP is missing/null.");
            }
            try {
                cm.sendMessage("Current Favor: " + DMiscUtil.getFavor(p));
            } catch (NullPointerException ne) {
                cm.sendMessage(ChatColor.RED + "Favor is missing/null.");
            }
            try {
                cm.sendMessage("Max Favor: " + DMiscUtil.getFavorCap(p));
            } catch (NullPointerException ne) {
                cm.sendMessage(ChatColor.RED + "Max Favor is missing/null.");
            }
            try {
                String s = "";
                for (Deity d : DMiscUtil.getDeities(p)) {
                    String name = d.getName();
                    try {
                        s += " " + name + ";" + DMiscUtil.getDevotion(p, name);
                    } catch (Exception e) {
                        cm.sendMessage(ChatColor.RED + "Error loading " + name + ".");
                    }
                }
                cm.sendMessage("Deities:" + s);
            } catch (NullPointerException ne) {
                cm.sendMessage(ChatColor.RED + "Deities are missing/null.");
            }
            try {
                cm.sendMessage("Ascensions: " + DMiscUtil.getAscensions(p));
            } catch (NullPointerException ne) {
                cm.sendMessage(ChatColor.RED + "Ascensions are missing/null.");
            }
            try {
                cm.sendMessage("Kills: " + DMiscUtil.getKills(p));
            } catch (NullPointerException ne) {
                cm.sendMessage(ChatColor.RED + "Kills are missing/null.");
            }
            try {
                cm.sendMessage("Deaths: " + DMiscUtil.getDeaths(p));
            } catch (NullPointerException ne) {
                cm.sendMessage(ChatColor.RED + "Deaths are missing/null.");
            }
            try {
                cm.sendMessage("Accessible:");
                for (WriteLocation w : DMiscUtil.getAccessibleShrines(p)) {
                    String name = DMiscUtil.getShrineName(w);
                    try {
                        cm.sendMessage(name + " " + w.getX() + " " + w.getY() + " " + w.getZ() + " " + w.getWorld());
                    } catch (Exception e) {
                        cm.sendMessage(ChatColor.RED + "Error loading " + name + ".");
                    }
                }
            } catch (NullPointerException ne) {
                cm.sendMessage(ChatColor.RED + "Accessible shrines list is missing/null.");
            }
            // Bindings will be cleared
            // Effects will be cleared
            try {
                cm.sendMessage("Shrines:");
                for (String name : DMiscUtil.getShrines(p).keySet()) {
                    try {
                        WriteLocation w = DMiscUtil.getShrines(p).get(name);
                        StringBuilder names = new StringBuilder();
                        for (UUID player : DMiscUtil.getShrineGuestlist(w))
                            names.append(player).append(" ");
                        cm.sendMessage(name + " " + w.getX() + " " + w.getY() + " " + w.getZ() + " " + w.getWorld() + " " + names.toString().trim());
                    } catch (Exception e) {
                        cm.sendMessage(ChatColor.RED + "Error loading shrine \"" + name + "\".");
                    }
                }
            } catch (NullPointerException ne) {
                cm.sendMessage(ChatColor.RED + "Shrines are missing/null.");
            }
            // All keys
            String keys = "";
            for (String key : DSave.getAllData(p).keySet())
                keys += key + ", ";
            if (keys.length() > 0) keys = keys.substring(0, keys.length() - 2);
            cm.sendMessage(ChatColor.YELLOW + "All keys in save: " + keys);
        }

        /**
         * Used to print a player's data to console.
         *
         * @param cm
         * @param p
         */
        public static void printData(Logger cm, UUID p) {
            try {
                cm.info("Name: " + DMiscUtil.getLastKnownName(p));
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "Name is missing/null.");
            }
            try {
                cm.info("Alliance: " + DMiscUtil.getAllegiance(p));
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "Alliance is missing/null.");
            }
            try {
                cm.info("Current HP: " + DMiscUtil.getHP(p));
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "HP is missing/null.");
            }
            try {
                cm.info("Max HP: " + DMiscUtil.getMaxHP(p));
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "Max HP is missing/null.");
            }
            try {
                cm.info("Current Favor: " + DMiscUtil.getFavor(p));
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "Favor is missing/null.");
            }
            try {
                cm.info("Max Favor: " + DMiscUtil.getFavorCap(p));
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "Max Favor is missing/null.");
            }
            try {
                String s = "";
                for (Deity d : DMiscUtil.getDeities(p)) {
                    String name = d.getName();
                    try {
                        s += " " + name + ";" + DMiscUtil.getDevotion(p, name);
                    } catch (Exception e) {
                        cm.warning(ChatColor.RED + "Error loading " + name + ".");
                    }
                }
                cm.info("Deities:" + s);
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "Deities are missing/null.");
            }
            try {
                cm.info("Ascensions: " + DMiscUtil.getAscensions(p));
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "Ascensions are missing/null.");
            }
            try {
                cm.info("Kills: " + DMiscUtil.getKills(p));
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "Kills are missing/null.");
            }
            try {
                cm.info("Deaths: " + DMiscUtil.getDeaths(p));
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "Deaths are missing/null.");
            }
            try {
                cm.info("Accessible:");
                for (WriteLocation w : DMiscUtil.getAccessibleShrines(p)) {
                    String name = DMiscUtil.getShrineName(w);
                    try {
                        cm.info(name + " " + w.getX() + " " + w.getY() + " " + w.getZ() + " " + w.getWorld());
                    } catch (Exception e) {
                        cm.info(ChatColor.RED + "Error loading " + name + ".");
                    }
                }
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "Accessible shrines list is missing/null.");
            }
            // Bindings will be cleared
            // Effects will be cleared
            try {
                cm.info("Shrines:");
                for (String name : DMiscUtil.getShrines(p).keySet()) {
                    try {
                        WriteLocation w = DMiscUtil.getShrines(p).get(name);
                        StringBuilder names = new StringBuilder();
                        for (UUID player : DMiscUtil.getShrineGuestlist(w))
                            names.append(DMiscUtil.getLastKnownName(player)).append(" ");
                        cm.info(name + " " + w.getX() + " " + w.getY() + " " + w.getZ() + " " + w.getWorld() + " " + names.toString().trim());
                    } catch (Exception e) {
                        cm.warning(ChatColor.RED + "Error loading shrine \"" + name + "\".");
                    }
                }
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "Shrines are missing/null.");
            }
            // All keys
            String keys = "";
            for (String key : DSave.getAllData(p).keySet())
                keys += key + ", ";
            if (keys.length() > 0) keys = keys.substring(0, keys.length() - 2);
            cm.info(ChatColor.YELLOW + "All keys in save: " + keys);
        }

        /**
         * Saves a player's data as a text file.
         *
         * @param p
         * @throws IOException
         */
        public static void writeData(UUID p) throws IOException {
            Logger.getLogger("Minecraft").info("[Demigods] Writing debug data for " + p + "...");
            FileWriter f = new FileWriter(new File(DSave.getPlayerSavePath() + p + ".txt"));
            try {
                f.write("Name: " + DMiscUtil.getLastKnownName(p) + "\r\n");
            } catch (NullPointerException ne) {
            }
            f.write("UUID: " + p.toString());
            try {
                f.write("Alliance: " + DMiscUtil.getAllegiance(p) + "\r\n");
            } catch (NullPointerException ne) {
                f.write("Alliance: NULL" + "\r\n");
            }
            try {
                f.write("Current_HP: " + DMiscUtil.getHP(p) + "\r\n");
            } catch (NullPointerException ne) {
                f.write("Current_HP: NULL" + "\r\n");
            }
            try {
                f.write("Max_HP: " + DMiscUtil.getMaxHP(p) + "\r\n");
            } catch (NullPointerException ne) {
                f.write("Max_HP: NULL" + "\r\n");
            }
            try {
                f.write("Current_Favor: " + DMiscUtil.getFavor(p) + "\r\n");
            } catch (NullPointerException ne) {
                f.write("Current_Favor: NULL" + "\r\n");
            }
            try {
                f.write("Max_Favor: " + DMiscUtil.getFavorCap(p) + "\r\n");
            } catch (NullPointerException ne) {
                f.write("Max_Favor: NULL" + "\r\n");
            }
            try {
                String s = "";
                for (Deity d : DMiscUtil.getDeities(p)) {
                    String name = d.getName();
                    try {
                        s += " " + name + ";" + DMiscUtil.getDevotion(p, name);
                    } catch (Exception e) {
                        s += " " + name + ";NULL";
                    }
                }
                f.write("Deities:" + s + "\r\n");
            } catch (NullPointerException ne) {
                f.write("Deities:\r\n");
            }
            try {
                f.write("Ascensions: " + DMiscUtil.getAscensions(p) + "\r\n");
            } catch (NullPointerException ne) {
                f.write("Ascensions: NULL" + "\r\n");
            }
            try {
                f.write("Kills: " + DMiscUtil.getKills(p) + "\r\n");
            } catch (NullPointerException ne) {
                f.write("Kills: NULL" + "\r\n");
            }
            try {
                f.write("Deaths: " + DMiscUtil.getDeaths(p) + "\r\n");
            } catch (NullPointerException ne) {
                f.write("Deaths: NULL" + "\r\n");
            }
            try {
                String s = "Accessible:\r\n";
                for (WriteLocation wr : DMiscUtil.getAccessibleShrines(p)) {
                    String name = DMiscUtil.getShrineName(wr);
                    try {
                        s += name + " " + wr.getX() + " " + wr.getY() + " " + wr.getZ() + " " + wr.getWorld() + "\r\n";
                    } catch (Exception e) {
                        s += name + " " + "ERROR" + "\r\n";
                    }
                }
                f.write(s);
            } catch (NullPointerException ne) {
                f.write("Accessible:\r\n");
            }
            // Bindings will be cleared on load
            // Effects will be cleared on load
            try {
                String s = "Shrines:\r\n";
                for (String name : DMiscUtil.getShrines(p).keySet()) {
                    try {
                        WriteLocation w = DMiscUtil.getShrines(p).get(name);
                        String names = "";
                        for (UUID player : DMiscUtil.getShrineGuestlist(w))
                            names += player + " ";
                        names = names.trim();
                        s += (name + " " + w.getX() + " " + w.getY() + " " + w.getZ() + " " + w.getWorld() + " " + names + "\r\n");
                    } catch (Exception e) {
                        s += (name + " " + "ERROR" + "\r\n");
                    }
                }
                f.write(s);
            } catch (NullPointerException ne) {
                f.write("Shrines:\r\n");
            }
            // All keys
            String keys = "";
            for (String key : DSave.getAllData(p).keySet())
                keys += key + ", ";
            if (keys.length() > 0) keys = keys.substring(0, keys.length() - 2);
            f.write("All keys in save: " + keys);
            f.close();
            Logger.getLogger("Minecraft").info("[Demigods] Finished writing debug data.");
        }

        public static void writeLegacyData(UUID p) throws IOException {
            // Legacy folder.
            File legacyFolder = new File("plugins/NorseDemigods/Legacy");
            legacyFolder.mkdir();

            // Delete file if it exists.
            File file = new File("plugins/NorseDemigods/Legacy/" + p + ".txt");
            if (file.exists()) file.delete();

            FileWriter f = new FileWriter(file);
            try {
                f.write("Name: " + DMiscUtil.getLastKnownName(p) + "\r\n");
            } catch (NullPointerException ne) {
                f.write("Name: " + p + "/r/n");
            }
            try {
                f.write("Alliance: " + DMiscUtil.getAllegiance(p) + "\r\n");
            } catch (NullPointerException ne) {
                f.write("Alliance: NULL" + "\r\n");
            }
            try {
                f.write("Current_HP: " + DMiscUtil.getHP(p) + "\r\n");
            } catch (NullPointerException ne) {
                f.write("Current_HP: NULL" + "\r\n");
            }
            try {
                f.write("Max_HP: " + DMiscUtil.getMaxHP(p) + "\r\n");
            } catch (NullPointerException ne) {
                f.write("Max_HP: NULL" + "\r\n");
            }
            try {
                f.write("Current_Favor: " + DMiscUtil.getFavor(p) + "\r\n");
            } catch (NullPointerException ne) {
                f.write("Current_Favor: NULL" + "\r\n");
            }
            try {
                f.write("Max_Favor: " + DMiscUtil.getFavorCap(p) + "\r\n");
            } catch (NullPointerException ne) {
                f.write("Max_Favor: NULL" + "\r\n");
            }
            try {
                String s = "";
                for (Deity d : DMiscUtil.getDeities(p)) {
                    String name = d.getName();
                    try {
                        s += " " + name + ";" + DMiscUtil.getDevotion(p, name);
                    } catch (Exception e) {
                        s += " " + name + ";NULL";
                    }
                }
                f.write("Deities:" + s + "\r\n");
            } catch (NullPointerException ne) {
                f.write("Deities:\r\n");
            }
            try {
                f.write("Ascensions: " + DMiscUtil.getAscensions(p) + "\r\n");
            } catch (NullPointerException ne) {
                f.write("Ascensions: NULL" + "\r\n");
            }
            try {
                f.write("Kills: " + DMiscUtil.getKills(p) + "\r\n");
            } catch (NullPointerException ne) {
                f.write("Kills: NULL" + "\r\n");
            }
            try {
                f.write("Deaths: " + DMiscUtil.getDeaths(p) + "\r\n");
            } catch (NullPointerException ne) {
                f.write("Deaths: NULL" + "\r\n");
            }
            try {
                String s = "Accessible:\r\n";
                for (WriteLocation wr : DMiscUtil.getAccessibleShrines(p)) {
                    String name = DMiscUtil.getShrineName(wr);
                    try {
                        s += name + " " + wr.getX() + " " + wr.getY() + " " + wr.getZ() + " " + wr.getWorld() + "\r\n";
                    } catch (Exception e) {
                        s += name + " " + "ERROR" + "\r\n";
                    }
                }
                f.write(s);
            } catch (NullPointerException ne) {
                f.write("Accessible:\r\n");
            }
            // Bindings will be cleared on load
            // Effects will be cleared on load
            try {
                String s = "Shrines:\r\n";
                for (String name : DMiscUtil.getShrines(p).keySet()) {
                    try {
                        WriteLocation w = DMiscUtil.getShrines(p).get(name);
                        String names = "";
                        for (UUID player : DMiscUtil.getShrineGuestlist(w))
                            names += player + " ";
                        names = names.trim();
                        s += (name + " " + w.getX() + " " + w.getY() + " " + w.getZ() + " " + w.getWorld() + " " + names + "\r\n");
                    } catch (Exception e) {
                        s += (name + " " + "ERROR" + "\r\n");
                    }
                }
                f.write(s);
            } catch (NullPointerException ne) {
                f.write("Shrines:\r\n");
            }
            // All keys
            String keys = "";
            for (String key : DSave.getAllData(p).keySet())
                keys += key + ", ";
            if (keys.length() > 0) keys = keys.substring(0, keys.length() - 2);
            f.write("All keys in save: " + keys);
            f.close();
        }
    }
}
