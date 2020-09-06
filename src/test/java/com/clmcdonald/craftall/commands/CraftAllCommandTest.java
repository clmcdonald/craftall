package com.clmcdonald.craftall.commands;

import be.seeseemelk.mockbukkit.MockBukkit;
import com.clmcdonald.craftall.CraftAll;
import com.clmcdonald.craftall.domain.CraftAllInventory;
import com.clmcdonald.craftall.domain.CraftAllPlayer;
import com.clmcdonald.craftall.domain.CraftAllRequest;
import com.clmcdonald.craftall.domain.InventoryIngredients;
import com.clmcdonald.craftall.services.MaterialsService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CraftAllCommandTest {

    private String label;
    private String[] args;
    private String materialArg;
    private String maximumArg;
    private String standardized;
    private Material material;
    private List<Recipe> recipeList;
    private ItemStack[] itemStacks;
    private Map<Material, Integer> materialMap;

    @Mock
    private CommandSender commandSenderMock;
    @Mock
    private Command commandMock;
    @Mock
    private CraftAllPlayer craftAllPlayerMock;
    @Mock
    private Server serverMock;
    @Mock
    private Recipe recipeMock;
    @Mock
    private CraftAllRequest craftAllRequestMock;
    @Mock
    private CraftAllInventory craftAllInventoryMock;
    @Mock
    private InventoryIngredients inventoryIngredientsMock;
    @Mock
    private ItemStack itemStackMock;
    @Mock
    private Inventory inventoryMock;

    @Captor
    ArgumentCaptor<ItemStack> itemStackCaptor;
    @Captor
    ArgumentCaptor<ItemStack[]> itemStackArrayCaptor;

    private CraftAllCommand craftAllCommand;

    @BeforeEach
    public void beforeEach() {
        label = "test-label";
        materialArg = "test-arg";
        maximumArg = "-1";
        args = new String[] {materialArg, maximumArg};
        standardized = "test-standardized";
        material = Material.COBBLESTONE;
        recipeList = Collections.singletonList(recipeMock);
        itemStacks = new ItemStack[] {itemStackMock};
        materialMap = new HashMap<>();

        MockBukkit.mock();
        CraftAll craftAll = MockBukkit.load(CraftAll.class);
        PluginCommand command = craftAll.getCommand("craftall");
        assertNotNull(command);
        assertTrue(command.getExecutor() instanceof CraftAllCommand);
        craftAllCommand = (CraftAllCommand) command.getExecutor();
    }

    @AfterEach
    public void afterEach() {
        MockBukkit.unmock();
    }

    @Test
    public void onPlayerCommandShouldStandardizeAndRetrieveMaterials() {
        setUpOnPlayerCommand((mockedStaticMap) -> {
            itemStacks = new ItemStack[] {itemStackMock, itemStackMock};
            materialMap.put(material, 10);

            mockedStaticMap.get(MaterialsService.class).when(() -> MaterialsService.standardizeName(materialArg)).thenReturn(standardized);
            mockedStaticMap.get(Material.class).when(() -> Material.getMaterial(standardized)).thenReturn(material);
            mockedStaticMap.get(Bukkit.class).when(Bukkit::getServer).thenReturn(serverMock);
            mockedStaticMap.get(CraftAllRequest.class).when(() -> CraftAllRequest.create(recipeList, craftAllInventoryMock, material, Integer.parseInt(maximumArg))).thenReturn(craftAllRequestMock);

            when(serverMock.getRecipesFor(any(ItemStack.class))).thenReturn(recipeList);
            when(craftAllPlayerMock.getInventory()).thenReturn(craftAllInventoryMock);
            when(craftAllRequestMock.getInventoryIngredients()).thenReturn(inventoryIngredientsMock);
            when(craftAllRequestMock.getMaterial()).thenReturn(material);
            when(inventoryIngredientsMock.getMaterialMap()).thenReturn(materialMap);
            when(craftAllInventoryMock.getBukkitInventory()).thenReturn(inventoryMock);
            when(inventoryMock.getStorageContents()).thenReturn(itemStacks);
            when(itemStackMock.getAmount()).thenReturn(5);
            when(itemStackMock.getType()).thenReturn(material);

            craftAllCommand.onPlayerCommand(craftAllPlayerMock, commandMock, label, args);

            verify(serverMock).getRecipesFor(itemStackCaptor.capture());

            ItemStack itemStack = itemStackCaptor.getValue();
            assertEquals(material, itemStack.getType());
        });
    }

    @Test
    public void onPlayerCommandShouldDefaultMaximumToInfinite() {
        setUpOnPlayerCommand((mockedStaticMap) -> {
            // Maximum arg is not passed
            args = new String[] {materialArg};
            itemStacks = new ItemStack[] {itemStackMock, itemStackMock, null};
            materialMap.put(material, 10);

            mockedStaticMap.get(MaterialsService.class).when(() -> MaterialsService.standardizeName(materialArg)).thenReturn(standardized);
            mockedStaticMap.get(Material.class).when(() -> Material.getMaterial(standardized)).thenReturn(material);
            mockedStaticMap.get(Bukkit.class).when(Bukkit::getServer).thenReturn(serverMock);
            // CraftAllRequest is only created if the maximum is passed as -1
            mockedStaticMap.get(CraftAllRequest.class).when(() -> CraftAllRequest.create(recipeList, craftAllInventoryMock, material, -1)).thenReturn(craftAllRequestMock);

            when(serverMock.getRecipesFor(any(ItemStack.class))).thenReturn(recipeList);
            when(craftAllPlayerMock.getInventory()).thenReturn(craftAllInventoryMock);
            when(craftAllRequestMock.getInventoryIngredients()).thenReturn(inventoryIngredientsMock);
            when(craftAllRequestMock.getMaterial()).thenReturn(material);
            when(inventoryIngredientsMock.getMaterialMap()).thenReturn(materialMap);
            when(craftAllInventoryMock.getBukkitInventory()).thenReturn(inventoryMock);
            when(inventoryMock.getStorageContents()).thenReturn(itemStacks);
            when(itemStackMock.getAmount()).thenReturn(5);
            when(itemStackMock.getType()).thenReturn(material);

            craftAllCommand.onPlayerCommand(craftAllPlayerMock, commandMock, label, args);
            // if we get to here then the CraftAllRequest must have been created successfully
        });
    }

    @Test
    public void onPlayerCommandShouldBuildItemStacksForResult() {
        setUpOnPlayerCommand((mockedStaticMap) -> {
            args = new String[] {materialArg, maximumArg};
            itemStacks = new ItemStack[] {itemStackMock, itemStackMock, null};
            materialMap.put(material, 10);

            mockedStaticMap.get(MaterialsService.class).when(() -> MaterialsService.standardizeName(materialArg)).thenReturn(standardized);
            mockedStaticMap.get(Material.class).when(() -> Material.getMaterial(standardized)).thenReturn(material);
            mockedStaticMap.get(Bukkit.class).when(Bukkit::getServer).thenReturn(serverMock);
            mockedStaticMap.get(CraftAllRequest.class).when(() -> CraftAllRequest.create(recipeList, craftAllInventoryMock, material, Integer.parseInt(maximumArg))).thenReturn(craftAllRequestMock);

            when(serverMock.getRecipesFor(any(ItemStack.class))).thenReturn(recipeList);
            when(craftAllPlayerMock.getInventory()).thenReturn(craftAllInventoryMock);
            when(craftAllRequestMock.getInventoryIngredients()).thenReturn(inventoryIngredientsMock);
            when(craftAllRequestMock.getMaterial()).thenReturn(material);
            when(inventoryIngredientsMock.getMaterialMap()).thenReturn(materialMap);
            when(craftAllInventoryMock.getBukkitInventory()).thenReturn(inventoryMock);
            when(inventoryMock.getStorageContents()).thenReturn(itemStacks);
            when(itemStackMock.getAmount()).thenReturn(5);
            when(itemStackMock.getType()).thenReturn(material);
            when(inventoryIngredientsMock.getCraftableAmount()).thenReturn(material.getMaxStackSize() + 1);

            craftAllCommand.onPlayerCommand(craftAllPlayerMock, commandMock, label, args);

            verify(inventoryMock).addItem(itemStackCaptor.capture());
            List<ItemStack> itemStackList = itemStackCaptor.getAllValues();
            assertEquals(2, itemStackList.size());
            assertEquals(material.getMaxStackSize(), itemStackList.get(0).getAmount());
            assertEquals(1, itemStackList.get(1).getAmount());
        });
    }

    @Test
    public void onPlayerCommandShouldSendUsageForNoArgs() {
        setUpOnPlayerCommand((mockedStaticMap) -> {
            args = new String[] {};

            when(commandMock.getUsage()).thenReturn("/<command>");

            craftAllCommand.onPlayerCommand(craftAllPlayerMock, commandMock, label, args);

            verify(craftAllPlayerMock).sendMessage(anyString(), eq("/" + label));
        });
    }

    @Test
    public void onPlayerCommandShouldGetErrorForInvalidMaterialArg() {
        setUpOnPlayerCommand((mockedStaticMap) -> {
            mockedStaticMap.get(MaterialsService.class).when(() -> MaterialsService.standardizeName(materialArg)).thenReturn(standardized);
            mockedStaticMap.get(Material.class).when(() -> Material.getMaterial(standardized)).thenReturn(null);

            when(commandMock.getUsage()).thenReturn("/<command>");

            craftAllCommand.onPlayerCommand(craftAllPlayerMock, commandMock, label, args);

            verify(craftAllPlayerMock).sendMessage(anyString(), eq(materialArg));
            verify(craftAllPlayerMock).sendMessage(anyString(), eq("/" + label));
        });
    }

    @Test
    public void onPlayerCommandShouldGetErrorForInvalidMaximumArg() {
        setUpOnPlayerCommand((mockedStaticMap) -> {
            maximumArg = "not-a-number";
            args = new String[] {materialArg, maximumArg};

            mockedStaticMap.get(MaterialsService.class).when(() -> MaterialsService.standardizeName(materialArg)).thenReturn(standardized);
            mockedStaticMap.get(Material.class).when(() -> Material.getMaterial(standardized)).thenReturn(material);

            when(commandMock.getUsage()).thenReturn("/<command>");

            craftAllCommand.onPlayerCommand(craftAllPlayerMock, commandMock, label, args);

            verify(craftAllPlayerMock).sendMessage(anyString(), eq(maximumArg));
            verify(craftAllPlayerMock).sendMessage(anyString(), eq("/" + label));
        });
    }

    @Test
    public void onPlayerCommandShouldGetErrorForTooManyArgs() {
        setUpOnPlayerCommand((mockedStaticMap) -> {
            args = new String[] {"too", "many", "args"};

            when(commandMock.getUsage()).thenReturn("/<command>");

            craftAllCommand.onPlayerCommand(craftAllPlayerMock, commandMock, label, args);

            verify(craftAllPlayerMock).sendMessage(anyString(), eq("/" + label));
        });
    }

    @Test
    public void onConsoleCommandShouldReject() {
        boolean result = craftAllCommand.onConsoleCommand(commandSenderMock, commandMock, "", new String[] {});

        verify(commandSenderMock).sendMessage(anyString());
        assertTrue(result);
    }

    private void setUpOnPlayerCommand(Consumer<Map<Class<?>, MockedStatic<?>>> test) {
        try (
                MockedStatic<MaterialsService> materialsServiceStaticMock = mockStatic(MaterialsService.class);
                MockedStatic<Material> materialStaticMock = mockStatic(Material.class);
                MockedStatic<Bukkit> bukkitStaticMock = mockStatic(Bukkit.class);
                MockedStatic<CraftAllRequest> craftAllRequestStaticMock = mockStatic(CraftAllRequest.class)
        ) {
            Map<Class<?>, MockedStatic<?>> mockedStaticMap = new HashMap<>();
            mockedStaticMap.put(MaterialsService.class, materialsServiceStaticMock);
            mockedStaticMap.put(Material.class, materialStaticMock);
            mockedStaticMap.put(Bukkit.class, bukkitStaticMock);
            mockedStaticMap.put(CraftAllRequest.class, craftAllRequestStaticMock);
            test.accept(mockedStaticMap);
        }
    }
}