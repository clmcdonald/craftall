package integration;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class ServerMock extends be.seeseemelk.mockbukkit.ServerMock {
    private final List<Recipe> recipes;

    public ServerMock() {
        super();

        this.recipes = new ArrayList<>();
    }

    @Override
    public boolean addRecipe(Recipe recipe) {
        assertMainThread();
        recipes.add(recipe);
        return true;
    }

    @Override
    public List<Recipe> getRecipesFor(ItemStack result) {
        assertMainThread();
        return recipes.stream().filter(recipe -> recipe.getResult().isSimilar(result)).collect(Collectors.toList());
    }

    @Override
    public Iterator<Recipe> recipeIterator() {
        assertMainThread();
        return recipes.iterator();
    }

    @Override
    public void clearRecipes() {
        assertMainThread();
        recipes.clear();
    }

    @Override
    public Recipe getRecipe(NamespacedKey key) {
        assertMainThread();

        for (Recipe recipe : recipes) {
            if (recipe instanceof Keyed && ((Keyed) recipe).getKey().equals(key)) {
                return recipe;
            }
        }

        return null;
    }
}
