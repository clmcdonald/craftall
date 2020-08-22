package com.clmcdonald.craftall;

import com.clmcdonald.craftall.commands.CraftAllCommand;

import org.bukkit.plugin.java.JavaPlugin;

public class CraftAll extends JavaPlugin {
    @Override
    public void onEnable() {
        this.getCommand("craftall").setExecutor(new CraftAllCommand());
    }
}
