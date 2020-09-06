package com.clmcdonald.craftall.domain;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CraftAllInventoryTest {

    @Mock
    private InventoryIngredients inventoryIngredientsMock;
    @Mock
    private Inventory inventoryMock;
    @Mock
    private ItemStack itemStackMock;

    private Map<Material, Integer> materialMap;
    private Material material;

    @BeforeEach
    public void beforeEach() {
        material = Material.COBBLESTONE;
        materialMap = new HashMap<>();
        materialMap.put(material, 10);
    }

    @Test
    public void shouldNotFitIfNoSpace() {
        when(inventoryIngredientsMock.getCraftableAmount()).thenReturn(10);
        when(inventoryIngredientsMock.getMaterialMap()).thenReturn(materialMap);
        when(inventoryMock.getStorageContents()).thenReturn(new ItemStack[] {itemStackMock});
        when(itemStackMock.getType()).thenReturn(material);
        when(itemStackMock.getAmount()).thenReturn(11);

        CraftAllInventory craftAllInventory = new CraftAllInventory(inventoryMock);
        assertFalse(craftAllInventory.canFit(material, inventoryIngredientsMock));
    }

    @Test
    public void shouldFitIfOneSpotWillClear() {
        when(inventoryIngredientsMock.getCraftableAmount()).thenReturn(10);
        when(inventoryIngredientsMock.getMaterialMap()).thenReturn(materialMap);
        when(inventoryMock.getStorageContents()).thenReturn(new ItemStack[] {itemStackMock, itemStackMock});
        when(itemStackMock.getType()).thenReturn(material);
        when(itemStackMock.getAmount()).thenReturn(5, 6);

        CraftAllInventory craftAllInventory = new CraftAllInventory(inventoryMock);
        assertTrue(craftAllInventory.canFit(material, inventoryIngredientsMock));
    }

    @Test
    public void shouldFitIfNullSpace() {
        when(inventoryIngredientsMock.getCraftableAmount()).thenReturn(10);
        when(inventoryIngredientsMock.getMaterialMap()).thenReturn(materialMap);
        when(itemStackMock.getType()).thenReturn(material);
        when(inventoryMock.getStorageContents()).thenReturn(new ItemStack[] {itemStackMock, null});
        when(itemStackMock.getAmount()).thenReturn(11);
        when(inventoryIngredientsMock.getCraftableAmount()).thenReturn(10);

        CraftAllInventory craftAllInventory = new CraftAllInventory(inventoryMock);
        assertTrue(craftAllInventory.canFit(material, inventoryIngredientsMock));
    }

    @Test
    public void shouldFitIfNoItems() {
        when(inventoryIngredientsMock.getCraftableAmount()).thenReturn(0);

        CraftAllInventory craftAllInventory = new CraftAllInventory(inventoryMock);
        assertTrue(craftAllInventory.canFit(material, inventoryIngredientsMock));
    }
}
