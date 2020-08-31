package com.clmcdonald.craftall.commands;

import com.clmcdonald.craftall.domain.CraftAllPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class CraftAllCommandExecutor implements org.bukkit.command.CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            CraftAllPlayer player = new CraftAllPlayer((Player) sender);
            player.setDebugEnabled(args.length > 0 && "DEBUG".equalsIgnoreCase(args[args.length - 1]));
            return this.onPlayerCommand(player, command, label, args);
        } else {
            return this.onConsoleCommand(sender, command, label, args);
        }
    }

    public abstract boolean onPlayerCommand(CraftAllPlayer player, Command command, String label, String[] args);

    public abstract boolean onConsoleCommand(CommandSender sender, Command command, String label, String[] args);
}