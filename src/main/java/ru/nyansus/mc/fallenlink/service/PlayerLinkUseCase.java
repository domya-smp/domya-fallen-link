package ru.nyansus.mc.fallenlink.service;

import org.bukkit.entity.Player;

public interface PlayerLinkUseCase {

    void linkPlayer(Player player, String code);
}
