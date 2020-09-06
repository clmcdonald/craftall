package com.clmcdonald.craftall.commands;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.clmcdonald.craftall.domain.CraftAllInventory;
import com.clmcdonald.craftall.domain.CraftAllPlayer;
import com.clmcdonald.craftall.domain.CraftAllRequest;

import com.clmcdonald.craftall.domain.InventoryIngredients;
import com.clmcdonald.craftall.services.MaterialsService;
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
            if(args.length > 2) {
                sendUsage(player, command, label);
                return true;
            }

            Material material = getMaterial(args[0]);
            int maximum = -1;
            if(args.length == 2) {
                if(isInteger(args[1])) {
                    maximum = Integer.parseInt(args[1]);
                } else {
                    throw new IllegalArgumentException(Args.MAXIMUM.name());
                }
            }

            player.sendDebugMessage("Crafting material: &3{0}", material);

            List<Recipe> availableRecipes = retrieveAvailableRecipes(material);
            player.sendDebugMessage("Available recipes: &3{0}", availableRecipes.size());

            CraftAllRequest craftAllRequest = CraftAllRequest.create(availableRecipes, player.getInventory(), material, maximum);
            player.sendDebugMessage("Crafting &3{0}&r of &3{1}", craftAllRequest.getInventoryIngredients().getCraftableAmount(), craftAllRequest.getMaterial());
            player.sendDebugMessage("Materials used:");
            craftAllRequest.getInventoryIngredients().getMaterialMap().forEach((key, value) -> player.sendDebugMessage("  &3{0}&r (x&3{1}&r)", key.name(), value));

            tradeIngredientsForResult(craftAllRequest.getInventoryIngredients(), player.getInventory(), material);

            player.sendMessage("&6Crafted &c{0}&6 of &c{1}", craftAllRequest.getInventoryIngredients().getCraftableAmount(), convertToReadable(craftAllRequest.getMaterial()));

            return true;
        } catch (IllegalArgumentException e) {
            if(e.getMessage().equalsIgnoreCase(Args.MATERIAL.name())) {
                player.sendMessage("&c{0} is not a recognized material", args[0]);
            } else if(e.getMessage().equalsIgnoreCase(Args.MAXIMUM.name())) {
                player.sendMessage("&c{0} is not an integer", args[1]);
            }

            sendUsage(player, command, label);
            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            sendUsage(player, command, label);
            return true;
        }
    }

    @Override
    public boolean onConsoleCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage(ChatColor.DARK_RED + "You must be a player to use this command.");
        return true;
    }

    private void sendUsage(CraftAllPlayer player, Command command, String label) {
        player.sendMessage("&cUsage: {0}", getUsageWithLabel(command, label));
    }

    private String getUsageWithLabel(Command command, String label) {
        return command.getUsage().replaceFirst("<command>", label);
    }

    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException ex) {
            return false;
        }
    }

    private String convertToReadable(Material material) {
        String[] split = material.name().split("_");
        return Arrays.stream(split)
                .map(String::toLowerCase)
                .collect(Collectors.joining(" "));
    }

    private Material getMaterial(String materialName) {
        String standardized = MaterialsService.standardizeName(materialName);
        Material material = Material.getMaterial(standardized);
        if(material != null) {
            return material;
        } else {
            throw new IllegalArgumentException(Args.MATERIAL.name());
        }
    }

    private List<Recipe> retrieveAvailableRecipes(Material material) {
        return Bukkit.getServer().getRecipesFor(new ItemStack(material));
    }

    private void tradeIngredientsForResult(InventoryIngredients inventoryIngredients, CraftAllInventory inventory, Material material) {
        Map<Material, Integer> neededIngredients = inventoryIngredients.getMaterialMap();
        Map<Material, Integer> removedIngredients = neededIngredients.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, (entry) -> 0));

        IntStream.range(0, inventory.getBukkitInventory().getStorageContents().length)
                .forEach((index) -> {
                    ItemStack itemStack = inventory.getBukkitInventory().getStorageContents()[index];

                    if(itemStack != null) {
                        Material itemMaterial = itemStack.getType();
                        if(neededIngredients.get(itemMaterial) != null && neededIngredients.get(itemMaterial) > removedIngredients.get(itemMaterial)) {
                            int remaining = neededIngredients.get(itemMaterial) - removedIngredients.get(itemMaterial);
                            if(remaining > itemStack.getAmount()) {
                                inventory.getBukkitInventory().clear(index);
                                removedIngredients.put(itemMaterial, itemStack.getAmount() + removedIngredients.get(itemMaterial));
                            } else {
                                itemStack.setAmount(itemStack.getAmount() - remaining);
                                inventory.getBukkitInventory().setItem(index, itemStack);
                                removedIngredients.put(itemMaterial, remaining + removedIngredients.get(itemMaterial));
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

    enum Args {
        MATERIAL, MAXIMUM
    }
}