package ru.nyansus.mc.fallenlink.player;

import java.util.Collection;
import org.bukkit.entity.Player;

public interface OnlinePlayerProvider {

    Collection<? extends Player> getOnlinePlayers();
}
