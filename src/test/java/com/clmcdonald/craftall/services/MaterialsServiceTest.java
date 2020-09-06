package com.clmcdonald.craftall.services;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MaterialsServiceTest {

    @Mock
    private PluginManager pluginManagerMock;
    @Mock
    private Plugin pluginMock;
    @Mock
    private JsonElement jsonElementMock1;
    @Mock
    private JsonElement jsonElementMock2;
    @Mock
    private JsonElement jsonElementMock3;
    @Mock
    private JsonObject jsonObjectMock1;
    @Mock
    private JsonObject jsonObjectMock2;
    @Mock
    private JsonPrimitive jsonPrimitiveMock;

    @Test
    public void standardizeNameShouldReturnParameterIfNoItemsJsonPresent() {
        try (MockedStatic<Bukkit> bukkitStaticMock = mockStatic(Bukkit.class)) {
            bukkitStaticMock.when(Bukkit::getPluginManager).thenReturn(pluginManagerMock);

            when(pluginManagerMock.getPlugin("Essentials")).thenReturn(null);

            String parameter = "test-parameter";
            String standardized = MaterialsService.standardizeName(parameter);
            assertEquals(parameter.toUpperCase(), standardized);
        }
    }

    @Test
    public void standardizeNameShouldReturnNameFromJson() {
        String actualName = "actual";
        String materialName = "material";

        try (
                MockedStatic<Bukkit> bukkitStaticMock = mockStatic(Bukkit.class);
                MockedStatic<Files> filesStaticMock = mockStatic(Files.class)
        ) {
            bukkitStaticMock.when(Bukkit::getPluginManager).thenReturn(pluginManagerMock);
            filesStaticMock.when(() -> Files.lines(any(Path.class))).thenAnswer((inv) -> Stream.of(
                    "# test comment\n",
                    "{\n",
                    "    \"nick\": \"actual\",\n",
                    "    \"actual\": {\n",
                    "        \"material\": \"material\"\n",
                    "    }\n",
                    "}\n"));

            when(pluginManagerMock.getPlugin("Essentials")).thenReturn(pluginMock);
            when(pluginMock.getDataFolder()).thenReturn(new File("/"));

            String standardized = MaterialsService.standardizeName(actualName);
            assertEquals(materialName, standardized);
        }
    }

    @Test
    public void standardizeNameShouldReturnNickNameFromJson() {
        String nickname = "nick";
        String materialName = "material";

        try (
                MockedStatic<Bukkit> bukkitStaticMock = mockStatic(Bukkit.class);
                MockedStatic<Files> filesStaticMock = mockStatic(Files.class)
        ) {
            bukkitStaticMock.when(Bukkit::getPluginManager).thenReturn(pluginManagerMock);
            filesStaticMock.when(() -> Files.lines(any(Path.class))).thenAnswer((inv) -> Stream.of(
                    "# test comment\n",
                    "{\n",
                    "    \"nick\": \"actual\",\n",
                    "    \"actual\": {\n",
                    "        \"material\": \"material\"\n",
                    "    }\n",
                    "}\n"));

            when(pluginManagerMock.getPlugin("Essentials")).thenReturn(pluginMock);
            when(pluginMock.getDataFolder()).thenReturn(new File("/"));

            String standardized = MaterialsService.standardizeName(nickname);
            assertEquals(materialName, standardized);
        }
    }

    @Test
    public void standardizeNameShouldReturnParameterOnIOException() {
        String nickname = "nick";

        try (
                MockedStatic<Bukkit> bukkitStaticMock = mockStatic(Bukkit.class);
                MockedStatic<Files> filesStaticMock = mockStatic(Files.class)
        ) {
            bukkitStaticMock.when(Bukkit::getPluginManager).thenReturn(pluginManagerMock);
            filesStaticMock.when(() -> Files.lines(any(Path.class))).thenThrow(new IOException());

            when(pluginManagerMock.getPlugin("Essentials")).thenReturn(pluginMock);
            when(pluginMock.getDataFolder()).thenReturn(new File("/"));

            String standardized = MaterialsService.standardizeName(nickname);
            assertEquals(nickname.toUpperCase(), standardized);
        }
    }

    @Test
    public void retrieveMaterialsShouldReturnAllKeys() {
        Material material = Material.COBBLESTONE;

        try (
                MockedStatic<Bukkit> bukkitStaticMock = mockStatic(Bukkit.class);
                MockedStatic<Files> filesStaticMock = mockStatic(Files.class);
                MockedStatic<Material> materialStaticMock = mockStatic(Material.class)
        ) {
            bukkitStaticMock.when(Bukkit::getPluginManager).thenReturn(pluginManagerMock);
            filesStaticMock.when(() -> Files.lines(any(Path.class))).thenAnswer((inv) -> Stream.of(
                    "# test comment\n",
                    "{\n",
                    "    \"nick\": \"actual\",\n",
                    "    \"actual\": {\n",
                    "        \"material\": \"material\"\n",
                    "    }\n",
                    "}\n"));
            filesStaticMock.when(() -> Files.exists(any(Path.class))).thenReturn(true);
            materialStaticMock.when(Material::values).thenReturn(new Material[] {material});

            when(pluginManagerMock.getPlugin("Essentials")).thenReturn(pluginMock);
            when(pluginMock.getDataFolder()).thenReturn(new File("/"));

            Set<String> materials = MaterialsService.retrieveMaterials();
            assertEquals(Arrays.asList("nick", "actual", material.name().toLowerCase()), new ArrayList<>(materials));
        }
    }
}
