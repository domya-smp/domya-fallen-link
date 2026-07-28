package ru.nyansus.mc.fallenlink.player;

import org.bukkit.entity.Player;

public interface PlayerNameProvider {

    String resolve(Player player);
}
