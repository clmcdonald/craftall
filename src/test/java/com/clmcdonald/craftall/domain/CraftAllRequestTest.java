package com.clmcdonald.craftall.domain;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CraftAllRequestTest {

    private Material material;
    private Integer maximum;

    @Mock
    private CraftAllInventory craftAllInventoryMock;
    @Mock
    private Recipe recipeMock1;
    @Mock
    private Recipe recipeMock2;
    @Mock
    private InventoryIngredients inventoryIngredients1;
    @Mock
    private InventoryIngredients inventoryIngredients2;
    @Mock
    private ItemStack itemStackMock;

    @BeforeEach
    public void beforeEach() {
        material = Material.COBBLESTONE;
        maximum = 10;
    }

    @Test
    public void createShouldReturnRequestWithNoRecipeIfEmptyList() {
        CraftAllRequest request = CraftAllRequest.create(Collections.emptyList(), craftAllInventoryMock, material, maximum);
        assertNull(request.getRecipe());
        assertEquals(material, request.getMaterial());
        assertEquals(maximum, request.getMaximum());
    }

    @Test
    public void createShouldReturnMaximumNumberCraftable() {
        try (MockedStatic<InventoryIngredients> inventoryIngredientsStaticMock = mockStatic(InventoryIngredients.class)) {
            inventoryIngredientsStaticMock.when(() -> InventoryIngredients.create(craftAllInventoryMock, recipeMock1)).thenReturn(inventoryIngredients1);
            inventoryIngredientsStaticMock.when(() -> InventoryIngredients.create(craftAllInventoryMock, recipeMock2)).thenReturn(inventoryIngredients2);

            when(inventoryIngredients1.getCraftableAmount()).thenReturn(8, 6);
            when(inventoryIngredients2.getCraftableAmount()).thenReturn(11, 9);
            when(craftAllInventoryMock.canFit(material, inventoryIngredients1)).thenReturn(false, true);
            when(craftAllInventoryMock.canFit(material, inventoryIngredients2)).thenReturn(true, true);
            when(recipeMock1.getResult()).thenReturn(itemStackMock);
            when(recipeMock2.getResult()).thenReturn(itemStackMock);
            when(itemStackMock.getAmount()).thenReturn(2);

            CraftAllRequest request = CraftAllRequest.create(Arrays.asList(recipeMock1, recipeMock2), craftAllInventoryMock, material, maximum);

            verify(inventoryIngredients1).setCraftableAmount(6-2);
            verify(inventoryIngredients2).setCraftableAmount(9-2);

            assertEquals(recipeMock2, request.getRecipe());
        }
    }

    @Test
    public void createShouldSetUnlimitedMaximumIfNegative() {
        maximum = -1;

        try (MockedStatic<InventoryIngredients> inventoryIngredientsStaticMock = mockStatic(InventoryIngredients.class)) {
            inventoryIngredientsStaticMock.when(() -> InventoryIngredients.create(craftAllInventoryMock, recipeMock1)).thenReturn(inventoryIngredients1);

            when(inventoryIngredients1.getCraftableAmount()).thenReturn(1000);
            when(craftAllInventoryMock.canFit(material, inventoryIngredients1)).thenReturn(true);

            CraftAllRequest request = CraftAllRequest.create(Collections.singletonList(recipeMock1), craftAllInventoryMock, material, maximum);

            verify(inventoryIngredients1, times(0)).setCraftableAmount(anyInt());

            assertEquals(recipeMock1, request.getRecipe());
        }
    }
}
