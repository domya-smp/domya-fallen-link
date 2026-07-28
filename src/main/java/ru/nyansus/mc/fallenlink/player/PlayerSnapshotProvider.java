package ru.nyansus.mc.fallenlink.player;

import org.bukkit.entity.Player;
import ru.nyansus.mc.fallenlink.model.PlayerSnapshot;

public interface PlayerSnapshotProvider {

    PlayerSnapshot create(Player player, boolean online);
}
