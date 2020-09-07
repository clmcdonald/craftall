package integration;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.entity.PlayerMock;
import com.clmcdonald.craftall.CraftAll;
import com.clmcdonald.craftall.commands.CraftAllCommand;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.inventory.*;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class CraftAllIntegrationTest {

    private ServerMock serverMock;
    private PlayerMock playerMock;
    private CraftAll craftAll;

    @Mock
    Command commandMock;

    @BeforeEach
    public void beforeEach() {
        serverMock = MockBukkit.mock(new ServerMock());
        craftAll = MockBukkit.load(CraftAll.class);
        playerMock = serverMock.addPlayer();
    }

    @AfterEach
    public void afterEach() {
        MockBukkit.unmock();
    }

    @Test
    public void shouldCraftOneLogIntoPlanks() {
        serverMock.addRecipe(planksRecipe(craftAll));

        List<ItemStack> inventory = new ArrayList<>();
        inventory.add(new ItemStack(Material.OAK_LOG, 1));

        String label = "ca";
        String[] args = new String[] {"oak_planks"};

        List<ItemStack> result = new ArrayList<>();
        result.add(new ItemStack(Material.OAK_PLANKS, 4));

        integrationTest(inventory, label, args, result);
    }

    @Test
    public void shouldCraftTwoLogIntoPlanks() {
        serverMock.addRecipe(planksRecipe(craftAll));

        List<ItemStack> inventory = new ArrayList<>();
        inventory.add(new ItemStack(Material.OAK_LOG, 2));

        String label = "ca";
        String[] args = new String[] {"oak_planks"};

        List<ItemStack> result = new ArrayList<>();
        result.add(new ItemStack(Material.OAK_PLANKS, 8));

        integrationTest(inventory, label, args, result);
    }

    @Test
    public void shouldCraftSnowBlockFromSnowballs() {
        serverMock.addRecipe(snowBlockRecipe(craftAll));

        List<ItemStack> inventory = new ArrayList<>();
        inventory.add(new ItemStack(Material.SNOWBALL, 4));

        String label = "ca";
        String[] args = new String[] {"snow_block"};

        List<ItemStack> result = new ArrayList<>();
        result.add(new ItemStack(Material.SNOW_BLOCK, 1));

        integrationTest(inventory, label, args, result);
    }

    @Test
    public void shouldCraftTwoSnowBlocksFromSnowballs() {
        serverMock.addRecipe(snowBlockRecipe(craftAll));

        List<ItemStack> inventory = new ArrayList<>();
        inventory.add(new ItemStack(Material.SNOWBALL, 8));

        String label = "ca";
        String[] args = new String[] {"snow_block"};

        List<ItemStack> result = new ArrayList<>();
        result.add(new ItemStack(Material.SNOW_BLOCK, 2));

        integrationTest(inventory, label, args, result);
    }

    @Test
    public void shouldLeaveLeftoversAlone() {
        serverMock.addRecipe(snowBlockRecipe(craftAll));

        List<ItemStack> inventory = new ArrayList<>();
        inventory.add(new ItemStack(Material.SNOWBALL, 11));

        String label = "ca";
        String[] args = new String[] {"snow_block"};

        List<ItemStack> result = new ArrayList<>();
        result.add(new ItemStack(Material.SNOW_BLOCK, 2));
        result.add(new ItemStack(Material.SNOWBALL, 3));

        integrationTest(inventory, label, args, result);
    }

    @Test
    public void shouldNotCraftAnyIfThereIsNoSpace() {
        serverMock.addRecipe(snowBlockRecipe(craftAll));

        List<ItemStack> inventory = new ArrayList<>();
        IntStream.range(0, 35).forEach((index) -> playerMock.getInventory().setItem(index, new ItemStack(Material.COBBLESTONE, 1)));
        inventory.add(new ItemStack(Material.SNOWBALL, 5));

        String label = "ca";
        String[] args = new String[] {"snow_block"};

        List<ItemStack> result = new ArrayList<>();
        result.add(new ItemStack(Material.SNOWBALL, 5));

        integrationTest(inventory, label, args, result);
    }

    private void integrationTest(List<ItemStack> inventory, String label, String[] args, List<ItemStack> result) {
        PlayerInventory playerInventory = playerMock.getInventory();
        inventory.forEach((playerInventory::addItem));

        CraftAllCommand craftAllCommand = new CraftAllCommand();

        craftAllCommand.onCommand(playerMock, commandMock, label, args);

        result.forEach((itemStack -> assertTrue(inventoryContains(playerInventory, itemStack))));
    }

    private boolean inventoryContains(PlayerInventory inventory, ItemStack itemStack) {
        return Arrays.asList(inventory.getStorageContents()).contains(itemStack);
    }

    private ShapelessRecipe planksRecipe(Plugin plugin) {
        ItemStack result = new ItemStack(Material.OAK_PLANKS, 4);
        ShapelessRecipe shapelessRecipe = new ShapelessRecipe(new NamespacedKey(plugin, "OAK_PLANKS"), result);
        shapelessRecipe.addIngredient(new RecipeChoice.MaterialChoice(Material.OAK_LOG));
        return shapelessRecipe;
    }

    private ShapelessRecipe snowBlockRecipe(Plugin plugin) {
        ItemStack result = new ItemStack(Material.SNOW_BLOCK, 1);
        ShapelessRecipe shapelessRecipe = new ShapelessRecipe(new NamespacedKey(plugin, "SNOW_BLOCK"), result);
        shapelessRecipe.addIngredient(new RecipeChoice.MaterialChoice(Material.SNOWBALL));
        shapelessRecipe.addIngredient(new RecipeChoice.MaterialChoice(Material.SNOWBALL));
        shapelessRecipe.addIngredient(new RecipeChoice.MaterialChoice(Material.SNOWBALL));
        shapelessRecipe.addIngredient(new RecipeChoice.MaterialChoice(Material.SNOWBALL));
        return shapelessRecipe;
    }
}
