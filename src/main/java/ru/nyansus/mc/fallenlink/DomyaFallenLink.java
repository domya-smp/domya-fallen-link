package ru.nyansus.mc.fallenlink;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class DomyaFallenLink extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (getCommand("domyasync") != null) {
            getCommand("domyasync").setExecutor(this);
        }
        if (getCommand("link") != null) {
            getCommand("link").setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("link")) {
            sender.sendMessage("domya-fallen-link skeleton: account linking is not implemented yet.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            sender.sendMessage("domya-fallen-link configuration reloaded.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("status")) {
            sender.sendMessage("domya-fallen-link v" + getDescription().getVersion());
            sender.sendMessage("API: " + getConfig().getString("api-url", ""));
            sender.sendMessage("Online players: " + getServer().getOnlinePlayers().size());
            return true;
        }

        if (args[0].equalsIgnoreCase("sync")) {
            sender.sendMessage("domya-fallen-link skeleton: sync is not implemented yet.");
            return true;
        }

        sender.sendMessage("Usage: /domyasync <sync|reload|status> or /link <code>");
        return true;
    }
}
