package com.demigodsrpg.norsedemigods.deity.aesir;

import com.demigodsrpg.norsedemigods.DMisc;
import com.demigodsrpg.norsedemigods.Deity;
import com.demigodsrpg.norsedemigods.Setting;
import com.demigodsrpg.norsedemigods.deity.AD;
import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class Bragi implements Deity {
    private static final int SKILLCOST = 150;
    private static final int SKILLDELAY = 3000; // milliseconds
    private static final int ULTIMATECOST = 6300;
    private static final int ULTIMATECOOLDOWNMAX = 600; // seconds
    private static final int ULTIMATECOOLDOWNMIN = 120;

    private static final String skillname = "Cure";
    private static final String ult = "Finale";

    @Override
    public String getName() {
        return "Bragi";
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
            /*
             * Special values
			 */
            // passive
            int duration = (int) Math.round(60 * Math.pow(devotion, 0.09)); // seconds
            // active
            int healamt = (int) Math.round(5 * Math.pow(devotion, 0.09));
            // ult
            int ultrange = (int) Math.round(20 * Math.pow(devotion, 0.15));
            int ultslowduration = (int) Math.round(10 * Math.pow(devotion, 0.05)); // seconds
            int ultattacks = (int) Math.round(4 * Math.pow(devotion, 0.08)); // number of arrow "waves"
            int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) * ((double) DMisc.getAscensions(p) / Setting.ASCENSION_CAP)));
            // print
            p.sendMessage("--" + ChatColor.GOLD + getName() + ChatColor.GRAY + "[" + devotion + "]");
            p.sendMessage(":Play a music disc to receive a buff lasting " + duration + " seconds.");
            p.sendMessage(":Left-click to heal yourself for " + (healamt / 2) + " and a target");
            p.sendMessage("ally for " + healamt + " health." + ChatColor.GREEN + " /cure " + ChatColor.YELLOW + "Costs " + SKILLCOST + " Favor.");
            if (save.getBind(skillname).isPresent())
                p.sendMessage(ChatColor.AQUA + "    Bound to " + save.getBind(skillname).get().name());
            else p.sendMessage(ChatColor.AQUA + "    Use /bind to bind this skill to an item.");
            p.sendMessage("Slow enemies in range " + ultrange + " for " + ultslowduration + " seconds and strike");
            p.sendMessage("them with " + ultattacks + " waves of arrows." + ChatColor.GREEN + " /finale");
            p.sendMessage(ChatColor.YELLOW + "Costs " + ULTIMATECOST + " Favor. Cooldown time: " + t + " seconds.");
            return;
        }
        p.sendMessage("--" + getName());
        p.sendMessage("Passive: Play a music disc to receive special buffs from Bragi.");
        p.sendMessage("Active: Heal yourself and a target ally." + ChatColor.GREEN + " /cure");
        p.sendMessage(ChatColor.YELLOW + "Costs " + SKILLCOST + " Favor. Can bind.");
        p.sendMessage("Ultimate: Slow enemies and rain arrows on them." + ChatColor.GREEN + " /finale ");
        p.sendMessage(ChatColor.YELLOW + "Costs " + ULTIMATECOST + " Favor. Has cooldown.");
        p.sendMessage(ChatColor.YELLOW + "Select item: jukebox");
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public void onEvent(Event ee) {
        if (ee instanceof PlayerInteractEvent) {
            PlayerInteractEvent e = (PlayerInteractEvent) ee;
            Player p = e.getPlayer();
            if (!DMisc.isFullParticipant(p) || !DMisc.hasDeity(p, getName())) return;
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (e.getClickedBlock().getType() != Material.JUKEBOX) return;
                if (p.getItemInHand() != null) switch (p.getItemInHand().getType()) {
                    case GOLD_RECORD:
                        applyEffect(p, PotionEffectType.SPEED, "speed");
                        break;
                    case GREEN_RECORD:
                        applyEffect(p, PotionEffectType.FAST_DIGGING, "mining speed");
                        break;
                    case RECORD_3:
                        applyEffect(p, null, "health regeneration");
                        break;
                    case RECORD_4:
                        applyEffect(p, null, "Favor regeneration");
                        break;
                    case RECORD_5:
                        applyEffect(p, PotionEffectType.INCREASE_DAMAGE, "strength");
                        break;
                    case RECORD_6:
                        applyEffect(p, PotionEffectType.JUMP, "jump");
                        break;
                    case RECORD_7:
                        applyEffect(p, PotionEffectType.DAMAGE_RESISTANCE, "damage resistance");
                        break;
                    case RECORD_8:
                        applyEffect(p, PotionEffectType.FIRE_RESISTANCE, "fire resistance");
                        break;
                    case RECORD_9:
                        applyEffect(p, PotionEffectType.WATER_BREATHING, "water breathing");
                        break;
                    case RECORD_10:
                        applyEffect(p, PotionEffectType.NIGHT_VISION, "night vision");
                        break;
                    case RECORD_11:
                        applyEffect(p, PotionEffectType.INVISIBILITY, "invisibility");
                        break;
                }
            }
            if (save.getAbilityData(skillname, AD.ACTIVE, false) || ((p.getItemInHand() != null) &&
                    save.getBind(skillname).isPresent() && (p.getItemInHand().getType() == save.getBind(skillname).get()))) {
                if (save.getAbilityData(skillname, AD.TIME, (double) System.currentTimeMillis()) > System.currentTimeMillis())
                    return;
                save.setAbilityData(skillname, AD.TIME, System.currentTimeMillis() + SKILLDELAY);
                if (DMisc.getFavor(p) >= SKILLCOST) {
                    cure(p);
                    DMisc.setFavor(p, DMisc.getFavor(p) - SKILLCOST);
                } else {
                    p.sendMessage(ChatColor.YELLOW + "You do not have enough Favor.");
                    save.setAbilityData(skillname, AD.ACTIVE, false);
                }
            }
        }
    }

    @Override
    public void onCommand(final Player p, String str, String[] args, boolean bind) {
        if (DMisc.hasDeity(p, getName())) {
            PlayerDataSaveable save = getBackend().getPlayerDataRegistry().fromPlayer(p);
            if (str.equalsIgnoreCase(skillname)) {
                if (bind) {
                    if (!save.getBind(skillname).isPresent()) {
                        if (DMisc.isBound(p, p.getItemInHand().getType()))
                            p.sendMessage(ChatColor.YELLOW + "That item is already bound to a skill.");
                        if (p.getItemInHand().getType() == Material.AIR)
                            p.sendMessage(ChatColor.YELLOW + "You cannot bind a skill to air.");
                        else {
                            save.setBind(skillname, p.getItemInHand().getType());
                            p.sendMessage(ChatColor.YELLOW + "" + skillname + " is now bound to " + p.getItemInHand().getType().name() + ".");
                        }
                    } else {
                        p.sendMessage(ChatColor.YELLOW + "" + skillname + " is no longer bound to " + save.getBind(skillname).get().name() + ".");
                        save.removeBind(skillname);
                    }
                    return;
                }
                if (save.getAbilityData(skillname, AD.ACTIVE, false)) {
                    save.setAbilityData(skillname, AD.ACTIVE, false);
                    p.sendMessage(ChatColor.YELLOW + "" + skillname + " is no longer active.");
                } else {
                    save.setAbilityData(skillname, AD.ACTIVE, true);
                    p.sendMessage(ChatColor.YELLOW + "" + skillname + " is now active.");
                }
            } else if (str.equalsIgnoreCase(ult)) {
                double TIME = save.getAbilityData(ult, AD.TIME, (double) System.currentTimeMillis());
                if (System.currentTimeMillis() < TIME) {
                    p.sendMessage(ChatColor.YELLOW + "You cannot use " + ult + " again for " + ((((TIME) / 1000) - (System.currentTimeMillis() / 1000))) / 60 + " minutes");
                    p.sendMessage(ChatColor.YELLOW + "and " + ((((TIME) / 1000) - (System.currentTimeMillis() / 1000)) % 60) + " seconds.");
                    return;
                }
                if (DMisc.getFavor(p) >= ULTIMATECOST) {
                    if (!DMisc.canTarget(p, p.getLocation())) {
                        p.sendMessage(ChatColor.YELLOW + "You can't do that from a no-PVP zone.");
                        return;
                    }
                    int t = (int) (ULTIMATECOOLDOWNMAX - ((ULTIMATECOOLDOWNMAX - ULTIMATECOOLDOWNMIN) * ((double) DMisc.getAscensions(p) / Setting.ASCENSION_CAP)));
                    int hit = finale(p);
                    if (hit > 0) {
                        save.setAbilityData(ult, AD.TIME, System.currentTimeMillis() + (t * 1000));
                        p.sendMessage(ChatColor.GOLD + "Bragi " + ChatColor.WHITE + " rains arrows on " + hit + " of your foes.");
                        DMisc.setFavor(p, DMisc.getFavor(p) - ULTIMATECOST);
                    } else p.sendMessage(ChatColor.YELLOW + "No targets for Finale were found.");
                } else p.sendMessage(ChatColor.YELLOW + "" + ult + " requires " + ULTIMATECOST + " Favor.");
            }
        }
    }

    @Override
    public void onSyncTick(long timeSent) {
        for (UUID id : getPlayerIds()) {
            Optional<PlayerDataSaveable> opSave = getBackend().getPlayerDataRegistry().fromKey(id.toString());
            if (opSave.isPresent()) {
                PlayerDataSaveable save = opSave.get();
                if (timeSent > save.getAbilityData("bragi_heal", AD.TIME, 0) + 10000) {
                    save.setAbilityData("bragi_heal", AD.TIME, timeSent);
                    if ((Bukkit.getPlayer(id) != null) && !Bukkit.getPlayer(id).isDead()) {
                        Player p = Bukkit.getPlayer(id);
                        if (DMisc.getActiveEffectsList(id).contains("Bragi health regeneration")) {
                            DMisc.setHP(p, p.getHealth() + 1);
                            if (p.getHealth() > p.getMaxHealth()) DMisc.setHP(p, p.getMaxHealth());
                        } else if (DMisc.getActiveEffectsList(id).contains("Bragi Favor regeneration")) {
                            DMisc.setFavor(p, DMisc.getFavor(p) + 5);
                            if (DMisc.getFavor(p) > DMisc.getFavorCap(p))
                                DMisc.setFavor(p, DMisc.getFavorCap(p));
                        }
                    }
                }
            }
        }
    }

    private void applyEffect(Player p, PotionEffectType e, String description) {
        int duration = (int) Math.round(60 * Math.pow(DMisc.getDevotion(p, getName()), 0.09));
        if (DMisc.getActiveEffectsList(p.getUniqueId()).contains("Music Buff")) {
            p.sendMessage(ChatColor.YELLOW + "You have already received a Music Buff from Bragi.");
            return;
        }
        if (e == null) {
            p.sendMessage(ChatColor.GOLD + "Bragi" + ChatColor.WHITE + " has granted you a " + description + " bonus for " + duration + " seconds.");
            p.sendMessage(ChatColor.YELLOW + "NOTE: This bonus cannot be applied to your allies.");
            DMisc.addActiveEffect(p.getUniqueId(), "Bragi " + description, duration);
            DMisc.addActiveEffect(p.getUniqueId(), "Music Buff", duration);
        } else for (Player pl : p.getWorld().getPlayers()) {
            if (pl.getLocation().toVector().isInSphere(p.getLocation().toVector(), 15)) {
                if (DMisc.isFullParticipant(pl)) {
                    if (DMisc.getAllegiance(pl).equalsIgnoreCase(DMisc.getAllegiance(p))) {
                        pl.sendMessage(ChatColor.GOLD + "Bragi" + ChatColor.WHITE + " has granted you a " + description + " bonus for " + duration + " seconds.");
                        pl.addPotionEffect(new PotionEffect(e, duration * 20, 0));
                        DMisc.addActiveEffect(pl.getUniqueId(), "Music Buff", duration);
                    }
                }
            }
        }
    }

    private void cure(Player p) {
        double healamt = p.getMaxHealth();
        double selfheal = healamt / 9;
        if (p.getHealth() + selfheal > p.getMaxHealth()) {
            selfheal = p.getMaxHealth() - p.getHealth();
        }
        DMisc.setHP(p, p.getHealth() + selfheal);
        p.sendMessage(ChatColor.GREEN + "Bragi has cured you for " + selfheal + " health.");
        LivingEntity le = DMisc.getTargetLivingEntity(p, 3);
        if (le instanceof Player) {
            Player pl = (Player) le;
            if (DMisc.isFullParticipant(pl) && DMisc.getAllegiance(pl).equalsIgnoreCase(DMisc.getAllegiance(pl))) {
                if (pl.getHealth() + healamt > pl.getMaxHealth()) {
                    healamt = pl.getMaxHealth() - pl.getHealth();
                }
                DMisc.setHP(pl, pl.getHealth() + healamt);
                pl.sendMessage(ChatColor.GREEN + "Bragi has cured you for " + healamt + " health.");
                p.sendMessage(ChatColor.YELLOW + pl.getName() + " has been cured for " + healamt + " health.");
            }
        }
    }

    private int finale(final Player p) {
        int devotion = DMisc.getDevotion(p, getName());
        int ultrange = (int) Math.round(20 * Math.pow(devotion, 0.15));
        int ultslowduration = (int) Math.round(10 * Math.pow(devotion, 0.05)); // seconds
        int ultattacks = (int) Math.round(4 * Math.pow(devotion, 0.08)); // number of arrow "waves"
        ArrayList<LivingEntity> entitylist = new ArrayList<LivingEntity>();
        Vector ploc = p.getLocation().toVector();
        for (LivingEntity anEntity : p.getWorld().getLivingEntities()) {
            if (anEntity instanceof Player) if (DMisc.isFullParticipant((Player) anEntity))
                if (DMisc.getAllegiance((Player) anEntity).equalsIgnoreCase(DMisc.getAllegiance(p))) continue;
            if (!DMisc.canTarget(anEntity, anEntity.getLocation())) continue;
            if (anEntity.getLocation().toVector().isInSphere(ploc, ultrange)) entitylist.add(anEntity);
        }
        for (final LivingEntity target : entitylist) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, ultslowduration * 20, 1));
            for (int i = 0; i <= ultattacks * 20; i += 20) {
                DMisc.getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(DMisc.getPlugin(), () -> {
                    if (target.isDead()) return;
                    target.getLocation().setY(target.getLocation().getBlockY() + 50);
                    Arrow ar = target.getWorld().spawnArrow(target.getLocation(), new Vector(0, -5, 0), 5, (float) 0.2);
                    ar.setVelocity(new Vector(0, -5, 0));
                    if (Math.random() > 0.7) ar.setFireTicks(500);
                }, i);
            }
        }
        return entitylist.size();
    }

    @Override
    public boolean canTribute() {
        return true;
    }
}
