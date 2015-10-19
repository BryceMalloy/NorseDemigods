package com.demigodsrpg.norsedemigods.chitchat;

import com.demigodsrpg.chitchat.tag.PlayerTag;
import com.demigodsrpg.norsedemigods.DMisc;
import com.demigodsrpg.norsedemigods.saveable.PlayerDataSaveable;
import com.google.common.base.Joiner;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

public class AllianceTag extends PlayerTag {
    @Override
    public String getName() {
        return "Alliance";
    }

    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public TextComponent getComponentFor(Player player) {
        PlayerDataSaveable save = DMisc.getPlugin().getPlayerDataRegistry().fromPlayer(player);
        String alliance = save.getAlliance();
        ChatColor color = alliance.equals("Jotunn") ? ChatColor.GOLD : alliance.equals("Human") ?
                ChatColor.GRAY : ChatColor.DARK_AQUA;
        String shortName = alliance.equals("Jotunn") ? "J" : alliance.equals("Human") ? "M" : "AE";
        TextComponent allegience = new TextComponent("[");
        allegience.setColor(ChatColor.DARK_GRAY);
        TextComponent middle = new TextComponent(shortName);
        middle.setColor(color);
        allegience.addExtra(middle);
        allegience.addExtra("]");
        allegience.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(Joiner.on(", ").join(save.getDeityList())).color(color).create())); // TODO Temp
        allegience.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/dg " + alliance));
        return allegience;
    }
}
