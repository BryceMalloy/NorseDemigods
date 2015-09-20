package com.demigodsrpg.norsedemigods.deity.jotunn;

import com.demigodsrpg.norsedemigods.deity.Deity;
import com.demigodsrpg.norsedemigods.util.DMiscUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class Thrymr implements Deity {

    /* General */
    private static final long serialVersionUID = 1898032566168889851L;
    private final int SKILLCOST = 95;
    private final int ULTIMATECOST = 6000;
    private final int ULTIMATECOOLDOWNMAX = 400;
    private final int ULTIMATECOOLDOWNMIN = 300;

    /* Specific to player */
    private final UUID PLAYER;
    private boolean SKILL = false;
    private long ULTIMATETIME;

    public Thrymr(UUID name) {
        PLAYER = name;
        ULTIMATETIME = System.currentTimeMillis();
    }

    @Override
    public String getName() {
        return "Thrymr";
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
        if (DMiscUtil.hasDeity(p, "Thrymr") && DMiscUtil.isFullParticipant(p)) {
            int devotion = DMiscUtil.getDevotion(p, getName());
            /*
             * Calculate special values first
			 */
            int reduction = (int) Math.round(Math.pow(devotion, 0.115));
            //
            int jump = (int) Math.ceil(0.85 * Math.pow(devotion, 0.08));
            int length = (int) Math.ceil(4 * Math.pow(devotion, 0.2475));
            //
            int duration = (int) (Math.ceil(35.819821 * Math.pow(DMiscUtil.getAscensions(p), 0.26798863))); // seconds
            int radius = (int) (Math.ceil(4.957781 * Math.pow(DMiscUtil.getAscensions(p), 0.45901927)));
            int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) * ((double) DMiscUtil.getAscensions(p) / 100)));
            /*
			 * The printed text
			 */
            p.sendMessage("--" + ChatColor.GOLD + "Thrymr" + ChatColor.GRAY + "[" + devotion + "]");
            p.sendMessage(":Reduce incoming combat damage by " + reduction + ".");
            p.sendMessage(":Temporarily increase jump height.");
            p.sendMessage("Duration: " + length + " Jump multiplier: " + jump + ChatColor.GREEN + " /unburden " + ChatColor.YELLOW + "Costs " + SKILLCOST + " Favor.");
            if (((Thrymr) DMiscUtil.getDeity(p, "Thrymr")).SKILL)
                p.sendMessage(ChatColor.AQUA + "    Skill is active.");
            p.sendMessage(":Thrymr shields you and nearby allies from harm.");
            p.sendMessage("50% damage reduction with range " + radius + " for " + duration + " seconds.");
            p.sendMessage(ChatColor.GREEN + " /invincible" + ChatColor.YELLOW + " Costs " + ULTIMATECOST + " Favor. Cooldown time: " + t + " seconds.");
            return;
        }
        p.sendMessage("--" + ChatColor.GOLD + "Thrymr");
        p.sendMessage("Passive: Reduce incoming combat damage.");
        p.sendMessage("Active: Release a great weight from your shoulders, increasing jump.");
        p.sendMessage(ChatColor.GREEN + "/unburden" + ChatColor.YELLOW + " Costs " + SKILLCOST + " Favor.");
        p.sendMessage("Ultimate: Thrymr shields you and nearby allies from harm.");
        p.sendMessage(ChatColor.GREEN + "/invincible" + ChatColor.YELLOW + " Costs " + ULTIMATECOST + " Favor. Has cooldown.");
        p.sendMessage(ChatColor.YELLOW + "Select item: obsidian");
    }

    @Override
    public void onEvent(Event ee) {
        // Nothing.
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCommand(final Player p, String str, String[] args, boolean bind) {
        if (!DMiscUtil.isFullParticipant(p)) return;
        if (!DMiscUtil.hasDeity(p, "Thrymr")) return;
        if (str.equalsIgnoreCase("unburden")) {
            if (DMiscUtil.getActiveEffects(p.getUniqueId()).containsKey("Unburden")) {
                SKILL = false;
                p.sendMessage(ChatColor.YELLOW + "Unburden is already active.");
            } else {
                if (DMiscUtil.getFavor(p) < SKILLCOST) {
                    p.sendMessage(ChatColor.YELLOW + "Unburden costs " + SKILLCOST + " Favor.");
                    return;
                }
                int devotion = DMiscUtil.getDevotion(p, getName());
                int jump = (int) Math.ceil(0.85 * Math.pow(devotion, 0.28));
                int length = (int) Math.ceil(4 * Math.pow(devotion, 0.2475));
                p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, length * 20, jump));
                DMiscUtil.addActiveEffect(p.getUniqueId(), "Unburden", length);
                SKILL = true;
                DMiscUtil.getPlugin().getServer().getScheduler().scheduleAsyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
                    @Override
                    public void run() {
                        SKILL = false;
                    }
                }, length * 20);
                DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) - SKILLCOST);
                p.sendMessage(ChatColor.YELLOW + "Unburden is now active.");
                p.sendMessage(ChatColor.YELLOW + "You will jump higher for " + length + " seconds.");
            }
        } else if (str.equalsIgnoreCase("invincible")) {
            long TIME = ULTIMATETIME;
            if (System.currentTimeMillis() < TIME) {
                p.sendMessage(ChatColor.YELLOW + "You cannot use Invincible again for " + ((((TIME) / 1000) - (System.currentTimeMillis() / 1000))) / 60 + " minutes");
                p.sendMessage(ChatColor.YELLOW + "and " + ((((TIME) / 1000) - (System.currentTimeMillis() / 1000)) % 60) + " seconds.");
                return;
            }
            if (DMiscUtil.getFavor(p) >= ULTIMATECOST) {
                int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) * ((double) DMiscUtil.getAscensions(p) / 100)));
                //
                final int seconds = (int) (Math.ceil(35.819821 * Math.pow(DMiscUtil.getAscensions(p), 0.26798863)));
                int INVINCIBLERANGE = (int) (Math.ceil(4.957781 * Math.pow(DMiscUtil.getAscensions(p), 0.45901927)));
                for (UUID s : DMiscUtil.getFullParticipants()) {
                    final Player pl = DMiscUtil.getOnlinePlayer(s);
                    if ((pl != null) && !pl.isDead() && (pl.getLocation().toVector().isInSphere(p.getLocation().toVector(), INVINCIBLERANGE))) {
                        pl.sendMessage(ChatColor.DARK_AQUA + "Thrymr" + ChatColor.GRAY + " shields you and your allies from harm.");
                        DMiscUtil.addActiveEffect(pl.getUniqueId(), "Invincible", seconds);
                        DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
                            @Override
                            public void run() {
                                pl.sendMessage(ChatColor.YELLOW + "Invincible will be in effect for " + seconds / 2 + " more seconds.");
                            }
                        }, seconds * 10);
                        DMiscUtil.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMiscUtil.getPlugin(), new Runnable() {
                            @Override
                            public void run() {
                                pl.sendMessage(ChatColor.YELLOW + "Invincible is no longer in effect.");
                            }
                        }, seconds * 20);
                    }
                }
                //
                DMiscUtil.setFavor(p, DMiscUtil.getFavor(p) - ULTIMATECOST);
                ULTIMATETIME = System.currentTimeMillis() + t * 1000;
            } else p.sendMessage(ChatColor.YELLOW + "Invincible requires " + ULTIMATECOST + " Favor.");
        }
    }

    @Override
    public void onTick(long timeSent) {

    }

    @Override
    public boolean canTribute() {
        return true;
    }
}
