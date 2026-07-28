package ru.nyansus.mc.fallenlink.service;

import org.bukkit.entity.Player;

public interface PlayerSyncUseCase {

    void syncOnlinePlayers();

    void syncPlayer(Player player, boolean online);
}
