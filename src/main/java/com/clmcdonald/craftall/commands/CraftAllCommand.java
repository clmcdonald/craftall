package com.clmcdonald.craftall.commands;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.clmcdonald.craftall.domain.CraftAllInventory;
import com.clmcdonald.craftall.domain.CraftAllPlayer;
import com.clmcdonald.craftall.domain.CraftAllRequest;

import com.clmcdonald.craftall.domain.InventoryIngredients;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

public class CraftAllCommand extends CraftAllCommandExecutor {

    @Override
    public boolean onPlayerCommand(CraftAllPlayer player, Command command, String label, String[] args) {
        try {
            Material material = Material.getMaterial(args[0].toUpperCase());
            player.sendDebugMessage("Crafting material: &3{0}", material);

            List<Recipe> availableRecipes = retrieveAvailableRecipes(material);
            player.sendDebugMessage("Available recipes: &3{0}", availableRecipes.size());

            CraftAllRequest craftAllRequest = new CraftAllRequest(availableRecipes, player.getInventory(), material);
            player.sendDebugMessage("Crafting &3{0}&r of &3{1}", craftAllRequest.getInventoryIngredients().getCraftableAmount(), craftAllRequest.getMaterial());
            player.sendDebugMessage("Materials used:");
            craftAllRequest.getInventoryIngredients().getMaterialMap().forEach((key, value) -> player.sendDebugMessage("  &3{0}&r (x&3{1}&r)", key.name(), value));

            tradeIngredientsForResult(craftAllRequest.getInventoryIngredients(), player.getInventory(), material);

            player.sendMessage("Crafted &3{0}&r of &3{1}", craftAllRequest.getInventoryIngredients().getCraftableAmount(), craftAllRequest.getMaterial());

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

    private List<Recipe> retrieveAvailableRecipes(Material material) throws IllegalArgumentException {
        if(material == null) {
            throw new IllegalArgumentException("Material not recognized");
        }
        
        return Bukkit.getServer().getRecipesFor(new ItemStack(material));
    }

    private void tradeIngredientsForResult(InventoryIngredients inventoryIngredients, CraftAllInventory inventory, Material material) {
        Map<Material, Long> neededIngredients = inventoryIngredients.getMaterialMap();
        Map<Material, Long> removedIngredients = neededIngredients.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, (entry) -> 0L));

        IntStream.range(0, inventory.getBukkitInventory().getStorageContents().length)
                .forEach((index) -> {
                    ItemStack itemStack = inventory.getBukkitInventory().getStorageContents()[index];
                    if(itemStack != null) {
                        Material itemMaterial = itemStack.getType();
                        if(neededIngredients.get(itemMaterial) != null && neededIngredients.get(itemMaterial) > removedIngredients.get(itemMaterial)) {
                            long remaining = neededIngredients.get(itemMaterial) - removedIngredients.get(itemMaterial);
                            if(remaining > itemStack.getAmount()) {
                                inventory.getBukkitInventory().clear(index);
                            } else {
                                itemStack.setAmount((int) (itemStack.getAmount() - remaining));
                                inventory.getBukkitInventory().setItem(index, itemStack);
                            }
                        }
                    }
                });

        List<ItemStack> result = new ArrayList<>();
        int fullStacks = inventoryIngredients.getCraftableAmount() / material.getMaxStackSize();
        int leftover = inventoryIngredients.getCraftableAmount() % material.getMaxStackSize();

        IntStream.range(0, fullStacks)
                .forEach((index) -> result.add(new ItemStack(material, material.getMaxStackSize())));
        result.add(new ItemStack(material, leftover));
        ItemStack[] itemStackArray = new ItemStack[result.size()];
        result.toArray(itemStackArray);

        inventory.getBukkitInventory().addItem(itemStackArray);
    }
}