package com.clmcdonald.craftall.domain;

import java.util.*;

import com.google.common.collect.Lists;

import org.bukkit.Material;
import org.bukkit.inventory.Recipe;

import lombok.Data;

/**
 * This object stores information that is needed to perform the craft all command.
 */
@Data
public class CraftAllRequest implements Comparable<CraftAllRequest> {
    private Material material;
    private Recipe recipe;
    private InventoryIngredients inventoryIngredients;

    /**
     * Creates a new CraftAllRequest from the given information
     * @param recipes the recipes that can be used to craft the material
     * @param inventory the player inventory of the user who is crafting
     * @param material the material to craft
     */
    public CraftAllRequest(List<Recipe> recipes, CraftAllInventory inventory, Material material) {

        if(recipes.size() == 0) {
            this.material = material;
            this.recipe = null;
            this.inventoryIngredients = new InventoryIngredients();
        } else if(recipes.size() == 1) {
            this.material = material;
            this.recipe = recipes.get(0);
            this.inventoryIngredients = this.maximumInventoryIngredients(inventory, this.material, recipe);
        } else {
            List<List<Recipe>> partitioned = Lists.partition(recipes, (int) Math.ceil(recipes.size()/2.0));
            
            CraftAllRequest request1 = new CraftAllRequest(partitioned.get(0), inventory, material);
            CraftAllRequest request2 = new CraftAllRequest(partitioned.get(1), inventory, material);

            CraftAllRequest greater = request1.compareTo(request2) < 0 ? request2 : request1;
            this.material = greater.getMaterial();
            this.recipe = greater.getRecipe();
            this.inventoryIngredients = greater.getInventoryIngredients();
        }
    }

    /**
     * Compares the CraftAllRequest based on the craftable amount
     * @param craftAllRequest request to compare to
     * @return
     */
    @Override
    public int compareTo(CraftAllRequest craftAllRequest) {
        return this.getInventoryIngredients().getCraftableAmount() - craftAllRequest.getInventoryIngredients().getCraftableAmount();
    }

    /**
     * Creates the InventoryIngredients and checks if it can fit the
     * maximum number of results. If not, it decreases it until it can
     * fit.
     * @param inventory the user's inventory
     * @param material the material that is being crafted
     * @param recipe the recipe to use to craft the material
     * @return the amount of items that can be crafted and fit
     */
    private InventoryIngredients maximumInventoryIngredients(CraftAllInventory inventory, Material material, Recipe recipe) {
        InventoryIngredients ingredients = new InventoryIngredients(inventory, recipe);

        while(!inventory.canFit(material, ingredients)) {
            ingredients.setCraftableAmount(ingredients.getCraftableAmount() - recipe.getResult().getAmount());
        }

        return ingredients;
    }
}