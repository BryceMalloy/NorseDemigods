package com.demigodsrpg.norsedemigods.deity.jotunn;

import com.demigodsrpg.norsedemigods.DMisc;
import com.demigodsrpg.norsedemigods.Deity;
import com.demigodsrpg.norsedemigods.deity.AD;
import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
import org.bukkit.Bukkit;
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

    @Override
    public String getName() {
        return "Thrymr";
    }

    @Override
    public String getDefaultAlliance() {
        return "Jotunn";
    }

    @Override
    public void printInfo(Player p) {
        if (DMisc.hasDeity(p, "Thrymr") && DMisc.isFullParticipant(p)) {
            int devotion = DMisc.getDevotion(p, getName());
            /*
             * Calculate special values first
             */
            int reduction = (int) Math.round(Math.pow(devotion, 0.115));
            //
            int jump = (int) Math.ceil(0.85 * Math.pow(devotion, 0.08));
            int length = (int) Math.ceil(4 * Math.pow(devotion, 0.2475));
            //
            int duration = (int) (Math.ceil(35.819821 * Math.pow(DMisc.getAscensions(p), 0.26798863))); // seconds
            int radius = (int) (Math.ceil(4.957781 * Math.pow(DMisc.getAscensions(p), 0.45901927)));
            int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) * ((double) DMisc.getAscensions(p) / 100)));
            /*
             * The printed text
             */
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            p.sendMessage("--" + ChatColor.GOLD + "Thrymr" + ChatColor.GRAY + "[" + devotion + "]");
            p.sendMessage(":Reduce incoming combat damage by " + reduction + ".");
            p.sendMessage(":Temporarily increase jump height.");
            p.sendMessage("Duration: " + length + " Jump multiplier: " + jump + ChatColor.GREEN + " /unburden " + ChatColor.YELLOW + "Costs " + SKILLCOST + " Favor.");
            boolean unburden = save.getAbilityData("unburden", AD.ACTIVE, false);
            if (unburden)
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
        if (!DMisc.isFullParticipant(p)) return;
        if (!DMisc.hasDeity(p, "Thrymr")) return;
        PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
        if (str.equalsIgnoreCase("unburden")) {
            if (DMisc.getActiveEffects(p.getUniqueId()).containsKey("Unburden")) {
                save.setAbilityData("unburden", AD.ACTIVE, false);
                p.sendMessage(ChatColor.YELLOW + "Unburden is already active.");
            } else {
                if (DMisc.getFavor(p) < SKILLCOST) {
                    p.sendMessage(ChatColor.YELLOW + "Unburden costs " + SKILLCOST + " Favor.");
                    return;
                }
                int devotion = DMisc.getDevotion(p, getName());
                int jump = (int) Math.ceil(0.85 * Math.pow(devotion, 0.28));
                int length = (int) Math.ceil(4 * Math.pow(devotion, 0.2475));
                p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, length * 20, jump));
                DMisc.addActiveEffect(p.getUniqueId(), "Unburden", length);
                save.setAbilityData("unburden", AD.ACTIVE, true);
                DMisc.getPlugin().getServer().getScheduler().scheduleAsyncDelayedTask(DMisc.getPlugin(), () -> {
                    PlayerDataSaveable save1 = getBackend().getPlayerDataRegistry().fromPlayer(p);
                    save1.setAbilityData("unburden", AD.ACTIVE, false);
                }, length * 20);
                DMisc.setFavor(p, DMisc.getFavor(p) - SKILLCOST);
                p.sendMessage(ChatColor.YELLOW + "Unburden is now active.");
                p.sendMessage(ChatColor.YELLOW + "You will jump higher for " + length + " seconds.");
            }
        } else if (str.equalsIgnoreCase("invincible")) {
            double time = save.getAbilityData("invincible", AD.TIME, (double) System.currentTimeMillis());
            if (System.currentTimeMillis() < time) {
                p.sendMessage(ChatColor.YELLOW + "You cannot use Invincible again for " + ((((time) / 1000) - (System.currentTimeMillis() / 1000))) / 60 + " minutes");
                p.sendMessage(ChatColor.YELLOW + "and " + ((((time) / 1000) - (System.currentTimeMillis() / 1000)) % 60) + " seconds.");
                return;
            }
            if (DMisc.getFavor(p) >= ULTIMATECOST) {
                int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) * ((double) DMisc.getAscensions(p) / 100)));
                //
                final int seconds = (int) (Math.ceil(35.819821 * Math.pow(DMisc.getAscensions(p), 0.26798863)));
                int INVINCIBLERANGE = (int) (Math.ceil(4.957781 * Math.pow(DMisc.getAscensions(p), 0.45901927)));
                for (String id : DMisc.getFullParticipants()) {
                    final Player pl = Bukkit.getPlayer(UUID.fromString(id));
                    if ((pl != null) && !pl.isDead() && (pl.getLocation().toVector().isInSphere(p.getLocation().toVector(), INVINCIBLERANGE))) {
                        pl.sendMessage(ChatColor.DARK_AQUA + "Thrymr" + ChatColor.GRAY + " shields you and your allies from harm.");
                        DMisc.addActiveEffect(pl.getUniqueId(), "Invincible", seconds);
                        DMisc.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMisc.getPlugin(), () ->
                                pl.sendMessage(ChatColor.YELLOW + "Invincible will be in effect for " + seconds / 2 +
                                        " more seconds."), seconds * 10);
                        DMisc.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMisc.getPlugin(), () ->
                                pl.sendMessage(ChatColor.YELLOW + "Invincible is no longer in effect."), seconds * 20);
                    }
                }
                //
                DMisc.setFavor(p, DMisc.getFavor(p) - ULTIMATECOST);
                save.setAbilityData("invincible", AD.TIME, System.currentTimeMillis() + t * 1000);
            } else p.sendMessage(ChatColor.YELLOW + "Invincible requires " + ULTIMATECOST + " Favor.");
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
