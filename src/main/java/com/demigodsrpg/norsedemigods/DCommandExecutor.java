package com.demigodsrpg.norsedemigods;

import com.demigodsrpg.norsedemigods.deity.Deities;
import com.demigodsrpg.norsedemigods.deity.aesir.Dwarf;
import com.demigodsrpg.norsedemigods.deity.jotunn.Dis;
import com.demigodsrpg.norsedemigods.deity.jotunn.FireGiant;
import com.demigodsrpg.norsedemigods.deity.jotunn.FrostGiant;
import com.demigodsrpg.norsedemigods.listener.DLevels;
import com.demigodsrpg.norsedemigods.saveable.LocationSaveable;
import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
import com.demigodsrpg.norsedemigods.saveable.ShrineSaveable;
import org.bukkit.Bukkit;
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

import java.util.*;
import java.util.logging.Logger;

public class DCommandExecutor implements CommandExecutor {
    private final NorseDemigods plugin;
    public static final double ADVANTAGEPERCENT = 1.3;
    private static final double TRANSFERTAX = 0.9;

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

        } else {
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
                boolean bind = false;
                if (args.length == 1) if (args[0].contains("bind")) bind = true;
                if (DMisc.getDeities(p) != null) for (Deity d : DMisc.getDeities(p))
                    d.onCommand(p, c.getName(), args, bind);
            }

        }
        return false;
    }

    /*
     * Every command gets it's own method below.
     */

    private boolean setFavor(String[] args) {
        if (args.length != 2) return false;
        try {
            UUID pl = DMisc.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            DMisc.setFavor(pl, amt);
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
            UUID pl = DMisc.getDemigodsPlayerId(args[0]);
            Logger.getLogger("Minecraft").info(DMisc.getFavor(pl) + "");
            return true;
        } catch (Exception error) {
            Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
            return false;
        }
    }

    private boolean addFavor(String[] args) {
        if (args.length != 2) return false;
        try {
            UUID pl = DMisc.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            DMisc.setFavor(pl, amt + DMisc.getFavor(pl));
            Logger.getLogger("Minecraft").info("[Demigods] Increased " + pl + "'s Favor by " + amt + " to " + DMisc.getFavor(pl) + ".");
            return true;
        } catch (Exception error) {
            Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
            return false;
        }
    }

    private boolean setMaxFavor(String[] args) {
        if (args.length != 2) return false;
        try {
            UUID pl = DMisc.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            DMisc.setFavorCap(pl, amt);
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
            UUID pl = DMisc.getDemigodsPlayerId(args[0]);
            Logger.getLogger("Minecraft").info(DMisc.getFavorCap(pl) + "");
            return true;
        } catch (Exception error) {
            Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
            return false;
        }
    }

    private boolean addMaxFavor(String[] args) {
        if (args.length != 2) return false;
        try {
            UUID pl = DMisc.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            DMisc.setFavorCap(pl, amt + DMisc.getFavor(pl));
            Logger.getLogger("Minecraft").info("[Demigods] Increased " + pl + "'s max Favor by " + amt + " to " + DMisc.getFavor(pl) + ".");
            return true;
        } catch (Exception error) {
            Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
            return false;
        }
    }

    private boolean getAscensions(String[] args) {
        if (args.length != 1) return false;
        try {
            UUID pl = DMisc.getDemigodsPlayerId(args[0]);
            Logger.getLogger("Minecraft").info(DMisc.getAscensions(pl) + "");
            return true;
        } catch (Exception error) {
            Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
            return false;
        }
    }

    private boolean setAscensions(String[] args) {
        if (args.length != 2) return false;
        try {
            UUID target = DMisc.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            DMisc.setAscensions(target, amt);
            long oldtotal = DMisc.getDevotion(target);
            int newtotal = DMisc.costForNextAscension(amt - 1);
            for (Deity d : DMisc.getDeities(target)) {
                int devotion = DMisc.getDevotion(target, d);
                DMisc.setDevotion(target, d, (int) Math.ceil((newtotal * 1.0 * devotion) / oldtotal));
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
            UUID target = DMisc.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            DMisc.setAscensions(target, DMisc.getAscensions(target) + amt);
            Logger.getLogger("Minecraft").info("[Demigods] Increased " + target + "'s ascensions by " + amt + " to " + DMisc.getAscensions(target) + ".");
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
            Player pl = Bukkit.getPlayer(args[0]);
            int amt = Integer.parseInt(args[1]);
            DMisc.setHP(pl, amt + pl.getHealth());
            Logger.getLogger("Minecraft").info("[Demigods] Increased " + pl + "'s hp by " + amt + " to " + pl.getHealth() + ".");
            return true;
        } catch (Exception error) {
            Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
            return false;
        }
    }

    private boolean getDevotion(String[] args) {
        if (args.length == 1) {
            try {
                UUID pl = DMisc.getDemigodsPlayerId(args[0]);
                Logger.getLogger("Minecraft").info(DMisc.getDevotion(pl) + "");
                return true;
            } catch (Exception error) {
                Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
                return false;
            }
        } else if (args.length == 2) {
            try {
                UUID pl = DMisc.getDemigodsPlayerId(args[0]);
                Deity deity = Deities.valueOf(args[0]);
                Logger.getLogger("Minecraft").info(DMisc.getDevotion(pl, deity) + "");
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
            UUID pl = DMisc.getDemigodsPlayerId(args[0]);
            Deity deity = Deities.valueOf(args[0]);
            int amt = Integer.parseInt(args[2]);
            DMisc.setDevotion(pl, deity, amt);
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
            UUID pl = DMisc.getDemigodsPlayerId(args[0]);
            Deity deity = Deities.valueOf(args[0]);
            int amt = Integer.parseInt(args[2]);
            int before = DMisc.getDevotion(pl, deity);
            DMisc.setDevotion(pl, deity, before + amt);
            Logger.getLogger("Minecraft").info("[Demigods] Increased " + pl + "'s devotion for " + deity.getName() + " by " + amt + " to " + DMisc.getDevotion(pl, deity) + ".");
            return true;
        } catch (Exception error) {
            Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
            return false;
        }
    }

    private boolean addUnclaimedDevotion(String[] args) {
        if (args.length != 2) return false;
        try {
            UUID pl = DMisc.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            int before = DMisc.getUnclaimedDevotion(pl);
            DMisc.setUnclaimedDevotion(pl, before + amt);
            Logger.getLogger("Minecraft").info("[Demigods] Increased " + pl + "'s unclaimed devotion by " + amt + " to " + DMisc.getUnclaimedDevotion(pl) + ".");
            return true;
        } catch (Exception error) {
            Logger.getLogger("Minecraft").warning("[Demigods] Unable to parse command.");
            return false;
        }
    }

    private boolean infoDG(Player p, String[] args) {
        if ((args.length == 2) || (args.length == 3)) {
            if (args[0].equalsIgnoreCase("debug") && DMisc.isAdminOrOp(p)) {
                UUID target = DMisc.getDemigodsPlayerId(args[1]);
                if (target == null) {
                    p.sendMessage(ChatColor.YELLOW + "Player not found.");
                    return true;
                }
                DDebug.printData(p, target);
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
                for (PlayerDataSaveable save : plugin.getPlayerDataRegistry().getFromDb().values()) {
                    try {
                        UUID id = save.getPlayerId();
                        if (!DMisc.isFullParticipant(id)) continue;
                        if (save.getLastLoginTime() < System.currentTimeMillis() - 604800000)
                            continue;
                        if (DMisc.getAllegiance(id).equalsIgnoreCase("Jotunn")) {
                            titancount++;
                            titankills += DMisc.getKills(id);
                            titandeaths += DMisc.getDeaths(id);
                            if (DMisc.getPlugin().getServer().getPlayer(id).isOnline()) {
                                onlinetitans.add(id);
                            }
                        } else if (DMisc.getAllegiance(id).equalsIgnoreCase("AEsir")) {
                            godcount++;
                            godkills += DMisc.getKills(id);
                            goddeaths += DMisc.getDeaths(id);
                            if (DMisc.getPlugin().getServer().getPlayer(id).isOnline()) {
                                onlinegods.add(id);
                            }
                        } else {
                            if (!DMisc.isFullParticipant(id)) continue;
                            othercount++;
                            otherkills += DMisc.getKills(id);
                            otherdeaths += DMisc.getDeaths(id);
                            if (DMisc.getPlugin().getServer().getPlayer(id).isOnline()) {
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
                StringBuilder str1 = new StringBuilder();
                if (onlinegods.size() > 0) {
                    for (UUID g : onlinegods) {
                        str1.append(DMisc.getLastKnownName(g)).append(", ");
                    }
                    str1 = new StringBuilder(str1.substring(0, str1.length() - 2));
                }
                StringBuilder str2 = new StringBuilder();
                if (onlinetitans.size() > 0) {
                    for (UUID t : onlinetitans) {
                        str2.append(DMisc.getLastKnownName(t)).append(", ");
                    }
                    str2 = new StringBuilder(str2.substring(0, str2.length() - 2));
                }
                StringBuilder str3 = new StringBuilder();
                if (onlineother.size() > 0) {
                    for (UUID o : onlineother) {
                        str3.append(DMisc.getLastKnownName(o)).append(", ");
                    }
                    str3 = new StringBuilder(str3.substring(0, str3.length() - 2));
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
                if (DMisc.getFullParticipants().size() < 1) {
                    p.sendMessage(ChatColor.GRAY + "There are no players to rank.");
                    return true;
                }
                // get list of gods and titans
                ArrayList<UUID> gods = new ArrayList<UUID>();
                ArrayList<UUID> titans = new ArrayList<UUID>();
                ArrayList<Long> gr = new ArrayList<Long>();
                ArrayList<Long> tr = new ArrayList<Long>();
                for (String s : DMisc.getFullParticipants()) {
                    Optional<PlayerDataSaveable> opSave = plugin.getPlayerDataRegistry().fromKey(s);
                    if (opSave.isPresent()) {
                        UUID id = UUID.fromString(s);
                        if (DMisc.getAllegiance(id).equalsIgnoreCase("AEsir")) {
                            if (opSave.get().getLastLoginTime() < System.currentTimeMillis() - 604800000)
                                continue;
                            gods.add(id);
                            gr.add(DMisc.getRanking(id));
                        } else if (DMisc.getAllegiance(id).equalsIgnoreCase("Jotunn")) {
                            if (opSave.get().getLastLoginTime() < System.currentTimeMillis() - 604800000)
                                continue;
                            titans.add(id);
                            tr.add(DMisc.getRanking(id));
                        }
                    }
                }
                String[] Gods = new String[gods.size()];
                String[] Titans = new String[titans.size()];
                Long[] GR = new Long[gods.size()];
                Long[] TR = new Long[titans.size()];
                for (int i = 0; i < Gods.length; i++) {
                    Gods[i] = DMisc.getLastKnownName(gods.get(i));
                    GR[i] = gr.get(i);
                }
                for (int i = 0; i < Titans.length; i++) {
                    Titans[i] = DMisc.getLastKnownName(titans.get(i));
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
                    if (Bukkit.getPlayer(Gods[i]) != null)
                        p.sendMessage(ChatColor.GREEN + "  " + (i + 1) + ". " + Gods[i] + " :: " + GR[i]);
                    else p.sendMessage(ChatColor.GRAY + "  " + (i + 1) + ". " + Gods[i] + " :: " + GR[i]);
                }
                int tp = Titans.length;
                if (tp > 5) tp = 5;
                p.sendMessage(ChatColor.DARK_RED + "-- Jotunn");
                for (int i = 0; i < tp; i++) {
                    if (Bukkit.getPlayer(Titans[i]) != null)
                        p.sendMessage(ChatColor.GREEN + "  " + (i + 1) + ". " + Titans[i] + " :: " + TR[i]);
                    else p.sendMessage(ChatColor.GRAY + "  " + (i + 1) + ". " + Titans[i] + " :: " + TR[i]);
                }
            } else {
                for (Deity deity : Deities.values()) {
                    if (deity.getName().equalsIgnoreCase(args[0])) deity.printInfo(p);
                }
            }
        } else if (args.length == 2) {
            for (Deity deity : Deities.values()) {
                if (deity.getName().equalsIgnoreCase(args[0] + " " + args[1])) deity.printInfo(p);
            }
            if (args[0].equalsIgnoreCase("ranking") || args[0].equalsIgnoreCase("rankings")) {
                if (args[1].equalsIgnoreCase("aesir") || args[1].equalsIgnoreCase("god")) {
                    // get list of gods
                    ArrayList<UUID> gods = new ArrayList<UUID>();
                    ArrayList<Long> gr = new ArrayList<Long>();
                    for (String s : DMisc.getFullParticipants()) {
                        Optional<PlayerDataSaveable> opSave = plugin.getPlayerDataRegistry().fromKey(s);
                        if (opSave.isPresent()) {
                            UUID id = UUID.fromString(s);
                            if (DMisc.getAllegiance(id).equalsIgnoreCase("AEsir")) {
                                if (opSave.get().getLastLoginTime() < System.currentTimeMillis() - 604800000)
                                    continue;
                                gods.add(id);
                                gr.add(DMisc.getRanking(id));
                            }
                        }
                    }
                    if (gods.size() < 1) {
                        p.sendMessage(ChatColor.GRAY + "There are no players to rank.");
                        return true;
                    }
                    String[] Gods = new String[gods.size()];
                    Long[] GR = new Long[gods.size()];
                    for (int i = 0; i < Gods.length; i++) {
                        Gods[i] = DMisc.getLastKnownName(gods.get(i));
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
                        if (Bukkit.getPlayer(Gods[i]) != null)
                            p.sendMessage(ChatColor.GREEN + "  " + (i + 1) + ". " + Gods[i] + " :: " + GR[i]);
                        else p.sendMessage(ChatColor.GRAY + "  " + (i + 1) + ". " + Gods[i] + " :: " + GR[i]);
                    }
                } else if (args[1].equalsIgnoreCase("jotunn")) {
                    // get list of titans
                    ArrayList<UUID> titans = new ArrayList<UUID>();
                    ArrayList<Long> tr = new ArrayList<Long>();
                    for (String s : DMisc.getFullParticipants()) {
                        Optional<PlayerDataSaveable> opSave = plugin.getPlayerDataRegistry().fromKey(s);
                        if (opSave.isPresent()) {
                            UUID id = UUID.fromString(s);
                            if (DMisc.getAllegiance(id).equalsIgnoreCase("Jotunn")) {
                                if (opSave.get().getLastLoginTime() < System.currentTimeMillis() - 604800000)
                                    continue;
                                titans.add(id);
                                tr.add(DMisc.getRanking(id));
                            }
                        }
                    }
                    if (titans.size() < 1) {
                        p.sendMessage(ChatColor.GRAY + "There are no players to rank.");
                        return true;
                    }
                    String[] Titans = new String[titans.size()];
                    Long[] TR = new Long[titans.size()];
                    for (int i = 0; i < Titans.length; i++) {
                        Titans[i] = DMisc.getLastKnownName(titans.get(i));
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
                        if (Bukkit.getPlayer(Titans[i]) != null)
                            p.sendMessage(ChatColor.GREEN + "  " + (i + 1) + ". " + Titans[i] + " :: " + TR[i]);
                        else p.sendMessage(ChatColor.GRAY + "  " + (i + 1) + ". " + Titans[i] + " :: " + TR[i]);
                    }
                }
            }
        } else if (args.length == 3) {
            if (DMisc.isAdminOrOp(p)) try {
                int one = Integer.parseInt(args[0]);
                int two = Integer.parseInt(args[1]);
                int three = Integer.parseInt(args[2]);
                DMisc.horseTeleport(p, new Location(p.getWorld(), one, two, three));
            } catch (Exception ignored) {
            }
        }
        return true;
    }

    private boolean checkCode(Player p) {
        if (!DMisc.isFullParticipant(p)) {
            p.sendMessage(ChatColor.YELLOW + "--" + p.getName() + "--Human--");
            p.sendMessage("You are not affiliated with any AEsir or Jotunn.");
            return true;
        }
        if (DMisc.getUnclaimedDevotion(p) > 0) {
            p.sendMessage(ChatColor.AQUA + "You have " + DMisc.getUnclaimedDevotion(p) + " unclaimed Devotion.");
            p.sendMessage(ChatColor.AQUA + "Allocate it with /adddevotion <deity> <amount>.");
        }
        p.sendMessage(ChatColor.YELLOW + "--" + p.getName() + "--" + DMisc.getRank(p) + "");
        // HP
        ChatColor color = ChatColor.GREEN;
        if ((p.getHealth() / p.getMaxHealth()) < 0.25) color = ChatColor.RED;
        else if ((p.getHealth() / p.getMaxHealth()) < 0.5) color = ChatColor.YELLOW;
        p.sendMessage("HP: " + color + p.getHealth() + "/" + p.getMaxHealth());
        // List deities
        StringBuilder send = new StringBuilder("You are empowered by:");
        for (Deity d : DMisc.getDeities(p)) {
            send.append(" ").append(d.getName()).append(" ").append(ChatColor.YELLOW).append(d.canTribute() ? "<" + DMisc.getDevotion(p, d) + ">" : "").append(ChatColor.WHITE);
        }
        p.sendMessage(send.toString());
        // Display Favor/Ascensions and K/D
        // float percentage = (DMiscUtil.getDevotion(p)-DMiscUtil.costForNextAscension(DMiscUtil.getAscensions(p)-1))/(float)(DMiscUtil.costForNextAscension(p)-DMiscUtil.costForNextAscension(DMiscUtil.getAscensions(p)-1))*100;
        String op = ChatColor.YELLOW + "   |   " + (DMisc.costForNextAscension(DMisc.getAscensions(p)) - DMisc.getDevotion(p)) + " until next Ascension";
        if (DMisc.getAscensions(p) >= Setting.ASCENSION_CAP) op = "";
        p.sendMessage("Devotion: " + DMisc.getDevotion(p) + op);
        p.sendMessage("Favor: " + DMisc.getFavor(p) + ChatColor.YELLOW + "/" + DMisc.getFavorCap(p));
        p.sendMessage("Ascensions: " + DMisc.getAscensions(p));
        p.sendMessage("Kills: " + ChatColor.GREEN + DMisc.getKills(p) + ChatColor.WHITE + " // " + "Deaths: " + ChatColor.RED + DMisc.getDeaths(p));
        // Deity information
        if (DMisc.getAscensions(p) < DMisc.costForNextDeity(p))
            p.sendMessage("You may form a new alliance at " + ChatColor.GOLD + DMisc.costForNextDeity(p) + ChatColor.WHITE + " Ascensions.");
        else {
            p.sendMessage(ChatColor.AQUA + "You are eligible for a new alliance.");
        }
        // Effects
        if (DMisc.getActiveEffects(p.getUniqueId()).size() > 0) {
            StringBuilder printout = new StringBuilder(ChatColor.YELLOW + "Active effects:");
            Map<String, Double> fx = DMisc.getActiveEffects(p.getUniqueId());
            for (Map.Entry<String, Double> str : fx.entrySet()) {
                printout.append(" ").append(str.getKey()).append("[").append(Math.round(str.getValue() - System.currentTimeMillis()) / 1000).append("s]");
            }
            p.sendMessage(printout.toString());
        }
        return true;
    }

    @SuppressWarnings("unused")
    private boolean transfer(Player p, String[] args) {
        if (!DMisc.isFullParticipant(p)) return true;
        if (args.length == 1) {
            try {
                int give = Integer.parseInt(args[0]);
                if (DMisc.getFavor(p) < give) {
                    p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor.");
                    return true;
                }
                List<Block> bL = p.getLineOfSight((Set) null, 5);
                for (Block b : bL) {
                    for (Player pl : p.getWorld().getPlayers()) {
                        if (pl.getLocation().distance(b.getLocation()) < 0.8) {
                            if (!DMisc.isFullParticipant(pl)) continue;
                            if (!DMisc.getAllegiance(pl).equalsIgnoreCase(DMisc.getAllegiance(p))) continue;
                            DMisc.setFavor(pl, DMisc.getFavor(pl) + give);
                            DMisc.setFavor(p, DMisc.getFavor(p) - give);
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
                Player pl = Bukkit.getPlayer(args[0]);
                if (pl.getUniqueId().equals(p.getUniqueId())) {
                    p.sendMessage(ChatColor.YELLOW + "You cannot send Favor to yourself.");
                    return true;
                }
                int give = Integer.parseInt(args[1]);
                int tax = (int) (TRANSFERTAX * give);
                if (DMisc.getFavor(p) < (give + tax)) {
                    p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor.");
                    p.sendMessage(ChatColor.YELLOW + "The tax for this long-distance transfer is " + tax + ".");
                    return true;
                }
                if (!DMisc.isFullParticipant(pl)) return true;
                if (!DMisc.getAllegiance(pl).equalsIgnoreCase(DMisc.getAllegiance(p))) return true;
                DMisc.setFavor(pl, DMisc.getFavor(pl) + give);
                DMisc.setFavor(p, DMisc.getFavor(p) - give - tax);
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
        if (!DMisc.isFullParticipant(p)) return true;
        PlayerDataSaveable save = plugin.getPlayerDataRegistry().fromPlayer(p);
        if (save.getTempStatus("ALLIANCECHAT")) {
            p.sendMessage(ChatColor.YELLOW + "Alliance chat has been turned off.");
            save.removeTempData("ALLIANCECHAT");
            return true;
        }
        p.sendMessage(ChatColor.YELLOW + "Alliance chat has been turned on.");
        save.setTempData("ALLIANCECHAT", true);
        return true;
    }

    private boolean checkPlayer(Player p, String[] args) {
        if (!(p.hasPermission("demigods.checkplayer") || p.hasPermission("demigods.admin")))
            return true;
        if (args.length != 1) return false;
        try {
            Player ptarget = Bukkit.getPlayer(args[0]);
            UUID target = DMisc.getDemigodsPlayerId(args[0]);
            if (DMisc.isFullParticipant(target)) {
                p.sendMessage(ChatColor.YELLOW + "--" + target + "--");
                // List deities
                StringBuilder send = new StringBuilder(target + "'s deities are:");
                for (Deity d : DMisc.getDeities(target)) {
                    send.append(" ").append(d.getName()).append(" ").append(ChatColor.YELLOW).append("<").append(DMisc.getDevotion(target, d)).append(">").append(ChatColor.WHITE);
                }
                p.sendMessage(send.toString());
                // HP
                ChatColor color = ChatColor.GREEN;
                if (ptarget != null) {
                    if ((ptarget.getHealth() / ptarget.getMaxHealth()) < 0.25) color = ChatColor.RED;
                    else if (ptarget.getHealth() / ptarget.getMaxHealth() < 0.5) color = ChatColor.YELLOW;
                    p.sendMessage("HP: " + color + ptarget.getHealth() + "/" + ptarget.getMaxHealth());
                }
                // Display Favor/Ascensions and K/D
                p.sendMessage("Devotion: " + DMisc.getDevotion(target) + ChatColor.YELLOW + "   |   " + (DMisc.costForNextAscension(DMisc.getAscensions(target)) - DMisc.getDevotion(target)) + " until next Ascension");
                p.sendMessage("Favor: " + DMisc.getFavor(target) + ChatColor.YELLOW + "/" + DMisc.getFavorCap(target));
                p.sendMessage("Ascensions: " + DMisc.getAscensions(target));
                p.sendMessage("Kills: " + ChatColor.GREEN + DMisc.getKills(target) + ChatColor.WHITE + " // " + "Deaths: " + ChatColor.RED + DMisc.getDeaths(target));
                // Deity information
                if (DMisc.costForNextDeity(target) > DMisc.getAscensions(target))
                    p.sendMessage(target + " may form a new alliance at " + ChatColor.GOLD + DMisc.costForNextDeity(target) + ChatColor.WHITE + " Ascensions.");
                else {
                    p.sendMessage(ChatColor.AQUA + DMisc.getLastKnownName(target) + " is eligible for a new alliance.");
                }
                // Effects
                if (DMisc.getActiveEffectsList(target).size() > 0) {
                    StringBuilder printout = new StringBuilder(ChatColor.YELLOW + "Active effects:");
                    for (String str : DMisc.getActiveEffectsList(target))
                        printout.append(" ").append(str);
                    p.sendMessage(printout.toString());
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
        if (!DMisc.isFullParticipant(p)) return true;
        // player has shrines for these deities
        StringBuilder str1 = new StringBuilder("Shrines:");
        for (ShrineSaveable shrine : DMisc.getShrines(p.getUniqueId())) {
            str1.append(" ").append(shrine.getDeity());
        }
        // player's named shrines
        StringBuilder str2 = new StringBuilder("Named shrines:");
        for (ShrineSaveable shrine : DMisc.getShrines(p.getUniqueId())) {
            str2.append(" ").append(shrine.getName());
        }
        // player can warp to these shrines
        StringBuilder str3 = new StringBuilder("Other shrines you may warp to:");
        for (ShrineSaveable shrine : DMisc.getAccessibleShrines(p.getUniqueId())) {
            str3.append(" ").append(shrine.getName());
        }
        p.sendMessage(ChatColor.YELLOW + str1.toString());
        p.sendMessage(ChatColor.YELLOW + str2.toString());
        p.sendMessage(ChatColor.LIGHT_PURPLE + str3.toString());
        return true;
    }

    private boolean shrineWarp(Player p, String[] args) {
        if (!(p.hasPermission("demigods.shrinewarp") || p.hasPermission("demigods.admin")))
            return true;
        ShrineSaveable target = null;
        if ((args.length != 1) && (args.length != 2)) return false;
        if (args.length == 1)
            // try matching the name to deities the player has
            target = DMisc.getShrine(p.getUniqueId(), args[0]);
        // try matching the name to another player's warp
        if ((target == null) && (args.length == 2)) {
            if (DMisc.isFullParticipant(DMisc.getDemigodsPlayerId(args[0]))) {
                target = DMisc.getShrine(DMisc.getDemigodsPlayerId(args[0]), args[1]);
            }
        }
        if ((target == null) && (args.length == 1)) target = DMisc.getShrineByName(args[0]);
        if (target == null) {
            p.sendMessage(ChatColor.YELLOW + "Target shrine not found. Shrine names are case sensitive.");
            return true;
        }
        // check for permission
        String playerId = p.getUniqueId().toString();
        if (!target.getGuestIds().contains(playerId) && !target.getOwnerId().equals(playerId)) {
            p.sendMessage(ChatColor.YELLOW + "You do not have permission for that warp.");
            return true;
        }
        // check if warp is clear
        Block b = DMisc.toLocation(target).getBlock();
        Block b1 = b.getRelative(BlockFace.UP);
        Block b2 = b1.getRelative(BlockFace.UP);
        if (b1.getType().isSolid() || b1.isLiquid() || b2.getType().isSolid() || b2.isLiquid()) {
            p.sendMessage(ChatColor.YELLOW + "The target location is blocked.");
            return true;
        }
        // warp code
        final LocationSaveable current = DMisc.toWriteLocation(p.getLocation());
        final double hp = p.getHealth();
        final float pitch = p.getLocation().getPitch();
        final float yaw = p.getLocation().getYaw();
        final Player pt = p;
        final LocationSaveable TARGET = DMisc.toWriteLocation(b.getRelative(BlockFace.UP).getLocation());
        DMisc.addActiveEffect(p.getUniqueId(), "Warping", 1000);
        p.sendMessage(ChatColor.YELLOW + "Don't move, warping in progress...");
        for (int i = 20; i <= 80; i += 20) {
            DMisc.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMisc.getPlugin(), () -> {
                if (DMisc.getActiveEffectsList(pt.getUniqueId()).contains("Warping")) {
                    if (!current.equalsApprox(DMisc.toWriteLocation(pt.getLocation()))) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to movement.");
                        DMisc.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                    if (pt.getHealth() < hp) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to loss of health.");
                        DMisc.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                }
            }, i);
        }
        DMisc.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMisc.getPlugin(), () -> {
            if (DMisc.getActiveEffectsList(pt.getUniqueId()).contains("Warping")) {
                Location newloc = DMisc.toLocation(TARGET);
                newloc.setPitch(pitch);
                newloc.setYaw(yaw);
                newloc.setX(newloc.getX() + 0.5);
                newloc.setZ(newloc.getZ() + 0.5);
                DMisc.horseTeleport(pt, newloc);
                pt.sendMessage(ChatColor.YELLOW + "Shrine warp successful.");
                DMisc.removeActiveEffect(pt.getUniqueId(), "Warping");
            }
        }, 90);
        return true;
    }

    private boolean forceShrineWarp(Player p, String[] args) {
        if (!(p.hasPermission("demigods.shrinewarp") || p.hasPermission("demigods.admin")))
            return true;
        ShrineSaveable target = null;
        if ((args.length != 1) && (args.length != 2)) return false;
        if (args.length == 1)
            // try matching the name to deities the player has
            target = DMisc.getShrine(p.getUniqueId(), args[0]);
        // try matching the name to another player's warp
        if ((target == null) && (args.length == 2)) {
            if (DMisc.isFullParticipant(DMisc.getDemigodsPlayerId(args[0]))) {
                target = DMisc.getShrine(DMisc.getDemigodsPlayerId(args[0]), args[1]);
            }
        }
        if ((target == null) && (args.length == 1)) target = DMisc.getShrineByName(args[0]);
        if (target == null) {
            p.sendMessage(ChatColor.YELLOW + "Target shrine not found. Shrine names are case sensitive.");
            return true;
        }
        // check for permission
        String playerId = p.getUniqueId().toString();
        if (!target.getGuestIds().contains(playerId) && !target.getOwnerId().equals(playerId)) {
            p.sendMessage(ChatColor.YELLOW + "You do not have permission for that warp.");
            return true;
        }
        Block b = DMisc.toLocation(target).getBlock();
        /*if ((b.getRelative(BlockFace.UP).getType() != Material.AIR) || (b.getRelative(BlockFace.UP).getRelative(BlockFace.UP).getType() != Material.AIR)) {
            p.sendMessage(ChatColor.YELLOW + "The target location is blocked.");
            return true;
        }*/
        // warp code
        final LocationSaveable current = DMisc.toWriteLocation(p.getLocation());
        final double hp = p.getHealth();
        final float pitch = p.getLocation().getPitch();
        final float yaw = p.getLocation().getYaw();
        final Player pt = p;
        final LocationSaveable TARGET = DMisc.toWriteLocation(b.getRelative(BlockFace.UP).getLocation());
        DMisc.addActiveEffect(p.getUniqueId(), "Warping", 1000);
        p.sendMessage(ChatColor.YELLOW + "Don't move, warping in progress...");
        for (int i = 20; i <= 180; i += 20) {
            DMisc.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMisc.getPlugin(), () -> {
                if (DMisc.getActiveEffectsList(pt.getUniqueId()).contains("Warping")) {
                    if (!current.equalsApprox(DMisc.toWriteLocation(pt.getLocation()))) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to movement.");
                        DMisc.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                    if (pt.getHealth() < hp) {
                        pt.sendMessage(ChatColor.RED + "Warp cancelled due to loss of health.");
                        DMisc.removeActiveEffect(pt.getUniqueId(), "Warping");
                    }
                }
            }, i);
        }
        DMisc.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMisc.getPlugin(), () -> {
            if (DMisc.getActiveEffectsList(pt.getUniqueId()).contains("Warping")) {
                Location newloc = DMisc.toLocation(TARGET);
                newloc.setPitch(pitch);
                newloc.setYaw(yaw);
                newloc.setX(newloc.getX() + 0.5);
                newloc.setZ(newloc.getZ() + 0.5);
                DMisc.horseTeleport(pt, newloc);
                pt.sendMessage(ChatColor.YELLOW + "Shrine warp successful.");
                DMisc.removeActiveEffect(pt.getUniqueId(), "Warping");
            }
        }, 190);
        return true;
    }

    private boolean shrineOwner(Player p, String[] args) {
        if (!(p.hasPermission("demigods.shrineowner") || p.hasPermission("demigods.admin")))
            return true;
        if (args.length != 2) return false;
        ShrineSaveable shrine = DMisc.getNearbyShrine(p.getLocation());
        if (shrine == null) {
            p.sendMessage(ChatColor.YELLOW + "No shrine nearby.");
            return true;
        }
        if (!shrine.getOwnerId().equals(p.getUniqueId().toString()) && !p.hasPermission("demigods.admin")) {
            p.sendMessage(ChatColor.YELLOW + "Only admins and the creator of a shrine can modify it.");
            return true;
        }
        // add <name>
        if (args[0].equalsIgnoreCase("add")) {
            UUID toadd = DMisc.getDemigodsPlayerId(args[1]);
            if (toadd == null) {
                p.sendMessage(ChatColor.YELLOW + "Player not found.");
            } else if (toadd.equals(p.getUniqueId())) {
                p.sendMessage(ChatColor.YELLOW + "You are already the shrine owner.");
            } else if (shrine.getGuestIds().contains(toadd.toString())) {
                p.sendMessage(ChatColor.YELLOW + DMisc.getLastKnownName(toadd) + " already has permission to warp to this shrine.");
            } else if (!DMisc.getAllegiance(toadd).equals(DMisc.getAllegiance(p))) {
                p.sendMessage(ChatColor.YELLOW + DMisc.getLastKnownName(toadd) + " is not in your alliance.");
            } else {
                p.sendMessage(ChatColor.YELLOW + DMisc.getLastKnownName(toadd) + " now has permission to warp to this shrine.");
                shrine.addGuest(toadd.toString());
            }
        }
        // remove <name>
        else if (args[0].equalsIgnoreCase("remove")) {
            UUID remove = DMisc.getDemigodsPlayerId(args[1]);
            if (remove == null) {
                p.sendMessage(ChatColor.YELLOW + "Player not found.");
            } else if (remove.equals(p.getUniqueId())) {
                p.sendMessage(ChatColor.YELLOW + "You cannot remove yourself as an owner.");
            } else if (!shrine.getGuestIds().contains(remove.toString())) {
                p.sendMessage(ChatColor.YELLOW + DMisc.getLastKnownName(remove) + " is not an owner of this shrine.");
            } else {
                shrine.removeGuest(remove.toString());
                p.sendMessage(ChatColor.YELLOW + DMisc.getLastKnownName(remove) + " no longer has permission to warp to this shrine.");
            }
        }
        // set <name>
        else if (args[0].equalsIgnoreCase("set")) {
            UUID newowner = DMisc.getDemigodsPlayerId(args[1]);
            if (newowner == null) {
                p.sendMessage(ChatColor.YELLOW + "Player not found.");
            } else if (newowner.toString().equals(shrine.getOwnerId())) {
                p.sendMessage(ChatColor.YELLOW + DMisc.getLastKnownName(newowner) + " is already the shrine's owner.");
            } else {
                p.sendMessage(ChatColor.YELLOW + DMisc.getLastKnownName(newowner) + " is the new owner of the shrine.");
                String deity = shrine.getDeity();
                String shrinename = shrine.getName();
                DMisc.removeShrine(shrine);
                DMisc.addShrine(newowner, deity, shrinename, shrine.toLocation(plugin));
            }
        } else return false;
        return true;
    }

    private boolean fixShrine(Player p) {
        ShrineSaveable shrine = DMisc.getNearbyShrine(p.getLocation());
        if (shrine == null) {
            p.sendMessage(ChatColor.YELLOW + "No shrine nearby.");
            return true;
        }
        // check if creator/admin
        if (!shrine.getOwnerId().equals(p.getUniqueId().toString()) && !p.hasPermission("demigods.admin")) {
            p.sendMessage(ChatColor.YELLOW + "Only admins and the creator of a shrine can modify it.");
            return true;
        }
        if (DMisc.toLocation(shrine).getBlock().getType() != Material.GOLD_BLOCK)
            DMisc.toLocation(shrine).getBlock().setType(Material.GOLD_BLOCK);
        p.sendMessage(ChatColor.YELLOW + "Shrine fixed.");
        return true;
    }

    private boolean listShrines(Player p) {
        if (!(p.hasPermission("demigods.listshrines") || p.hasPermission("demigods.admin")))
            return true;
        StringBuilder str = new StringBuilder();
        for (ShrineSaveable w : DMisc.getAllShrines()) {
            String toadd = w.getName();
            if (!str.toString().contains(toadd)) str.append(toadd).append(", ");
        }
        if (str.length() > 3) str = new StringBuilder(str.substring(0, str.length() - 2));
        if (str.length() > 0) p.sendMessage(str.toString());
        else p.sendMessage(ChatColor.YELLOW + "No shrines found.");
        return true;
    }

    private boolean removeShrine(Player p, String[] args) {
        if (!(p.hasPermission("demigods.removeshrine") || p.hasPermission("demigods.admin")))
            return true;
        if ((args.length == 1) && p.hasPermission("demigods.admin") && args[0].equals("all")) {
            for (ShrineSaveable w : DMisc.getAllShrines()) {
                p.sendMessage("Deleting " + w.getName());
                DMisc.toLocation(w).getBlock().setType(Material.AIR);
                DMisc.removeShrine(w);
            }
            return true;
        }
        if (args.length != 0) return false;
        // find nearby shrine
        ShrineSaveable shrine = DMisc.getNearbyShrine(p.getLocation());
        if (shrine == null) {
            p.sendMessage(ChatColor.YELLOW + "No shrine nearby.");
            return true;
        }
        // check if creator/admin
        if (!shrine.getOwnerId().equals(p.getUniqueId().toString()) && !p.hasPermission("demigods.admin")) {
            p.sendMessage(ChatColor.YELLOW + "Only admins and the creator of a shrine can modify it.");
            return true;
        }
        // remove
        String deity = shrine.getDeity();
        DMisc.toLocation(shrine).getBlock().setType(Material.AIR);
        p.sendMessage(ChatColor.YELLOW + "The shrine " + shrine.getName() + " has been removed.");
        DMisc.removeShrine(shrine);
        if (!p.hasPermission("demigods.admin")) {
            // penalty
            DMisc.setDevotion(p, deity, (int) (DMisc.getDevotion(p, deity) * 0.75));
            p.sendMessage(ChatColor.RED + "Your Devotion for " + deity + " has been reduced to " + DMisc.getDevotion(p, deity) + ".");
        }
        return true;
    }

    private boolean nameShrine(Player p, String[] args) {
        if (!(p.hasPermission("demigods.nameshrine") || p.hasPermission("demigods.admin")))
            return true;
        if (args.length != 1) return false;
        // find nearby shrine
        ShrineSaveable shrine = DMisc.getNearbyShrine(p.getLocation());
        if (shrine == null) {
            p.sendMessage(ChatColor.YELLOW + "No shrine nearby.");
            return true;
        }
        // check if creator/admin
        if (!shrine.getOwnerId().equals(p.getUniqueId().toString()) && !p.hasPermission("demigods.admin")) {
            p.sendMessage(ChatColor.YELLOW + "Only admins and the creator of a shrine can modify it.");
            return true;
        }
        // remove
        if (DMisc.renameShrine(shrine.toLocation(plugin), args[0]))
            p.sendMessage(ChatColor.YELLOW + "The shrine has been renamed to " + args[0] + ".");
        else p.sendMessage(ChatColor.YELLOW + "Error. Is there already a shrine named " + args[1] + "?");
        return true;
    }

    private boolean giveDeity(Player p, String[] args) {
        if (!(p.hasPermission("demigods.givedeity") || p.hasPermission("demigods.admin")))
            return true;
        if (args.length < 2) return false;
        UUID target = DMisc.getDemigodsPlayerId(args[0]);
        if (DMisc.hasDeity(target, args[1])) {
            p.sendMessage(ChatColor.YELLOW + "" + target + " already has that deity.");
            return true;
        } else {
            String s = args[1].toLowerCase();
            String targetName = DMisc.getLastKnownName(target);
            String success = ChatColor.YELLOW + "Success! " + targetName + " now has the deity " + args[1] + ".";
            switch (s) {
                case "thor":
                    DMisc.giveDeity(target, Deities.THOR);
                    break;
                case "vidar":
                    DMisc.giveDeity(target, Deities.VIDAR);
                    break;
                case "odin":
                    DMisc.giveDeity(target, Deities.ODIN);
                    break;
                case "fire":
                case "firegiant":
                    DMisc.giveDeity(target, Deities.FIRE_GIANT);
                    success = ChatColor.YELLOW + "Success! " + targetName + " now has divine fire.";
                    break;
                case "jord":
                    DMisc.giveDeity(target, Deities.JORD);
                    break;
                case "hel":
                    DMisc.giveDeity(target, Deities.HEL);
                    break;
                case "jormungand":
                    DMisc.giveDeity(target, Deities.JORMUNGAND);
                    break;
                case "thrymr":
                    DMisc.giveDeity(target, Deities.THRYMR);
                    break;
                case "heimdallr":
                    DMisc.giveDeity(target, Deities.HEIMDALLR);
                    break;
                case "frost":
                case "frostgiant":
                    DMisc.giveDeity(target, Deities.FROST_GIANT);
                    success = ChatColor.YELLOW + "Success! " + targetName + " now has divine frost.";
                    break;
                case "baldr":
                    DMisc.giveDeity(target, Deities.BALDR);
                    break;
                case "dwarf":
                    DMisc.giveDeity(target, Deities.DWARF);
                    success = ChatColor.YELLOW + "Success! " + targetName + " now has dwarven powers.";
                    break;
                case "bragi":
                    DMisc.giveDeity(target, Deities.BRAGI);
                    break;
                case "dís":
                case "dis":
                    DMisc.giveDeity(target, Deities.DIS);
                    success = ChatColor.YELLOW + "Success! " + targetName + " has joined the dísir.";
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
        if (!(p.hasPermission("demigods.removedeity") || p.hasPermission("demigods.admin")))
            return true;
        if (args.length != 2) return false;
        UUID target = DMisc.getDemigodsPlayerId(args[0]);
        if (!DMisc.hasDeity(target, args[1])) {
            p.sendMessage(ChatColor.YELLOW + "" + target + " does not have that deity.");
        } else {
            PlayerDataSaveable save = plugin.getPlayerDataRegistry().fromPlayer(p);
            save.removeDeity(args[1]);
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
        if (!DMisc.hasDeity(p, deity)) {
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
        if (amount > DMisc.getUnclaimedDevotion(p)) {
            p.sendMessage(ChatColor.YELLOW + "You do not enough unclaimed Devotion.");
            return true;
        } else if (amount < 1) {
            p.sendMessage(ChatColor.YELLOW + "Why would you want to do that?");
            return true;
        }
        Deity d = Deities.valueOf(deity);
        DMisc.setUnclaimedDevotion(p, DMisc.getUnclaimedDevotion(p) - amount);
        DMisc.setDevotion(p, d, DMisc.getDevotion(p, d) + amount);
        p.sendMessage(ChatColor.YELLOW + "Your Devotion for " + d.getName() + " has increased to " + DMisc.getDevotion(p, d) + ".");
        DLevels.levelProcedure(p);
        p.sendMessage("You have " + DMisc.getUnclaimedDevotion(p) + " unclaimed Devotion remaining.");
        return true;
    }

    private boolean forsake(Player p, String[] args) {
        if (!(p.hasPermission("demigods.forsake") || p.hasPermission("demigods.admin")))
            return true;
        if (!DMisc.isFullParticipant(p)) return true;
        if (args.length == 1 || args.length == 2) {
            if (args[0].equalsIgnoreCase("all")) {
                DMisc.getPlugin().getServer().broadcastMessage(ChatColor.RED + p.getName() + " has forsaken their deities.");
                p.kickPlayer(ChatColor.RED + "You are mortal.");
                DMisc.getShrines(p.getUniqueId()).forEach(DMisc::removeShrine);
                return true;
            }
            String deityName = args[0];
            if (args.length == 2) {
                deityName = args[0] + " " + args[1];
            }
            if (!DMisc.hasDeity(p, deityName)) {
                p.sendMessage(ChatColor.YELLOW + "You do not have that deity.");
            } else {
                if (DMisc.getDeities(p).size() >= 2) {
                    String str = "";
                    Deity toremove = Deities.valueOf(deityName);
                    DLevels.levelProcedure(p);
                    p.sendMessage(ChatColor.YELLOW + "You have forsaken " + toremove.getName() + "." + str);
                    DMisc.getPlugin().getServer().broadcastMessage(ChatColor.RED + p.getName() + " has forsaken " + toremove.getName() + ".");
                    PlayerDataSaveable save = plugin.getPlayerDataRegistry().fromPlayer(p);
                    save.removeDeity(toremove.getName());
                } else {
                    Deity toremove = Deities.valueOf(deityName);
                    p.sendMessage(ChatColor.YELLOW + "You have forsaken " + toremove.getName() + ".");
                    p.kickPlayer(ChatColor.YELLOW + "You have forsaken " + toremove.getName() + ChatColor.WHITE + " -- " + ChatColor.RED + "You are mortal.");
                    DMisc.getPlugin().getServer().broadcastMessage(ChatColor.RED + p.getName() + " has forsaken " + toremove.getName() + ".");
                    plugin.getPlayerDataRegistry().remove(p.getUniqueId().toString());
                    plugin.getPlayerDataRegistry().fromPlayer(p);
                }
            }
            return true;
        }
        return false;
    }

    private boolean setFavor(Player p, String[] args) {
        if (!(p.hasPermission("demigods.setfavor") || p.hasPermission("demigods.admin")))
            return true;
        if (args.length != 2) return false;
        try {
            UUID target = DMisc.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            if (amt < 0) {
                p.sendMessage(ChatColor.YELLOW + "The amount must be greater than 0.");
                return true;
            }
            DMisc.setFavor(target, amt);
            p.sendMessage(ChatColor.YELLOW + "Success! " + target + " now has " + amt + " Favor.");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean setMaxFavor(Player p, String[] args) {
        if (!(p.hasPermission("demigods.setfavor") || p.hasPermission("demigods.admin")))
            return true;
        if (args.length != 2) return false;
        try {
            UUID target = DMisc.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            if (amt < 0) {
                p.sendMessage(ChatColor.YELLOW + "The amount must be greater than 0.");
                return true;
            }
            DMisc.setFavorCap(target, amt);
            p.sendMessage(ChatColor.YELLOW + "Success! " + target + " now has " + amt + " max Favor.");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean setHP(Player p, String[] args) {
        if (!(p.hasPermission("demigods.sethp") || p.hasPermission("demigods.admin")))
            return true;
        if (args.length != 2) return false;
        try {
            Player ptarget = Bukkit.getPlayer(args[0]);
            if (ptarget != null) {
                int amt = Integer.parseInt(args[1]);
                if (amt < 0) {
                    p.sendMessage(ChatColor.YELLOW + "The amount must be greater than 0.");
                    return true;
                }
                if (amt > ptarget.getMaxHealth()) DMisc.setMaxHP(ptarget, amt);
                DMisc.setHP(ptarget, amt);
                p.sendMessage(ChatColor.YELLOW + "Success! " + args[0] + " now has " + amt + " HP.");
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private boolean setMaxHP(Player p, String[] args) {
        if (!(p.hasPermission("demigods.setmaxhp") || p.hasPermission("demigods.admin")))
            return true;
        if (args.length != 2) return false;
        try {
            Player ptarget = Bukkit.getPlayer(args[0]);
            if (ptarget != null) {
                int amt = Integer.parseInt(args[1]);
                if (amt < 0) {
                    p.sendMessage(ChatColor.YELLOW + "The amount must be greater than 0.");
                    return true;
                }
                DMisc.setMaxHP(ptarget, amt);
                p.sendMessage(ChatColor.YELLOW + "Success! " + args[0] + " now has " + amt + " max HP.");
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean setDevotion(Player p, String[] args) {
        if (!(p.hasPermission("demigods.setdevotion") || p.hasPermission("demigods.admin")))
            return true;
        if (args.length != 3) return false;
        UUID target = DMisc.getDemigodsPlayerId(args[0]);
        if (!DMisc.isFullParticipant(target)) {
            p.sendMessage("That player is a mortal.");
            return true;
        }
        int amt = Integer.parseInt(args[2]);
        if (amt < 0) {
            p.sendMessage(ChatColor.YELLOW + "The amount must be greater than 0.");
            return true;
        }
        if (DMisc.hasDeity(target, args[1])) {
            DMisc.setDevotion(target, Deities.valueOf(args[1]), amt);
            p.sendMessage(ChatColor.YELLOW + "Success! " + target + " now has " + amt + " Devotion for " + args[1].toUpperCase() + ".");
            return true;
        }
        return false;
    }

    private boolean setAscensions(Player p, String[] args) {
        if (!(p.hasPermission("demigods.setascensions") || p.hasPermission("demigods.admin")))
            return true;
        if (args.length != 2) return false;
        try {
            UUID target = DMisc.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            if (amt < 0) {
                p.sendMessage(ChatColor.YELLOW + "The number must be greater than 0.");
                return true;
            }
            DMisc.setAscensions(target, amt);
            long oldtotal = DMisc.getDevotion(target);
            int newtotal = DMisc.costForNextAscension(amt - 1);
            for (Deity d : DMisc.getDeities(target)) {
                int devotion = DMisc.getDevotion(target, d);
                DMisc.setDevotion(target, d, (int) Math.ceil((newtotal * 1.0 * devotion) / oldtotal));
            }
            p.sendMessage(ChatColor.YELLOW + "Success! " + target + " now has " + amt + " Ascensions.");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean setKills(Player p, String[] args) {
        if (!(p.hasPermission("demigods.setkills") || p.hasPermission("demigods.admin")))
            return true;
        if (args.length != 2) return false;
        try {
            UUID target = DMisc.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            if (amt < 0) {
                p.sendMessage(ChatColor.YELLOW + "The amount must be greater than 0.");
                return true;
            }
            DMisc.setKills(target, amt);
            p.sendMessage(ChatColor.YELLOW + "Success! " + target + " now has " + amt + " kills.");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean setDeaths(Player p, String[] args) {
        if (!(p.hasPermission("demigods.setdeaths") || p.hasPermission("demigods.admin")))
            return true;
        if (args.length != 2) return false;
        try {
            UUID target = DMisc.getDemigodsPlayerId(args[0]);
            int amt = Integer.parseInt(args[1]);
            if (amt < 0) {
                p.sendMessage(ChatColor.YELLOW + "The amount must be greater than 0.");
                return true;
            }
            DMisc.setDeaths(target, amt);
            p.sendMessage(ChatColor.YELLOW + "Success! " + target + " now has " + amt + " deaths.");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean setAlliance(Player p, String[] args) {
        if (!(p.hasPermission("demigods.setalliance") || p.hasPermission("demigods.admin")))
            return true;
        if (args.length != 2) return false;
        try {
            UUID target = DMisc.getDemigodsPlayerId(args[0]);
            String allegiance = args[1];
            if (allegiance.equalsIgnoreCase("aesir")) DMisc.setAEsir(target);
            else if (allegiance.equalsIgnoreCase("jotunn")) DMisc.setJotunn(target);
            else DMisc.setAllegiance(target, allegiance);
            p.sendMessage(ChatColor.YELLOW + "Success! " + target + " is now in the " + DMisc.getAllegiance(target) + " allegiance.");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean removePlayer(Player p, String[] args) {
        if (!(p.hasPermission("demigods.removeplayer") || p.hasPermission("demigods.admin")))
            return true;
        if (args.length != 1) return false;
        UUID id = DMisc.getDemigodsPlayerId(args[0]);
        plugin.getPlayerDataRegistry().remove(id.toString());
        p.sendMessage(ChatColor.YELLOW + args[0] + " was successfully removed from the save.");
        Player toremove = plugin.getServer().getPlayer(args[0]);
        if (toremove != null) {
            toremove.kickPlayer("Save removed. Please log in again.");
        } else p.sendMessage(ChatColor.YELLOW + "That player is not in the save.");
        return true;
    }

    @SuppressWarnings("incomplete-switch")
    private boolean claim(Player p) {
        /*
         * Check for a new player first
         */
        if ((DMisc.getDeities(p) == null) || (DMisc.getDeities(p).size() == 0)) {
            Deity choice = null;
            switch (p.getItemInHand().getType()) {
                case SOUL_SAND:
                    choice = Deities.ODIN;
                    break;
                case IRON_INGOT:
                    choice = Deities.THOR;
                    break;
                case ROSE_BUSH:
                case SUNFLOWER:
                case CORNFLOWER:
                case CHORUS_FLOWER:
                    choice = Deities.BALDR;
                    break;
                //
                case FLINT_AND_STEEL:
                    choice = Deities.FIRE_GIANT;
                    break;
                case WATER_BUCKET:
                    choice = Deities.JORMUNGAND;
                    break;
                case BONE:
                    choice = Deities.HEL;
                    break;
            }
            if (choice != null) {
                if (!p.hasPermission(choice.getDefaultAlliance().toLowerCase() + "." + choice.getName().toLowerCase().replace(" ", "_")) && (!p.hasPermission(choice.getDefaultAlliance().toLowerCase() + ".all"))) {
                    p.sendMessage(ChatColor.RED + "You do not have permission to claim this deity.");
                    return true;
                }
                p.sendMessage(ChatColor.YELLOW + "The great powers ponder your decision...");
                if (Setting.BALANCE_TEAMS && DMisc.hasAdvantage(choice.getDefaultAlliance())) {
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
                DMisc.initializePlayer(p, choice.getDefaultAlliance(), choice);
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
        if (!DMisc.isFullParticipant(p)) return true;
        if (DMisc.getAscensions(p) < DMisc.costForNextDeity(p)) {
            p.sendMessage(ChatColor.YELLOW + "You must have " + DMisc.costForNextDeity(p) + " Ascensions to claim another deity.");
            return true;
        }
        Deity choice = null;
        switch (p.getItemInHand().getType()) {
            case IRON_INGOT:
                choice = Deities.THOR;
                break;
            case GOLDEN_SWORD:
                choice = Deities.VIDAR;
                break;
            case SOUL_SAND:
                choice = Deities.ODIN;
                break;
            case BOOK:
                choice = Deities.HEIMDALLR;
                break;
            case FURNACE:
                choice = Deities.DWARF;
                break;
            case JUKEBOX:
                choice = Deities.BRAGI;
                break;
            case ROSE_BUSH:
            case SUNFLOWER:
            case CORNFLOWER:
            case CHORUS_FLOWER:
                choice = Deities.BALDR;
                break;
            //
            case WATER_BUCKET:
                choice = Deities.JORMUNGAND;
                break;
            case BONE:
                choice = Deities.HEL;
                break;
            case FLINT_AND_STEEL:
                choice = Deities.FIRE_GIANT;
                break;
            case VINE:
                choice = Deities.JORD;
                break;
            case OBSIDIAN:
                choice = Deities.THRYMR;
                break;
            case GLASS_BOTTLE:
                choice = Deities.FROST_GIANT;
                break;
            case COMPASS:
                choice = Deities.DIS;
                break;
        }
        if (choice == null) {
            p.sendMessage(ChatColor.YELLOW + "That is not a valid selection item.");
            return true;
        }
        if (!p.hasPermission(choice.getDefaultAlliance().toLowerCase() + "." + choice.getName().toLowerCase().replace(" ", "_")) && (!p.hasPermission(choice.getDefaultAlliance().toLowerCase() + ".all"))) {
            p.sendMessage(ChatColor.RED + "You do not have permission to claim this deity.");
            return true;
        }
        if (!choice.getDefaultAlliance().equalsIgnoreCase(DMisc.getAllegiance(p))) {
            if (p.hasPermission("demigods.bypassclaim")) {
                p.sendMessage(ChatColor.YELLOW + choice.getName() + " has offered you power in exchange for loyalty.");
            } else {
                p.sendMessage(ChatColor.RED + "That deity is not of your alliance.");
                return true;
            }
        }
        if (DMisc.hasDeity(p, choice.getName())) {
            p.sendMessage(ChatColor.RED + "You are already allied with " + choice.getName() + ".");
            return true;
        }
        DMisc.giveDeity(p, choice);
        DMisc.setFavorCap(p, DMisc.getFavorCap(p) + 100);
        DMisc.setFavor(p, DMisc.getFavor(p) + 100);
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
        if (DMisc.isFullParticipant(p)) if (p.getItemInHand() != null)
            p.sendMessage(ChatColor.YELLOW + p.getItemInHand().getType().name() + " x" + p.getItemInHand().getAmount() + " is worth " + (DMisc.getValue(p.getItemInHand()) * Setting.FAVOR_MULTIPLIER) + " at a shrine.");
        return true;
    }

    private boolean bindings(Player p) {
        if (!DMisc.isFullParticipant(p)) return true;
        if (!(p.hasPermission("demigods.bindings") || p.hasPermission("demigods.admin")))
            return true;
        List<Material> items = DMisc.getBindings(p);
        if ((items != null) && (items.size() > 0)) {
            StringBuilder disp = new StringBuilder(ChatColor.YELLOW + "Bound items:");
            for (Material m : items)
                disp.append(" ").append(m.name().toLowerCase());
            p.sendMessage(disp.toString());
        } else p.sendMessage(ChatColor.YELLOW + "You have no bindings.");
        return true;
    }

    private boolean assemble(Player p) {
        if (!DMisc.isFullParticipant(p)) return true;
        if (!DMisc.getActiveEffectsList(p.getUniqueId()).contains("Congregate")) return true;
        for (Player pl : p.getWorld().getPlayers()) {
            if (DMisc.isFullParticipant(pl) && DMisc.getActiveEffectsList(pl.getUniqueId()).contains("Congregate Call")) {
                DMisc.removeActiveEffect(p.getUniqueId(), "Congregate");
                DMisc.addActiveEffect(p.getUniqueId(), "Ceasefire", 60);
                DMisc.horseTeleport(p, pl.getLocation());
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
                cm.sendMessage("Name: " + DMisc.getLastKnownName(p));
            } catch (NullPointerException ne) {
                cm.sendMessage(ChatColor.RED + "Name is missing/null.");
            }
            try {
                cm.sendMessage("Alliance: " + DMisc.getAllegiance(p));
            } catch (NullPointerException ne) {
                cm.sendMessage(ChatColor.RED + "Alliance is missing/null.");
            }
            try {
                cm.sendMessage("Current HP: " + Bukkit.getPlayer(p).getHealth());
            } catch (Exception ignored) {
            }
            try {
                cm.sendMessage("Max HP: " + Bukkit.getPlayer(p).getMaxHealth());
            } catch (Exception ignored) {
            }
            try {
                cm.sendMessage("Current Favor: " + DMisc.getFavor(p));
            } catch (NullPointerException ne) {
                cm.sendMessage(ChatColor.RED + "Favor is missing/null.");
            }
            try {
                cm.sendMessage("Max Favor: " + DMisc.getFavorCap(p));
            } catch (NullPointerException ne) {
                cm.sendMessage(ChatColor.RED + "Max Favor is missing/null.");
            }
            try {
                StringBuilder s = new StringBuilder();
                for (Deity d : DMisc.getDeities(p)) {
                    String name = d.getName();
                    try {
                        s.append(" ").append(name).append(";").append(DMisc.getDevotion(p, name));
                    } catch (Exception e) {
                        cm.sendMessage(ChatColor.RED + "Error loading " + name + ".");
                    }
                }
                cm.sendMessage("Deities:" + s);
            } catch (NullPointerException ne) {
                cm.sendMessage(ChatColor.RED + "Deities are missing/null.");
            }
            try {
                cm.sendMessage("Ascensions: " + DMisc.getAscensions(p));
            } catch (NullPointerException ne) {
                cm.sendMessage(ChatColor.RED + "Ascensions are missing/null.");
            }
            try {
                cm.sendMessage("Kills: " + DMisc.getKills(p));
            } catch (NullPointerException ne) {
                cm.sendMessage(ChatColor.RED + "Kills are missing/null.");
            }
            try {
                cm.sendMessage("Deaths: " + DMisc.getDeaths(p));
            } catch (NullPointerException ne) {
                cm.sendMessage(ChatColor.RED + "Deaths are missing/null.");
            }
            try {
                cm.sendMessage("Accessible:");
                for (ShrineSaveable w : DMisc.getAccessibleShrines(p)) {
                    String name = w.getName();
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
                for (ShrineSaveable w : DMisc.getShrines(p)) {
                    try {
                        StringBuilder names = new StringBuilder();
                        for (String playerId : w.getGuestIds())
                            names.append(playerId).append(" ");
                        cm.sendMessage(w.getName() + " " + w.getX() + " " + w.getY() + " " + w.getZ() + " " + w.getWorld() + " " + names.toString().trim());
                    } catch (Exception e) {
                        cm.sendMessage(ChatColor.RED + "Error loading shrine \"" + w.getName() + "\".");
                    }
                }
            } catch (NullPointerException ne) {
                cm.sendMessage(ChatColor.RED + "Shrines are missing/null.");
            }
        }

        /**
         * Used to print a player's data to console.
         *
         * @param cm
         * @param p
         */
        public static void printData(Logger cm, UUID p) {
            try {
                cm.info("Name: " + DMisc.getLastKnownName(p));
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "Name is missing/null.");
            }
            try {
                cm.info("Alliance: " + DMisc.getAllegiance(p));
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "Alliance is missing/null.");
            }
            try {
                cm.info("Current HP: " + Bukkit.getPlayer(p).getHealth());
            } catch (Exception ignored) {
            }
            try {
                cm.info("Max HP: " + Bukkit.getPlayer(p).getMaxHealth());
            } catch (Exception ignored) {
            }
            try {
                cm.info("Current Favor: " + DMisc.getFavor(p));
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "Favor is missing/null.");
            }
            try {
                cm.info("Max Favor: " + DMisc.getFavorCap(p));
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "Max Favor is missing/null.");
            }
            try {
                StringBuilder s = new StringBuilder();
                for (Deity d : DMisc.getDeities(p)) {
                    String name = d.getName();
                    try {
                        s.append(" ").append(name).append(";").append(DMisc.getDevotion(p, name));
                    } catch (Exception e) {
                        cm.warning(ChatColor.RED + "Error loading " + name + ".");
                    }
                }
                cm.info("Deities:" + s);
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "Deities are missing/null.");
            }
            try {
                cm.info("Ascensions: " + DMisc.getAscensions(p));
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "Ascensions are missing/null.");
            }
            try {
                cm.info("Kills: " + DMisc.getKills(p));
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "Kills are missing/null.");
            }
            try {
                cm.info("Deaths: " + DMisc.getDeaths(p));
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "Deaths are missing/null.");
            }
            try {
                cm.info("Accessible:");
                for (ShrineSaveable w : DMisc.getAccessibleShrines(p)) {
                    String name = w.getName();
                    try {
                        cm.info(name + " " + w.getX() + " " + w.getY() + " " + w.getZ() + " " + w.getWorld());
                    } catch (Exception e) {
                        cm.warning(ChatColor.RED + "Error loading " + name + ".");
                    }
                }
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "Accessible shrines list is missing/null.");
            }
            // Bindings will be cleared
            // Effects will be cleared
            try {
                cm.info("Shrines:");
                for (ShrineSaveable w : DMisc.getShrines(p)) {
                    try {
                        StringBuilder names = new StringBuilder();
                        for (String playerId : w.getGuestIds())
                            names.append(playerId).append(" ");
                        cm.info(w.getName() + " " + w.getX() + " " + w.getY() + " " + w.getZ() + " " + w.getWorld() + " " + names.toString().trim());
                    } catch (Exception e) {
                        cm.warning(ChatColor.RED + "Error loading shrine \"" + w.getName() + "\".");
                    }
                }
            } catch (NullPointerException ne) {
                cm.warning(ChatColor.RED + "Shrines are missing/null.");
            }
        }
    }
}
