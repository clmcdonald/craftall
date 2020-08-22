package com.clmcdonald.craftall.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.RecipeChoice.MaterialChoice;

import lombok.Data;

@Data
public class CraftAllRequest implements Comparable<CraftAllRequest> {
    private Material material;
    private Recipe recipe;
    private int amount;

    public CraftAllRequest(List<Recipe> recipes, Inventory inventory, Material material) {
        if(recipes.size() == 0) {
            this.material = material;
            this.recipe = null;
            this.amount = 0;
        } else if(recipes.size() == 1) {
            this.material = material;
            this.recipe = recipes.get(0);
            this.amount = this.calculateAmount(inventory, this.material, recipe);
        } else {
            List<List<Recipe>> partitioned = Lists.partition(recipes, (int) Math.ceil(recipes.size()/2.0));
            
            CraftAllRequest request1 = new CraftAllRequest(partitioned.get(0), inventory, material);
            CraftAllRequest request2 = new CraftAllRequest(partitioned.get(1), inventory, material);

            CraftAllRequest greater = request1.compareTo(request2) < 0 ? request2 : request1;
            this.material = greater.getMaterial();
            this.recipe = greater.getRecipe();
            this.amount = greater.getAmount();
        }
    }

    @Override
    public int compareTo(CraftAllRequest craftAllRequest) {
        return this.getAmount() - craftAllRequest.getAmount();
    }

    private int calculateAmount(Inventory inventory, Material material, Recipe recipe) {
        List<ItemStack> inventoryList = Arrays.asList(inventory.getStorageContents());
        int craftable = countCraftable(inventoryList, recipe);
        Bukkit.getLogger().info("Initial craftable: " + craftable);
        
        while(!canInventoryFit(inventory, material, recipe, craftable)) {
            craftable--;
        }
        Bukkit.getLogger().info("Updated craftable: " + craftable);

        return craftable;
    }

    private boolean canInventoryFit(Inventory inventory, Material material, Recipe recipe, int amount) {
        if(amount == 0) {
            return true;
        }

        Map<Material, Integer> recipeMap = this.mapRecipeToIngredients(recipe);
        Map<Material, Integer> locatedIngredients = new HashMap<>();
        int openSlots = 0;

        for(ItemStack itemStack : inventory.getStorageContents()) {
            if(itemStack == null) {
                openSlots++;
            } else if(recipeMap.containsKey(itemStack.getType())) {
                int target = recipeMap.get(itemStack.getType())*amount;
                int located = 0;
                if(locatedIngredients.containsKey(itemStack.getType())) {
                    located = locatedIngredients.get(itemStack.getType());
                }

                if(located < target) {
                    int current = itemStack.getAmount();
                    if(current + located > target) {
                        current = target - located;
                    } else {
                        openSlots++;
                    }

                    locatedIngredients.put(itemStack.getType(), located + current);
                }
            }
        }

        int requiredSlots = (int) Math.ceil((1.0*amount) / material.getMaxStackSize());

        return openSlots >= requiredSlots;
    }

    private int countCraftable(List<ItemStack> inventory, Recipe recipe) {
        Map<Material, Integer> inventoryMap = this.mapItemStacksToIngredients(inventory);

        Optional<List<RecipeChoice>> optionalRecipeChoices = getRecipeChoices(recipe);

        if(optionalRecipeChoices.isPresent()) {
            List<MaterialChoice> materialChoices = optionalRecipeChoices.get().stream().map((recipeChoice) -> (MaterialChoice) recipeChoice).collect(Collectors.toList());
            Map<MaterialChoice, Integer> numberOfMaterialChoicePerCraft = materialChoices.stream().collect(
                Collectors.toMap(
                    Function.identity(),
                    (materialChoice) -> 1,
                    (existing, replacement) -> existing + replacement
                )
            );
            Map<MaterialChoice, Integer> locatedMaterials = new HashMap<>();
            inventoryMap.entrySet().stream().forEach((entry) -> {
                List<MaterialChoice> memberMaterialChoices = materialChoices.stream().filter((materialChoice) -> materialChoice.test(new ItemStack(entry.getKey()))).collect(Collectors.toList());
                memberMaterialChoices.forEach((member) -> {
                    locatedMaterials.put(member, entry.getValue());
                });
            });
        }

        if(recipeMap != null) {
            return recipeMap.entrySet()
                .stream()
                .map((ingredientEntry) -> {
                    int amountPerItem = ingredientEntry.getValue();
                    int amountInInventory = inventoryMap.getOrDefault(ingredientEntry.getKey(), 0);

                    Bukkit.getLogger().info("amount in inventory: " + amountInInventory);
                    Bukkit.getLogger().info("amount per item: " + amountPerItem);
                    Bukkit.getLogger().info("recipe result amount: " + recipe.getResult().getAmount());
                    Bukkit.getLogger().info("crafted with " + ingredientEntry.getKey().name() + ": " + (amountInInventory / amountPerItem) * recipe.getResult().getAmount());

                    return (amountInInventory / amountPerItem) * recipe.getResult().getAmount();
                })
                .min(Integer::compare)
                .get();
        } else {
            return 0;
        }
    }

    private Optional<List<RecipeChoice>> getRecipeChoices(Recipe recipe) {
        if(recipe instanceof ShapelessRecipe) {
            return Optional.of(((ShapelessRecipe) recipe).getChoiceList());
        }

        if(recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
            return Optional.of(new ArrayList<>(shapedRecipe.getChoiceMap().values()));
        }

        return Optional.empty();
    }

    private Map<Material, Integer> mapItemStacksToIngredients(List<ItemStack> itemStacks) {
        return itemStacks
            .stream()
            .filter(itemStack -> itemStack != null)
            .collect(
                Collectors.toMap(
                    ItemStack::getType,
                    ItemStack::getAmount,
                    (existing, replacement) -> existing + replacement
                )
            );
    }
}