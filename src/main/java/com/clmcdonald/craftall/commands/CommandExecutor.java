package com.clmcdonald.craftall.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class CommandExecutor implements org.bukkit.command.CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            return this.onPlayerCommand((Player) sender, command, label, args);
        } else {
            return this.onConsoleCommand(sender, command, label, args);
        }
    }

    public abstract boolean onPlayerCommand(Player player, Command command, String label, String[] args);

    public abstract boolean onConsoleCommand(CommandSender sender, Command command, String label, String[] args);
}