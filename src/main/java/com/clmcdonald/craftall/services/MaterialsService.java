package com.clmcdonald.craftall.services;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class MaterialsService {
    public static String standardizeName(String materialName) {
        Optional<Path> itemsJsonPath = getItemsJsonPath();

        if(itemsJsonPath.isPresent()) {
            JsonObject itemsJsonObject = getItemsJsonFromFile(itemsJsonPath.get()).getAsJsonObject();

            Optional<JsonElement> optionalJsonMaterial = itemsJsonObject.entrySet().stream()
                    .filter((entry) -> entry.getKey().equalsIgnoreCase(materialName))
                    .map(Map.Entry::getValue)
                    .findFirst();

            if(optionalJsonMaterial.isPresent()) {
                JsonElement jsonMaterial = optionalJsonMaterial.get();
                if(jsonMaterial.isJsonObject()) {
                    JsonObject jsonMaterialObject = jsonMaterial.getAsJsonObject();
                    return jsonMaterialObject.get("material").getAsString();
                } else {
                    assert jsonMaterial.isJsonPrimitive();
                    assert jsonMaterial.getAsJsonPrimitive().isString();

                    return standardizeName(jsonMaterial.getAsString());
                }
            }
        }

        return materialName.toUpperCase();
    }

    public static Set<String> retrieveMaterials() {
        Set<String> materials = new HashSet<>();

        Optional<Path> itemsJsonPath = getItemsJsonPath();
        if(itemsJsonPath.isPresent() && Files.exists(itemsJsonPath.get())) {
            JsonObject jsonObject = getItemsJsonFromFile(itemsJsonPath.get()).getAsJsonObject();
            materials.addAll(jsonObject.entrySet().stream()
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet()));
        }

        materials.addAll(Arrays.stream(Material.values())
                .map(Material::name)
                .map(String::toLowerCase)
                .collect(Collectors.toSet()));

        return materials;
    }

    private static Optional<Path> getItemsJsonPath() {
        Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
        if(essentials != null) {
            return Optional.of(essentials.getDataFolder().toPath().resolve("items.json"));
        } else {
            return Optional.empty();
        }
    }

    private static JsonElement getItemsJsonFromFile(Path jsonPath) {
        try {
            String joined = Files.lines(jsonPath)
                    .filter((line) -> !line.trim().startsWith("#"))
                    .collect(Collectors.joining());
            return new JsonParser().parse(joined);
        } catch (IOException e) {
            return new JsonParser().parse("{}");
        }
    }
}
