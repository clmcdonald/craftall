package com.clmcdonald.craftall.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Contains the ingredients for a recipe that the user has in their inventory.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryIngredients {
    private int craftableAmount;
    private Map<RecipeChoice.MaterialChoice, List<Material>> resourceMap;
    private Map<RecipeChoice.MaterialChoice, Integer> multiplicity;

    public static InventoryIngredients create(CraftAllInventory inventory, Recipe recipe) {
        Map<Material, Integer> inventoryMap = itemStacksToMaterialMap(Arrays.asList(inventory.getBukkitInventory().getStorageContents()));

        Optional<List<RecipeChoice>> optionalRecipeChoices = getRecipeChoices(recipe);

        if(optionalRecipeChoices.isPresent()) {
            List<RecipeChoice.MaterialChoice> materialChoices = optionalRecipeChoices.get().stream().map((recipeChoice) -> (RecipeChoice.MaterialChoice) recipeChoice).collect(Collectors.toList());

            // Calculate the multiplicity of each MaterialChoice
            Map<RecipeChoice.MaterialChoice, Integer> multiplicity = materialChoices.stream().collect(
                    Collectors.toMap(
                            Function.identity(),
                            (materialChoice) -> 1,
                            Integer::sum
                    )
            );

            // Stack the resources so that the stack with the lowest number of items in it
            // gets the next one
            Map<RecipeChoice.MaterialChoice, List<Material>> locatedResources = new HashMap<>();
            materialChoices.forEach((choice) -> locatedResources.put(choice, new ArrayList<>()));

            inventoryMap.forEach((key, value) -> {
                for (int i = 0; i < value; i++) {
                    // choose the MaterialChoice to put this item in
                    Optional<AbstractMap.SimpleEntry<RecipeChoice.MaterialChoice, Integer>> optionalMinimum = locatedResources.keySet().stream()
                            .filter((choice) -> choice.test(new ItemStack(key)))
                            .map((choice) -> new AbstractMap.SimpleEntry<>(choice, locatedResources.get(choice).size() / multiplicity.get(choice)))
                            .min(Map.Entry.comparingByValue());

                    if (optionalMinimum.isPresent()) {
                        Map.Entry<RecipeChoice.MaterialChoice, Integer> minimum = optionalMinimum.get();
                        locatedResources.get(minimum.getKey()).add(key);
                    } else {
                        break;
                    }
                }
            });

            Optional<AbstractMap.SimpleEntry<RecipeChoice.MaterialChoice, Integer>> optionalMinimum = locatedResources.entrySet().stream()
                    .map((entry) -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().size() / multiplicity.get(entry.getKey())))
                    .min(Map.Entry.comparingByValue());

            if(optionalMinimum.isPresent()) {
                Map.Entry<RecipeChoice.MaterialChoice, Integer> minimum = optionalMinimum.get();
                int amountCraftable = minimum.getValue()*recipe.getResult().getAmount();

                locatedResources.forEach((key, value) -> {
                    int needed = (amountCraftable / recipe.getResult().getAmount()) * multiplicity.get(key);
                    while (value.size() > needed) {
                        value.remove(0);
                    }
                });

                return new InventoryIngredients(amountCraftable, locatedResources, multiplicity);
            }
        }

        return new InventoryIngredients();
    }

    /**
     * @return a map that gets the amount of each material needed to complete the recipe
     */
    public Map<Material, Integer> getMaterialMap() {
        if(resourceMap != null) {
            return resourceMap.values().stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.groupingBy(
                            Function.identity(),
                            Collectors.counting()
                    ))
                    .entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, (entry) -> entry.getValue().intValue()));
        } else {
            return new HashMap<>();
        }
    }

    /**
     * Sets the craftable amount.
     * Decreases the resources in the resource map if the
     * craftable amount is set lower. If it is set higher,
     * the resource map will be lacking enough resources to
     * craft the amount.
     * @param craftableAmount the amount to set
     */
    public void setCraftableAmount(int craftableAmount) {
        this.craftableAmount = craftableAmount;

        if(resourceMap != null) {
            resourceMap.forEach((key, value) -> {
                int multiplicity = this.multiplicity.get(key);
                int expectedNumberOfMaterials = craftableAmount*multiplicity;

                while(value.size() > expectedNumberOfMaterials) {
                    value.remove(0);
                }
            });
        }
    }

    private static Optional<List<RecipeChoice>> getRecipeChoices(Recipe recipe) {
        if(recipe instanceof ShapelessRecipe) {
            return Optional.of(((ShapelessRecipe) recipe).getChoiceList());
        }

        if(recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
            return Optional.of(new ArrayList<>(shapedRecipe.getChoiceMap().values()));
        }

        return Optional.empty();
    }

    private static Map<Material, Integer> itemStacksToMaterialMap(List<ItemStack> itemStacks) {
        return itemStacks
                .stream()
                .filter(Objects::nonNull)
                .collect(
                        Collectors.toMap(
                                ItemStack::getType,
                                ItemStack::getAmount,
                                Integer::sum
                        )
                );
    }
}
