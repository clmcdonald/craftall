package com.clmcdonald.craftall.domain;

import org.bukkit.Material;
import org.bukkit.inventory.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InventoryIngredientsTest {

    @Mock
    private CraftAllInventory craftAllInventoryMock;
    @Mock
    private ShapelessRecipe shapelessRecipeMock;
    @Mock
    private ShapedRecipe shapedRecipeMock;
    @Mock
    private Recipe recipeMock;
    @Mock
    private PlayerInventory playerInventoryMock;
    @Mock
    private ItemStack itemStackMock;
    @Mock
    private ItemStack resultItemStackMock;
    @Mock
    private RecipeChoice.MaterialChoice materialChoiceMock1;
    @Mock
    private RecipeChoice.MaterialChoice materialChoiceMock2;

    @Test
    public void shouldGetTheNumberThatCanBeCrafted() {
        when(craftAllInventoryMock.getBukkitInventory()).thenReturn(playerInventoryMock);
        when(playerInventoryMock.getStorageContents()).thenReturn(new ItemStack[] {itemStackMock});
        when(itemStackMock.getType()).thenReturn(Material.COBBLESTONE);
        when(itemStackMock.getAmount()).thenReturn(32);
        when(shapelessRecipeMock.getChoiceList()).thenReturn(Arrays.asList(materialChoiceMock1, materialChoiceMock1));
        when(shapelessRecipeMock.getResult()).thenReturn(resultItemStackMock);
        when(resultItemStackMock.getAmount()).thenReturn(2);
        when(materialChoiceMock1.test(any(ItemStack.class))).thenReturn(true);

        InventoryIngredients inventoryIngredients = InventoryIngredients.create(craftAllInventoryMock, shapelessRecipeMock);
        assertEquals(32, inventoryIngredients.getCraftableAmount());
    }

    @Test
    public void shouldNotTakeMoreThanIsNeeded() {
        Map<Character, RecipeChoice> choiceMap = new HashMap<>();
        choiceMap.put('1', materialChoiceMock1);
        choiceMap.put('2', materialChoiceMock1);

        when(craftAllInventoryMock.getBukkitInventory()).thenReturn(playerInventoryMock);
        when(playerInventoryMock.getStorageContents()).thenReturn(new ItemStack[] {itemStackMock});
        when(itemStackMock.getType()).thenReturn(Material.COBBLESTONE);
        when(itemStackMock.getAmount()).thenReturn(33);
        when(shapedRecipeMock.getChoiceMap()).thenReturn(choiceMap);
        when(shapedRecipeMock.getResult()).thenReturn(resultItemStackMock);
        when(resultItemStackMock.getAmount()).thenReturn(2);
        when(materialChoiceMock1.test(any(ItemStack.class))).thenReturn(true);

        InventoryIngredients inventoryIngredients = InventoryIngredients.create(craftAllInventoryMock, shapedRecipeMock);
        assertEquals(32, inventoryIngredients.getCraftableAmount());
    }

    @Test
    public void shouldGetZeroCraftableWhenNoItemInInventoryMatches() {
        when(craftAllInventoryMock.getBukkitInventory()).thenReturn(playerInventoryMock);
        when(playerInventoryMock.getStorageContents()).thenReturn(new ItemStack[] {itemStackMock});
        when(itemStackMock.getType()).thenReturn(Material.COBBLESTONE);
        when(itemStackMock.getAmount()).thenReturn(32);
        when(shapelessRecipeMock.getChoiceList()).thenReturn(Arrays.asList(materialChoiceMock1, materialChoiceMock1));
        when(shapelessRecipeMock.getResult()).thenReturn(resultItemStackMock);
        when(resultItemStackMock.getAmount()).thenReturn(2);
        when(materialChoiceMock1.test(any(ItemStack.class))).thenReturn(false);

        InventoryIngredients inventoryIngredients = InventoryIngredients.create(craftAllInventoryMock, shapelessRecipeMock);
        assertEquals(0, inventoryIngredients.getCraftableAmount());
    }

    @Test
    public void shouldGetBlankInventoryIngredientsWhenNotShapelessOrShapedRecipe() {
        when(craftAllInventoryMock.getBukkitInventory()).thenReturn(playerInventoryMock);
        when(playerInventoryMock.getStorageContents()).thenReturn(new ItemStack[] {itemStackMock});
        when(itemStackMock.getType()).thenReturn(Material.COBBLESTONE);
        when(itemStackMock.getAmount()).thenReturn(32);

        InventoryIngredients inventoryIngredients = InventoryIngredients.create(craftAllInventoryMock, recipeMock);
        assertEquals(0, inventoryIngredients.getCraftableAmount());
        assertNull(inventoryIngredients.getMultiplicity());
        assertNull(inventoryIngredients.getResourceMap());
    }

    @Test
    public void getMaterialMapShouldBeEmptyIfResourceMapIsNull() {
        InventoryIngredients inventoryIngredients = new InventoryIngredients(0, null, null);
        assertEquals(0, inventoryIngredients.getMaterialMap().size());
    }

    @Test
    public void getMaterialMapShouldReturnCorrectNumberOfItems() {
        Map<RecipeChoice.MaterialChoice, List<Material>> resourceMap = new HashMap<>();
        resourceMap.put(materialChoiceMock1, Arrays.asList(Material.COBBLESTONE, Material.COBBLESTONE));
        resourceMap.put(materialChoiceMock2, Arrays.asList(Material.COBBLESTONE, Material.COBBLESTONE));
        InventoryIngredients inventoryIngredients = new InventoryIngredients(0, resourceMap, null);
        assertEquals(4, inventoryIngredients.getMaterialMap().get(Material.COBBLESTONE));
    }

    @Test
    public void setCraftableAmountJustSetValueIfResourceMapNull() {
        InventoryIngredients inventoryIngredients = new InventoryIngredients();
        inventoryIngredients.setCraftableAmount(32);
        assertEquals(32, inventoryIngredients.getCraftableAmount());
        inventoryIngredients.setCraftableAmount(0);
        assertEquals(0, inventoryIngredients.getCraftableAmount());
    }

    @Test
    public void setCraftableAmountAdjustsResourceMap() {
        Map<RecipeChoice.MaterialChoice, List<Material>> resourceMap = new HashMap<>();
        resourceMap.put(materialChoiceMock1, new ArrayList<>(Arrays.asList(Material.COBBLESTONE, Material.COBBLESTONE)));
        resourceMap.put(materialChoiceMock2, new ArrayList<>(Arrays.asList(Material.COBBLESTONE, Material.COBBLESTONE)));

        Map<RecipeChoice.MaterialChoice, Integer> multiplicity = new HashMap<>();
        multiplicity.put(materialChoiceMock1, 1);
        multiplicity.put(materialChoiceMock2, 1);

        InventoryIngredients inventoryIngredients = new InventoryIngredients(2, resourceMap, multiplicity);
        inventoryIngredients.setCraftableAmount(1);
        assertEquals(1, inventoryIngredients.getResourceMap().get(materialChoiceMock1).size());
        assertEquals(1, inventoryIngredients.getResourceMap().get(materialChoiceMock2).size());
    }
}
