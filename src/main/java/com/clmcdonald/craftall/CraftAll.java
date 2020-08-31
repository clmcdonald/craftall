package com.clmcdonald.craftall;

import com.clmcdonald.craftall.commands.CraftAllCommand;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CraftAll extends JavaPlugin {
    @Override
    public void onEnable() {
        PluginCommand pluginCommand = this.getCommand("craftall");
        assert pluginCommand != null;
        pluginCommand.setTabCompleter((sender, command, alias, args) -> {
            List<String> completions = new ArrayList<>();
            StringUtil.copyPartialMatches(args[0], Arrays.asList("a", "aa", "aaa", "aaaa", "aaaaa"), completions);
            Collections.sort(completions);
            return completions;
        });
        pluginCommand.setExecutor(new CraftAllCommand());

        this.saveDefaultConfig();
        FileConfiguration config = this.getConfig();
    }
}
