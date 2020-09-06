package com.clmcdonald.craftall;

import com.clmcdonald.craftall.commands.CraftAllCommand;
import com.clmcdonald.craftall.services.MaterialsService;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.util.StringUtil;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CraftAll extends JavaPlugin {

    public CraftAll() {
        // Needed for MockBukkit
        super();
    }

    protected CraftAll(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        // Needed for MockBukkit
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onEnable() {
        PluginCommand pluginCommand = this.getCommand("craftall");
        assert pluginCommand != null;
        pluginCommand.setTabCompleter(this::tabCompleter);
        pluginCommand.setExecutor(new CraftAllCommand());
    }

    private List<String> tabCompleter(CommandSender sender, Command command, String alias, String[] argsArray) {
        List<String> args = Arrays.asList(argsArray);

        if(args.size() > 0) {
            List<String> completions = new ArrayList<>();
            Set<String> suggestions = null;

            if(args.size() == 1) {
                suggestions = MaterialsService.retrieveMaterials();
            } else if(args.size() == 2) {
                suggestions = Stream.of("1", "64").collect(Collectors.toSet());
            } else {
                suggestions = new HashSet<>();
            }

            StringUtil.copyPartialMatches(args.get(args.size() - 1), suggestions, completions);
            Collections.sort(completions);
            return completions;
        } else {
            return new ArrayList<>();
        }
    }
}
