package com.clmcdonald.craftall.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.inventory.Recipe;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This object stores information that is needed to perform the craft all command.
 */
@Data
@AllArgsConstructor
public class CraftAllRequest implements Comparable<CraftAllRequest> {
    private Recipe recipe;

    @NonNull
    private Material material;
    @NonNull
    private InventoryIngredients inventoryIngredients;
    @NonNull
    private Integer maximum;

    /**
     * Creates a new CraftAllRequest from the given information
     * @param recipes the recipes that can be used to craft the material
     * @param inventory the player inventory of the user who is crafting
     * @param material the material to craft
     */
    public static CraftAllRequest create(List<Recipe> recipes, CraftAllInventory inventory, Material material, Integer maximum) {
        if(recipes.size() == 0) {
            return new CraftAllRequest(null, material, new InventoryIngredients(), maximum);
        } else {
            List<CraftAllRequest> craftAllRequestList = recipes
                    .stream()
                    .map((recipe) -> {
                        InventoryIngredients inventoryIngredients = maximumInventoryIngredients(recipe, material, inventory, maximum);
                        return new CraftAllRequest(recipe, material, inventoryIngredients, maximum);
                    })
                    .collect(Collectors.toList());

            return Collections.max(craftAllRequestList);
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
    private static InventoryIngredients maximumInventoryIngredients(Recipe recipe, Material material, CraftAllInventory inventory, Integer maximum) {
        InventoryIngredients ingredients = InventoryIngredients.create(inventory, recipe);

        while(greaterThanMax(ingredients.getCraftableAmount(), maximum) || !inventory.canFit(material, ingredients)) {
            ingredients.setCraftableAmount(ingredients.getCraftableAmount() - recipe.getResult().getAmount());
        }

        return ingredients;
    }

    private static boolean greaterThanMax(int amount, int maximum) {
        if(maximum < 0)
            return false;
        else
            return amount > maximum;
    }
}