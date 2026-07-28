package ru.nyansus.mc.fallenlink.message;

import org.bukkit.command.CommandSender;

public interface MessageProvider {

    String get(CommandSender sender, String key, String... replacements);

    String get(String key, String... replacements);
}
