package com.clmcdonald.craftall.commands;

import java.util.List;

import com.clmcdonald.craftall.domain.CraftAllRequest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class CraftAllCommand extends CommandExecutor {

    @Override
    public boolean onPlayerCommand(Player player, Command command, String label, String[] args) {
        try {
            Material material = Material.getMaterial(args[0].toUpperCase());
            List<Recipe> availableRecipes = retrieveAvailableRecipes(material);

            Bukkit.getLogger().info("recipes: " + availableRecipes.size());

            CraftAllRequest craftAllRequest = new CraftAllRequest(availableRecipes, player.getInventory(), material);

            player.sendMessage("You can craft " + craftAllRequest.getAmount() + " of " + args[0]);
            return true;
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + args[0] + " is not a recognized material");
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    @Override
    public boolean onConsoleCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.DARK_RED + "You must be a player to use this command.");
        return false;
    }

    /**
     * Retrieve available recipes for a given material
     * @param materialArg
     * @return available recipes
     * @throws IllegalArgumentException
     */
    private List<Recipe> retrieveAvailableRecipes(Material material) throws IllegalArgumentException {
        if(material == null) {
            throw new IllegalArgumentException("Material not recognized");
        }
        
        return Bukkit.getServer().getRecipesFor(new ItemStack(material));
    }
}