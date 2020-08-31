package com.clmcdonald.craftall.domain;

import lombok.Data;
import lombok.NonNull;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains additional methods that can be applied to an inventory.
 */
@Data
public class CraftAllInventory {
    @NonNull
    private Inventory bukkitInventory;

    /**
     * Returns true if the inventory can fit the results of crafting
     * the given material using the inventory ingredients given.
     * @param material the material that is being crafted
     * @param inventoryIngredients the inventory ingredients to use on the craft
     * @return true if the inventory can fit the results of the recipe, otherwise false
     */
    public boolean canFit(Material material, InventoryIngredients inventoryIngredients) {
        if(inventoryIngredients.getCraftableAmount() == 0) {
            return true;
        }

        int openSlots = 0;

        Map<Material, Integer> materialsUsed = new HashMap<>();

        for(ItemStack itemStack : this.getBukkitInventory().getStorageContents()) {
            if(itemStack == null) {
                openSlots++;
            } else if(inventoryIngredients.getMaterialMap().containsKey(itemStack.getType())) {
                long target = inventoryIngredients.getMaterialMap().get(itemStack.getType());
                int located = 0;
                if(materialsUsed.containsKey(itemStack.getType())) {
                    located = materialsUsed.get(itemStack.getType());
                }

                if(located < target) {
                    int current = itemStack.getAmount();
                    if(current + located > target) {
                        current = (int) (target - located);
                    } else {
                        openSlots++;
                    }

                    materialsUsed.put(itemStack.getType(), located + current);
                }
            }
        }

        int requiredSlots = (int) Math.ceil((1.0*inventoryIngredients.getCraftableAmount()) / material.getMaxStackSize());
        return openSlots >= requiredSlots;
    }
}
